package com.liquidglass.calculator.ui.screens

import android.content.res.Configuration
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicText
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalTextInputService
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.liquidglass.calculator.AppDestination
import com.liquidglass.calculator.CalculatorUiState
import com.liquidglass.calculator.CalculatorViewModel
import com.liquidglass.calculator.data.AppSettings
import com.liquidglass.calculator.data.CalculatorMode
import com.liquidglass.calculator.data.OneHandedMode
import com.liquidglass.calculator.engine.AngleMode
import com.liquidglass.calculator.engine.FinancialTool
import com.liquidglass.calculator.ui.components.GlassCard
import com.liquidglass.calculator.ui.components.GlassCircleButton
import com.liquidglass.calculator.ui.components.GlassKeyButton
import com.liquidglass.calculator.ui.components.GlassModalCardDialog
import com.liquidglass.calculator.ui.components.GlassSegmentedModeBar
import com.liquidglass.calculator.ui.components.GlassSegmentedTextBar
import com.liquidglass.calculator.ui.components.GlassText
import com.liquidglass.calculator.ui.components.IconBackspaceKey
import com.liquidglass.calculator.ui.components.IconCalculatorBadge
import com.liquidglass.calculator.ui.components.IconCheckmark
import com.liquidglass.calculator.ui.components.IconCloseX
import com.liquidglass.calculator.ui.components.IconCopySheets
import com.liquidglass.calculator.ui.components.IconExpandCorners
import com.liquidglass.calculator.ui.components.IconGraphWave
import com.liquidglass.calculator.ui.components.IconGridMode
import com.liquidglass.calculator.ui.components.IconHistoryClock
import com.liquidglass.calculator.ui.components.IconMoreDots
import com.liquidglass.calculator.ui.components.IconRedoArrow
import com.liquidglass.calculator.ui.components.IconSettingsGear
import com.liquidglass.calculator.ui.components.IconShareArrow
import com.liquidglass.calculator.ui.components.IconSwapArrows
import com.liquidglass.calculator.ui.components.IconUndoArrow
import com.liquidglass.calculator.ui.theme.LocalGlassPalette

