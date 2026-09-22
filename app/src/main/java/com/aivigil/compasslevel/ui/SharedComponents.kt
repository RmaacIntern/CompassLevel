package com.aivigil.compasslevel.ui

import android.graphics.Paint
import android.graphics.Typeface
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aivigil.compasslevel.ui.theme.*
import kotlin.math.*

// ─────────────────────────────────────────────────────────────────────────────
// TOP ACTION BAR
// ─────────────────────────────────────────────────────────────────────────────

@Composable
fun TopActionBar() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(PureBlack)
            .padding(horizontal = 20.dp, vertical = 14.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // App name
        Text(
            text = "COMPASS & LEVEL",
            color = TextWhite,
            fontSize = 13.sp,
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Bold,
            letterSpacing = 2.sp
        )
        // Calibration hint dot
        Box(
            modifier = Modifier
                .size(8.dp)
                .background(TargetGreen, shape = androidx.compose.foundation.shape.CircleShape)
        )
    }
    // Separator line
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(0.5.dp)
            .background(BorderSubtle)
    )
}

// ─────────────────────────────────────────────────────────────────────────────
// STATE SELECTOR BAR  (only Compass / Level tabs in production)
// ─────────────────────────────────────────────────────────────────────────────

@Composable
fun StateSelectorBar(
    selectedTab: String,
    onTabSelected: (String) -> Unit
) {
    val tabs = listOf("Live", "Content", "Loading", "Empty", "Error")

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(DarkSurface)
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        tabs.forEach { tab ->
            val isSelected = tab == selectedTab
            val bg by animateColorAsState(
                targetValue = if (isSelected) CardSurface else Color.Transparent,
                animationSpec = tween(200),
                label = "tabBg"
            )
            val borderColor by animateColorAsState(
                targetValue = if (isSelected) TargetGreen else BorderSubtle,
                animationSpec = tween(200),
                label = "tabBorder"
            )
            val textColor by animateColorAsState(
                targetValue = if (isSelected) TextWhite else TextMuted,
                animationSpec = tween(200),
                label = "tabText"
            )
            Box(
                modifier = Modifier
                    .weight(1f)
                    .background(bg, RoundedCornerShape(6.dp))
                    .border(
                        width = if (isSelected) 1.dp else 0.5.dp,
                        color = borderColor,
                        shape = RoundedCornerShape(6.dp)
                    )
                    .clickable { onTabSelected(tab) }
                    .padding(vertical = 6.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = tab,
                    color = textColor,
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                )
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// AD BANNER
// ─────────────────────────────────────────────────────────────────────────────

@Composable
fun AdBannerBottom() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(50.dp)
            .background(BannerBg)
            .border(width = 0.5.dp, color = BannerBorder, shape = RoundedCornerShape(0.dp)),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "AD BANNER — 320×50",
            color = BannerText,
            fontSize = 11.sp,
            fontFamily = FontFamily.Monospace,
            letterSpacing = 1.sp
        )
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// PILL CARD  (color-coded pitch / roll values)
// ─────────────────────────────────────────────────────────────────────────────

@Composable
fun PillCard(label: String, value: String) {
    val numericValue = value.replace("°", "").toFloatOrNull() ?: 0f
    val valueColor by animateColorAsState(
        targetValue = when {
            abs(numericValue) < 2f  -> TargetGreen
            abs(numericValue) < 10f -> AmberWarning
            else                    -> NorthRed
        },
        animationSpec = tween(300),
        label = "pillColor"
    )

    Row(
        modifier = Modifier
            .background(CardSurface, RoundedCornerShape(20.dp))
            .border(0.5.dp, BorderStrong, RoundedCornerShape(20.dp))
            .padding(horizontal = 16.dp, vertical = 9.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = label,
            color = TextMuted,
            fontSize = 10.sp,
            fontFamily = FontFamily.Monospace,
            letterSpacing = 1.5.sp
        )
        Text(
            text = value,
            color = valueColor,
            fontSize = 15.sp,
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Bold
        )
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// COMPASS ROSE DIAL  (full upgraded version)
// ─────────────────────────────────────────────────────────────────────────────

@Composable
fun CompassRoseDial(
    heading: Float,
    pitch: Float,
    roll: Float,
    modifier: Modifier = Modifier
) {
    val isLevel = abs(pitch) < 3f && abs(roll) < 3f

    Box(modifier = modifier.size(280.dp), contentAlignment = Alignment.Center) {

        // ── Rotating compass rose canvas ───────────────────────────────────
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .rotate(-heading)
        ) {
            val cx = size.width / 2f
            val cy = size.height / 2f
            val center = Offset(cx, cy)
            val outerR = 128.dp.toPx()
            val innerR = 108.dp.toPx()
            val labelR  = outerR - 20.dp.toPx()

            // Outer ring
            drawCircle(
                color = BorderSubtle,
                radius = outerR,
                center = center,
                style = Stroke(1.dp.toPx())
            )
            // Inner ring
            drawCircle(
                color = BorderStrong,
                radius = innerR,
                center = center,
                style = Stroke(0.5.dp.toPx())
            )

            // Tick marks every 5°, major every 30°
            for (i in 0 until 72) {
                val angleDeg = i * 5.0
                val angleRad = Math.toRadians(angleDeg - 90.0)
                val isMajor = (i % 6 == 0)     // every 30°
                val isMid   = (i % 3 == 0)      // every 15°

                val tickLen = when {
                    isMajor -> 14.dp.toPx()
                    isMid   -> 9.dp.toPx()
                    else    -> 5.dp.toPx()
                }
                val tickColor = when {
                    isMajor -> TextMuted
                    isMid   -> BorderStrong
                    else    -> BorderSubtle
                }
                val strokeW = if (isMajor) 1.5.dp.toPx() else 0.75.dp.toPx()

                val start = Offset(
                    x = (cx + (outerR - tickLen) * cos(angleRad)).toFloat(),
                    y = (cy + (outerR - tickLen) * sin(angleRad)).toFloat()
                )
                val end = Offset(
                    x = (cx + outerR * cos(angleRad)).toFloat(),
                    y = (cy + outerR * sin(angleRad)).toFloat()
                )
                drawLine(color = tickColor, start = start, end = end, strokeWidth = strokeW)
            }

            // Cardinal & intercardinal labels
            val cardinals = listOf(
                Triple("N",  0.0,  true),
                Triple("NE", 45.0, false),
                Triple("E",  90.0, true),
                Triple("SE", 135.0, false),
                Triple("S",  180.0, true),
                Triple("SW", 225.0, false),
                Triple("W",  270.0, true),
                Triple("NW", 315.0, false)
            )

            val textPaint = Paint().apply { isAntiAlias = true; textAlign = Paint.Align.CENTER }
            for ((label, deg, isCardinal) in cardinals) {
                val rad = Math.toRadians(deg - 90.0)
                val r = labelR
                val x = (cx + r * cos(rad)).toFloat()
                val y = (cy + r * sin(rad)).toFloat()

                textPaint.textSize = if (label == "N") 14.sp.toPx() else if (isCardinal) 12.sp.toPx() else 9.sp.toPx()
                textPaint.typeface = Typeface.create(Typeface.MONOSPACE, if (label == "N" || isCardinal) Typeface.BOLD else Typeface.NORMAL)

                when (label) {
                    "N"  -> textPaint.color = android.graphics.Color.parseColor("#FF3B30")
                    "E", "S", "W" -> textPaint.color = android.graphics.Color.WHITE
                    else -> textPaint.color = android.graphics.Color.parseColor("#636366")
                }

                val textY = y - ((textPaint.descent() + textPaint.ascent()) / 2f)
                drawContext.canvas.nativeCanvas.drawText(label, x, textY, textPaint)
            }
        }

        // ── Fixed top – North index arrow (does NOT rotate) ───────────────
        Canvas(modifier = Modifier.fillMaxSize()) {
            val cx = size.width / 2f

            // Glow halo behind arrow
            drawIntoCanvas { canvas ->
                val glowPaint = android.graphics.Paint().apply {
                    isAntiAlias = true
                    style = android.graphics.Paint.Style.FILL
                    color = android.graphics.Color.parseColor("#55FF3B30")
                    maskFilter = android.graphics.BlurMaskFilter(
                        12.dp.toPx(),
                        android.graphics.BlurMaskFilter.Blur.NORMAL
                    )
                }
                val arrowPath = android.graphics.Path().apply {
                    moveTo(cx, 2.dp.toPx())
                    lineTo(cx - 7.dp.toPx(), 20.dp.toPx())
                    lineTo(cx, 16.dp.toPx())
                    lineTo(cx + 7.dp.toPx(), 20.dp.toPx())
                    close()
                }
                canvas.nativeCanvas.drawPath(arrowPath, glowPaint)
            }

            // Solid red north needle tip
            val arrowPath = Path().apply {
                moveTo(cx, 2.dp.toPx())
                lineTo(cx - 7.dp.toPx(), 20.dp.toPx())
                lineTo(cx, 16.dp.toPx())
                lineTo(cx + 7.dp.toPx(), 20.dp.toPx())
                close()
            }
            drawPath(path = arrowPath, color = NorthRed)
        }

        // ── Center concentric spirit level bubble ─────────────────────────
        Canvas(modifier = Modifier.size(96.dp)) {
            val center = Offset(size.width / 2f, size.height / 2f)
            val outerR  = 44.dp.toPx()
            val snapR   = 20.dp.toPx()
            val bubbleR = 11.dp.toPx()

            // Outer boundary ring
            drawCircle(
                color = BorderSubtle,
                radius = outerR,
                center = center,
                style = Stroke(1.dp.toPx())
            )

            // Snap ring – green when levelled
            val snapColor = if (isLevel) TargetGreen else BorderStrong
            drawCircle(color = snapColor, radius = snapR, center = center, style = Stroke(1.5.dp.toPx()))

            // Optional green glow around snap ring when levelled
            if (isLevel) {
                drawCircle(color = GlowGreen, radius = snapR + 4.dp.toPx(), center = center, style = Stroke(6.dp.toPx()))
            }

            // Bubble position
            val maxOffset = outerR - bubbleR - 2.dp.toPx()
            val offsetX = (roll.coerceIn(-20f, 20f)  / 20f * maxOffset)
            val offsetY = (pitch.coerceIn(-20f, 20f) / 20f * maxOffset)
            val bubbleCenter = Offset(center.x + offsetX, center.y + offsetY)
            val bubbleColor  = if (isLevel) TargetGreen else TextMuted

            // Glass highlight (radial gradient feel — drawn as concentric fills)
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(BubbleGlassLight, BubbleGlassMid, BubbleGlassDark),
                    center = Offset(bubbleCenter.x - 2.dp.toPx(), bubbleCenter.y - 2.dp.toPx()),
                    radius = bubbleR
                ),
                radius = bubbleR,
                center = bubbleCenter
            )
            // Bubble outline ring
            drawCircle(color = bubbleColor, radius = bubbleR, center = bubbleCenter, style = Stroke(1.5.dp.toPx()))
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// RETICLE SPIRIT LEVEL  (Level-only / fallback screen)
// ─────────────────────────────────────────────────────────────────────────────

@Composable
fun ReticleSpiritLevel(pitch: Float, roll: Float) {
    val isLevel = abs(pitch) < 3f && abs(roll) < 3f

    Canvas(modifier = Modifier.size(260.dp)) {
        val center   = Offset(size.width / 2f, size.height / 2f)
        val outerR   = 118.dp.toPx()
        val midR     = 80.dp.toPx()
        val snapR    = 34.dp.toPx()
        val bubbleR  = 13.dp.toPx()

        val ringColor = if (isLevel) TargetGreen else BorderStrong

        // Concentric rings
        drawCircle(color = BorderSubtle, radius = outerR, center = center, style = Stroke(1.dp.toPx()))
        drawCircle(color = BorderSubtle, radius = midR,   center = center, style = Stroke(1.dp.toPx()))
        drawCircle(color = ringColor,    radius = snapR,  center = center, style = Stroke(1.5.dp.toPx()))

        // Crosshair lines (extend to outer ring)
        drawLine(color = BorderStrong, start = Offset(center.x - outerR, center.y), end = Offset(center.x + outerR, center.y), strokeWidth = 0.5.dp.toPx())
        drawLine(color = BorderStrong, start = Offset(center.x, center.y - outerR), end = Offset(center.x, center.y + outerR), strokeWidth = 0.5.dp.toPx())
        // Inner crosshair through snap ring
        drawLine(color = ringColor, start = Offset(center.x - snapR, center.y), end = Offset(center.x + snapR, center.y), strokeWidth = 1.dp.toPx())
        drawLine(color = ringColor, start = Offset(center.x, center.y - snapR), end = Offset(center.x, center.y + snapR), strokeWidth = 1.dp.toPx())

        // Green glow ring when levelled
        if (isLevel) {
            drawCircle(color = GlowGreenSoft, radius = snapR + 6.dp.toPx(), center = center, style = Stroke(10.dp.toPx()))
        }

        // Bubble position
        val maxOffset = snapR - bubbleR
        val offsetX = (roll.coerceIn(-20f, 20f)  / 20f * maxOffset)
        val offsetY = (pitch.coerceIn(-20f, 20f) / 20f * maxOffset)
        val bubbleCenter = Offset(center.x + offsetX, center.y + offsetY)
        val bubbleColor  = if (isLevel) TargetGreen else AmberWarning

        // Glass bubble fill
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(BubbleGlassLight, BubbleGlassMid, BubbleGlassDark),
                center = Offset(bubbleCenter.x - 3.dp.toPx(), bubbleCenter.y - 3.dp.toPx()),
                radius = bubbleR
            ),
            radius = bubbleR,
            center = bubbleCenter
        )
        // Bubble ring
        drawCircle(color = bubbleColor, radius = bubbleR, center = bubbleCenter, style = Stroke(2.dp.toPx()))

        // Degree tick marks on outer ring (every 10°)
        for (i in 0 until 36) {
            val angleDeg = i * 10.0
            val rad = Math.toRadians(angleDeg - 90.0)
            val isMajor = (i % 9 == 0)
            val tickLen = if (isMajor) 12.dp.toPx() else 6.dp.toPx()
            val start = Offset((center.x + (outerR - tickLen) * cos(rad)).toFloat(), (center.y + (outerR - tickLen) * sin(rad)).toFloat())
            val end   = Offset((center.x + outerR * cos(rad)).toFloat(), (center.y + outerR * sin(rad)).toFloat())
            drawLine(color = if (isMajor) BorderStrong else BorderSubtle, start = start, end = end, strokeWidth = if (isMajor) 1.5.dp.toPx() else 0.75.dp.toPx())
        }
    }
}
