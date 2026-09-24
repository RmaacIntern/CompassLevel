package com.aivigil.compasslevel.ui

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aivigil.compasslevel.sensor.LocationData
import com.aivigil.compasslevel.ui.theme.*
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.max
import kotlin.math.sin

// ─────────────────────────────────────────────────────────────────────────────
// SCREEN 1: LOADING (Precision Sensor Initialization)
// ─────────────────────────────────────────────────────────────────────────────

@Composable
fun ScreenLoadingView() {
    val infiniteTransition = rememberInfiniteTransition(label = "loading")

    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(tween(1200, easing = LinearEasing), RepeatMode.Restart),
        label = "arc_spin"
    )

    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.2f,
        targetValue = 0.5f,
        animationSpec = infiniteRepeatable(tween(1000, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "glow_pulse"
    )

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.size(160.dp)) {
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(GlowGreen.copy(alpha = glowAlpha), Color.Transparent),
                    radius = 80.dp.toPx()
                ),
                radius = 80.dp.toPx()
            )
        }

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Canvas(modifier = Modifier.size(76.dp)) {
                // Background track
                drawArc(
                    color = BorderSubtle,
                    startAngle = 0f,
                    sweepAngle = 360f,
                    useCenter = false,
                    style = Stroke(2.dp.toPx())
                )
                // Neon sweep arc
                drawArc(
                    color = NeonEmerald,
                    startAngle = rotation,
                    sweepAngle = 100f,
                    useCenter = false,
                    style = Stroke(3.dp.toPx(), cap = StrokeCap.Round)
                )
                // Bright leading tip dot
                val leadRad = Math.toRadians((rotation + 100.0))
                val r = 38.dp.toPx()
                drawCircle(
                    color = Color.White,
                    radius = 4.dp.toPx(),
                    center = Offset(
                        x = (size.width / 2 + r * cos(leadRad)).toFloat(),
                        y = (size.height / 2 + r * sin(leadRad)).toFloat()
                    )
                )
            }

            Spacer(modifier = Modifier.height(28.dp))

            Text(
                text = "CALIBRATING SENSORS",
                color = TextPrimary,
                fontSize = 13.sp,
                fontFamily = FontFamily.Default,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "Aligning magnetic & inertial pipeline",
                color = TextSecondary,
                fontSize = 11.sp,
                fontFamily = FontFamily.Default
            )
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// SCREEN 2: LIVE COMPASS (Commercial Precision Instrument)
// ─────────────────────────────────────────────────────────────────────────────

@Composable
fun ScreenLiveCompassView(
    heading: Float,
    pitch: Float,
    roll: Float,
    isLevel: Boolean,
    isBearingLocked: Boolean = false,
    lockedHeading: Float = 0f,
    onBearingLockToggle: () -> Unit = {},
    onSaveNoteClick: (heading: Float, cardinal: String) -> Unit = { _, _ -> },
    skin: SkinPalette,
    locationData: LocationData = LocationData()
) {
    val cardinal = when (heading) {
        in 22.5f..67.5f   -> "NE"
        in 67.5f..112.5f  -> "E"
        in 112.5f..157.5f -> "SE"
        in 157.5f..202.5f -> "S"
        in 202.5f..247.5f -> "SW"
        in 247.5f..292.5f -> "W"
        in 292.5f..337.5f -> "NW"
        else              -> "N"
    }

    val isNorth = cardinal == "N"

    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // ── Top Readout: Heading Number & Cardinal Badge (Google Style) ──
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(top = 10.dp)
        ) {
            val normalizedHeading = ((heading % 360f) + 360f) % 360f
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Text(
                    text = String.format(java.util.Locale.US, "%03d", normalizedHeading.toInt()),
                    fontSize = 48.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isBearingLocked) Color(0xFFFFD700) else skin.textPrimary,
                    fontFamily = FontFamily.Default,
                    letterSpacing = (-0.5).sp
                )
                Text(
                    text = "°",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Normal,
                    color = if (isBearingLocked) Color(0xFFFFD700) else skin.primaryAccent,
                    fontFamily = FontFamily.Default,
                    modifier = Modifier.padding(bottom = 8.dp, start = 2.dp)
                )

                Spacer(modifier = Modifier.width(10.dp))

                // Integrated Cardinal Pill
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (isNorth) skin.primaryAccent.copy(alpha = 0.15f) else skin.cardBackground)
                        .border(
                            width = 1.dp,
                            color = if (isNorth) skin.primaryAccent else skin.cardBorder,
                            shape = RoundedCornerShape(8.dp)
                        )
                        .padding(horizontal = 12.dp, vertical = 5.dp)
                ) {
                    Text(
                        text = cardinal,
                        color = if (isNorth) skin.primaryAccent else skin.textPrimary,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Default,
                        letterSpacing = 1.sp
                    )
                }
            }

            // Bearing Locked Info Banner
            if (isBearingLocked) {
                Spacer(modifier = Modifier.height(6.dp))
                val delta = ((heading - lockedHeading + 540f) % 360f) - 180f
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(Color(0xFFFFD700).copy(alpha = 0.2f))
                            .border(1.dp, Color(0xFFFFD700), RoundedCornerShape(6.dp))
                            .padding(horizontal = 10.dp, vertical = 3.dp)
                    ) {
                        Text(
                            text = "LOCKED ${lockedHeading.toInt()}°",
                            color = Color(0xFFFFD700),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Default
                        )
                    }
                    Text(
                        text = "Δ ${if (delta >= 0) "+" else ""}${delta.toInt()}°",
                        color = skin.textSecondary,
                        fontSize = 12.sp,
                        fontFamily = FontFamily.Default,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            // Live Coordinates readout right on Compass Screen
            val hasCoords = locationData.hasFix || locationData.latitude != 0.0 || locationData.longitude != 0.0
            if (hasCoords) {
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = "📍 ${locationData.latitudeDms}, ${locationData.longitudeDms}",
                        color = skin.primaryAccent,
                        fontSize = 12.sp,
                        fontFamily = FontFamily.Default,
                        fontWeight = FontWeight.Medium
                    )
                    if (locationData.altitudeMeters > 0) {
                        Text(
                            text = "• ${locationData.altitudeMeters.toInt()}m",
                            color = skin.textSecondary,
                            fontSize = 12.sp,
                            fontFamily = FontFamily.Default
                        )
                    }
                }
            }
        }

        // ── Center Dial: 60/120 FPS Luxury Porsche Precision Instrument ────
        CompassRoseDial(
            heading = heading,
            pitch = pitch,
            roll = roll,
            isLevel = isLevel,
            skin = skin
        )

        // ── Bottom Action Row: Integrated Cockpit Telemetry Deck ────────────
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp),
            contentAlignment = Alignment.Center
        ) {
            CockpitTelemetryDeck(
                pitch = pitch,
                roll = roll,
                isBearingLocked = isBearingLocked,
                onBearingLockToggle = onBearingLockToggle,
                onSaveNoteClick = { onSaveNoteClick(heading, cardinal) },
                skin = skin
            )
        }
    }
}