@Composable
fun CalculatorScreen(
    viewModel: CalculatorViewModel,
    settings: AppSettings,
    calcState: CalculatorUiState,
    converterContent: @Composable () -> Unit
) {
    val p = LocalGlassPalette.current
    val context = LocalContext.current
    val config = LocalConfiguration.current
    val isLandscape = config.orientation == Configuration.ORIENTATION_LANDSCAPE

    var showModeModal by remember { mutableStateOf(false) }
    var showMoreMenu by remember { mutableStateOf(false) }
    var showDisplayActions by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(horizontal = if (isLandscape) 16.dp else 14.dp, vertical = 8.dp)
    ) {
        // Top Navigation Bar (matching Images 1, 3, 4)
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            GlassCircleButton(
                onClick = { showModeModal = true },
                contentDescription = "Calculator Modes",
                size = 40.dp
            ) {
                IconGridMode(p.textPrimary, 19.dp)
            }

            GlassSegmentedModeBar(
                selectedMode = settings.lastMode,
                onSelectMode = { viewModel.selectMode(it, alsoNavigateToCalculator = false) },
                modifier = Modifier.weight(1f)
            )

            GlassCircleButton(
                onClick = { viewModel.navigateTo(AppDestination.HISTORY) },
                contentDescription = "History",
                size = 40.dp
            ) {
                IconHistoryClock(p.textPrimary, 19.dp)
            }

            GlassCircleButton(
                onClick = { showMoreMenu = true },
                contentDescription = "More tools and settings",
                size = 40.dp
            ) {
                IconMoreDots(p.textPrimary, 19.dp)
            }
        }

        Spacer(modifier = Modifier.height(if (isLandscape) 8.dp else 12.dp))

        if (settings.lastMode == CalculatorMode.CONVERTER) {
            converterContent()
        } else if (isLandscape) {
            // Side-by-side Landscape Layout: Display + Scientific left, Numeric Standard right
            Row(
                modifier = Modifier.fillMaxSize(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Column(
                    modifier = Modifier.weight(1.05f).fillMaxHeight(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    CalculatorDisplayCard(
                        settings = settings,
                        calcState = calcState,
                        onExpressionChange = viewModel::onExpressionFieldChanged,
                        onUndo = viewModel::undo,
                        onRedo = viewModel::redo,
                        onExpandClick = { viewModel.navigateTo(AppDestination.GRAPH) },
                        onDisplayActions = { showDisplayActions = true },
                        modifier = Modifier.weight(0.46f)
                    )
                    ScientificFunctionsCompactPanel(
                        viewModel = viewModel,
                        settings = settings,
                        calcState = calcState,
                        modifier = Modifier.weight(0.54f)
                    )
                }
                StandardKeypad(
                    viewModel = viewModel,
                    calcState = calcState,
                    modifier = Modifier.weight(0.95f).fillMaxHeight()
                )
            }
        } else {
            // Portrait Layout: generous display + compact lower-thumb keypad with One-Handed support
            val isStd = settings.lastMode == CalculatorMode.STANDARD
            val keypadWidthFrac by animateFloatAsState(
                targetValue = if (settings.oneHandedMode == OneHandedMode.FULL) 1f else 0.84f,
                animationSpec = spring(dampingRatio = 0.82f, stiffness = 420f),
                label = "oneHandWidth"
            )

            CalculatorDisplayCard(
                settings = settings,
                calcState = calcState,
                onExpressionChange = viewModel::onExpressionFieldChanged,
                onUndo = viewModel::undo,
                onRedo = viewModel::redo,
                onExpandClick = { viewModel.navigateTo(AppDestination.GRAPH) },
                onCycleReachability = {
                    val next = OneHandedMode.entries[(settings.oneHandedMode.ordinal + 1) % OneHandedMode.entries.size]
                    viewModel.setOneHandedMode(next)
                },
                onDisplayActions = { showDisplayActions = true },
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(if (isStd) 1f else 0.25f)
            )

            Spacer(modifier = Modifier.height(12.dp))

            Box(
                modifier = if (isStd) {
                    Modifier.fillMaxWidth()
                } else {
                    Modifier.fillMaxWidth().weight(0.75f)
                },
                contentAlignment = when (settings.oneHandedMode) {
                    OneHandedMode.FULL -> Alignment.BottomCenter
                    OneHandedMode.LEFT -> Alignment.BottomStart
                    OneHandedMode.RIGHT -> Alignment.BottomEnd
                }
            ) {
                if (isStd) {
                    StandardKeypad(
                        viewModel = viewModel,
                        calcState = calcState,
                        isPortraitCompact = true,
                        modifier = Modifier.fillMaxWidth(keypadWidthFrac)
                    )
                } else {
                    ScientificKeypad(
                        viewModel = viewModel,
                        settings = settings,
                        calcState = calcState,
                        modifier = Modifier
                            .fillMaxWidth(keypadWidthFrac)
                            .fillMaxHeight()
                    )
                }
            }
        }
    }

    // Mode Switcher Modal (matching Image 1 bottom-right & Image 3 bottom-left)
    if (showModeModal) {
        GlassModalCardDialog(onDismiss = { showModeModal = false }) {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    GlassText(text = "Calculator Mode", fontSizeSp = 18f, fontWeight = FontWeight.SemiBold)
                    GlassCircleButton(
                        onClick = { showModeModal = false },
                        contentDescription = "Close",
                        size = 32.dp
                    ) {
                        IconCloseX(p.textSecondary, 15.dp)
                    }
                }
                Spacer(modifier = Modifier.height(14.dp))

                ModeModalRow(
                    label = "Standard",
                    selected = settings.lastMode == CalculatorMode.STANDARD,
                    icon = { IconCalculatorBadge(p.textPrimary, 19.dp) },
                    onClick = {
                        viewModel.selectMode(CalculatorMode.STANDARD)
                        showModeModal = false
                    }
                )
                ModeModalRow(
                    label = "Scientific",
                    selected = settings.lastMode == CalculatorMode.SCIENTIFIC,
                    icon = { GlassText("ƒ(x)", fontSizeSp = 15f, fontWeight = FontWeight.SemiBold) },
                    onClick = {
                        viewModel.selectMode(CalculatorMode.SCIENTIFIC)
                        showModeModal = false
                    }
                )
                ModeModalRow(
                    label = "Converter",
                    selected = settings.lastMode == CalculatorMode.CONVERTER,
                    icon = { IconSwapArrows(p.textPrimary, 19.dp) },
                    onClick = {
                        viewModel.selectMode(CalculatorMode.CONVERTER)
                        showModeModal = false
                    }
                )
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                        .height(0.8.dp)
                        .background(p.subtleDivider)
                )
                ModeModalRow(
                    label = "Interactive Graph",
                    selected = false,
                    icon = { IconGraphWave(p.textPrimary, 19.dp) },
                    onClick = {
                        showModeModal = false
                        viewModel.navigateTo(AppDestination.GRAPH)
                    }
                )
                ModeModalRow(
                    label = "Functions Library",
                    selected = false,
                    icon = { GlassText("ƒ", fontSizeSp = 16f, fontWeight = FontWeight.Bold) },
                    onClick = {
                        showModeModal = false
                        viewModel.navigateTo(AppDestination.FUNCTIONS)
                    }
                )
                ModeModalRow(
                    label = "Financial Calculators (GST / EMI)",
                    selected = false,
                    icon = { GlassText("%", fontSizeSp = 15f, fontWeight = FontWeight.Bold) },
                    onClick = {
                        showModeModal = false
                        viewModel.openFinancialTool(FinancialTool.GST)
                    }
                )
                ModeModalRow(
                    label = "Home Dashboard",
                    selected = false,
                    icon = { IconGridMode(p.textPrimary, 18.dp) },
                    onClick = {
                        showModeModal = false
                        viewModel.navigateTo(AppDestination.HOME)
                    }
                )
            }
        }
    }

    // More Menu Modal (Settings, Copy/Paste, Share, Undo/Redo)
    if (showMoreMenu) {
        GlassModalCardDialog(onDismiss = { showMoreMenu = false }) {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    GlassText(text = "Tools & Actions", fontSizeSp = 18f, fontWeight = FontWeight.SemiBold)
                    GlassCircleButton(
                        onClick = { showMoreMenu = false },
                        contentDescription = "Close",
                        size = 32.dp
                    ) {
                        IconCloseX(p.textSecondary, 15.dp)
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))

                ActionSheetRow("Graph Plotter", { IconGraphWave(p.textPrimary, 18.dp) }) {
                    showMoreMenu = false
                    viewModel.navigateTo(AppDestination.GRAPH)
                }
                ActionSheetRow("Financial Calculators", { GlassText("%", fontSizeSp = 16f, fontWeight = FontWeight.Bold) }) {
                    showMoreMenu = false
                    viewModel.openFinancialTool(FinancialTool.GST)
                }
                ActionSheetRow("Functions Reference", { GlassText("ƒ(x)", fontSizeSp = 14f, fontWeight = FontWeight.SemiBold) }) {
                    showMoreMenu = false
                    viewModel.navigateTo(AppDestination.FUNCTIONS)
                }
                ActionSheetRow("Copy Result", { IconCopySheets(p.textPrimary, 18.dp) }) {
                    showMoreMenu = false
                    viewModel.copyResult(context)
                }
                ActionSheetRow("Paste Expression", { IconCopySheets(p.textSecondary, 18.dp) }) {
                    showMoreMenu = false
                    viewModel.pasteFromClipboard(context)
                }
                ActionSheetRow("Share Calculation", { IconShareArrow(p.textPrimary, 18.dp) }) {
                    showMoreMenu = false
                    viewModel.shareCalculation(context)
                }
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 6.dp)
                        .height(0.8.dp)
                        .background(p.subtleDivider)
                )
                ActionSheetRow("Settings", { IconSettingsGear(p.textPrimary, 18.dp) }) {
                    showMoreMenu = false
                    viewModel.navigateTo(AppDestination.SETTINGS)
                }
                Spacer(modifier = Modifier.height(10.dp))
                GlassText("One-Handed Reachability", color = p.textSecondary, fontSizeSp = 12.5f, fontWeight = FontWeight.SemiBold)
                Spacer(modifier = Modifier.height(6.dp))
                GlassSegmentedTextBar(
                    options = OneHandedMode.entries.map { it.label },
                    selectedIndex = OneHandedMode.entries.indexOf(settings.oneHandedMode),
                    onSelect = { idx -> viewModel.setOneHandedMode(OneHandedMode.entries[idx]) }
                )
            }
        }
    }

    // Display Quick Actions Dialog (on tap/long-press result)
    if (showDisplayActions) {
        GlassModalCardDialog(onDismiss = { showDisplayActions = false }) {
            Column {
                GlassText(
                    text = calcState.liveResult,
                    fontSizeSp = 22f,
                    fontWeight = FontWeight.Bold
                )
                if (calcState.expressionField.text.isNotBlank()) {
                    Spacer(modifier = Modifier.height(2.dp))
                    GlassText(
                        text = calcState.expressionField.text,
                        color = p.textSecondary,
                        fontSizeSp = 14f
                    )
                }
                Spacer(modifier = Modifier.height(14.dp))
                ActionSheetRow("Copy Result", { IconCopySheets(p.textPrimary, 18.dp) }) {
                    showDisplayActions = false
                    viewModel.copyResult(context)
                }
                ActionSheetRow("Copy Full Equation", { IconCopySheets(p.textSecondary, 18.dp) }) {
                    showDisplayActions = false
                    val eq = "${calcState.expressionField.text} = ${calcState.liveResult}"
                    viewModel.copyResult(context, eq)
                }
                ActionSheetRow("Paste from Clipboard", { IconCopySheets(p.textPrimary, 18.dp) }) {
                    showDisplayActions = false
                    viewModel.pasteFromClipboard(context)
                }
                ActionSheetRow("Share Calculation", { IconShareArrow(p.textPrimary, 18.dp) }) {
                    showDisplayActions = false
                    viewModel.shareCalculation(context)
                }
            }
        }
    }
}

