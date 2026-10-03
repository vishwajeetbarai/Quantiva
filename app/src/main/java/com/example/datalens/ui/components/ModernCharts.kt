package com.example.datalens.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.datalens.model.ChartDataPoint
import com.example.ui.theme.*
import java.util.Locale

@Composable
fun DataLensBarChart(
    points: List<ChartDataPoint>,
    modifier: Modifier = Modifier,
    barColor: Color = CyanPrimary,
    accentColor: Color = IndigoAccent
) {
    if (points.isEmpty()) return

    val animatedProgress = remember { Animatable(0f) }
    LaunchedEffect(points) {
        animatedProgress.snapTo(0f)
        animatedProgress.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 650, easing = FastOutSlowInEasing)
        )
    }

    val maxValue = remember(points) { (points.maxOfOrNull { it.value } ?: 1.0).coerceAtLeast(1.0) }
    val outlineColor = MaterialTheme.colorScheme.outlineVariant

    Column(modifier = modifier.fillMaxWidth()) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(180.dp)
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val width = size.width
                val height = size.height
                val bottomPadding = 24.dp.toPx()
                val topPadding = 16.dp.toPx()
                val chartHeight = height - bottomPadding - topPadding

                // Horizontal Grid lines (3 levels)
                for (i in 0..3) {
                    val y = topPadding + (chartHeight / 3f) * i
                    drawLine(
                        color = outlineColor.copy(alpha = 0.4f),
                        start = Offset(0f, y),
                        end = Offset(width, y),
                        strokeWidth = 1.dp.toPx(),
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)
                    )
                }

                val barCount = points.size
                val totalSpacing = width * 0.3f
                val barWidth = ((width - totalSpacing) / barCount).coerceIn(16.dp.toPx(), 44.dp.toPx())
                val step = width / barCount

                points.forEachIndexed { index, point ->
                    val normalized = (point.value / maxValue).toFloat().coerceIn(0f, 1f)
                    val currentBarHeight = chartHeight * normalized * animatedProgress.value
                    val x = step * index + (step - barWidth) / 2f
                    val y = topPadding + chartHeight - currentBarHeight

                    val isTop = point.value == maxValue
                    val brush = Brush.verticalGradient(
                        colors = if (isTop) listOf(accentColor, CyanPrimary) else listOf(barColor.copy(alpha = 0.9f), barColor.copy(alpha = 0.6f))
                    )

                    drawRoundRect(
                        brush = brush,
                        topLeft = Offset(x, y),
                        size = Size(barWidth, currentBarHeight),
                        cornerRadius = CornerRadius(6.dp.toPx(), 6.dp.toPx())
                    )
                }
            }
        }

        // Labels row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 6.dp),
            horizontalArrangement = Arrangement.SpaceAround
        ) {
            points.forEach { point ->
                Text(
                    text = point.label,
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1
                )
            }
        }
    }
}

