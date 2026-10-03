package com.example.datalens.viewmodel

import android.app.Application
import android.net.Uri
import android.provider.OpenableColumns
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.datalens.ai.GeminiAnalyticsService
import com.example.datalens.data.AnalysisEntity
import com.example.datalens.data.AnalysisRepository
import com.example.datalens.data.DataLensDatabase
import com.example.datalens.engine.AnalyticsEngine
import com.example.datalens.engine.CsvParser
import com.example.datalens.engine.SampleDataGenerator
import com.example.datalens.engine.XlsxParser
import com.example.datalens.model.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.InputStream

enum class AppScreen {
    HOME,
    UPLOAD,
    OVERVIEW,
    QUALITY,
    INSIGHTS,
    ASK_DATA,
    VISUALIZATION,
    ADVANCED_ANALYTICS,
    REPORT,
    SETTINGS
}

class DataLensViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: AnalysisRepository
    private val geminiService = GeminiAnalyticsService()

    private val _userProfile = MutableStateFlow(UserProfile())
    val userProfile: StateFlow<UserProfile> = _userProfile.asStateFlow()

    private val _isDarkTheme = MutableStateFlow(true)
    val isDarkTheme: StateFlow<Boolean> = _isDarkTheme.asStateFlow()

    fun toggleDarkTheme() {
        _isDarkTheme.value = !_isDarkTheme.value
    }

    fun setDarkTheme(isDark: Boolean) {
        _isDarkTheme.value = isDark
    }

    fun updateUserRole(newRole: String) {
        _userProfile.value = _userProfile.value.copy(role = newRole)
    }

    init {
        val db = DataLensDatabase.getDatabase(application)
        repository = AnalysisRepository(db.analysisDao())
    }

    val analysisHistory: StateFlow<List<AnalysisEntity>> = repository.allAnalyses
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _currentScreen = MutableStateFlow(AppScreen.HOME)
    val currentScreen: StateFlow<AppScreen> = _currentScreen.asStateFlow()

    private val _screenBackStack = mutableListOf<AppScreen>()

    private val _activeDataset = MutableStateFlow<ParsedDataset?>(null)
    val activeDataset: StateFlow<ParsedDataset?> = _activeDataset.asStateFlow()

    private val _datasetProfile = MutableStateFlow<DatasetProfile?>(null)
    val datasetProfile: StateFlow<DatasetProfile?> = _datasetProfile.asStateFlow()

    private val _insights = MutableStateFlow<List<DataInsight>>(emptyList())
    val insights: StateFlow<List<DataInsight>> = _insights.asStateFlow()

    private val _selectedInsightDetail = MutableStateFlow<DataInsight?>(null)
    val selectedInsightDetail: StateFlow<DataInsight?> = _selectedInsightDetail.asStateFlow()

    private val _qualityIssues = MutableStateFlow<List<DataQualityIssue>>(emptyList())
    val qualityIssues: StateFlow<List<DataQualityIssue>> = _qualityIssues.asStateFlow()

    private val _askDataMessages = MutableStateFlow<List<AskDataMessage>>(emptyList())
    val askDataMessages: StateFlow<List<AskDataMessage>> = _askDataMessages.asStateFlow()

    private val _businessReport = MutableStateFlow<BusinessReport?>(null)
    val businessReport: StateFlow<BusinessReport?> = _businessReport.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _loadingStatus = MutableStateFlow("Analyzing dataset...")
    val loadingStatus: StateFlow<String> = _loadingStatus.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    private val _selectedInsightCategory = MutableStateFlow<InsightCategory?>(null)
    val selectedInsightCategory: StateFlow<InsightCategory?> = _selectedInsightCategory.asStateFlow()

    init {
        loadSampleDataset(silently = true)
    }

    fun navigateTo(screen: AppScreen) {
        if (_currentScreen.value != screen) {
            _screenBackStack.add(_currentScreen.value)
            _currentScreen.value = screen
        }
    }

    fun handleBack(): Boolean {
        if (_selectedInsightDetail.value != null) {
            _selectedInsightDetail.value = null
            return true
        }
        if (_screenBackStack.isNotEmpty()) {
            _currentScreen.value = _screenBackStack.removeAt(_screenBackStack.size - 1)
            return true
        } else if (_currentScreen.value != AppScreen.HOME) {
            _currentScreen.value = AppScreen.HOME
            return true
        }
        return false
    }

    fun selectInsightForDetail(insight: DataInsight?) {
        _selectedInsightDetail.value = insight
    }

    fun setInsightFilter(category: InsightCategory?) {
        _selectedInsightCategory.value = category
    }

    fun clearError() {
        _errorMessage.value = null
    }

    fun loadFromCloudUrl(urlString: String) {
        val cleanUrl = urlString.trim()
        if (cleanUrl.isEmpty()) return

        viewModelScope.launch {
            _isLoading.value = true
            _loadingStatus.value = "Fetching spreadsheet from cloud..."
            try {
                val dataset = withContext(Dispatchers.IO) {
                    val url = java.net.URL(cleanUrl)
                    val conn = url.openConnection() as java.net.HttpURLConnection
                    conn.connectTimeout = 15000
                    conn.readTimeout = 20000
                    conn.requestMethod = "GET"
                    conn.inputStream.use { stream ->
                        val fileName = if (cleanUrl.contains(".xlsx")) "Cloud_Dataset.xlsx" else "Cloud_Dataset.csv"
                        if (fileName.endsWith(".xlsx")) {
                            XlsxParser.parse(stream, fileName, "Cloud Stream")
                        } else {
                            CsvParser.parse(stream, fileName, "Cloud Stream")
                        }
                    }
                }
                processDataset(dataset)
                navigateTo(AppScreen.OVERVIEW)
            } catch (e: Exception) {
                _errorMessage.value = "Failed to load cloud spreadsheet: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun loadSampleDataset(silently: Boolean = false) {
        viewModelScope.launch {
            if (!silently) {
                _isLoading.value = true
                _loadingStatus.value = "Profiling sample dataset..."
            }
            try {
                val dataset = withContext(Dispatchers.Default) {
                    SampleDataGenerator.createSampleDataset()
                }
                processDataset(dataset)
                if (!silently) {
                    navigateTo(AppScreen.OVERVIEW)
                }
            } catch (e: Exception) {
                _errorMessage.value = "Failed to load sample dataset: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun loadFileFromUri(uri: Uri) {
        viewModelScope.launch {
            _isLoading.value = true
            _loadingStatus.value = "Detecting file format and structure..."
            try {
                val context = getApplication<Application>().applicationContext
                val contentResolver = context.contentResolver

                var fileName = "uploaded_data.csv"
                var fileSize = "Unknown"

                contentResolver.query(uri, null, null, null, null)?.use { cursor ->
                    if (cursor.moveToFirst()) {
                        val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                        val sizeIndex = cursor.getColumnIndex(OpenableColumns.SIZE)
                        if (nameIndex >= 0) {
                            fileName = cursor.getString(nameIndex) ?: fileName
                        }
                        if (sizeIndex >= 0) {
                            val bytes = cursor.getLong(sizeIndex)
                            fileSize = "${bytes / 1024} KB"
                        }
                    }
                }

                _loadingStatus.value = "Parsing columns and records..."

                val inputStream: InputStream = contentResolver.openInputStream(uri)
                    ?: throw IllegalArgumentException("Could not open file stream.")

                val dataset = withContext(Dispatchers.Default) {
                    inputStream.use { stream ->
                        if (fileName.endsWith(".xlsx", ignoreCase = true) || fileName.endsWith(".xls", ignoreCase = true)) {
                            XlsxParser.parse(stream, fileName, fileSize)
                        } else {
                            CsvParser.parse(stream, fileName, fileSize)
                        }
                    }
                }

                _loadingStatus.value = "Calculating statistics and detecting anomalies..."
                processDataset(dataset)
                navigateTo(AppScreen.OVERVIEW)
            } catch (e: Exception) {
                _errorMessage.value = "Could not parse file: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    private suspend fun processDataset(dataset: ParsedDataset) {
        withContext(Dispatchers.Default) {
            val profile = AnalyticsEngine.generateProfile(dataset)
            val generatedInsights = AnalyticsEngine.generateInsights(dataset)
            val issues = AnalyticsEngine.detectQualityIssues(dataset)
            val baseReport = AnalyticsEngine.generateBusinessReport(dataset, profile, generatedInsights)

            _activeDataset.value = dataset
            _datasetProfile.value = profile
            _insights.value = generatedInsights
            _qualityIssues.value = issues
            _businessReport.value = baseReport

            _askDataMessages.value = listOf(
                AskDataMessage(
                    text = "Welcome to Data Lens AI. Your dataset is loaded with ${dataset.rowCount} rows. What would you like to investigate?",
                    isUser = false,
                    directAnswer = "Ready to explore ${dataset.name}. Ask any question or tap a suggested topic below.",
                    keyMetric = "${profile.qualityScore}/100 Quality",
                    verificationBadge = "Data Lens Analytics Engine"
                )
            )

            val topInsight = generatedInsights.firstOrNull()
            repository.insert(
                AnalysisEntity(
                    id = dataset.id,
                    fileName = dataset.name,
                    fileSize = dataset.fileSizeString,
                    rowCount = dataset.rowCount,
                    columnCount = dataset.columnCount,
                    qualityScore = profile.qualityScore,
                    dateRange = profile.dateRange,
                    topInsightTitle = topInsight?.title ?: "Analysis Complete",
                    topInsightMetric = topInsight?.metric ?: "${dataset.rowCount} records",
                    executiveSummary = baseReport.executiveSummary
                )
            )
        }
    }

    fun askQuestion(query: String) {
        val trimmed = query.trim()
        if (trimmed.isEmpty()) return

        val dataset = _activeDataset.value ?: return
        val profile = _datasetProfile.value ?: return

        val userMsg = AskDataMessage(text = trimmed, isUser = true)
        _askDataMessages.value = _askDataMessages.value + userMsg

        viewModelScope.launch {
            _isLoading.value = true
            _loadingStatus.value = "Calculating verified metrics..."
            try {
                val fallback = withContext(Dispatchers.Default) {
                    AnalyticsEngine.answerQuery(dataset, trimmed)
                }

                val finalAnswer = geminiService.askData(dataset, profile, trimmed, fallback)

                _askDataMessages.value = _askDataMessages.value + finalAnswer
            } catch (e: Exception) {
                _askDataMessages.value = _askDataMessages.value + AskDataMessage(
                    text = trimmed,
                    isUser = false,
                    directAnswer = "Unable to process query: ${e.message}",
                    verificationBadge = "Engine Error"
                )
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun applyQualityFix(issueId: String) {
        val dataset = _activeDataset.value ?: return
        viewModelScope.launch {
            _isLoading.value = true
            _loadingStatus.value = "Normalizing records & recalculating health score..."
            try {
                val cleaned = withContext(Dispatchers.Default) {
                    AnalyticsEngine.cleanDataset(dataset, issueId)
                }
                processDataset(cleaned)
            } catch (e: Exception) {
                _errorMessage.value = "Failed to apply fix: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun getExportableCsv(): String {
        val dataset = _activeDataset.value ?: return ""
        return AnalyticsEngine.exportDatasetAsCsv(dataset)
    }

    fun generateExecutiveReport() {
        val dataset = _activeDataset.value ?: return
        val profile = _datasetProfile.value ?: return
        val report = _businessReport.value ?: return

        viewModelScope.launch {
            _isLoading.value = true
            _loadingStatus.value = "Synthesizing executive business recommendations..."
            try {
                val enhanced = geminiService.enhanceExecutiveReport(dataset, profile, report)
                _businessReport.value = enhanced
            } catch (e: Exception) {
                _errorMessage.value = "Report enhancement error: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun deleteAnalysis(id: String) {
        viewModelScope.launch {
            repository.deleteById(id)
            if (_activeDataset.value?.id == id) {
                clearCurrentDataset()
            }
        }
    }

    fun clearCurrentDataset() {
        _activeDataset.value = null
        _datasetProfile.value = null
        _insights.value = emptyList()
        _qualityIssues.value = emptyList()
        _askDataMessages.value = emptyList()
        _businessReport.value = null
        _currentScreen.value = AppScreen.HOME
    }
}
