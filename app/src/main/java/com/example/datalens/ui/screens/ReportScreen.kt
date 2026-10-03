package com.example.datalens.ui.screens

import android.content.Context
import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Feed
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.datalens.model.BusinessReport
import com.example.datalens.ui.components.MetricCard
import com.example.datalens.ui.components.SectionHeader
import com.example.datalens.viewmodel.DataLensViewModel
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportScreen(
    viewModel: DataLensViewModel,
    modifier: Modifier = Modifier
) {
    val report by viewModel.businessReport.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val context = LocalContext.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Executive Business Report",
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
                actions = {
                    if (report != null) {
                        IconButton(
                            onClick = { shareReport(context, report!!) },
                            modifier = Modifier.testTag("share_report_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Share,
                                contentDescription = "Share",
                                tint = CyanPrimary
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        }
    ) { innerPadding ->
        if (report == null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                Text("No report generated yet.")
            }
            return@Scaffold
        }

        val r = report!!

        LazyColumn(
            modifier = modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Action banner: AI Enhancement button
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Business Intelligence Briefing",
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Dataset: ${r.datasetName}",
                                style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Button(
                            onClick = { viewModel.generateExecutiveReport() },
                            enabled = !isLoading,
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = CyanPrimary)
                        ) {
                            if (isLoading) {
                                CircularProgressIndicator(modifier = Modifier.size(16.dp), color = Slate950, strokeWidth = 2.dp)
                            } else {
                                Icon(Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(14.dp), tint = Slate950)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("AI Polish", style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold), color = Slate950)
                            }
                        }
                    }
                }
            }

            // 1. Executive Summary
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.AutoMirrored.Filled.Feed, contentDescription = null, tint = CyanPrimary, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Executive Summary",
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = r.executiveSummary,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface,
                            lineHeight = 22.sp
                        )
                    }
                }
            }

            // 2. Key KPIs
            item {
                SectionHeader(title = "Key Performance Indicators")
            }

            item {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    val chunks = r.keyKpis.chunked(2)
                    chunks.forEach { pairList ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            pairList.forEach { (label, value) ->
                                MetricCard(
                                    title = label,
                                    value = value,
                                    modifier = Modifier.weight(1f)
                                )
                            }
                            if (pairList.size == 1) {
                                Spacer(modifier = Modifier.weight(1f))
                            }
                        }
                    }
                }
            }

            // 3. Major Trends
            item {
                SectionHeader(title = "Major Trends")
            }

            items(r.majorTrends) { trend ->
                ReportBulletCard(
                    icon = Icons.AutoMirrored.Filled.TrendingUp,
                    tint = CyanPrimary,
                    text = trend
                )
            }

            // 4. Important Anomalies
            if (r.importantAnomalies.isNotEmpty()) {
                item {
                    SectionHeader(title = "Important Anomalies")
                }
                items(r.importantAnomalies) { anomaly ->
                    ReportBulletCard(
                        icon = Icons.Default.WarningAmber,
                        tint = AmberWarning,
                        text = anomaly
                    )
                }
            }

            // 5. Top & Poor Performers
            item {
                SectionHeader(title = "Product & Category Performers")
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Card(
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Text(
                                text = "Top Performers",
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                color = EmeraldGreen
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            r.topPerformers.forEach { item ->
                                Text(
                                    text = "• $item",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    modifier = Modifier.padding(vertical = 2.dp)
                                )
                            }
                        }
                    }

                    Card(
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Text(
                                text = "Areas to Improve",
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                color = AmberWarning
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            r.poorPerformers.forEach { item ->
                                Text(
                                    text = "• $item",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    modifier = Modifier.padding(vertical = 2.dp)
                                )
                            }
                        }
                    }
                }
            }

            // 6. Strategic Recommendations
            item {
                SectionHeader(title = "Strategic Recommendations")
            }

            items(r.recommendations) { rec ->
                ReportBulletCard(
                    icon = Icons.Default.CheckCircleOutline,
                    tint = IndigoAccent,
                    text = rec
                )
            }

            // 7. 30-Day and 90-Day Execution Roadmaps
            if (r.roadmap30Days.isNotEmpty() || r.roadmap90Days.isNotEmpty()) {
                item {
                    SectionHeader(title = "Phased Execution Roadmap")
                }

                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = "First 30 Days (Immediate Impact)",
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                color = CyanPrimary
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            r.roadmap30Days.forEach { action ->
                                Text(
                                    text = "• $action",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    modifier = Modifier.padding(vertical = 2.dp)
                                )
                            }

                            HorizontalDivider(modifier = Modifier.padding(vertical = 10.dp))

                            Text(
                                text = "60 to 90 Days (Scale & Optimization)",
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                color = IndigoAccent
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            r.roadmap90Days.forEach { action ->
                                Text(
                                    text = "• $action",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    modifier = Modifier.padding(vertical = 2.dp)
                                )
                            }
                        }
                    }
                }
            }

            // Export Actions Row
            item {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Button(
                        onClick = { com.example.datalens.export.PdfReportGenerator.generateAndSharePdf(context, r) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("export_pdf_button"),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = CyanPrimary)
                    ) {
                        Icon(
                            imageVector = Icons.Default.PictureAsPdf,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp),
                            tint = Slate950
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Export Executive PDF Report",
                            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                            color = Slate950
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedButton(
                            onClick = { shareReport(context, r) },
                            modifier = Modifier
                                .weight(1f)
                                .height(44.dp)
                                .testTag("export_report_button"),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Share,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Share Text",
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold)
                            )
                        }

                        OutlinedButton(
                            onClick = {
                                val csvContent = viewModel.getExportableCsv()
                                val sendIntent = Intent().apply {
                                    action = Intent.ACTION_SEND
                                    putExtra(Intent.EXTRA_TEXT, csvContent)
                                    putExtra(Intent.EXTRA_TITLE, "Cleaned_Dataset_${r.datasetName}")
                                    type = "text/csv"
                                }
                                context.startActivity(Intent.createChooser(sendIntent, "Export Cleaned CSV"))
                            },
                            modifier = Modifier
                                .weight(1f)
                                .height(44.dp),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(Icons.Default.FileDownload, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Export CSV",
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold)
                            )
                        }
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
private fun ReportBulletCard(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    tint: Color,
    text: String
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.Top
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = tint,
                modifier = Modifier
                    .size(18.dp)
                    .padding(top = 2.dp)
            )
            Spacer(modifier = Modifier.width(10.dp))
            Text(
                text = text,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

private fun shareReport(context: Context, report: BusinessReport) {
    val content = buildString {
        append("DATA LENS AI - EXECUTIVE BUSINESS REPORT\n")
        append("Dataset: ${report.datasetName}\n\n")
        append("--- EXECUTIVE SUMMARY ---\n")
        append("${report.executiveSummary}\n\n")
        append("--- KEY KPIS ---\n")
        report.keyKpis.forEach { append("${it.first}: ${it.second}\n") }
        append("\n--- MAJOR TRENDS ---\n")
        report.majorTrends.forEach { append("• $it\n") }
        append("\n--- STRATEGIC RECOMMENDATIONS ---\n")
        report.recommendations.forEach { append("1. $it\n") }
        if (report.roadmap30Days.isNotEmpty()) {
            append("\n--- 30-DAY ROADMAP ---\n")
            report.roadmap30Days.forEach { append("• $it\n") }
        }
        if (report.roadmap90Days.isNotEmpty()) {
            append("\n--- 90-DAY ROADMAP ---\n")
            report.roadmap90Days.forEach { append("• $it\n") }
        }
        append("\nGenerated by Data Lens AI (Your AI Data Analyst)")
    }

    val sendIntent = Intent().apply {
        action = Intent.ACTION_SEND
        putExtra(Intent.EXTRA_TEXT, content)
        putExtra(Intent.EXTRA_TITLE, "Executive Report - ${report.datasetName}")
        type = "text/plain"
    }
    val shareIntent = Intent.createChooser(sendIntent, "Share Executive Report")
    context.startActivity(shareIntent)
}
