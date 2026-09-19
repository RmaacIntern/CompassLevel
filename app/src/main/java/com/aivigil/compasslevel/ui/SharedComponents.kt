package com.aivigil.compasslevel.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
import kotlin.math.*

@Composable
fun TopAppBar(
    showBack: Boolean = false,
    onBackClick: () -> Unit = {},
    onOverflowClick: () -> Unit = {}
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(52.dp)
            .background(Background)
            .border(width = 1.dp, color = BorderSubtle, shape = RoundedCornerShape(0.dp))
            .padding(horizontal = 14.dp),
        contentAlignment = Alignment.CenterStart
    ) {
        if (showBack) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clickable { onBackClick() },
                contentAlignment = Alignment.Center
            ) {
                Canvas(modifier = Modifier.size(14.dp)) {
                    val p = Path().apply {
                        moveTo(size.width, 0f)
                        lineTo(0f, size.height / 2f)
                        lineTo(size.width, size.height)
                    }
                    drawPath(p, color = TextPrimary, style = Stroke(width = 2.dp.toPx()))
                }
            }
        }

        Text(
            text = "Compass & Level",
            color = TextPrimary,
            fontSize = 18.sp,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.padding(start = if (showBack) 40.dp else 4.dp)
        )

        Box(
            modifier = Modifier
                .size(40.dp)
                .align(Alignment.CenterEnd)
                .clickable { onOverflowClick() },
            contentAlignment = Alignment.Center
        ) {
            Canvas(modifier = Modifier.size(20.dp)) {
                val cx = size.width / 2f
                drawCircle(color = TextSecondary, radius = 2.dp.toPx(), center = Offset(cx, 4.dp.toPx()))
                drawCircle(color = TextSecondary, radius = 2.dp.toPx(), center = Offset(cx, 10.dp.toPx()))
                drawCircle(color = TextSecondary, radius = 2.dp.toPx(), center = Offset(cx, 16.dp.toPx()))
            }
        }
    }
}

@Composable
fun AdBanner(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(48.dp)
            .background(SurfaceLow)
            .border(width = 1.dp, color = BorderSubtle, shape = RoundedCornerShape(0.dp)),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(4.dp)
                    .border(1.dp, BorderStrong, CircleShape)
            )
            Text(
                text = "ADVERTISEMENT · 320×50",
                color = BorderStrong,
                fontSize = 9.sp,
                fontFamily = FontFamily.Monospace,
                letterSpacing = 1.4.sp,
                fontWeight = FontWeight.Normal
            )
            Box(
                modifier = Modifier
                    .size(4.dp)
                    .border(1.dp, BorderStrong, CircleShape)
            )
        }
    }
}

@Composable
fun PillCard(label: String, value: String) {
    Row(
        modifier = Modifier
            .background(SurfaceMid, RoundedCornerShape(50))
            .border(1.dp, BorderSubtle, RoundedCornerShape(50))
            .padding(horizontal = 14.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            color = TextSecondary,
            fontSize = 9.sp,
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.sp
        )
        Box(
            modifier = Modifier
                .padding(horizontal = 8.dp)
                .height(12.dp)
                .width(1.dp)
                .background(BorderSubtle)
        )
        Text(
            text = value,
            color = TextPrimary,
            fontSize = 16.sp,
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Normal
        )
    }
}

