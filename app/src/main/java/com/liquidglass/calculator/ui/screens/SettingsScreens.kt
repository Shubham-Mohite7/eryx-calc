package com.liquidglass.calculator.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.liquidglass.calculator.AppDestination
import com.liquidglass.calculator.BuildConfig
import com.liquidglass.calculator.CalculatorViewModel
import com.liquidglass.calculator.data.AccentOption
import com.liquidglass.calculator.data.AppIconStyle
import com.liquidglass.calculator.data.AppSettings
import com.liquidglass.calculator.data.CalculatorLayoutStyle
import com.liquidglass.calculator.data.HapticStrength
import com.liquidglass.calculator.data.MemoryBehavior
import com.liquidglass.calculator.data.OneHandedMode
import com.liquidglass.calculator.data.ThemeMode
import com.liquidglass.calculator.data.UpdateStatus
import com.liquidglass.calculator.engine.AngleMode
import com.liquidglass.calculator.engine.DecimalPrecisionMode
import com.liquidglass.calculator.ui.components.GlassCard
import com.liquidglass.calculator.ui.components.GlassCircleButton
import com.liquidglass.calculator.ui.components.GlassModalCardDialog
import com.liquidglass.calculator.ui.components.GlassSegmentedTextBar
import com.liquidglass.calculator.ui.components.GlassSubpageTopBar
import com.liquidglass.calculator.ui.components.GlassSwitch
import com.liquidglass.calculator.ui.components.GlassText
import com.liquidglass.calculator.ui.components.IconCalculatorBadge
import com.liquidglass.calculator.ui.components.IconCheckmark
import com.liquidglass.calculator.ui.components.IconChevronRight
import com.liquidglass.calculator.ui.components.IconCloseX
import com.liquidglass.calculator.ui.components.IconGridMode
import com.liquidglass.calculator.ui.components.IconTrashBin
import com.liquidglass.calculator.ui.theme.LocalGlassPalette
import kotlin.math.roundToInt

@Composable
fun SettingsMainScreen(
    settings: AppSettings,
    updateStatus: UpdateStatus,
    onNavigate: (AppDestination) -> Unit,
    onBack: () -> Unit
) {
    val p = LocalGlassPalette.current
    Column(
        modifier = Modifier
            .fillMaxSize()
            .navigationBarsPadding()
    ) {
        GlassSubpageTopBar(title = "Settings", onBack = onBack)

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            SettingsNavRow(
                title = "Appearance",
                subtitle = "${settings.themeMode.label} • ${settings.calculatorLayout.label}",
                badgeText = "Aa"
            ) { onNavigate(AppDestination.SETTINGS_APPEARANCE) }

            SettingsNavRow(
                title = "Haptics & Sound",
                subtitle = if (settings.hapticFeedback) "Haptics ${settings.hapticStrength.label}" else "Haptics Off",
                badgeText = "~"
            ) { onNavigate(AppDestination.SETTINGS_HAPTICS) }

            SettingsNavRow(
                title = "Calculation",
                subtitle = "${settings.angleMode.shortLabel} • ${settings.decimalPrecision.label}",
                badgeText = "Σ"
            ) { onNavigate(AppDestination.SETTINGS_CALCULATION) }

            SettingsNavRow(
                title = "Memory",
                subtitle = if (settings.memoryValue == "0") "Empty" else "Stored: ${settings.memoryValue}",
                badgeText = "M"
            ) { onNavigate(AppDestination.SETTINGS_MEMORY) }

            SettingsNavRow(
                title = "Accessibility",
                subtitle = "Text size ${(settings.textScale * 100).toInt()}%${if (settings.voiceResults) " • Voice On" else ""}",
                badgeText = "A+"
            ) { onNavigate(AppDestination.SETTINGS_ACCESSIBILITY) }

            val aboutSubtitle = when (updateStatus) {
                is UpdateStatus.Available -> "Update v${updateStatus.info.versionName} available"
                is UpdateStatus.ReadyToInstall -> "Update ready to install"
                else -> "Offline-first • v${BuildConfig.VERSION_NAME} • Check updates"
            }
            SettingsNavRow(
                title = "About & Updates",
                subtitle = aboutSubtitle,
                badgeText = "i"
            ) { onNavigate(AppDestination.SETTINGS_ABOUT) }
        }
    }
}

