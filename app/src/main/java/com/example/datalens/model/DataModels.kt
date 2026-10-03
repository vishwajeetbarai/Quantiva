package com.example.datalens.model

enum class ColumnType {
    NUMERICAL,
    CATEGORICAL,
    DATE,
    BOOLEAN,
    TEXT
}

data class DataColumnInfo(
    val name: String,
    val type: ColumnType,
    val nullCount: Int = 0,
    val uniqueCount: Int = 0,
    val sampleValues: List<String> = emptyList(),
    val min: Double? = null,
    val max: Double? = null,
    val mean: Double? = null,
    val median: Double? = null,
    val sum: Double? = null,
    val topCategories: Map<String, Int> = emptyMap()
)

data class ParsedDataset(
    val id: String = java.util.UUID.randomUUID().toString(),
    val name: String,
    val rowCount: Int,
    val columnCount: Int,
    val columns: List<DataColumnInfo>,
    val headers: List<String>,
    val rows: List<List<String>>,
    val fileFormat: String,
    val fileSizeString: String,
    val uploadedAt: Long = System.currentTimeMillis()
)

data class DatasetProfile(
    val datasetName: String,
    val totalRows: Int,
    val totalColumns: Int,
    val qualityScore: Int, // 0 - 100
    val missingColumnsCount: Int,
    val totalMissingCells: Int,
    val duplicateRowsCount: Int,
    val potentialAnomaliesCount: Int,
    val dateRange: String?,
    val numericalColumns: List<String>,
    val categoricalColumns: List<String>,
    val dateColumns: List<String>,
    val plainLanguageExplanations: List<String>
)

enum class InsightCategory(val displayName: String) {
    TREND("Trend"),
    ANOMALY("Anomaly"),
    TOP_PERFORMER("Top Performer"),
    UNDERPERFORMER("Underperformer"),
    CORRELATION("Correlation"),
    DISTRIBUTION("Distribution"),
    SEGMENT("Segment"),
    BUSINESS_RISK("Business Risk"),
    OPPORTUNITY("Opportunity")
}

enum class Severity {
    CRITICAL,
    WARNING,
    NEUTRAL,
    POSITIVE
}

enum class ChartType {
    BAR,
    LINE,
    DONUT,
    HISTOGRAM,
    SCATTER,
    KPI_CARD,
    TABLE
}

data class ChartDataPoint(
    val label: String,
    val value: Double,
    val secondaryValue: Double? = null,
    val colorHex: String? = null
)

data class DataInsight(
    val id: String = java.util.UUID.randomUUID().toString(),
    val category: InsightCategory,
    val title: String,
    val metric: String,
    val explanation: String,
    val evidence: String,
    val severity: Severity,
    val recommendedInvestigation: String,
    val chartType: ChartType = ChartType.BAR,
    val chartData: List<ChartDataPoint> = emptyList(),
    val breakdownRows: List<Pair<String, String>> = emptyList(),
    val sensitivitySimulation: String? = null,
    val actionChecklist: List<String> = emptyList()
)

data class DataQualityIssue(
    val id: String = java.util.UUID.randomUUID().toString(),
    val columnName: String,
    val category: String, // "Inconsistent Categories", "Missing Values", "Duplicate Records", "Potential Outliers"
    val problem: String,
    val affectedRows: Int,
    val sampleValues: List<String>,
    val suggestedFix: String,
    val fixActionType: String,
    val canonicalValue: String? = null,
    val previewTransformations: List<Pair<String, String>> = emptyList()
)

data class AskDataMessage(
    val id: String = java.util.UUID.randomUUID().toString(),
    val text: String,
    val isUser: Boolean,
    val timestamp: Long = System.currentTimeMillis(),
    val directAnswer: String? = null,
    val keyMetric: String? = null,
    val calculationEvidence: String? = null,
    val chartType: ChartType? = null,
    val chartData: List<ChartDataPoint> = emptyList(),
    val tableHeaders: List<String>? = null,
    val tableSnippet: List<List<String>>? = null,
    val verificationBadge: String = "Verified by Analytics Engine"
)

data class BusinessReport(
    val id: String = java.util.UUID.randomUUID().toString(),
    val datasetName: String,
    val generatedAt: Long = System.currentTimeMillis(),
    val executiveSummary: String,
    val keyKpis: List<Pair<String, String>>,
    val majorTrends: List<String>,
    val importantAnomalies: List<String>,
    val topPerformers: List<String>,
    val poorPerformers: List<String>,
    val dataQualityIssues: List<String>,
    val recommendations: List<String>,
    val roadmap30Days: List<String> = emptyList(),
    val roadmap90Days: List<String> = emptyList()
)

// Phase 4: Advanced Analytics, User Profiles & Forecasting
data class UserProfile(
    val name: String = "Vishwajeet B.",
    val email: String = "vishwajeetb17@gmail.com",
    val role: String = "Executive Director", // "Executive Director", "Senior Financial Analyst", "Operations Lead"
    val workspace: String = "Global Retail Operations",
    val avatarInitials: String = "VB"
)

data class ForecastResult(
    val historicalPoints: List<ChartDataPoint>,
    val baselinePoints: List<ChartDataPoint>,
    val optimisticPoints: List<ChartDataPoint>,
    val conservativePoints: List<ChartDataPoint>,
    val projectedQ4Revenue: String,
    val forecastModelName: String = "Holt's Linear Exponential Smoothing",
    val confidenceIntervalPct: Double = 95.0
)

data class ParetoSegment(
    val segmentName: String,
    val shareOfRevenuePct: Double,
    val shareOfEntitiesPct: Double,
    val count: Int,
    val description: String
)

data class SimulationResult(
    val projectedRevenue: String,
    val projectedProfit: String,
    val netProfitDelta: String,
    val grossMarginPct: String,
    val isPositiveDelta: Boolean = true
)
