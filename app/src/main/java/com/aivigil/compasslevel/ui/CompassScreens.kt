package com.aivigil.compasslevel.ui

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aivigil.compasslevel.sensor.CompassUiState
import com.aivigil.compasslevel.ui.theme.*
import kotlin.math.min
import kotlin.math.roundToInt

@Composable
fun ScreenLoading() {
    val infiniteTransition = rememberInfiniteTransition(label = "spin")
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(animation = tween(1200, easing = LinearEasing)),
        label = "rotation"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Background)
            .safeDrawingPadding(),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        TopAppBar()

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(modifier = Modifier.size(80.dp), contentAlignment = Alignment.Center) {
                androidx.compose.foundation.Canvas(modifier = Modifier.fillMaxSize()) {
                    drawCircle(color = SurfaceMid, radius = 36.dp.toPx(), style = Stroke(3.dp.toPx()))
                }
                androidx.compose.foundation.Canvas(modifier = Modifier.fillMaxSize().rotate(rotation)) {
                    drawArc(
                        color = AccentGreen,
                        startAngle = 0f,
                        sweepAngle = 100f,
                        useCenter = false,
                        style = Stroke(3.dp.toPx(), cap = StrokeCap.Round)
                    )
                }
            }

            Spacer(Modifier.height(28.dp))

            Text(
                text = "INITIALIZING SENSORS",
                color = TextSecondary,
                fontSize = 11.sp,
                fontFamily = FontFamily.Monospace,
                letterSpacing = 1.8.sp
            )

            Spacer(Modifier.height(10.dp))

            Box(
                modifier = Modifier
                    .width(140.dp)
                    .height(2.dp)
                    .background(SurfaceMid)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .fillMaxWidth(0.6f)
                        .background(AccentGreen)
                )
            }

            Spacer(Modifier.height(24.dp))

            Text(
                text = "COMPASS & LEVEL",
                color = BorderStrong,
                fontSize = 12.sp,
                fontWeight = FontWeight.Thin,
                letterSpacing = 3.sp
            )
        }

        AdBanner()
    }
}

@Composable
fun ScreenContent(
    state: CompassUiState,
    onMenuClick: () -> Unit = {}
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Background)
            .safeDrawingPadding(),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        TopAppBar(onOverflowClick = onMenuClick)

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "${state.heading.roundToInt()}°",
                color = TextPrimary,
                fontSize = 64.sp,
                fontWeight = FontWeight.Thin,
                lineHeight = 64.sp
            )
            Text(
                text = getCardinalFromDegrees(state.heading),
                color = TextSecondary,
                fontSize = 18.sp,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Light,
                letterSpacing = 3.sp
            )
        }

        // Dynamic auto-fitting dial box
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .padding(vertical = 8.dp),
            contentAlignment = Alignment.Center
        ) {
            val dialSize = min(maxHeight.value, maxWidth.value) * 0.82f
            val dialDp = min(dialSize, 260f).dp

            Box(modifier = Modifier.size(dialDp), contentAlignment = Alignment.Center) {
                CompassRose(heading = state.heading, modifier = Modifier.fillMaxSize())
                SpiritLevel(pitch = state.pitch, roll = state.roll, modifier = Modifier.fillMaxSize(0.38f))
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 14.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterHorizontally)
        ) {
            PillCard(label = "PITCH", value = "${state.pitch.roundToInt()}°")
            PillCard(label = "ROLL", value = "${state.roll.roundToInt()}°")
        }

        AdBanner()
    }
}

