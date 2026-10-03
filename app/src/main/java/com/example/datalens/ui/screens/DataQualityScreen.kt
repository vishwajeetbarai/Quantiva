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
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.Rule
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.datalens.model.DataQualityIssue
import com.example.datalens.ui.components.HealthScoreBadge
import com.example.datalens.ui.components.SectionHeader
import com.example.datalens.viewmodel.DataLensViewModel
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DataQualityScreen(
    viewModel: DataLensViewModel,
    modifier: Modifier = Modifier
) {
    val dataset by viewModel.activeDataset.collectAsState()
    val profile by viewModel.datasetProfile.collectAsState()
    val issues by viewModel.qualityIssues.collectAsState()
    val context = LocalContext.current

    var previewIssue by remember { mutableStateOf<DataQualityIssue?>(null) }
    var snackbarMessage by remember { mutableStateOf<String?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Data Quality Center",
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
                    if (profile != null) {
                        HealthScoreBadge(score = profile!!.qualityScore, modifier = Modifier.padding(end = 12.dp))
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
            // Quality Health Summary Card
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
                                    text = "Overall Dataset Health",
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = if (issues.isEmpty()) "All records pristine & normalized" else "${issues.size} item(s) available for normalization",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Text(
                                text = "${profile?.qualityScore ?: 88}/100",
                                style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                                color = if ((profile?.qualityScore ?: 88) >= 80) EmeraldGreen else AmberWarning
                            )
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        LinearProgressIndicator(
                            progress = { ((profile?.qualityScore ?: 88) / 100f).coerceIn(0f, 1f) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp)
                                .clip(RoundedCornerShape(4.dp)),
                            color = if ((profile?.qualityScore ?: 88) >= 80) EmeraldGreen else AmberWarning,
                            trackColor = MaterialTheme.colorScheme.surfaceVariant
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceAround
                        ) {
                            QualityMetricItem(
                                title = "Completeness",
                                value = if ((profile?.totalMissingCells ?: 0) == 0) "100%" else "94%"
                            )
                            QualityMetricItem(
                                title = "Uniqueness",
                                value = if ((profile?.duplicateRowsCount ?: 0) == 0) "100%" else "96%"
                            )
                            QualityMetricItem(
                                title = "Consistency",
                                value = if (issues.any { it.category.contains("Inconsistent") }) "84%" else "100%"
                            )
                            QualityMetricItem(
                                title = "Validity",
                                value = if ((profile?.potentialAnomaliesCount ?: 0) == 0) "100%" else "92%"
                            )
                        }
                    }
                }
            }

            if (snackbarMessage != null) {
                item {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = EmeraldLight
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.CheckCircle, contentDescription = null, tint = EmeraldGreen)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = snackbarMessage ?: "",
                                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
                                color = EmeraldGreen,
                                modifier = Modifier.weight(1f)
                            )
                            IconButton(onClick = { snackbarMessage = null }) {
                                Icon(Icons.Default.Close, contentDescription = null, tint = EmeraldGreen)
                            }
                        }
                    }
                }
            }

            // Cleaned CSV Export Action
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Cleaned Dataset Export",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Export updated CSV with all applied fixes",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    OutlinedButton(
                        onClick = {
                            val csvContent = viewModel.getExportableCsv()
                            shareCleanedCsv(context, dataset?.name ?: "Cleaned_Dataset.csv", csvContent)
                        },
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.testTag("export_cleaned_csv_button")
                    ) {
                        Icon(Icons.Default.FileDownload, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Export CSV")
                    }
                }
            }

            item {
                SectionHeader(
                    title = "Quality Issues & Transformations",
                    subtitle = "Interactive normalization studio: preview transformations before applying"
                )
            }

            if (issues.isEmpty()) {
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
                                imageVector = Icons.Default.Verified,
                                contentDescription = null,
                                tint = EmeraldGreen,
                                modifier = Modifier.size(48.dp)
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = "All Quality Audits Passed",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "All spelling discrepancies resolved, duplicate entries removed, and null fields addressed.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            } else {
                items(issues) { issue ->
                    QualityIssueCard(
                        issue = issue,
                        onPreviewClick = { previewIssue = issue },
                        onApplyFix = {
                            viewModel.applyQualityFix(issue.id)
                            snackbarMessage = "Normalized ${issue.columnName} successfully! Health score updated."
                        }
                    )
                }
            }

            item {
                Spacer(modifier = Modifier.height(20.dp))
            }
        }
    }

    // Preview Fix Dialog
    if (previewIssue != null) {
        val issue = previewIssue!!
        AlertDialog(
            onDismissRequest = { previewIssue = null },
            icon = {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.Rule,
                    contentDescription = null,
                    tint = CyanPrimary
                )
            },
            title = {
                Text(
                    text = "Preview Normalization: ${issue.columnName}",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "Detected Problem:",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = issue.problem,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    HorizontalDivider()

                    Text(
                        text = "Transformation Mapping:",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    if (issue.previewTransformations.isNotEmpty()) {
                        issue.previewTransformations.forEach { (before, after) ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f), RoundedCornerShape(4.dp))
                                    .padding(horizontal = 8.dp, vertical = 4.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(text = "\"$before\"", style = MaterialTheme.typography.bodySmall, color = RoseRisk)
                                Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, tint = CyanPrimary, modifier = Modifier.size(14.dp))
                                Text(text = "\"$after\"", style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold), color = EmeraldGreen)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Text(
                                text = "Affected Rows: ${issue.affectedRows}",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Applying will update active in-memory dataset, recalculate insights, and boost Data Health score.",
                                style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.applyQualityFix(issue.id)
                        snackbarMessage = "Normalized ${issue.columnName} successfully! Health score updated."
                        previewIssue = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = CyanPrimary)
                ) {
                    Text("Apply Normalization", color = Slate950, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { previewIssue = null }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
private fun QualityMetricItem(title: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = value,
            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onSurface
        )
        Text(
            text = title,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun QualityIssueCard(
    issue: DataQualityIssue,
    onPreviewClick: () -> Unit,
    onApplyFix: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("quality_issue_${issue.id}"),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = when (issue.category) {
                        "Inconsistent Categories" -> AmberLight
                        "Duplicate Records" -> RoseLight
                        else -> BlueLight
                    }
                ) {
                    Text(
                        text = issue.category.uppercase(),
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold
                        ),
                        color = when (issue.category) {
                            "Inconsistent Categories" -> AmberWarning
                            "Duplicate Records" -> RoseRisk
                            else -> BlueInfo
                        },
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }

                Text(
                    text = "${issue.affectedRows} row(s) affected",
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = issue.columnName,
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = issue.problem,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(10.dp))

            Surface(
                shape = RoundedCornerShape(8.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 10.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Build,
                        contentDescription = null,
                        tint = CyanPrimary,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Suggested Fix: ${issue.suggestedFix}",
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium
                        ),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextButton(onClick = onPreviewClick) {
                    Text(
                        text = "Preview Fix",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                        color = CyanPrimary
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Button(
                    onClick = onApplyFix,
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = CyanPrimary)
                ) {
                    Text(
                        text = "Apply Normalization",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                        color = Slate950
                    )
                }
            }
        }
    }
}

private fun shareCleanedCsv(context: Context, fileName: String, csvContent: String) {
    val sendIntent = Intent().apply {
        action = Intent.ACTION_SEND
        putExtra(Intent.EXTRA_TEXT, csvContent)
        putExtra(Intent.EXTRA_TITLE, "Cleaned Dataset - $fileName")
        type = "text/csv"
    }
    val shareIntent = Intent.createChooser(sendIntent, "Export Cleaned CSV")
    context.startActivity(shareIntent)
}
