package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.datalens.engine.AnalyticsEngine
import com.example.datalens.engine.SampleDataGenerator
import com.example.datalens.model.InsightCategory
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("Quantiva", appName)
  }

  @Test
  fun `sample dataset loads and profiles correctly`() {
    val dataset = SampleDataGenerator.createSampleDataset()
    assertNotNull(dataset)
    assertTrue(dataset.rowCount > 20)
    assertTrue(dataset.columns.isNotEmpty())

    val profile = AnalyticsEngine.generateProfile(dataset)
    assertTrue(profile.qualityScore in 40..100)
    assertTrue(profile.totalRows == dataset.rowCount)

    val insights = AnalyticsEngine.generateInsights(dataset)
    assertTrue(insights.isNotEmpty())
    assertTrue(insights.any { it.category == InsightCategory.TREND })
    assertTrue(insights.any { it.category == InsightCategory.TOP_PERFORMER })

    val issues = AnalyticsEngine.detectQualityIssues(dataset)
    assertTrue(issues.isNotEmpty())
  }

  @Test
  fun `cleanDataset normalizes spellings and deduplicates rows`() {
    val dataset = SampleDataGenerator.createSampleDataset()
    val initialRows = dataset.rowCount
    val issues = AnalyticsEngine.detectQualityIssues(dataset)
    
    val dupIssue = issues.firstOrNull { it.fixActionType == "REMOVE_DUPLICATES" }
    assertNotNull(dupIssue)
    
    val cleaned = AnalyticsEngine.cleanDataset(dataset, dupIssue!!.id)
    assertEquals(initialRows - dupIssue.affectedRows, cleaned.rowCount)

    val exportedCsv = AnalyticsEngine.exportDatasetAsCsv(cleaned)
    assertTrue(exportedCsv.contains("Date") && exportedCsv.contains("Product") && exportedCsv.contains("Category"))
    assertTrue(exportedCsv.contains("UltraBook Pro X"))
  }

  @Test
  fun `ask your data answers customer queries with table snippets`() {
    val dataset = SampleDataGenerator.createSampleDataset()
    val answer = AnalyticsEngine.answerQuery(dataset, "Which customers generate the most revenue?")
    
    assertNotNull(answer.directAnswer)
    assertNotNull(answer.tableSnippet)
    assertTrue(answer.tableSnippet!!.isNotEmpty())
    assertNotNull(answer.tableHeaders)
  }

  @Test
  fun `advanced analytics forecasting and Pareto calculations`() {
    val dataset = SampleDataGenerator.createSampleDataset()
    
    val forecast = AnalyticsEngine.calculateForecast(dataset)
    assertTrue(forecast.historicalPoints.isNotEmpty())
    assertTrue(forecast.baselinePoints.isNotEmpty())
    assertTrue(forecast.projectedQ4Revenue.isNotBlank())

    val pareto = AnalyticsEngine.calculateParetoAnalysis(dataset)
    assertEquals(3, pareto.size)
    assertTrue(pareto.first().shareOfRevenuePct in 10.0..100.0)
    assertTrue(pareto.first().count > 0)

    val simulation = AnalyticsEngine.simulateFinancialScenario(
        dataset = dataset,
        discountAdjustmentPct = -2.0,
        cloudAttachRatePct = 30.0,
        supplyBufferUnits = 30
    )
    assertTrue(simulation.isPositiveDelta)
    assertTrue(simulation.projectedProfit.isNotBlank())
  }
}
