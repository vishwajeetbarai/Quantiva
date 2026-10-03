package com.example.datalens.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.datalens.model.ColumnType
import com.example.datalens.model.DataColumnInfo
import com.example.datalens.ui.components.HealthScoreBadge
import com.example.datalens.ui.components.MetricCard
import com.example.datalens.ui.components.SectionHeader
import com.example.datalens.viewmodel.AppScreen
import com.example.datalens.viewmodel.DataLensViewModel
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OverviewScreen(
    viewModel: DataLensViewModel,
    modifier: Modifier = Modifier
) {
    val dataset by viewModel.activeDataset.collectAsState()
    val profile by viewModel.datasetProfile.collectAsState()

    var showRawTable by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = dataset?.name ?: "Dataset Profile",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            maxLines = 1
                        )
                        Text(
                            text = "${dataset?.rowCount ?: 0} rows • ${dataset?.columnCount ?: 0} columns",
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
                actions = {
                    IconButton(
                        onClick = { viewModel.navigateTo(AppScreen.ASK_DATA) },
                        modifier = Modifier.testTag("ask_data_top_action")
                    ) {
                        Icon(
                            imageVector = Icons.Default.ChatBubbleOutline,
                            contentDescription = "Ask Data",
                            tint = CyanPrimary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        }
    ) { innerPadding ->
        if (dataset == null || profile == null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "No active dataset. Upload a file or load sample data.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            return@Scaffold
        }

        val d = dataset!!
        val p = profile!!

        LazyColumn(
            modifier = modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // "Your data is ready." Banner
            item {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = EmeraldLight.copy(alpha = 0.6f),
                    border = androidx.compose.foundation.BorderStroke(1.dp, EmeraldGreen.copy(alpha = 0.3f))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = EmeraldGreen,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Your data is ready.",
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                color = EmeraldGreen
                            )
                            Text(
                                text = "Structure detected, data types indexed, and automated profile generated.",
                                style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                                color = Slate700
                            )
                        }
                    }
                }
            }

            // High-Level Dataset Overview Grid
            item {
                SectionHeader(title = "Dataset Overview")
            }

            item {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        MetricCard(
                            title = "Rows",
                            value = "${p.totalRows}",
                            subtitle = "Records loaded",
                            modifier = Modifier.weight(1f)
                        )
                        MetricCard(
                            title = "Columns",
                            value = "${p.totalColumns}",
                            subtitle = "Dimensions & metrics",
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        MetricCard(
                            title = "Data Quality",
                            value = "${p.qualityScore}/100",
                            delta = if (p.qualityScore >= 80) "Optimal" else "Needs Fixes",
                            isPositiveDelta = p.qualityScore >= 80,
                            modifier = Modifier.weight(1f),
                            onClick = { viewModel.navigateTo(AppScreen.QUALITY) }
                        )
                        MetricCard(
                            title = "Date Range",
                            value = p.dateRange ?: "N/A",
                            subtitle = "Active timeline",
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        MetricCard(
                            title = "Missing Values",
                            value = "${p.missingColumnsCount} cols",
                            subtitle = "${p.totalMissingCells} empty cells",
                            modifier = Modifier.weight(1f)
                        )
                        MetricCard(
                            title = "Duplicate Rows",
                            value = "${p.duplicateRowsCount}",
                            subtitle = "Identical entries",
                            modifier = Modifier.weight(1f)
                        )
                        MetricCard(
                            title = "Anomalies",
                            value = "${p.potentialAnomaliesCount}",
                            subtitle = "IQR outliers",
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            // Plain-Language Explanations Section
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Lightbulb,
                                contentDescription = null,
                                tint = AmberWarning,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Analyst Observations (Plain Language)",
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        p.plainLanguageExplanations.forEach { explanation ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                verticalAlignment = Alignment.Top
                            ) {
                                Text(
                                    text = "•",
                                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                    color = CyanPrimary,
                                    modifier = Modifier.padding(end = 8.dp)
                                )
                                Text(
                                    text = explanation,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }
                }
            }

            // Quick Next Step Pills
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = { viewModel.navigateTo(AppScreen.INSIGHTS) },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = CyanPrimary)
                    ) {
                        Text(
                            text = "View Insights",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                            color = Slate950
                        )
                    }

                    OutlinedButton(
                        onClick = { viewModel.navigateTo(AppScreen.ASK_DATA) },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = "Ask Data",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }

            // Column Structure Breakdown
            item {
                SectionHeader(
                    title = "Column Structure & Types",
                    subtitle = "${d.columns.size} detected fields"
                )
            }

            items(d.columns) { col ->
                ColumnDetailCard(col = col)
            }

            // Raw Data Preview Toggle
            item {
                OutlinedButton(
                    onClick = { showRawTable = !showRawTable },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(
                        imageVector = if (showRawTable) Icons.Default.VisibilityOff else Icons.Default.TableChart,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(text = if (showRawTable) "Hide Raw Data Table" else "Inspect Raw Table (${d.rows.size} rows)")
                }
            }

            if (showRawTable) {
                item {
                    RawDataTableView(headers = d.headers, rows = d.rows.take(15))
                }
            }

            item {
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

@Composable
private fun ColumnDetailCard(col: DataColumnInfo) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(10.dp),
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
                    text = col.name,
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )
                ColumnTypeBadge(col.type)
            }

            Spacer(modifier = Modifier.height(6.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "Nulls: ${col.nullCount}",
                    style = MaterialTheme.typography.bodySmall,
                    color = if (col.nullCount > 0) AmberWarning else MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "Unique: ${col.uniqueCount}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                if (col.type == ColumnType.NUMERICAL && col.mean != null) {
                    Text(
                        text = "Avg: ${String.format(java.util.Locale.US, "%.1f", col.mean)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            if (col.sampleValues.isNotEmpty()) {
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Sample: ${col.sampleValues.joinToString(", ")}",
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1
                )
            }
        }
    }
}