@Composable
fun AppearanceSettingsScreen(
    settings: AppSettings,
    viewModel: CalculatorViewModel,
    onBack: () -> Unit
) {
    val p = LocalGlassPalette.current
    Column(
        modifier = Modifier
            .fillMaxSize()
            .navigationBarsPadding()
    ) {
        GlassSubpageTopBar(title = "Appearance", onBack = onBack)

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Theme Preview Cards (matching Image 2 bottom-4th)
            GlassCard(shape = RoundedCornerShape(24.dp)) {
                Column(modifier = Modifier.padding(16.dp)) {
                    GlassText("Theme", fontSizeSp = 15f, fontWeight = FontWeight.SemiBold)
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        ThemeMode.entries.forEach { mode ->
                            val selected = settings.themeMode == mode
                            Column(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(18.dp))
                                    .background(if (selected) p.activePillSurface else p.secondaryKeySurface)
                                    .border(
                                        width = if (selected) 1.5.dp else 0.8.dp,
                                        color = if (selected) p.accent else p.glassBorder,
                                        shape = RoundedCornerShape(18.dp)
                                    )
                                    .clickable { viewModel.setThemeMode(mode) }
                                    .padding(vertical = 12.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                // Mini Phone Preview
                                ThemeMiniPreview(mode = mode)
                                Spacer(modifier = Modifier.height(8.dp))
                                GlassText(mode.label, fontSizeSp = 13f, fontWeight = FontWeight.SemiBold)
                                Spacer(modifier = Modifier.height(4.dp))
                                if (selected) {
                                    Box(
                                        modifier = Modifier
                                            .size(20.dp)
                                            .clip(CircleShape)
                                            .background(p.accent),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        IconCheckmark(p.onAccent, 12.dp)
                                    }
                                } else {
                                    Spacer(modifier = Modifier.size(20.dp))
                                }
                            }
                        }
                    }
                }
            }

            // Accent Color Swatches (matching Image 2 bottom-4th)
            GlassCard(shape = RoundedCornerShape(24.dp)) {
                Column(modifier = Modifier.padding(16.dp)) {
                    GlassText("Accent Color", fontSizeSp = 15f, fontWeight = FontWeight.SemiBold)
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        AccentOption.entries.forEach { accent ->
                            val selected = settings.accentOption == accent
                            Box(
                                modifier = Modifier
                                    .size(44.dp)
                                    .clip(CircleShape)
                                    .background(Color(accent.colorHex))
                                    .border(
                                        width = if (selected) 2.5.dp else 1.dp,
                                        color = if (selected) p.textPrimary else p.glassBorder,
                                        shape = CircleShape
                                    )
                                    .clickable { viewModel.setAccentOption(accent) },
                                contentAlignment = Alignment.Center
                            ) {
                                if (selected) IconCheckmark(Color.White, 18.dp)
                            }
                        }
                    }
                }
            }

            // Calculator Layout (Circular vs Rounded — from Image 1 Settings)
            GlassCard(shape = RoundedCornerShape(24.dp)) {
                Column(modifier = Modifier.padding(16.dp)) {
                    GlassText("Calculator Layout", fontSizeSp = 15f, fontWeight = FontWeight.SemiBold)
                    Spacer(modifier = Modifier.height(10.dp))
                    GlassSegmentedTextBar(
                        options = CalculatorLayoutStyle.entries.map { it.label },
                        selectedIndex = CalculatorLayoutStyle.entries.indexOf(settings.calculatorLayout),
                        onSelect = { idx -> viewModel.setCalculatorLayout(CalculatorLayoutStyle.entries[idx]) }
                    )
                    Spacer(modifier = Modifier.height(14.dp))
                    GlassText("One-Handed Reachability", fontSizeSp = 15f, fontWeight = FontWeight.SemiBold)
                    Spacer(modifier = Modifier.height(8.dp))
                    GlassSegmentedTextBar(
                        options = OneHandedMode.entries.map { it.label },
                        selectedIndex = OneHandedMode.entries.indexOf(settings.oneHandedMode),
                        onSelect = { idx -> viewModel.setOneHandedMode(OneHandedMode.entries[idx]) }
                    )
                }
            }

            // App Icon Selector (matching Image 2 bottom-4th)
            GlassCard(shape = RoundedCornerShape(24.dp)) {
                Column(modifier = Modifier.padding(16.dp)) {
                    GlassText("App Icon", fontSizeSp = 15f, fontWeight = FontWeight.SemiBold)
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        AppIconStyle.entries.forEach { style ->
                            val selected = settings.appIconStyle == style
                            Column(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(if (selected) p.activePillSurface else p.secondaryKeySurface)
                                    .border(
                                        width = if (selected) 1.5.dp else 0.8.dp,
                                        color = if (selected) p.accent else p.glassBorder,
                                        shape = RoundedCornerShape(16.dp)
                                    )
                                    .clickable { viewModel.setAppIconStyle(style) }
                                    .padding(vertical = 10.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                val tileBg = when (style) {
                                    AppIconStyle.GLASS_LIGHT -> Color(0xFFE9EEF2)
                                    AppIconStyle.GLASS_DARK -> Color(0xFF1D2026)
                                    AppIconStyle.MINIMAL -> Color(0xFFF7F9FB)
                                    AppIconStyle.GRID -> Color(0xFF2B303A)
                                }
                                val tileFg = when (style) {
                                    AppIconStyle.GLASS_LIGHT, AppIconStyle.MINIMAL -> Color(0xFF1D2229)
                                    AppIconStyle.GLASS_DARK, AppIconStyle.GRID -> Color.White
                                }
                                Box(
                                    modifier = Modifier
                                        .size(38.dp)
                                        .clip(RoundedCornerShape(11.dp))
                                        .background(tileBg)
                                        .border(0.8.dp, p.glassBorder, RoundedCornerShape(11.dp)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (style == AppIconStyle.GRID) IconGridMode(tileFg, 18.dp)
                                    else IconCalculatorBadge(tileFg, 18.dp)
                                }
                                Spacer(modifier = Modifier.height(5.dp))
                                GlassText(style.label, fontSizeSp = 11f, fontWeight = FontWeight.Medium)
                            }
                        }
                    }
                }
            }

            // Glass Toggles (matching Image 2 bottom-4th)
            GlassCard(shape = RoundedCornerShape(24.dp)) {
                Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)) {
                    SettingsSwitchRow(
                        title = "Translucent UI (Liquid Glass)",
                        subtitle = "Frosted backdrop blur & glass layering",
                        checked = settings.translucentUi,
                        onCheckedChange = viewModel::setTranslucentUi
                    )
                    SettingsDivider()
                    SettingsSwitchRow(
                        title = "Reduce Transparency",
                        subtitle = "Higher opacity surfaces for legibility",
                        checked = settings.reduceTransparency,
                        onCheckedChange = viewModel::setReduceTransparency
                    )
                }
            }
        }
    }
}

