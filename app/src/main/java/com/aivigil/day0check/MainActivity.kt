package com.aivigil.day0check

import android.os.Bundle
import android.view.HapticFeedbackConstants
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.*

enum class DemoState { LIVE, LOADING, CONTENT, EMPTY, ERROR }

@OptIn(ExperimentalMaterial3Api::class)
class MainActivity : ComponentActivity() {
    private val viewModel: CompassViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val uiState by viewModel.uiState.collectAsState()
            var demoOverride by remember { mutableStateOf(DemoState.LIVE) }

            CompassTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = Color(0xFF000000)
                ) {
                    Scaffold(
                        containerColor = Color(0xFF000000),
                        topBar = {
                            Column {
                                TopAppBar(
                                    title = {
                                        Text(
                                            text = "Compass & Level",
                                            fontSize = 18.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = Color(0xFFFFFFFF)
                                        )
                                    },
                                    actions = {
                                        IconButton(onClick = { /* Day 2 Settings */ }) {
                                            Canvas(modifier = Modifier.size(24.dp)) {
                                                val cx = size.width / 2f
                                                val cy = size.height / 2f
                                                val r = 1.5.dp.toPx()
                                                val dotColor = Color(0xFF8E8E93)
                                                drawCircle(dotColor, r, Offset(cx, cy - 6.dp.toPx()))
                                                drawCircle(dotColor, r, Offset(cx, cy))
                                                drawCircle(dotColor, r, Offset(cx, cy + 6.dp.toPx()))
                                            }
                                        }
                                    },
                                    colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF000000))
                                )
                                // Scannable compact state strip
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(Color(0xFF0D0D0E))
                                        .padding(horizontal = 8.dp, vertical = 6.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    DemoTab("Live", demoOverride == DemoState.LIVE) { demoOverride = DemoState.LIVE }
                                    DemoTab("Loading", demoOverride == DemoState.LOADING) { demoOverride = DemoState.LOADING }
                                    DemoTab("Content", demoOverride == DemoState.CONTENT) { demoOverride = DemoState.CONTENT }
                                    DemoTab("Empty", demoOverride == DemoState.EMPTY) { demoOverride = DemoState.EMPTY }
                                    DemoTab("Error", demoOverride == DemoState.ERROR) { demoOverride = DemoState.ERROR }
                                }
                            }
                        },
                        bottomBar = {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(50.dp)
                                    .background(Color(0xFF0D0D0E))
                                    .border(1.dp, Color(0xFF1C1C1E)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "- ADVERTISEMENT - 320x50",
                                    fontSize = 11.sp,
                                    fontFamily = FontFamily.Monospace,
                                    color = Color(0xFF636366),
                                    letterSpacing = 1.5.sp
                                )
                            }
                        }
                    ) { innerPadding ->
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(innerPadding)
                        ) {
                            when (demoOverride) {
                                DemoState.LOADING -> LoadingView()
                                DemoState.EMPTY -> EmptyStateView()
                                DemoState.ERROR -> ErrorStateView()
                                DemoState.CONTENT -> {
                                    // Mocked 324 NW Compass Content state as shown in design board
                                    val view = LocalView.current
                                    ContentView(
                                        state = CompassUiState.Content(
                                            headingDegrees = 324f,
                                            pitchDegrees = 1f,
                                            rollDegrees = 0f,
                                            isLevel = false,
                                            isCompassAvailable = true
                                        ),
                                        onTareClick = { view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP) }
                                    )
                                }
                                DemoState.LIVE -> {
                                    when (uiState) {
                                        is CompassUiState.Content -> {
                                            val view = LocalView.current
                                            ContentView(
                                                state = uiState as CompassUiState.Content,
                                                onTareClick = {
                                                    view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                                                    val c = uiState as CompassUiState.Content
                                                    if (c.isCalibrated) viewModel.resetCalibration() else viewModel.calibrateZero()
                                                }
                                            )
                                        }
                                        is CompassUiState.NoSensor -> ErrorStateView()
                                        is CompassUiState.Settings -> Unit
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun DemoTab(label: String, isSelected: Boolean, onClick: () -> Unit) {
    Surface(
        shape = RoundedCornerShape(6.dp),
        color = if (isSelected) Color(0xFF1C3A27) else Color.Transparent,
        modifier = Modifier.clickable(onClick = onClick)
    ) {
        Text(
            text = label,
            fontSize = 11.sp,
            fontFamily = FontFamily.Monospace,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
            color = if (isSelected) Color(0xFF34C759) else Color(0xFF8E8E93),
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        )
    }
}

@Composable
fun CompassTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = darkColorScheme(
            background = Color(0xFF000000),
            surface = Color(0xFF000000),
            onBackground = Color(0xFFFFFFFF),
            onSurface = Color(0xFFFFFFFF)
        ),
        content = content
    )
}

@Composable
fun LoadingView() {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        CircularProgressIndicator(
            color = Color(0xFF34C759),
            strokeWidth = 2.5.dp,
            modifier = Modifier.size(54.dp)
        )
        Spacer(modifier = Modifier.height(24.dp))
        Text(
            text = "INITIALIZING SENSORS",
            fontFamily = FontFamily.Monospace,
            fontSize = 12.sp,
            color = Color(0xFF8E8E93),
            letterSpacing = 2.sp
        )
    }
}

