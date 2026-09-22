package com.aivigil.compasslevel.ui

import android.graphics.Paint
import android.graphics.Typeface
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aivigil.compasslevel.ui.theme.*
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.sin

// ─────────────────────────────────────────────────────────────────────────────
// TOP BAR: Clean Aeronautical Header
// ─────────────────────────────────────────────────────────────────────────────

@Composable
fun TopActionBar(
    isReliable: Boolean = true,
    onCalibrateClick: () -> Unit = {},
    onSettingsClick: () -> Unit = {}
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(DarkSurface)
            .padding(horizontal = 20.dp, vertical = 14.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(if (isReliable) NeonEmerald else AmberWarning)
            )
            Text(
                text = "COMPASS & LEVEL",
                color = TextPrimary,
                fontSize = 13.sp,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.5.sp
            )
        }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            if (!isReliable) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(GlowAmberSoft)
                        .border(0.5.dp, AmberWarning, RoundedCornerShape(6.dp))
                        .clickable { onCalibrateClick() }
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "CALIBRATE",
                        color = AmberWarning,
                        fontSize = 10.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(CardSurface)
                    .border(1.dp, BorderStrong, RoundedCornerShape(8.dp))
                    .clickable { onSettingsClick() }
                    .padding(horizontal = 10.dp, vertical = 4.dp)
            ) {
                Text(
                    text = "⚙ SETTINGS",
                    color = TextSecondary,
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(1.dp)
            .background(BorderSubtle)
    )
}

// ─────────────────────────────────────────────────────────────────────────────
// COMMERCIAL SEGMENTED SWITCHER (Compass vs Level)
// ─────────────────────────────────────────────────────────────────────────────

@Composable
fun SegmentedModeSelector(
    selectedMode: String,
    onModeSelected: (String) -> Unit
) {
    val modes = listOf("Compass", "Level")

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 10.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(CardSurface)
                .border(1.dp, BorderSubtle, RoundedCornerShape(12.dp))
                .padding(4.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            modes.forEach { mode ->
                val isSelected = mode == selectedMode
                val bg by animateColorAsState(
                    targetValue = if (isSelected) CardSurfaceElevated else Color.Transparent,
                    animationSpec = tween(150),
                    label = "modeBg"
                )
                val textColor by animateColorAsState(
                    targetValue = if (isSelected) TextWhite else TextSecondary,
                    animationSpec = tween(150),
                    label = "modeText"
                )

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(8.dp))
                        .background(bg)
                        .border(
                            width = if (isSelected) 1.dp else 0.dp,
                            color = if (isSelected) BorderStrong else Color.Transparent,
                            shape = RoundedCornerShape(8.dp)
                        )
                        .clickable { onModeSelected(mode) }
                        .padding(vertical = 10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (mode == "Compass") "COMPASS" else "SPIRIT LEVEL",
                        color = textColor,
                        fontSize = 12.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                        letterSpacing = 1.sp
                    )
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// MONETIZATION-READY AD CONTAINER (50dp strict isolation)
// ─────────────────────────────────────────────────────────────────────────────

@Composable
fun AdBannerBottom() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(52.dp)
            .background(BannerBg)
            .border(width = 0.5.dp, color = BannerBorder, shape = RoundedCornerShape(0.dp)),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(3.dp))
                    .border(0.5.dp, BannerText, RoundedCornerShape(3.dp))
                    .padding(horizontal = 4.dp, vertical = 1.dp)
            ) {
                Text(
                    text = "AD",
                    color = BannerText,
                    fontSize = 9.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold
                )
            }
            Text(
                text = "SPONSORED PLACEMENT (320x50)",
                color = BannerText,
                fontSize = 11.sp,
                fontFamily = FontFamily.Monospace,
                letterSpacing = 1.sp
            )
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// PRECISION PILL CARD (Pitch & Roll Decimal Readouts)
// ─────────────────────────────────────────────────────────────────────────────

@Composable
fun PrecisionPillCard(
    label: String,
    valueDegrees: Float
) {
    val absVal = abs(valueDegrees)
    val valueColor = when {
        absVal <= 0.5f -> NeonEmerald
        absVal <= 5.0f -> AmberWarning
        else           -> TextPrimary
    }

    val formattedValue = String.format("%+.1f°", valueDegrees)

    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(16.dp))
            .background(CardSurface)
            .border(1.dp, BorderStrong, RoundedCornerShape(16.dp))
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = label,
            color = TextSecondary,
            fontSize = 11.sp,
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.SemiBold,
            letterSpacing = 1.sp
        )
        Text(
            text = formattedValue,
            color = valueColor,
            fontSize = 15.sp,
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Bold
        )
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// COMPASS ROSE DIAL (60/120 FPS GPU Rendered via graphicsLayer)
// ─────────────────────────────────────────────────────────────────────────────

