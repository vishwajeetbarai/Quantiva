package com.example.datalens.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.datalens.model.ChartType
import com.example.datalens.model.DataInsight
import com.example.datalens.model.InsightCategory
import com.example.datalens.model.Severity
import com.example.datalens.ui.components.DataLensBarChart
import com.example.datalens.ui.components.DataLensDonutChart
import com.example.datalens.ui.components.DataLensLineChart
import com.example.datalens.viewmodel.DataLensViewModel
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InsightsScreen(
    viewModel: DataLensViewModel,
    modifier: Modifier = Modifier
) {
    val insights by viewModel.insights.collectAsState()
    val selectedCategory by viewModel.selectedInsightCategory.collectAsState()
    val selectedDetail by viewModel.selectedInsightDetail.collectAsState()

    val filteredInsights = remember(insights, selectedCategory) {
        if (selectedCategory == null) insights else insights.filter { it.category == selectedCategory }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Automated Insights",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { viewModel.handleBack() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Category Filter Pills
            item {
                val scrollState = rememberScrollState()
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(scrollState),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = selectedCategory == null,
                        onClick = { viewModel.setInsightFilter(null) },
                        label = { Text("All (${insights.size})") }
                    )
                    InsightCategory.values().forEach { cat ->
                        val count = insights.count { it.category == cat }
                        if (count > 0) {
                            FilterChip(
                                selected = selectedCategory == cat,
                                onClick = { viewModel.setInsightFilter(if (selectedCategory == cat) null else cat) },
                                label = { Text("${cat.displayName} ($count)") }
                            )
                        }
                    }
                }
            }

            if (filteredInsights.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(32.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = Icons.Default.Info,
                                contentDescription = null,
                                tint = CyanPrimary,
                                modifier = Modifier.size(36.dp)
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                text = "No insights found in this category.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            } else {
                items(filteredInsights) { insight ->
                    InsightCard(
                        insight = insight,
                        onClick = { viewModel.selectInsightForDetail(insight) }
                    )
                }
            }

            item {
                Spacer(modifier = Modifier.height(20.dp))
            }
        }
    }

    // Insight Deep Dive Modal / Dialog
    if (selectedDetail != null) {
        val detail = selectedDetail!!
        AlertDialog(
            onDismissRequest = { viewModel.selectInsightForDetail(null) },
            icon = {
                Icon(
                    imageVector = Icons.Default.Troubleshoot,
                    contentDescription = null,
                    tint = CyanPrimary
                )
            },
            title = {
                Column {
                    Text(
                        text = detail.title,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "${detail.category.displayName} • ${detail.metric}",
                        style = MaterialTheme.typography.labelSmall,
                        color = CyanPrimary
                    )
                }
            },
            text = {
                LazyColumn(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    item {
                        Text(
                            text = detail.explanation,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    // Sensitivity / What If Simulation
                    if (detail.sensitivitySimulation != null) {
                        item {
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = CyanLight.copy(alpha = 0.6f),
                                border = androidx.compose.foundation.BorderStroke(1.dp, CyanPrimary.copy(alpha = 0.3f))
                            ) {
                                Column(modifier = Modifier.padding(10.dp)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.AutoMirrored.Filled.TrendingUp, contentDescription = null, tint = CyanPrimaryDark, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = "What-If Simulation",
                                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                            color = CyanPrimaryDark
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = detail.sensitivitySimulation ?: "",
                                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                                        color = Slate800
                                    )
                                }
                            }
                        }
                    }

                    // Contributing Breakdown Rows
                    if (detail.breakdownRows.isNotEmpty()) {
                        item {
                            Text(
                                text = "Contributing Dimensions:",
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                        items(detail.breakdownRows) { (k, v) ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f), RoundedCornerShape(6.dp))
                                    .padding(horizontal = 10.dp, vertical = 6.dp),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(text = k, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface)
                                Text(text = v, style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold), color = CyanPrimary)
                            }
                        }
                    }

                    // Evidence
                    item {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Text(
                                    text = "Mathematical Proof:",
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Spacer(modifier = Modifier.height(3.dp))
                                Text(
                                    text = detail.evidence,
                                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }

                    // Action Checklist
                    if (detail.actionChecklist.isNotEmpty()) {
                        item {
                            Text(
                                text = "Recommended Action Checklist:",
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                        items(detail.actionChecklist) { action ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.Top
                            ) {
                                Icon(Icons.Default.CheckBoxOutlineBlank, contentDescription = null, tint = CyanPrimary, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = action,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = { viewModel.selectInsightForDetail(null) },
                    colors = ButtonDefaults.buttonColors(containerColor = CyanPrimary)
                ) {
                    Text("Done", color = Slate950, fontWeight = FontWeight.Bold)
                }
            }
        )
    }
}

@Composable
private fun InsightCard(
    insight: DataInsight,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .testTag("insight_card_${insight.id}"),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header Row: Category Badge + Severity
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant
                ) {
                    Text(
                        text = insight.category.displayName.uppercase(),
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold
                        ),
                        color = CyanPrimary,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }

                SeverityBadge(severity = insight.severity)
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Title and Metric
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Text(
                    text = insight.title,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.weight(1f)
                )

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
                ) {
                    Text(
                        text = insight.metric,
                        style = MaterialTheme.typography.titleSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = CyanPrimary
                        ),
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = insight.explanation,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            // Embedded Supporting Chart
            if (insight.chartData.isNotEmpty()) {
                Spacer(modifier = Modifier.height(14.dp))
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                ) {
                    Box(modifier = Modifier.padding(12.dp)) {
                        when (insight.chartType) {
                            ChartType.LINE -> DataLensLineChart(points = insight.chartData)
                            ChartType.DONUT -> DataLensDonutChart(points = insight.chartData)
                            else -> DataLensBarChart(points = insight.chartData)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Supporting Evidence & Mathematical Proof
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
            ) {
                Column(modifier = Modifier.padding(10.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Calculate,
                            contentDescription = null,
                            tint = IndigoAccent,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Supporting Evidence & Calculation",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            ),
                            color = IndigoAccent
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = insight.evidence,
                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Based on your uploaded data.",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontSize = 9.sp,
                            fontWeight = FontWeight.SemiBold
                        ),
                        color = EmeraldGreen
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Recommended Investigation
            Row(
                verticalAlignment = Alignment.Top,
                modifier = Modifier.padding(top = 2.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = null,
                    tint = CyanPrimary,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Recommended Investigation:",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        ),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = insight.recommendedInvestigation,
                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.OpenInNew,
                    contentDescription = "Details",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}

@Composable
fun SeverityBadge(severity: Severity) {
    val (bg, fg, label) = when (severity) {
        Severity.CRITICAL -> Triple(RoseLight, RoseRisk, "Critical")
        Severity.WARNING -> Triple(AmberLight, AmberWarning, "Attention")
        Severity.POSITIVE -> Triple(EmeraldLight, EmeraldGreen, "Positive")
        Severity.NEUTRAL -> Triple(Slate100, Slate700, "Observation")
    }

    Surface(
        shape = RoundedCornerShape(6.dp),
        color = bg
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall.copy(
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold
            ),
            color = fg,
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
        )
    }
}