@Composable
private fun ThemeMiniPreview(mode: ThemeMode) {
    val bg = when (mode) {
        ThemeMode.LIGHT -> Color(0xFFEDF1F5)
        ThemeMode.DARK -> Color(0xFF1A1E24)
        ThemeMode.SYSTEM -> Color(0xFF8A94A3)
    }
    val card = when (mode) {
        ThemeMode.LIGHT -> Color.White
        ThemeMode.DARK -> Color(0xFF292F38)
        ThemeMode.SYSTEM -> Color(0xFFD0D7DF)
    }
    Box(
        modifier = Modifier
            .width(48.dp)
            .height(72.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(bg)
            .border(0.8.dp, Color.White.copy(alpha = 0.4f), RoundedCornerShape(10.dp))
            .padding(5.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(20.dp)
                    .clip(RoundedCornerShape(5.dp))
                    .background(card)
            )
            repeat(3) {
                Row(horizontalArrangement = Arrangement.spacedBy(3.dp)) {
                    repeat(3) {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(10.dp)
                                .clip(CircleShape)
                                .background(card)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun HapticsSoundSettingsScreen(
    settings: AppSettings,
    viewModel: CalculatorViewModel,
    onBack: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .navigationBarsPadding()
    ) {
        GlassSubpageTopBar(title = "Haptics & Sound", onBack = onBack)
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            GlassCard(shape = RoundedCornerShape(24.dp)) {
                Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)) {
                    SettingsSwitchRow(
                        title = "Haptic Feedback",
                        subtitle = "Tactile vibration on keypad press",
                        checked = settings.hapticFeedback,
                        onCheckedChange = viewModel::setHapticFeedback
                    )
                    SettingsDivider()
                    SettingsSwitchRow(
                        title = "Sound",
                        subtitle = "Play a subtle click sound on press",
                        checked = settings.soundEnabled,
                        onCheckedChange = viewModel::setSoundEnabled
                    )
                }
            }

            GlassCard(shape = RoundedCornerShape(24.dp)) {
                Column(modifier = Modifier.padding(16.dp)) {
                    GlassText("Haptic Strength", fontSizeSp = 15f, fontWeight = FontWeight.SemiBold)
                    Spacer(modifier = Modifier.height(10.dp))
                    GlassSegmentedTextBar(
                        options = HapticStrength.entries.map { it.label },
                        selectedIndex = HapticStrength.entries.indexOf(settings.hapticStrength),
                        onSelect = { idx -> viewModel.setHapticStrength(HapticStrength.entries[idx]) }
                    )
                }
            }
        }
    }
}

@Composable
fun CalculationSettingsScreen(
    settings: AppSettings,
    viewModel: CalculatorViewModel,
    onBack: () -> Unit
) {
    val p = LocalGlassPalette.current
    var showPrecisionPicker by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .navigationBarsPadding()
    ) {
        GlassSubpageTopBar(title = "Calculation", onBack = onBack)
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Decimal Precision (matching Image 2 bottom-5th)
            GlassCard(
                shape = RoundedCornerShape(22.dp),
                onClick = { showPrecisionPicker = true }
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        GlassText("Decimal Precision", fontSizeSp = 15.5f, fontWeight = FontWeight.SemiBold)
                        GlassText(settings.decimalPrecision.label, color = p.textSecondary, fontSizeSp = 13f)
                    }
                    IconChevronRight(p.textSecondary, 16.dp)
                }
            }

            // Angle Mode
            GlassCard(shape = RoundedCornerShape(22.dp)) {
                Column(modifier = Modifier.padding(16.dp)) {
                    GlassText("Angle Mode", fontSizeSp = 15.5f, fontWeight = FontWeight.SemiBold)
                    Spacer(modifier = Modifier.height(10.dp))
                    GlassSegmentedTextBar(
                        options = listOf("Degrees (°)", "Radians", "Gradians"),
                        selectedIndex = AngleMode.entries.indexOf(settings.angleMode),
                        onSelect = { idx -> viewModel.setAngleMode(AngleMode.entries[idx]) }
                    )
                }
            }

            // Memory Behavior
            GlassCard(shape = RoundedCornerShape(22.dp)) {
                Column(modifier = Modifier.padding(16.dp)) {
                    GlassText("Memory Behavior", fontSizeSp = 15.5f, fontWeight = FontWeight.SemiBold)
                    Spacer(modifier = Modifier.height(10.dp))
                    GlassSegmentedTextBar(
                        options = MemoryBehavior.entries.map { it.label },
                        selectedIndex = MemoryBehavior.entries.indexOf(settings.memoryBehavior),
                        onSelect = { idx -> viewModel.setMemoryBehavior(MemoryBehavior.entries[idx]) }
                    )
                }
            }

            // Toggles matching Image 2 bottom-5th
            GlassCard(shape = RoundedCornerShape(24.dp)) {
                Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)) {
                    SettingsSwitchRow(
                        title = "Show Thousand Separator",
                        subtitle = "Format 56570 as 56,570",
                        checked = settings.showThousandSeparator,
                        onCheckedChange = viewModel::setThousandSeparator
                    )
                    SettingsDivider()
                    SettingsSwitchRow(
                        title = "Auto Close Brackets",
                        subtitle = "Automatically balance open '(' during evaluation",
                        checked = settings.autoCloseBrackets,
                        onCheckedChange = viewModel::setAutoCloseBrackets
                    )
                    SettingsDivider()
                    SettingsSwitchRow(
                        title = "Expression in Main Display",
                        subtitle = "Emphasize editable formula in primary line",
                        checked = settings.expressionInMainDisplay,
                        onCheckedChange = viewModel::setExpressionInMainDisplay
                    )
                    SettingsDivider()
                    SettingsSwitchRow(
                        title = "Copy Result with Units",
                        subtitle = "Include unit symbol when copying from Converter",
                        checked = settings.copyResultWithUnits,
                        onCheckedChange = viewModel::setCopyResultWithUnits
                    )
                }
            }
        }
    }

    if (showPrecisionPicker) {
        GlassModalCardDialog(onDismiss = { showPrecisionPicker = false }) {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    GlassText("Decimal Precision", fontSizeSp = 18f, fontWeight = FontWeight.SemiBold)
                    GlassCircleButton(onClick = { showPrecisionPicker = false }, contentDescription = "Close", size = 32.dp) {
                        IconCloseX(p.textSecondary, 15.dp)
                    }
                }
                Spacer(modifier = Modifier.height(10.dp))
                DecimalPrecisionMode.entries.forEach { mode ->
                    val sel = mode == settings.decimalPrecision
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 3.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(if (sel) p.activePillSurface else p.cardSurface)
                            .border(0.8.dp, p.glassBorder, RoundedCornerShape(14.dp))
                            .clickable {
                                viewModel.setDecimalPrecision(mode)
                                showPrecisionPicker = false
                            }
                            .padding(horizontal = 14.dp, vertical = 12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        GlassText(mode.label, fontSizeSp = 15f, fontWeight = if (sel) FontWeight.SemiBold else FontWeight.Normal)
                        if (sel) IconCheckmark(p.textPrimary, 17.dp)
                    }
                }
            }
        }
    }
}

