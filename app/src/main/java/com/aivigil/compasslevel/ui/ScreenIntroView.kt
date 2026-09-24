package com.aivigil.compasslevel.ui

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aivigil.compasslevel.ui.theme.*

/**
 * ScreenIntroView: Introductory / Main Start Screen shown on launch.
 * Displays app branding, hardware sensor readiness, feature tool previews,
 * and a dedicated monetization ad slot ready for Google AdMob insertion.
 */
@Composable
fun ScreenIntroView(
    hasMagnetometer: Boolean = true,
    hasLocationPermission: Boolean = false,
    skin: SkinPalette,
    onStartTool: (mode: String) -> Unit,
    adSlot: (@Composable () -> Unit)? = null
) {
    val haptic = LocalHapticFeedback.current
    val scrollState = rememberScrollState()

    // Smooth subtle ambient pulse for the compass logo
    val infiniteTransition = rememberInfiniteTransition(label = "intro_rotation")
    val pulseGlow by infiniteTransition.animateFloat(
        initialValue = 0.25f,
        targetValue = 0.65f,
        animationSpec = infiniteRepeatable(tween(2200, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "pulse_glow"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(skin.appBackground)
            .statusBarsPadding()
            .navigationBarsPadding()
            .verticalScroll(scrollState)
            .padding(horizontal = 20.dp, vertical = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        Spacer(modifier = Modifier.height(8.dp))

        // ── 1. Branded Instrument Icon / Graphic ─────────────────────────────
        Box(
            modifier = Modifier
                .size(130.dp),
            contentAlignment = Alignment.Center
        ) {
            // Ambient Radial Glow - Rendered on GPU layer to eliminate GC churn and frame drops
            val glowBrush = remember(skin.primaryAccent) {
                Brush.radialGradient(
                    colors = listOf(
                        skin.primaryAccent.copy(alpha = 0.45f),
                        Color.Transparent
                    )
                )
            }
            Canvas(
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer {
                        alpha = pulseGlow
                    }
            ) {
                drawCircle(
                    brush = glowBrush,
                    radius = size.width / 2f,
                    center = center
                )
            }

            // High Precision Bezel & Compass Needle
            Canvas(
                modifier = Modifier
                    .size(108.dp)
                    .clip(CircleShape)
            ) {
                val cx = size.width / 2f
                val cy = size.height / 2f
                val r = size.width / 2f - 4.dp.toPx()

                // Bezel outer ring
                drawCircle(
                    color = skin.dialOuterBezel,
                    radius = r + 2.dp.toPx(),
                    center = Offset(cx, cy)
                )
                drawCircle(
                    color = skin.dialBackground,
                    radius = r,
                    center = Offset(cx, cy)
                )
                drawCircle(
                    color = skin.ringBorder,
                    radius = r,
                    center = Offset(cx, cy),
                    style = Stroke(2.dp.toPx())
                )

                // Concentric guide circle
                drawCircle(
                    color = skin.ringBorder.copy(alpha = 0.5f),
                    radius = r * 0.6f,
                    center = Offset(cx, cy),
                    style = Stroke(1.dp.toPx())
                )

                // 12 Outer Ticks
                for (i in 0 until 12) {
                    val angleRad = Math.toRadians((i * 30.0) - 90.0)
                    val isCardinal = (i % 3 == 0)
                    val tickLen = if (isCardinal) 10.dp.toPx() else 6.dp.toPx()
                    val startX = cx + (r - tickLen) * kotlin.math.cos(angleRad).toFloat()
                    val startY = cy + (r - tickLen) * kotlin.math.sin(angleRad).toFloat()
                    val endX = cx + r * kotlin.math.cos(angleRad).toFloat()
                    val endY = cy + r * kotlin.math.sin(angleRad).toFloat()

                    drawLine(
                        color = if (isCardinal) skin.primaryAccent else skin.secondaryAccent.copy(alpha = 0.5f),
                        start = Offset(startX, startY),
                        end = Offset(endX, endY),
                        strokeWidth = if (isCardinal) 2.dp.toPx() else 1.dp.toPx()
                    )
                }

                // Precision North-South Needle
                val needleLen = r * 0.72f
                val needleW = 7.dp.toPx()

                // North Arrow (Red / Accent)
                val northPath = Path().apply {
                    moveTo(cx, cy - needleLen)
                    lineTo(cx + needleW, cy)
                    lineTo(cx, cy - 4.dp.toPx())
                    close()
                }
                drawPath(northPath, RedAccent)

                val northPathL = Path().apply {
                    moveTo(cx, cy - needleLen)
                    lineTo(cx - needleW, cy)
                    lineTo(cx, cy - 4.dp.toPx())
                    close()
                }
                drawPath(northPathL, RedAccent.copy(alpha = 0.85f))

                // South Arrow (Silver / White)
                val southPath = Path().apply {
                    moveTo(cx, cy + needleLen)
                    lineTo(cx + needleW, cy)
                    lineTo(cx, cy + 4.dp.toPx())
                    close()
                }
                drawPath(southPath, Color(0xFFDDDDDD))

                val southPathL = Path().apply {
                    moveTo(cx, cy + needleLen)
                    lineTo(cx - needleW, cy)
                    lineTo(cx, cy + 4.dp.toPx())
                    close()
                }
                drawPath(southPathL, Color(0xFF999999))

                // Center Pivot Cap
                drawCircle(color = PureBlack, radius = 5.dp.toPx(), center = Offset(cx, cy))
                drawCircle(color = skin.primaryAccent, radius = 3.dp.toPx(), center = Offset(cx, cy))
            }
        }

        // ── 2. App Name & Typography ─────────────────────────────────────────
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(
                text = "COMPASS & CLINOMETER",
                color = skin.textPrimary,
                fontSize = 19.sp,
                fontWeight = FontWeight.ExtraBold,
                fontFamily = FontFamily.Default,
                letterSpacing = 0.5.sp,
                textAlign = TextAlign.Center,
                maxLines = 1
            )
            Text(
                text = "Professional Precision Level & Navigation Suite",
                color = skin.textSecondary,
                fontSize = 12.sp,
                fontWeight = FontWeight.Normal,
                fontFamily = FontFamily.Default,
                textAlign = TextAlign.Center,
                lineHeight = 16.sp,
                maxLines = 2
            )

            // Hardware Status Pill
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.padding(top = 4.dp)
            ) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(skin.cardBackground)
                        .border(1.dp, skin.cardBorder, RoundedCornerShape(12.dp))
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(7.dp)
                                .clip(CircleShape)
                                .background(if (hasMagnetometer) skin.primaryAccent else AmberWarning)
                        )
                        Text(
                            text = if (hasMagnetometer) "HARDWARE SENSORS READY" else "ACCEL / LEVEL ACTIVE",
                            color = skin.textPrimary,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Default,
                            maxLines = 1,
                            softWrap = false
                        )
                    }
                }
            }
        }

        // ── 3. Dedicated Monetization / Ad Placement Area ────────────────────
        // This is the designated slot where AdMob Native or Banner ad is shown
        // as requested by leadership for monetization.
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp)),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = skin.cardBackground),
            border = CardDefaults.outlinedCardBorder().copy(
                brush = androidx.compose.ui.graphics.SolidColor(skin.cardBorder)
            )
        ) {
            if (adSlot != null) {
                // Live Ad Injection Slot
                adSlot()
            } else {
                // High-End Monetization Native Ad Placeholder
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(Color(0xFFEAA200))
                                    .padding(horizontal = 5.dp, vertical = 1.dp)
                            ) {
                                Text(
                                    text = "Ad",
                                    color = PureBlack,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Text(
                                text = "SPONSORED RECOMMENDATION",
                                color = skin.textSecondary,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 0.5.sp
                            )
                        }

                        Icon(
                            imageVector = Icons.Outlined.Info,
                            contentDescription = null,
                            tint = skin.textSecondary.copy(alpha = 0.6f),
                            modifier = Modifier.size(16.dp)
                        )
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(skin.surfaceBackground)
                                .border(1.dp, skin.cardBorder, RoundedCornerShape(10.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Speed,
                                contentDescription = null,
                                tint = skin.primaryAccent,
                                modifier = Modifier.size(24.dp)
                            )
                        }

                        Column(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(2.dp)
                        ) {
                            Text(
                                text = "Ultra Precision Pro Suite",
                                color = skin.textPrimary,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = "Calibrated digital tools for field engineers and architects.",
                                color = skin.textSecondary,
                                fontSize = 11.5.sp,
                                lineHeight = 15.sp,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis
                            )
                        }

                        Button(
                            onClick = {
                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            },
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = skin.primaryAccent.copy(alpha = 0.2f),
                                contentColor = skin.primaryAccent
                            ),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = "GET",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }

        // ── 4. Tool Suite Showcase (4 Quick Cards) ───────────────────────────
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                text = "AVAILABLE INSTRUMENTS",
                color = skin.textSecondary,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.8.sp,
                modifier = Modifier.padding(start = 4.dp)
            )

            // Grid / Row of Tools
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                IntroToolCard(
                    title = "Compass",
                    subtitle = "Azimuth & Bearing",
                    icon = Icons.Default.Explore,
                    modifier = Modifier.weight(1f),
                    skin = skin,
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        onStartTool("Compass")
                    }
                )
                IntroToolCard(
                    title = "Spirit Level",
                    subtitle = "Dual-Axis Bubble",
                    icon = Icons.Default.LinearScale,
                    modifier = Modifier.weight(1f),
                    skin = skin,
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        onStartTool("Level")
                    }
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                IntroToolCard(
                    title = "Clinometer",
                    subtitle = "Optical AR Horizon",
                    icon = Icons.Default.Straighten,
                    modifier = Modifier.weight(1f),
                    skin = skin,
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        onStartTool("Clinometer")
                    }
                )
                IntroToolCard(
                    title = "Location",
                    subtitle = "GPS Waypoint & Alt",
                    icon = Icons.Default.LocationOn,
                    modifier = Modifier.weight(1f),
                    skin = skin,
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        onStartTool("Location")
                    }
                )
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        // ── 5. Primary Start Action ──────────────────────────────────────────
        Button(
            onClick = {
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                onStartTool("Compass")
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(54.dp),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = skin.primaryAccent,
                contentColor = PureBlack
            ),
            elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "START COMPASS & TOOLS",
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 16.sp,
                    letterSpacing = 0.5.sp
                )
                Spacer(modifier = Modifier.width(8.dp))
                Icon(
                    imageVector = Icons.Default.ArrowForward,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
            }
        }

        Text(
            text = "Press BACK anytime to open the exit rating prompt",
            color = skin.textSecondary.copy(alpha = 0.7f),
            fontSize = 11.sp,
            fontFamily = FontFamily.Default,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(12.dp))
    }
}

@Composable
private fun IntroToolCard(
    title: String,
    subtitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    modifier: Modifier = Modifier,
    skin: SkinPalette,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .clickable { onClick() },
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = skin.cardBackground),
        border = CardDefaults.outlinedCardBorder().copy(
            brush = androidx.compose.ui.graphics.SolidColor(skin.cardBorder)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .height(102.dp)
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(skin.primaryAccent.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = skin.primaryAccent,
                        modifier = Modifier.size(18.dp)
                    )
                }

                Icon(
                    imageVector = Icons.Default.ArrowForward,
                    contentDescription = null,
                    tint = skin.textSecondary.copy(alpha = 0.4f),
                    modifier = Modifier.size(14.dp)
                )
            }

            Column(
                verticalArrangement = Arrangement.spacedBy(1.dp)
            ) {
                Text(
                    text = title,
                    color = skin.textPrimary,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    softWrap = false,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = subtitle,
                    color = skin.textSecondary,
                    fontSize = 10.5.sp,
                    maxLines = 1,
                    softWrap = false,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}
