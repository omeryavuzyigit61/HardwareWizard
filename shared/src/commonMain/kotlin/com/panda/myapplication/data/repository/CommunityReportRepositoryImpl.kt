package com.panda.myapplication.data.repository

import com.panda.myapplication.data.remote.SupabaseConfig
import com.panda.myapplication.data.remote.SupabaseConfig.applySupabaseHeaders
import com.panda.myapplication.domain.common.DomainError
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
import io.ktor.http.isSuccess
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
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
    private val repoScope = CoroutineScope(SupervisorJob() + ioDispatcher)

    init {
        repoScope.launch {
            while (true) {
                delay(20_000.milliseconds)
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
            val url = "${SupabaseConfig.restApiUrl}/${SupabaseConfig.TABLE_CONTENT_REPORTS}?select=*&order=created_at_epoch_ms.desc"
            val response = client.get(url) {
                applySupabaseHeaders()
            }
            if (response.status == HttpStatusCode.OK) {
                val array = jsonParser.parseToJsonElement(response.bodyAsText()).jsonArray
                val remoteList = array.mapNotNull { elem ->
                    val obj = elem.jsonObject
                    val id = obj["id"]?.jsonPrimitive?.content ?: return@mapNotNull null
                    val targetTypeStr = obj["target_type"]?.jsonPrimitive?.content ?: "TUNING_PROFILE"
                    val targetId = obj["target_id"]?.jsonPrimitive?.content ?: ""
                    val targetTitle = obj["target_title"]?.jsonPrimitive?.content ?: "İçerik"
                    val reportedUserId = obj["reported_user_id"]?.jsonPrimitive?.content ?: ""
                    val reportedUserName = obj["reported_user_display_name"]?.jsonPrimitive?.content ?: ""
                    val reporterId = obj["reporter_user_id"]?.jsonPrimitive?.content ?: ""
                    val reporterName = obj["reporter_user_display_name"]?.jsonPrimitive?.content ?: "Kullanıcı"
                    val isSelf = obj["is_self_report"]?.jsonPrimitive?.booleanOrNull ?: false
                    val reasonStr = obj["reason"]?.jsonPrimitive?.content ?: ReportReason.OFFENSIVE_OR_INAPPROPRIATE.name
                    val details = obj["details"]?.jsonPrimitive?.content ?: ""
                    val statusStr = obj["status"]?.jsonPrimitive?.content ?: ReportStatus.PENDING.name
                    val epoch = obj["created_at_epoch_ms"]?.jsonPrimitive?.longOrNull ?: 0L

                    val reason = runCatching { ReportReason.valueOf(reasonStr) }
                        .getOrDefault(ReportReason.OFFENSIVE_OR_INAPPROPRIATE)
                    val targetType = runCatching { ReportTargetType.valueOf(targetTypeStr.uppercase()) }
                        .getOrDefault(ReportTargetType.COMMENT)
                    val reportStatus = runCatching { ReportStatus.valueOf(statusStr.uppercase()) }
                        .getOrDefault(ReportStatus.PENDING)

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
        val client = httpClient
        if (client != null) {
            try {
                val payload = buildJsonObject {
                    put("id", report.id)
                    put("target_type", report.targetType.name)
                    put("target_id", report.targetId)
                    put("target_title", report.targetTitle)
                    put("reported_user_id", report.reportedUserId)
                    put("reported_user_display_name", report.reportedUserDisplayName)
                    put("reporter_user_id", report.reporterUserId)
                    put("reporter_user_display_name", report.reporterUserDisplayName)
                    put("is_self_report", report.isSelfReport)
                    put("reason", report.reason.name)
                    put("details", report.details)
                    put("status", ReportStatus.PENDING.name)
                    put("created_at_epoch_ms", report.timestampMillis)
                }

                val url = "${SupabaseConfig.restApiUrl}/${SupabaseConfig.TABLE_CONTENT_REPORTS}"
                val response = client.post(url) {
                    applySupabaseHeaders(preferMinimal = true)
                    contentType(ContentType.Application.Json)
                    setBody(payload.toString())
                }
                if (!response.status.isSuccess()) {
                    return@withContext DomainResult.Failure(
                        DomainError.Unknown(IllegalStateException("Bildirim sunucuya gönderilemedi (HTTP ${response.status.value}).")),
                    )
                }
            } catch (e: Exception) {
                return@withContext DomainResult.Failure(DomainError.Unknown(e))
            }
        }

        _reports.update { current ->
            listOf(report) + current.filterNot { it.id == report.id }
        }
        DomainResult.Success(Unit)
    }

    override suspend fun resolveReport(reportId: String, newStatus: ReportStatus): DomainResult<Unit> = withContext(ioDispatcher) {
        val client = httpClient
        if (client != null) {
            try {
                val payload = buildJsonObject { put("status", newStatus.name) }
                val url = "${SupabaseConfig.restApiUrl}/${SupabaseConfig.TABLE_CONTENT_REPORTS}?id=eq.$reportId"
                val response = client.patch(url) {
                    applySupabaseHeaders(preferMinimal = true)
                    contentType(ContentType.Application.Json)
                    setBody(payload.toString())
                }
                if (!response.status.isSuccess()) {
                    return@withContext DomainResult.Failure(
                        DomainError.Unknown(IllegalStateException("Rapor güncellenemedi (HTTP ${response.status.value}).")),
                    )
                }
            } catch (e: Exception) {
                return@withContext DomainResult.Failure(DomainError.Unknown(e))
            }
        }
        _reports.update { current ->
            current.map { r ->
                if (r.id == reportId) r.copy(status = newStatus) else r
            }
        }
        DomainResult.Success(Unit)
    }

    override suspend fun deleteReport(reportId: String): DomainResult<Unit> = withContext(ioDispatcher) {
        val client = httpClient
        if (client != null) {
            try {
                val url = "${SupabaseConfig.restApiUrl}/${SupabaseConfig.TABLE_CONTENT_REPORTS}?id=eq.$reportId"
                val response = client.delete(url) {
                    applySupabaseHeaders(preferMinimal = true)
                }
                if (!response.status.isSuccess()) {
                    return@withContext DomainResult.Failure(
                        DomainError.Unknown(IllegalStateException("Rapor silinemedi (HTTP ${response.status.value}).")),
                    )
                }
            } catch (e: Exception) {
                return@withContext DomainResult.Failure(DomainError.Unknown(e))
            }
        }
        _reports.update { current ->
            current.filterNot { it.id == reportId }
        }
        DomainResult.Success(Unit)
    }
}