@Composable
fun MemorySettingsScreen(
    settings: AppSettings,
    viewModel: CalculatorViewModel,
    onBack: () -> Unit
) {
    val p = LocalGlassPalette.current
    Column(
        modifier = Modifier
            .fillMaxSize()
            .navigationBarsPadding()
    ) {
        GlassSubpageTopBar(title = "Memory", onBack = onBack)
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            GlassCard(shape = RoundedCornerShape(24.dp), containerColor = p.displaySurface) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(18.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        GlassText("Active Memory Register", color = p.textSecondary, fontSizeSp = 13f)
                        GlassText(settings.memoryValue, fontSizeSp = 28f, fontWeight = FontWeight.Bold)
                    }
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(p.danger.copy(alpha = 0.14f))
                            .clickable { viewModel.memoryClear() }
                            .padding(horizontal = 14.dp, vertical = 8.dp)
                    ) {
                        GlassText("Clear MC", color = p.danger, fontSizeSp = 13.5f, fontWeight = FontWeight.SemiBold)
                    }
                }
            }

            if (settings.memorySlots.isNotEmpty()) {
                GlassText("Stored Slots (tap to insert)", color = p.textSecondary, fontSizeSp = 13f, fontWeight = FontWeight.SemiBold)
                settings.memorySlots.forEachIndexed { idx, slot ->
                    GlassCard(
                        shape = RoundedCornerShape(18.dp),
                        onClick = { viewModel.recallMemorySlot(slot) }
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 14.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            GlassText(slot, fontSizeSp = 17f, fontWeight = FontWeight.SemiBold)
                            Box(
                                modifier = Modifier
                                    .size(30.dp)
                                    .clip(CircleShape)
                                    .clickable { viewModel.removeMemorySlot(idx) },
                                contentAlignment = Alignment.Center
                            ) {
                                IconTrashBin(p.textSecondary, 16.dp)
                            }
                        }
                    }
                }
            } else {
                GlassCard(shape = RoundedCornerShape(20.dp)) {
                    Box(modifier = Modifier.fillMaxWidth().padding(18.dp)) {
                        GlassText(
                            "Use MS, M+, or M- in Scientific mode to save values to memory.",
                            color = p.textSecondary,
                            fontSizeSp = 14f
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun AccessibilitySettingsScreen(
    settings: AppSettings,
    viewModel: CalculatorViewModel,
    onBack: () -> Unit
) {
    val p = LocalGlassPalette.current
    Column(
        modifier = Modifier
            .fillMaxSize()
            .navigationBarsPadding()
    ) {
        GlassSubpageTopBar(title = "Accessibility", onBack = onBack)
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Text Size Slider (matching Image 2 bottom-6th)
            GlassCard(shape = RoundedCornerShape(24.dp)) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        GlassText("Text Size", fontSizeSp = 15.5f, fontWeight = FontWeight.SemiBold)
                        GlassText("${(settings.textScale * 100).toInt()}%", color = p.textSecondary, fontSizeSp = 14f)
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        GlassText("A", fontSizeSp = 13f, fontWeight = FontWeight.SemiBold)
                        GlassValueSlider(
                            value = (settings.textScale - 0.85f) / 0.45f,
                            onValueChange = { frac ->
                                viewModel.setTextScale(0.85f + frac * 0.45f)
                            },
                            modifier = Modifier.weight(1f)
                        )
                        GlassText("A", fontSizeSp = 21f, fontWeight = FontWeight.Bold)
                    }
                }
            }

            // Accessibility toggles (matching Image 2 bottom-6th)
            GlassCard(shape = RoundedCornerShape(24.dp)) {
                Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)) {
                    SettingsSwitchRow(
                        title = "High Contrast",
                        subtitle = "Increase border & text contrast for readability",
                        checked = settings.highContrast,
                        onCheckedChange = viewModel::setHighContrast
                    )
                    SettingsDivider()
                    SettingsSwitchRow(
                        title = "Button Labels",
                        subtitle = "Show descriptive labels under scientific functions",
                        checked = settings.buttonLabels,
                        onCheckedChange = viewModel::setButtonLabels
                    )
                    SettingsDivider()
                    SettingsSwitchRow(
                        title = "Haptic Feedback",
                        subtitle = "Light vibration on button press",
                        checked = settings.hapticFeedback,
                        onCheckedChange = viewModel::setHapticFeedback
                    )
                    SettingsDivider()
                    SettingsSwitchRow(
                        title = "Sound",
                        subtitle = "Play a subtle click sound",
                        checked = settings.soundEnabled,
                        onCheckedChange = viewModel::setSoundEnabled
                    )
                    SettingsDivider()
                    SettingsSwitchRow(
                        title = "Voice Results",
                        subtitle = "Read the calculated result aloud",
                        checked = settings.voiceResults,
                        onCheckedChange = viewModel::setVoiceResults
                    )
                }
            }
        }
    }
}

