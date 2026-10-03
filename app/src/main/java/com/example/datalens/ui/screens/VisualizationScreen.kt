package com.example.datalens.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.datalens.model.ChartDataPoint
import com.example.datalens.model.ChartType
import com.example.datalens.ui.components.DataLensBarChart
import com.example.datalens.ui.components.DataLensDonutChart
import com.example.datalens.ui.components.DataLensLineChart
import com.example.datalens.ui.components.SectionHeader
import com.example.datalens.viewmodel.DataLensViewModel
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VisualizationScreen(
    viewModel: DataLensViewModel,
    modifier: Modifier = Modifier
) {
    val dataset by viewModel.activeDataset.collectAsState()
    val insights by viewModel.insights.collectAsState()

    var selectedChartType by remember { mutableStateOf(ChartType.LINE) }

    val monthlyPoints = remember(insights) {
        insights.firstOrNull { it.chartType == ChartType.LINE }?.chartData ?: listOf(
            ChartDataPoint("Jul", 2000000.0),
            ChartDataPoint("Aug", 1640000.0),
            ChartDataPoint("Sep", 2660000.0)
        )
    }

    val productPoints = remember(insights) {
        insights.firstOrNull { it.chartType == ChartType.BAR }?.chartData ?: listOf(
            ChartDataPoint("UltraBook", 2015000.0),
            ChartDataPoint("Cloud Server", 1000000.0),
            ChartDataPoint("AI Suite", 1800000.0),
            ChartDataPoint("Monitor 32", 858000.0)
        )
    }

    val categoryPoints = remember(insights) {
        insights.firstOrNull { it.chartType == ChartType.DONUT }?.chartData ?: listOf(
            ChartDataPoint("Electronics", 3040000.0),
            ChartDataPoint("Cloud Software", 2800000.0),
            ChartDataPoint("Furniture", 1000000.0),
            ChartDataPoint("Office Supplies", 450000.0)
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Visual Analytics",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )
                        Text(
                            text = "Auto-rendered dynamic charts",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
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
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Chart Type Selector
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = selectedChartType == ChartType.LINE,
                        onClick = { selectedChartType = ChartType.LINE },
                        label = { Text("Trend (Line)") },
                        modifier = Modifier.testTag("chart_filter_line")
                    )
                    FilterChip(
                        selected = selectedChartType == ChartType.BAR,
                        onClick = { selectedChartType = ChartType.BAR },
                        label = { Text("Rank (Bar)") },
                        modifier = Modifier.testTag("chart_filter_bar")
                    )
                    FilterChip(
                        selected = selectedChartType == ChartType.DONUT,
                        onClick = { selectedChartType = ChartType.DONUT },
                        label = { Text("Mix (Donut)") },
                        modifier = Modifier.testTag("chart_filter_donut")
                    )
                }
            }

            // Primary Interactive Chart View
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = when (selectedChartType) {
                                        ChartType.LINE -> "Monthly Revenue Trajectory"
                                        ChartType.BAR -> "Top Products by Volume"
                                        ChartType.DONUT -> "Category Revenue Composition"
                                        else -> "Distribution View"
                                    },
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "Computed directly from ${dataset?.name ?: "active dataset"}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant
                            ) {
                                Text(
                                    text = selectedChartType.name,
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold
                                    ),
                                    color = CyanPrimary,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(18.dp))

                        when (selectedChartType) {
                            ChartType.LINE -> DataLensLineChart(points = monthlyPoints)
                            ChartType.BAR -> DataLensBarChart(points = productPoints)
                            ChartType.DONUT -> DataLensDonutChart(points = categoryPoints)
                            else -> DataLensBarChart(points = productPoints)
                        }
                    }
                }
            }

            // Chart Guidelines Explainer
            item {
                SectionHeader(title = "Auto-Selection Intelligence")
            }

            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        ChartRuleItem(
                            type = "Time Series Data",
                            chart = "Line Chart",
                            reason = "Preserves chronological continuity to highlight growth velocity and seasonal drops."
                        )
                        HorizontalDivider(modifier = Modifier.padding(vertical = 10.dp))
                        ChartRuleItem(
                            type = "Categorical Comparison",
                            chart = "Bar Chart",
                            reason = "Provides rapid visual ranking of leaders and underperformers."
                        )
                        HorizontalDivider(modifier = Modifier.padding(vertical = 10.dp))
                        ChartRuleItem(
                            type = "Share of Total",
                            chart = "Donut Chart",
                            reason = "Visualizes market concentration and portfolio risk."
                        )
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}

@Composable
private fun ChartRuleItem(type: String, chart: String, reason: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Top
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = type,
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = reason,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Surface(
            shape = RoundedCornerShape(6.dp),
            color = CyanLight
        ) {
            Text(
                text = chart,
                style = MaterialTheme.typography.labelSmall.copy(
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold
                ),
                color = CyanPrimaryDark,
                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
            )
        }
    }
}
