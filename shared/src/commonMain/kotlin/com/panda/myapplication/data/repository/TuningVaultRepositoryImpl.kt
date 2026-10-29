package com.panda.myapplication.data.repository

import com.panda.myapplication.data.remote.SupabaseConfig
import com.panda.myapplication.data.remote.SupabaseConfig.applySupabaseHeaders
import com.panda.myapplication.domain.common.DomainError
import com.panda.myapplication.domain.common.DomainResult
import com.panda.myapplication.domain.model.HardwareProTip
import com.panda.myapplication.domain.model.PriceDropAlert
import com.panda.myapplication.domain.model.TuningComment
import com.panda.myapplication.domain.model.TuningProfile
import com.panda.myapplication.domain.model.TuningReactionType
import com.panda.myapplication.domain.model.TuningStatus
import com.panda.myapplication.domain.model.TuningType
import com.panda.myapplication.domain.model.UserProfile
import com.panda.myapplication.domain.repository.TuningVaultRepository
import com.panda.myapplication.domain.util.ProfanityFilter
import kotlin.time.Clock
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
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.booleanOrNull
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.doubleOrNull
import kotlinx.serialization.json.intOrNull
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.longOrNull
import kotlinx.serialization.json.put

class TuningVaultRepositoryImpl(
    private val httpClient: HttpClient,
    private val json: Json,
    private val ioDispatcher: CoroutineDispatcher,
) : TuningVaultRepository {

    private val _tuningProfiles = MutableStateFlow<List<TuningProfile>>(emptyList())
    private val _comments = MutableStateFlow<Map<String, List<TuningComment>>>(emptyMap())
    private val _proTips = MutableStateFlow<List<HardwareProTip>>(seedDefaultHardwareReviews())
    private val _priceAlerts = MutableStateFlow<List<PriceDropAlert>>(emptyList())

    init {
        CoroutineScope(ioDispatcher).launch {
            fetchRemoteTunings()
            fetchRemoteTips()
            // Lifecycle-aware senkronizasyon: Yalnızca aktif UI abonesi varken bulut sorgusu yap
            while (true) {
                delay(8000)
                if (_tuningProfiles.subscriptionCount.value > 0) {
                    fetchRemoteTunings()
                }
                if (_proTips.subscriptionCount.value > 0) {
                    fetchRemoteTips()
                }
            }
        }
    }

    private suspend fun fetchRemoteTunings() {
        runCatching {
            val url = "${SupabaseConfig.restApiUrl}/${SupabaseConfig.TABLE_TUNING_PROFILES}?select=*&order=created_at_epoch_ms.desc"
            val response = httpClient.get(url) {
                applySupabaseHeaders()
            }
            if (response.status == HttpStatusCode.OK) {
                val jsonArray = json.parseToJsonElement(response.bodyAsText()).jsonArray
                val profiles = jsonArray.mapNotNull { element ->
                    val obj = element.jsonObject
                    val id = obj["id"]?.jsonPrimitive?.content ?: return@mapNotNull null
                    val hardwareId = obj["hardware_id"]?.jsonPrimitive?.content ?: return@mapNotNull null
                    val hardwareName = obj["hardware_name"]?.jsonPrimitive?.content ?: ""
                    val category = obj["hardware_category"]?.jsonPrimitive?.content ?: "CPU"
                    val tuningTypeStr = obj["tuning_type"]?.jsonPrimitive?.content
                    val parsedTypes = TuningType.fromStringList(tuningTypeStr)
                    val profileTitle = obj["profile_title"]?.jsonPrimitive?.content ?: "Ayar Profili"
                    val languageCode = obj["language_code"]?.jsonPrimitive?.content ?: "tr"
                    val voltageOffset = obj["voltage_offset_mv"]?.jsonPrimitive?.doubleOrNull
                    val clockMhz = obj["clock_mhz"]?.jsonPrimitive?.doubleOrNull
                    val powerLimit = obj["power_limit_watt"]?.jsonPrimitive?.doubleOrNull
                    val tempBefore = obj["temp_before_c"]?.jsonPrimitive?.doubleOrNull
                    val tempAfter = obj["temp_after_c"]?.jsonPrimitive?.doubleOrNull
                    val scoreBefore = obj["score_before"]?.jsonPrimitive?.doubleOrNull
                    val scoreAfter = obj["score_after"]?.jsonPrimitive?.doubleOrNull
                    val benchName = obj["benchmark_name"]?.jsonPrimitive?.content
                    val proofImg = obj["proof_image_url"]?.jsonPrimitive?.content
                    val notes = obj["guide_notes"]?.jsonPrimitive?.content
                    val userId = obj["user_id"]?.jsonPrimitive?.content ?: "anon"
                    val userName = obj["user_display_name"]?.jsonPrimitive?.content ?: "Kullanıcı"
                    val badge = obj["user_badge"]?.jsonPrimitive?.content ?: "Lehim Çırağı"
                    val statusStr = obj["status"]?.jsonPrimitive?.content ?: "PENDING"
                    val rejectionReason = obj["rejection_reason"]?.jsonPrimitive?.content
                    val hot = obj["reaction_hot_count"]?.jsonPrimitive?.intOrNull ?: 0
                    val cold = obj["reaction_cold_count"]?.jsonPrimitive?.intOrNull ?: 0
                    val efficient = obj["reaction_efficient_count"]?.jsonPrimitive?.intOrNull ?: 0
                    val stable = obj["reaction_stable_count"]?.jsonPrimitive?.intOrNull ?: 0
                    val unstable = obj["reaction_unstable_count"]?.jsonPrimitive?.intOrNull ?: 0
                    val commentsCount = obj["comments_count"]?.jsonPrimitive?.intOrNull ?: 0
                    val epochMs = obj["created_at_epoch_ms"]?.jsonPrimitive?.longOrNull ?: 0L
                    val mbName = obj["motherboard_name"]?.jsonPrimitive?.content
                    val coolerName = obj["cooler_name"]?.jsonPrimitive?.content
                    val ramSpec = obj["ram_spec"]?.jsonPrimitive?.content
                    val secHardware = obj["secondary_hardware"]?.jsonPrimitive?.content

                    TuningProfile(
                        id = id,
                        hardwareId = hardwareId,
                        hardwareName = hardwareName,
                        hardwareCategory = category,
                        tuningType = parsedTypes.first(),
                        tuningTypes = parsedTypes,
                        profileTitle = profileTitle,
                        languageCode = languageCode,
                        voltageOffsetMv = voltageOffset,
                        clockMhz = clockMhz,
                        powerLimitWatt = powerLimit,
                        tempBeforeC = tempBefore,
                        tempAfterC = tempAfter,
                        scoreBefore = scoreBefore,
                        scoreAfter = scoreAfter,
                        benchmarkName = benchName,
                        proofImageUrl = proofImg,
                        guideNotes = notes,
                        userId = userId,
                        userDisplayName = userName,
                        userBadge = badge,
                        status = TuningStatus.fromString(statusStr),
                        rejectionReason = rejectionReason,
                        reactionHotCount = hot,
                        reactionColdCount = cold,
                        reactionEfficientCount = efficient,
                        reactionStableCount = stable,
                        reactionUnstableCount = unstable,
                        commentsCount = commentsCount,
                        createdAtEpochMs = epochMs,
                        motherboardName = mbName,
                        coolerName = coolerName,
                        ramSpec = ramSpec,
                        secondaryHardware = secHardware,
                    )
                }
                _tuningProfiles.value = profiles
            }
        }
    }

    private suspend fun fetchRemoteTips() {
        runCatching {
            val url = "${SupabaseConfig.restApiUrl}/${SupabaseConfig.TABLE_HARDWARE_PRO_TIPS}?select=*&order=upvotes_count.desc"
            val response = httpClient.get(url) {
                headers {
                    append("apikey", SupabaseConfig.supabaseAnonKey)
                    append("Authorization", "Bearer ${SupabaseConfig.supabaseAnonKey}")
                }
            }
            if (response.status == HttpStatusCode.OK) {
                val jsonArray = json.parseToJsonElement(response.bodyAsText()).jsonArray
                val tips = jsonArray.mapNotNull { element ->
                    val obj = element.jsonObject
                    val id = obj["id"]?.jsonPrimitive?.content ?: return@mapNotNull null
                    val hardwareId = obj["hardware_id"]?.jsonPrimitive?.content ?: return@mapNotNull null
                    val hardwareName = obj["hardware_name"]?.jsonPrimitive?.content ?: ""
                    val userId = obj["user_id"]?.jsonPrimitive?.content ?: ""
                    val userName = obj["user_display_name"]?.jsonPrimitive?.content ?: ""
                    val badge = obj["user_badge"]?.jsonPrimitive?.content ?: "Lehim Çırağı"
                    val tipText = obj["tip_text"]?.jsonPrimitive?.content ?: ""
                    val upvotes = obj["upvotes_count"]?.jsonPrimitive?.intOrNull ?: 0
                    val epochMs = obj["created_at_epoch_ms"]?.jsonPrimitive?.longOrNull ?: 0L

                    val rating = obj["rating"]?.jsonPrimitive?.intOrNull ?: 5
                    HardwareProTip(
                        id = id,
                        hardwareId = hardwareId,
                        hardwareName = hardwareName,
                        userId = userId,
                        userDisplayName = userName,
                        userBadge = badge,
                        tipText = tipText,
                        upvotesCount = upvotes,
                        isUpvotedByMe = false,
                        createdAtEpochMs = epochMs,
                        rating = rating,
                    )
                }
                if (tips.isNotEmpty()) {
                    val defaultSeeds = seedDefaultHardwareReviews()
                    val merged = tips + defaultSeeds.filterNot { seed -> tips.any { it.id == seed.id } }
                    _proTips.value = merged
                }
            }
        }
    }

    override fun observeTuningsForHardware(hardwareId: String): Flow<List<TuningProfile>> =
        _tuningProfiles.map { list ->
            list.filter { it.hardwareId.equals(hardwareId, ignoreCase = true) && it.status == TuningStatus.APPROVED }
        }

    override fun observeAllApprovedTunings(): Flow<List<TuningProfile>> =
        _tuningProfiles.map { list -> list.filter { it.status == TuningStatus.APPROVED } }

    override fun observePendingTunings(): Flow<List<TuningProfile>> =
        _tuningProfiles.map { list -> list.filter { it.status == TuningStatus.PENDING } }

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

    override suspend fun submitTuningProfile(profile: TuningProfile): DomainResult<Unit> = withContext(ioDispatcher) {
        val suspensionMsg = checkUserSuspended(profile.userId)
        if (suspensionMsg != null) {
            return@withContext DomainResult.Failure(DomainError.Unknown(IllegalStateException(suspensionMsg)))
        }

        runCatching {
            val maskedTitle = ProfanityFilter.mask(profile.profileTitle)
            val maskedNotes = profile.guideNotes?.let { ProfanityFilter.mask(it) }

            val url = "${SupabaseConfig.restApiUrl}/${SupabaseConfig.TABLE_TUNING_PROFILES}"
            val payload = buildJsonObject {
                put("id", profile.id)
                put("hardware_id", profile.hardwareId)
                put("hardware_name", profile.hardwareName)
                put("hardware_category", profile.hardwareCategory)
                put("tuning_type", TuningType.toStorageString(profile.effectiveTuningTypes))
                put("profile_title", maskedTitle)
                put("language_code", profile.languageCode)
                profile.voltageOffsetMv?.let { put("voltage_offset_mv", it) }
                profile.clockMhz?.let { put("clock_mhz", it) }
                profile.powerLimitWatt?.let { put("power_limit_watt", it) }
                profile.tempBeforeC?.let { put("temp_before_c", it) }
                profile.tempAfterC?.let { put("temp_after_c", it) }
                profile.scoreBefore?.let { put("score_before", it) }
                profile.scoreAfter?.let { put("score_after", it) }
                profile.benchmarkName?.let { put("benchmark_name", it) }
                profile.proofImageUrl?.let { put("proof_image_url", it) }
                maskedNotes?.let { put("guide_notes", it) }
                profile.motherboardName?.let { put("motherboard_name", it) }
                profile.coolerName?.let { put("cooler_name", it) }
                profile.ramSpec?.let { put("ram_spec", it) }
                profile.secondaryHardware?.let { put("secondary_hardware", it) }
                put("user_id", profile.userId)
                put("user_display_name", profile.userDisplayName)
                put("user_badge", profile.userBadge)
                put("status", "PENDING")
                put("created_at_epoch_ms", profile.createdAtEpochMs)
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
                _tuningProfiles.update { listOf(profile) + it }
                patchUserProfileStats(userId = profile.userId, tuningsDelta = 1)
                DomainResult.Success(Unit)
            } else {
                DomainResult.Failure(DomainError.Network)
            }
        }.getOrElse {
            DomainResult.Failure(DomainError.Unknown(it))
        }
    }

    override suspend fun approveTuningProfile(id: String): DomainResult<Unit> = withContext(ioDispatcher) {
        runCatching {
            val url = "${SupabaseConfig.restApiUrl}/${SupabaseConfig.TABLE_TUNING_PROFILES}?id=eq.$id"
            val payload = buildJsonObject {
                put("status", "APPROVED")
            }
            val response = httpClient.patch(url) {
                headers {
                    append("apikey", SupabaseConfig.supabaseAnonKey)
                    append("Authorization", "Bearer ${SupabaseConfig.supabaseAnonKey}")
                }
                contentType(ContentType.Application.Json)
                setBody(payload.toString())
            }
            if (response.status == HttpStatusCode.OK || response.status == HttpStatusCode.NoContent) {
                _tuningProfiles.update { list ->
                    list.map { if (it.id == id) it.copy(status = TuningStatus.APPROVED) else it }
                }
                DomainResult.Success(Unit)
            } else {
                DomainResult.Failure(DomainError.Network)
            }
        }.getOrElse { DomainResult.Failure(DomainError.Unknown(it)) }
    }

    override suspend fun rejectTuningProfile(id: String, reason: String): DomainResult<Unit> = withContext(ioDispatcher) {
        runCatching {
            val url = "${SupabaseConfig.restApiUrl}/${SupabaseConfig.TABLE_TUNING_PROFILES}?id=eq.$id"
            val payload = buildJsonObject {
                put("status", "REJECTED")
                put("rejection_reason", reason)
            }
            val response = httpClient.patch(url) {
                headers {
                    append("apikey", SupabaseConfig.supabaseAnonKey)
                    append("Authorization", "Bearer ${SupabaseConfig.supabaseAnonKey}")
                }
                contentType(ContentType.Application.Json)
                setBody(payload.toString())
            }
            if (response.status == HttpStatusCode.OK || response.status == HttpStatusCode.NoContent) {
                _tuningProfiles.update { list ->
                    list.map { if (it.id == id) it.copy(status = TuningStatus.REJECTED, rejectionReason = reason) else it }
                }
                DomainResult.Success(Unit)
            } else {
                DomainResult.Failure(DomainError.Network)
            }
        }.getOrElse { DomainResult.Failure(DomainError.Unknown(it)) }
    }

    private suspend fun patchUserProfileStats(
        userId: String,
        pointsDelta: Int = 0,
        dealsDelta: Int = 0,
        tuningsDelta: Int = 0,
        tipsDelta: Int = 0,
    ) {
        if (userId.isBlank() || userId.startsWith("guest")) return
        runCatching {
            val safeCols = "reputation_score,total_deals_count,total_tunings_count,total_tips_count,badge_title"
            val getUrl = "${SupabaseConfig.restApiUrl}/${SupabaseConfig.TABLE_USER_PROFILES}?id=eq.$userId&select=$safeCols"
            val res = httpClient.get(getUrl) {
                applySupabaseHeaders()
            }
            if (res.status == HttpStatusCode.OK) {
                val arr = json.parseToJsonElement(res.bodyAsText()).jsonArray
                if (arr.isNotEmpty()) {
                    val uObj = arr[0].jsonObject
                    val curScore = uObj["reputation_score"]?.jsonPrimitive?.intOrNull ?: 0
                    val curDeals = uObj["total_deals_count"]?.jsonPrimitive?.intOrNull ?: 0
                    val curTunings = uObj["total_tunings_count"]?.jsonPrimitive?.intOrNull ?: 0
                    val curTips = uObj["total_tips_count"]?.jsonPrimitive?.intOrNull ?: 0

                    val newScore = maxOf(0, curScore + pointsDelta)
                    val newDeals = maxOf(0, curDeals + dealsDelta)
                    val newTunings = maxOf(0, curTunings + tuningsDelta)
                    val newTips = maxOf(0, curTips + tipsDelta)

                    val newBadge = UserProfile.calculateBadgeTitle(newScore)

                    val patchUrl = "${SupabaseConfig.restApiUrl}/${SupabaseConfig.TABLE_USER_PROFILES}?id=eq.$userId"
                    val patchBody = buildJsonObject {
                        put("reputation_score", newScore)
                        put("total_deals_count", newDeals)
                        put("total_tunings_count", newTunings)
                        put("total_tips_count", newTips)
                        put("badge_title", newBadge)
                    }

                    httpClient.patch(patchUrl) {
                        applySupabaseHeaders()
                        contentType(ContentType.Application.Json)
                        setBody(patchBody.toString())
                    }
                }
            }
        }
    }

    override suspend fun toggleReaction(tuningId: String, reactionType: TuningReactionType, userId: String): DomainResult<Unit> = withContext(ioDispatcher) {
        runCatching {
            val profile = _tuningProfiles.value.find { it.id == tuningId }
            if (profile != null && profile.userId == userId) {
                return@withContext DomainResult.Failure(DomainError.Unknown(IllegalStateException("Kendi ayarınıza tepki veremezsiniz.")))
            }

            val reactionId = "${tuningId}_${userId}_${reactionType.key}"
            val checkUrl = "${SupabaseConfig.restApiUrl}/${SupabaseConfig.TABLE_TUNING_REACTIONS}?id=eq.$reactionId"
            val checkRes = httpClient.get(checkUrl) {
                headers {
                    append("apikey", SupabaseConfig.supabaseAnonKey)
                    append("Authorization", "Bearer ${SupabaseConfig.supabaseAnonKey}")
                }
            }
            val alreadyReacted = if (checkRes.status == HttpStatusCode.OK) {
                json.parseToJsonElement(checkRes.bodyAsText()).jsonArray.isNotEmpty()
            } else false

            val curHot = profile?.reactionHotCount ?: 0
            val curCold = profile?.reactionColdCount ?: 0
            val curEff = profile?.reactionEfficientCount ?: 0
            val curStable = profile?.reactionStableCount ?: 0
            val curUnstable = profile?.reactionUnstableCount ?: 0

            val (newHot, newCold, newEff, newStable, newUnstable) = when (reactionType) {
                TuningReactionType.HOT -> (if (alreadyReacted) maxOf(0, curHot - 1) else curHot + 1) to (curCold to (curEff to (curStable to curUnstable)))
                TuningReactionType.COLD -> curHot to ((if (alreadyReacted) maxOf(0, curCold - 1) else curCold + 1) to (curEff to (curStable to curUnstable)))
                TuningReactionType.EFFICIENT -> curHot to (curCold to ((if (alreadyReacted) maxOf(0, curEff - 1) else curEff + 1) to (curStable to curUnstable)))
                TuningReactionType.STABLE -> curHot to (curCold to (curEff to ((if (alreadyReacted) maxOf(0, curStable - 1) else curStable + 1) to curUnstable)))
                TuningReactionType.UNSTABLE -> curHot to (curCold to (curEff to (curStable to (if (alreadyReacted) maxOf(0, curUnstable - 1) else curUnstable + 1))))
            }.let { (h, rest) ->
                val (c, rest2) = rest
                val (e, rest3) = rest2
                val (s, u) = rest3
                listOf(h, c, e, s, u)
            }

            if (alreadyReacted) {
                val delUrl = "${SupabaseConfig.restApiUrl}/${SupabaseConfig.TABLE_TUNING_REACTIONS}?id=eq.$reactionId"
                httpClient.delete(delUrl) {
                    headers {
                        append("apikey", SupabaseConfig.supabaseAnonKey)
                        append("Authorization", "Bearer ${SupabaseConfig.supabaseAnonKey}")
                    }
                }
                if (profile != null) {
                    patchUserProfileStats(userId = profile.userId, pointsDelta = -1)
                }
            } else {
                val postUrl = "${SupabaseConfig.restApiUrl}/${SupabaseConfig.TABLE_TUNING_REACTIONS}"
                val payload = buildJsonObject {
                    put("id", reactionId)
                    put("tuning_id", tuningId)
                    put("user_id", userId)
                    put("reaction_type", reactionType.key)
                }
                httpClient.post(postUrl) {
                    headers {
                        append("apikey", SupabaseConfig.supabaseAnonKey)
                        append("Authorization", "Bearer ${SupabaseConfig.supabaseAnonKey}")
                    }
                    contentType(ContentType.Application.Json)
                    setBody(payload.toString())
                }
                if (profile != null) {
                    patchUserProfileStats(userId = profile.userId, pointsDelta = 1)
                }
            }

            val patchTuningUrl = "${SupabaseConfig.restApiUrl}/${SupabaseConfig.TABLE_TUNING_PROFILES}?id=eq.$tuningId"
            val patchBody = buildJsonObject {
                put("reaction_hot_count", newHot)
                put("reaction_cold_count", newCold)
                put("reaction_efficient_count", newEff)
                put("reaction_stable_count", newStable)
                put("reaction_unstable_count", newUnstable)
            }
            httpClient.patch(patchTuningUrl) {
                headers {
                    append("apikey", SupabaseConfig.supabaseAnonKey)
                    append("Authorization", "Bearer ${SupabaseConfig.supabaseAnonKey}")
                }
                contentType(ContentType.Application.Json)
                setBody(patchBody.toString())
            }

            _tuningProfiles.update { list ->
                list.map {
                    if (it.id == tuningId) {
                        it.copy(
                            reactionHotCount = newHot,
                            reactionColdCount = newCold,
                            reactionEfficientCount = newEff,
                            reactionStableCount = newStable,
                            reactionUnstableCount = newUnstable,
                        )
                    } else it
                }
            }

            DomainResult.Success(Unit)
        }.getOrElse { DomainResult.Failure(DomainError.Unknown(it)) }
    }

    override fun observeComments(tuningId: String): Flow<List<TuningComment>> {
        CoroutineScope(ioDispatcher).launch {
            runCatching {
                val endpoint = "${SupabaseConfig.restApiUrl}/${SupabaseConfig.TABLE_TUNING_COMMENTS}?tuning_id=eq.$tuningId&order=created_at_epoch_ms.asc"
                val response = httpClient.get(endpoint) {
                    headers {
                        append("apikey", SupabaseConfig.supabaseAnonKey)
                        append("Authorization", "Bearer ${SupabaseConfig.supabaseAnonKey}")
                    }
                }
                if (response.status == HttpStatusCode.OK) {
                    val body = response.bodyAsText()
                    val jsonArray = json.parseToJsonElement(body).jsonArray
                    val loaded = jsonArray.map { elem ->
                        val obj = elem.jsonObject
                        val rawCommentText = obj["comment_text"]?.jsonPrimitive?.content ?: ""
                        TuningComment(
                            id = obj["id"]?.jsonPrimitive?.content ?: "",
                            tuningId = obj["tuning_id"]?.jsonPrimitive?.content ?: tuningId,
                            userId = obj["user_id"]?.jsonPrimitive?.content ?: "",
                            userDisplayName = obj["user_display_name"]?.jsonPrimitive?.content ?: "Topluluk Üyesi",
                            userBadge = obj["user_badge"]?.jsonPrimitive?.content ?: "Lehim Çırağı",
                            userReputation = obj["user_reputation"]?.jsonPrimitive?.intOrNull ?: 10,
                            commentText = ProfanityFilter.mask(rawCommentText),
                            createdAtEpochMs = obj["created_at_epoch_ms"]?.jsonPrimitive?.longOrNull ?: 0L,
                        )
                    }
                    _comments.update { it + (tuningId to loaded) }
                }
            }
        }
        return _comments.map { it[tuningId] ?: emptyList() }
    }

    override suspend fun addComment(comment: TuningComment): DomainResult<Unit> = withContext(ioDispatcher) {
        val suspensionMsg = checkUserSuspended(comment.userId)
        if (suspensionMsg != null) {
            return@withContext DomainResult.Failure(DomainError.Unknown(IllegalStateException(suspensionMsg)))
        }

        runCatching {
            val maskedText = ProfanityFilter.mask(comment.commentText)
            val url = "${SupabaseConfig.restApiUrl}/${SupabaseConfig.TABLE_TUNING_COMMENTS}"
            val payload = buildJsonObject {
                put("id", comment.id)
                put("tuning_id", comment.tuningId)
                put("user_id", comment.userId)
                put("user_display_name", comment.userDisplayName)
                put("user_badge", comment.userBadge)
                put("user_reputation", comment.userReputation)
                put("comment_text", maskedText)
                put("created_at_epoch_ms", comment.createdAtEpochMs)
            }

            httpClient.post(url) {
                headers {
                    append("apikey", SupabaseConfig.supabaseAnonKey)
                    append("Authorization", "Bearer ${SupabaseConfig.supabaseAnonKey}")
                }
                contentType(ContentType.Application.Json)
                setBody(payload.toString())
            }

            val maskedComment = comment.copy(commentText = maskedText)
            _comments.update { map ->
                val curList = map[comment.tuningId] ?: emptyList()
                map + (comment.tuningId to (curList + maskedComment))
            }
            val curCount = _tuningProfiles.value.find { it.id == comment.tuningId }?.commentsCount ?: 0
            val newCount = curCount + 1
            _tuningProfiles.update { list ->
                list.map { if (it.id == comment.tuningId) it.copy(commentsCount = newCount) else it }
            }

            val patchTuningUrl = "${SupabaseConfig.restApiUrl}/${SupabaseConfig.TABLE_TUNING_PROFILES}?id=eq.${comment.tuningId}"
            httpClient.patch(patchTuningUrl) {
                headers {
                    append("apikey", SupabaseConfig.supabaseAnonKey)
                    append("Authorization", "Bearer ${SupabaseConfig.supabaseAnonKey}")
                }
                contentType(ContentType.Application.Json)
                setBody(buildJsonObject { put("comments_count", newCount) }.toString())
            }

            patchUserProfileStats(userId = comment.userId, pointsDelta = 2)

            DomainResult.Success(Unit)
        }.getOrElse { DomainResult.Failure(DomainError.Unknown(it)) }
    }

    override suspend fun uploadProofImage(bytes: ByteArray, fileName: String): DomainResult<String> = withContext(ioDispatcher) {
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

    override fun observeTipsForHardware(hardwareId: String): Flow<List<HardwareProTip>> =
        _proTips.map { list ->
            val userAndSeedTips = list.filter { it.hardwareId.equals(hardwareId, ignoreCase = true) }
            if (userAndSeedTips.isNotEmpty()) {
                userAndSeedTips
            } else {
                generateContextualFallbackTips(hardwareId)
            }
        }

    override suspend fun submitTip(tip: HardwareProTip): DomainResult<Unit> = withContext(ioDispatcher) {
        val suspensionMsg = checkUserSuspended(tip.userId)
        if (suspensionMsg != null) {
            return@withContext DomainResult.Failure(DomainError.Unknown(IllegalStateException(suspensionMsg)))
        }

        runCatching {
            val maskedText = ProfanityFilter.mask(tip.tipText)
            val url = "${SupabaseConfig.restApiUrl}/${SupabaseConfig.TABLE_HARDWARE_PRO_TIPS}"
            val payload = buildJsonObject {
                put("id", tip.id)
                put("hardware_id", tip.hardwareId)
                put("hardware_name", tip.hardwareName)
                put("user_id", tip.userId)
                put("user_display_name", tip.userDisplayName)
                put("user_badge", tip.userBadge)
                put("tip_text", maskedText)
                put("upvotes_count", tip.upvotesCount)
                put("rating", tip.rating)
                put("status", "APPROVED")
                put("created_at_epoch_ms", tip.createdAtEpochMs)
            }

            httpClient.post(url) {
                headers {
                    append("apikey", SupabaseConfig.supabaseAnonKey)
                    append("Authorization", "Bearer ${SupabaseConfig.supabaseAnonKey}")
                }
                contentType(ContentType.Application.Json)
                setBody(payload.toString())
            }

            _proTips.update { listOf(tip.copy(tipText = maskedText)) + it }
            patchUserProfileStats(userId = tip.userId, pointsDelta = 10, tipsDelta = 1)

            DomainResult.Success(Unit)
        }.getOrElse { DomainResult.Failure(DomainError.Unknown(it)) }
    }

    override suspend fun deleteTip(tipId: String): DomainResult<Unit> = withContext(ioDispatcher) {
        runCatching {
            val url = "${SupabaseConfig.restApiUrl}/${SupabaseConfig.TABLE_HARDWARE_PRO_TIPS}?id=eq.$tipId"
            httpClient.delete(url) {
                headers {
                    append("apikey", SupabaseConfig.supabaseAnonKey)
                    append("Authorization", "Bearer ${SupabaseConfig.supabaseAnonKey}")
                }
            }
            _proTips.update { list -> list.filterNot { it.id == tipId } }
            DomainResult.Success(Unit)
        }.getOrElse {
            _proTips.update { list -> list.filterNot { it.id == tipId } }
            DomainResult.Success(Unit)
        }
    }

    override suspend fun upvoteTip(tipId: String, userId: String): DomainResult<Unit> = withContext(ioDispatcher) {
        runCatching {
            val tip = _proTips.value.find { it.id == tipId }
            if (tip != null && tip.userId == userId) {
                return@withContext DomainResult.Failure(DomainError.Unknown(IllegalStateException("Kendi ipucunuza oy veremezsiniz.")))
            }

            val voteId = "${tipId}_${userId}"
            val checkUrl = "${SupabaseConfig.restApiUrl}/${SupabaseConfig.TABLE_PRO_TIP_UPVOTES}?id=eq.$voteId"
            val checkRes = httpClient.get(checkUrl) {
                headers {
                    append("apikey", SupabaseConfig.supabaseAnonKey)
                    append("Authorization", "Bearer ${SupabaseConfig.supabaseAnonKey}")
                }
            }
            val alreadyVoted = if (checkRes.status == HttpStatusCode.OK) {
                json.parseToJsonElement(checkRes.bodyAsText()).jsonArray.isNotEmpty()
            } else false

            val curVotes = tip?.upvotesCount ?: 0
            val newVotes: Int
            if (alreadyVoted) {
                newVotes = maxOf(0, curVotes - 1)
                val delUrl = "${SupabaseConfig.restApiUrl}/${SupabaseConfig.TABLE_PRO_TIP_UPVOTES}?id=eq.$voteId"
                httpClient.delete(delUrl) {
                    headers {
                        append("apikey", SupabaseConfig.supabaseAnonKey)
                        append("Authorization", "Bearer ${SupabaseConfig.supabaseAnonKey}")
                    }
                }
                if (tip != null) {
                    patchUserProfileStats(userId = tip.userId, pointsDelta = -1)
                }
            } else {
                newVotes = curVotes + 1
                val postUrl = "${SupabaseConfig.restApiUrl}/${SupabaseConfig.TABLE_PRO_TIP_UPVOTES}"
                val payload = buildJsonObject {
                    put("id", voteId)
                    put("tip_id", tipId)
                    put("user_id", userId)
                }
                httpClient.post(postUrl) {
                    headers {
                        append("apikey", SupabaseConfig.supabaseAnonKey)
                        append("Authorization", "Bearer ${SupabaseConfig.supabaseAnonKey}")
                    }
                    contentType(ContentType.Application.Json)
                    setBody(payload.toString())
                }
                if (tip != null) {
                    patchUserProfileStats(userId = tip.userId, pointsDelta = 1)
                }
            }

            val patchTipUrl = "${SupabaseConfig.restApiUrl}/${SupabaseConfig.TABLE_HARDWARE_PRO_TIPS}?id=eq.$tipId"
            httpClient.patch(patchTipUrl) {
                headers {
                    append("apikey", SupabaseConfig.supabaseAnonKey)
                    append("Authorization", "Bearer ${SupabaseConfig.supabaseAnonKey}")
                }
                contentType(ContentType.Application.Json)
                setBody(buildJsonObject { put("upvotes_count", newVotes) }.toString())
            }

            _proTips.update { list ->
                list.map { if (it.id == tipId) it.copy(upvotesCount = newVotes, isUpvotedByMe = !alreadyVoted) else it }
            }

            DomainResult.Success(Unit)
        }.getOrElse { DomainResult.Failure(DomainError.Unknown(it)) }
    }

    override suspend fun createPriceAlert(alert: PriceDropAlert): DomainResult<Unit> = withContext(ioDispatcher) {
        runCatching {
            val url = "${SupabaseConfig.restApiUrl}/${SupabaseConfig.TABLE_PRICE_DROP_ALERTS}"
            val payload = buildJsonObject {
                put("id", alert.id)
                put("user_id", alert.userId)
                put("hardware_id", alert.hardwareId)
                put("hardware_name", alert.hardwareName)
                put("target_price_try", alert.targetPriceTry)
                put("only_retail_campaign", alert.onlyRetailCampaign)
                put("is_triggered", alert.isTriggered)
            }

            httpClient.post(url) {
                headers {
                    append("apikey", SupabaseConfig.supabaseAnonKey)
                    append("Authorization", "Bearer ${SupabaseConfig.supabaseAnonKey}")
                }
                contentType(ContentType.Application.Json)
                setBody(payload.toString())
            }

            _priceAlerts.update { listOf(alert) + it }
            DomainResult.Success(Unit)
        }.getOrElse { DomainResult.Failure(DomainError.Unknown(it)) }
    }

    override fun observeUserPriceAlerts(userId: String): Flow<List<PriceDropAlert>> =
        _priceAlerts.map { list -> list.filter { it.userId == userId } }

    override suspend fun deletePriceAlert(alertId: String): DomainResult<Unit> = withContext(ioDispatcher) {
        runCatching {
            val url = "${SupabaseConfig.restApiUrl}/${SupabaseConfig.TABLE_PRICE_DROP_ALERTS}?id=eq.$alertId"
            httpClient.delete(url) {
                headers {
                    append("apikey", SupabaseConfig.supabaseAnonKey)
                    append("Authorization", "Bearer ${SupabaseConfig.supabaseAnonKey}")
                }
            }
            _priceAlerts.update { list -> list.filterNot { it.id == alertId } }
            DomainResult.Success(Unit)
        }.getOrElse { DomainResult.Failure(DomainError.Unknown(it)) }
    }

    private fun seedDefaultHardwareReviews(): List<HardwareProTip> = listOf(
        // GPU Reviews
        HardwareProTip(
            id = "seed-review-gpu-1",
            hardwareId = "gpu-nvidia-geforce-rtx-4070-super",
            hardwareName = "NVIDIA GeForce RTX 4070 Super",
            userId = "usr-ahmet-kaya",
            userDisplayName = "AhmetKaya_PC",
            userBadge = "🏆 Sistem Mimarı",
            tipText = "1440p Ultra ayarlarda Cyberpunk 2077 ve Alan Wake 2'de DLSS 3 Frame Gen ile 110+ FPS alıyorum. Güç tüketimi 215W civarında kalıyor, kasa içi ısınmıyor.",
            upvotesCount = 38,
            isUpvotedByMe = false,
            createdAtEpochMs = 1730000000000L - 86400000L * 2,
            rating = 5,
        ),
        HardwareProTip(
            id = "seed-review-gpu-2",
            hardwareId = "gpu-nvidia-geforce-rtx-4070-super",
            hardwareName = "NVIDIA GeForce RTX 4070 Super",
            userId = "usr-silikon-avcisi",
            userDisplayName = "SilikonAvcısı",
            userBadge = "⚡ Overclock Ustası",
            tipText = "950mV voltaja undervolt yaptım, frekansı 2700 MHz'e kilitledim. Güç tüketimi 175W'a, fan sesi ise neredeyse sıfıra indi. Kesinlikle undervolt tavsiye ederim.",
            upvotesCount = 54,
            isUpvotedByMe = false,
            createdAtEpochMs = 1730000000000L - 86400000L * 5,
            rating = 5,
        ),
        HardwareProTip(
            id = "seed-review-gpu-3",
            hardwareId = "gpu-nvidia-geforce-rtx-4090",
            hardwareName = "NVIDIA GeForce RTX 4090",
            userId = "usr-donanim-canavari",
            userDisplayName = "TitanBuild",
            userBadge = "👑 Ekstrem Overclock",
            tipText = "4K 144Hz OLED panelde saf raster ve ray tracing performansında rakipsiz. 12VHPWR kablosunun bükülmeden en az 3.5 cm boşlukla takılmasına dikkat edin.",
            upvotesCount = 72,
            isUpvotedByMe = false,
            createdAtEpochMs = 1730000000000L - 86400000L * 3,
            rating = 5,
        ),

        // CPU Reviews
        HardwareProTip(
            id = "seed-review-cpu-1",
            hardwareId = "cpu-amd-ryzen-7-7800x3d",
            hardwareName = "AMD Ryzen 7 7800X3D",
            userId = "usr-fps-avcisi",
            userDisplayName = "FPS_Uzmanı",
            userBadge = "🏆 Sistem Mimarı",
            tipText = "3D V-Cache teknolojisi rekabetçi oyunlarda (CS2, Valorant, Warzone) %1 low FPS değerlerini uçuruyor. PBO Curve Optimizer ile -28 all core stabil çalıştı.",
            upvotesCount = 94,
            isUpvotedByMe = false,
            createdAtEpochMs = 1730000000000L - 86400000L * 4,
            rating = 5,
        ),
        HardwareProTip(
            id = "seed-review-cpu-2",
            hardwareId = "cpu-intel-core-i7-14700k",
            hardwareName = "Intel Core i7-14700K",
            userId = "usr-render-pro",
            userDisplayName = "KreatifMühendis",
            userBadge = "⚡ Donanım Gurusu",
            tipText = "Blender ve Premiere Pro render sürelerinde tam bir canavar. Anakart BIOS'undan Intel Baseline Profile seçip PL1/PL2 limitlerini 253W olarak sabitlemenizi öneririm.",
            upvotesCount = 47,
            isUpvotedByMe = false,
            createdAtEpochMs = 1730000000000L - 86400000L * 6,
            rating = 5,
        ),

        // Fan Reviews
        HardwareProTip(
            id = "seed-review-fan-1",
            hardwareId = "case-fan-lian-li-uni-fan-sl-inf-120",
            hardwareName = "Lian Li UNI Fan SL-INF 120",
            userId = "usr-rgb-sever",
            userDisplayName = "AuraMaster",
            userBadge = "🎨 Modlama Ustası",
            tipText = "Papatya dizilimi (daisy-chain) kablo karmaşasını tek kalemde bitiriyor. L-Connect 3 yazılımından ayna derinlik efektini ayarlayınca kasa içi görsel şölene dönüşüyor.",
            upvotesCount = 31,
            isUpvotedByMe = false,
            createdAtEpochMs = 1730000000000L - 86400000L * 1,
            rating = 5,
        ),
        HardwareProTip(
            id = "seed-review-fan-2",
            hardwareId = "case-fan-noctua-nf-a12x25-pwm",
            hardwareName = "Noctua NF-A12x25 PWM",
            userId = "usr-akustik-pro",
            userDisplayName = "SessizlikAşığı",
            userBadge = "🔧 Tecrübeli Kullanıcı",
            tipText = "Sterrox polimer malzeme sayesinde sıfır rezonans. %65 devire kadar fısıltı gibi çalışıyor, radyatör üstünde statik basıncı muhteşem.",
            upvotesCount = 62,
            isUpvotedByMe = false,
            createdAtEpochMs = 1730000000000L - 86400000L * 8,
            rating = 5,
        ),

        // UPS Reviews
        HardwareProTip(
            id = "seed-review-ups-1",
            hardwareId = "ups-apc-back-ups-pro-1500va",
            hardwareName = "APC Back-UPS Pro 1500VA",
            userId = "usr-enerji-uzmani",
            userDisplayName = "GüçMuhendisi",
            userBadge = "⚡ Donanım Gurusu",
            tipText = "Otomatik voltaj regülasyonu (AVR) şebeke dalgalanmalarında kusursuz koruma sağlıyor. 750W yük altındaki oyun kasamı ve çift monitörümü 14 dakika açık tuttu.",
            upvotesCount = 45,
            isUpvotedByMe = false,
            createdAtEpochMs = 1730000000000L - 86400000L * 3,
            rating = 5,
        ),
        HardwareProTip(
            id = "seed-review-ups-2",
            hardwareId = "ups-tuncmatik-digitech-pro-1000va",
            hardwareName = "Tunçmatik Digitech Pro 1000VA",
            userId = "usr-tr-kullanici",
            userDisplayName = "DonanımSeverTr",
            userBadge = "🔧 Tecrübeli Kullanıcı",
            tipText = "LCD ekranından anlık çekilen watt ve akü doluluk oranını görmek çok pratik. Ani kesintilerde gecikmesiz aküye geçiyor, veri kaybı yaşamıyorum.",
            upvotesCount = 28,
            isUpvotedByMe = false,
            createdAtEpochMs = 1730000000000L - 86400000L * 7,
            rating = 5,
        ),

        // Gaming Desk Reviews
        HardwareProTip(
            id = "seed-review-desk-1",
            hardwareId = "gaming-desk-secretlab-magnus-pro",
            hardwareName = "Secretlab Magnus Pro",
            userId = "usr-battlestation-king",
            userDisplayName = "ErgonomiUzmanı",
            userBadge = "🏆 Sistem Mimarı",
            tipText = "Entegre güç beslemeli bacak tasarımı ve manyetik kablo kanalı sayesinde masada sıfır kablo görünümü elde ediyorsunuz. Çift 34 inç monitör kolunu hiç esnemeden taşıyor.",
            upvotesCount = 59,
            isUpvotedByMe = false,
            createdAtEpochMs = 1730000000000L - 86400000L * 4,
            rating = 5,
        ),

        // Networking Reviews
        HardwareProTip(
            id = "seed-review-router-1",
            hardwareId = "router-asus-rog-rapture-gt-axe16000",
            hardwareName = "ASUS ROG Rapture GT-AXE16000",
            userId = "usr-ag-muhendisi",
            userDisplayName = "FiberNetworker",
            userBadge = "⚡ Donanım Gurusu",
            tipText = "6 GHz bandı sayesinde apartmandaki tüm parazitlerden kurtuldum. Çift 10G portu NAS sunucusu için inanılmaz bir lüks. Bufferbloat A+ skoru veriyor.",
            upvotesCount = 34,
            isUpvotedByMe = false,
            createdAtEpochMs = 1730000000000L - 86400000L * 2,
            rating = 5,
        ),
        HardwareProTip(
            id = "seed-review-router-2",
            hardwareId = "router-tp-link-archer-axe75",
            hardwareName = "TP-Link Archer AXE75",
            userId = "usr-oyuncu-mert",
            userDisplayName = "MertCan_Gamer",
            userBadge = "🎮 Espor Koçu",
            tipText = "Fiyatına göre Wi-Fi 6E desteği harika. 3 katlı evin her noktasına kayıpsız ulaştı, kablosuz VR başlığında gecikmesiz 1200 Mbps bağlantı sağladı.",
            upvotesCount = 41,
            isUpvotedByMe = false,
            createdAtEpochMs = 1730000000000L - 86400000L * 9,
            rating = 5,
        ),

        // Smartwatch Reviews
        HardwareProTip(
            id = "seed-review-watch-1",
            hardwareId = "smartwatch-apple-watch-ultra-2",
            hardwareName = "Apple Watch Ultra 2",
            userId = "usr-outdoor-pro",
            userDisplayName = "DogaVeSpor",
            userBadge = "🏆 Sistem Mimarı",
            tipText = "3000 nit tepe parlaklığı ile parlak öğle güneşinde bile ekran cam gibi net. Çift frekanslı GPS rotaları milimetrik çiziyor. Titanyum gövde darbeye dayanıklı.",
            upvotesCount = 53,
            isUpvotedByMe = false,
            createdAtEpochMs = 1730000000000L - 86400000L * 3,
            rating = 5,
        ),

        // Gaming Lighting Reviews
        HardwareProTip(
            id = "seed-review-light-1",
            hardwareId = "gaming-lighting-nanoleaf-shapes-triangles",
            hardwareName = "Nanoleaf Shapes Triangles",
            userId = "usr-streamer-can",
            userDisplayName = "YayıncıCan",
            userBadge = "🎨 Modlama Ustası",
            tipText = "Ekran aynalama moduyla oyun oynarken monitördeki renkler duvara taşıyor, odaya inanılmaz bir derinlik katıyor. Müzik senkronizasyonu da çok başarılı.",
            upvotesCount = 37,
            isUpvotedByMe = false,
            createdAtEpochMs = 1730000000000L - 86400000L * 5,
            rating = 5,
        )
    )

    private fun generateContextualFallbackTips(hardwareId: String): List<HardwareProTip> {
        val lower = hardwareId.lowercase()
        return when {
            lower.contains("fan") -> listOf(
                HardwareProTip(
                    id = "dyn-rev-$hardwareId-1",
                    hardwareId = hardwareId,
                    hardwareName = "Kasa Fanı",
                    userId = "usr-sogutma-avcisi",
                    userDisplayName = "KasaSogutmaUzmani",
                    userBadge = "🏆 Sistem Mimarı",
                    tipText = "PWM devir profilini %50-%65 arasına sabitledim, kasa içi hava akışını ciddi şekilde iyileştirdi. Titreşim önleyici padler sayesinde sıfır uğultu.",
                    upvotesCount = 24,
                    isUpvotedByMe = false,
                    createdAtEpochMs = 1730000000000L - 86400000L * 2,
                    rating = 5,
                ),
                HardwareProTip(
                    id = "dyn-rev-$hardwareId-2",
                    hardwareId = hardwareId,
                    hardwareName = "Kasa Fanı",
                    userId = "usr-rgb-gamer",
                    userDisplayName = "RGB_Gamer",
                    userBadge = "⚡ Donanım Gurusu",
                    tipText = "Kablo yönetimi ve zincirleme bağlantı özelliği montajı çok kolaylaştırdı. Aydınlatma geçişleri anakart yazılımı ile kusursuz senkronize oluyor.",
                    upvotesCount = 18,
                    isUpvotedByMe = false,
                    createdAtEpochMs = 1730000000000L - 86400000L * 4,
                    rating = 5,
                )
            )
            lower.contains("ups") -> listOf(
                HardwareProTip(
                    id = "dyn-rev-$hardwareId-1",
                    hardwareId = hardwareId,
                    hardwareName = "Kesintisiz Güç Kaynağı",
                    userId = "usr-elektrik-muh",
                    userDisplayName = "ElektrikMuhendisi",
                    userBadge = "🏆 Sistem Mimarı",
                    tipText = "Şebeke dalgalanmalarında gecikmesiz devreye giriyor. 750W sistem ve monitör bağlıyken elektrik kesintisinde 10-12 dakika kesintisiz çalışma sağladı.",
                    upvotesCount = 33,
                    isUpvotedByMe = false,
                    createdAtEpochMs = 1730000000000L - 86400000L * 2,
                    rating = 5,
                ),
                HardwareProTip(
                    id = "dyn-rev-$hardwareId-2",
                    hardwareId = hardwareId,
                    hardwareName = "Kesintisiz Güç Kaynağı",
                    userId = "usr-guvenli-pc",
                    userDisplayName = "GuvenliSistem",
                    userBadge = "⚡ Donanım Gurusu",
                    tipText = "Voltaj regülasyonu son derece stabil. Ani voltaj düşüşlerinde bilgisayarın kapanmasını tamamen önleyerek donanımı güvenceye alıyor.",
                    upvotesCount = 21,
                    isUpvotedByMe = false,
                    createdAtEpochMs = 1730000000000L - 86400000L * 5,
                    rating = 5,
                )
            )
            lower.contains("desk") || lower.contains("masa") -> listOf(
                HardwareProTip(
                    id = "dyn-rev-$hardwareId-1",
                    hardwareId = hardwareId,
                    hardwareName = "Oyuncu Masası",
                    userId = "usr-ergonomi-fan",
                    userDisplayName = "ErgonomiSevdalisi",
                    userBadge = "🏆 Sistem Mimarı",
                    tipText = "Masa tablası oldukça sağlam, çift ağır monitör kolu taktım en ufak esneme veya sallanma yapmadı. Kablo kanalı masa altını tertemiz tutuyor.",
                    upvotesCount = 29,
                    isUpvotedByMe = false,
                    createdAtEpochMs = 1730000000000L - 86400000L * 3,
                    rating = 5,
                ),
                HardwareProTip(
                    id = "dyn-rev-$hardwareId-2",
                    hardwareId = hardwareId,
                    hardwareName = "Oyuncu Masası",
                    userId = "usr-minimalist",
                    userDisplayName = "MinimalistSetup",
                    userBadge = "⚡ Donanım Gurusu",
                    tipText = "Yüzey kaplaması çizilmelere ve lekelere karşı çok dayanıklı. Montaj kılavuzu anlaşılır ve parçalar tam oturdu, çok memnunum.",
                    upvotesCount = 19,
                    isUpvotedByMe = false,
                    createdAtEpochMs = 1730000000000L - 86400000L * 6,
                    rating = 5,
                )
            )
            lower.contains("router") || lower.contains("modem") || lower.contains("access") || lower.contains("extender") || lower.contains("network") -> listOf(
                HardwareProTip(
                    id = "dyn-rev-$hardwareId-1",
                    hardwareId = hardwareId,
                    hardwareName = "Ağ Donanımı",
                    userId = "usr-ag-uzmani",
                    userDisplayName = "AgUzmani",
                    userBadge = "🏆 Sistem Mimarı",
                    tipText = "Wi-Fi sinyal menzili kalın duvarlara rağmen mükemmel. 1000 Mbps internetimin tamamını diğer odalara kayıpsız aktarıyor. Ping dalgalanması yaşatmıyor.",
                    upvotesCount = 37,
                    isUpvotedByMe = false,
                    createdAtEpochMs = 1730000000000L - 86400000L * 2,
                    rating = 5,
                ),
                HardwareProTip(
                    id = "dyn-rev-$hardwareId-2",
                    hardwareId = hardwareId,
                    hardwareName = "Ağ Donanımı",
                    userId = "usr-online-pro",
                    userDisplayName = "OnlineOyuncu",
                    userBadge = "⚡ Donanım Gurusu",
                    tipText = "QoS ve oyun önceliklendirme özelliği devredeyken evde başkaları video izlese bile oyunlarda ping sıçraması olmuyor, arayüzü çok kullanışlı.",
                    upvotesCount = 26,
                    isUpvotedByMe = false,
                    createdAtEpochMs = 1730000000000L - 86400000L * 5,
                    rating = 5,
                )
            )
            lower.contains("watch") || lower.contains("saat") -> listOf(
                HardwareProTip(
                    id = "dyn-rev-$hardwareId-1",
                    hardwareId = hardwareId,
                    hardwareName = "Akıllı Saat",
                    userId = "usr-tekno-takip",
                    userDisplayName = "TeknolojiTakipcisi",
                    userBadge = "🏆 Sistem Mimarı",
                    tipText = "Ekran parlaklığı doğrudan güneş ışığı altında bile cam gibi net okunuyor. Nabız ve uyku takibi medikal cihazlarla neredeyse birebir tutarlı.",
                    upvotesCount = 32,
                    isUpvotedByMe = false,
                    createdAtEpochMs = 1730000000000L - 86400000L * 3,
                    rating = 5,
                ),
                HardwareProTip(
                    id = "dyn-rev-$hardwareId-2",
                    hardwareId = hardwareId,
                    hardwareName = "Akıllı Saat",
                    userId = "usr-spor-yasam",
                    userDisplayName = "SporVeYasam",
                    userBadge = "⚡ Donanım Gurusu",
                    tipText = "Pil ömrü yoğun kullanımda 3 günü çok rahat çıkarıyor. Bildirim titreşimleri ve telefonla senkronizasyon kusursuz çalışıyor.",
                    upvotesCount = 23,
                    isUpvotedByMe = false,
                    createdAtEpochMs = 1730000000000L - 86400000L * 6,
                    rating = 5,
                )
            )
            lower.contains("light") || lower.contains("rgb") || lower.contains("aydinlatma") -> listOf(
                HardwareProTip(
                    id = "dyn-rev-$hardwareId-1",
                    hardwareId = hardwareId,
                    hardwareName = "Akıllı Aydınlatma",
                    userId = "usr-oda-tasarim",
                    userDisplayName = "OdaTasarimi",
                    userBadge = "🏆 Sistem Mimarı",
                    tipText = "Ekran aynalama moduyla oyun oynarken monitördeki renkler odaya taşıyor, atmosfer ikiye katlanıyor. Mobil uygulaması üzerinden renk paletleri çok zengin.",
                    upvotesCount = 27,
                    isUpvotedByMe = false,
                    createdAtEpochMs = 1730000000000L - 86400000L * 2,
                    rating = 5,
                ),
                HardwareProTip(
                    id = "dyn-rev-$hardwareId-2",
                    hardwareId = hardwareId,
                    hardwareName = "Akıllı Aydınlatma",
                    userId = "usr-aydinlatma-pro",
                    userDisplayName = "AydinlatmaUstasi",
                    userBadge = "⚡ Donanım Gurusu",
                    tipText = "Işık parlaklığı ve renk doygunluğu harika. Gece modu ve zamanlayıcı özellikleri günlük kullanımda büyük konfor sağlıyor.",
                    upvotesCount = 18,
                    isUpvotedByMe = false,
                    createdAtEpochMs = 1730000000000L - 86400000L * 5,
                    rating = 5,
                )
            )
            lower.contains("gpu") || lower.contains("rtx") || lower.contains("rx") -> listOf(
                HardwareProTip(
                    id = "dyn-rev-$hardwareId-1",
                    hardwareId = hardwareId,
                    hardwareName = "Ekran Kartı",
                    userId = "usr-grafik-pro",
                    userDisplayName = "GrafikTutkunu",
                    userBadge = "🏆 Sistem Mimarı",
                    tipText = "1440p ve 4K oyunlarda mükemmel FPS değerleri veriyor. Yük altındayken bile fanlar %55 devri geçmiyor ve sıcaklık 64 dereceyi aşmıyor.",
                    upvotesCount = 42,
                    isUpvotedByMe = false,
                    createdAtEpochMs = 1730000000000L - 86400000L * 2,
                    rating = 5,
                ),
                HardwareProTip(
                    id = "dyn-rev-$hardwareId-2",
                    hardwareId = hardwareId,
                    hardwareName = "Ekran Kartı",
                    userId = "usr-donanim-guru",
                    userDisplayName = "DonanimGurusu",
                    userBadge = "⚡ Donanım Gurusu",
                    tipText = "Güç verimliliği beklentimin üzerinde. Güncel sürücülerle coil whine sorunu yaşamadım, görüntü işleme ve render performansı çok başarılı.",
                    upvotesCount = 31,
                    isUpvotedByMe = false,
                    createdAtEpochMs = 1730000000000L - 86400000L * 4,
                    rating = 5,
                )
            )
            lower.contains("cpu") || lower.contains("ryzen") || lower.contains("core") -> listOf(
                HardwareProTip(
                    id = "dyn-rev-$hardwareId-1",
                    hardwareId = hardwareId,
                    hardwareName = "İşlemci",
                    userId = "usr-silikon-krali",
                    userDisplayName = "SilikonKralı",
                    userBadge = "🏆 Sistem Mimarı",
                    tipText = "Oyunlarda önbellek avantajı inanılmaz fark yaratıyor, %1 low FPS değerleri kaya gibi stabil. 240mm/360mm soğutucuyla sıcaklıklar çok kontrol edilebilir.",
                    upvotesCount = 49,
                    isUpvotedByMe = false,
                    createdAtEpochMs = 1730000000000L - 86400000L * 2,
                    rating = 5,
                ),
                HardwareProTip(
                    id = "dyn-rev-$hardwareId-2",
                    hardwareId = hardwareId,
                    hardwareName = "İşlemci",
                    userId = "usr-donanimci-ali",
                    userDisplayName = "DonanimciAli",
                    userBadge = "⚡ Donanım Gurusu",
                    tipText = "Çoklu çekirdek performansı ve günlük üretkenlik işlerinde çok akıcı. Uygun voltaj ayarlarıyla tam yük altında bile serin çalışıyor.",
                    upvotesCount = 35,
                    isUpvotedByMe = false,
                    createdAtEpochMs = 1730000000000L - 86400000L * 5,
                    rating = 5,
                )
            )
            else -> listOf(
                HardwareProTip(
                    id = "dyn-rev-$hardwareId-1",
                    hardwareId = hardwareId,
                    hardwareName = "Donanım",
                    userId = "usr-donanim-sever",
                    userDisplayName = "DonanimSever",
                    userBadge = "🏆 Sistem Mimarı",
                    tipText = "Malzeme kalitesi ve kutu içeriği çok başarılı. Beklentilerimi fazlasıyla karşıladı, sistemi kurarken hiçbir uyumluluk sorunu yaşamadım.",
                    upvotesCount = 28,
                    isUpvotedByMe = false,
                    createdAtEpochMs = 1730000000000L - 86400000L * 2,
                    rating = 5,
                ),
                HardwareProTip(
                    id = "dyn-rev-$hardwareId-2",
                    hardwareId = hardwareId,
                    hardwareName = "Donanım",
                    userId = "usr-tecrubeli-kullanici",
                    userDisplayName = "TecrubeliKullanici",
                    userBadge = "⚡ Donanım Gurusu",
                    tipText = "Uzun süreli kullanımda son derece kararlı ve stabil. Fiyat/performans oranı gayet tatmin edici, kesinlikle tavsiye ederim.",
                    upvotesCount = 22,
                    isUpvotedByMe = false,
                    createdAtEpochMs = 1730000000000L - 86400000L * 5,
                    rating = 5,
                )
            )
        }
    }
}