@Composable
fun AboutSettingsScreen(
    settings: AppSettings,
    updateStatus: UpdateStatus,
    viewModel: CalculatorViewModel,
    onBack: () -> Unit
) {
    val p = LocalGlassPalette.current
    val context = androidx.compose.ui.platform.LocalContext.current
    Column(
        modifier = Modifier
            .fillMaxSize()
            .navigationBarsPadding()
    ) {
        GlassSubpageTopBar(title = "About & Updates", onBack = onBack)
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            GlassCard(shape = RoundedCornerShape(26.dp), containerColor = p.displaySurface) {
                Column(
                    modifier = Modifier.fillMaxWidth().padding(22.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(64.dp)
                            .clip(RoundedCornerShape(18.dp))
                            .background(p.accent),
                        contentAlignment = Alignment.Center
                    ) {
                        IconCalculatorBadge(p.onAccent, 32.dp)
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    GlassText("eryx", fontSizeSp = 22f, fontWeight = FontWeight.Bold)
                    GlassText("Version ${BuildConfig.VERSION_NAME} • Liquid Glass Edition", color = p.textSecondary, fontSizeSp = 13.5f)
                }
            }

            // Software Update Card
            GlassCard(shape = RoundedCornerShape(22.dp)) {
                Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    GlassText("Software Updates", fontSizeSp = 15.5f, fontWeight = FontWeight.SemiBold)
                    val statusText = when (updateStatus) {
                        UpdateStatus.Idle -> "Check GitHub releases for new eryx versions."
                        UpdateStatus.Checking -> "Checking for updates..."
                        is UpdateStatus.UpToDate -> "You're on the latest version (v${BuildConfig.VERSION_NAME})."
                        is UpdateStatus.Available -> "Version ${updateStatus.info.versionName} is available!\n${updateStatus.info.releaseNotes}"
                        is UpdateStatus.Downloading -> "Downloading v${updateStatus.info.versionName} (${(updateStatus.progress * 100).toInt()}%)..."
                        is UpdateStatus.ReadyToInstall -> "v${updateStatus.info.versionName} downloaded and ready to install."
                        is UpdateStatus.Error -> updateStatus.message
                    }
                    GlassText(statusText, color = p.textSecondary, fontSizeSp = 13.5f)

                    if (updateStatus is UpdateStatus.Downloading) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(6.dp)
                                .clip(RoundedCornerShape(999.dp))
                                .background(p.switchTrackOff)
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth(updateStatus.progress.coerceIn(0.05f, 1f))
                                    .height(6.dp)
                                    .clip(RoundedCornerShape(999.dp))
                                    .background(p.accent)
                            )
                        }
                    }

                    val btnLabel = when (updateStatus) {
                        UpdateStatus.Checking -> "Checking..."
                        is UpdateStatus.Available -> "Download & Install v${updateStatus.info.versionName}"
                        is UpdateStatus.Downloading -> "Downloading ${(updateStatus.progress * 100).toInt()}%"
                        is UpdateStatus.ReadyToInstall -> "Install Update Now"
                        else -> "Check for Updates"
                    }
                    val enabled = updateStatus !is UpdateStatus.Checking && updateStatus !is UpdateStatus.Downloading
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(44.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(if (enabled) p.accent else p.secondaryKeySurface)
                            .clickable(enabled = enabled) {
                                when (updateStatus) {
                                    is UpdateStatus.Available -> viewModel.downloadAndInstallUpdate(context)
                                    is UpdateStatus.ReadyToInstall -> viewModel.installReadyApk(context)
                                    else -> viewModel.checkForUpdates(manual = true)
                                }
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        GlassText(
                            text = btnLabel,
                            color = if (enabled) p.onAccent else p.textSecondary,
                            fontSizeSp = 14.5f,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    SettingsDivider()
                    SettingsSwitchRow(
                        title = "Auto-check for Updates",
                        subtitle = "Quietly check GitHub releases once every 12h",
                        checked = settings.autoCheckUpdates,
                        onCheckedChange = viewModel::setAutoCheckUpdates
                    )
                }
            }

            GlassCard(shape = RoundedCornerShape(22.dp)) {
                Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    GlassText("Privacy & Performance", fontSizeSp = 15.5f, fontWeight = FontWeight.SemiBold)
                    GlassText(
                        "100% free, ad-free, offline-first, and zero tracking. Internet access is used solely when you request live ECB currency exchange rates.",
                        color = p.textSecondary,
                        fontSizeSp = 13.5f
                    )
                }
            }

            GlassCard(shape = RoundedCornerShape(22.dp)) {
                Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    GlassText("Gestures & Shortcuts", fontSizeSp = 15.5f, fontWeight = FontWeight.SemiBold)
                    GlassText("• Swipe left on display to Undo, swipe right to Redo", color = p.textSecondary, fontSizeSp = 13f)
                    GlassText("• Tap the display result to Copy, Paste, or Share", color = p.textSecondary, fontSizeSp = 13f)
                    GlassText("• Tap [⛶] in display to launch Interactive Graph", color = p.textSecondary, fontSizeSp = 13f)
                    GlassText("• Swipe left on History rows for Copy / Edit / Delete", color = p.textSecondary, fontSizeSp = 13f)
                    GlassText("• Rotate device for wide Landscape Scientific keypad", color = p.textSecondary, fontSizeSp = 13f)
                }
            }
        }
    }
}

