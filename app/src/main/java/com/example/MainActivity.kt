package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.datalens.ui.screens.*
import com.example.datalens.viewmodel.AppScreen
import com.example.datalens.viewmodel.DataLensViewModel
import com.example.ui.theme.CyanPrimary
import com.example.ui.theme.DataLensAITheme

class MainActivity : ComponentActivity() {

    private val viewModel: DataLensViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            val isDarkTheme by viewModel.isDarkTheme.collectAsState()
            DataLensAITheme(darkTheme = isDarkTheme) {
                MainAppContent(viewModel = viewModel)
            }
        }
    }
}

@Composable
fun MainAppContent(viewModel: DataLensViewModel) {
    val currentScreen by viewModel.currentScreen.collectAsState()

    BackHandler(enabled = currentScreen != AppScreen.HOME) {
        viewModel.handleBack()
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = MaterialTheme.colorScheme.onSurface
            ) {
                NavigationBarItem(
                    selected = currentScreen == AppScreen.HOME,
                    onClick = { viewModel.navigateTo(AppScreen.HOME) },
                    icon = {
                        Icon(
                            imageVector = if (currentScreen == AppScreen.HOME) Icons.Filled.Home else Icons.Outlined.Home,
                            contentDescription = "Home",
                            modifier = Modifier.size(22.dp)
                        )
                    },
                    label = {
                        Text(
                            text = "Home",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = if (currentScreen == AppScreen.HOME) FontWeight.Bold else FontWeight.Normal,
                                fontSize = 11.sp
                            )
                        )
                    },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = CyanPrimary,
                        selectedTextColor = CyanPrimary,
                        indicatorColor = CyanPrimary.copy(alpha = 0.12f)
                    ),
                    modifier = Modifier.testTag("nav_home")
                )

                NavigationBarItem(
                    selected = currentScreen == AppScreen.OVERVIEW || currentScreen == AppScreen.UPLOAD,
                    onClick = { viewModel.navigateTo(AppScreen.OVERVIEW) },
                    icon = {
                        Icon(
                            imageVector = if (currentScreen == AppScreen.OVERVIEW) Icons.Filled.TableChart else Icons.Outlined.TableChart,
                            contentDescription = "Profile",
                            modifier = Modifier.size(22.dp)
                        )
                    },
                    label = {
                        Text(
                            text = "Profile",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = if (currentScreen == AppScreen.OVERVIEW) FontWeight.Bold else FontWeight.Normal,
                                fontSize = 11.sp
                            )
                        )
                    },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = CyanPrimary,
                        selectedTextColor = CyanPrimary,
                        indicatorColor = CyanPrimary.copy(alpha = 0.12f)
                    ),
                    modifier = Modifier.testTag("nav_profile")
                )

                NavigationBarItem(
                    selected = currentScreen == AppScreen.INSIGHTS,
                    onClick = { viewModel.navigateTo(AppScreen.INSIGHTS) },
                    icon = {
                        Icon(
                            imageVector = if (currentScreen == AppScreen.INSIGHTS) Icons.Filled.AutoAwesome else Icons.Outlined.AutoAwesome,
                            contentDescription = "Insights",
                            modifier = Modifier.size(22.dp)
                        )
                    },
                    label = {
                        Text(
                            text = "Insights",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = if (currentScreen == AppScreen.INSIGHTS) FontWeight.Bold else FontWeight.Normal,
                                fontSize = 11.sp
                            )
                        )
                    },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = CyanPrimary,
                        selectedTextColor = CyanPrimary,
                        indicatorColor = CyanPrimary.copy(alpha = 0.12f)
                    ),
                    modifier = Modifier.testTag("nav_insights")
                )

                NavigationBarItem(
                    selected = currentScreen == AppScreen.ASK_DATA,
                    onClick = { viewModel.navigateTo(AppScreen.ASK_DATA) },
                    icon = {
                        Icon(
                            imageVector = if (currentScreen == AppScreen.ASK_DATA) Icons.Filled.ChatBubble else Icons.Outlined.ChatBubbleOutline,
                            contentDescription = "Ask Data",
                            modifier = Modifier.size(22.dp)
                        )
                    },
                    label = {
                        Text(
                            text = "Ask Data",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = if (currentScreen == AppScreen.ASK_DATA) FontWeight.Bold else FontWeight.Normal,
                                fontSize = 11.sp
                            )
                        )
                    },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = CyanPrimary,
                        selectedTextColor = CyanPrimary,
                        indicatorColor = CyanPrimary.copy(alpha = 0.12f)
                    ),
                    modifier = Modifier.testTag("nav_ask_data")
                )

                NavigationBarItem(
                    selected = currentScreen == AppScreen.REPORT,
                    onClick = { viewModel.navigateTo(AppScreen.REPORT) },
                    icon = {
                        Icon(
                            imageVector = if (currentScreen == AppScreen.REPORT) Icons.Filled.Assessment else Icons.Outlined.Assessment,
                            contentDescription = "Report",
                            modifier = Modifier.size(22.dp)
                        )
                    },
                    label = {
                        Text(
                            text = "Report",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = if (currentScreen == AppScreen.REPORT) FontWeight.Bold else FontWeight.Normal,
                                fontSize = 11.sp
                            )
                        )
                    },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = CyanPrimary,
                        selectedTextColor = CyanPrimary,
                        indicatorColor = CyanPrimary.copy(alpha = 0.12f)
                    ),
                    modifier = Modifier.testTag("nav_report")
                )
            }
        }
    ) { innerPadding ->
        AnimatedContent(
            targetState = currentScreen,
            transitionSpec = { fadeIn() togetherWith fadeOut() },
            modifier = Modifier.padding(innerPadding),
            label = "ScreenTransition"
        ) { screen ->
            when (screen) {
                AppScreen.HOME -> HomeScreen(viewModel = viewModel)
                AppScreen.UPLOAD -> UploadScreen(viewModel = viewModel)
                AppScreen.OVERVIEW -> OverviewScreen(viewModel = viewModel)
                AppScreen.QUALITY -> DataQualityScreen(viewModel = viewModel)
                AppScreen.INSIGHTS -> InsightsScreen(viewModel = viewModel)
                AppScreen.ASK_DATA -> AskDataScreen(viewModel = viewModel)
                AppScreen.VISUALIZATION -> VisualizationScreen(viewModel = viewModel)
                AppScreen.ADVANCED_ANALYTICS -> AdvancedAnalyticsScreen(viewModel = viewModel)
                AppScreen.REPORT -> ReportScreen(viewModel = viewModel)
                AppScreen.SETTINGS -> SettingsScreen(viewModel = viewModel)
            }
        }
    }
}
