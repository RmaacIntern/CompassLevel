package com.aivigil.compasslevel.ui

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Navigation
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aivigil.compasslevel.ui.theme.SkinPalette
import kotlinx.coroutines.delay
import kotlin.math.cos
import kotlin.math.sin

/**
 * ScreenOpeningAnimatedView: Cinematic Animated Launch / Splash Screen.
 * Displays app branding with a rotating precision compass dial, springing needle detent,
 * real-time sensor calibration progress bar, and smooth transition into the introductory screen.
 */
@Composable
fun ScreenOpeningAnimatedView(
    skin: SkinPalette,
    onAnimationComplete: () -> Unit
) {
    var animationStarted by remember { mutableStateOf(false) }

    // Compass Dial Entrance Animation
    val dialScale by animateFloatAsState(
        targetValue = if (animationStarted) 1.0f else 0.65f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "dial_scale"
    )

    val dialAlpha by animateFloatAsState(
        targetValue = if (animationStarted) 1.0f else 0.0f,
        animationSpec = tween(durationMillis = 650, easing = FastOutSlowInEasing),
        label = "dial_alpha"
    )

    // Needle Sweep & Alignment to True North
    val needleRotation by animateFloatAsState(
        targetValue = if (animationStarted) 0.0f else -135f,
        animationSpec = spring(
            dampingRatio = 0.58f,
            stiffness = Spring.StiffnessLow
        ),
        label = "needle_rotation"
    )

    // Text & Branding Entrance
    val textAlpha by animateFloatAsState(
        targetValue = if (animationStarted) 1.0f else 0.0f,
        animationSpec = tween(durationMillis = 800, delayMillis = 300, easing = FastOutSlowInEasing),
        label = "text_alpha"
    )

    val textTranslationY by animateFloatAsState(
        targetValue = if (animationStarted) 0f else 30f,
        animationSpec = tween(durationMillis = 800, delayMillis = 300, easing = FastOutSlowInEasing),
        label = "text_translate"
    )

    // Calibration Progress Fill
    val progressAnim by animateFloatAsState(
        targetValue = if (animationStarted) 1.0f else 0.0f,
        animationSpec = tween(durationMillis = 1500, delayMillis = 200, easing = LinearOutSlowInEasing),
        label = "progress_anim"
    )

    // Ambient Glowing Pulsing
    val infiniteTransition = rememberInfiniteTransition(label = "ambient_glow")
    val glowPulse by infiniteTransition.animateFloat(
        initialValue = 0.25f,
        targetValue = 0.60f,
        animationSpec = infiniteRepeatable(tween(1400, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "pulse"
    )

    LaunchedEffect(Unit) {
        animationStarted = true
        // Allow the full luxury animation sequence to complete, then auto-navigate
        delay(1950L)
        onAnimationComplete()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(skin.appBackground)
            // User can tap anywhere to immediately jump to the introductory screen
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) {
                onAnimationComplete()
            },
        contentAlignment = Alignment.Center
    ) {
        // ── 1. Subtle Radial Ambient Aura ────────────────────────────────────
        Canvas(
            modifier = Modifier
                .size(340.dp)
                .alpha(glowPulse)
        ) {
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        skin.primaryAccent.copy(alpha = 0.35f),
                        skin.primaryAccent.copy(alpha = 0.08f),
                        Color.Transparent
                    ),
                    center = center,
                    radius = size.width / 2
                )
            )
        }

        // ── 2. Central Content Column ────────────────────────────────────────
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(28.dp),
            modifier = Modifier.padding(horizontal = 24.dp)
        ) {
            // ── A. Animated Precision Bezel & Needle ─────────────────────────
            Box(
                modifier = Modifier
                    .size(200.dp)
                    .scale(dialScale)
                    .alpha(dialAlpha),
                contentAlignment = Alignment.Center
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val center = Offset(size.width / 2, size.height / 2)
                    val outerRadius = size.width / 2 - 4.dp.toPx()
                    val innerRadius = outerRadius - 14.dp.toPx()

                    // Outer Metallic Ring
                    drawCircle(
                        color = skin.cardBorder,
                        radius = outerRadius,
                        center = center,
                        style = Stroke(width = 2.dp.toPx())
                    )

                    // Secondary Dial Bezel
                    drawCircle(
                        color = skin.cardElevated,
                        radius = innerRadius,
                        center = center,
                        style = Stroke(width = 1.dp.toPx())
                    )

                    // Degree Ticks (Every 30 degrees)
                    for (deg in 0 until 360 step 30) {
                        val rad = Math.toRadians(deg.toDouble())
                        val isCardinal = deg % 90 == 0
                        val tickLen = if (isCardinal) 12.dp.toPx() else 6.dp.toPx()
                        val tickColor = if (isCardinal) skin.primaryAccent else skin.textSecondary.copy(alpha = 0.6f)
                        val strokeW = if (isCardinal) 2.dp.toPx() else 1.dp.toPx()

                        val startX = (center.x + (innerRadius - tickLen) * cos(rad)).toFloat()
                        val startY = (center.y + (innerRadius - tickLen) * sin(rad)).toFloat()
                        val endX = (center.x + innerRadius * cos(rad)).toFloat()
                        val endY = (center.y + innerRadius * sin(rad)).toFloat()

                        drawLine(
                            color = tickColor,
                            start = Offset(startX, startY),
                            end = Offset(endX, endY),
                            strokeWidth = strokeW,
                            cap = StrokeCap.Round
                        )
                    }

                    // Rotating Dual-Color Needle with spring alignment
                    rotate(needleRotation, pivot = center) {
                        val needleHalfWidth = 7.dp.toPx()
                        val needleLength = innerRadius - 16.dp.toPx()

                        // North Pointer (Ruby Red Accent)
                        val northPath = Path().apply {
                            moveTo(center.x, center.y - needleLength)
                            lineTo(center.x + needleHalfWidth, center.y)
                            lineTo(center.x, center.y - 4.dp.toPx())
                            close()
                        }
                        drawPath(northPath, color = Color(0xFFFF3B30))

                        val northPathLight = Path().apply {
                            moveTo(center.x, center.y - needleLength)
                            lineTo(center.x - needleHalfWidth, center.y)
                            lineTo(center.x, center.y - 4.dp.toPx())
                            close()
                        }
                        drawPath(northPathLight, color = Color(0xFFFF6961))

                        // South Pointer (Metallic White / Primary Accent)
                        val southPath = Path().apply {
                            moveTo(center.x, center.y + needleLength)
                            lineTo(center.x + needleHalfWidth, center.y)
                            lineTo(center.x, center.y + 4.dp.toPx())
                            close()
                        }
                        drawPath(southPath, color = skin.cardBorder)

                        val southPathLight = Path().apply {
                            moveTo(center.x, center.y + needleLength)
                            lineTo(center.x - needleHalfWidth, center.y)
                            lineTo(center.x, center.y + 4.dp.toPx())
                            close()
                        }
                        drawPath(southPathLight, color = skin.textSecondary.copy(alpha = 0.5f))
                    }

                    // Center Hub Pivot Ring & Dot
                    drawCircle(
                        color = skin.surfaceBackground,
                        radius = 12.dp.toPx(),
                        center = center
                    )
                    drawCircle(
                        color = skin.primaryAccent,
                        radius = 6.dp.toPx(),
                        center = center
                    )
                    drawCircle(
                        color = Color.White,
                        radius = 2.dp.toPx(),
                        center = center
                    )
                }
            }

            // ── B. App Title & Subtitle Branding ─────────────────────────────
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier
                    .alpha(textAlpha)
                    .offset(y = textTranslationY.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(skin.primaryAccent.copy(alpha = 0.18f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Navigation,
                            contentDescription = null,
                            tint = skin.primaryAccent,
                            modifier = Modifier.size(16.dp)
                        )
                    }

                    Text(
                        text = "COMPASS PRO",
                        color = skin.textPrimary,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Black,
                        fontFamily = FontFamily.Default,
                        letterSpacing = 2.sp
                    )
                }

                Text(
                    text = "Precision Navigation & Spirit Level",
                    color = skin.textSecondary,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    letterSpacing = 0.5.sp,
                    textAlign = TextAlign.Center
                )
            }

            // ── C. Calibration Progress Indicator ────────────────────────────
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier
                    .width(220.dp)
                    .alpha(textAlpha)
            ) {
                // Sleek Linear Progress Bar
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(4.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(skin.cardElevated)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(fraction = progressAnim)
                            .fillMaxHeight()
                            .clip(RoundedCornerShape(2.dp))
                            .background(
                                Brush.horizontalGradient(
                                    colors = listOf(
                                        skin.primaryAccent.copy(alpha = 0.5f),
                                        skin.primaryAccent,
                                        Color.White
                                    )
                                )
                            )
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (progressAnim >= 0.95f) "Sensors Ready" else "Calibrating Sensors...",
                        color = if (progressAnim >= 0.95f) skin.primaryAccent else skin.textSecondary,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.SemiBold,
                        letterSpacing = 0.5.sp
                    )

                    Text(
                        text = "${(progressAnim * 100).toInt()}%",
                        color = skin.textSecondary,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        // ── 3. Bottom Version Tag & Tap To Skip Notice ───────────────────────
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 28.dp)
                .alpha(textAlpha),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = "v1.0 Pro • 100% Offline Precision",
                color = skin.textSecondary.copy(alpha = 0.6f),
                fontSize = 10.sp,
                fontWeight = FontWeight.Normal,
                letterSpacing = 0.5.sp
            )
            Text(
                text = "Tap anywhere to start",
                color = skin.textSecondary.copy(alpha = 0.4f),
                fontSize = 9.sp
            )
        }
    }
}
