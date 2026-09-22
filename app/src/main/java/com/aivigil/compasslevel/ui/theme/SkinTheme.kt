package com.aivigil.compasslevel.ui.theme

import androidx.compose.ui.graphics.Color

data class SkinPalette(
    val primaryAccent: Color,
    val secondaryAccent: Color,
    val bubbleColor: Color,
    val bubbleGlow: Color,
    val appBackground: Color,
    val surfaceBackground: Color,
    val cardBackground: Color,
    val cardBorder: Color,
    val cardElevated: Color,
    val dialBackground: Color,
    val dialOuterBezel: Color,
    val ringBorder: Color,
    val textPrimary: Color,
    val textSecondary: Color,
    val glowColor: Color,
    val needleNorth: Color,
    val needleSouth: Color,
    val isDark: Boolean
)

enum class AppSkin(
    val id: String,
    val displayName: String,
    val description: String,
    val isPremium: Boolean = false
) {
    CLASSIC_EMERALD(
        id = "classic_emerald",
        displayName = "Nothing Stealth",
        description = "Minimalist matte obsidian with calm sage luminescence",
        isPremium = false
    ),
    LUXURY_GOLD(
        id = "luxury_gold",
        displayName = "Porsche Carbon Bronze",
        description = "Dark graphite titanium with warm champagne bronze",
        isPremium = true
    ),
    MARINE_BLUE(
        id = "marine_blue",
        displayName = "Nordic Polar Glacier",
        description = "Abyss navy titanium with crisp arctic ice cyan",
        isPremium = false
    ),
    CYBERPUNK_PURPLE(
        id = "cyberpunk_purple",
        displayName = "Cyber Matrix Violet",
        description = "Smoked void graphite with subtle laser indigo",
        isPremium = true
    ),
    SONAR_CYAN(
        id = "sonar_cyan",
        displayName = "Aero Horizon Teal",
        description = "Matte aerospace carbon with calm horizon teal",
        isPremium = false
    ),
    MILITARY_HUD(
        id = "military_hud",
        displayName = "Tactical Spec-Ops",
        description = "Low-viz night reconnaissance with muted field olive",
        isPremium = true
    );

    val darkPalette: SkinPalette by lazy {
        when (this) {
            CLASSIC_EMERALD -> SkinPalette(
                primaryAccent = Color(0xFF34D399),
                secondaryAccent = Color(0xFF6EE7B7),
                bubbleColor = Color(0xFF34D399),
                bubbleGlow = Color(0x2834D399),
                appBackground = Color(0xFF0C0D0E),
                surfaceBackground = Color(0xFF141618),
                cardBackground = Color(0xFF1B1E21),
                cardBorder = Color(0xFF282D32),
                cardElevated = Color(0xFF22262B),
                dialBackground = Color(0xFF101214),
                dialOuterBezel = Color(0xFF1D2124),
                ringBorder = Color(0xFF26302B),
                textPrimary = Color(0xFFF1F5F9),
                textSecondary = Color(0xFF8896A6),
                glowColor = Color(0x1834D399),
                needleNorth = Color(0xFFEF4444),
                needleSouth = Color(0xFF94A3B8),
                isDark = true
            )
            LUXURY_GOLD -> SkinPalette(
                primaryAccent = Color(0xFFD4AF37),
                secondaryAccent = Color(0xFFC5A059),
                bubbleColor = Color(0xFFE2C974),
                bubbleGlow = Color(0x28D4AF37),
                appBackground = Color(0xFF0E0F11),
                surfaceBackground = Color(0xFF16181B),
                cardBackground = Color(0xFF1E2125),
                cardBorder = Color(0xFF2F343A),
                cardElevated = Color(0xFF262A30),
                dialBackground = Color(0xFF121417),
                dialOuterBezel = Color(0xFF23272D),
                ringBorder = Color(0xFF3A352B),
                textPrimary = Color(0xFFF8F5EE),
                textSecondary = Color(0xFFA1998A),
                glowColor = Color(0x18D4AF37),
                needleNorth = Color(0xFFEF4444),
                needleSouth = Color(0xFFB09B65),
                isDark = true
            )
            MARINE_BLUE -> SkinPalette(
                primaryAccent = Color(0xFF38BDF8),
                secondaryAccent = Color(0xFF7DD3FC),
                bubbleColor = Color(0xFF38BDF8),
                bubbleGlow = Color(0x2838BDF8),
                appBackground = Color(0xFF0B0F14),
                surfaceBackground = Color(0xFF111720),
                cardBackground = Color(0xFF17202C),
                cardBorder = Color(0xFF233144),
                cardElevated = Color(0xFF1D2837),
                dialBackground = Color(0xFF0E131B),
                dialOuterBezel = Color(0xFF1A2433),
                ringBorder = Color(0xFF22344B),
                textPrimary = Color(0xFFF0F6FC),
                textSecondary = Color(0xFF8B9CB0),
                glowColor = Color(0x1838BDF8),
                needleNorth = Color(0xFFEF4444),
                needleSouth = Color(0xFF7DD3FC),
                isDark = true
            )
            CYBERPUNK_PURPLE -> SkinPalette(
                primaryAccent = Color(0xFF818CF8),
                secondaryAccent = Color(0xFFA5B4FC),
                bubbleColor = Color(0xFF818CF8),
                bubbleGlow = Color(0x28818CF8),
                appBackground = Color(0xFF0D0B12),
                surfaceBackground = Color(0xFF15121E),
                cardBackground = Color(0xFF1D1929),
                cardBorder = Color(0xFF2D263E),
                cardElevated = Color(0xFF252034),
                dialBackground = Color(0xFF100E17),
                dialOuterBezel = Color(0xFF1F1A2C),
                ringBorder = Color(0xFF2D2641),
                textPrimary = Color(0xFFF5F3FF),
                textSecondary = Color(0xFF9E96B5),
                glowColor = Color(0x18818CF8),
                needleNorth = Color(0xFFF43F5E),
                needleSouth = Color(0xFFA5B4FC),
                isDark = true
            )
            SONAR_CYAN -> SkinPalette(
                primaryAccent = Color(0xFF2DD4BF),
                secondaryAccent = Color(0xFF5EEAD4),
                bubbleColor = Color(0xFF2DD4BF),
                bubbleGlow = Color(0x282DD4BF),
                appBackground = Color(0xFF0B1012),
                surfaceBackground = Color(0xFF121A1D),
                cardBackground = Color(0xFF182327),
                cardBorder = Color(0xFF25373E),
                cardElevated = Color(0xFF1E2D32),
                dialBackground = Color(0xFF0D1416),
                dialOuterBezel = Color(0xFF1B272B),
                ringBorder = Color(0xFF20363B),
                textPrimary = Color(0xFFF0FDFA),
                textSecondary = Color(0xFF879FA5),
                glowColor = Color(0x182DD4BF),
                needleNorth = Color(0xFFEF4444),
                needleSouth = Color(0xFF5EEAD4),
                isDark = true
            )
            MILITARY_HUD -> SkinPalette(
                primaryAccent = Color(0xFFA3E635),
                secondaryAccent = Color(0xFFBEF264),
                bubbleColor = Color(0xFFA3E635),
                bubbleGlow = Color(0x28A3E635),
                appBackground = Color(0xFF0D100C),
                surfaceBackground = Color(0xFF131911),
                cardBackground = Color(0xFF1A2318),
                cardBorder = Color(0xFF273624),
                cardElevated = Color(0xFF202B1E),
                dialBackground = Color(0xFF0F140E),
                dialOuterBezel = Color(0xFF1C241A),
                ringBorder = Color(0xFF283824),
                textPrimary = Color(0xFFF7FEE7),
                textSecondary = Color(0xFF8E9E86),
                glowColor = Color(0x18A3E635),
                needleNorth = Color(0xFFEF4444),
                needleSouth = Color(0xFFBEF264),
                isDark = true
            )
        }
    }

    val lightPalette: SkinPalette by lazy {
        when (this) {
            CLASSIC_EMERALD -> SkinPalette(
                primaryAccent = Color(0xFF059669),
                secondaryAccent = Color(0xFF10B981),
                bubbleColor = Color(0xFF10B981),
                bubbleGlow = Color(0x2210B981),
                appBackground = Color(0xFFF8FAF9),
                surfaceBackground = Color(0xFFFFFFFF),
                cardBackground = Color(0xFFF0F4F2),
                cardBorder = Color(0xFFD8E2DC),
                cardElevated = Color(0xFFE5EDE8),
                dialBackground = Color(0xFFF2F6F3),
                dialOuterBezel = Color(0xFFDEE8E1),
                ringBorder = Color(0xFFCBDCD1),
                textPrimary = Color(0xFF111827),
                textSecondary = Color(0xFF4B5563),
                glowColor = Color(0x15059669),
                needleNorth = Color(0xFFDC2626),
                needleSouth = Color(0xFF6B7280),
                isDark = false
            )
            LUXURY_GOLD -> SkinPalette(
                primaryAccent = Color(0xFFB45309),
                secondaryAccent = Color(0xFFD97706),
                bubbleColor = Color(0xFFD97706),
                bubbleGlow = Color(0x22D97706),
                appBackground = Color(0xFFFAF9F6),
                surfaceBackground = Color(0xFFFFFFFF),
                cardBackground = Color(0xFFF4F1EA),
                cardBorder = Color(0xFFE2DDD1),
                cardElevated = Color(0xFFEBE6DA),
                dialBackground = Color(0xFFF6F3EC),
                dialOuterBezel = Color(0xFFE3DDCF),
                ringBorder = Color(0xFFD3CABE),
                textPrimary = Color(0xFF1C1917),
                textSecondary = Color(0xFF57534E),
                glowColor = Color(0x15B45309),
                needleNorth = Color(0xFFDC2626),
                needleSouth = Color(0xFF78716C),
                isDark = false
            )
            MARINE_BLUE -> SkinPalette(
                primaryAccent = Color(0xFF0284C7),
                secondaryAccent = Color(0xFF38BDF8),
                bubbleColor = Color(0xFF0284C7),
                bubbleGlow = Color(0x220284C7),
                appBackground = Color(0xFFF8FAFC),
                surfaceBackground = Color(0xFFFFFFFF),
                cardBackground = Color(0xFFEFF4F9),
                cardBorder = Color(0xFFD6E2EE),
                cardElevated = Color(0xFFE2ECF6),
                dialBackground = Color(0xFFF1F6FB),
                dialOuterBezel = Color(0xFFDBE7F3),
                ringBorder = Color(0xFFCADBEB),
                textPrimary = Color(0xFF0F172A),
                textSecondary = Color(0xFF475569),
                glowColor = Color(0x150284C7),
                needleNorth = Color(0xFFDC2626),
                needleSouth = Color(0xFF64748B),
                isDark = false
            )
            CYBERPUNK_PURPLE -> SkinPalette(
                primaryAccent = Color(0xFF6366F1),
                secondaryAccent = Color(0xFF818CF8),
                bubbleColor = Color(0xFF6366F1),
                bubbleGlow = Color(0x226366F1),
                appBackground = Color(0xFFF9F9FC),
                surfaceBackground = Color(0xFFFFFFFF),
                cardBackground = Color(0xFFF1F2F9),
                cardBorder = Color(0xFFD8DAEC),
                cardElevated = Color(0xFFE5E7F4),
                dialBackground = Color(0xFFF3F4FB),
                dialOuterBezel = Color(0xFFDCE0F0),
                ringBorder = Color(0xFFCBCFE6),
                textPrimary = Color(0xFF1E1B4B),
                textSecondary = Color(0xFF4F46E5),
                glowColor = Color(0x156366F1),
                needleNorth = Color(0xFFE11D48),
                needleSouth = Color(0xFF6B7280),
                isDark = false
            )
            SONAR_CYAN -> SkinPalette(
                primaryAccent = Color(0xFF0D9488),
                secondaryAccent = Color(0xFF14B8A6),
                bubbleColor = Color(0xFF0D9488),
                bubbleGlow = Color(0x220D9488),
                appBackground = Color(0xFFF7FAFA),
                surfaceBackground = Color(0xFFFFFFFF),
                cardBackground = Color(0xFFEFF5F5),
                cardBorder = Color(0xFFD3E3E3),
                cardElevated = Color(0xFFE1EDED),
                dialBackground = Color(0xFFF0F6F6),
                dialOuterBezel = Color(0xFFD9E7E7),
                ringBorder = Color(0xFFC7DCDB),
                textPrimary = Color(0xFF132A29),
                textSecondary = Color(0xFF3F6260),
                glowColor = Color(0x150D9488),
                needleNorth = Color(0xFFDC2626),
                needleSouth = Color(0xFF5A7574),
                isDark = false
            )
            MILITARY_HUD -> SkinPalette(
                primaryAccent = Color(0xFF4D7C0F),
                secondaryAccent = Color(0xFF65A30D),
                bubbleColor = Color(0xFF4D7C0F),
                bubbleGlow = Color(0x224D7C0F),
                appBackground = Color(0xFFF8FAF7),
                surfaceBackground = Color(0xFFFFFFFF),
                cardBackground = Color(0xFFF0F4EE),
                cardBorder = Color(0xFFD6E2D2),
                cardElevated = Color(0xFFE3EDE0),
                dialBackground = Color(0xFFF1F6EF),
                dialOuterBezel = Color(0xFFDCE6DA),
                ringBorder = Color(0xFFCCD9C9),
                textPrimary = Color(0xFF1A2E05),
                textSecondary = Color(0xFF4D6136),
                glowColor = Color(0x154D7C0F),
                needleNorth = Color(0xFFDC2626),
                needleSouth = Color(0xFF576846),
                isDark = false
            )
        }
    }

    fun palette(isDark: Boolean): SkinPalette = if (isDark) darkPalette else lightPalette

    // Direct delegates for backwards-compatibility
    val primaryAccent: Color get() = darkPalette.primaryAccent
    val secondaryAccent: Color get() = darkPalette.secondaryAccent
    val bubbleColor: Color get() = darkPalette.bubbleColor
    val bubbleGlow: Color get() = darkPalette.bubbleGlow
    val appBackground: Color get() = darkPalette.appBackground
    val surfaceBackground: Color get() = darkPalette.surfaceBackground
    val cardBackground: Color get() = darkPalette.cardBackground
    val cardBorder: Color get() = darkPalette.cardBorder
    val cardElevated: Color get() = darkPalette.cardElevated
    val dialBackground: Color get() = darkPalette.dialBackground
    val dialOuterBezel: Color get() = darkPalette.dialOuterBezel
    val ringBorder: Color get() = darkPalette.ringBorder
    val textPrimary: Color get() = darkPalette.textPrimary
    val textSecondary: Color get() = darkPalette.textSecondary
    val glowColor: Color get() = darkPalette.glowColor
}
