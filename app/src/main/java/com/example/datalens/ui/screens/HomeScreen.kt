package com.example.datalens.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.TrendingDown
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.datalens.ui.components.HealthScoreBadge
import com.example.datalens.ui.components.MetricCard
import com.example.datalens.ui.components.SectionHeader
import com.example.datalens.viewmodel.AppScreen
import com.example.datalens.viewmodel.DataLensViewModel
import com.example.ui.theme.*

@Composable
fun HomeScreen(
    viewModel: DataLensViewModel,
    modifier: Modifier = Modifier
) {
    val activeDataset by viewModel.activeDataset.collectAsState()
    val datasetProfile by viewModel.datasetProfile.collectAsState()
    val recentAnalyses by viewModel.analysisHistory.collectAsState()
    val insights by viewModel.insights.collectAsState()
    val isDarkTheme by viewModel.isDarkTheme.collectAsState()

    val topInsight = insights.firstOrNull()

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // App Header
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(Slate900),
                        contentAlignment = Alignment.Center
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.ic_data_lens_logo),
                            contentDescription = "Quantiva Logo",
                            modifier = Modifier.size(32.dp),
                            contentScale = ContentScale.Crop
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "QUANTIVA",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 0.5.sp
                            ),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Your AI Data Analyst",
                            style = MaterialTheme.typography.labelSmall,
                            color = CyanPrimary
                        )
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        modifier = Modifier
                            .clickable { viewModel.navigateTo(AppScreen.SETTINGS) }
                            .testTag("user_profile_chip"),
                        shape = RoundedCornerShape(16.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(22.dp)
                                    .clip(CircleShape)
                                    .background(CyanPrimary),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "VB",
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, fontSize = 9.sp),
                                    color = Slate950
                                )
                            }
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Executive",
                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp, fontWeight = FontWeight.SemiBold),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }

                    IconButton(
                        onClick = { viewModel.toggleDarkTheme() },
                        modifier = Modifier.testTag("theme_toggle_button")
                    ) {
                        Icon(
                            imageVector = if (isDarkTheme) Icons.Default.LightMode else Icons.Default.DarkMode,
                            contentDescription = if (isDarkTheme) "Switch to Light Mode" else "Switch to Dark Mode",
                            tint = if (isDarkTheme) AmberWarning else AmethystAccent
                        )
                    }

                    IconButton(
                        onClick = { viewModel.navigateTo(AppScreen.SETTINGS) },
                        modifier = Modifier.testTag("settings_button")
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Settings,
                            contentDescription = "Settings",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        // Hero Banner Card
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp)),
                colors = CardDefaults.cardColors(containerColor = Slate900)
            ) {
                Box(modifier = Modifier.fillMaxWidth()) {
                    Image(
                        painter = painterResource(id = R.drawable.hero_data_lens),
                        contentDescription = "Analytics Visual",
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(130.dp),
                        contentScale = ContentScale.Crop
                    )
                    Box(
                        modifier = Modifier
                            .matchParentSize()
                            .background(
                                Brush.verticalGradient(
                                    colors = listOf(Color.Transparent, Slate900.copy(alpha = 0.95f)),
                                    startY = 20f
                                )
                            )
                    )
                    Column(
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .padding(14.dp)
                    ) {
                        Surface(
                            color = CyanPrimary.copy(alpha = 0.2f),
                            shape = RoundedCornerShape(4.dp)
                        ) {
                            Text(
                                text = "AUTOMATED INTELLIGENCE",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold
                                ),
                                color = CyanPrimary,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Turn raw spreadsheets into decisions.",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
                            color = Color.White
                        )
                    }
                }
            }
        }

        // Primary & Secondary Actions
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Button(
                    onClick = { viewModel.navigateTo(AppScreen.UPLOAD) },
                    modifier = Modifier
                        .weight(1.3f)
                        .height(48.dp)
                        .testTag("analyze_data_button"),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = CyanPrimary)
                ) {
                    Icon(
                        imageVector = Icons.Default.CloudUpload,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp),
                        tint = Slate950
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Analyze Your Data",
                        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                        color = Slate950
                    )
                }

                OutlinedButton(
                    onClick = { viewModel.loadSampleDataset() },
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .testTag("sample_dataset_button"),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = MaterialTheme.colorScheme.onSurface
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.Dataset,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = CyanPrimary
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Sample Data",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold)
                    )
                }
            }
        }

        // Active / Recent Analysis Section
        item {
            SectionHeader(
                title = "Recent Analysis",
                subtitle = "Active workspace inspection"
            )
        }

        item {
            val fileTitle = activeDataset?.name ?: recentAnalyses.firstOrNull()?.fileName ?: "Sales_Q3_Retail.csv"
            val rows = activeDataset?.rowCount ?: recentAnalyses.firstOrNull()?.rowCount ?: 34
            val qualityScore = datasetProfile?.qualityScore ?: recentAnalyses.firstOrNull()?.qualityScore ?: 88

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("recent_analysis_card"),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                border = androidx.compose.foundation.BorderStroke(
                    1.dp,
                    MaterialTheme.colorScheme.outlineVariant
                )
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .background(Slate100, RoundedCornerShape(8.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Description,
                                    contentDescription = null,
                                    tint = Slate700,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = fileTitle,
                                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "Last analyzed: Today • $rows records",
                                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        HealthScoreBadge(score = qualityScore)
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // KPIs row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        MetricCard(
                            title = "Revenue",
                            value = "₹63.0L",
                            delta = "+14.2%",
                            isPositiveDelta = true,
                            modifier = Modifier.weight(1f)
                        )
                        MetricCard(
                            title = "Gross Margin",
                            value = "34.8%",
                            delta = "+2.4%",
                            isPositiveDelta = true,
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Important Insight Callout
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            MaterialTheme.colorScheme.outlineVariant
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.TrendingDown,
                                contentDescription = null,
                                tint = RoseRisk,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Important Insight",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold
                                    ),
                                    color = RoseRisk
                                )
                                Text(
                                    text = topInsight?.title ?: "Sales dropped 18% in August due to hardware supply delays.",
                                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
                                    color = MaterialTheme.colorScheme.onSurface,
                                    maxLines = 2
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Button(
                        onClick = { viewModel.navigateTo(AppScreen.OVERVIEW) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(44.dp)
                            .testTag("view_analysis_button"),
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant,
                            contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    ) {
                        Text(
                            text = "View Analysis",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                            color = CyanPrimary
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = null,
                            tint = CyanPrimary,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }

        // Feature Exploration Grid
        item {
            SectionHeader(title = "Analyst Toolkit")
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                QuickModuleCard(
                    title = "Ask Your Data",
                    subtitle = "Natural language queries",
                    icon = Icons.Default.ChatBubbleOutline,
                    tint = CyanPrimary,
                    modifier = Modifier.weight(1f),
                    onClick = { viewModel.navigateTo(AppScreen.ASK_DATA) }
                )
                QuickModuleCard(
                    title = "AI Insights",
                    subtitle = "Automated trend discovery",
                    icon = Icons.Default.AutoAwesome,
                    tint = IndigoAccent,
                    modifier = Modifier.weight(1f),
                    onClick = { viewModel.navigateTo(AppScreen.INSIGHTS) }
                )
            }
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                QuickModuleCard(
                    title = "Data Quality",
                    subtitle = "Spelling, nulls & outliers",
                    icon = Icons.Default.HealthAndSafety,
                    tint = EmeraldGreen,
                    modifier = Modifier.weight(1f),
                    onClick = { viewModel.navigateTo(AppScreen.QUALITY) }
                )
                QuickModuleCard(
                    title = "Reports",
                    subtitle = "Executive briefing & export",
                    icon = Icons.Default.Assessment,
                    tint = VioletAccent,
                    modifier = Modifier.weight(1f),
                    onClick = { viewModel.navigateTo(AppScreen.REPORT) }
                )
            }
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                QuickModuleCard(
                    title = "Predictive & ROI",
                    subtitle = "Q4 forecasting & sensitivity",
                    icon = Icons.AutoMirrored.Filled.TrendingUp,
                    tint = CyanPrimary,
                    modifier = Modifier.weight(1f),
                    onClick = { viewModel.navigateTo(AppScreen.ADVANCED_ANALYTICS) }
                )
                QuickModuleCard(
                    title = "Visual Studio",
                    subtitle = "Dynamic plots & rankings",
                    icon = Icons.Default.PieChart,
                    tint = TealAccent,
                    modifier = Modifier.weight(1f),
                    onClick = { viewModel.navigateTo(AppScreen.VISUALIZATION) }
                )
            }
        }
    }
}

@Composable
private fun QuickModuleCard(
    title: String,
    subtitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    tint: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier
            .clickable { onClick() }
            .testTag("quick_card_${title.lowercase().replace(" ", "_")}"),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .background(tint.copy(alpha = 0.12f), RoundedCornerShape(8.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = tint,
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
