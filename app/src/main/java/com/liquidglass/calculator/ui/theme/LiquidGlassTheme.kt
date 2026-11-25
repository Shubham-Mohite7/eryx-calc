package com.liquidglass.calculator.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import com.liquidglass.calculator.data.AccentOption
import com.liquidglass.calculator.data.AppSettings
import com.liquidglass.calculator.data.CalculatorLayoutStyle
import com.liquidglass.calculator.data.ThemeMode

@Immutable
data class GlassPalette(
    val isDark: Boolean,
    val backgroundBase: Color,
    val backdropBlobA: Color,
    val backdropBlobB: Color,
    val backdropBlobC: Color,
    val cardSurface: Color,
    val displaySurface: Color,
    val keySurface: Color,
    val keyPressedSurface: Color,
    val operatorKeySurface: Color,
    val operatorKeyBorder: Color,
    val operatorKeyText: Color,
    val clearKeySurface: Color,
    val clearKeyBorder: Color,
    val clearKeyText: Color,
    val secondaryKeySurface: Color,
    val pillContainerSurface: Color,
    val activePillSurface: Color,
    val modalSurface: Color,
    val glassBorder: Color,
    val subtleDivider: Color,
    val textPrimary: Color,
    val textSecondary: Color,
    val textMuted: Color,
    val accent: Color,
    val onAccent: Color,
    val danger: Color,
    val switchTrackOn: Color,
    val switchTrackOff: Color,
    val switchThumb: Color,
    val textScale: Float,
    val isCircularKeys: Boolean,
    val blurEnabled: Boolean
)

val LocalGlassPalette = staticCompositionLocalOf<GlassPalette> {
    error("GlassPalette not provided")
}