@Composable
fun CompassRoseDial(
    heading: Float,
    pitch: Float,
    roll: Float,
    isLevel: Boolean,
    modifier: Modifier = Modifier
) {
    val haptic = LocalHapticFeedback.current

    // Trigger haptic tick on exact level snap
    LaunchedEffect(isLevel) {
        if (isLevel) {
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
        }
    }

    // Pre-allocate text paints once outside draw loop to eliminate GC churn
    val textPaint = remember {
        Paint().apply {
            isAntiAlias = true
            textAlign = Paint.Align.CENTER
        }
    }

    val cardinals = remember {
        listOf(
            Triple("N", 0.0, true),
            Triple("NE", 45.0, false),
            Triple("E", 90.0, true),
            Triple("SE", 135.0, false),
            Triple("S", 180.0, true),
            Triple("SW", 225.0, false),
            Triple("W", 270.0, true),
            Triple("NW", 315.0, false)
        )
    }

    // Fixed pre-allocated needle path
    val needlePath = remember { Path() }
    val glowPath = remember { android.graphics.Path() }

    Box(
        modifier = modifier.size(290.dp),
        contentAlignment = Alignment.Center
    ) {
        // ── 1. Outer Machined Bezel Frame (Static, Zero GPU Re-draw) ───────
        Canvas(modifier = Modifier.fillMaxSize()) {
            val center = Offset(size.width / 2f, size.height / 2f)
            val outerRadius = 140.dp.toPx()
            val bezelRadius = 136.dp.toPx()

            // Metallic outer bezel
            drawCircle(color = BorderSubtle, radius = outerRadius, center = center, style = Stroke(1.5.dp.toPx()))
            drawCircle(color = DialBackground, radius = bezelRadius, center = center)
            drawCircle(color = BorderStrong, radius = bezelRadius, center = center, style = Stroke(1.dp.toPx()))
        }

        // ── 2. Rotating Dial Track (GPU Rotated via graphicsLayer Lambda) ──
        Box(
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer {
                    // This rotation happens entirely in the GPU RenderThread with ZERO recomposition!
                    rotationZ = -heading
                }
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val cx = size.width / 2f
                val cy = size.height / 2f
                val center = Offset(cx, cy)
                val trackRadius = 132.dp.toPx()
                val innerTrackRadius = 110.dp.toPx()
                val labelRadius = 94.dp.toPx()

                // Tick ring boundary
                drawCircle(color = BorderSubtle, radius = trackRadius, center = center, style = Stroke(0.5.dp.toPx()))
                drawCircle(color = BorderStrong, radius = innerTrackRadius, center = center, style = Stroke(0.5.dp.toPx()))

                // Draw precision 360° ticks (every 2 degrees, major at 10, super major at 30)
                for (i in 0 until 180) {
                    val deg = i * 2.0
                    val rad = Math.toRadians(deg - 90.0)
                    val isSuperMajor = (i % 15 == 0) // every 30°
                    val isMajor = (i % 5 == 0)      // every 10°

                    val tickLen = when {
                        isSuperMajor -> 14.dp.toPx()
                        isMajor      -> 9.dp.toPx()
                        else         -> 5.dp.toPx()
                    }
                    val tickColor = when {
                        isSuperMajor -> TextPrimary
                        isMajor      -> TextSecondary
                        else         -> BorderStrong
                    }
                    val strokeW = if (isSuperMajor) 1.5.dp.toPx() else 0.75.dp.toPx()

                    val startX = (cx + (trackRadius - tickLen) * cos(rad)).toFloat()
                    val startY = (cy + (trackRadius - tickLen) * sin(rad)).toFloat()
                    val endX = (cx + trackRadius * cos(rad)).toFloat()
                    val endY = (cy + trackRadius * sin(rad)).toFloat()

                    drawLine(
                        color = tickColor,
                        start = Offset(startX, startY),
                        end = Offset(endX, endY),
                        strokeWidth = strokeW
                    )
                }

                // Draw Cardinal labels using pre-allocated textPaint
                cardinals.forEach { (label, deg, isCardinal) ->
                    val rad = Math.toRadians(deg - 90.0)
                    val x = (cx + labelRadius * cos(rad)).toFloat()
                    val y = (cy + labelRadius * sin(rad)).toFloat()

                    textPaint.textSize = if (label == "N") 16.sp.toPx() else if (isCardinal) 13.sp.toPx() else 10.sp.toPx()
                    textPaint.typeface = Typeface.create(Typeface.MONOSPACE, if (isCardinal) Typeface.BOLD else Typeface.NORMAL)
                    textPaint.color = when (label) {
                        "N"  -> android.graphics.Color.parseColor("#FF2A2A")
                        "E", "S", "W" -> android.graphics.Color.WHITE
                        else -> android.graphics.Color.parseColor("#8A909E")
                    }

                    val textY = y - ((textPaint.descent() + textPaint.ascent()) / 2f)
                    drawContext.canvas.nativeCanvas.drawText(label, x, textY, textPaint)
                }
            }
        }

        // ── 3. Static Precision North Index / Lubber Line (Laser Red) ──────
        Canvas(modifier = Modifier.fillMaxSize()) {
            val cx = size.width / 2f

            // Laser glow behind index
            drawIntoCanvas { canvas ->
                val glowPaint = android.graphics.Paint().apply {
                    isAntiAlias = true
                    style = android.graphics.Paint.Style.FILL
                    color = android.graphics.Color.parseColor("#55FF2A2A")
                    maskFilter = android.graphics.BlurMaskFilter(8.dp.toPx(), android.graphics.BlurMaskFilter.Blur.NORMAL)
                }
                glowPath.reset()
                glowPath.moveTo(cx, 4.dp.toPx())
                glowPath.lineTo(cx - 8.dp.toPx(), 22.dp.toPx())
                glowPath.lineTo(cx, 18.dp.toPx())
                glowPath.lineTo(cx + 8.dp.toPx(), 22.dp.toPx())
                glowPath.close()
                canvas.nativeCanvas.drawPath(glowPath, glowPaint)
            }

            needlePath.reset()
            needlePath.moveTo(cx, 4.dp.toPx())
            needlePath.lineTo(cx - 7.dp.toPx(), 20.dp.toPx())
            needlePath.lineTo(cx, 16.dp.toPx())
            needlePath.lineTo(cx + 7.dp.toPx(), 20.dp.toPx())
            needlePath.close()
            drawPath(path = needlePath, color = LaserRed)
        }

        // ── 4. Center 2D Bullseye Spirit Level ─────────────────────────────
        Canvas(modifier = Modifier.size(100.dp)) {
            val center = Offset(size.width / 2f, size.height / 2f)
            val outerRadius = 45.dp.toPx()
            val snapRadius = 18.dp.toPx()
            val bubbleRadius = 11.dp.toPx()

            // Bezel for center level
            drawCircle(color = DialBackground, radius = outerRadius, center = center)
            drawCircle(color = BorderSubtle, radius = outerRadius, center = center, style = Stroke(1.dp.toPx()))

            // Snap boundary target
            val targetColor = if (isLevel) NeonEmerald else BorderStrong
            drawCircle(color = targetColor, radius = snapRadius, center = center, style = Stroke(1.5.dp.toPx()))

            // Crosshairs through snap circle
            drawLine(
                color = targetColor.copy(alpha = 0.5f),
                start = Offset(center.x - snapRadius, center.y),
                end = Offset(center.x + snapRadius, center.y),
                strokeWidth = 0.75.dp.toPx()
            )
            drawLine(
                color = targetColor.copy(alpha = 0.5f),
                start = Offset(center.x, center.y - snapRadius),
                end = Offset(center.x, center.y + snapRadius),
                strokeWidth = 0.75.dp.toPx()
            )

            // Neon emerald halo when perfectly level
            if (isLevel) {
                drawCircle(color = GlowGreen, radius = snapRadius + 6.dp.toPx(), center = center, style = Stroke(6.dp.toPx()))
            }

            // Liquid bubble position with physics clamp
            val maxTravel = outerRadius - bubbleRadius - 2.dp.toPx()
            val offsetX = (roll.coerceIn(-20f, 20f) / 20f * maxTravel)
            val offsetY = (pitch.coerceIn(-20f, 20f) / 20f * maxTravel)
            val bubbleCenter = Offset(center.x + offsetX, center.y + offsetY)

            // Fluid highlight
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        BubbleGlassLight,
                        if (isLevel) BubbleFluidGreen else BubbleGlassMid,
                        BubbleGlassDark
                    ),
                    center = Offset(bubbleCenter.x - 2.dp.toPx(), bubbleCenter.y - 2.dp.toPx()),
                    radius = bubbleRadius
                ),
                radius = bubbleRadius,
                center = bubbleCenter
            )

            // Bubble glass rim
            drawCircle(
                color = if (isLevel) NeonEmerald else TextSecondary,
                radius = bubbleRadius,
                center = bubbleCenter,
                style = Stroke(1.5.dp.toPx())
            )
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// FULL-SCREEN RETICLE SPIRIT LEVEL (Level Mode)
// ─────────────────────────────────────────────────────────────────────────────