@Composable
fun ColumnTypeBadge(type: ColumnType) {
    val (bg, fg) = when (type) {
        ColumnType.NUMERICAL -> Pair(CyanLight, CyanPrimaryDark)
        ColumnType.CATEGORICAL -> Pair(Slate100, Slate800)
        ColumnType.DATE -> Pair(EmeraldLight, EmeraldGreen)
        ColumnType.BOOLEAN -> Pair(AmberLight, AmberWarning)
        ColumnType.TEXT -> Pair(BlueLight, BlueInfo)
    }

    Surface(
        shape = RoundedCornerShape(6.dp),
        color = bg
    ) {
        Text(
            text = type.name,
            style = MaterialTheme.typography.labelSmall.copy(
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold
            ),
            color = fg,
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
        )
    }
}

@Composable
private fun RawDataTableView(
    headers: List<String>,
    rows: List<List<String>>
) {
    val horizontalScroll = rememberScrollState()

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(horizontalScroll)
                .padding(12.dp)
        ) {
            // Header Row
            Row(
                modifier = Modifier
                    .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(4.dp))
                    .padding(vertical = 8.dp)
            ) {
                headers.forEach { header ->
                    Text(
                        text = header,
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier
                            .width(130.dp)
                            .padding(horizontal = 8.dp)
                    )
                }
            }

            // Data Rows
            rows.forEachIndexed { idx, row ->
                Row(
                    modifier = Modifier
                        .background(if (idx % 2 == 0) Color.Transparent else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
                        .padding(vertical = 6.dp)
                ) {
                    headers.indices.forEach { colIdx ->
                        val cell = row.getOrNull(colIdx) ?: ""
                        Text(
                            text = if (cell.isEmpty()) "—" else cell,
                            style = MaterialTheme.typography.bodySmall,
                            color = if (cell.isEmpty()) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier
                                .width(130.dp)
                                .padding(horizontal = 8.dp),
                            maxLines = 1
                        )
                    }
                }
            }
        }
    }
}