@Composable
fun DataLensLineChart(
    points: List<ChartDataPoint>,
    modifier: Modifier = Modifier,
    lineColor: Color = CyanPrimary
) {
    if (points.isEmpty()) return

    val animatedProgress = remember { Animatable(0f) }
    LaunchedEffect(points) {
        animatedProgress.snapTo(0f)
        animatedProgress.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 750, easing = FastOutSlowInEasing)
        )
    }

    val maxValue = remember(points) { (points.maxOfOrNull { it.value } ?: 1.0).coerceAtLeast(1.0) }
    val minValue = remember(points) { (points.minOfOrNull { it.value } ?: 0.0).coerceAtLeast(0.0) }
    val range = (maxValue - minValue).coerceAtLeast(1.0)
    val outlineColor = MaterialTheme.colorScheme.outlineVariant

    Column(modifier = modifier.fillMaxWidth()) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(180.dp)
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val width = size.width
                val height = size.height
                val bottomPadding = 20.dp.toPx()
                val topPadding = 16.dp.toPx()
                val chartHeight = height - bottomPadding - topPadding

                // Grid lines
                for (i in 0..2) {
                    val y = topPadding + (chartHeight / 2f) * i
                    drawLine(
                        color = outlineColor.copy(alpha = 0.4f),
                        start = Offset(0f, y),
                        end = Offset(width, y),
                        strokeWidth = 1.dp.toPx()
                    )
                }

                if (points.size >= 2) {
                    val stepX = width / (points.size - 1)
                    val coords = points.mapIndexed { idx, pt ->
                        val norm = ((pt.value - minValue) / range).toFloat().coerceIn(0f, 1f)
                        val y = topPadding + chartHeight - (chartHeight * norm * animatedProgress.value)
                        Offset(idx * stepX, y)
                    }

                    // Fill Path under curve
                    val fillPath = Path().apply {
                        moveTo(coords.first().x, topPadding + chartHeight)
                        coords.forEach { lineTo(it.x, it.y) }
                        lineTo(coords.last().x, topPadding + chartHeight)
                        close()
                    }

                    drawPath(
                        path = fillPath,
                        brush = Brush.verticalGradient(
                            colors = listOf(lineColor.copy(alpha = 0.35f), lineColor.copy(alpha = 0.02f)),
                            startY = topPadding,
                            endY = topPadding + chartHeight
                        )
                    )

                    // Stroke Path
                    val strokePath = Path().apply {
                        moveTo(coords.first().x, coords.first().y)
                        for (i in 1 until coords.size) {
                            lineTo(coords[i].x, coords[i].y)
                        }
                    }

                    drawPath(
                        path = strokePath,
                        color = lineColor,
                        style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)
                    )

                    // Points
                    coords.forEach { coord ->
                        drawCircle(
                            color = Color.White,
                            radius = 4.dp.toPx(),
                            center = coord
                        )
                        drawCircle(
                            color = lineColor,
                            radius = 2.5.dp.toPx(),
                            center = coord
                        )
                    }
                }
            }
        }

        // Labels
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 6.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            points.forEach { pt ->
                Text(
                    text = pt.label,
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
fun DataLensDonutChart(
    points: List<ChartDataPoint>,
    modifier: Modifier = Modifier
) {
    if (points.isEmpty()) return

    val animatedProgress = remember { Animatable(0f) }
    LaunchedEffect(points) {
        animatedProgress.snapTo(0f)
        animatedProgress.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 800, easing = FastOutSlowInEasing)
        )
    }

    val palette = listOf(CyanPrimary, IndigoAccent, VioletAccent, TealAccent, EmeraldGreen, AmberWarning)
    val total = remember(points) { points.sumOf { it.value }.coerceAtLeast(1.0) }

    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(150.dp)
                .padding(8.dp),
            contentAlignment = Alignment.Center
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val strokeWidth = 26.dp.toPx()
                val radius = (size.minDimension - strokeWidth) / 2f
                val centerOffset = Offset(size.width / 2f, size.height / 2f)

                var startAngle = -90f
                points.forEachIndexed { idx, pt ->
                    val sweep = ((pt.value / total) * 360f).toFloat() * animatedProgress.value
                    val color = palette[idx % palette.size]
                    drawArc(
                        color = color,
                        startAngle = startAngle,
                        sweepAngle = sweep - 2f,
                        useCenter = false,
                        topLeft = Offset(centerOffset.x - radius, centerOffset.y - radius),
                        size = Size(radius * 2, radius * 2),
                        style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                    )
                    startAngle += sweep
                }
            }

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "${points.size}",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "Categories",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Spacer(modifier = Modifier.width(16.dp))

        // Legend
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            points.take(5).forEachIndexed { idx, pt ->
                val color = palette[idx % palette.size]
                val pct = (pt.value / total) * 100.0
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .background(color, CircleShape)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = pt.label,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 1
                        )
                    }
                    Text(
                        text = "${String.format(Locale.US, "%.1f", pct)}%",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}