@Composable
fun ScreenFallbackLevel(
    state: CompassUiState,
    onTapToZero: () -> Unit = {}
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Background)
            .safeDrawingPadding(),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        TopAppBar()

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "${state.pitch.roundToInt()}°",
                color = TextPrimary,
                fontSize = 64.sp,
                fontWeight = FontWeight.Thin
            )

            Spacer(Modifier.height(6.dp))

            Row(
                modifier = Modifier
                    .background(SurfaceMid, RoundedCornerShape(8.dp))
                    .border(1.dp, BorderSubtle, RoundedCornerShape(8.dp))
                    .clickable { onTapToZero() }
                    .padding(horizontal = 14.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text("+", color = AccentGreen, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                Text(
                    text = "TAP TO ZERO",
                    color = TextPrimary,
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Medium,
                    letterSpacing = 1.2.sp
                )
            }
        }

        BoxWithConstraints(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .padding(vertical = 8.dp),
            contentAlignment = Alignment.Center
        ) {
            val dialSize = min(maxHeight.value, maxWidth.value) * 0.82f
            val dialDp = min(dialSize, 260f).dp

            Box(modifier = Modifier.size(dialDp), contentAlignment = Alignment.Center) {
                androidx.compose.foundation.Canvas(modifier = Modifier.fillMaxSize()) {
                    val c = center
                    val r = size.width / 2f
                    drawCircle(color = BorderSubtle, radius = r * 0.95f, center = c, style = Stroke(1.dp.toPx()))
                    drawCircle(color = BorderSubtle, radius = r * 0.65f, center = c, style = Stroke(1.dp.toPx()))
                    drawLine(color = BorderStrong, start = Offset(c.x - r, c.y), end = Offset(c.x + r, c.y), strokeWidth = 1.dp.toPx())
                    drawLine(color = BorderStrong, start = Offset(c.x, c.y - r), end = Offset(c.x, c.y + r), strokeWidth = 1.dp.toPx())
                    drawCircle(color = BorderStrong, radius = 2.dp.toPx(), center = c)
                }
                SpiritLevel(pitch = state.pitch, roll = state.roll, modifier = Modifier.fillMaxSize(0.38f))
            }
        }

        Text(
            text = "MAGNETOMETER UNAVAILABLE — LEVEL ONLY",
            color = BorderStrong,
            fontSize = 11.sp,
            letterSpacing = 0.5.sp,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth().padding(bottom = 10.dp)
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 14.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterHorizontally)
        ) {
            PillCard(label = "PITCH", value = "${state.pitch.roundToInt()}°")
            PillCard(label = "ROLL", value = "${state.roll.roundToInt()}°")
        }

        AdBanner()
    }
}

@Composable
fun ScreenUnreliable(
    state: CompassUiState,
    onStartCalibration: () -> Unit = {}
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Background)
            .safeDrawingPadding(),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        TopAppBar()

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(text = "${state.heading.roundToInt()}°", color = TextPrimary, fontSize = 56.sp, fontWeight = FontWeight.Thin)
                Spacer(Modifier.width(8.dp))
                Text(text = getCardinalFromDegrees(state.heading), color = TextSecondary, fontSize = 28.sp, fontWeight = FontWeight.Light)
            }

            Spacer(Modifier.height(6.dp))

            Row(
                modifier = Modifier
                    .background(WarningAmberBg, RoundedCornerShape(8.dp))
                    .border(1.dp, WarningAmber, RoundedCornerShape(8.dp))
                    .padding(horizontal = 12.dp, vertical = 5.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text("⚠", color = WarningAmber, fontSize = 11.sp)
                Text(
                    text = "CALIBRATION NEEDED",
                    color = WarningAmber,
                    fontSize = 10.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Medium,
                    letterSpacing = 1.3.sp
                )
            }
        }

        BoxWithConstraints(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .padding(vertical = 6.dp),
            contentAlignment = Alignment.Center
        ) {
            val dialSize = min(maxHeight.value, maxWidth.value) * 0.82f
            val dialDp = min(dialSize, 260f).dp

            Box(modifier = Modifier.size(dialDp), contentAlignment = Alignment.Center) {
                CompassRose(heading = state.heading, modifier = Modifier.fillMaxSize().alpha(0.28f))
                SpiritLevel(pitch = state.pitch, roll = state.roll, modifier = Modifier.fillMaxSize(0.38f).alpha(0.4f))

                Box(
                    modifier = Modifier
                        .size(34.dp)
                        .background(SurfaceMid, CircleShape)
                        .border(1.dp, WarningAmber, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text("🔒", fontSize = 13.sp)
                }
            }
        }

        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Wave phone in a figure-8 pattern to calibrate",
                color = TextSecondary,
                fontSize = 12.sp,
                textAlign = TextAlign.Center,
                lineHeight = 18.sp,
                modifier = Modifier.width(220.dp)
            )

            Spacer(Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterHorizontally)
            ) {
                PillCard(label = "PITCH", value = "${state.pitch.roundToInt()}°")
                PillCard(label = "ROLL", value = "${state.roll.roundToInt()}°")
            }

            Spacer(Modifier.height(10.dp))

            OutlinedButton(
                onClick = onStartCalibration,
                border = androidx.compose.foundation.BorderStroke(1.dp, WarningAmber),
                shape = RoundedCornerShape(4.dp),
                colors = ButtonDefaults.outlinedButtonColors(containerColor = Color.Transparent),
                modifier = Modifier.padding(bottom = 12.dp)
            ) {
                Text(
                    text = "START CALIBRATION",
                    color = WarningAmber,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    letterSpacing = 1.sp
                )
            }
        }

        AdBanner()
    }
}

fun getCardinalFromDegrees(deg: Float): String {
    val norm = (deg % 360 + 360) % 360
    return when {
        norm >= 337.5 || norm < 22.5 -> "N"
        norm < 67.5 -> "NE"
        norm < 112.5 -> "E"
        norm < 157.5 -> "SE"
        norm < 202.5 -> "S"
        norm < 247.5 -> "SW"
        norm < 292.5 -> "W"
        else -> "NW"
    }
}