@Composable
fun ReticleSpiritLevel(
    pitch: Float,
    roll: Float,
    isLevel: Boolean,
    modifier: Modifier = Modifier
) {
    val haptic = LocalHapticFeedback.current

    LaunchedEffect(isLevel) {
        if (isLevel) {
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
        }
    }

    Canvas(modifier = modifier.size(280.dp)) {
        val center = Offset(size.width / 2f, size.height / 2f)
        val outerRadius = 130.dp.toPx()
        val midRadius = 85.dp.toPx()
        val snapRadius = 32.dp.toPx()
        val bubbleRadius = 14.dp.toPx()

        val snapColor = if (isLevel) NeonEmerald else BorderStrong

        // Concentric precision rings
        drawCircle(color = DialBackground, radius = outerRadius, center = center)
        drawCircle(color = BorderSubtle, radius = outerRadius, center = center, style = Stroke(1.dp.toPx()))
        drawCircle(color = BorderSubtle, radius = midRadius, center = center, style = Stroke(1.dp.toPx()))
        drawCircle(color = snapColor, radius = snapRadius, center = center, style = Stroke(2.dp.toPx()))

        // Full-span precision crosshairs
        drawLine(
            color = BorderStrong,
            start = Offset(center.x - outerRadius, center.y),
            end = Offset(center.x + outerRadius, center.y),
            strokeWidth = 0.75.dp.toPx()
        )
        drawLine(
            color = BorderStrong,
            start = Offset(center.x, center.y - outerRadius),
            end = Offset(center.x, center.y + outerRadius),
            strokeWidth = 0.75.dp.toPx()
        )

        // Center crosshairs
        drawLine(
            color = snapColor,
            start = Offset(center.x - snapRadius, center.y),
            end = Offset(center.x + snapRadius, center.y),
            strokeWidth = 1.dp.toPx()
        )
        drawLine(
            color = snapColor,
            start = Offset(center.x, center.y - snapRadius),
            end = Offset(center.x, center.y + snapRadius),
            strokeWidth = 1.dp.toPx()
        )

        // Emerald glow halo when leveled
        if (isLevel) {
            drawCircle(
                color = GlowGreenSoft,
                radius = snapRadius + 8.dp.toPx(),
                center = center,
                style = Stroke(12.dp.toPx())
            )
        }

        // Angle ticks on outer circumference
        for (i in 0 until 72) {
            val deg = i * 5.0
            val rad = Math.toRadians(deg)
            val isMajor = (i % 9 == 0) // every 45°
            val tickLen = if (isMajor) 14.dp.toPx() else 7.dp.toPx()
            val start = Offset(
                (center.x + (outerRadius - tickLen) * cos(rad)).toFloat(),
                (center.y + (outerRadius - tickLen) * sin(rad)).toFloat()
            )
            val end = Offset(
                (center.x + outerRadius * cos(rad)).toFloat(),
                (center.y + outerRadius * sin(rad)).toFloat()
            )
            drawLine(
                color = if (isMajor) TextSecondary else BorderStrong,
                start = start,
                end = end,
                strokeWidth = if (isMajor) 1.5.dp.toPx() else 0.75.dp.toPx()
            )
        }

        // Fluid bubble position
        val maxTravel = outerRadius - bubbleRadius - 4.dp.toPx()
        val offsetX = (roll.coerceIn(-25f, 25f) / 25f * maxTravel)
        val offsetY = (pitch.coerceIn(-25f, 25f) / 25f * maxTravel)
        val bubbleCenter = Offset(center.x + offsetX, center.y + offsetY)

        // Bubble fill with radial highlight
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(
                    BubbleGlassLight,
                    if (isLevel) BubbleFluidGreen else BubbleGlassMid,
                    BubbleGlassDark
                ),
                center = Offset(bubbleCenter.x - 3.dp.toPx(), bubbleCenter.y - 3.dp.toPx()),
                radius = bubbleRadius
            ),
            radius = bubbleRadius,
            center = bubbleCenter
        )

        drawCircle(
            color = if (isLevel) NeonEmerald else AmberWarning,
            radius = bubbleRadius,
            center = bubbleCenter,
            style = Stroke(2.dp.toPx())
        )
    }
}
