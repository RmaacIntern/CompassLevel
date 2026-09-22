package com.aivigil.compasslevel.ui

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aivigil.compasslevel.ui.theme.*
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.sin

// ─────────────────────────────────────────────────────────────────────────────
// SCREEN 1 — LOADING
// ─────────────────────────────────────────────────────────────────────────────

@Composable
fun ScreenLoadingView() {
    val infiniteTransition = rememberInfiniteTransition(label = "loading")

    // Spinning arc
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(tween(1200, easing = LinearEasing), RepeatMode.Restart),
        label = "arc_spin"
    )

    // Pulsing glow alpha
    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.15f,
        targetValue = 0.45f,
        animationSpec = infiniteRepeatable(tween(900, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "glow_pulse"
    )

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        // Radial glow behind spinner
        Canvas(modifier = Modifier.size(140.dp)) {
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        GlowGreen.copy(alpha = glowAlpha),
                        Color.Transparent
                    )
                ),
                radius = 70.dp.toPx()
            )
        }

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Spinning arc ring
            Canvas(modifier = Modifier.size(72.dp)) {
                // Background track
                drawArc(
                    color = BorderSubtle,
                    startAngle = 0f,
                    sweepAngle = 360f,
                    useCenter = false,
                    style = Stroke(2.dp.toPx(), cap = StrokeCap.Round)
                )
                // Green sweep arc
                drawArc(
                    color = TargetGreen,
                    startAngle = rotation,
                    sweepAngle = 90f,
                    useCenter = false,
                    style = Stroke(2.5.dp.toPx(), cap = StrokeCap.Round)
                )
                // Brighter leading dot
                val leadRad = Math.toRadians((rotation + 90.0))
                val r = 36.dp.toPx()
                drawCircle(
                    color = TargetGreen,
                    radius = 3.5.dp.toPx(),
                    center = Offset(
                        x = (size.width / 2 + r * cos(leadRad)).toFloat(),
                        y = (size.height / 2 + r * sin(leadRad)).toFloat()
                    )
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            Text(
                text = "COMPASS & LEVEL",
                color = TextWhite,
                fontSize = 14.sp,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold,
                letterSpacing = 3.sp
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "INITIALIZING SENSORS",
                color = TextMuted,
                fontSize = 11.sp,
                fontFamily = FontFamily.Monospace,
                letterSpacing = 1.5.sp
            )
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// SCREEN 2 — LIVE COMPASS (main experience)
// ─────────────────────────────────────────────────────────────────────────────

@Composable
fun ScreenLiveCompassView(heading: Float, pitch: Float, roll: Float) {
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
    val cardinalBadgeBg = if (isNorth) GlowRed else CardSurface
    val cardinalTextColor = if (isNorth) NorthRed else TextWhite

    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // ── Top: Heading readout ───────────────────────────────────────────
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(top = 20.dp)
        ) {
            Text(
                text = "${heading.toInt()}°",
                fontSize = 80.sp,
                fontWeight = FontWeight.Bold,
                color = TextWhite,
                fontFamily = FontFamily.Monospace
            )

            // Cardinal direction badge
            Box(
                modifier = Modifier
                    .background(cardinalBadgeBg, RoundedCornerShape(8.dp))
                    .border(
                        width = 1.dp,
                        color = if (isNorth) NorthRed else BorderStrong,
                        shape = RoundedCornerShape(8.dp)
                    )
                    .padding(horizontal = 16.dp, vertical = 5.dp)
            ) {
                Text(
                    text = cardinal,
                    color = cardinalTextColor,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace,
                    letterSpacing = 3.sp
                )
            }
        }

        // ── Center: Compass rose ───────────────────────────────────────────
        CompassRoseDial(
            heading = heading,
            pitch   = pitch,
            roll    = roll
        )

        // ── Bottom: Pitch & Roll pills ─────────────────────────────────────
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(bottom = 20.dp)
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                PillCard(label = "PITCH", value = "${pitch.toInt()}°")
                PillCard(label = "ROLL",  value = "${roll.toInt()}°")
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// SCREEN 3 — LEVEL ONLY (magnetometer absent)
// ─────────────────────────────────────────────────────────────────────────────

@Composable
fun ScreenContentView(pitch: Float, roll: Float, isStaticMock: Boolean = false) {
    val displayPitch = if (isStaticMock) -2f else pitch
    val displayRoll  = if (isStaticMock) -1f else roll
    val inclination  = maxOf(abs(displayPitch), abs(displayRoll))

    // Degree readout color transitions green as it approaches 0
    val degreeColor by animateColorAsState(
        targetValue = when {
            inclination < 1f  -> TargetGreen
            inclination < 5f  -> AmberWarning
            else              -> TextWhite
        },
        animationSpec = tween(400),
        label = "degColor"
    )

    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // ── Top: inclination readout ───────────────────────────────────────
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(top = 20.dp)
        ) {
            Text(
                text = "${inclination.toInt()}°",
                fontSize = 80.sp,
                fontWeight = FontWeight.Bold,
                color = degreeColor,
                fontFamily = FontFamily.Monospace
            )

            Text(
                text = "LEVEL ONLY",
                color = TextMuted,
                fontSize = 13.sp,
                fontFamily = FontFamily.Monospace,
                letterSpacing = 1.5.sp
            )

            Spacer(modifier = Modifier.height(12.dp))

            // TAP TO ZERO button
            Box(
                modifier = Modifier
                    .background(CardSurface, RoundedCornerShape(10.dp))
                    .border(1.dp, BorderStrong, RoundedCornerShape(10.dp))
                    .padding(horizontal = 20.dp, vertical = 8.dp)
            ) {
                Text(
                    text = "+ TAP TO ZERO",
                    color = TargetGreen,
                    fontSize = 12.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
            }
        }

        // ── Center: reticle spirit level ───────────────────────────────────
        ReticleSpiritLevel(pitch = displayPitch, roll = displayRoll)

        // ── Bottom: status + pills ─────────────────────────────────────────
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(bottom = 20.dp)
        ) {
            // Magnetometer absent notice
            Box(
                modifier = Modifier
                    .background(CardSurface, RoundedCornerShape(6.dp))
                    .border(0.5.dp, AmberWarning.copy(alpha = 0.4f), RoundedCornerShape(6.dp))
                    .padding(horizontal = 12.dp, vertical = 5.dp)
            ) {
                Text(
                    text = "⚠  MAGNETOMETER UNAVAILABLE",
                    color = AmberWarning,
                    fontSize = 10.sp,
                    fontFamily = FontFamily.Monospace,
                    letterSpacing = 0.5.sp
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                PillCard(label = "PITCH", value = "${displayPitch.toInt()}°")
                PillCard(label = "ROLL",  value = "${displayRoll.toInt()}°")
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// SCREEN — EMPTY (no sensor activity)
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
                .size(76.dp)
                .background(CardSurface, RoundedCornerShape(20.dp))
                .border(1.dp, BorderStrong, RoundedCornerShape(20.dp)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "N/A",
                color = TextMuted,
                fontSize = 20.sp,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold
            )
        }
        Spacer(modifier = Modifier.height(28.dp))
        Text(
            text = "NO SENSOR ACTIVITY",
            color = TextWhite,
            fontSize = 14.sp,
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.5.sp
        )
        Spacer(modifier = Modifier.height(10.dp))
        Text(
            text = "Device sensors are idle or waiting\nfor motion input.",
            color = TextMuted,
            fontSize = 12.sp,
            textAlign = TextAlign.Center,
            lineHeight = 19.sp,
            fontFamily = FontFamily.Monospace
        )
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// SCREEN 4 — ERROR / CALIBRATION NEEDED
// ─────────────────────────────────────────────────────────────────────────────

@Composable
fun ScreenErrorView() {
    val infiniteTransition = rememberInfiniteTransition(label = "error_pulse")

    // Pulsing ring alpha
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.2f,
        targetValue  = 0.7f,
        animationSpec = infiniteRepeatable(tween(900, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "amber_pulse"
    )
    // Pulsing ring scale
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1.0f,
        targetValue  = 1.15f,
        animationSpec = infiniteRepeatable(tween(900, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "scale_pulse"
    )

    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Pulsing amber ring around warning icon
        Box(contentAlignment = Alignment.Center) {
            Canvas(modifier = Modifier.size(100.dp)) {
                // Pulsing outer glow ring
                drawCircle(
                    color = AmberWarning.copy(alpha = pulseAlpha * 0.3f),
                    radius = (46.dp * pulseScale).toPx(),
                    style = Stroke(12.dp.toPx())
                )
                // Static inner ring
                drawCircle(
                    color = AmberWarning.copy(alpha = 0.25f),
                    radius = 36.dp.toPx(),
                    style = Stroke(1.5.dp.toPx())
                )
            }

            // Warning icon exclamation
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .background(
                        brush = Brush.radialGradient(
                            listOf(GlowAmber, Color.Transparent)
                        ),
                        shape = androidx.compose.foundation.shape.CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "!",
                    color = AmberWarning,
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // CALIBRATION NEEDED badge
        Box(
            modifier = Modifier
                .background(GlowAmberSoft, RoundedCornerShape(8.dp))
                .border(1.dp, AmberWarning, RoundedCornerShape(8.dp))
                .padding(horizontal = 16.dp, vertical = 7.dp)
        ) {
            Text(
                text = "CALIBRATION NEEDED",
                color = AmberWarning,
                fontSize = 12.sp,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.5.sp
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        Text(
            text = "MAGNETIC INTERFERENCE\nDETECTED",
            color = TextWhite,
            fontSize = 15.sp,
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            letterSpacing = 1.sp,
            lineHeight = 24.sp
        )

        Spacer(modifier = Modifier.height(14.dp))

        // Figure-8 instruction
        Box(
            modifier = Modifier
                .background(CardSurface, RoundedCornerShape(10.dp))
                .border(0.5.dp, BorderStrong, RoundedCornerShape(10.dp))
                .padding(horizontal = 20.dp, vertical = 12.dp)
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "∞",
                    color = AmberWarning,
                    fontSize = 32.sp
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Wave phone in a figure-8\npattern to recalibrate",
                    color = TextMuted,
                    fontSize = 12.sp,
                    textAlign = TextAlign.Center,
                    lineHeight = 18.sp,
                    fontFamily = FontFamily.Monospace
                )
            }
        }
    }
}
