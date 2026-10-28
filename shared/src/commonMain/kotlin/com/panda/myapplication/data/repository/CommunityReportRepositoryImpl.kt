package com.panda.myapplication.data.repository

import com.panda.myapplication.domain.common.DomainResult
import com.panda.myapplication.domain.model.ContentReport
import com.panda.myapplication.domain.model.ReportReason
import com.panda.myapplication.domain.model.ReportStatus
import com.panda.myapplication.domain.model.ReportTargetType
import com.panda.myapplication.domain.repository.CommunityReportRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.withContext

class CommunityReportRepositoryImpl(
    private val ioDispatcher: CoroutineDispatcher,
) : CommunityReportRepository {

    private val _reports = MutableStateFlow<List<ContentReport>>(
        listOf(
            ContentReport(
                id = "rep-init-1",
                targetType = ReportTargetType.TUNING_PROFILE,
                targetId = "tuning-sample-1",
                targetTitle = "Ryzen 7 7800X3D + 1.45V Aşırı Voltaj Profili",
                reportedUserId = "user-risky-10",
                reportedUserDisplayName = "VoltMonster",
                reporterUserId = "user-community-1",
                reporterUserDisplayName = "SilisyumUzmanı",
                isSelfReport = false,
                reason = ReportReason.DANGEROUS_VOLTAGE,
                details = "7800X3D için 1.45V SoC voltajı işlemciyi yakabilir. AMD maksimum 1.30V öneriyor.",
                timestampMillis = 1730000000000L,
                status = ReportStatus.PENDING,
            ),
            ContentReport(
                id = "rep-init-2",
                targetType = ReportTargetType.BATTLESTATION,
                targetId = "battlestation-sample-2",
                targetTitle = "Custom Lian Li O11 RGB Setup",
                reportedUserId = "user-self-demo",
                reportedUserDisplayName = "MasaMimarı",
                reporterUserId = "user-self-demo",
                reporterUserDisplayName = "MasaMimarı",
                isSelfReport = true,
                reason = ReportReason.AUTHOR_RETRACTION,
                details = "Kendi sistem fotoğrafımı güncelleyeceğim, eski sistemimi yayından kaldırmanızı rica ediyorum.",
                timestampMillis = 1730010000000L,
                status = ReportStatus.PENDING,
            )
        )
    )

    override fun observePendingReports(): Flow<List<ContentReport>> {
        return _reports.map { list -> list.filter { it.status == ReportStatus.PENDING } }
    }

    override fun observeAllReports(): Flow<List<ContentReport>> {
        return _reports.asStateFlow()
    }

    override suspend fun submitReport(report: ContentReport): DomainResult<Unit> = withContext(ioDispatcher) {
        _reports.update { current ->
            listOf(report) + current
        }
        DomainResult.Success(Unit)
    }

    override suspend fun resolveReport(reportId: String, newStatus: ReportStatus): DomainResult<Unit> = withContext(ioDispatcher) {
        _reports.update { current ->
            current.map { r ->
                if (r.id == reportId) r.copy(status = newStatus) else r
            }
        }
        DomainResult.Success(Unit)
    }

    override suspend fun deleteReport(reportId: String): DomainResult<Unit> = withContext(ioDispatcher) {
        _reports.update { current ->
            current.filterNot { it.id == reportId }
        }
        DomainResult.Success(Unit)
    }
}