@Composable
private fun CalculatorDisplayCard(
    settings: AppSettings,
    calcState: CalculatorUiState,
    onExpressionChange: (TextFieldValue) -> Unit,
    onUndo: () -> Unit,
    onRedo: () -> Unit,
    onExpandClick: () -> Unit,
    onCycleReachability: (() -> Unit)? = null,
    onDisplayActions: () -> Unit,
    modifier: Modifier = Modifier
) {
    val p = LocalGlassPalette.current
    var dragAccum by remember { mutableFloatStateOf(0f) }

    GlassCard(
        modifier = modifier.pointerInput(Unit) {
            detectHorizontalDragGestures(
                onDragStart = { dragAccum = 0f },
                onHorizontalDrag = { _, dragAmount -> dragAccum += dragAmount },
                onDragEnd = {
                    if (dragAccum < -70f) onUndo()
                    else if (dragAccum > 70f) onRedo()
                    dragAccum = 0f
                }
            )
        },
        shape = RoundedCornerShape(28.dp),
        containerColor = p.displaySurface
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 18.dp, vertical = 14.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Top bar inside display: status badges + Undo/Redo + Editable Expression
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    if (settings.memoryValue != "0") {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(p.accent.copy(alpha = 0.14f))
                                .padding(horizontal = 7.dp, vertical = 2.dp)
                        ) {
                            GlassText(
                                text = "M",
                                color = p.accent,
                                fontSizeSp = 11f,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                    if (settings.lastMode == CalculatorMode.SCIENTIFIC) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(p.secondaryKeySurface)
                                .padding(horizontal = 7.dp, vertical = 2.dp)
                        ) {
                            GlassText(
                                text = settings.angleMode.shortLabel.uppercase(),
                                color = p.textSecondary,
                                fontSizeSp = 10.5f,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                    AnimatedVisibility(visible = calcState.canUndo, enter = fadeIn(), exit = fadeOut()) {
                        Box(
                            modifier = Modifier
                                .size(28.dp)
                                .clip(CircleShape)
                                .background(p.secondaryKeySurface)
                                .clickable(onClick = onUndo),
                            contentAlignment = Alignment.Center
                        ) {
                            IconUndoArrow(p.textSecondary, 14.dp)
                        }
                    }
                    AnimatedVisibility(visible = calcState.canRedo, enter = fadeIn(), exit = fadeOut()) {
                        Box(
                            modifier = Modifier
                                .size(28.dp)
                                .clip(CircleShape)
                                .background(p.secondaryKeySurface)
                                .clickable(onClick = onRedo),
                            contentAlignment = Alignment.Center
                        ) {
                            IconRedoArrow(p.textSecondary, 14.dp)
                        }
                    }
                }

                // Editable expression field (suppresses soft IME keyboard so calculator keypad stays clean)
                val exprFontSize = if (settings.expressionInMainDisplay) 26f else 18f
                @Suppress("DEPRECATION")
                CompositionLocalProvider(LocalTextInputService provides null) {
                    BasicTextField(
                        value = calcState.expressionField,
                        onValueChange = onExpressionChange,
                        singleLine = true,
                        cursorBrush = SolidColor(p.accent),
                        textStyle = TextStyle(
                            color = if (settings.expressionInMainDisplay) p.textPrimary else p.textSecondary,
                            fontSize = (exprFontSize * p.textScale).sp,
                            fontWeight = if (settings.expressionInMainDisplay) FontWeight.SemiBold else FontWeight.Medium,
                            textAlign = TextAlign.End,
                            fontFeatureSettings = "tnum"
                        ),
                        modifier = Modifier
                            .weight(1f)
                            .padding(start = 10.dp),
                        decorationBox = { innerTextField ->
                            Box(contentAlignment = Alignment.CenterEnd) {
                                if (calcState.expressionField.text.isEmpty()) {
                                    BasicText(
                                        text = "0",
                                        style = TextStyle(
                                            color = p.textMuted,
                                            fontSize = (exprFontSize * p.textScale).sp,
                                            textAlign = TextAlign.End
                                        )
                                    )
                                }
                                innerTextField()
                            }
                        }
                    )
                }
            }

            // Bottom row inside display: [⛶] Expand/Graph + Reachability pill on left, large tabular Result on right
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Bottom,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(34.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(p.secondaryKeySurface)
                            .border(0.7.dp, p.glassBorder, RoundedCornerShape(10.dp))
                            .clickable(onClick = onExpandClick),
                        contentAlignment = Alignment.Center
                    ) {
                        IconExpandCorners(p.textSecondary, 16.dp)
                    }
                    if (onCycleReachability != null) {
                        val reachLabel = when (settings.oneHandedMode) {
                            OneHandedMode.FULL -> "⇔"
                            OneHandedMode.LEFT -> "◀"
                            OneHandedMode.RIGHT -> "▶"
                        }
                        Box(
                            modifier = Modifier
                                .height(34.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(
                                    if (settings.oneHandedMode != OneHandedMode.FULL) p.accent.copy(alpha = 0.14f)
                                    else p.secondaryKeySurface
                                )
                                .border(0.7.dp, p.glassBorder, RoundedCornerShape(10.dp))
                                .clickable(onClick = onCycleReachability)
                                .padding(horizontal = 10.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            GlassText(
                                text = reachLabel,
                                color = if (settings.oneHandedMode != OneHandedMode.FULL) p.accent else p.textSecondary,
                                fontSizeSp = 13f,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }

                val displayString = calcState.errorMessage ?: calcState.liveResult
                val len = displayString.length
                val baseSize = when {
                    settings.expressionInMainDisplay -> 24f
                    len <= 7 -> 48f
                    len <= 10 -> 38f
                    len <= 14 -> 30f
                    else -> 24f
                }

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .padding(start = 12.dp)
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                            onClick = onDisplayActions
                        ),
                    contentAlignment = Alignment.BottomEnd
                ) {
                    BasicText(
                        text = displayString,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        style = TextStyle(
                            color = if (calcState.errorMessage != null) p.danger else p.textPrimary,
                            fontSize = (baseSize * p.textScale).sp,
                            fontWeight = FontWeight.SemiBold,
                            textAlign = TextAlign.End,
                            fontFeatureSettings = "tnum"
                        )
                    )
                }
            }
        }
    }
}

@Composable
private fun StandardKeypad(
    viewModel: CalculatorViewModel,
    calcState: CalculatorUiState,
    isPortraitCompact: Boolean = false,
    modifier: Modifier = Modifier
) {
    val p = LocalGlassPalette.current
    val clearLabel = if (calcState.expressionField.text.isEmpty()) "AC" else "C"

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(10.dp, Alignment.Bottom)
    ) {
        val rowMod = if (isPortraitCompact) {
            Modifier.height(68.dp).fillMaxWidth()
        } else {
            Modifier.weight(1f).fillMaxWidth()
        }
        // Row 1: AC (Red), Backspace, %, ÷ (Amber)
        Row(modifier = rowMod, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            CalcKey(
                label = clearLabel,
                modifier = Modifier.weight(1f).fillMaxHeight(),
                containerColor = p.clearKeySurface,
                textColor = p.clearKeyText,
                fontSizeSp = 22f,
                fontWeight = FontWeight.SemiBold,
                borderColor = p.clearKeyBorder
            ) {
                viewModel.clearAll()
            }
            GlassKeyButton(
                onClick = viewModel::backspace,
                modifier = Modifier.weight(1f).fillMaxHeight(),
                containerColor = p.secondaryKeySurface,
                contentDescription = "Backspace"
            ) {
                IconBackspaceKey(p.textPrimary, 25.dp)
            }
            CalcKey("%", Modifier.weight(1f).fillMaxHeight(), p.secondaryKeySurface, p.textPrimary, 23f, FontWeight.SemiBold) {
                viewModel.insertToken("%")
            }
            CalcKey(
                label = "÷",
                modifier = Modifier.weight(1f).fillMaxHeight(),
                containerColor = p.operatorKeySurface,
                textColor = p.operatorKeyText,
                fontSizeSp = 32f,
                fontWeight = FontWeight.SemiBold,
                borderColor = p.operatorKeyBorder
            ) {
                viewModel.insertToken("÷")
            }
        }
        // Row 2: 7 8 9 ×
        Row(modifier = rowMod, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            CalcKey("7", Modifier.weight(1f).fillMaxHeight(), p.keySurface, p.textPrimary, 25f, FontWeight.Medium) { viewModel.insertToken("7") }
            CalcKey("8", Modifier.weight(1f).fillMaxHeight(), p.keySurface, p.textPrimary, 25f, FontWeight.Medium) { viewModel.insertToken("8") }
            CalcKey("9", Modifier.weight(1f).fillMaxHeight(), p.keySurface, p.textPrimary, 25f, FontWeight.Medium) { viewModel.insertToken("9") }
            CalcKey(
                label = "×",
                modifier = Modifier.weight(1f).fillMaxHeight(),
                containerColor = p.operatorKeySurface,
                textColor = p.operatorKeyText,
                fontSizeSp = 31f,
                fontWeight = FontWeight.SemiBold,
                borderColor = p.operatorKeyBorder
            ) {
                viewModel.insertToken("×")
            }
        }
        // Row 3: 4 5 6 −
        Row(modifier = rowMod, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            CalcKey("4", Modifier.weight(1f).fillMaxHeight(), p.keySurface, p.textPrimary, 25f, FontWeight.Medium) { viewModel.insertToken("4") }
            CalcKey("5", Modifier.weight(1f).fillMaxHeight(), p.keySurface, p.textPrimary, 25f, FontWeight.Medium) { viewModel.insertToken("5") }
            CalcKey("6", Modifier.weight(1f).fillMaxHeight(), p.keySurface, p.textPrimary, 25f, FontWeight.Medium) { viewModel.insertToken("6") }
            CalcKey(
                label = "−",
                modifier = Modifier.weight(1f).fillMaxHeight(),
                containerColor = p.operatorKeySurface,
                textColor = p.operatorKeyText,
                fontSizeSp = 32f,
                fontWeight = FontWeight.SemiBold,
                borderColor = p.operatorKeyBorder
            ) {
                viewModel.insertToken("−")
            }
        }
        // Row 4: 1 2 3 +
        Row(modifier = rowMod, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            CalcKey("1", Modifier.weight(1f).fillMaxHeight(), p.keySurface, p.textPrimary, 25f, FontWeight.Medium) { viewModel.insertToken("1") }
            CalcKey("2", Modifier.weight(1f).fillMaxHeight(), p.keySurface, p.textPrimary, 25f, FontWeight.Medium) { viewModel.insertToken("2") }
            CalcKey("3", Modifier.weight(1f).fillMaxHeight(), p.keySurface, p.textPrimary, 25f, FontWeight.Medium) { viewModel.insertToken("3") }
            CalcKey(
                label = "+",
                modifier = Modifier.weight(1f).fillMaxHeight(),
                containerColor = p.operatorKeySurface,
                textColor = p.operatorKeyText,
                fontSizeSp = 31f,
                fontWeight = FontWeight.SemiBold,
                borderColor = p.operatorKeyBorder
            ) {
                viewModel.insertToken("+")
            }
        }
        // Row 5: +/- 0 . =
        Row(modifier = rowMod, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            CalcKey("+/−", Modifier.weight(1f).fillMaxHeight(), p.keySurface, p.textPrimary, 21f, FontWeight.SemiBold) {
                viewModel.toggleSign()
            }
            CalcKey("0", Modifier.weight(1f).fillMaxHeight(), p.keySurface, p.textPrimary, 25f, FontWeight.Medium) { viewModel.insertToken("0") }
            CalcKey(".", Modifier.weight(1f).fillMaxHeight(), p.keySurface, p.textPrimary, 29f, FontWeight.Bold) {
                viewModel.insertToken(".")
            }
            CalcKey(
                label = "=",
                modifier = Modifier.weight(1f).fillMaxHeight(),
                containerColor = p.accent,
                textColor = p.onAccent,
                fontSizeSp = 34f,
                fontWeight = FontWeight.Bold,
                pressedColor = p.accent.copy(alpha = 0.85f)
            ) {
                viewModel.evaluateEquals()
            }
        }
    }
}

