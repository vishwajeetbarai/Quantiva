package com.example.datalens.engine

import com.example.datalens.model.ColumnType
import com.example.datalens.model.DataColumnInfo
import com.example.datalens.model.ParsedDataset
import java.io.BufferedReader
import java.io.InputStream
import java.io.InputStreamReader
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.UUID

object CsvParser {

    private val DATE_PATTERNS = listOf(
        "yyyy-MM-dd",
        "yyyy/MM/dd",
        "dd-MM-yyyy",
        "dd/MM/yyyy",
        "MM/dd/yyyy",
        "yyyy-MM-dd HH:mm:ss",
        "MMM yyyy",
        "MMMM yyyy"
    )

    fun parse(
        inputStream: InputStream,
        fileName: String,
        fileSizeApprox: String = "Unknown"
    ): ParsedDataset {
        val reader = BufferedReader(InputStreamReader(inputStream, Charsets.UTF_8))
        val rawLines = mutableListOf<String>()
        var line: String?

        while (reader.readLine().also { line = it } != null) {
            val trimmed = line!!.trim()
            if (trimmed.isNotEmpty()) {
                rawLines.add(line!!)
            }
        }

        if (rawLines.isEmpty()) {
            throw IllegalArgumentException("The selected file is empty.")
        }

        // Detect delimiter (comma, semicolon, tab)
        val delimiter = detectDelimiter(rawLines.take(5))

        val parsedRows = mutableListOf<List<String>>()
        for (rawLine in rawLines) {
            val tokens = parseCsvLine(rawLine, delimiter)
            if (tokens.isNotEmpty()) {
                parsedRows.add(tokens)
            }
        }

        if (parsedRows.isEmpty()) {
            throw IllegalArgumentException("No tabular rows could be parsed from the file.")
        }

        val headers = parsedRows.first().mapIndexed { index, header ->
            val clean = header.trim().replace("\"", "").replace("\uFEFF", "")
            if (clean.isEmpty()) "Column_${index + 1}" else clean
        }

        val dataRows = if (parsedRows.size > 1) parsedRows.subList(1, parsedRows.size) else emptyList()

        // Normalize row lengths
        val normalizedRows = dataRows.map { row ->
            if (row.size < headers.size) {
                row + List(headers.size - row.size) { "" }
            } else if (row.size > headers.size) {
                row.take(headers.size)
            } else {
                row
            }
        }

        // Profile each column
        val columns = headers.mapIndexed { colIndex, headerName ->
            val columnValues = normalizedRows.map { it.getOrElse(colIndex) { "" }.trim() }
            analyzeColumn(headerName, columnValues)
        }

        return ParsedDataset(
            id = UUID.randomUUID().toString(),
            name = fileName,
            rowCount = normalizedRows.size,
            columnCount = headers.size,
            columns = columns,
            headers = headers,
            rows = normalizedRows,
            fileFormat = "CSV",
            fileSizeString = fileSizeApprox,
            uploadedAt = System.currentTimeMillis()
        )
    }

    private fun detectDelimiter(sampleLines: List<String>): Char {
        val candidates = listOf(',', ';', '\t')
        var bestChar = ','
        var maxMatches = -1

        for (c in candidates) {
            val counts = sampleLines.map { line -> line.count { it == c } }
            val firstCount = counts.firstOrNull() ?: 0
            if (firstCount > 0 && counts.all { it == firstCount }) {
                if (firstCount > maxMatches) {
                    maxMatches = firstCount
                    bestChar = c
                }
            }
        }
        return bestChar
    }

    fun parseCsvLine(line: String, delimiter: Char = ','): List<String> {
        val tokens = mutableListOf<String>()
        val sb = StringBuilder()
        var inQuotes = false
        var i = 0

        while (i < line.length) {
            val c = line[i]
            if (c == '"') {
                if (inQuotes && i + 1 < line.length && line[i + 1] == '"') {
                    sb.append('"')
                    i++ // Skip escaped quote
                } else {
                    inQuotes = !inQuotes
                }
            } else if (c == delimiter && !inQuotes) {
                tokens.add(sb.toString().trim())
                sb.setLength(0)
            } else {
                sb.append(c)
            }
            i++
        }
        tokens.add(sb.toString().trim())
        return tokens
    }