@Composable
fun CompassRose(
    heading: Float,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier) {
        val center = Offset(size.width / 2f, size.height / 2f)
        val outerR = size.width / 2f - 4.dp.toPx()
        val innerR = outerR - 14.dp.toPx()

        drawCircle(color = BorderSubtle, radius = outerR, center = center, style = Stroke(1.dp.toPx()))
        drawCircle(color = SurfaceMid, radius = innerR, center = center, style = Stroke(1.dp.toPx()))

        // 12 ticks every 30 degrees (rotating clockwise with negative heading)
        for (i in 0 until 360 step 30) {
            val angleDeg = (i - heading - 90.0)
            val angleRad = Math.toRadians(angleDeg)
            val isMajor = (i % 90 == 0)
            val tickLen = if (isMajor) 14.dp.toPx() else 8.dp.toPx()
            val tickColor = if (isMajor) TextSecondary else BorderStrong
            val strokeW = if (isMajor) 1.5.dp.toPx() else 1.dp.toPx()

            val startX = center.x + (outerR - tickLen) * cos(angleRad).toFloat()
            val startY = center.y + (outerR - tickLen) * sin(angleRad).toFloat()
            val endX = center.x + outerR * cos(angleRad).toFloat()
            val endY = center.y + outerR * sin(angleRad).toFloat()

            drawLine(color = tickColor, start = Offset(startX, startY), end = Offset(endX, endY), strokeWidth = strokeW)
        }

        // Standard clockwise compass order: N (0°), NE (45°), E (90°), SE (135°), S (180°), SW (225°), W (270°), NW (315°)
        val labels = listOf("N", "NE", "E", "SE", "S", "SW", "W", "NW")
        for (i in 0 until 8) {
            val deg = i * 45
            val angleRad = Math.toRadians((deg - heading - 90.0))
            val labelRadius = outerR - 24.dp.toPx()
            val textX = center.x + labelRadius * cos(angleRad).toFloat()
            val textY = center.y + labelRadius * sin(angleRad).toFloat()

            val label = labels[i]
            val paint = android.graphics.Paint().apply {
                textAlign = android.graphics.Paint.Align.CENTER
                isAntiAlias = true
                when (label) {
                    "N" -> {
                        color = android.graphics.Color.parseColor("#FF3B30")
                        textSize = 14.sp.toPx()
                        isFakeBoldText = true
                    }
                    "E", "S", "W" -> {
                        color = android.graphics.Color.WHITE
                        textSize = 12.sp.toPx()
                    }
                    else -> {
                        color = android.graphics.Color.parseColor("#8E8E93")
                        textSize = 10.sp.toPx()
                    }
                }
            }
            drawContext.canvas.nativeCanvas.drawText(label, textX, textY + 4.dp.toPx(), paint)
        }

        // Fixed Top North Arrow
        val arrowPath = Path().apply {
            moveTo(center.x, center.y - outerR + 2.dp.toPx())
            lineTo(center.x - 5.dp.toPx(), center.y - outerR + 12.dp.toPx())
            lineTo(center.x + 5.dp.toPx(), center.y - outerR + 12.dp.toPx())
            close()
        }
        drawPath(arrowPath, color = NorthRed)
        drawLine(
            color = NorthRed,
            start = Offset(center.x, center.y - outerR + 12.dp.toPx()),
            end = Offset(center.x, center.y - outerR + 20.dp.toPx()),
            strokeWidth = 1.5.dp.toPx()
        )
        drawLine(
            color = BorderStrong,
            start = Offset(center.x, center.y + outerR - 8.dp.toPx()),
            end = Offset(center.x, center.y + outerR),
            strokeWidth = 1.5.dp.toPx()
        )
    }
}

@Composable
fun SpiritLevel(
    pitch: Float,
    roll: Float,
    modifier: Modifier = Modifier
) {
    val isLevel = abs(pitch) < 4f && abs(roll) < 4f

    Canvas(modifier = modifier) {
        val center = Offset(size.width / 2f, size.height / 2f)
        val outerR = size.width / 2f
        val innerR = outerR * 0.5f

        // Outer Ring
        drawCircle(color = Color(0x99000000), radius = outerR, center = center)
        drawCircle(color = BorderSubtle, radius = outerR, center = center, style = Stroke(1.dp.toPx()))

        // Inner Target Ring (Green when level, else grey)
        val targetColor = if (isLevel) AccentGreen else BorderStrong
        drawCircle(color = targetColor, radius = innerR, center = center, style = Stroke(1.dp.toPx()))

        // Crosshairs
        drawLine(color = BorderStrong, start = Offset(center.x - innerR, center.y), end = Offset(center.x + innerR, center.y), strokeWidth = 0.5.dp.toPx())
        drawLine(color = BorderStrong, start = Offset(center.x, center.y - innerR), end = Offset(center.x, center.y + innerR), strokeWidth = 0.5.dp.toPx())
        drawCircle(color = BorderStrong, radius = 1.5.dp.toPx(), center = center)

        // Bubble Physics
        val maxDisplacement = innerR * 0.85f
        val clampedRoll = roll.coerceIn(-45f, 45f)
        val clampedPitch = pitch.coerceIn(-45f, 45f)
        val bubbleX = center.x + (clampedRoll / 45f) * maxDisplacement
        val bubbleY = center.y + (clampedPitch / 45f) * maxDisplacement
        val bubbleR = innerR * 0.55f

        val bubbleFill = if (isLevel) AccentGreenBubbleBg else Color(0x1AFFFFFF)
        val bubbleStroke = if (isLevel) AccentGreen else TextSecondary

        drawCircle(color = bubbleFill, radius = bubbleR, center = Offset(bubbleX, bubbleY))
        drawCircle(color = bubbleStroke, radius = bubbleR, center = Offset(bubbleX, bubbleY), style = Stroke(1.dp.toPx()))
    }
}
