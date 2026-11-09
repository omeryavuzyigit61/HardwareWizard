package com.panda.myapplication.data.repository

import com.panda.myapplication.data.remote.SupabaseConfig
import com.panda.myapplication.data.remote.SupabaseConfig.applySupabaseHeaders
import com.panda.myapplication.domain.common.DomainResult
import com.panda.myapplication.domain.model.ContentReport
import com.panda.myapplication.domain.model.ReportReason
import com.panda.myapplication.domain.model.ReportStatus
import com.panda.myapplication.domain.model.ReportTargetType
import com.panda.myapplication.domain.repository.CommunityReportRepository
import io.ktor.client.HttpClient
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.patch
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.booleanOrNull
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.longOrNull
import kotlinx.serialization.json.put
import kotlin.time.Duration.Companion.milliseconds

internal class CommunityReportRepositoryImpl(
    private val httpClient: HttpClient? = null,
    private val json: Json? = null,
    private val ioDispatcher: CoroutineDispatcher,
) : CommunityReportRepository {

    private val _reports = MutableStateFlow<List<ContentReport>>(emptyList())

    init {
        CoroutineScope(ioDispatcher).launch {
            fetchRemoteReports()
            while (true) {
                delay(15000.milliseconds)
                if (_reports.subscriptionCount.value > 0) {
                    fetchRemoteReports()
                }
            }
        }
    }

    private suspend fun fetchRemoteReports() {
        val client = httpClient ?: return
        val jsonParser = json ?: return

        runCatching {
            val url = "${SupabaseConfig.restApiUrl}/${SupabaseConfig.TABLE_HARDWARE_PRO_TIPS}?hardware_id=like.REPORT:*&order=created_at_epoch_ms.desc"
            val response = client.get(url) {
                applySupabaseHeaders()
            }
            if (response.status == HttpStatusCode.OK) {
                val array = jsonParser.parseToJsonElement(response.bodyAsText()).jsonArray
                val remoteList = array.mapNotNull { elem ->
                    val obj = elem.jsonObject
                    val id = obj["id"]?.jsonPrimitive?.content ?: return@mapNotNull null
                    val hwId = obj["hardware_id"]?.jsonPrimitive?.content ?: ""
                    val parts = hwId.split(":")
                    val targetTypeStr = parts.getOrNull(1) ?: "TUNING_PROFILE"
                    val targetId = parts.drop(2).joinToString(":")
                    val targetTitle = obj["hardware_name"]?.jsonPrimitive?.content ?: "İçerik"
                    val reporterId = obj["user_id"]?.jsonPrimitive?.content ?: ""
                    val reporterName = obj["user_display_name"]?.jsonPrimitive?.content ?: "Kullanıcı"
                    val statusStr = obj["status"]?.jsonPrimitive?.content ?: "PENDING"
                    val tipText = obj["tip_text"]?.jsonPrimitive?.content ?: ""
                    val epoch = obj["created_at_epoch_ms"]?.jsonPrimitive?.longOrNull ?: 0L

                    var reportedUserId = ""
                    var reportedUserName = ""
                    var isSelf = false
                    var reason = ReportReason.OFFENSIVE_OR_INAPPROPRIATE
                    var details = tipText

                    runCatching {
                        val parsed = jsonParser.parseToJsonElement(tipText).jsonObject
                        details = parsed["details"]?.jsonPrimitive?.content ?: tipText
                        reportedUserId = parsed["reportedUserId"]?.jsonPrimitive?.content ?: ""
                        reportedUserName = parsed["reportedUserDisplayName"]?.jsonPrimitive?.content ?: ""
                        isSelf = parsed["isSelfReport"]?.jsonPrimitive?.booleanOrNull ?: false
                        val reasonStr = parsed["reason"]?.jsonPrimitive?.content
                        if (reasonStr != null) {
                            reason = runCatching { ReportReason.valueOf(reasonStr) }.getOrDefault(ReportReason.OFFENSIVE_OR_INAPPROPRIATE)
                        }
                    }

                    val targetType = when (targetTypeStr.uppercase()) {
                        "BATTLESTATION" -> ReportTargetType.BATTLESTATION
                        "TUNING", "TUNING_PROFILE" -> ReportTargetType.TUNING_PROFILE
                        "PRICE_DEAL" -> ReportTargetType.PRICE_DEAL
                        else -> ReportTargetType.COMMENT
                    }

                    val reportStatus = when (statusStr.uppercase()) {
                        "APPROVED" -> ReportStatus.RESOLVED_DISMISSED
                        "REJECTED" -> ReportStatus.RESOLVED_REMOVED
                        else -> ReportStatus.PENDING
                    }

                    ContentReport(
                        id = id,
                        targetType = targetType,
                        targetId = targetId,
                        targetTitle = targetTitle,
                        reportedUserId = reportedUserId,
                        reportedUserDisplayName = reportedUserName,
                        reporterUserId = reporterId,
                        reporterUserDisplayName = reporterName,
                        isSelfReport = isSelf,
                        reason = reason,
                        details = details,
                        timestampMillis = epoch,
                        status = reportStatus,
                    )
                }

                _reports.value = remoteList
            }
        }
    }

    override fun observePendingReports(): Flow<List<ContentReport>> {
        return _reports.map { list -> list.filter { it.status == ReportStatus.PENDING } }
    }

    override fun observeAllReports(): Flow<List<ContentReport>> {
        return _reports.asStateFlow()
    }

    override suspend fun submitReport(report: ContentReport): DomainResult<Unit> = withContext(ioDispatcher) {
        _reports.update { current ->
            listOf(report) + current.filterNot { it.id == report.id }
        }

        val client = httpClient
        val jsonParser = json
        if (client != null && jsonParser != null) {
            runCatching {
                val detailsJson = buildJsonObject {
                    put("details", report.details)
                    put("reportedUserId", report.reportedUserId)
                    put("reportedUserDisplayName", report.reportedUserDisplayName)
                    put("isSelfReport", report.isSelfReport)
                    put("reason", report.reason.name)
                }.toString()

                val payload = buildJsonObject {
                    put("id", report.id)
                    put("hardware_id", "REPORT:${report.targetType.name}:${report.targetId}")
                    put("hardware_name", report.targetTitle)
                    put("user_id", report.reporterUserId)
                    put("user_display_name", report.reporterUserDisplayName)
                    put("user_badge", report.reason.name)
                    put("tip_text", detailsJson)
                    put("upvotes_count", 0)
                    put("status", "PENDING")
                    put("created_at_epoch_ms", report.timestampMillis)
                }

                val url = "${SupabaseConfig.restApiUrl}/${SupabaseConfig.TABLE_HARDWARE_PRO_TIPS}"
                client.post(url) {
                    applySupabaseHeaders()
                    contentType(ContentType.Application.Json)
                    setBody(payload.toString())
                }
            }
        }
        DomainResult.Success(Unit)
    }

    override suspend fun resolveReport(reportId: String, newStatus: ReportStatus): DomainResult<Unit> = withContext(ioDispatcher) {
        _reports.update { current ->
            current.map { r ->
                if (r.id == reportId) r.copy(status = newStatus) else r
            }
        }

        val client = httpClient
        if (client != null) {
            runCatching {
                val sbStatus = if (newStatus == ReportStatus.RESOLVED_REMOVED) "REJECTED" else "APPROVED"
                val payload = buildJsonObject { put("status", sbStatus) }
                val url = "${SupabaseConfig.restApiUrl}/${SupabaseConfig.TABLE_HARDWARE_PRO_TIPS}?id=eq.$reportId"
                client.patch(url) {
                    applySupabaseHeaders()
                    contentType(ContentType.Application.Json)
                    setBody(payload.toString())
                }
            }
        }
        DomainResult.Success(Unit)
    }

    override suspend fun deleteReport(reportId: String): DomainResult<Unit> = withContext(ioDispatcher) {
        _reports.update { current ->
            current.filterNot { it.id == reportId }
        }
        val client = httpClient
        if (client != null) {
            runCatching {
                val url = "${SupabaseConfig.restApiUrl}/${SupabaseConfig.TABLE_HARDWARE_PRO_TIPS}?id=eq.$reportId"
                client.delete(url) {
                    applySupabaseHeaders()
                }
            }
        }
        DomainResult.Success(Unit)
    }
}
