package com.aivigil.compasslevel.ui

import android.Manifest
import android.content.Context
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.aivigil.compasslevel.ui.theme.*
import java.util.Locale
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.tan

@Composable
fun ScreenClinometerView(
    pitch: Float,
    roll: Float,
    isLocked: Boolean,
    onLockToggle: () -> Unit,
    onSaveNoteClick: (pitch: Float, slope: Double) -> Unit,
    onFlashlightToggle: () -> Unit,
    isFlashlightOn: Boolean,
    skin: SkinPalette
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    var hasCameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.CAMERA
            ) == android.content.pm.PackageManager.PERMISSION_GRANTED
        )
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        hasCameraPermission = granted
    }

    val slopePercent = abs(tan(Math.toRadians(pitch.coerceIn(-89.9f, 89.9f).toDouble())) * 100.0)

    Box(modifier = Modifier.fillMaxSize()) {
        if (hasCameraPermission) {
            // CameraX Live Preview
            AndroidView(
                factory = { ctx ->
                    val previewView = PreviewView(ctx).apply {
                        scaleType = PreviewView.ScaleType.FILL_CENTER
                    }
                    val cameraProviderFuture = ProcessCameraProvider.getInstance(ctx)
                    cameraProviderFuture.addListener({
                        val cameraProvider = cameraProviderFuture.get()
                        val preview = Preview.Builder().build().also {
                            it.setSurfaceProvider(previewView.surfaceProvider)
                        }
                        val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA
                        try {
                            cameraProvider.unbindAll()
                            cameraProvider.bindToLifecycle(
                                lifecycleOwner,
                                cameraSelector,
                                preview
                            )
                        } catch (e: Exception) {
                            // Camera bind error
                        }
                    }, ContextCompat.getMainExecutor(ctx))
                    previewView
                },
                modifier = Modifier.fillMaxSize()
            )

            // AR Overlay HUD Canvas
            Canvas(modifier = Modifier.fillMaxSize()) {
                val cx = size.width / 2f
                val cy = size.height / 2f

                // Central Crosshair
                val crossSize = 28.dp.toPx()
                drawLine(
                    color = skin.primaryAccent,
                    start = Offset(cx - crossSize, cy),
                    end = Offset(cx + crossSize, cy),
                    strokeWidth = 2.dp.toPx(),
                    cap = StrokeCap.Round
                )
                drawLine(
                    color = skin.primaryAccent,
                    start = Offset(cx, cy - crossSize),
                    end = Offset(cx, cy + crossSize),
                    strokeWidth = 2.dp.toPx(),
                    cap = StrokeCap.Round
                )
                drawCircle(
                    color = skin.primaryAccent,
                    radius = 8.dp.toPx(),
                    center = Offset(cx, cy),
                    style = Stroke(1.5.dp.toPx())
                )

                // Artificial Horizon Line
                // Pitch displaces vertical offset, Roll rotates angle
                val rollRad = Math.toRadians(-roll.toDouble())
                val pitchOffsetPx = (pitch * 12.dp.toPx()).coerceIn(-cy * 0.7f, cy * 0.7f)
                val lineLength = size.width * 0.75f

                val dx = (lineLength / 2f * cos(rollRad)).toFloat()
                val dy = (lineLength / 2f * sin(rollRad)).toFloat()

                val lineCenterY = cy + pitchOffsetPx

                // Horizon Main Line
                drawLine(
                    color = if (abs(pitch) <= 0.5f) NeonEmerald else Color(0xFFFFD700),
                    start = Offset(cx - dx, lineCenterY - dy),
                    end = Offset(cx + dx, lineCenterY + dy),
                    strokeWidth = 3.dp.toPx(),
                    cap = StrokeCap.Round
                )

                // Horizon pitch tick marks
                val ladderSteps = listOf(-30, -20, -10, 10, 20, 30)
                ladderSteps.forEach { stepDeg ->
                    val stepOffset = (stepDeg * 10.dp.toPx())
                    val stepY = lineCenterY + stepOffset
                    if (stepY in 80f..(size.height - 80f)) {
                        val tickW = 32.dp.toPx()
                        drawLine(
                            color = Color.White.copy(alpha = 0.45f),
                            start = Offset(cx - tickW, stepY),
                            end = Offset(cx + tickW, stepY),
                            strokeWidth = 1.dp.toPx()
                        )
                    }
                }
            }

            // Top HUD Status Bar
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp, start = 16.dp, end = 16.dp)
                    .align(Alignment.TopCenter),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = skin.cardBackground.copy(alpha = 0.85f)),
                    border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(skin.cardBorder))
                ) {
                    Column(
                        modifier = Modifier.padding(horizontal = 24.dp, vertical = 12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = String.format(Locale.US, "%+.1f°", pitch),
                                fontSize = 48.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (abs(pitch) <= 0.5f) skin.primaryAccent else Color(0xFFFFD700),
                                fontFamily = FontFamily.Default
                            )
                        }

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "SLOPE: ${String.format(Locale.US, "%.1f", slopePercent)}%",
                                color = skin.textPrimary,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Default
                            )
                            Text(
                                text = "ROLL: ${String.format(Locale.US, "%+.1f°", roll)}",
                                color = skin.textSecondary,
                                fontSize = 12.sp,
                                fontFamily = FontFamily.Default
                            )
                        }

                        if (isLocked) {
                            Spacer(modifier = Modifier.height(4.dp))
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(RedAccent.copy(alpha = 0.25f))
                                    .padding(horizontal = 8.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = "MEASUREMENT LOCKED",
                                    color = RedAccent,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Default
                                )
                            }
                        }
                    }
                }
            }

            // Bottom Floating Controls
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 24.dp, start = 24.dp, end = 24.dp)
                    .align(Alignment.BottomCenter),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Flashlight Toggle
                FloatingActionButton(
                    onClick = onFlashlightToggle,
                    containerColor = if (isFlashlightOn) Color(0xFFFFD700) else skin.cardBackground.copy(alpha = 0.85f),
                    contentColor = if (isFlashlightOn) PureBlack else skin.textPrimary,
                    shape = CircleShape,
                    modifier = Modifier
                        .size(54.dp)
                        .border(1.dp, if (isFlashlightOn) Color(0xFFFFD700) else skin.cardBorder, CircleShape)
                ) {
                    Icon(
                        imageVector = if (isFlashlightOn) Icons.Default.FlashlightOn else Icons.Default.FlashlightOff,
                        contentDescription = "Torch",
                        modifier = Modifier.size(24.dp)
                    )
                }

                // Lock Angle Button
                FloatingActionButton(
                    onClick = onLockToggle,
                    containerColor = if (isLocked) RedAccent else skin.primaryAccent,
                    contentColor = PureBlack,
                    shape = CircleShape,
                    modifier = Modifier.size(64.dp)
                ) {
                    Icon(
                        imageVector = if (isLocked) Icons.Default.Lock else Icons.Default.LockOpen,
                        contentDescription = "Lock",
                        modifier = Modifier.size(28.dp)
                    )
                }

                // Save Note Button
                FloatingActionButton(
                    onClick = { onSaveNoteClick(pitch, slopePercent) },
                    containerColor = skin.cardBackground.copy(alpha = 0.85f),
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

        } else {
            // Permission Request State
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .clip(CircleShape)
                        .background(CardBackground)
                        .border(1.dp, BorderStrong, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.CameraAlt,
                        contentDescription = null,
                        tint = skin.primaryAccent,
                        modifier = Modifier.size(36.dp)
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    text = "CAMERA PERMISSION NEEDED",
                    color = TextPrimary,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Default,
                    letterSpacing = 0.5.sp
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "The Clinometer uses the live camera feed to project real-time slope and pitch horizon lines onto target objects.",
                    color = TextSecondary,
                    fontSize = 13.sp,
                    fontFamily = FontFamily.Default,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                    lineHeight = 18.sp
                )

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = { permissionLauncher.launch(Manifest.permission.CAMERA) },
                    colors = ButtonDefaults.buttonColors(containerColor = skin.primaryAccent),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = "ENABLE CAMERA",
                        color = PureBlack,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Default
                    )
                }
            }
        }
    }
}

@Composable
fun ScreenClinometerView(
    pitch: Float,
    roll: Float,
    isLocked: Boolean,
    onLockToggle: () -> Unit,
    onSaveNoteClick: (pitch: Float, slope: Double) -> Unit,
    onFlashlightToggle: () -> Unit,
    isFlashlightOn: Boolean,
    skin: AppSkin = AppSkin.CLASSIC_EMERALD
) {
    ScreenClinometerView(
        pitch = pitch,
        roll = roll,
        isLocked = isLocked,
        onLockToggle = onLockToggle,
        onSaveNoteClick = onSaveNoteClick,
        onFlashlightToggle = onFlashlightToggle,
        isFlashlightOn = isFlashlightOn,
        skin = skin.palette(true)
    )
}
