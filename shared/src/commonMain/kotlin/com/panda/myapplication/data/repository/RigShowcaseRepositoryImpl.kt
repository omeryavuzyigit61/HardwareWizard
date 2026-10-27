package com.panda.myapplication.data.repository

import com.panda.myapplication.data.remote.SupabaseConfig
import com.panda.myapplication.data.remote.SupabaseConfig.applySupabaseHeaders
import com.panda.myapplication.domain.common.DomainError
import com.panda.myapplication.domain.common.DomainResult
import com.panda.myapplication.domain.model.RigShowcase
import com.panda.myapplication.domain.repository.RigShowcaseRepository
import com.panda.myapplication.domain.util.ProfanityFilter
import io.ktor.client.HttpClient
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.headers
import io.ktor.client.request.patch
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import kotlin.time.Clock
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.booleanOrNull
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.intOrNull
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.longOrNull
import kotlinx.serialization.json.put

class RigShowcaseRepositoryImpl(
    private val httpClient: HttpClient,
    private val json: Json,
    private val ioDispatcher: CoroutineDispatcher,
) : RigShowcaseRepository {

    private val _showcases = MutableStateFlow<List<RigShowcase>>(emptyList())
    private val _userLikes = MutableStateFlow<Set<String>>(emptySet())

    init {
        CoroutineScope(ioDispatcher).launch {
            fetchRemoteShowcases()
            while (true) {
                delay(12000)
                if (_showcases.subscriptionCount.value > 0) {
                    fetchRemoteShowcases()
                }
            }
        }
    }

    private suspend fun fetchRemoteShowcases() {
        runCatching {
            val url = "${SupabaseConfig.restApiUrl}/${SupabaseConfig.TABLE_COMMUNITY_RIG_SHOWCASES}?select=*&order=created_at_epoch_ms.desc"
            val response = httpClient.get(url) {
                applySupabaseHeaders()
            }
            if (response.status == HttpStatusCode.OK) {
                val array = json.parseToJsonElement(response.bodyAsText()).jsonArray
                val items = array.mapNotNull { elem ->
                    val obj = elem.jsonObject
                    val id = obj["id"]?.jsonPrimitive?.content ?: return@mapNotNull null
                    val userId = obj["user_id"]?.jsonPrimitive?.content ?: ""
                    val userName = obj["user_display_name"]?.jsonPrimitive?.content ?: "Kullanıcı"
                    val userBadge = obj["user_badge"]?.jsonPrimitive?.content ?: "Lehim Çırağı"
                    val title = obj["title"]?.jsonPrimitive?.content ?: "Sistem Vitrini"
                    val desc = obj["description"]?.jsonPrimitive?.contentOrNull
                    val rigUrl = obj["photo_rig_url"]?.jsonPrimitive?.content ?: ""
                    val setupUrl = obj["photo_setup_url"]?.jsonPrimitive?.content ?: ""
                    val specs = obj["hardware_specs_json"]?.jsonPrimitive?.contentOrNull
                    val likes = obj["likes_count"]?.jsonPrimitive?.intOrNull ?: 0
                    val epoch = obj["created_at_epoch_ms"]?.jsonPrimitive?.longOrNull ?: 0L

                    RigShowcase(
                        id = id,
                        userId = userId,
                        userDisplayName = userName,
                        userBadge = userBadge,
                        title = title,
                        description = desc,
                        photoRigUrl = rigUrl,
                        photoSetupUrl = setupUrl,
                        hardwareSpecsJson = specs,
                        likesCount = likes,
                        isLikedByMe = _userLikes.value.contains(id),
                        createdAtEpochMs = epoch,
                    )
                }
                _showcases.value = items
            }
        }
    }

    private suspend fun checkUserSuspended(userId: String): String? {
        if (userId.isBlank() || userId.startsWith("guest") || userId == "anon") return null
        return runCatching {
            val checkUrl = "${SupabaseConfig.restApiUrl}/${SupabaseConfig.TABLE_USER_PROFILES}?id=eq.$userId&select=is_banned,banned_until_epoch_ms,ban_reason"
            val response = httpClient.get(checkUrl) {
                applySupabaseHeaders()
            }
            if (response.status == HttpStatusCode.OK) {
                val arr = json.parseToJsonElement(response.bodyAsText()).jsonArray
                if (arr.isNotEmpty()) {
                    val obj = arr[0].jsonObject
                    val isBanned = obj["is_banned"]?.jsonPrimitive?.booleanOrNull ?: false
                    val bannedUntil = obj["banned_until_epoch_ms"]?.jsonPrimitive?.longOrNull ?: 0L
                    val reason = obj["ban_reason"]?.jsonPrimitive?.contentOrNull ?: "Topluluk kuralları ihlali"
                    val now = Clock.System.now().toEpochMilliseconds()
                    if (isBanned) {
                        return@runCatching "Hesabınız süresiz olarak askıya alınmıştır. Gerekçe: $reason"
                    } else if (bannedUntil > now) {
                        val diffMs = bannedUntil - now
                        val hours = (diffMs / (1000 * 60 * 60)).coerceAtLeast(1)
                        val days = hours / 24
                        val remainingStr = if (days > 0) "$days gün ${hours % 24} saat" else "$hours saat"
                        return@runCatching "Hesabınız $remainingStr süreyle askıya alınmıştır. Gerekçe: $reason"
                    }
                }
            }
            null
        }.getOrNull()
    }

    override fun observeAllShowcases(): Flow<List<RigShowcase>> = _showcases.asStateFlow()

    override suspend fun submitShowcase(showcase: RigShowcase): DomainResult<Unit> = withContext(ioDispatcher) {
        val suspensionMsg = checkUserSuspended(showcase.userId)
        if (suspensionMsg != null) {
            return@withContext DomainResult.Failure(DomainError.Unknown(IllegalStateException(suspensionMsg)))
        }

        runCatching {
            val maskedTitle = ProfanityFilter.mask(showcase.title)
            val maskedDesc = showcase.description?.let { ProfanityFilter.mask(it) }

            val url = "${SupabaseConfig.restApiUrl}/${SupabaseConfig.TABLE_COMMUNITY_RIG_SHOWCASES}"
            val payload = buildJsonObject {
                put("id", showcase.id)
                put("user_id", showcase.userId)
                put("user_display_name", showcase.userDisplayName)
                put("user_badge", showcase.userBadge)
                put("title", maskedTitle)
                maskedDesc?.let { put("description", it) }
                put("photo_rig_url", showcase.photoRigUrl)
                put("photo_setup_url", showcase.photoSetupUrl)
                showcase.hardwareSpecsJson?.let { put("hardware_specs_json", it) }
                put("likes_count", showcase.likesCount)
                put("created_at_epoch_ms", showcase.createdAtEpochMs)
            }

            val response = httpClient.post(url) {
                headers {
                    append("apikey", SupabaseConfig.supabaseAnonKey)
                    append("Authorization", "Bearer ${SupabaseConfig.supabaseAnonKey}")
                    append("Prefer", "return=minimal")
                }
                contentType(ContentType.Application.Json)
                setBody(payload.toString())
            }

            if (response.status == HttpStatusCode.Created || response.status == HttpStatusCode.OK) {
                _showcases.update { listOf(showcase.copy(title = maskedTitle, description = maskedDesc)) + it }
                DomainResult.Success(Unit)
            } else {
                DomainResult.Failure(DomainError.Network)
            }
        }.getOrElse { DomainResult.Failure(DomainError.Unknown(it)) }
    }

    override suspend fun toggleLike(showcaseId: String, userId: String): DomainResult<Unit> = withContext(ioDispatcher) {
        if (userId.isBlank() || userId.startsWith("guest")) {
            return@withContext DomainResult.Failure(DomainError.Unknown(IllegalStateException("Beğenmek için lütfen giriş yapın.")))
        }

        runCatching {
            val likeId = "${showcaseId}_${userId}"
            val checkUrl = "${SupabaseConfig.restApiUrl}/${SupabaseConfig.TABLE_SHOWCASE_LIKES}?id=eq.$likeId"
            val checkRes = httpClient.get(checkUrl) { applySupabaseHeaders() }
            val isAlreadyLiked = if (checkRes.status == HttpStatusCode.OK) {
                json.parseToJsonElement(checkRes.bodyAsText()).jsonArray.isNotEmpty()
            } else false

            val current = _showcases.value.find { it.id == showcaseId }
            val curLikes = current?.likesCount ?: 0

            if (isAlreadyLiked) {
                // Unlike
                httpClient.delete(checkUrl) { applySupabaseHeaders() }
                val newLikes = (curLikes - 1).coerceAtLeast(0)
                patchLikesCount(showcaseId, newLikes)
                _userLikes.update { it - showcaseId }
                _showcases.update { list ->
                    list.map { if (it.id == showcaseId) it.copy(likesCount = newLikes, isLikedByMe = false) else it }
                }
            } else {
                // Like
                val postUrl = "${SupabaseConfig.restApiUrl}/${SupabaseConfig.TABLE_SHOWCASE_LIKES}"
                val payload = buildJsonObject {
                    put("id", likeId)
                    put("showcase_id", showcaseId)
                    put("user_id", userId)
                }
                httpClient.post(postUrl) {
                    applySupabaseHeaders()
                    contentType(ContentType.Application.Json)
                    setBody(payload.toString())
                }
                val newLikes = curLikes + 1
                patchLikesCount(showcaseId, newLikes)
                _userLikes.update { it + showcaseId }
                _showcases.update { list ->
                    list.map { if (it.id == showcaseId) it.copy(likesCount = newLikes, isLikedByMe = true) else it }
                }
            }

            DomainResult.Success(Unit)
        }.getOrElse { DomainResult.Failure(DomainError.Unknown(it)) }
    }

    private suspend fun patchLikesCount(showcaseId: String, newLikes: Int) {
        val patchUrl = "${SupabaseConfig.restApiUrl}/${SupabaseConfig.TABLE_COMMUNITY_RIG_SHOWCASES}?id=eq.$showcaseId"
        httpClient.patch(patchUrl) {
            applySupabaseHeaders()
            contentType(ContentType.Application.Json)
            setBody(buildJsonObject { put("likes_count", newLikes) }.toString())
        }
    }

    override suspend fun uploadShowcaseImage(bytes: ByteArray, fileName: String): DomainResult<String> = withContext(ioDispatcher) {
        runCatching {
            val uploadUrl = "${SupabaseConfig.storageApiUrl}/object/${SupabaseConfig.STORAGE_BUCKET_TUNING}/$fileName"
            val response = httpClient.post(uploadUrl) {
                headers {
                    append("apikey", SupabaseConfig.supabaseAnonKey)
                    append("Authorization", "Bearer ${SupabaseConfig.supabaseAnonKey}")
                    append("x-upsert", "true")
                }
                contentType(ContentType.Image.JPEG)
                setBody(bytes)
            }

            if (response.status == HttpStatusCode.OK || response.status == HttpStatusCode.Created) {
                val publicUrl = "${SupabaseConfig.storageApiUrl}/object/public/${SupabaseConfig.STORAGE_BUCKET_TUNING}/$fileName"
                DomainResult.Success(publicUrl)
            } else {
                DomainResult.Failure(DomainError.Network)
            }
        }.getOrElse { DomainResult.Failure(DomainError.Unknown(it)) }
    }
}