@Composable
private fun ScientificKeypad(
    viewModel: CalculatorViewModel,
    settings: AppSettings,
    calcState: CalculatorUiState,
    modifier: Modifier = Modifier
) {
    val p = LocalGlassPalette.current
    val sec = calcState.isSecondFunction
    val showHints = settings.buttonLabels
    val clearLabel = if (calcState.expressionField.text.isEmpty()) "AC" else "C"

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        // Memory row (matching Image 4 right)
        Row(
            modifier = Modifier.fillMaxWidth().height(34.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            SciMiniKey("MC", Modifier.weight(1f).fillMaxHeight(), p.secondaryKeySurface) { viewModel.memoryClear() }
            SciMiniKey("MR", Modifier.weight(1f).fillMaxHeight(), p.secondaryKeySurface) { viewModel.memoryRecall() }
            SciMiniKey("M+", Modifier.weight(1f).fillMaxHeight(), p.secondaryKeySurface) { viewModel.memoryAdd(1) }
            SciMiniKey("M-", Modifier.weight(1f).fillMaxHeight(), p.secondaryKeySurface) { viewModel.memoryAdd(-1) }
            SciMiniKey("MS", Modifier.weight(1f).fillMaxHeight(), p.secondaryKeySurface) { viewModel.memoryStore() }
        }

        // Angle selector + parens + |x| + x! (matching Image 3 & 4)
        Row(
            modifier = Modifier.fillMaxWidth().height(36.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            GlassSegmentedTextBar(
                options = AngleMode.entries.map { it.shortLabel },
                selectedIndex = AngleMode.entries.indexOf(settings.angleMode),
                onSelect = { idx -> viewModel.setAngleMode(AngleMode.entries[idx]) },
                modifier = Modifier.weight(2.1f),
                height = 36.dp
            )
            SciMiniKey("(", Modifier.weight(0.75f).fillMaxHeight(), p.secondaryKeySurface) { viewModel.insertToken("(") }
            SciMiniKey(")", Modifier.weight(0.75f).fillMaxHeight(), p.secondaryKeySurface) { viewModel.insertToken(")") }
            SciMiniKey("|x|", Modifier.weight(0.75f).fillMaxHeight(), p.secondaryKeySurface) { viewModel.insertFunction("abs(") }
            SciMiniKey("x!", Modifier.weight(0.75f).fillMaxHeight(), p.secondaryKeySurface) { viewModel.insertToken("!") }
        }

        // Scientific function grid (4 rows x 5 cols)
        Row(modifier = Modifier.weight(0.85f).fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            SciFuncKey("2nd", if (sec) "active" else null, Modifier.weight(1f).fillMaxHeight(), if (sec) p.accent else p.secondaryKeySurface, if (sec) p.onAccent else p.textPrimary, showHints) {
                viewModel.toggleSecondFunction()
            }
            SciFuncKey(if (sec) "sin⁻¹" else "sin", "sine", Modifier.weight(1f).fillMaxHeight(), p.secondaryKeySurface, p.textPrimary, showHints) {
                viewModel.insertFunction(if (sec) "sin⁻¹(" else "sin(")
            }
            SciFuncKey(if (sec) "cos⁻¹" else "cos", "cosine", Modifier.weight(1f).fillMaxHeight(), p.secondaryKeySurface, p.textPrimary, showHints) {
                viewModel.insertFunction(if (sec) "cos⁻¹(" else "cos(")
            }
            SciFuncKey(if (sec) "tan⁻¹" else "tan", "tangent", Modifier.weight(1f).fillMaxHeight(), p.secondaryKeySurface, p.textPrimary, showHints) {
                viewModel.insertFunction(if (sec) "tan⁻¹(" else "tan(")
            }
            SciFuncKey("÷", "divide", Modifier.weight(1f).fillMaxHeight(), p.operatorKeySurface, p.operatorKeyText, false, 24f, FontWeight.SemiBold, p.operatorKeyBorder) {
                viewModel.insertToken("÷")
            }
        }

        Row(modifier = Modifier.weight(0.85f).fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            SciFuncKey(if (sec) "sinh⁻¹" else "sinh", "hyp sin", Modifier.weight(1f).fillMaxHeight(), p.secondaryKeySurface, p.textPrimary, showHints) {
                viewModel.insertFunction(if (sec) "sinh⁻¹(" else "sinh(")
            }
            SciFuncKey(if (sec) "cosh⁻¹" else "cosh", "hyp cos", Modifier.weight(1f).fillMaxHeight(), p.secondaryKeySurface, p.textPrimary, showHints) {
                viewModel.insertFunction(if (sec) "cosh⁻¹(" else "cosh(")
            }
            SciFuncKey(if (sec) "tanh⁻¹" else "tanh", "hyp tan", Modifier.weight(1f).fillMaxHeight(), p.secondaryKeySurface, p.textPrimary, showHints) {
                viewModel.insertFunction(if (sec) "tanh⁻¹(" else "tanh(")
            }
            SciFuncKey(if (sec) "eˣ" else "ln", "nat log", Modifier.weight(1f).fillMaxHeight(), p.secondaryKeySurface, p.textPrimary, showHints) {
                viewModel.insertFunction(if (sec) "e^(" else "ln(")
            }
            SciFuncKey("×", "multiply", Modifier.weight(1f).fillMaxHeight(), p.operatorKeySurface, p.operatorKeyText, false, 23f, FontWeight.SemiBold, p.operatorKeyBorder) {
                viewModel.insertToken("×")
            }
        }

        Row(modifier = Modifier.weight(0.85f).fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            SciFuncKey(if (sec) "√x" else "x²", "square", Modifier.weight(1f).fillMaxHeight(), p.secondaryKeySurface, p.textPrimary, showHints) {
                if (sec) viewModel.insertFunction("√(") else viewModel.insertToken("²")
            }
            SciFuncKey(if (sec) "³√x" else "x³", "cube", Modifier.weight(1f).fillMaxHeight(), p.secondaryKeySurface, p.textPrimary, showHints) {
                if (sec) viewModel.insertFunction("³√(") else viewModel.insertToken("³")
            }
            SciFuncKey("xʸ", "power", Modifier.weight(1f).fillMaxHeight(), p.secondaryKeySurface, p.textPrimary, showHints) {
                viewModel.insertToken("^")
            }
            SciFuncKey(if (sec) "10ˣ" else "log", "base 10", Modifier.weight(1f).fillMaxHeight(), p.secondaryKeySurface, p.textPrimary, showHints) {
                if (sec) viewModel.insertToken("10^(") else viewModel.insertFunction("log₁₀(")
            }
            SciFuncKey("−", "minus", Modifier.weight(1f).fillMaxHeight(), p.operatorKeySurface, p.operatorKeyText, false, 24f, FontWeight.SemiBold, p.operatorKeyBorder) {
                viewModel.insertToken("−")
            }
        }

        Row(modifier = Modifier.weight(0.85f).fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            SciFuncKey("π", "pi", Modifier.weight(1f).fillMaxHeight(), p.secondaryKeySurface, p.textPrimary, showHints) { viewModel.insertToken("π") }
            SciFuncKey("e", "euler", Modifier.weight(1f).fillMaxHeight(), p.secondaryKeySurface, p.textPrimary, showHints) { viewModel.insertToken("e") }
            SciFuncKey(if (sec) "1/x" else "log₂", "log2", Modifier.weight(1f).fillMaxHeight(), p.secondaryKeySurface, p.textPrimary, showHints) {
                if (sec) viewModel.insertToken("^(-1)") else viewModel.insertFunction("log₂(")
            }
            SciFuncKey(if (sec) "Ans" else "EXP", "sci exp", Modifier.weight(1f).fillMaxHeight(), p.secondaryKeySurface, p.textPrimary, showHints) {
                viewModel.insertToken(if (sec) "Ans" else "EXP")
            }
            SciFuncKey("+", "plus", Modifier.weight(1f).fillMaxHeight(), p.operatorKeySurface, p.operatorKeyText, false, 23f, FontWeight.SemiBold, p.operatorKeyBorder) {
                viewModel.insertToken("+")
            }
        }

        // Numeric & Action rows (5 cols: [7 8 9 ⌫ °], [4 5 6 % Ans], [1 2 3 +/- =], [AC 0 . =])
        Row(modifier = Modifier.weight(1f).fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            CalcKey("7", Modifier.weight(1f).fillMaxHeight(), p.keySurface, p.textPrimary, 20f) { viewModel.insertToken("7") }
            CalcKey("8", Modifier.weight(1f).fillMaxHeight(), p.keySurface, p.textPrimary, 20f) { viewModel.insertToken("8") }
            CalcKey("9", Modifier.weight(1f).fillMaxHeight(), p.keySurface, p.textPrimary, 20f) { viewModel.insertToken("9") }
            GlassKeyButton(
                onClick = viewModel::backspace,
                modifier = Modifier.weight(1f).fillMaxHeight(),
                containerColor = p.secondaryKeySurface,
                contentDescription = "Backspace"
            ) { IconBackspaceKey(p.textPrimary, 20.dp) }
            CalcKey("°", Modifier.weight(1f).fillMaxHeight(), p.secondaryKeySurface, p.textPrimary, 18f) { viewModel.insertToken("°") }
        }

        Row(modifier = Modifier.weight(1f).fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            CalcKey("4", Modifier.weight(1f).fillMaxHeight(), p.keySurface, p.textPrimary, 20f) { viewModel.insertToken("4") }
            CalcKey("5", Modifier.weight(1f).fillMaxHeight(), p.keySurface, p.textPrimary, 20f) { viewModel.insertToken("5") }
            CalcKey("6", Modifier.weight(1f).fillMaxHeight(), p.keySurface, p.textPrimary, 20f) { viewModel.insertToken("6") }
            CalcKey("%", Modifier.weight(1f).fillMaxHeight(), p.secondaryKeySurface, p.textPrimary, 17f) { viewModel.insertToken("%") }
            CalcKey("Ans", Modifier.weight(1f).fillMaxHeight(), p.secondaryKeySurface, p.textPrimary, 13.5f, FontWeight.SemiBold) { viewModel.insertToken("Ans") }
        }

        Row(modifier = Modifier.weight(1f).fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            CalcKey("1", Modifier.weight(1f).fillMaxHeight(), p.keySurface, p.textPrimary, 20f) { viewModel.insertToken("1") }
            CalcKey("2", Modifier.weight(1f).fillMaxHeight(), p.keySurface, p.textPrimary, 20f) { viewModel.insertToken("2") }
            CalcKey("3", Modifier.weight(1f).fillMaxHeight(), p.keySurface, p.textPrimary, 20f) { viewModel.insertToken("3") }
            CalcKey("+/−", Modifier.weight(1f).fillMaxHeight(), p.secondaryKeySurface, p.textPrimary, 16f) { viewModel.toggleSign() }
            CalcKey("√", Modifier.weight(1f).fillMaxHeight(), p.secondaryKeySurface, p.textPrimary, 19f) { viewModel.insertFunction("√(") }
        }

        Row(modifier = Modifier.weight(1f).fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            CalcKey(clearLabel, Modifier.weight(1f).fillMaxHeight(), p.clearKeySurface, p.clearKeyText, 18f, FontWeight.SemiBold, borderColor = p.clearKeyBorder) {
                viewModel.clearAll()
            }
            CalcKey("0", Modifier.weight(1f).fillMaxHeight(), p.keySurface, p.textPrimary, 20f, FontWeight.Medium) { viewModel.insertToken("0") }
            CalcKey(".", Modifier.weight(1f).fillMaxHeight(), p.keySurface, p.textPrimary, 23f, FontWeight.Bold) { viewModel.insertToken(".") }
            CalcKey("=", Modifier.weight(2.05f).fillMaxHeight(), p.accent, p.onAccent, 27f, FontWeight.Bold, pressedColor = p.accent.copy(alpha = 0.85f)) {
                viewModel.evaluateEquals()
            }
        }
    }
}

@Composable
private fun ScientificFunctionsCompactPanel(
    viewModel: CalculatorViewModel,
    settings: AppSettings,
    calcState: CalculatorUiState,
    modifier: Modifier = Modifier
) {
    val p = LocalGlassPalette.current
    val sec = calcState.isSecondFunction
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Row(modifier = Modifier.weight(1f).fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            SciMiniKey("MC", Modifier.weight(1f).fillMaxHeight(), p.secondaryKeySurface) { viewModel.memoryClear() }
            SciMiniKey("MR", Modifier.weight(1f).fillMaxHeight(), p.secondaryKeySurface) { viewModel.memoryRecall() }
            SciMiniKey("M+", Modifier.weight(1f).fillMaxHeight(), p.secondaryKeySurface) { viewModel.memoryAdd(1) }
            SciMiniKey("M-", Modifier.weight(1f).fillMaxHeight(), p.secondaryKeySurface) { viewModel.memoryAdd(-1) }
            SciMiniKey(settings.angleMode.shortLabel, Modifier.weight(1f).fillMaxHeight(), p.secondaryKeySurface) {
                val next = AngleMode.entries[(settings.angleMode.ordinal + 1) % AngleMode.entries.size]
                viewModel.setAngleMode(next)
            }
        }
        Row(modifier = Modifier.weight(1f).fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            SciMiniKey("2nd", Modifier.weight(1f).fillMaxHeight(), if (sec) p.accent else p.secondaryKeySurface, if (sec) p.onAccent else p.textPrimary) {
                viewModel.toggleSecondFunction()
            }
            SciMiniKey(if (sec) "sin⁻¹" else "sin", Modifier.weight(1f).fillMaxHeight(), p.secondaryKeySurface) { viewModel.insertFunction(if (sec) "sin⁻¹(" else "sin(") }
            SciMiniKey(if (sec) "cos⁻¹" else "cos", Modifier.weight(1f).fillMaxHeight(), p.secondaryKeySurface) { viewModel.insertFunction(if (sec) "cos⁻¹(" else "cos(") }
            SciMiniKey(if (sec) "tan⁻¹" else "tan", Modifier.weight(1f).fillMaxHeight(), p.secondaryKeySurface) { viewModel.insertFunction(if (sec) "tan⁻¹(" else "tan(") }
            SciMiniKey("(", Modifier.weight(1f).fillMaxHeight(), p.secondaryKeySurface) { viewModel.insertToken("(") }
        }
        Row(modifier = Modifier.weight(1f).fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            SciMiniKey("ln", Modifier.weight(1f).fillMaxHeight(), p.secondaryKeySurface) { viewModel.insertFunction("ln(") }
            SciMiniKey("log", Modifier.weight(1f).fillMaxHeight(), p.secondaryKeySurface) { viewModel.insertFunction("log₁₀(") }
            SciMiniKey("√", Modifier.weight(1f).fillMaxHeight(), p.secondaryKeySurface) { viewModel.insertFunction("√(") }
            SciMiniKey("xʸ", Modifier.weight(1f).fillMaxHeight(), p.secondaryKeySurface) { viewModel.insertToken("^") }
            SciMiniKey(")", Modifier.weight(1f).fillMaxHeight(), p.secondaryKeySurface) { viewModel.insertToken(")") }
        }
        Row(modifier = Modifier.weight(1f).fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            SciMiniKey("π", Modifier.weight(1f).fillMaxHeight(), p.secondaryKeySurface) { viewModel.insertToken("π") }
            SciMiniKey("e", Modifier.weight(1f).fillMaxHeight(), p.secondaryKeySurface) { viewModel.insertToken("e") }
            SciMiniKey("x!", Modifier.weight(1f).fillMaxHeight(), p.secondaryKeySurface) { viewModel.insertToken("!") }
            SciMiniKey("Ans", Modifier.weight(1f).fillMaxHeight(), p.secondaryKeySurface) { viewModel.insertToken("Ans") }
            SciMiniKey("°", Modifier.weight(1f).fillMaxHeight(), p.secondaryKeySurface) { viewModel.insertToken("°") }
        }
    }
}

