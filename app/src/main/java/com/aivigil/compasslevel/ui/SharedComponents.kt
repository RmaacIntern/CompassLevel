package com.aivigil.compasslevel.ui

import android.graphics.Paint
import android.graphics.Typeface
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aivigil.compasslevel.ui.theme.*
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun TopActionBar() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
            .background(PureBlack)
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = "Compass & Level",
            color = TextWhite,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold
        )
        Canvas(modifier = Modifier.size(24.dp)) {
            val cx = size.width / 2f
            drawCircle(color = TextMuted, radius = 2.dp.toPx(), center = Offset(cx, 5.dp.toPx()))
            drawCircle(color = TextMuted, radius = 2.dp.toPx(), center = Offset(cx, 12.dp.toPx()))
            drawCircle(color = TextMuted, radius = 2.dp.toPx(), center = Offset(cx, 19.dp.toPx()))
        }
    }
}

@Composable
fun StateSelectorBar(
    selectedTab: String,
    onTabSelected: (String) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        listOf("Live", "Loading", "Content", "Empty", "Error").forEach { tab ->
            val isSelected = (selectedTab == tab)
            val bg = if (isSelected) Color(0xFF1A3826) else Color.Transparent
            val textColor = if (isSelected) TargetGreen else TextMuted
            val border = if (isSelected) TargetGreen else Color.Transparent

            Box(
                modifier = Modifier
                    .weight(1f)
                    .background(bg, RoundedCornerShape(6.dp))
                    .border(1.dp, border, RoundedCornerShape(6.dp))
                    .clickable { onTabSelected(tab) }
                    .padding(vertical = 6.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = tab,
                    color = textColor,
                    fontSize = 12.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

@Composable
fun AdBannerBottom() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(50.dp)
            .background(BannerBg),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "- |||DVERTISEONT - 320<50",
            color = BannerText,
            fontSize = 11.sp,
            fontFamily = FontFamily.Monospace,
            letterSpacing = 1.sp
        )
    }
}

@Composable
fun PillCard(label: String, value: String) {
    Row(
        modifier = Modifier
            .background(CardSurface, RoundedCornerShape(16.dp))
            .padding(horizontal = 14.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Text(
            text = label,
            color = TextMuted,
            fontSize = 11.sp,
            fontFamily = FontFamily.Monospace
        )
        Text(
            text = value,
            color = TextWhite,
            fontSize = 14.sp,
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun CompassRoseDial(heading: Float, pitch: Float, roll: Float, modifier: Modifier = Modifier) {
    Box(modifier = modifier.size(260.dp), contentAlignment = Alignment.Center) {
        // Rotating compass circle
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .rotate(-heading)
        ) {
            val center = Offset(size.width / 2f, size.height / 2f)
            val outerRadius = 120.dp.toPx()
            val innerRadius = 84.dp.toPx()

            drawCircle(color = BorderSubtle, radius = outerRadius, center = center, style = Stroke(1.dp.toPx()))
            drawCircle(color = BorderSubtle, radius = innerRadius, center = center, style = Stroke(1.dp.toPx()))

            for (i in 0 until 12) {
                val angleDeg = i * 30.0
                val angleRad = Math.toRadians(angleDeg - 90.0)
                val isMajor = (i % 3 == 0)
                val tickLen = if (isMajor) 14.dp.toPx() else 8.dp.toPx()
                val tickColor = if (isMajor) TextMuted else BorderStrong
                val tickStroke = if (isMajor) 1.5.dp.toPx() else 1.dp.toPx()

                val start = Offset(
                    x = (center.x + (outerRadius - tickLen) * cos(angleRad)).toFloat(),
                    y = (center.y + (outerRadius - tickLen) * sin(angleRad)).toFloat()
                )
                val end = Offset(
                    x = (center.x + outerRadius * cos(angleRad)).toFloat(),
                    y = (center.y + outerRadius * sin(angleRad)).toFloat()
                )
                drawLine(color = tickColor, start = start, end = end, strokeWidth = tickStroke)
            }

            val textPaint = Paint().apply {
                isAntiAlias = true
                textAlign = Paint.Align.CENTER
                typeface = Typeface.create(Typeface.MONOSPACE, Typeface.NORMAL)
            }

            val cardinals = listOf(
                Pair("N", 0.0), Pair("NE", 45.0), Pair("E", 90.0), Pair("SE", 135.0),
                Pair("S", 180.0), Pair("SW", 225.0), Pair("W", 270.0), Pair("NW", 315.0)
            )

            for ((label, deg) in cardinals) {
                val rad = Math.toRadians(deg - 90.0)
                val r = outerRadius - 24.dp.toPx()

                if (label == "N") {
                    textPaint.color = android.graphics.Color.parseColor("#FF3B30")
                    textPaint.textSize = 14.sp.toPx()
                    textPaint.typeface = Typeface.create(Typeface.MONOSPACE, Typeface.BOLD)
                } else if (label in listOf("E", "S", "W")) {
                    textPaint.color = android.graphics.Color.WHITE
                    textPaint.textSize = 12.sp.toPx()
                    textPaint.typeface = Typeface.create(Typeface.MONOSPACE, Typeface.NORMAL)
                } else {
                    textPaint.color = android.graphics.Color.parseColor("#8E8E93")
                    textPaint.textSize = 10.sp.toPx()
                    textPaint.typeface = Typeface.create(Typeface.MONOSPACE, Typeface.NORMAL)
                }

                val x = (center.x + r * cos(rad)).toFloat()
                val y = (center.y + r * sin(rad)).toFloat() - ((textPaint.descent() + textPaint.ascent()) / 2f)
                drawContext.canvas.nativeCanvas.drawText(label, x, y, textPaint)
            }
        }

        // Top North Fixed Arrow
        Canvas(modifier = Modifier.fillMaxSize()) {
            val arrow = Path().apply {
                moveTo(size.width / 2f, 4.dp.toPx())
                lineTo(size.width / 2f - 6.dp.toPx(), 18.dp.toPx())
                lineTo(size.width / 2f, 15.dp.toPx())
                lineTo(size.width / 2f + 6.dp.toPx(), 18.dp.toPx())
                close()
            }
            drawPath(path = arrow, color = NorthRed)
        }

        // Concentric Spirit Level Bubble in Center
        Canvas(modifier = Modifier.size(90.dp)) {
            val center = Offset(size.width / 2f, size.height / 2f)
            val outerR = 42.dp.toPx()
            val targetR = 18.dp.toPx()
            val bubbleR = 10.dp.toPx()

            drawCircle(color = BorderSubtle, radius = outerR, center = center, style = Stroke(1.dp.toPx()))
            val isLevel = kotlin.math.abs(pitch) < 3f && kotlin.math.abs(roll) < 3f
            drawCircle(color = if (isLevel) TargetGreen else BorderStrong, radius = targetR, center = center, style = Stroke(1.dp.toPx()))

            val maxOffset = outerR - bubbleR - 2.dp.toPx()
            val offsetX = (roll.coerceIn(-20f, 20f) / 20f * maxOffset)
            val offsetY = (pitch.coerceIn(-20f, 20f) / 20f * maxOffset)
            drawCircle(color = if (isLevel) TargetGreen else TextMuted, radius = bubbleR, center = Offset(center.x + offsetX, center.y + offsetY), style = Stroke(1.5.dp.toPx()))
        }
    }
}

@Composable
fun ReticleSpiritLevel(pitch: Float, roll: Float) {
    Canvas(modifier = Modifier.size(240.dp)) {
        val center = Offset(size.width / 2f, size.height / 2f)
        drawCircle(color = BorderSubtle, radius = 110.dp.toPx(), center = center, style = Stroke(1.dp.toPx()))
        drawCircle(color = BorderSubtle, radius = 75.dp.toPx(), center = center, style = Stroke(1.dp.toPx()))

        val levelRadius = 32.dp.toPx()
        val isLevel = kotlin.math.abs(pitch) < 3f && kotlin.math.abs(roll) < 3f
        val circleColor = if (isLevel) TargetGreen else BorderStrong

        drawCircle(color = circleColor, radius = levelRadius, center = center, style = Stroke(1.5.dp.toPx()))
        drawLine(color = circleColor, start = Offset(center.x - levelRadius, center.y), end = Offset(center.x + levelRadius, center.y), strokeWidth = 1.dp.toPx())
        drawLine(color = circleColor, start = Offset(center.x, center.y - levelRadius), end = Offset(center.x, center.y + levelRadius), strokeWidth = 1.dp.toPx())

        val bubbleRadius = 12.dp.toPx()
        val maxOffset = levelRadius - bubbleRadius
        val offsetX = (roll.coerceIn(-20f, 20f) / 20f * maxOffset)
        val offsetY = (pitch.coerceIn(-20f, 20f) / 20f * maxOffset)
        drawCircle(color = TargetGreen, radius = bubbleRadius, center = Offset(center.x + offsetX, center.y + offsetY), style = Stroke(1.5.dp.toPx()))
    }
}
