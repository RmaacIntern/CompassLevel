package com.aivigil.day0check

import android.os.Bundle
import android.view.HapticFeedbackConstants
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.abs
import kotlin.math.roundToInt

class MainActivity : ComponentActivity() {
    private val viewModel: CompassViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val state by viewModel.uiState.collectAsState()
            CompassScreen(
                state = state,
                onCalibrate = { viewModel.calibrateZero() },
                onResetCalibration = { viewModel.resetCalibration() }
            )
        }
    }
}

@Composable
fun CompassScreen(
    state: CompassUiState,
    onCalibrate: () -> Unit,
    onResetCalibration: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF000000))
            .systemBarsPadding()
            .clickable(
                interactionSource = interactionSource,
                indication = null
            ) {
                if (state is CompassUiState.Content) {
                    if (state.isCalibrated) onResetCalibration() else onCalibrate()
                }
            },
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            when (state) {
                is CompassUiState.NoSensor -> NoSensorStateView()
                is CompassUiState.Content -> ContentStateView(state)
                is CompassUiState.Settings -> Unit
            }
        }

        // Bottom Ad Banner Safe Container (Never overlaps gauge)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
                .background(Color(0xFF080808)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "AD BANNER",
                fontSize = 10.sp,
                fontWeight = FontWeight.Medium,
                letterSpacing = 2.sp,
                color = Color(0xFF2C2C2E)
            )
        }
    }
}

@Composable
fun ContentStateView(state: CompassUiState.Content) {
    val view = LocalView.current

    // Haptic feedback on snap
    LaunchedEffect(state.isLevel) {
        if (state.isLevel) {
            view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
        }
    }

    // Hardware-accelerated smooth spring animations
    val animatedPitch by animateFloatAsState(
        targetValue = state.pitchDegrees,
        animationSpec = spring(stiffness = Spring.StiffnessLow, dampingRatio = Spring.DampingRatioNoBouncy),
        label = "pitchAnim"
    )
    val animatedRoll by animateFloatAsState(
        targetValue = state.rollDegrees,
        animationSpec = spring(stiffness = Spring.StiffnessLow, dampingRatio = Spring.DampingRatioNoBouncy),
        label = "rollAnim"
    )
    val animatedHeading by animateFloatAsState(
        targetValue = state.headingDegrees,
        animationSpec = spring(stiffness = Spring.StiffnessLow, dampingRatio = Spring.DampingRatioNoBouncy),
        label = "headingAnim"
    )

    val levelColor by animateColorAsState(
        targetValue = if (state.isLevel) Color(0xFF34C759) else Color.White,
        animationSpec = tween(durationMillis = 150),
        label = "colorAnim"
    )

    val deviationAngle = maxOf(abs(animatedPitch), abs(animatedRoll)).roundToInt()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp, vertical = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // Top Apple Typography Readout
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            if (state.isCompassAvailable) {
                Text(
                    text = "${animatedHeading.roundToInt()}°",
                    fontSize = 80.sp,
                    fontWeight = FontWeight.ExtraLight,
                    color = Color.White,
                    letterSpacing = (-3).sp
                )
                Text(
                    text = getCardinalDirection(animatedHeading),
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFF8E8E93),
                    letterSpacing = 3.sp
                )
            } else {
                Text(
                    text = "${deviationAngle}°",
                    fontSize = 80.sp,
                    fontWeight = FontWeight.ExtraLight,
                    color = levelColor,
                    letterSpacing = (-3).sp
                )
                Text(
                    text = if (state.isLevel) "LEVEL" else if (state.isCalibrated) "CALIBRATED ZERO" else "TAP TO ZERO",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = if (state.isLevel) Color(0xFF34C759) else Color(0xFF8E8E93),
                    letterSpacing = 2.5.sp
                )
            }
        }

        // Center Apple Dual Bubble & Compass Rose
        Box(
            modifier = Modifier.size(290.dp),
            contentAlignment = Alignment.Center
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val center = this.center
                val radius = size.minDimension / 2f

                // Outer Compass Rose ticks
                if (state.isCompassAvailable) {
                    rotate(-animatedHeading, pivot = center) {
                        drawCompassRose(center, radius)
                    }
                } else {
                    drawCircle(
                        color = Color(0xFF1C1C1E),
                        radius = radius,
                        style = Stroke(width = 1.dp.toPx())
                    )
                }

                // Apple-style Level Discs
                val maxTravel = radius * 0.55f
                val bubbleOffsetX = (animatedRoll / 30f).coerceIn(-1f, 1f) * maxTravel
                val bubbleOffsetY = (animatedPitch / 30f).coerceIn(-1f, 1f) * maxTravel
                val bubbleRadius = 42.dp.toPx()

                if (state.isLevel) {
                    // Merged solid green disk on true level
                    drawCircle(
                        color = Color(0xFF34C759),
                        radius = bubbleRadius,
                        center = center
                    )
                } else {
                    // Fixed central target circle
                    drawCircle(
                        color = Color.White,
                        radius = bubbleRadius,
                        center = center,
                        style = Stroke(width = 1.25.dp.toPx())
                    )

                    // Moving spirit bubble
                    drawCircle(
                        color = Color(0xFF1C1C1E),
                        radius = bubbleRadius - 1.dp.toPx(),
                        center = Offset(center.x + bubbleOffsetX, center.y + bubbleOffsetY)
                    )
                    drawCircle(
                        color = Color.White.copy(alpha = 0.85f),
                        radius = bubbleRadius,
                        center = Offset(center.x + bubbleOffsetX, center.y + bubbleOffsetY),
                        style = Stroke(width = 1.25.dp.toPx())
                    )
                    // Inner dot
                    drawCircle(
                        color = Color.White,
                        radius = 2.dp.toPx(),
                        center = Offset(center.x + bubbleOffsetX, center.y + bubbleOffsetY)
                    )
                }

                // Precision crosshairs
                drawLine(
                    color = Color(0xFF2C2C2E),
                    start = Offset(center.x - 12.dp.toPx(), center.y),
                    end = Offset(center.x + 12.dp.toPx(), center.y),
                    strokeWidth = 1.dp.toPx()
                )
                drawLine(
                    color = Color(0xFF2C2C2E),
                    start = Offset(center.x, center.y - 12.dp.toPx()),
                    end = Offset(center.x, center.y + 12.dp.toPx()),
                    strokeWidth = 1.dp.toPx()
                )
            }
        }

        // Bottom Metrics: Pitch & Roll Readout
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, bottom = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            MetricPill(label = "PITCH", degrees = animatedPitch.roundToInt())
            MetricPill(label = "ROLL", degrees = animatedRoll.roundToInt())
        }
    }
}

