package com.example.datalens.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.datalens.model.AskDataMessage
import com.example.datalens.model.ChartType
import com.example.datalens.ui.components.DataLensBarChart
import com.example.datalens.ui.components.DataLensDonutChart
import com.example.datalens.ui.components.DataLensLineChart
import com.example.datalens.viewmodel.DataLensViewModel
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AskDataScreen(
    viewModel: DataLensViewModel,
    modifier: Modifier = Modifier
) {
    val messages by viewModel.askDataMessages.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val loadingStatus by viewModel.loadingStatus.collectAsState()
    val dataset by viewModel.activeDataset.collectAsState()

    var inputText by remember { mutableStateOf("") }
    val listState = rememberLazyListState()

    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    val suggestedQuestions = listOf(
        "What are the top 5 products?",
        "Which customers generate the most revenue?",
        "Which month had the highest revenue?",
        "Why did sales decline?",
        "Find unusual transactions",
        "Show me the worst performing category",
        "What is our gross profit margin?",
        "What should I investigate first?"
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Ask Your Data",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )
                        Text(
                            text = "Dataset: ${dataset?.name ?: "Sales_Q3_Retail.csv"}",
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
        },
        bottomBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surface)
                    .padding(horizontal = 12.dp, vertical = 8.dp)
            ) {
                // Quick Question Suggestions
                val scrollState = rememberScrollState()
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(scrollState)
                        .padding(bottom = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    suggestedQuestions.forEach { question ->
                        SuggestionChip(
                            onClick = {
                                viewModel.askQuestion(question)
                            },
                            label = {
                                Text(
                                    text = question,
                                    style = MaterialTheme.typography.labelSmall
                                )
                            },
                            shape = RoundedCornerShape(16.dp)
                        )
                    }
                }

                // Input row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = inputText,
                        onValueChange = { inputText = it },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("ask_data_input"),
                        placeholder = {
                            Text(
                                "Ask a question about your data...",
                                style = MaterialTheme.typography.bodyMedium
                            )
                        },
                        maxLines = 3,
                        shape = RoundedCornerShape(24.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = CyanPrimary,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
                        ),
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                        keyboardActions = KeyboardActions(
                            onSend = {
                                if (inputText.isNotBlank() && !isLoading) {
                                    viewModel.askQuestion(inputText)
                                    inputText = ""
                                }
                            }
                        )
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    IconButton(
                        onClick = {
                            if (inputText.isNotBlank() && !isLoading) {
                                viewModel.askQuestion(inputText)
                                inputText = ""
                            }
                        },
                        enabled = inputText.isNotBlank() && !isLoading,
                        modifier = Modifier
                            .size(48.dp)
                            .background(if (inputText.isNotBlank() && !isLoading) CyanPrimary else MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(24.dp))
                            .testTag("ask_data_send_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Send,
                            contentDescription = "Send",
                            tint = if (inputText.isNotBlank() && !isLoading) Slate950 else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        LazyColumn(
            state = listState,
            modifier = modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                Spacer(modifier = Modifier.height(4.dp))
            }

            items(messages) { msg ->
                if (msg.isUser) {
                    UserMessageBubble(text = msg.text)
                } else {
                    AnalystResponseCard(message = msg)
                }
            }

            if (isLoading) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                color = CyanPrimary,
                                strokeWidth = 2.dp
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = loadingStatus,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

@Composable
private fun UserMessageBubble(text: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.End
    ) {
        Surface(
            shape = RoundedCornerShape(16.dp, 16.dp, 4.dp, 16.dp),
            color = CyanPrimary
        ) {
            Text(
                text = text,
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                color = Slate950,
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)
            )
        }
    }
}

@Composable
private fun AnalystResponseCard(message: AskDataMessage) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("analyst_response_${message.id}"),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(22.dp)
                            .background(CyanPrimary.copy(alpha = 0.15f), RoundedCornerShape(6.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Analytics,
                            contentDescription = null,
                            tint = CyanPrimary,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "DATA LENS ANALYST",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        ),
                        color = CyanPrimary
                    )
                }

                if (message.keyMetric != null) {
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant
                    ) {
                        Text(
                            text = message.keyMetric,
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            ),
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Direct answer
            Text(
                text = message.directAnswer ?: message.text,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface
            )

            // Table Snippet if present
            if (message.tableHeaders != null && message.tableSnippet != null) {
                Spacer(modifier = Modifier.height(12.dp))
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 6.dp)
                        ) {
                            message.tableHeaders.forEach { header ->
                                Text(
                                    text = header,
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.onSurface,
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                        message.tableSnippet.forEach { row ->
                            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp)
                            ) {
                                row.forEach { cell ->
                                    Text(
                                        text = cell,
                                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.weight(1f)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Mini Chart if present
            if (message.chartData.isNotEmpty()) {
                Spacer(modifier = Modifier.height(14.dp))
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                ) {
                    Box(modifier = Modifier.padding(12.dp)) {
                        when (message.chartType) {
                            ChartType.LINE -> DataLensLineChart(points = message.chartData)
                            ChartType.DONUT -> DataLensDonutChart(points = message.chartData)
                            else -> DataLensBarChart(points = message.chartData)
                        }
                    }
                }
            }

            // Calculation Evidence
            if (message.calculationEvidence != null) {
                Spacer(modifier = Modifier.height(12.dp))
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
                                modifier = Modifier.size(13.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Supporting Calculation & Proof",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold
                                ),
                                color = IndigoAccent
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = message.calculationEvidence,
                            style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "✓ ${message.verificationBadge}",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontSize = 9.sp,
                                fontWeight = FontWeight.SemiBold
                            ),
                            color = EmeraldGreen
                        )
                    }
                }
            }
        }
    }
}