@Composable
private fun CalcKey(
    label: String,
    modifier: Modifier,
    containerColor: Color,
    textColor: Color,
    fontSizeSp: Float = 24f,
    fontWeight: FontWeight = FontWeight.Normal,
    pressedColor: Color? = null,
    borderColor: Color? = null,
    onClick: () -> Unit
) {
    val p = LocalGlassPalette.current
    GlassKeyButton(
        onClick = onClick,
        modifier = modifier,
        containerColor = containerColor,
        pressedColor = pressedColor,
        borderColor = borderColor,
        contentDescription = label
    ) {
        BasicText(
            text = label,
            maxLines = 1,
            style = TextStyle(
                color = textColor,
                fontSize = (fontSizeSp * p.textScale).sp,
                fontWeight = fontWeight,
                fontFeatureSettings = "tnum"
            )
        )
    }
}

@Composable
private fun SciMiniKey(
    label: String,
    modifier: Modifier,
    containerColor: Color,
    textColor: Color? = null,
    onClick: () -> Unit
) {
    val p = LocalGlassPalette.current
    GlassKeyButton(
        onClick = onClick,
        modifier = modifier,
        containerColor = containerColor,
        contentDescription = label,
        shape = RoundedCornerShape(999.dp)
    ) {
        BasicText(
            text = label,
            maxLines = 1,
            style = TextStyle(
                color = textColor ?: p.textPrimary,
                fontSize = (13f * p.textScale).coerceAtMost(16f).sp,
                fontWeight = FontWeight.Medium
            )
        )
    }
}