private fun DrawScope.drawCompassRose(center: Offset, radius: Float) {
    for (i in 0 until 360 step 30) {
        val isCardinal = i % 90 == 0
        val tickLength = if (isCardinal) 18.dp.toPx() else 8.dp.toPx()
        val tickColor = if (i == 0) Color(0xFFFF3B30) else if (isCardinal) Color.White else Color(0xFF3A3A3C)
        val stroke = if (isCardinal) 2.dp.toPx() else 1.dp.toPx()

        rotate(i.toFloat(), pivot = center) {
            drawLine(
                color = tickColor,
                start = Offset(center.x, center.y - radius),
                end = Offset(center.x, center.y - radius + tickLength),
                strokeWidth = stroke,
                cap = StrokeCap.Round
            )
        }
    }
}

@Composable
fun MetricPill(label: String, degrees: Int) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = label,
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold,
            color = Color(0xFF636366),
            letterSpacing = 2.sp
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = "${degrees}°",
            fontSize = 24.sp,
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Light,
            color = Color.White
        )
    }
}

@Composable
fun NoSensorStateView() {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier.padding(32.dp)
    ) {
        Text(
            text = "SENSOR UNAVAILABLE",
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFFFF453A),
            letterSpacing = 2.sp
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Neither orientation nor accelerometer sensors are available on this device.",
            fontSize = 13.sp,
            color = Color(0xFF8E8E93),
            lineHeight = 18.sp
        )
    }
}

private fun getCardinalDirection(heading: Float): String {
    return when ((heading + 22.5f) % 360) {
        in 0.0f..45.0f -> "N"
        in 45.0f..90.0f -> "NE"
        in 90.0f..135.0f -> "E"
        in 135.0f..180.0f -> "SE"
        in 180.0f..225.0f -> "S"
        in 225.0f..270.0f -> "SW"
        in 270.0f..315.0f -> "W"
        else -> "NW"
    }
}
