package com.example.datalens.export

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import androidx.core.content.FileProvider
import com.example.datalens.model.BusinessReport
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object PdfReportGenerator {

    fun generateAndSharePdf(context: Context, report: BusinessReport) {
        val document = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create() // A4 dimensions
        val page = document.startPage(pageInfo)
        val canvas = page.canvas

        val titlePaint = Paint().apply {
            color = Color.rgb(15, 23, 42) // Slate900
            textSize = 20f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            isAntiAlias = true
        }

        val subtitlePaint = Paint().apply {
            color = Color.rgb(14, 165, 233) // CyanPrimary
            textSize = 11f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            isAntiAlias = true
        }

        val textPaint = Paint().apply {
            color = Color.rgb(30, 41, 59)
            textSize = 10f
            isAntiAlias = true
        }

        val secondaryPaint = Paint().apply {
            color = Color.rgb(100, 116, 139)
            textSize = 9f
            isAntiAlias = true
        }

        val headerBgPaint = Paint().apply {
            color = Color.rgb(241, 245, 249)
            style = Paint.Style.FILL
        }

        val cardBgPaint = Paint().apply {
            color = Color.rgb(248, 250, 252)
            style = Paint.Style.FILL
        }

        val borderPaint = Paint().apply {
            color = Color.rgb(226, 232, 240)
            style = Paint.Style.STROKE
            strokeWidth = 1f
        }

        val accentCardPaint = Paint().apply {
            color = Color.rgb(224, 242, 254)
            style = Paint.Style.FILL
        }

        // Draw Header
        canvas.drawRect(0f, 0f, 595f, 75f, headerBgPaint)
        canvas.drawText("QUANTIVA", 36f, 38f, titlePaint)
        canvas.drawText("EXECUTIVE BUSINESS INTELLIGENCE REPORT", 36f, 54f, subtitlePaint)

        val dateStr = SimpleDateFormat("MMM dd, yyyy", Locale.US).format(Date(report.generatedAt))
        canvas.drawText("Dataset: ${report.datasetName} | Date: $dateStr", 350f, 38f, secondaryPaint)

        var yPos = 95f

        // Executive Summary Card
        canvas.drawRoundRect(36f, yPos, 559f, yPos + 75f, 6f, 6f, cardBgPaint)
        canvas.drawRoundRect(36f, yPos, 559f, yPos + 75f, 6f, 6f, borderPaint)

        canvas.drawText("EXECUTIVE SUMMARY", 50f, yPos + 20f, subtitlePaint)
        
        // Wrap executive summary lines
        val summaryWords = report.executiveSummary.split(" ")
        var currentLine = StringBuilder()
        var lineY = yPos + 36f
        for (word in summaryWords) {
            if (currentLine.length + word.length > 85) {
                canvas.drawText(currentLine.toString(), 50f, lineY, textPaint)
                lineY += 14f
                currentLine = StringBuilder()
            }
            currentLine.append(word).append(" ")
        }
        if (currentLine.isNotEmpty()) {
            canvas.drawText(currentLine.toString(), 50f, lineY, textPaint)
        }

        yPos += 95f

        // Key KPIs Grid
        canvas.drawText("KEY PERFORMANCE INDICATORS", 36f, yPos, titlePaint.apply { textSize = 13f })
        yPos += 14f

        val kpiWidth = 120f
        val kpiHeight = 48f
        report.keyKpis.take(4).forEachIndexed { index, (label, value) ->
            val kpiX = 36f + index * (kpiWidth + 14f)
            canvas.drawRoundRect(kpiX, yPos, kpiX + kpiWidth, yPos + kpiHeight, 6f, 6f, accentCardPaint)
            canvas.drawRoundRect(kpiX, yPos, kpiX + kpiWidth, yPos + kpiHeight, 6f, 6f, borderPaint)

            canvas.drawText(label, kpiX + 10f, yPos + 18f, secondaryPaint)
            canvas.drawText(value, kpiX + 10f, yPos + 38f, titlePaint.apply { textSize = 13f })
        }

        yPos += 70f

        // Major Trends
        canvas.drawText("MAJOR TRENDS & SEASONALITY", 36f, yPos, titlePaint.apply { textSize = 13f })
        yPos += 16f
        report.majorTrends.take(3).forEach { trend ->
            canvas.drawCircle(42f, yPos - 3f, 3f, subtitlePaint)
            canvas.drawText(trend.take(90), 52f, yPos, textPaint)
            yPos += 16f
        }

        yPos += 14f

        // Strategic Recommendations
        canvas.drawText("STRATEGIC RECOMMENDATIONS", 36f, yPos, titlePaint.apply { textSize = 13f })
        yPos += 16f
        report.recommendations.take(3).forEachIndexed { idx, rec ->
            canvas.drawText("${idx + 1}. ${rec.take(95)}", 36f, yPos, textPaint)
            yPos += 16f
        }

        yPos += 14f

        // Phased Execution Roadmaps
        if (report.roadmap30Days.isNotEmpty()) {
            canvas.drawText("30-DAY OPERATIONAL ROADMAP", 36f, yPos, subtitlePaint)
            yPos += 16f
            report.roadmap30Days.take(3).forEach { action ->
                canvas.drawText("• $action", 36f, yPos, textPaint)
                yPos += 15f
            }
        }

        yPos += 10f

        if (report.roadmap90Days.isNotEmpty()) {
            canvas.drawText("90-DAY GROWTH & SCALE ROADMAP", 36f, yPos, subtitlePaint)
            yPos += 16f
            report.roadmap90Days.take(3).forEach { action ->
                canvas.drawText("• $action", 36f, yPos, textPaint)
                yPos += 15f
            }
        }

        // Footer
        canvas.drawText("Confidential • Generated by Quantiva • Verified by Analytics Engine & Gemini AI", 36f, 810f, secondaryPaint)

        document.finishPage(page)

        // Save PDF to cache and share
        val cacheFile = File(context.cacheDir, "Quantiva_Executive_Report_${System.currentTimeMillis()}.pdf")
        FileOutputStream(cacheFile).use { out ->
            document.writeTo(out)
        }
        document.close()

        val uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            cacheFile
        )

        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "application/pdf"
            putExtra(Intent.EXTRA_STREAM, uri)
            putExtra(Intent.EXTRA_TITLE, "Quantiva - Executive Report")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }

        context.startActivity(Intent.createChooser(intent, "Share Executive PDF Report"))
    }
}
