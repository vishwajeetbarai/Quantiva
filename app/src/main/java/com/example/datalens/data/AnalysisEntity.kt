package com.example.datalens.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "analyses")
data class AnalysisEntity(
    @PrimaryKey
    val id: String,
    val fileName: String,
    val fileSize: String,
    val rowCount: Int,
    val columnCount: Int,
    val qualityScore: Int,
    val dateRange: String?,
    val analyzedAt: Long = System.currentTimeMillis(),
    val topInsightTitle: String?,
    val topInsightMetric: String?,
    val executiveSummary: String?
)
