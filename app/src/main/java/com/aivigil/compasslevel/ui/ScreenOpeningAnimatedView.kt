package com.aivigil.compasslevel.ui

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.SquareFoot
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aivigil.compasslevel.ui.ads.AdmobAdaptiveBannerView
import com.aivigil.compasslevel.ui.theme.SkinPalette
import kotlinx.coroutines.delay
import kotlin.math.cos
import kotlin.math.sin

/**
 * ScreenOpeningAnimatedView: Branded Splash Screen modeled after standard production apps
 * (Referencing modern Android utility apps: Hero Logo + Title + Tagline + Loading Bar + Bottom AdBanner).
 * Once loading completes, triggers onAnimationComplete to immediately launch AdMob Interstitial.
 */
@Composable
fun ScreenOpeningAnimatedView(
    skin: SkinPalette,
    onAnimationComplete: () -> Unit
) {
    var animationStarted by remember { mutableStateOf(false) }

    // ── Smooth Entrance & Progress Animations ────────────────────────
    val heroScale by animateFloatAsState(
        targetValue = if (animationStarted) 1f else 0.72f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "hero_scale"
    )

    val contentAlpha by animateFloatAsState(
        targetValue = if (animationStarted) 1f else 0f,
        animationSpec = tween(durationMillis = 700, easing = FastOutSlowInEasing),
        label = "content_alpha"
    )

    // Needle sweep alignment
    val needleAngle by animateFloatAsState(
        targetValue = if (animationStarted) 0f else -135f,
        animationSpec = spring(dampingRatio = 0.55f, stiffness = Spring.StiffnessLow),
        label = "needle_angle"
    )

    // Animated Loading Progress Bar (0% -> 100% over 2.9 seconds)
    val progressAnim by animateFloatAsState(
        targetValue = if (animationStarted) 1f else 0f,
        animationSpec = tween(durationMillis = 2900, delayMillis = 150, easing = LinearOutSlowInEasing),
        label = "progress_anim"
    )

    // Subtle ambient pulsing glow behind icon
    val infiniteTransition = rememberInfiniteTransition(label = "ambient_glow")
    val pulseGlow by infiniteTransition.animateFloat(
        initialValue = 0.35f,
        targetValue = 0.75f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_glow"
    )

    val ringRotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(12000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "ring_rotation"
    )

    LaunchedEffect(Unit) {
        animationStarted = true
        // Allow loading bar to reach 100%, then trigger interstitial ad
        delay(3200L)
        onAnimationComplete()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF070B10),
                        Color(0xFF0F1722),
                        Color(0xFF070B10)
                    )
                )
            )
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) {
                // Allow fast tap-through to proceed
                onAnimationComplete()
            }
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            // ═════════════════════════════════════════════════════════════
            // ── TOP SPACER (Keeps hero art centered & uncluttered) ──────
            // ═════════════════════════════════════════════════════════════
            Spacer(modifier = Modifier.height(24.dp))

            // ═════════════════════════════════════════════════════════════
            // ── CENTER HERO SECTION (Like the reference video) ──────────
            // ═════════════════════════════════════════════════════════════
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(horizontal = 28.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {

                // ── 1. Hero App Icon Card with Glowing Border & Ambient Halo ──
                Box(
                    modifier = Modifier
                        .size(130.dp)
                        .scale(heroScale)
                        .alpha(contentAlpha),
                    contentAlignment = Alignment.Center
                ) {
                    // Ambient radial glow behind the card
                    Box(
                        modifier = Modifier
                            .size(180.dp)
                            .background(
                                Brush.radialGradient(
                                    colors = listOf(
                                        skin.primaryAccent.copy(alpha = 0.38f * pulseGlow),
                                        Color.Transparent
                                    )
                                )
                            )
                    )

                    // Rounded App Icon Card
                    Box(
                        modifier = Modifier
                            .size(110.dp)
                            .shadow(16.dp, RoundedCornerShape(26.dp), spotColor = skin.primaryAccent)
                            .clip(RoundedCornerShape(26.dp))
                            .background(
                                Brush.linearGradient(
                                    colors = listOf(
                                        Color(0xFF1B2433),
                                        Color(0xFF0D131C)
                                    )
                                )
                            )
                            .border(
                                width = 1.5.dp,
                                brush = Brush.linearGradient(
                                    colors = listOf(
                                        skin.primaryAccent.copy(alpha = 0.8f),
                                        skin.primaryAccent.copy(alpha = 0.2f),
                                        skin.primaryAccent.copy(alpha = 0.6f)
                                    )
                                ),
                                shape = RoundedCornerShape(26.dp)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        // High-tech Compass & Gyroscope dial graphic inside logo
                        Canvas(modifier = Modifier.size(90.dp)) {
                            val center = Offset(size.width / 2f, size.height / 2f)
                            val r = size.width / 2f - 4.dp.toPx()

                            // Rotating tick ring
                            rotate(ringRotation, pivot = center) {
                                for (deg in 0 until 360 step 30) {
                                    val rad = Math.toRadians(deg.toDouble())
                                    val isCard = deg % 90 == 0
                                    val len = if (isCard) 6.dp.toPx() else 3.dp.toPx()
                                    val col = if (isCard) skin.primaryAccent else skin.textSecondary.copy(alpha = 0.4f)
                                    drawLine(
                                        color = col,
                                        start = Offset(
                                            (center.x + (r - len) * cos(rad)).toFloat(),
                                            (center.y + (r - len) * sin(rad)).toFloat()
                                        ),
                                        end = Offset(
                                            (center.x + r * cos(rad)).toFloat(),
                                            (center.y + r * sin(rad)).toFloat()
                                        ),
                                        strokeWidth = if (isCard) 1.5.dp.toPx() else 1.dp.toPx(),
                                        cap = StrokeCap.Round
                                    )
                                }
                            }

                            // Thin guide circle
                            drawCircle(
                                color = skin.primaryAccent.copy(alpha = 0.35f),
                                radius = r - 10.dp.toPx(),
                                style = Stroke(width = 1.dp.toPx())
                            )

                            // Dynamic compass needle
                            rotate(needleAngle, pivot = center) {
                                val needleW = 4.5.dp.toPx()
                                val needleL = r - 16.dp.toPx()

                                // North needle (Ruby Red)
                                val northPath = Path().apply {
                                    moveTo(center.x, center.y - needleL)
                                    lineTo(center.x + needleW, center.y)
                                    lineTo(center.x - needleW, center.y)
                                    close()
                                }
                                drawPath(northPath, color = Color(0xFFFF3B30))

                                // South needle (Silver)
                                val southPath = Path().apply {
                                    moveTo(center.x, center.y + needleL)
                                    lineTo(center.x - needleW, center.y)
                                    lineTo(center.x + needleW, center.y)
                                    close()
                                }
                                drawPath(southPath, color = Color(0xFFB0BEC5))
                            }

                            // Center pivot pin
                            drawCircle(color = Color(0xFF0D131C), radius = 6.dp.toPx(), center = center)
                            drawCircle(color = skin.primaryAccent, radius = 3.dp.toPx(), center = center)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(22.dp))

                // ── 2. App Name & Title (Like "VR Player" in video) ───────────
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.alpha(contentAlpha)
                ) {
                    Text(
                        text = "Compass Level",
                        color = Color.White,
                        fontSize = 28.sp,
                        fontWeight = FontWeight.ExtraBold,
                        fontFamily = FontFamily.Default,
                        letterSpacing = 0.5.sp
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // ── 3. Tagline / Welcome Description (Matching reference video) ──
                    Text(
                        text = "Welcome to Compass Level – Precision Dual-Axis Level & 3D Magnetic Compass",
                        color = Color.White.copy(alpha = 0.72f),
                        fontSize = 13.5.sp,
                        lineHeight = 19.sp,
                        fontWeight = FontWeight.Normal,
                        textAlign = TextAlign.Center
                    )
                }

                Spacer(modifier = Modifier.height(34.dp))

                // ── 4. Sleek Progress Bar & Status (Matching reference video) ──
                Column(
                    modifier = Modifier
                        .fillMaxWidth(0.85f)
                        .alpha(contentAlpha),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Modern slim track
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(4.dp)
                            .clip(RoundedCornerShape(2.dp))
                            .background(Color.White.copy(alpha = 0.12f))
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(fraction = progressAnim)
                                .fillMaxHeight()
                                .clip(RoundedCornerShape(2.dp))
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

                    // Status and percentage
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (progressAnim >= 0.98f) "Ready! Starting..." else "Loading resources...",
                            color = Color.White.copy(alpha = 0.55f),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium
                        )

                        Text(
                            text = "${(progressAnim * 100).toInt()}%",
                            color = skin.primaryAccent,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            // ═════════════════════════════════════════════════════════════
            // ── BOTTOM ADMOB ADAPTIVE BANNER (Matching reference video) ─
            // ═════════════════════════════════════════════════════════════
            AdmobAdaptiveBannerView(skin = skin)
        }
    }
}