@Composable
fun EmptyStateView() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = Color(0xFF141416),
            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF2C2C2E)),
            modifier = Modifier.size(80.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Text(
                    text = "N/A",
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    fontSize = 22.sp,
                    color = Color(0xFF636366)
                )
            }
        }
        Spacer(modifier = Modifier.height(20.dp))
        Text(
            text = "NO SENSOR ACTIVITY",
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Bold,
            fontSize = 14.sp,
            color = Color(0xFFFFFFFF),
            letterSpacing = 1.sp
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Device sensors are idle or waiting for motion input.",
            fontSize = 13.sp,
            color = Color(0xFF8E8E93),
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )
    }
}

@Composable
fun ErrorStateView() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = Color(0xFF2C1E0A),
            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFF9F0A))
        ) {
            Text(
                text = "! CALIBRATION NEEDED",
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                color = Color(0xFFFF9F0A),
                fontSize = 12.sp,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold
            )
        }
        Spacer(modifier = Modifier.height(24.dp))
        Text(
            text = "MAGNETIC INTERFERENCE DETECTED",
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.SemiBold,
            fontSize = 13.sp,
            color = Color(0xFFFFFFFF)
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Wave phone in a figure-8 pattern to recalibrate the sensor compass.",
            fontSize = 12.sp,
            color = Color(0xFF8E8E93),
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )
    }
}

@Composable
fun ContentView(
    state: CompassUiState.Content,
    onTareClick: () -> Unit
) {
    val view = LocalView.current
    var prevSnap by remember { mutableStateOf(false) }

    LaunchedEffect(state.isLevel) {
        if (state.isLevel && !prevSnap) {
            view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
        }
        prevSnap = state.isLevel
    }

    val animatedHeading by animateFloatAsState(
        targetValue = state.headingDegrees,
        animationSpec = spring(stiffness = 300f, dampingRatio = 0.8f),
        label = "HeadingSpring"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) { onTareClick() },
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(top = 16.dp)
        ) {
            val degSymbol = "\u00B0"
            if (state.isCompassAvailable) {
                Text(
                    text = "${animatedHeading.roundToInt()}$degSymbol",
                    fontSize = 72.sp,
                    fontWeight = FontWeight.ExtraLight,
                    color = Color.White
                )
                Text(
                    text = getCardinalDirection(animatedHeading),
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Normal,
                    color = Color(0xFF8E8E93),
                    letterSpacing = 2.sp
                )
            } else {
                val totalTilt = sqrt(state.pitchDegrees * state.pitchDegrees + state.rollDegrees * state.rollDegrees)
                Text(
                    text = "${totalTilt.roundToInt()}$degSymbol",
                    fontSize = 72.sp,
                    fontWeight = FontWeight.ExtraLight,
                    color = Color.White
                )
                Text(
                    text = if (state.isCalibrated) "CALIBRATED ZERO" else "LEVEL ONLY",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF8E8E93),
                    letterSpacing = 1.sp
                )
            }

            Spacer(modifier = Modifier.height(8.dp))
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = if (state.isCalibrated) Color(0xFF1B3820) else Color(0xFF1C1C1E),
                border = androidx.compose.foundation.BorderStroke(
                    1.dp,
                    if (state.isCalibrated) Color(0xFF34C759) else Color(0xFF3A3A3C)
                )
            ) {
                Text(
                    text = if (state.isCalibrated) "+ TARE ACTIVE" else "+ TAP TO ZERO",
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                    color = if (state.isCalibrated) Color(0xFF34C759) else Color(0xFF8E8E93),
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }

        Box(
            modifier = Modifier
                .size(280.dp)
                .padding(8.dp),
            contentAlignment = Alignment.Center
        ) {
            DialCanvas(
                heading = animatedHeading,
                pitch = state.pitchDegrees,
                roll = state.rollDegrees,
                isSnapped = state.isLevel,
                showCompass = state.isCompassAvailable
            )
        }

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(bottom = 20.dp)
        ) {
            val degSymbol = "\u00B0"
            if (!state.isCompassAvailable) {
                Text(
                    text = "MAGNETOMETER UNAVAILABLE - LEVEL ONLY",
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace,
                    color = Color(0xFF636366),
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }

            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                MetricPill(label = "PITCH", value = "${state.pitchDegrees.roundToInt()}$degSymbol")
                MetricPill(label = "ROLL", value = "${state.rollDegrees.roundToInt()}$degSymbol")
            }
        }
    }
}