    fun analyzeColumn(name: String, values: List<String>): DataColumnInfo {
        var nullCount = 0
        val nonNullValues = mutableListOf<String>()
        val numericValues = mutableListOf<Double>()
        var dateCount = 0
        var boolCount = 0

        for (v in values) {
            val clean = v.trim()
            if (clean.isEmpty() || clean.equals("null", ignoreCase = true) || clean.equals("n/a", ignoreCase = true) || clean.equals("nan", ignoreCase = true) || clean == "-") {
                nullCount++
            } else {
                nonNullValues.add(clean)
                val num = parseNumeric(clean)
                if (num != null) {
                    numericValues.add(num)
                } else if (isDate(clean)) {
                    dateCount++
                } else if (clean.equals("true", ignoreCase = true) || clean.equals("false", ignoreCase = true) || clean.equals("yes", ignoreCase = true) || clean.equals("no", ignoreCase = true)) {
                    boolCount++
                }
            }
        }

        val totalNonNull = nonNullValues.size
        val type = when {
            totalNonNull == 0 -> ColumnType.TEXT
            numericValues.size.toDouble() / totalNonNull >= 0.75 -> ColumnType.NUMERICAL
            dateCount.toDouble() / totalNonNull >= 0.60 -> ColumnType.DATE
            boolCount.toDouble() / totalNonNull >= 0.80 -> ColumnType.BOOLEAN
            else -> {
                val uniqueSet = nonNullValues.toSet()
                if (uniqueSet.size <= 30 || uniqueSet.size.toDouble() / totalNonNull < 0.25) {
                    ColumnType.CATEGORICAL
                } else {
                    ColumnType.TEXT
                }
            }
        }

        val uniqueCount = nonNullValues.toSet().size
        val sampleValues = nonNullValues.distinct().take(5)

        val topCategories = if (type == ColumnType.CATEGORICAL || type == ColumnType.TEXT) {
            nonNullValues.groupingBy { it }.eachCount().toList()
                .sortedByDescending { it.second }
                .take(6)
                .toMap()
        } else {
            emptyMap()
        }

        var min: Double? = null
        var max: Double? = null
        var mean: Double? = null
        var median: Double? = null
        var sum: Double? = null

        if (type == ColumnType.NUMERICAL && numericValues.isNotEmpty()) {
            val sorted = numericValues.sorted()
            min = sorted.first()
            max = sorted.last()
            sum = sorted.sum()
            mean = sum / sorted.size
            median = if (sorted.size % 2 == 0) {
                (sorted[sorted.size / 2 - 1] + sorted[sorted.size / 2]) / 2.0
            } else {
                sorted[sorted.size / 2]
            }
        }

        return DataColumnInfo(
            name = name,
            type = type,
            nullCount = nullCount,
            uniqueCount = uniqueCount,
            sampleValues = sampleValues,
            min = min,
            max = max,
            mean = mean,
            median = median,
            sum = sum,
            topCategories = topCategories
        )
    }

    private fun parseNumeric(value: String): Double? {
        val sanitized = value
            .replace(",", "")
            .replace("$", "")
            .replace("₹", "")
            .replace("€", "")
            .replace("£", "")
            .replace("%", "")
            .trim()
        return sanitized.toDoubleOrNull()
    }

    private fun isDate(value: String): Boolean {
        if (value.length < 6 || value.length > 30) return false
        for (pattern in DATE_PATTERNS) {
            try {
                val sdf = SimpleDateFormat(pattern, Locale.US)
                sdf.isLenient = false
                sdf.parse(value)
                return true
            } catch (_: Exception) {
            }
        }
        return false
    }
}