@Composable
private fun SciFuncKey(
    label: String,
    hint: String?,
    modifier: Modifier,
    containerColor: Color,
    textColor: Color,
    showHint: Boolean,
    fontSizeSp: Float = 15f,
    fontWeight: FontWeight = FontWeight.Medium,
    borderColor: Color? = null,
    onClick: () -> Unit
) {
    val p = LocalGlassPalette.current
    GlassKeyButton(
        onClick = onClick,
        modifier = modifier,
        containerColor = containerColor,
        borderColor = borderColor,
        contentDescription = label
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            BasicText(
                text = label,
                maxLines = 1,
                style = TextStyle(
                    color = textColor,
                    fontSize = (fontSizeSp * p.textScale).coerceAtMost(28f).sp,
                    fontWeight = fontWeight
                )
            )
            if (showHint && hint != null) {
                BasicText(
                    text = hint,
                    maxLines = 1,
                    style = TextStyle(
                        color = textColor.copy(alpha = 0.65f),
                        fontSize = 8.5.sp
                    )
                )
            }
        }
    }
}

@Composable
private fun ModeModalRow(
    label: String,
    selected: Boolean,
    icon: @Composable () -> Unit,
    onClick: () -> Unit
) {
    val p = LocalGlassPalette.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 3.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(if (selected) p.activePillSurface else p.cardSurface)
            .border(0.8.dp, p.glassBorder, RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(modifier = Modifier.width(28.dp), contentAlignment = Alignment.Center) {
            icon()
        }
        Spacer(modifier = Modifier.width(12.dp))
        GlassText(
            text = label,
            fontSizeSp = 15.5f,
            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Medium,
            modifier = Modifier.weight(1f)
        )
        if (selected) {
            IconCheckmark(p.textPrimary, 18.dp)
        }
    }
}

@Composable
fun ActionSheetRow(
    label: String,
    icon: @Composable () -> Unit,
    textColor: Color? = null,
    onClick: () -> Unit
) {
    val p = LocalGlassPalette.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 10.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(modifier = Modifier.width(26.dp), contentAlignment = Alignment.Center) {
            icon()
        }
        Spacer(modifier = Modifier.width(12.dp))
        GlassText(
            text = label,
            color = textColor ?: p.textPrimary,
            fontSizeSp = 15.5f,
            fontWeight = FontWeight.Medium
        )
    }
}