@Composable
fun ScreenLiveCompassView(
    heading: Float,
    pitch: Float,
    roll: Float,
    isLevel: Boolean,
    isBearingLocked: Boolean = false,
    lockedHeading: Float = 0f,
    onBearingLockToggle: () -> Unit = {},
    onSaveNoteClick: (heading: Float, cardinal: String) -> Unit = { _, _ -> },
    skin: AppSkin = AppSkin.CLASSIC_EMERALD,
    locationData: LocationData = LocationData()
) {
    ScreenLiveCompassView(
        heading = heading,
        pitch = pitch,
        roll = roll,
        isLevel = isLevel,
        isBearingLocked = isBearingLocked,
        lockedHeading = lockedHeading,
        onBearingLockToggle = onBearingLockToggle,
        onSaveNoteClick = onSaveNoteClick,
        skin = skin.palette(true),
        locationData = locationData
    )
}

// ─────────────────────────────────────────────────────────────────────────────
// SCREEN 3: FULL LEVEL MODE (Dual-Axis X & Y Spirit Level as in Reference)
// ─────────────────────────────────────────────────────────────────────────────

@Composable
fun ScreenContentView(
    pitch: Float,
    roll: Float,
    isLevel: Boolean,
    isAngleLocked: Boolean = false,
    usePercentGrade: Boolean = false,
    onTareClick: () -> Unit = {},
    onAngleLockToggle: () -> Unit = {},
    onFlashlightToggle: () -> Unit = {},
    isFlashlightOn: Boolean = false,
    onSaveNoteClick: (pitch: Float, roll: Float) -> Unit = { _, _ -> },
    skin: SkinPalette
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // ── Top Bar: Tare Button + Horizontal X-Axis Spirit Tube ─────────────
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Target / Tare Button (Upper Left)
            IconButton(
                onClick = onTareClick,
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(skin.cardBackground)
                    .border(1.dp, if (isLevel) skin.primaryAccent else skin.cardBorder, CircleShape)
            ) {
                Icon(
                    imageVector = Icons.Default.MyLocation,
                    contentDescription = "Tare / Zero Surface",
                    tint = if (isLevel) skin.primaryAccent else skin.textPrimary,
                    modifier = Modifier.size(22.dp)
                )
            }

            // Top Horizontal Spirit Tube (X-Axis)
            Box(
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 12.dp, end = 8.dp)
                    .height(42.dp)
                    .clip(RoundedCornerShape(21.dp))
                    .background(skin.dialBackground)
                    .border(1.5.dp, skin.ringBorder, RoundedCornerShape(21.dp)),
                contentAlignment = Alignment.Center
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val cx = size.width / 2f
                    val cy = size.height / 2f
                    val maxTravel = size.width * 0.38f
                    val bubbleRadius = 13.dp.toPx()

                    // Center hairline
                    drawLine(
                        color = skin.primaryAccent.copy(alpha = 0.5f),
                        start = Offset(cx, 0f),
                        end = Offset(cx, size.height),
                        strokeWidth = 1.dp.toPx()
                    )

                    // Target zone
                    drawCircle(
                        color = skin.primaryAccent.copy(alpha = 0.3f),
                        radius = bubbleRadius + 2.dp.toPx(),
                        center = Offset(cx, cy),
                        style = Stroke(1.dp.toPx())
                    )

                    // Bubble sliding along X (Roll)
                    val xOffset = (roll.coerceIn(-15f, 15f) / 15f) * maxTravel
                    val bubbleCenter = Offset(cx + xOffset, cy)

                    // Glow behind bubble
                    drawCircle(
                        color = skin.bubbleGlow,
                        radius = bubbleRadius + 4.dp.toPx(),
                        center = bubbleCenter
                    )
                    // Bubble body (translucent fluid)
                    drawCircle(
                        color = if (isLevel) skin.primaryAccent else skin.bubbleColor,
                        radius = bubbleRadius,
                        center = bubbleCenter
                    )
                    // Specular gloss highlight reflection
                    drawCircle(
                        color = Color.White.copy(alpha = 0.85f),
                        radius = bubbleRadius * 0.38f,
                        center = Offset(bubbleCenter.x - 2.dp.toPx(), bubbleCenter.y - 2.dp.toPx())
                    )
                    // Bubble rim
                    drawCircle(
                        color = Color.White.copy(alpha = 0.45f),
                        radius = bubbleRadius,
                        center = bubbleCenter,
                        style = Stroke(1.2.dp.toPx())
                    )
                }
            }

            // Readout X Angle
            Text(
                text = "${String.format(java.util.Locale.US, "%.1f", kotlin.math.abs(roll))}°\nX",
                color = skin.textPrimary,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Default,
                textAlign = TextAlign.Center,
                lineHeight = 16.sp
            )
        }

        // ── Main Instruments Area: Left Y-Tube + Center Bullseye ───────────
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .padding(vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Left Column: Vertical Y-Axis Spirit Tube + Y Readout
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Box(
                    modifier = Modifier
                        .width(42.dp)
                        .fillMaxHeight(0.85f)
                        .heightIn(min = 150.dp, max = 240.dp)
                        .clip(RoundedCornerShape(21.dp))
                        .background(skin.dialBackground)
                        .border(1.5.dp, skin.ringBorder, RoundedCornerShape(21.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val cx = size.width / 2f
                        val cy = size.height / 2f
                        val maxTravel = size.height * 0.38f
                        val bubbleRadius = 13.dp.toPx()

                        // Center hairline
                        drawLine(
                            color = skin.primaryAccent.copy(alpha = 0.5f),
                            start = Offset(0f, cy),
                            end = Offset(size.width, cy),
                            strokeWidth = 1.dp.toPx()
                        )

                        // Target zone
                        drawCircle(
                            color = skin.primaryAccent.copy(alpha = 0.3f),
                            radius = bubbleRadius + 2.dp.toPx(),
                            center = Offset(cx, cy),
                            style = Stroke(1.dp.toPx())
                        )

                        // Bubble sliding along Y (Pitch) - floats up when top is lifted
                        val yOffset = (pitch.coerceIn(-15f, 15f) / 15f) * maxTravel
                        val bubbleCenter = Offset(cx, cy - yOffset)

                        // Glow behind bubble
                        drawCircle(
                            color = skin.bubbleGlow,
                            radius = bubbleRadius + 4.dp.toPx(),
                            center = bubbleCenter
                        )
                        // Bubble body (translucent fluid)
                        drawCircle(
                            color = if (isLevel) skin.primaryAccent else skin.bubbleColor,
                            radius = bubbleRadius,
                            center = bubbleCenter
                        )
                        // Specular gloss highlight reflection
                        drawCircle(
                            color = Color.White.copy(alpha = 0.85f),
                            radius = bubbleRadius * 0.38f,
                            center = Offset(bubbleCenter.x - 2.dp.toPx(), bubbleCenter.y - 2.dp.toPx())
                        )
                        // Bubble rim
                        drawCircle(
                            color = Color.White.copy(alpha = 0.45f),
                            radius = bubbleRadius,
                            center = bubbleCenter,
                            style = Stroke(1.2.dp.toPx())
                        )
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = "Y\n${String.format(java.util.Locale.US, "%+.1f", pitch)}°",
                    color = skin.textPrimary,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Default,
                    textAlign = TextAlign.Center,
                    lineHeight = 15.sp
                )
            }

            // Center Circular Bullseye Level with 12 Degree Ticks
            BoxWithConstraints(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight(),
                contentAlignment = Alignment.Center
            ) {
                val bullseyeSize = minOf(maxWidth * 0.96f, maxHeight * 0.96f, 250.dp)
                Canvas(modifier = Modifier.size(bullseyeSize)) {
                    val cx = size.width / 2f
                    val cy = size.height / 2f
                    val outerR = (size.width / 2f) - 6.dp.toPx()
                    val snapR = outerR * 0.28f
                    val bubbleR = outerR * 0.16f
                    val maxTravel = outerR - bubbleR - 4.dp.toPx()

                    // Outer Bezel
                    drawCircle(
                        color = skin.dialOuterBezel,
                        radius = outerR + 4.dp.toPx(),
                        center = Offset(cx, cy)
                    )

                    // Background surface
                    drawCircle(
                        color = skin.dialBackground,
                        radius = outerR,
                        center = Offset(cx, cy)
                    )

                    // Outer Rim
                    drawCircle(
                        color = skin.ringBorder,
                        radius = outerR,
                        center = Offset(cx, cy),
                        style = Stroke(2.dp.toPx())
                    )

                    // 12 Degree Radial Ticks (every 30°)
                    for (i in 0 until 12) {
                        val angleRad = Math.toRadians((i * 30.0))
                        val isQuarter = (i % 3 == 0)
                        val tickLen = if (isQuarter) 16.dp.toPx() else 10.dp.toPx()
                        val startX = cx + (outerR - tickLen) * kotlin.math.cos(angleRad).toFloat()
                        val startY = cy + (outerR - tickLen) * kotlin.math.sin(angleRad).toFloat()
                        val endX = cx + outerR * kotlin.math.cos(angleRad).toFloat()
                        val endY = cy + outerR * kotlin.math.sin(angleRad).toFloat()

                        drawLine(
                            color = if (isQuarter) skin.primaryAccent.copy(alpha = 0.8f) else skin.secondaryAccent.copy(alpha = 0.4f),
                            start = Offset(startX, startY),
                            end = Offset(endX, endY),
                            strokeWidth = if (isQuarter) 1.5.dp.toPx() else 1.dp.toPx()
                        )
                    }

                    // Inner Tolerance Circle
                    drawCircle(
                        color = if (isLevel) skin.primaryAccent else skin.secondaryAccent.copy(alpha = 0.5f),
                        radius = snapR,
                        center = Offset(cx, cy),
                        style = Stroke(1.5.dp.toPx())
                    )

                    // Center Crosshairs
                    drawLine(
                        color = if (isLevel) skin.primaryAccent else skin.secondaryAccent.copy(alpha = 0.4f),
                        start = Offset(cx - snapR * 1.3f, cy),
                        end = Offset(cx + snapR * 1.3f, cy),
                        strokeWidth = 1.dp.toPx()
                    )
                    drawLine(
                        color = if (isLevel) skin.primaryAccent else skin.secondaryAccent.copy(alpha = 0.4f),
                        start = Offset(cx, cy - snapR * 1.3f),
                        end = Offset(cx, cy + snapR * 1.3f),
                        strokeWidth = 1.dp.toPx()
                    )

                    // Level halo if leveled
                    if (isLevel) {
                        drawCircle(
                            color = skin.primaryAccent.copy(alpha = 0.25f),
                            radius = snapR + 8.dp.toPx(),
                            center = Offset(cx, cy)
                        )
                    }

                    // 2D Bubble Motion (Roll -> X, Pitch -> Y) with true circular fluid bounds
                    val rawBxOffset = (roll.coerceIn(-25f, 25f) / 20f) * maxTravel
                    val rawByOffset = (pitch.coerceIn(-25f, 25f) / 20f) * maxTravel
                    val bDist = Math.hypot(rawBxOffset.toDouble(), rawByOffset.toDouble()).toFloat()
                    val bScale = if (bDist > maxTravel) maxTravel / bDist else 1f
                    val bCenter = Offset(cx + rawBxOffset * bScale, cy - rawByOffset * bScale)

                    // Glow behind bubble
                    drawCircle(
                        color = skin.bubbleGlow,
                        radius = bubbleR + 6.dp.toPx(),
                        center = bCenter
                    )
                    // Bubble body (translucent fluid)
                    drawCircle(
                        color = if (isLevel) skin.primaryAccent else skin.bubbleColor,
                        radius = bubbleR,
                        center = bCenter
                    )
                    // Specular gloss highlight reflection
                    drawCircle(
                        color = Color.White.copy(alpha = 0.85f),
                        radius = bubbleR * 0.4f,
                        center = Offset(bCenter.x - 3.dp.toPx(), bCenter.y - 3.dp.toPx())
                    )
                    // Bubble rim
                    drawCircle(
                        color = Color.White.copy(alpha = 0.45f),
                        radius = bubbleR,
                        center = bCenter,
                        style = Stroke(1.5.dp.toPx())
                    )
                }
            }
        }

        // ── Bottom Floating Actions: Lock + Flashlight + Save Note ──────────
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Lock Angle Button
            FloatingActionButton(
                onClick = onAngleLockToggle,
                containerColor = if (isAngleLocked) RedAccent else skin.cardBackground,
                contentColor = if (isAngleLocked) PureBlack else skin.primaryAccent,
                shape = CircleShape,
                modifier = Modifier
                    .size(54.dp)
                    .border(1.dp, if (isAngleLocked) RedAccent else skin.cardBorder, CircleShape)
            ) {
                Icon(
                    imageVector = if (isAngleLocked) Icons.Default.Lock else Icons.Default.LockOpen,
                    contentDescription = "Lock Angles",
                    modifier = Modifier.size(24.dp)
                )
            }

            // Flashlight Toggle Button
            FloatingActionButton(
                onClick = onFlashlightToggle,
                containerColor = if (isFlashlightOn) Color(0xFFFFD700) else skin.cardBackground,
                contentColor = if (isFlashlightOn) PureBlack else skin.textPrimary,
                shape = CircleShape,
                modifier = Modifier
                    .size(54.dp)
                    .border(1.dp, if (isFlashlightOn) Color(0xFFFFD700) else skin.cardBorder, CircleShape)
            ) {
                Icon(
                    imageVector = if (isFlashlightOn) Icons.Default.FlashlightOn else Icons.Default.FlashlightOff,
                    contentDescription = "Flashlight",
                    modifier = Modifier.size(24.dp)
                )
            }

            // Save Measurement to Notes
            FloatingActionButton(
                onClick = { onSaveNoteClick(pitch, roll) },
                containerColor = skin.cardBackground,
                contentColor = skin.primaryAccent,
                shape = CircleShape,
                modifier = Modifier
                    .size(54.dp)
                    .border(1.dp, skin.cardBorder, CircleShape)
            ) {
                Icon(
                    imageVector = Icons.Default.BookmarkAdd,
                    contentDescription = "Save Note",
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}

@Composable
fun ScreenContentView(
    pitch: Float,
    roll: Float,
    isLevel: Boolean,
    isAngleLocked: Boolean = false,
    usePercentGrade: Boolean = false,
    onTareClick: () -> Unit = {},
    onAngleLockToggle: () -> Unit = {},
    onFlashlightToggle: () -> Unit = {},
    isFlashlightOn: Boolean = false,
    onSaveNoteClick: (pitch: Float, roll: Float) -> Unit = { _, _ -> },
    skin: AppSkin = AppSkin.CLASSIC_EMERALD
) {
    ScreenContentView(
        pitch = pitch,
        roll = roll,
        isLevel = isLevel,
        isAngleLocked = isAngleLocked,
        usePercentGrade = usePercentGrade,
        onTareClick = onTareClick,
        onAngleLockToggle = onAngleLockToggle,
        onFlashlightToggle = onFlashlightToggle,
        isFlashlightOn = isFlashlightOn,
        onSaveNoteClick = onSaveNoteClick,
        skin = skin.palette(true)
    )
}

// ─────────────────────────────────────────────────────────────────────────────
// SCREEN 4: EMPTY SENSOR STATE
// ─────────────────────────────────────────────────────────────────────────────

@Composable
fun ScreenEmptyView() {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(72.dp)
                .clip(RoundedCornerShape(18.dp))
                .background(CardSurface)
                .border(1.dp, BorderStrong, RoundedCornerShape(18.dp)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "IDLE",
                color = TextSecondary,
                fontSize = 14.sp,
                fontFamily = FontFamily.Default,
                fontWeight = FontWeight.Bold
            )
        }
        Spacer(modifier = Modifier.height(20.dp))
        Text(
            text = "AWAITING SENSOR MOTION",
            color = TextPrimary,
            fontSize = 13.sp,
            fontFamily = FontFamily.Default,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.sp
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Pick up or rotate your device to initiate reading",
            color = TextMuted,
            fontSize = 12.sp,
            textAlign = TextAlign.Center,
            fontFamily = FontFamily.Default
        )
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// SCREEN 5: SENSOR CALIBRATION MODAL (Nothing-Style Smooth Lemniscate Visualizer)
// ─────────────────────────────────────────────────────────────────────────────

@Composable
fun ScreenErrorView(
    skin: SkinPalette,
    onDismiss: () -> Unit = {}
) {
    val infiniteTransition = rememberInfiniteTransition(label = "tuning_pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.08f,
        animationSpec = infiniteRepeatable(tween(1400, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "pulseScale"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier.size(90.dp),
            contentAlignment = Alignment.Center
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                drawCircle(
                    color = skin.bubbleGlow,
                    radius = (40.dp * pulseScale).toPx(),
                    style = Stroke(6.dp.toPx())
                )
            }

            Box(
                modifier = Modifier
                    .size(54.dp)
                    .clip(CircleShape)
                    .background(skin.cardBackground)
                    .border(1.5.dp, skin.primaryAccent, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Sync,
                    contentDescription = "Optimize",
                    tint = skin.primaryAccent,
                    modifier = Modifier.size(26.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        Text(
            text = "FIELD OPTIMIZATION",
            color = skin.textPrimary,
            fontSize = 15.sp,
            fontFamily = FontFamily.Default,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.2.sp
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Geomagnetic variance detected in local environment.\nWave your device in a gentle figure-8 motion to optimize.",
            color = skin.textSecondary,
            fontSize = 12.sp,
            textAlign = TextAlign.Center,
            lineHeight = 18.sp,
            fontFamily = FontFamily.Default
        )

        Spacer(modifier = Modifier.height(24.dp))

        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(12.dp))
                .background(skin.cardBackground)
                .border(1.dp, skin.primaryAccent, RoundedCornerShape(12.dp))
                .clickable { onDismiss() }
                .padding(horizontal = 24.dp, vertical = 11.dp)
        ) {
            Text(
                text = "CONTINUE",
                color = skin.primaryAccent,
                fontSize = 12.sp,
                fontFamily = FontFamily.Default,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.5.sp
            )
        }
    }
}

@Composable
fun ScreenErrorView(
    skin: AppSkin = AppSkin.CLASSIC_EMERALD,
    onDismiss: () -> Unit = {}
) {
    ScreenErrorView(
        skin = skin.palette(true),
        onDismiss = onDismiss
    )
}

@Composable
fun CalibrationDialog(
    skin: SkinPalette,
    onDismiss: () -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "figure8_anim")
    val phase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = (2 * Math.PI).toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 3600, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "phase"
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .clip(CircleShape)
                        .background(skin.primaryAccent)
                )
                Text(
                    text = "SENSOR CALIBRATION",
                    color = skin.textPrimary,
                    fontSize = 15.sp,
                    fontFamily = FontFamily.Default,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.8.sp
                )
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Rotate your phone smoothly in a continuous figure-8 motion to balance the 3-axis magnetometer.",
                    color = skin.textPrimary,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    lineHeight = 18.sp
                )

                // Animated Lemniscate (Figure-8) Canvas
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(95.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(skin.dialBackground)
                        .border(1.dp, skin.cardBorder, RoundedCornerShape(14.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val cx = size.width / 2f
                        val cy = size.height / 2f
                        val a = size.width * 0.36f
                        val b = size.height * 0.32f

                        // Pre-calculate and draw figure-8 track
                        val trackPath = Path()
                        val steps = 80
                        for (i in 0..steps) {
                            val t = (i.toFloat() / steps) * 2 * Math.PI
                            val x = cx + a * sin(t).toFloat()
                            val y = cy + b * (sin(t) * cos(t)).toFloat()
                            if (i == 0) trackPath.moveTo(x, y) else trackPath.lineTo(x, y)
                        }
                        trackPath.close()

                        // Draw background track
                        drawPath(
                            path = trackPath,
                            color = skin.cardBorder.copy(alpha = 0.8f),
                            style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round)
                        )

                        // Animated orb current coordinates
                        val orbX = cx + a * sin(phase.toDouble()).toFloat()
                        val orbY = cy + b * (sin(phase.toDouble()) * cos(phase.toDouble())).toFloat()

                        // Orb soft glow
                        drawCircle(
                            color = skin.bubbleGlow,
                            radius = 12.dp.toPx(),
                            center = Offset(orbX, orbY)
                        )

                        // Orb solid core
                        drawCircle(
                            color = skin.primaryAccent,
                            radius = 6.dp.toPx(),
                            center = Offset(orbX, orbY)
                        )

                        // Orb specular highlight
                        drawCircle(
                            color = Color.White,
                            radius = 2.dp.toPx(),
                            center = Offset(orbX - 1.5.dp.toPx(), orbY - 1.5.dp.toPx())
                        )
                    }
                }

                // Reassuring status badge
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(skin.cardBackground)
                        .border(1.dp, skin.cardBorder, RoundedCornerShape(8.dp))
                        .padding(horizontal = 10.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .clip(CircleShape)
                            .background(skin.primaryAccent)
                    )
                    Text(
                        text = "HARDWARE HEALTHY • FUSION READY",
                        color = skin.textSecondary,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Default,
                        letterSpacing = 0.5.sp
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(containerColor = skin.primaryAccent),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.padding(horizontal = 4.dp)
            ) {
                Text(
                    text = "DONE",
                    color = if (skin.isDark) Color.Black else Color.White,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Default,
                    letterSpacing = 0.5.sp
                )
            }
        },
        containerColor = skin.cardElevated,
        shape = RoundedCornerShape(22.dp)
    )
}