@Composable
private fun GlassValueSlider(
    value: Float,
    onValueChange: (Float) -> Unit,
    modifier: Modifier = Modifier
) {
    val p = LocalGlassPalette.current
    var trackWidthPx by remember { mutableStateOf(1) }
    val clamped = value.coerceIn(0f, 1f)

    Box(
        modifier = modifier
            .height(32.dp)
            .onSizeChanged { trackWidthPx = it.width.coerceAtLeast(1) }
            .pointerInput(Unit) {
                detectHorizontalDragGestures { change, _ ->
                    change.consume()
                    val frac = (change.position.x / trackWidthPx).coerceIn(0f, 1f)
                    onValueChange(frac)
                }
            },
        contentAlignment = Alignment.CenterStart
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .clip(RoundedCornerShape(999.dp))
                .background(p.switchTrackOff)
        )
        Box(
            modifier = Modifier
                .fillMaxWidth(clamped)
                .height(6.dp)
                .clip(RoundedCornerShape(999.dp))
                .background(p.accent)
        )
        val thumbSizePx = 22.dp
        Box(
            modifier = Modifier
                .offset {
                    val travel = (trackWidthPx - 56).coerceAtLeast(1)
                    IntOffset((clamped * travel).roundToInt(), 0)
                }
                .size(thumbSizePx)
                .clip(CircleShape)
                .background(Color.White)
                .border(1.5.dp, p.accent, CircleShape)
        )
    }
}

