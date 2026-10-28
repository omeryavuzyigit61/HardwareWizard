package com.panda.myapplication.domain.repository

import com.panda.myapplication.domain.common.DomainResult
import com.panda.myapplication.domain.model.ContentReport
import com.panda.myapplication.domain.model.ReportStatus
import kotlinx.coroutines.flow.Flow

interface CommunityReportRepository {
    fun observePendingReports(): Flow<List<ContentReport>>
    fun observeAllReports(): Flow<List<ContentReport>>
    suspend fun submitReport(report: ContentReport): DomainResult<Unit>
    suspend fun resolveReport(reportId: String, newStatus: ReportStatus): DomainResult<Unit>
    suspend fun deleteReport(reportId: String): DomainResult<Unit>
}
