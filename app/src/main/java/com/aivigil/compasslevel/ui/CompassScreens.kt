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
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold,
                letterSpacing = 2.sp
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "Aligning magnetic & inertial pipeline",
                color = TextSecondary,
                fontSize = 11.sp,
                fontFamily = FontFamily.Monospace
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
    isLevel: Boolean
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
        // ── Top Readout: Heading Number & Cardinal Badge ──────────────────
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(top = 16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "${heading.toInt()}",
                    fontSize = 76.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextWhite,
                    fontFamily = FontFamily.Monospace
                )
                Text(
                    text = "°",
                    fontSize = 38.sp,
                    fontWeight = FontWeight.Bold,
                    color = LaserRed,
                    fontFamily = FontFamily.Monospace
                )
            }

            // Cardinal Badge
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(if (isNorth) GlowRed else CardSurface)
                    .border(
                        width = 1.dp,
                        color = if (isNorth) LaserRed else BorderStrong,
                        shape = RoundedCornerShape(8.dp)
                    )
                    .padding(horizontal = 18.dp, vertical = 5.dp)
            ) {
                Text(
                    text = cardinal,
                    color = if (isNorth) LaserRed else TextWhite,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace,
                    letterSpacing = 2.5.sp
                )
            }
        }

        // ── Center Dial: 60/120 FPS Rotating Compass Rose with Level ───────
        CompassRoseDial(
            heading = heading,
            pitch = pitch,
            roll = roll,
            isLevel = isLevel
        )

        // ── Bottom: High-Precision Pitch & Roll ────────────────────────────
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(bottom = 16.dp)
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                PrecisionPillCard(label = "PITCH", valueDegrees = pitch)
                PrecisionPillCard(label = "ROLL",  valueDegrees = roll)
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// SCREEN 3: FULL LEVEL MODE (Dual-Axis Bullseye Reticle)
// ─────────────────────────────────────────────────────────────────────────────

@Composable
fun ScreenContentView(
    pitch: Float,
    roll: Float,
    isLevel: Boolean,
    usePercentGrade: Boolean = false,
    onTareClick: () -> Unit = {}
) {
    val totalInclination = max(abs(pitch), abs(roll))
    val displayValue = if (usePercentGrade) {
        val grade = kotlin.math.tan(Math.toRadians(totalInclination.toDouble())) * 100.0
        String.format("%.1f", grade)
    } else {
        String.format("%.1f", totalInclination)
    }
    val unitSymbol = if (usePercentGrade) "%" else "°"

    val angleColor by animateColorAsState(
        targetValue = when {
            isLevel -> NeonEmerald
            totalInclination < 3f -> AmberWarning
            else -> TextWhite
        },
        animationSpec = tween(200),
        label = "angleColor"
    )

    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // ── Top Readout: Total Inclination & Tare Button ────────────────────
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(top = 16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Text(
                    text = displayValue,
                    fontSize = 76.sp,
                    fontWeight = FontWeight.Bold,
                    color = angleColor,
                    fontFamily = FontFamily.Monospace
                )
                Text(
                    text = unitSymbol,
                    fontSize = 38.sp,
                    fontWeight = FontWeight.Bold,
                    color = angleColor,
                    fontFamily = FontFamily.Monospace
                )
            }

            Text(
                text = if (isLevel) "PERFECTLY LEVEL" else "SURFACE INCLINATION",
                color = if (isLevel) NeonEmerald else Color(0xFFC0C7D5),
                fontSize = 13.sp,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.5.sp
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Tap to Zero (Tare) Button
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(10.dp))
                    .background(CardSurface)
                    .border(1.dp, if (isLevel) NeonEmerald else BorderStrong, RoundedCornerShape(10.dp))
                    .clickable { onTareClick() }
                    .padding(horizontal = 22.dp, vertical = 10.dp)
            ) {
                Text(
                    text = "+ TARE / ZERO SURFACE",
                    color = if (isLevel) NeonEmerald else TextWhite,
                    fontSize = 12.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
            }
        }

        // ── Center Reticle: Full 2D Spirit Level ───────────────────────────
        ReticleSpiritLevel(
            pitch = pitch,
            roll = roll,
            isLevel = isLevel
        )

        // ── Bottom Pills ───────────────────────────────────────────────────
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(bottom = 16.dp)
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                PrecisionPillCard(label = "PITCH", valueDegrees = pitch)
                PrecisionPillCard(label = "ROLL",  valueDegrees = roll)
            }
        }
    }
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
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold
            )
        }
        Spacer(modifier = Modifier.height(20.dp))
        Text(
            text = "AWAITING SENSOR MOTION",
            color = TextPrimary,
            fontSize = 13.sp,
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.sp
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Pick up or rotate your device to initiate reading",
            color = TextMuted,
            fontSize = 12.sp,
            textAlign = TextAlign.Center,
            fontFamily = FontFamily.Monospace
        )
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// SCREEN 5: ERROR / CALIBRATION MODAL
// ─────────────────────────────────────────────────────────────────────────────