@Composable
fun LiquidGlassTheme(
    settings: AppSettings,
    content: @Composable () -> Unit
) {
    val systemDark = isSystemInDarkTheme()
    val isDark = when (settings.themeMode) {
        ThemeMode.LIGHT -> false
        ThemeMode.DARK -> true
        ThemeMode.SYSTEM -> systemDark
    }

    val translucent = settings.translucentUi && !settings.reduceTransparency
    val hc = settings.highContrast
    val rawAccent = Color(settings.accentOption.colorHex)
    val accent = if (isDark && settings.accentOption == AccentOption.GRAPHITE) {
        Color(0xFFF2F4F7)
    } else {
        rawAccent
    }
    val onAccent = if (isDark && settings.accentOption == AccentOption.GRAPHITE) {
        Color(0xFF14161A)
    } else {
        Color.White
    }

    val palette = if (!isDark) {
        GlassPalette(
            isDark = false,
            backgroundBase = Color(0xFFE3E8EE),
            backdropBlobA = Color(0x88FFFFFF),
            backdropBlobB = Color(0x66C7D1DC),
            backdropBlobC = Color(0x55D6DFE8),
            cardSurface = when {
                hc -> Color(0xFFFCFDFF)
                translucent -> Color(0xA8FFFFFF)
                else -> Color(0xF5F7F9FB)
            },
            displaySurface = when {
                hc -> Color(0xFFFFFFFF)
                translucent -> Color(0xBEFFFFFF)
                else -> Color(0xFAFFFFFF)
            },
            keySurface = when {
                hc -> Color(0xFFFFFFFF)
                translucent -> Color(0xB8FFFFFF)
                else -> Color(0xF5FFFFFF)
            },
            keyPressedSurface = Color(0xE6EDF1F5),
            operatorKeySurface = when {
                hc -> Color(0xFFFFE4C2)
                translucent -> Color(0x28FF9500)
                else -> Color(0xFFF5E6D3)
            },
            operatorKeyBorder = if (hc) Color(0xFFB35900) else Color(0x45D97706),
            operatorKeyText = if (hc) Color(0xFF9A4800) else Color(0xFFC46200),
            clearKeySurface = when {
                hc -> Color(0xFFFFD9D6)
                translucent -> Color(0x22EF4444)
                else -> Color(0xFFF8E1E0)
            },
            clearKeyBorder = if (hc) Color(0xFFB91C1C) else Color(0x45DC2626),
            clearKeyText = if (hc) Color(0xFFB91C1C) else Color(0xFFD92D20),
            secondaryKeySurface = when {
                hc -> Color(0xFFE6ECF2)
                translucent -> Color(0x9AEFF3F7)
                else -> Color(0xFFEAF0F5)
            },
            pillContainerSurface = if (translucent) Color(0x66CED6E0) else Color(0xFFD9E0E7),
            activePillSurface = if (translucent) Color(0xEAFFFFFF) else Color(0xFFFFFFFF),
            modalSurface = if (translucent) Color(0xF0F4F7FA) else Color(0xFFF8FAFC),
            glassBorder = if (hc) Color(0xFF1D2229) else Color(0x241D2229),
            subtleDivider = Color(0x1F1D2229),
            textPrimary = if (hc) Color(0xFF000000) else Color(0xFF14171C),
            textSecondary = if (hc) Color(0xFF23272F) else Color(0xFF565E69),
            textMuted = Color(0xFF7D8692),
            accent = accent,
            onAccent = onAccent,
            danger = Color(0xFFDC3545),
            switchTrackOn = Color(0xFF1A1D22),
            switchTrackOff = Color(0xFFC8CFD8),
            switchThumb = Color.White,
            textScale = settings.textScale,
            isCircularKeys = settings.calculatorLayout == CalculatorLayoutStyle.CIRCULAR,
            blurEnabled = translucent
        )
    } else {
        GlassPalette(
            isDark = true,
            backgroundBase = Color(0xFF000000),
            backdropBlobA = Color.Transparent,
            backdropBlobB = Color.Transparent,
            backdropBlobC = Color.Transparent,
            cardSurface = when {
                hc -> Color(0xFF0D0E10)
                translucent -> Color(0xCC0B0C0F)
                else -> Color(0xFF0A0B0D)
            },
            displaySurface = when {
                hc -> Color(0xFF08090B)
                translucent -> Color(0xD907080A)
                else -> Color(0xFF07080A)
            },
            keySurface = when {
                hc -> Color(0xFF15171B)
                translucent -> Color(0xD9111216)
                else -> Color(0xFF111215)
            },
            keyPressedSurface = Color(0xFF252830),
            operatorKeySurface = when {
                hc -> Color(0xFF3D290F)
                translucent -> Color(0x2CFF9F0A)
                else -> Color(0xFF2B1D0B)
            },
            operatorKeyBorder = if (hc) Color(0xFFFFB340) else Color(0x55FF9F0A),
            operatorKeyText = Color(0xFFFFAD33),
            clearKeySurface = when {
                hc -> Color(0xFF421515)
                translucent -> Color(0x2CFF453A)
                else -> Color(0xFF2E1214)
            },
            clearKeyBorder = if (hc) Color(0xFFFF6961) else Color(0x55FF453A),
            clearKeyText = Color(0xFFFF6961),
            secondaryKeySurface = when {
                hc -> Color(0xFF1C1F25)
                translucent -> Color(0xD917191E)
                else -> Color(0xFF16181D)
            },
            pillContainerSurface = Color(0xFF0B0C0F),
            activePillSurface = Color(0xFF1B1D24),
            modalSurface = Color(0xF50A0B0E),
            glassBorder = if (hc) Color(0xD9FFFFFF) else Color(0x2BFFFFFF),
            subtleDivider = Color(0x24FFFFFF),
            textPrimary = Color(0xFFFFFFFF),
            textSecondary = if (hc) Color(0xFFE0E4EC) else Color(0xFF9EA6B2),
            textMuted = Color(0xFF707885),
            accent = accent,
            onAccent = onAccent,
            danger = Color(0xFFFF5252),
            switchTrackOn = accent,
            switchTrackOff = Color(0xFF2B2E36),
            switchThumb = Color.White,
            textScale = settings.textScale,
            isCircularKeys = settings.calculatorLayout == CalculatorLayoutStyle.CIRCULAR,
            blurEnabled = false
        )
    }

    CompositionLocalProvider(LocalGlassPalette provides palette) {
        content()
    }
}
