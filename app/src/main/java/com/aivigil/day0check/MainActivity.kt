package com.aivigil.day0check

import android.os.Bundle
import android.view.HapticFeedbackConstants
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
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
            CompassScreen(state = state)
        }
    }
}

@Composable
fun CompassScreen(state: CompassUiState) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF000000))
            .systemBarsPadding(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Main Utility Workspace (Takes remaining height, never clipped by ads)
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

        // Bottom Ad Banner Inset Zone (Guarantees banner never covers the dial)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .background(Color(0xFF0D0D0E)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "AD BANNER CONTAINER",
                fontSize = 10.sp,
                fontWeight = FontWeight.Medium,
                letterSpacing = 1.5.sp,
                color = Color(0xFF3A3A3C)
            )
        }
    }
}

@Composable
fun ContentStateView(state: CompassUiState.Content) {
    val view = LocalView.current

    LaunchedEffect(state.isLevel) {
        if (state.isLevel) {
            view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
        }
    }

    val accentColor by animateColorAsState(
        targetValue = if (state.isLevel) Color(0xFF34C759) else Color.White,
        animationSpec = tween(durationMillis = 180),
        label = "levelAccent"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp, vertical = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // Top Heading Readout
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            if (state.isCompassAvailable) {
                Text(
                    text = "${state.headingDegrees.roundToInt()}°",
                    fontSize = 68.sp,
                    fontWeight = FontWeight.Light,
                    color = Color.White,
                    letterSpacing = (-2).sp
                )
                Text(
                    text = getCardinalDirection(state.headingDegrees),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF8E8E93),
                    letterSpacing = 2.sp
                )
            } else {
                Text(
                    text = if (state.isLevel) "0°" else "${maxOf(abs(state.pitchDegrees), abs(state.rollDegrees)).roundToInt()}°",
                    fontSize = 68.sp,
                    fontWeight = FontWeight.Light,
                    color = accentColor,
                    letterSpacing = (-2).sp
                )
                Text(
                    text = if (state.isLevel) "LEVEL" else "TILT",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = if (state.isLevel) Color(0xFF34C759) else Color(0xFF8E8E93),
                    letterSpacing = 3.sp
                )
            }

            // Unreliable Accuracy / Calibration Warning Tag
            if (state.isUnreliable) {
                Spacer(modifier = Modifier.height(8.dp))
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color(0x33FF9F0A)
                ) {
                    Text(
                        text = "CALIBRATION NEEDED (TILT IN FIGURE 8)",
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFFF9F0A),
                        letterSpacing = 1.sp,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }
            }
        }

        // Center Dual Compass & Level Reticle
        Box(
            modifier = Modifier.size(260.dp),
            contentAlignment = Alignment.Center
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val center = this.center
                val radius = size.minDimension / 2f

                if (state.isCompassAvailable) {
                    rotate(-state.headingDegrees, pivot = center) {
                        for (i in 0 until 360 step 30) {
                            val isCardinal = i % 90 == 0
                            val tickLength = if (isCardinal) 14.dp.toPx() else 7.dp.toPx()
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
                } else {
                    drawCircle(
                        color = Color(0xFF2C2C2E),
                        radius = radius,
                        style = Stroke(width = 1.dp.toPx())
                    )
                }

                // Level boundary rings
                drawCircle(
                    color = Color(0xFF1C1C1E),
                    radius = radius * 0.45f,
                    style = Stroke(width = 1.dp.toPx())
                )

                val maxOffset = radius * 0.40f
                val offsetX = (state.rollDegrees / 45f).coerceIn(-1f, 1f) * maxOffset
                val offsetY = (state.pitchDegrees / 45f).coerceIn(-1f, 1f) * maxOffset

                drawCircle(
                    color = accentColor.copy(alpha = 0.3f),
                    radius = 8.dp.toPx(),
                    center = center
                )

                // Spirit level bubble
                drawCircle(
                    color = accentColor,
                    radius = 16.dp.toPx(),
                    center = Offset(center.x + offsetX, center.y + offsetY),
                    style = Stroke(width = 2.dp.toPx())
                )
            }
        }

        // Bottom Metrics: Monospace Pitch and Roll
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            MinimalMetric(label = "PITCH", value = "${state.pitchDegrees.roundToInt()}°")
            MinimalMetric(label = "ROLL", value = "${state.rollDegrees.roundToInt()}°")
        }
    }
}

@Composable
fun MinimalMetric(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = label,
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium,
            color = Color(0xFF636366),
            letterSpacing = 1.5.sp
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = value,
            fontSize = 22.sp,
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
            text = "Orientation and accelerometer sensors are not available on this device.",
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
