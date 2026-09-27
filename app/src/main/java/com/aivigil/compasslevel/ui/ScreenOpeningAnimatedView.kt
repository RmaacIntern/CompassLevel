package com.aivigil.compasslevel.ui

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aivigil.compasslevel.ui.ads.AdmobAdaptiveBannerView
import com.aivigil.compasslevel.ui.theme.SkinPalette
import kotlinx.coroutines.delay
import kotlin.math.cos
import kotlin.math.sin

/**
 * ScreenOpeningAnimatedView: Loading Splash Screen with real Google AdMob banners.
 * Features:
 * - Top AdMob Adaptive Banner (real Google AdMob ad)
 * - Central App Branding with rotating precision compass dial & "App Loading..."
 * - Animated loading progress bar (0% -> 100%)
 * - Bottom AdMob Adaptive Banner (real Google AdMob ad)
 * - Seamless transition to main introductory screen upon load or skip
 */
@Composable
fun ScreenOpeningAnimatedView(
    skin: SkinPalette,
    onAnimationComplete: () -> Unit
) {
    var animationStarted by remember { mutableStateOf(false) }

    // Compass Dial Entrance Animation
    val dialScale by animateFloatAsState(
        targetValue = if (animationStarted) 1.0f else 0.70f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "dial_scale"
    )

    val dialAlpha by animateFloatAsState(
        targetValue = if (animationStarted) 1.0f else 0.0f,
        animationSpec = tween(durationMillis = 600, easing = FastOutSlowInEasing),
        label = "dial_alpha"
    )

    // Needle Sweep & Alignment to True North
    val needleRotation by animateFloatAsState(
        targetValue = if (animationStarted) 0.0f else -140f,
        animationSpec = spring(
            dampingRatio = 0.58f,
            stiffness = Spring.StiffnessLow
        ),
        label = "needle_rotation"
    )

    // Text & Branding Entrance
    val textAlpha by animateFloatAsState(
        targetValue = if (animationStarted) 1.0f else 0.0f,
        animationSpec = tween(durationMillis = 600, delayMillis = 200, easing = FastOutSlowInEasing),
        label = "text_alpha"
    )

    // Loading Progress Fill (0% -> 100% over 2.8 seconds)
    val progressAnim by animateFloatAsState(
        targetValue = if (animationStarted) 1.0f else 0.0f,
        animationSpec = tween(durationMillis = 2800, delayMillis = 150, easing = LinearOutSlowInEasing),
        label = "progress_anim"
    )

    // Ambient Glowing Pulsing
    val infiniteTransition = rememberInfiniteTransition(label = "ambient_glow")
    val glowPulse by infiniteTransition.animateFloat(
        initialValue = 0.25f,
        targetValue = 0.55f,
        animationSpec = infiniteRepeatable(tween(1400, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "pulse"
    )

    LaunchedEffect(Unit) {
        animationStarted = true
        delay(3200L)
        onAnimationComplete()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(skin.appBackground)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) {
                onAnimationComplete()
            }
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(vertical = 8.dp),
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // ═══════════════════════════════════════════════════════════
            // ── 1. TOP — REAL GOOGLE ADMOB ADAPTIVE BANNER ──────────────
            // ═══════════════════════════════════════════════════════════
            AdmobAdaptiveBannerView(skin = skin)

            // ═══════════════════════════════════════════════════════════
            // ── 2. CENTER — LOADING & COMPASS BRANDING ───────────────────
            // ═══════════════════════════════════════════════════════════
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 12.dp)
            ) {
                // Compass Dial
                Box(
                    modifier = Modifier
                        .size(170.dp)
                        .scale(dialScale)
                        .alpha(dialAlpha),
                    contentAlignment = Alignment.Center
                ) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val center = Offset(size.width / 2, size.height / 2)
                        val outerRadius = size.width / 2 - 4.dp.toPx()
                        val innerRadius = outerRadius - 12.dp.toPx()

                        // Ambient Glow
                        drawCircle(
                            brush = Brush.radialGradient(
                                colors = listOf(
                                    skin.primaryAccent.copy(alpha = 0.35f * glowPulse),
                                    Color.Transparent
                                ),
                                center = center,
                                radius = size.width / 1.5f
                            )
                        )

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

                        // Degree Ticks
                        for (deg in 0 until 360 step 30) {
                            val rad = Math.toRadians(deg.toDouble())
                            val isCardinal = deg % 90 == 0
                            val tickLen = if (isCardinal) 10.dp.toPx() else 5.dp.toPx()
                            val tickColor = if (isCardinal) skin.primaryAccent else skin.textSecondary.copy(alpha = 0.5f)
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

                        // Rotating Needle
                        rotate(needleRotation, pivot = center) {
                            val needleHalfWidth = 6.dp.toPx()
                            val needleLength = innerRadius - 14.dp.toPx()

                            val northPath = Path().apply {
                                moveTo(center.x, center.y - needleLength)
                                lineTo(center.x + needleHalfWidth, center.y)
                                lineTo(center.x, center.y - 3.dp.toPx())
                                close()
                            }
                            drawPath(northPath, color = Color(0xFFFF3B30))

                            val southPath = Path().apply {
                                moveTo(center.x, center.y + needleLength)
                                lineTo(center.x - needleHalfWidth, center.y)
                                lineTo(center.x, center.y + 3.dp.toPx())
                                close()
                            }
                            drawPath(southPath, color = skin.textSecondary)
                        }

                        // Center Pivot Hub
                        drawCircle(color = skin.appBackground, radius = 9.dp.toPx(), center = center)
                        drawCircle(color = skin.primaryAccent, radius = 5.dp.toPx(), center = center)
                    }
                }

                // App Title & Loading Text
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(3.dp),
                    modifier = Modifier.alpha(textAlpha)
                ) {
                    Text(
                        text = "COMPASS PRO",
                        color = skin.textPrimary,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Black,
                        fontFamily = FontFamily.Default,
                        letterSpacing = 1.sp
                    )

                    Text(
                        text = "App Loading...",
                        color = skin.primaryAccent,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.5.sp
                    )
                }

                // Loading Progress Bar
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier
                        .width(220.dp)
                        .alpha(textAlpha)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(5.dp)
                            .clip(RoundedCornerShape(3.dp))
                            .background(skin.cardElevated)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(fraction = progressAnim)
                                .fillMaxHeight()
                                .clip(RoundedCornerShape(3.dp))
                                .background(
                                    Brush.horizontalGradient(
                                        colors = listOf(
                                            skin.primaryAccent.copy(alpha = 0.6f),
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
                            text = if (progressAnim >= 0.98f) "Ready! Starting..." else "Loading Resources...",
                            color = skin.textSecondary,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = "${(progressAnim * 100).toInt()}%",
                            color = skin.textPrimary,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Text(
                    text = "Tap anywhere to continue",
                    color = skin.textSecondary.copy(alpha = 0.45f),
                    fontSize = 10.sp,
                    modifier = Modifier.alpha(textAlpha)
                )
            }

            // ═══════════════════════════════════════════════════════════
            // ── 3. BOTTOM — REAL GOOGLE ADMOB ADAPTIVE BANNER ──────────
            // ═══════════════════════════════════════════════════════════
            AdmobAdaptiveBannerView(skin = skin)
        }
    }
}