@Composable
fun CalibrationDialog(
    skin: AppSkin = AppSkin.CLASSIC_EMERALD,
    onDismiss: () -> Unit
) {
    CalibrationDialog(
        skin = skin.palette(true),
        onDismiss = onDismiss
    )
}


// ─────────────────────────────────────────────────────────────────────────────
// SCREEN 6: SETTINGS (Material 3 Bottom Sheet)
// ─────────────────────────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsBottomSheet(
    isTrueNorth: Boolean,
    declination: Float,
    usePercentGrade: Boolean,
    skin: SkinPalette,
    isDarkMode: Boolean = false,
    onThemeToggle: (Boolean) -> Unit = {},
    onTrueNorthToggle: (Boolean) -> Unit,
    onDeclinationChange: (Float) -> Unit,
    onPercentGradeToggle: (Boolean) -> Unit,
    onCalibrateClick: () -> Unit = {},
    onResetTare: () -> Unit = {},
    onClose: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onClose,
        sheetState = sheetState,
        containerColor = skin.surfaceBackground,
        dragHandle = { BottomSheetDefaults.DragHandle(color = skin.cardBorder) },
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 8.dp)
                .padding(bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "SETTINGS & CALIBRATION",
                    color = skin.textPrimary,
                    fontSize = 17.sp,
                    fontFamily = FontFamily.Default,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.5.sp
                )
                IconButton(
                    onClick = onClose,
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(skin.cardBackground)
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close",
                        tint = skin.textPrimary,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            // Card 0: Appearance / Theme (Light vs Dark)
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(skin.cardBackground)
                    .border(1.dp, skin.cardBorder, RoundedCornerShape(14.dp))
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "APPEARANCE",
                    color = skin.textSecondary,
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Default,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (!isDarkMode) skin.primaryAccent.copy(alpha = 0.15f) else Color.Transparent)
                            .border(1.dp, if (!isDarkMode) skin.primaryAccent else skin.cardBorder, RoundedCornerShape(10.dp))
                            .clickable { onThemeToggle(false) }
                            .padding(vertical = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.LightMode,
                                contentDescription = null,
                                tint = if (!isDarkMode) skin.primaryAccent else skin.textSecondary,
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = "LIGHT",
                                color = if (!isDarkMode) skin.primaryAccent else skin.textSecondary,
                                fontSize = 12.sp,
                                fontFamily = FontFamily.Default,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (isDarkMode) skin.primaryAccent.copy(alpha = 0.15f) else Color.Transparent)
                            .border(1.dp, if (isDarkMode) skin.primaryAccent else skin.cardBorder, RoundedCornerShape(10.dp))
                            .clickable { onThemeToggle(true) }
                            .padding(vertical = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.DarkMode,
                                contentDescription = null,
                                tint = if (isDarkMode) skin.primaryAccent else skin.textSecondary,
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = "DARK",
                                color = if (isDarkMode) skin.primaryAccent else skin.textSecondary,
                                fontSize = 12.sp,
                                fontFamily = FontFamily.Default,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            // Card 1: North Reference
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(skin.cardBackground)
                    .border(1.dp, skin.cardBorder, RoundedCornerShape(14.dp))
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "NORTH REFERENCE",
                    color = skin.textSecondary,
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Default,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (!isTrueNorth) skin.primaryAccent.copy(alpha = 0.15f) else Color.Transparent)
                            .border(1.dp, if (!isTrueNorth) skin.primaryAccent else skin.cardBorder, RoundedCornerShape(10.dp))
                            .clickable { onTrueNorthToggle(false) }
                            .padding(vertical = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "MAGNETIC",
                            color = if (!isTrueNorth) skin.primaryAccent else skin.textSecondary,
                            fontSize = 12.sp,
                            fontFamily = FontFamily.Default,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (isTrueNorth) skin.primaryAccent.copy(alpha = 0.15f) else Color.Transparent)
                            .border(1.dp, if (isTrueNorth) skin.primaryAccent else skin.cardBorder, RoundedCornerShape(10.dp))
                            .clickable { onTrueNorthToggle(true) }
                            .padding(vertical = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "TRUE NORTH",
                            color = if (isTrueNorth) skin.primaryAccent else skin.textSecondary,
                            fontSize = 12.sp,
                            fontFamily = FontFamily.Default,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                if (isTrueNorth) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Declination: ${if (declination >= 0) "+" else ""}${declination.toInt()}°",
                            color = skin.textPrimary,
                            fontSize = 12.sp,
                            fontFamily = FontFamily.Default
                        )
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(skin.cardElevated)
                                    .border(1.dp, skin.cardBorder, RoundedCornerShape(6.dp))
                                    .clickable { onDeclinationChange(declination - 1f) }
                                    .padding(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                Text(text = "-1°", color = skin.textPrimary, fontSize = 12.sp, fontFamily = FontFamily.Default)
                            }
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(skin.cardElevated)
                                    .border(1.dp, skin.cardBorder, RoundedCornerShape(6.dp))
                                    .clickable { onDeclinationChange(declination + 1f) }
                                    .padding(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                Text(text = "+1°", color = skin.textPrimary, fontSize = 12.sp, fontFamily = FontFamily.Default)
                            }
                        }
                    }
                }
            }

            // Card 2: Angle Units
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(skin.cardBackground)
                    .border(1.dp, skin.cardBorder, RoundedCornerShape(14.dp))
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "LEVEL ANGLE UNITS",
                    color = skin.textSecondary,
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Default,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (!usePercentGrade) skin.primaryAccent.copy(alpha = 0.15f) else Color.Transparent)
                            .border(1.dp, if (!usePercentGrade) skin.primaryAccent else skin.cardBorder, RoundedCornerShape(10.dp))
                            .clickable { onPercentGradeToggle(false) }
                            .padding(vertical = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "DEGREES (°)",
                            color = if (!usePercentGrade) skin.primaryAccent else skin.textSecondary,
                            fontSize = 12.sp,
                            fontFamily = FontFamily.Default,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (usePercentGrade) skin.primaryAccent.copy(alpha = 0.15f) else Color.Transparent)
                            .border(1.dp, if (usePercentGrade) skin.primaryAccent else skin.cardBorder, RoundedCornerShape(10.dp))
                            .clickable { onPercentGradeToggle(true) }
                            .padding(vertical = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "% GRADE",
                            color = if (usePercentGrade) skin.primaryAccent else skin.textSecondary,
                            fontSize = 12.sp,
                            fontFamily = FontFamily.Default,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            // Card 3: Calibration & Hardware Diagnostics (For future calibration)
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(skin.cardBackground)
                    .border(1.dp, skin.cardBorder, RoundedCornerShape(14.dp))
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = "CALIBRATION & SENSORS",
                    color = skin.textSecondary,
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Default,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )

                // Recalibrate Magnetometer Action
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(skin.primaryAccent.copy(alpha = 0.12f))
                        .border(1.dp, skin.primaryAccent.copy(alpha = 0.35f), RoundedCornerShape(10.dp))
                        .clickable {
                            onClose()
                            onCalibrateClick()
                        }
                        .padding(horizontal = 14.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Sync,
                            contentDescription = "Calibrate",
                            tint = skin.primaryAccent,
                            modifier = Modifier.size(20.dp)
                        )
                        Column {
                            Text(
                                text = "Calibrate Magnetometer",
                                color = skin.textPrimary,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Default
                            )
                            Text(
                                text = "Run figure-8 motion to eliminate magnetic distortion",
                                color = skin.textSecondary,
                                fontSize = 11.sp,
                                fontFamily = FontFamily.Default
                            )
                        }
                    }
                    Icon(
                        imageVector = Icons.Default.ChevronRight,
                        contentDescription = null,
                        tint = skin.primaryAccent,
                        modifier = Modifier.size(18.dp)
                    )
                }

                // Reset Zero Level / Tare Action
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(skin.cardElevated)
                        .border(1.dp, skin.cardBorder, RoundedCornerShape(10.dp))
                        .clickable { onResetTare() }
                        .padding(horizontal = 14.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.MyLocation,
                            contentDescription = "Tare",
                            tint = skin.textPrimary,
                            modifier = Modifier.size(20.dp)
                        )
                        Column {
                            Text(
                                text = "Reset Zero Level / Tare",
                                color = skin.textPrimary,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Default
                            )
                            Text(
                                text = "Clear camera bump surface offset",
                                color = skin.textSecondary,
                                fontSize = 11.sp,
                                fontFamily = FontFamily.Default
                            )
                        }
                    }
                    Text(
                        text = "RESET",
                        color = skin.primaryAccent,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Default
                    )
                }
            }

            // Card 4: About (Basic Info Only)
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(skin.cardBackground)
                    .border(1.dp, skin.cardBorder, RoundedCornerShape(14.dp))
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    text = "ABOUT",
                    color = skin.textSecondary,
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Default,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
                Text(
                    text = "Compass & Level",
                    color = skin.textPrimary,
                    fontSize = 14.sp,
                    fontFamily = FontFamily.Default,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = "Version 1.2.0 • Offline Precision Tool",
                    color = skin.textSecondary,
                    fontSize = 12.sp,
                    fontFamily = FontFamily.Default
                )
                Text(
                    text = "Sensors: Rotation Vector, Accelerometer, Magnetometer, GPS.\nTelemetry is processed entirely on-device.",
                    color = skin.textSecondary.copy(alpha = 0.7f),
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Default,
                    lineHeight = 16.sp
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsBottomSheet(
    isTrueNorth: Boolean,
    declination: Float,
    usePercentGrade: Boolean,
    skin: AppSkin = AppSkin.CLASSIC_EMERALD,
    onTrueNorthToggle: (Boolean) -> Unit,
    onDeclinationChange: (Float) -> Unit,
    onPercentGradeToggle: (Boolean) -> Unit,
    onCalibrateClick: () -> Unit = {},
    onResetTare: () -> Unit = {},
    onClose: () -> Unit
) {
    SettingsBottomSheet(
        isTrueNorth = isTrueNorth,
        declination = declination,
        usePercentGrade = usePercentGrade,
        skin = skin.palette(true),
        isDarkMode = true,
        onThemeToggle = {},
        onTrueNorthToggle = onTrueNorthToggle,
        onDeclinationChange = onDeclinationChange,
        onPercentGradeToggle = onPercentGradeToggle,
        onCalibrateClick = onCalibrateClick,
        onResetTare = onResetTare,
        onClose = onClose
    )
}

@Composable
fun ScreenSettingsView(
    isTrueNorth: Boolean,
    declination: Float,
    usePercentGrade: Boolean,
    skin: SkinPalette,
    onTrueNorthToggle: (Boolean) -> Unit,
    onDeclinationChange: (Float) -> Unit,
    onPercentGradeToggle: (Boolean) -> Unit,
    onCalibrateClick: () -> Unit = {},
    onResetTare: () -> Unit = {},
    onClose: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(skin.appBackground)
            .padding(horizontal = 20.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "SETTINGS & CALIBRATION",
                    color = skin.textPrimary,
                    fontSize = 18.sp,
                    fontFamily = FontFamily.Default,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(skin.cardBackground)
                        .border(1.dp, skin.cardBorder, RoundedCornerShape(8.dp))
                        .clickable { onClose() }
                        .padding(horizontal = 14.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = "DONE",
                        color = skin.primaryAccent,
                        fontSize = 12.sp,
                        fontFamily = FontFamily.Default,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // Card 1: North Reference (Magnetic vs True North)
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(skin.cardBackground)
                    .border(1.dp, skin.cardBorder, RoundedCornerShape(12.dp))
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "NORTH REFERENCE",
                    color = skin.textSecondary,
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Default,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (!isTrueNorth) skin.cardElevated else Color.Transparent)
                            .border(1.dp, if (!isTrueNorth) skin.primaryAccent else skin.cardBorder, RoundedCornerShape(8.dp))
                            .clickable { onTrueNorthToggle(false) }
                            .padding(vertical = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "MAGNETIC",
                            color = if (!isTrueNorth) skin.primaryAccent else skin.textSecondary,
                            fontSize = 12.sp,
                            fontFamily = FontFamily.Default,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (isTrueNorth) skin.cardElevated else Color.Transparent)
                            .border(1.dp, if (isTrueNorth) skin.primaryAccent else skin.cardBorder, RoundedCornerShape(8.dp))
                            .clickable { onTrueNorthToggle(true) }
                            .padding(vertical = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "TRUE NORTH",
                            color = if (isTrueNorth) skin.primaryAccent else skin.textSecondary,
                            fontSize = 12.sp,
                            fontFamily = FontFamily.Default,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                if (isTrueNorth) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Declination: ${if (declination >= 0) "+" else ""}${declination.toInt()}°",
                            color = skin.textPrimary,
                            fontSize = 12.sp,
                            fontFamily = FontFamily.Default
                        )
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(skin.cardElevated)
                                    .border(1.dp, skin.cardBorder, RoundedCornerShape(6.dp))
                                    .clickable { onDeclinationChange(declination - 1f) }
                                    .padding(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                Text(text = "-1°", color = skin.textPrimary, fontSize = 12.sp, fontFamily = FontFamily.Default)
                            }
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(skin.cardElevated)
                                    .border(1.dp, skin.cardBorder, RoundedCornerShape(6.dp))
                                    .clickable { onDeclinationChange(declination + 1f) }
                                    .padding(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                Text(text = "+1°", color = skin.textPrimary, fontSize = 12.sp, fontFamily = FontFamily.Default)
                            }
                        }
                    }
                }
            }

            // Card 2: Angle Units (Degrees vs % Grade)
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(skin.cardBackground)
                    .border(1.dp, skin.cardBorder, RoundedCornerShape(12.dp))
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "LEVEL ANGLE UNITS",
                    color = skin.textSecondary,
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Default,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (!usePercentGrade) skin.cardElevated else Color.Transparent)
                            .border(1.dp, if (!usePercentGrade) skin.primaryAccent else skin.cardBorder, RoundedCornerShape(8.dp))
                            .clickable { onPercentGradeToggle(false) }
                            .padding(vertical = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "DEGREES (°)",
                            color = if (!usePercentGrade) skin.primaryAccent else skin.textSecondary,
                            fontSize = 12.sp,
                            fontFamily = FontFamily.Default,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (usePercentGrade) skin.cardElevated else Color.Transparent)
                            .border(1.dp, if (usePercentGrade) skin.primaryAccent else skin.cardBorder, RoundedCornerShape(8.dp))
                            .clickable { onPercentGradeToggle(true) }
                            .padding(vertical = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "% GRADE",
                            color = if (usePercentGrade) skin.primaryAccent else skin.textSecondary,
                            fontSize = 12.sp,
                            fontFamily = FontFamily.Default,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            // Card 3: Calibration & Hardware Diagnostics
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(skin.cardBackground)
                    .border(1.dp, skin.cardBorder, RoundedCornerShape(12.dp))
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = "CALIBRATION & SENSORS",
                    color = skin.textSecondary,
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Default,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )

                // Recalibrate Magnetometer Action
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(skin.primaryAccent.copy(alpha = 0.12f))
                        .border(1.dp, skin.primaryAccent.copy(alpha = 0.35f), RoundedCornerShape(8.dp))
                        .clickable {
                            onClose()
                            onCalibrateClick()
                        }
                        .padding(horizontal = 14.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Sync,
                            contentDescription = "Calibrate",
                            tint = skin.primaryAccent,
                            modifier = Modifier.size(20.dp)
                        )
                        Column {
                            Text(
                                text = "Calibrate Magnetometer",
                                color = skin.textPrimary,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Default
                            )
                            Text(
                                text = "Run figure-8 motion to eliminate magnetic distortion",
                                color = skin.textSecondary,
                                fontSize = 11.sp,
                                fontFamily = FontFamily.Default
                            )
                        }
                    }
                    Icon(
                        imageVector = Icons.Default.ChevronRight,
                        contentDescription = null,
                        tint = skin.primaryAccent,
                        modifier = Modifier.size(18.dp)
                    )
                }

                // Reset Tare / Zero Level
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(skin.cardElevated)
                        .border(1.dp, skin.cardBorder, RoundedCornerShape(8.dp))
                        .clickable { onResetTare() }
                        .padding(horizontal = 14.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.MyLocation,
                            contentDescription = "Tare",
                            tint = skin.textPrimary,
                            modifier = Modifier.size(20.dp)
                        )
                        Column {
                            Text(
                                text = "Reset Zero Level / Tare",
                                color = skin.textPrimary,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Default
                            )
                            Text(
                                text = "Clear camera bump surface offset",
                                color = skin.textSecondary,
                                fontSize = 11.sp,
                                fontFamily = FontFamily.Default
                            )
                        }
                    }
                    Text(
                        text = "RESET",
                        color = skin.primaryAccent,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Default
                    )
                }
            }

            // Card 4: About & Verification
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(skin.cardBackground)
                    .border(1.dp, skin.cardBorder, RoundedCornerShape(12.dp))
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    text = "ABOUT",
                    color = skin.textSecondary,
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Default,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
                Text(
                    text = "Compass & Level",
                    color = skin.textPrimary,
                    fontSize = 14.sp,
                    fontFamily = FontFamily.Default,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = "Version 1.2.0 • Offline Precision Tool",
                    color = skin.textSecondary,
                    fontSize = 12.sp,
                    fontFamily = FontFamily.Default
                )
                Text(
                    text = "Sensors: Rotation Vector, Accelerometer, Magnetometer, GPS.\nTelemetry is processed entirely on-device.",
                    color = skin.textSecondary.copy(alpha = 0.7f),
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Default,
                    lineHeight = 16.sp
                )
            }
        }
    }
}

@Composable
fun ScreenSettingsView(
    isTrueNorth: Boolean,
    declination: Float,
    usePercentGrade: Boolean,
    skin: AppSkin = AppSkin.CLASSIC_EMERALD,
    onTrueNorthToggle: (Boolean) -> Unit,
    onDeclinationChange: (Float) -> Unit,
    onPercentGradeToggle: (Boolean) -> Unit,
    onCalibrateClick: () -> Unit = {},
    onResetTare: () -> Unit = {},
    onClose: () -> Unit
) {
    ScreenSettingsView(
        isTrueNorth = isTrueNorth,
        declination = declination,
        usePercentGrade = usePercentGrade,
        skin = skin.palette(true),
        onTrueNorthToggle = onTrueNorthToggle,
        onDeclinationChange = onDeclinationChange,
        onPercentGradeToggle = onPercentGradeToggle,
        onCalibrateClick = onCalibrateClick,
        onResetTare = onResetTare,
        onClose = onClose
    )
}
