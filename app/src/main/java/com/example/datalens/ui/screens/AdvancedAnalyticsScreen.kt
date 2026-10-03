package com.example.datalens.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.datalens.engine.AnalyticsEngine
import com.example.datalens.model.ChartDataPoint
import com.example.datalens.model.ForecastResult
import com.example.datalens.model.ParetoSegment
import com.example.datalens.model.SimulationResult
import com.example.datalens.ui.components.DataLensLineChart
import com.example.datalens.ui.components.MetricCard
import com.example.datalens.ui.components.SectionHeader
import com.example.datalens.viewmodel.DataLensViewModel
import com.example.ui.theme.*
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdvancedAnalyticsScreen(
    viewModel: DataLensViewModel,
    modifier: Modifier = Modifier
) {
    val dataset by viewModel.activeDataset.collectAsState()

    var forecastMode by remember { mutableStateOf("Baseline") }
    var discountSlider by remember { mutableStateOf(0f) } // -5% to +5%
    var cloudAttachSlider by remember { mutableStateOf(25f) } // 10% to 50%
    var bufferStockSlider by remember { mutableStateOf(30f) } // 0 to 60 units

    val forecastResult = remember(dataset) {
        if (dataset != null) AnalyticsEngine.calculateForecast(dataset!!)
        else ForecastResult(emptyList(), emptyList(), emptyList(), emptyList(), "₹0")
    }

    val paretoSegments = remember(dataset) {
        if (dataset != null) AnalyticsEngine.calculateParetoAnalysis(dataset!!)
        else emptyList()
    }

    val simulationResult = remember(dataset, discountSlider, cloudAttachSlider, bufferStockSlider) {
        if (dataset != null) {
            AnalyticsEngine.simulateFinancialScenario(
                dataset = dataset!!,
                discountAdjustmentPct = discountSlider.toDouble(),
                cloudAttachRatePct = cloudAttachSlider.toDouble(),
                supplyBufferUnits = bufferStockSlider.toInt()
            )
        } else {
            SimulationResult("₹0", "₹0", "₹0", "0%")
        }
    }

    val combinedForecastPoints = remember(forecastResult, forecastMode) {
        val future = when (forecastMode) {
            "Optimistic" -> forecastResult.optimisticPoints
            "Conservative" -> forecastResult.conservativePoints
            else -> forecastResult.baselinePoints
        }
        forecastResult.historicalPoints + future
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Advanced Analytics & Forecasting",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )
                        Text(
                            text = "Predictive models & financial simulation",
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
            // 1. Predictive Demand & Revenue Forecast
            item {
                SectionHeader(
                    title = "Predictive Q4 Demand Forecast",
                    subtitle = "Exponential trend smoothing with 95% confidence intervals"
                )
            }

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
                                    text = "Projected Q4 Revenue",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = forecastResult.projectedQ4Revenue,
                                    style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                                    color = CyanPrimary
                                )
                            }

                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                listOf("Baseline", "Optimistic", "Conservative").forEach { mode ->
                                    FilterChip(
                                        selected = forecastMode == mode,
                                        onClick = { forecastMode = mode },
                                        label = {
                                            Text(
                                                text = mode,
                                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp)
                                            )
                                        },
                                        shape = RoundedCornerShape(8.dp)
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        DataLensLineChart(points = combinedForecastPoints, lineColor = CyanPrimary)

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = "Historical: Jul, Aug, Sep • Projected: Oct, Nov, Dec (Year-end surge modeled)",
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // 2. Interactive ROI & Financial Sensitivity Simulator
            item {
                SectionHeader(
                    title = "Financial Sensitivity Sandbox",
                    subtitle = "Adjust strategic levers to forecast net profit impact"
                )
            }

            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        // Projected Output Card
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text(
                                            text = "Simulated Gross Profit",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        Text(
                                            text = simulationResult.projectedProfit,
                                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                    }

                                    Surface(
                                        shape = RoundedCornerShape(6.dp),
                                        color = if (simulationResult.isPositiveDelta) EmeraldGreen.copy(alpha = 0.2f) else RoseRisk.copy(alpha = 0.2f)
                                    ) {
                                        Text(
                                            text = simulationResult.netProfitDelta,
                                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                            color = if (simulationResult.isPositiveDelta) EmeraldGreen else RoseRisk,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(8.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = "Revenue: ${simulationResult.projectedRevenue}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Text(
                                        text = "Margin: ${simulationResult.grossMarginPct}",
                                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                                        color = CyanPrimary
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Lever 1: Discount Rate
                        Text(
                            text = "Discount Rate Adjustment: ${String.format(Locale.US, "%+.1f", discountSlider)}%",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Slider(
                            value = discountSlider,
                            onValueChange = { discountSlider = it },
                            valueRange = -5f..5f,
                            steps = 9,
                            modifier = Modifier.testTag("discount_slider")
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        // Lever 2: Cloud Software Attach Rate
                        Text(
                            text = "Cloud Cross-Sell Attachment Target: ${cloudAttachSlider.toInt()}%",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Slider(
                            value = cloudAttachSlider,
                            onValueChange = { cloudAttachSlider = it },
                            valueRange = 10f..50f,
                            steps = 7,
                            modifier = Modifier.testTag("cloud_attach_slider")
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        // Lever 3: Supply Buffer Units
                        Text(
                            text = "Category A Buffer Inventory: ${bufferStockSlider.toInt()} units",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Slider(
                            value = bufferStockSlider,
                            onValueChange = { bufferStockSlider = it },
                            valueRange = 0f..60f,
                            steps = 5,
                            modifier = Modifier.testTag("buffer_stock_slider")
                        )
                    }
                }
            }

            // 3. Pareto 80/20 Account Tiering
            item {
                SectionHeader(
                    title = "Pareto 80/20 Distribution Analysis",
                    subtitle = "Strategic classification of clients & revenue concentration"
                )
            }

            items(paretoSegments) { segment ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = segment.segmentName,
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = CyanLight
                            ) {
                                Text(
                                    text = "${String.format(Locale.US, "%.1f", segment.shareOfRevenuePct)}% Rev",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold
                                    ),
                                    color = CyanPrimaryDark,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        Text(
                            text = segment.description,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
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