@Composable
fun ScreenErrorView(
    onDismiss: () -> Unit = {}
) {
    val infiniteTransition = rememberInfiniteTransition(label = "error_pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.15f,
        animationSpec = infiniteRepeatable(tween(900, easing = FastOutSlowInEasing), RepeatMode.Reverse),
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
                    color = GlowAmber,
                    radius = (40.dp * pulseScale).toPx(),
                    style = Stroke(8.dp.toPx())
                )
            }

            Box(
                modifier = Modifier
                    .size(54.dp)
                    .clip(CircleShape)
                    .background(AmberWarning),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "!",
                    color = Color.Black,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        Text(
            text = "MAGNETIC INTERFERENCE",
            color = TextPrimary,
            fontSize = 15.sp,
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.5.sp
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Nearby metallic objects or cases may distort accuracy.\nWave your device in a figure-8 motion to recalibrate.",
            color = TextSecondary,
            fontSize = 12.sp,
            textAlign = TextAlign.Center,
            lineHeight = 18.sp,
            fontFamily = FontFamily.Monospace
        )

        Spacer(modifier = Modifier.height(24.dp))

        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(10.dp))
                .background(CardSurface)
                .border(1.dp, BorderStrong, RoundedCornerShape(10.dp))
                .clickable { onDismiss() }
                .padding(horizontal = 24.dp, vertical = 10.dp)
        ) {
            Text(
                text = "CONTINUE ANYWAY",
                color = TextPrimary,
                fontSize = 12.sp,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// SCREEN 6: SETTINGS (True/Magnetic North, Units, About)
// ─────────────────────────────────────────────────────────────────────────────

@Composable
fun ScreenSettingsView(
    isTrueNorth: Boolean,
    declination: Float,
    usePercentGrade: Boolean,
    onTrueNorthToggle: (Boolean) -> Unit,
    onDeclinationChange: (Float) -> Unit,
    onPercentGradeToggle: (Boolean) -> Unit,
    onClose: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkSurface)
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
                    text = "SETTINGS",
                    color = TextWhite,
                    fontSize = 18.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 2.sp
                )
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(CardSurface)
                        .border(1.dp, BorderStrong, RoundedCornerShape(8.dp))
                        .clickable { onClose() }
                        .padding(horizontal = 14.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = "DONE",
                        color = NeonEmerald,
                        fontSize = 12.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // Card 1: North Reference (Magnetic vs True North)
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(CardSurface)
                    .border(1.dp, BorderSubtle, RoundedCornerShape(12.dp))
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "NORTH REFERENCE",
                    color = TextSecondary,
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace,
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
                            .background(if (!isTrueNorth) CardSurfaceElevated else Color.Transparent)
                            .border(1.dp, if (!isTrueNorth) LaserRed else BorderSubtle, RoundedCornerShape(8.dp))
                            .clickable { onTrueNorthToggle(false) }
                            .padding(vertical = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "MAGNETIC",
                            color = if (!isTrueNorth) TextWhite else TextSecondary,
                            fontSize = 12.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (isTrueNorth) CardSurfaceElevated else Color.Transparent)
                            .border(1.dp, if (isTrueNorth) LaserRed else BorderSubtle, RoundedCornerShape(8.dp))
                            .clickable { onTrueNorthToggle(true) }
                            .padding(vertical = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "TRUE NORTH",
                            color = if (isTrueNorth) TextWhite else TextSecondary,
                            fontSize = 12.sp,
                            fontFamily = FontFamily.Monospace,
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
                            color = TextPrimary,
                            fontSize = 12.sp,
                            fontFamily = FontFamily.Monospace
                        )
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(CardSurfaceElevated)
                                    .border(1.dp, BorderStrong, RoundedCornerShape(6.dp))
                                    .clickable { onDeclinationChange(declination - 1f) }
                                    .padding(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                Text(text = "-1°", color = TextWhite, fontSize = 12.sp, fontFamily = FontFamily.Monospace)
                            }
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(CardSurfaceElevated)
                                    .border(1.dp, BorderStrong, RoundedCornerShape(6.dp))
                                    .clickable { onDeclinationChange(declination + 1f) }
                                    .padding(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                Text(text = "+1°", color = TextWhite, fontSize = 12.sp, fontFamily = FontFamily.Monospace)
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
                    .background(CardSurface)
                    .border(1.dp, BorderSubtle, RoundedCornerShape(12.dp))
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "LEVEL ANGLE UNITS",
                    color = TextSecondary,
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace,
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
                            .background(if (!usePercentGrade) CardSurfaceElevated else Color.Transparent)
                            .border(1.dp, if (!usePercentGrade) NeonEmerald else BorderSubtle, RoundedCornerShape(8.dp))
                            .clickable { onPercentGradeToggle(false) }
                            .padding(vertical = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "DEGREES (°)",
                            color = if (!usePercentGrade) TextWhite else TextSecondary,
                            fontSize = 12.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (usePercentGrade) CardSurfaceElevated else Color.Transparent)
                            .border(1.dp, if (usePercentGrade) NeonEmerald else BorderSubtle, RoundedCornerShape(8.dp))
                            .clickable { onPercentGradeToggle(true) }
                            .padding(vertical = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "% GRADE",
                            color = if (usePercentGrade) TextWhite else TextSecondary,
                            fontSize = 12.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            // Card 3: About & Verification
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(CardSurface)
                    .border(1.dp, BorderSubtle, RoundedCornerShape(12.dp))
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    text = "ABOUT & PRIVACY",
                    color = TextSecondary,
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
                Text(
                    text = "Compass & Level v1.0",
                    color = TextWhite,
                    fontSize = 13.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Target SDK: 36 (Android 16) | Pure Offline Utility",
                    color = TextSecondary,
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace
                )
                Text(
                    text = "Zero runtime permissions required.\nNo GPS, no background services, no tracking.",
                    color = TextMuted,
                    fontSize = 10.sp,
                    fontFamily = FontFamily.Monospace,
                    lineHeight = 14.sp
                )
            }
        }
    }
}