@Composable
private fun SettingsNavRow(
    title: String,
    subtitle: String,
    badgeText: String,
    onClick: () -> Unit
) {
    val p = LocalGlassPalette.current
    GlassCard(shape = RoundedCornerShape(22.dp), onClick = onClick) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 15.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(RoundedCornerShape(11.dp))
                    .background(p.secondaryKeySurface)
                    .border(0.8.dp, p.glassBorder, RoundedCornerShape(11.dp)),
                contentAlignment = Alignment.Center
            ) {
                GlassText(badgeText, fontSizeSp = 15f, fontWeight = FontWeight.SemiBold)
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                GlassText(title, fontSizeSp = 15.5f, fontWeight = FontWeight.SemiBold)
                GlassText(subtitle, color = p.textSecondary, fontSizeSp = 12.5f)
            }
            IconChevronRight(p.textSecondary, 16.dp)
        }
    }
}

@Composable
private fun SettingsSwitchRow(
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    val p = LocalGlassPalette.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onCheckedChange(!checked) }
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(modifier = Modifier.weight(1f).padding(end = 12.dp)) {
            GlassText(title, fontSizeSp = 15f, fontWeight = FontWeight.Medium)
            Spacer(modifier = Modifier.height(2.dp))
            GlassText(subtitle, color = p.textSecondary, fontSizeSp = 12f)
        }
        GlassSwitch(checked = checked, onCheckedChange = onCheckedChange)
    }
}

@Composable
private fun SettingsDivider() {
    val p = LocalGlassPalette.current
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(0.8.dp)
            .background(p.subtleDivider)
    )
}
