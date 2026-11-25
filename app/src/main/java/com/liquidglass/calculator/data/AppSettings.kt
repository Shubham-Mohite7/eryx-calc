package com.liquidglass.calculator.data

import com.liquidglass.calculator.engine.AngleMode
import com.liquidglass.calculator.engine.DecimalPrecisionMode

enum class ThemeMode(val label: String) {
    LIGHT("Light"),
    DARK("Dark"),
    SYSTEM("System")
}

enum class AccentOption(val label: String, val colorHex: Long) {
    GRAPHITE("Graphite", 0xFF22252A),
    COBALT("Cobalt", 0xFF3B82F6),
    VIOLET("Violet", 0xFF8B5CF6),
    CORAL("Coral", 0xFFEC4899),
    EMERALD("Emerald", 0xFF10B981)
}

enum class AppIconStyle(val label: String, val aliasClass: String) {
    GLASS_LIGHT("Frosted", "com.liquidglass.calculator.LauncherGlassLight"),
    GLASS_DARK("Obsidian", "com.liquidglass.calculator.LauncherGlassDark"),
    MINIMAL("Minimal", "com.liquidglass.calculator.LauncherMinimal"),
    GRID("Grid", "com.liquidglass.calculator.LauncherGrid")
}

enum class CalculatorLayoutStyle(val label: String) {
    CIRCULAR("Circular"),
    ROUNDED("Rounded")
}

enum class HapticStrength(val label: String) {
    LIGHT("Light"),
    MEDIUM("Medium"),
    STRONG("Strong")
}

enum class MemoryBehavior(val label: String) {
    STANDARD("Standard"),
    MULTI_SLOT("Multi-Slot")
}

enum class CalculatorMode(val label: String) {
    STANDARD("Standard"),
    SCIENTIFIC("Scientific"),
    CONVERTER("Converter")
}

enum class OneHandedMode(val label: String) {
    FULL("Standard"),
    LEFT("Left Thumb"),
    RIGHT("Right Thumb")
}

data class AppSettings(
    // Appearance
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val accentOption: AccentOption = AccentOption.GRAPHITE,
    val appIconStyle: AppIconStyle = AppIconStyle.GLASS_LIGHT,
    val calculatorLayout: CalculatorLayoutStyle = CalculatorLayoutStyle.CIRCULAR,
    val oneHandedMode: OneHandedMode = OneHandedMode.FULL,
    val translucentUi: Boolean = true,
    val reduceTransparency: Boolean = false,
    // Haptics & Sound
    val hapticFeedback: Boolean = true,
    val hapticStrength: HapticStrength = HapticStrength.MEDIUM,
    val soundEnabled: Boolean = false,
    // Calculation
    val decimalPrecision: DecimalPrecisionMode = DecimalPrecisionMode.AUTO,
    val angleMode: AngleMode = AngleMode.DEG,
    val memoryBehavior: MemoryBehavior = MemoryBehavior.STANDARD,
    val showThousandSeparator: Boolean = true,
    val autoCloseBrackets: Boolean = true,
    val expressionInMainDisplay: Boolean = false,
    val copyResultWithUnits: Boolean = false,
    // Accessibility
    val textScale: Float = 1.0f,
    val highContrast: Boolean = false,
    val buttonLabels: Boolean = false,
    val voiceResults: Boolean = false,
    // Navigation & State
    val lastMode: CalculatorMode = CalculatorMode.STANDARD,
    val memoryValue: String = "0",
    val memorySlots: List<String> = emptyList(),
    val lastAnswer: String = "0",
    val autoCheckUpdates: Boolean = true,
    val lastUpdateCheckMillis: Long = 0L
)
