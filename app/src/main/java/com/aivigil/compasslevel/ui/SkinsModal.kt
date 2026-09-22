package com.aivigil.compasslevel.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aivigil.compasslevel.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SkinsModal(
    currentSkin: AppSkin,
    isDarkMode: Boolean = false,
    onSkinSelected: (AppSkin) -> Unit,
    onClose: () -> Unit
) {
    val activePalette = currentSkin.palette(isDarkMode)
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onClose,
        sheetState = sheetState,
        containerColor = activePalette.surfaceBackground,
        dragHandle = { BottomSheetDefaults.DragHandle(color = activePalette.cardBorder) },
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 6.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "INSTRUMENT THEMES",
                        color = activePalette.textPrimary,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Default,
                        letterSpacing = 0.5.sp
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(Color(0xFFFFD700).copy(alpha = 0.2f))
                            .padding(horizontal = 7.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "6 PRESETS",
                            color = Color(0xFFFFD700),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Default
                        )
                    }
                }

                IconButton(
                    onClick = onClose,
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(activePalette.cardBackground)
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close",
                        tint = activePalette.textPrimary,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "Swipe to preview custom aesthetics for Compass, Spirit Level, and Camera HUD",
                color = activePalette.textSecondary,
                fontSize = 12.sp,
                fontFamily = FontFamily.Default,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(18.dp))

            // Horizontal Carousel
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(14.dp),
                contentPadding = PaddingValues(horizontal = 4.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(AppSkin.values()) { skin ->
                    SkinCarouselCard(
                        skin = skin,
                        isDarkMode = isDarkMode,
                        isSelected = skin == currentSkin,
                        onSelect = { onSkinSelected(skin) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            Text(
                text = "Active Theme: ${currentSkin.displayName}",
                color = activePalette.primaryAccent,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Default
            )
            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}

@Composable
fun SkinCarouselCard(
    skin: AppSkin,
    isDarkMode: Boolean = false,
    isSelected: Boolean,
    onSelect: () -> Unit
) {
    val palette = skin.palette(isDarkMode)
    Card(
        modifier = Modifier
            .width(220.dp)
            .clip(RoundedCornerShape(18.dp))
            .border(
                width = if (isSelected) 2.dp else 1.dp,
                color = if (isSelected) palette.primaryAccent else BorderSubtle,
                shape = RoundedCornerShape(18.dp)
            )
            .clickable { onSelect() },
        colors = CardDefaults.cardColors(containerColor = palette.surfaceBackground)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Badge row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (skin.isPremium) {
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(Color(0xFFFFD700).copy(alpha = 0.2f))
                            .padding(horizontal = 6.dp, vertical = 2.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = "VIP",
                            tint = Color(0xFFFFD700),
                            modifier = Modifier.size(10.dp)
                        )
                        Spacer(modifier = Modifier.width(3.dp))
                        Text(
                            text = "PRO",
                            color = Color(0xFFFFD700),
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Default
                        )
                    }
                } else {
                    Text(
                        text = "FREE",
                        color = palette.textSecondary,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Default
                    )
                }

                if (isSelected) {
                    Box(
                        modifier = Modifier
                            .size(20.dp)
                            .clip(CircleShape)
                            .background(palette.primaryAccent),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = "Active",
                            tint = PureBlack,
                            modifier = Modifier.size(12.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Miniature Graphic Preview (Spirit Level Dial + Bubble)
            Box(
                modifier = Modifier
                    .size(110.dp)
                    .clip(CircleShape)
                    .background(palette.dialBackground)
                    .border(2.dp, palette.ringBorder, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    // Outer crosshairs
                    drawLine(
                        color = palette.primaryAccent.copy(alpha = 0.35f),
                        start = Offset(0f, size.height / 2),
                        end = Offset(size.width, size.height / 2),
                        strokeWidth = 1.dp.toPx()
                    )
                    drawLine(
                        color = palette.primaryAccent.copy(alpha = 0.35f),
                        start = Offset(size.width / 2, 0f),
                        end = Offset(size.width / 2, size.height),
                        strokeWidth = 1.dp.toPx()
                    )
                    // Inner precision circle
                    drawCircle(
                        color = palette.primaryAccent.copy(alpha = 0.5f),
                        radius = size.width * 0.28f,
                        style = Stroke(1.5.dp.toPx())
                    )
                    // Level bubble
                    drawCircle(
                        color = palette.bubbleColor,
                        radius = size.width * 0.12f,
                        center = Offset(size.width * 0.46f, size.height * 0.44f)
                    )
                    // Bubble core reflection
                    drawCircle(
                        color = Color.White.copy(alpha = 0.7f),
                        radius = size.width * 0.04f,
                        center = Offset(size.width * 0.44f, size.height * 0.42f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = skin.displayName,
                color = if (isSelected) palette.primaryAccent else palette.textPrimary,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Default,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = skin.description,
                color = palette.textSecondary,
                fontSize = 11.sp,
                fontFamily = FontFamily.Default,
                textAlign = TextAlign.Center,
                lineHeight = 14.sp,
                maxLines = 2
            )

            Spacer(modifier = Modifier.height(12.dp))

            Button(
                onClick = onSelect,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(34.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isSelected) palette.primaryAccent else palette.cardBorder.copy(alpha = 0.3f)
                ),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(
                    text = if (isSelected) "APPLIED" else "SELECT",
                    color = if (isSelected) PureBlack else palette.textPrimary,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Default
                )
            }
        }
    }
}

@Composable
fun SkinCarouselCard(
    skin: AppSkin,
    isSelected: Boolean,
    onSelect: () -> Unit
) {
    SkinCarouselCard(
        skin = skin,
        isDarkMode = false,
        isSelected = isSelected,
        onSelect = onSelect
    )
}
