package com.example.datalens.engine

import android.util.Xml
import com.example.datalens.model.ParsedDataset
import org.xmlpull.v1.XmlPullParser
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.InputStream
import java.util.zip.ZipInputStream

object XlsxParser {

    fun parse(
        inputStream: InputStream,
        fileName: String,
        fileSizeApprox: String = "Unknown"
    ): ParsedDataset {
        // Read zip entries in memory or stream
        val sharedStrings = mutableListOf<String>()
        var sheetBytes: ByteArray? = null

        val zip = ZipInputStream(inputStream)
        var entry = zip.nextEntry

        while (entry != null) {
            val name = entry.name.lowercase()
            if (name.endsWith("sharedstrings.xml")) {
                val baos = ByteArrayOutputStream()
                val buffer = ByteArray(4096)
                var len: Int
                while (zip.read(buffer).also { len = it } > 0) {
                    baos.write(buffer, 0, len)
                }
                parseSharedStrings(ByteArrayInputStream(baos.toByteArray()), sharedStrings)
            } else if (name.contains("sheet1.xml") || (name.contains("sheet") && name.endsWith(".xml") && sheetBytes == null)) {
                val baos = ByteArrayOutputStream()
                val buffer = ByteArray(4096)
                var len: Int
                while (zip.read(buffer).also { len = it } > 0) {
                    baos.write(buffer, 0, len)
                }
                sheetBytes = baos.toByteArray()
            }
            zip.closeEntry()
            entry = zip.nextEntry
        }

        if (sheetBytes == null) {
            throw IllegalArgumentException("Could not find worksheet in Excel file.")
        }

        val rows = parseSheetRows(ByteArrayInputStream(sheetBytes), sharedStrings)
        if (rows.isEmpty()) {
            throw IllegalArgumentException("The Excel sheet contains no readable rows.")
        }

        // Convert the parsed rows into CSV format string and pass to CsvParser for unified column profiling
        val csvBuilder = StringBuilder()
        for (row in rows) {
            val line = row.joinToString(",") { cell ->
                "\"${cell.replace("\"", "\"\"")}\""
            }
            csvBuilder.append(line).append("\n")
        }

        return CsvParser.parse(
            inputStream = ByteArrayInputStream(csvBuilder.toString().toByteArray(Charsets.UTF_8)),
            fileName = fileName,
            fileSizeApprox = fileSizeApprox
        )
    }

    private fun parseSharedStrings(stream: InputStream, outStrings: MutableList<String>) {
        val parser = Xml.newPullParser()
        parser.setInput(stream, "UTF-8")
        var eventType = parser.eventType
        val currentText = StringBuilder()
        var insideT = false

        while (eventType != XmlPullParser.END_DOCUMENT) {
            val tagName = parser.name
            when (eventType) {
                XmlPullParser.START_TAG -> {
                    if (tagName.equals("t", ignoreCase = true)) {
                        insideT = true
                    } else if (tagName.equals("si", ignoreCase = true)) {
                        currentText.setLength(0)
                    }
                }
                XmlPullParser.TEXT -> {
                    if (insideT) {
                        currentText.append(parser.text)
                    }
                }
                XmlPullParser.END_TAG -> {
                    if (tagName.equals("t", ignoreCase = true)) {
                        insideT = false
                    } else if (tagName.equals("si", ignoreCase = true)) {
                        outStrings.add(currentText.toString())
                    }
                }
            }
            eventType = parser.next()
        }
    }

    private fun parseSheetRows(
        stream: InputStream,
        sharedStrings: List<String>
    ): List<List<String>> {
        val rows = mutableListOf<List<String>>()
        val parser = Xml.newPullParser()
        parser.setInput(stream, "UTF-8")

        var eventType = parser.eventType
        val currentRow = mutableMapOf<Int, String>()
        var currentCellRef = ""
        var cellType = ""
        var insideV = false
        var cellValue = StringBuilder()

        while (eventType != XmlPullParser.END_DOCUMENT) {
            val tagName = parser.name
            when (eventType) {
                XmlPullParser.START_TAG -> {
                    if (tagName.equals("row", ignoreCase = true)) {
                        currentRow.clear()
                    } else if (tagName.equals("c", ignoreCase = true)) {
                        currentCellRef = parser.getAttributeValue(null, "r") ?: ""
                        cellType = parser.getAttributeValue(null, "t") ?: ""
                        cellValue.setLength(0)
                    } else if (tagName.equals("v", ignoreCase = true)) {
                        insideV = true
                    }
                }
                XmlPullParser.TEXT -> {
                    if (insideV) {
                        cellValue.append(parser.text)
                    }
                }
                XmlPullParser.END_TAG -> {
                    if (tagName.equals("v", ignoreCase = true)) {
                        insideV = false
                    } else if (tagName.equals("c", ignoreCase = true)) {
                        val colIndex = colRefToIndex(currentCellRef)
                        var resolvedText = cellValue.toString().trim()
                        if (cellType == "s") {
                            val strIndex = resolvedText.toIntOrNull()
                            if (strIndex != null && strIndex in sharedStrings.indices) {
                                resolvedText = sharedStrings[strIndex]
                            }
                        }
                        if (colIndex >= 0) {
                            currentRow[colIndex] = resolvedText
                        }
                    } else if (tagName.equals("row", ignoreCase = true)) {
                        if (currentRow.isNotEmpty()) {
                            val maxCol = currentRow.keys.maxOrNull() ?: 0
                            val rowList = (0..maxCol).map { col -> currentRow[col] ?: "" }
                            rows.add(rowList)
                        }
                    }
                }
            }
            eventType = parser.next()
        }
        return rows
    }

    private fun colRefToIndex(ref: String): Int {
        val letters = ref.takeWhile { it.isLetter() }.uppercase()
        if (letters.isEmpty()) return -1
        var result = 0
        for (c in letters) {
            result = result * 26 + (c - 'A' + 1)
        }
        return result - 1
    }
}