@Composable
fun MetricPill(label: String, value: String) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = Color(0xFF141416),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF2C2C2E)),
        modifier = Modifier.width(96.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = label,
                fontSize = 10.sp,
                fontFamily = FontFamily.Monospace,
                color = Color(0xFF8E8E93),
                fontWeight = FontWeight.Medium
            )
            Text(
                text = value,
                fontSize = 13.sp,
                fontFamily = FontFamily.Monospace,
                color = Color(0xFFFFFFFF),
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun DialCanvas(
    heading: Float,
    pitch: Float,
    roll: Float,
    isSnapped: Boolean,
    showCompass: Boolean
) {
    val snapColor by animateColorAsState(
        targetValue = if (isSnapped) Color(0xFF34C759) else Color(0x3334C759),
        label = "SnapColorAnim"
    )

    Canvas(modifier = Modifier.fillMaxSize()) {
        val center = Offset(size.width / 2f, size.height / 2f)
        val outerRadius = size.width * 0.46f

        drawCircle(
            color = Color(0xFF2C2C2E),
            radius = outerRadius,
            center = center,
            style = Stroke(width = 1.5.dp.toPx())
        )
        drawCircle(
            color = Color(0xFF1C1C1E),
            radius = outerRadius * 0.65f,
            center = center,
            style = Stroke(width = 1.dp.toPx())
        )

        drawLine(
            color = Color(0xFF2C2C2E),
            start = Offset(center.x - 24.dp.toPx(), center.y),
            end = Offset(center.x + 24.dp.toPx(), center.y),
            strokeWidth = 1.dp.toPx()
        )
        drawLine(
            color = Color(0xFF2C2C2E),
            start = Offset(center.x, center.y - 24.dp.toPx()),
            end = Offset(center.x, center.y + 24.dp.toPx()),
            strokeWidth = 1.dp.toPx()
        )

        if (showCompass) {
            val paint = android.graphics.Paint().apply {
                color = android.graphics.Color.parseColor("#8E8E93")
                textSize = 12.sp.toPx()
                textAlign = android.graphics.Paint.Align.CENTER
                isAntiAlias = true
            }

            for (angle in 0 until 360 step 30) {
                val rad = Math.toRadians((angle - heading - 90).toDouble())
                val isMajor = angle % 90 == 0
                val tickLength = if (isMajor) 14.dp.toPx() else 7.dp.toPx()
                val tickColor = when {
                    angle == 0 -> Color(0xFFFF3B30)
                    isMajor -> Color(0xFFFFFFFF)
                    else -> Color(0xFF636366)
                }

                val startX = center.x + (outerRadius - tickLength) * cos(rad).toFloat()
                val startY = center.y + (outerRadius - tickLength) * sin(rad).toFloat()
                val stopX = center.x + outerRadius * cos(rad).toFloat()
                val stopY = center.y + outerRadius * sin(rad).toFloat()

                drawLine(
                    color = tickColor,
                    start = Offset(startX, startY),
                    end = Offset(stopX, stopY),
                    strokeWidth = if (angle == 0) 2.5.dp.toPx() else 1.5.dp.toPx(),
                    cap = StrokeCap.Round
                )

                if (isMajor) {
                    val label = when (angle) {
                        0 -> "N"
                        90 -> "E"
                        180 -> "S"
                        270 -> "W"
                        else -> ""
                    }
                    val textDist = outerRadius - 26.dp.toPx()
                    val textX = center.x + textDist * cos(rad).toFloat()
                    val textY = center.y + textDist * sin(rad).toFloat() + 4.dp.toPx()
                    
                    if (angle == 0) {
                        paint.color = android.graphics.Color.parseColor("#FF3B30")
                    } else {
                        paint.color = android.graphics.Color.parseColor("#8E8E93")
                    }
                    drawContext.canvas.nativeCanvas.drawText(label, textX, textY, paint)
                }
            }
        }

        val targetRadius = 32.dp.toPx()
        drawCircle(
            color = if (isSnapped) Color(0xFF34C759) else Color(0xFF3A3A3C),
            radius = targetRadius,
            center = center,
            style = Stroke(width = if (isSnapped) 2.5.dp.toPx() else 1.5.dp.toPx())
        )

        val maxOffset = outerRadius * 0.55f
        val bubbleX = (center.x + (roll / 45f) * maxOffset).coerceIn(center.x - maxOffset, center.x + maxOffset)
        val bubbleY = (center.y + (pitch / 45f) * maxOffset).coerceIn(center.y - maxOffset, center.y + maxOffset)

        drawCircle(
            color = snapColor,
            radius = 24.dp.toPx(),
            center = Offset(bubbleX, bubbleY)
        )
        drawCircle(
            color = if (isSnapped) Color(0xFF34C759) else Color(0xFFFFFFFF),
            radius = 24.dp.toPx(),
            center = Offset(bubbleX, bubbleY),
            style = Stroke(width = 1.5.dp.toPx())
        )
    }
}

fun getCardinalDirection(heading: Float): String {
    val normalized = (heading % 360 + 360) % 360
    return when {
        normalized >= 337.5 || normalized < 22.5 -> "N"
        normalized < 67.5 -> "NE"
        normalized < 112.5 -> "E"
        normalized < 157.5 -> "SE"
        normalized < 202.5 -> "S"
        normalized < 247.5 -> "SW"
        normalized < 292.5 -> "W"
        else -> "NW"
    }
}
