package com.liquidglass.calculator.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicText
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.liquidglass.calculator.CalculatorViewModel
import com.liquidglass.calculator.ConverterUiState
import com.liquidglass.calculator.data.AppSettings
import com.liquidglass.calculator.data.CurrencyState
import com.liquidglass.calculator.engine.FinancialTool
import com.liquidglass.calculator.engine.UnitCategory
import com.liquidglass.calculator.engine.UnitConverter
import com.liquidglass.calculator.engine.UnitDef
import com.liquidglass.calculator.ui.components.GlassCard
import com.liquidglass.calculator.ui.components.GlassCircleButton
import com.liquidglass.calculator.ui.components.GlassKeyButton
import com.liquidglass.calculator.ui.components.GlassModalCardDialog
import com.liquidglass.calculator.ui.components.GlassText
import com.liquidglass.calculator.ui.components.IconBackspaceKey
import com.liquidglass.calculator.ui.components.IconCheckmark
import com.liquidglass.calculator.ui.components.IconChevronDown
import com.liquidglass.calculator.ui.components.IconChevronRight
import com.liquidglass.calculator.ui.components.IconCloseX
import com.liquidglass.calculator.ui.components.IconSearchLens
import com.liquidglass.calculator.ui.components.IconSwapArrows
import com.liquidglass.calculator.ui.components.IconSwapVertical
import com.liquidglass.calculator.ui.theme.LocalGlassPalette
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun ConverterContent(
    viewModel: CalculatorViewModel,
    settings: AppSettings,
    state: ConverterUiState,
    currencyState: CurrencyState
) {
    val p = LocalGlassPalette.current
    val context = LocalContext.current
    var pickingFromUnit by remember { mutableStateOf(false) }
    var pickingToUnit by remember { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxSize()) {
        // Category dropdown pill + Swap / Finance quick pills (matching Image 3 top-3rd)
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            GlassCard(
                modifier = Modifier.weight(1f).height(44.dp),
                shape = RoundedCornerShape(999.dp),
                onClick = { viewModel.setShowConverterCategoryList(!state.showCategoryList) }
            ) {
                Row(
                    modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    GlassText(
                        text = state.category.title,
                        fontSizeSp = 15.5f,
                        fontWeight = FontWeight.SemiBold
                    )
                    IconChevronDown(p.textSecondary, 15.dp)
                }
            }

            if (state.category == UnitCategory.CURRENCY) {
                GlassCard(
                    modifier = Modifier.height(44.dp),
                    shape = RoundedCornerShape(999.dp),
                    onClick = { viewModel.refreshCurrencyRates() }
                ) {
                    Box(
                        modifier = Modifier.padding(horizontal = 14.dp).fillMaxHeight(),
                        contentAlignment = Alignment.Center
                    ) {
                        GlassText(
                            text = if (currencyState.isRefreshing) "Updating..." else "Refresh",
                            fontSizeSp = 13f,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }

            GlassCircleButton(
                onClick = viewModel::swapConverterUnits,
                contentDescription = "Swap Units",
                size = 44.dp
            ) {
                IconSwapArrows(p.textPrimary, 19.dp)
            }
        }

        if (state.category == UnitCategory.CURRENCY) {
            val subtitle = when {
                currencyState.statusMessage != null -> currencyState.statusMessage
                currencyState.lastUpdatedMillis > 0L -> {
                    val fmt = SimpleDateFormat("MMM d, HH:mm", Locale.getDefault())
                    "Live ECB rates • Updated ${fmt.format(Date(currencyState.lastUpdatedMillis))}"
                }
                else -> "Offline baseline rates • Tap Refresh for live ECB rates"
            }
            Spacer(modifier = Modifier.height(6.dp))
            GlassText(
                text = subtitle,
                color = p.textSecondary,
                fontSizeSp = 12f,
                modifier = Modifier.padding(horizontal = 6.dp)
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        if (state.showCategoryList) {
            // Category + Financial Calculators list (matching Image 2 top-right)
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(UnitCategory.entries) { cat ->
                    GlassCard(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(20.dp),
                        onClick = { viewModel.selectConverterCategory(cat) }
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(horizontal = 18.dp, vertical = 15.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                GlassText(text = cat.title, fontSizeSp = 16f, fontWeight = FontWeight.SemiBold)
                                GlassText(text = cat.subtitle, color = p.textSecondary, fontSizeSp = 12.5f)
                            }
                            IconChevronRight(p.textSecondary, 16.dp)
                        }
                    }
                }
                item {
                    Spacer(modifier = Modifier.height(6.dp))
                    GlassText(
                        text = "Financial Calculators",
                        color = p.textSecondary,
                        fontSizeSp = 13f,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 4.dp)
                    )
                }
                items(FinancialTool.entries) { tool ->
                    GlassCard(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(20.dp),
                        onClick = { viewModel.openFinancialTool(tool) }
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(horizontal = 18.dp, vertical = 15.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                GlassText(text = tool.title, fontSizeSp = 16f, fontWeight = FontWeight.SemiBold)
                                GlassText(text = tool.subtitle, color = p.textSecondary, fontSizeSp = 12.5f)
                            }
                            IconChevronRight(p.textSecondary, 16.dp)
                        }
                    }
                }
            }
        } else {
            // Dual Conversion Cards + Floating Swap Circle (matching Image 1 & 3)
            Box(modifier = Modifier.fillMaxWidth()) {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    // FROM Card
                    GlassCard(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(24.dp),
                        containerColor = p.displaySurface
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(horizontal = 18.dp, vertical = 16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                BasicText(
                                    text = state.inputText,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    style = TextStyle(
                                        color = p.textPrimary,
                                        fontSize = (30f * p.textScale).sp,
                                        fontWeight = FontWeight.SemiBold,
                                        fontFeatureSettings = "tnum"
                                    )
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                GlassText(
                                    text = state.fromUnit.name,
                                    color = p.textSecondary,
                                    fontSizeSp = 13f
                                )
                            }
                            UnitDropdownPill(
                                symbol = state.fromUnit.symbol,
                                onClick = { pickingFromUnit = true }
                            )
                        }
                    }

                    // TO Card (tap to copy result)
                    GlassCard(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(24.dp),
                        containerColor = p.displaySurface,
                        onClick = {
                            val toCopy = if (settings.copyResultWithUnits) {
                                "${state.outputText} ${state.toUnit.symbol}"
                            } else {
                                state.outputText
                            }
                            viewModel.copyResult(context, toCopy)
                        }
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(horizontal = 18.dp, vertical = 16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                BasicText(
                                    text = state.outputText,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    style = TextStyle(
                                        color = p.textPrimary,
                                        fontSize = (30f * p.textScale).sp,
                                        fontWeight = FontWeight.SemiBold,
                                        fontFeatureSettings = "tnum"
                                    )
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                GlassText(
                                    text = state.toUnit.name,
                                    color = p.textSecondary,
                                    fontSizeSp = 13f
                                )
                            }
                            UnitDropdownPill(
                                symbol = state.toUnit.symbol,
                                onClick = { pickingToUnit = true }
                            )
                        }
                    }
                }

                // Floating vertical swap pill between cards
                GlassCircleButton(
                    onClick = viewModel::swapConverterUnits,
                    contentDescription = "Swap From and To units",
                    size = 42.dp,
                    containerColor = p.displaySurface,
                    modifier = Modifier.align(Alignment.Center)
                ) {
                    IconSwapVertical(p.textPrimary, 18.dp)
                }
            }

            Spacer(modifier = Modifier.weight(0.25f))

            // Built-in Numeric Keypad (matching Image 1 right & Image 3 top-3rd)
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                verticalArrangement = Arrangement.spacedBy(10.dp, Alignment.Bottom)
            ) {
                Row(modifier = Modifier.weight(1f, fill = false).height(70.dp).fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    ConvKey("7", Modifier.weight(1f).fillMaxHeight(), p.keySurface, p.textPrimary) { viewModel.converterAppend("7") }
                    ConvKey("8", Modifier.weight(1f).fillMaxHeight(), p.keySurface, p.textPrimary) { viewModel.converterAppend("8") }
                    ConvKey("9", Modifier.weight(1f).fillMaxHeight(), p.keySurface, p.textPrimary) { viewModel.converterAppend("9") }
                    GlassKeyButton(
                        onClick = { viewModel.converterAppend("⌫") },
                        modifier = Modifier.weight(1f).fillMaxHeight(),
                        containerColor = p.secondaryKeySurface,
                        contentDescription = "Backspace"
                    ) { IconBackspaceKey(p.textPrimary, 22.dp) }
                }
                Row(modifier = Modifier.weight(1f, fill = false).height(70.dp).fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    ConvKey("4", Modifier.weight(1f).fillMaxHeight(), p.keySurface, p.textPrimary) { viewModel.converterAppend("4") }
                    ConvKey("5", Modifier.weight(1f).fillMaxHeight(), p.keySurface, p.textPrimary) { viewModel.converterAppend("5") }
                    ConvKey("6", Modifier.weight(1f).fillMaxHeight(), p.keySurface, p.textPrimary) { viewModel.converterAppend("6") }
                    ConvKey("AC", Modifier.weight(1f).fillMaxHeight(), p.clearKeySurface, p.clearKeyText, 19f, FontWeight.SemiBold, borderColor = p.clearKeyBorder) {
                        viewModel.converterAppend("AC")
                    }
                }
                Row(modifier = Modifier.weight(1f, fill = false).height(70.dp).fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    ConvKey("1", Modifier.weight(1f).fillMaxHeight(), p.keySurface, p.textPrimary) { viewModel.converterAppend("1") }
                    ConvKey("2", Modifier.weight(1f).fillMaxHeight(), p.keySurface, p.textPrimary) { viewModel.converterAppend("2") }
                    ConvKey("3", Modifier.weight(1f).fillMaxHeight(), p.keySurface, p.textPrimary) { viewModel.converterAppend("3") }
                    ConvKey("+/−", Modifier.weight(1f).fillMaxHeight(), p.secondaryKeySurface, p.textPrimary, 18f, FontWeight.Medium) {
                        viewModel.converterAppend("+/-")
                    }
                }
                Row(modifier = Modifier.weight(1f, fill = false).height(70.dp).fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    ConvKey("0", Modifier.weight(2f).fillMaxHeight(), p.keySurface, p.textPrimary) { viewModel.converterAppend("0") }
                    ConvKey(".", Modifier.weight(1f).fillMaxHeight(), p.keySurface, p.textPrimary, 26f, FontWeight.Bold) {
                        viewModel.converterAppend(".")
                    }
                    GlassKeyButton(
                        onClick = viewModel::swapConverterUnits,
                        modifier = Modifier.weight(1f).fillMaxHeight(),
                        containerColor = p.accent,
                        pressedColor = p.accent.copy(alpha = 0.85f),
                        contentDescription = "Swap"
                    ) { IconSwapArrows(p.onAccent, 22.dp) }
                }
            }
        }
    }

    if (pickingFromUnit || pickingToUnit) {
        val units = UnitConverter.unitsFor(state.category, currencyState.rates)
        val currentId = if (pickingFromUnit) state.fromUnit.id else state.toUnit.id
        UnitPickerModal(
            title = if (pickingFromUnit) "Convert From" else "Convert To",
            units = units,
            selectedId = currentId,
            onSelect = { unit ->
                if (pickingFromUnit) viewModel.selectFromUnit(unit) else viewModel.selectToUnit(unit)
                pickingFromUnit = false
                pickingToUnit = false
            },
            onDismiss = {
                pickingFromUnit = false
                pickingToUnit = false
            }
        )
    }
}

@Composable
private fun UnitDropdownPill(symbol: String, onClick: () -> Unit) {
    val p = LocalGlassPalette.current
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(999.dp))
            .background(p.secondaryKeySurface)
            .border(0.8.dp, p.glassBorder, RoundedCornerShape(999.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 7.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        GlassText(text = symbol, fontSizeSp = 14.5f, fontWeight = FontWeight.SemiBold)
        Spacer(modifier = Modifier.width(5.dp))
        IconChevronDown(p.textSecondary, 13.dp)
    }
}

@Composable
private fun ConvKey(
    label: String,
    modifier: Modifier,
    bg: Color,
    fg: Color,
    sizeSp: Float = 24f,
    weight: FontWeight = FontWeight.Medium,
    borderColor: Color? = null,
    onClick: () -> Unit
) {
    val p = LocalGlassPalette.current
    GlassKeyButton(onClick = onClick, modifier = modifier, containerColor = bg, borderColor = borderColor, contentDescription = label) {
        BasicText(
            text = label,
            style = TextStyle(
                color = fg,
                fontSize = (sizeSp * p.textScale).sp,
                fontWeight = weight,
                fontFeatureSettings = "tnum"
            )
        )
    }
}

@Composable
private fun UnitPickerModal(
    title: String,
    units: List<UnitDef>,
    selectedId: String,
    onSelect: (UnitDef) -> Unit,
    onDismiss: () -> Unit
) {
    val p = LocalGlassPalette.current
    var query by remember { mutableStateOf("") }
    val filtered = remember(query, units) {
        if (query.isBlank()) units
        else units.filter {
            it.name.contains(query, ignoreCase = true) || it.symbol.contains(query, ignoreCase = true)
        }
    }

    GlassModalCardDialog(onDismiss = onDismiss) {
        Column(modifier = Modifier.heightIn(max = 460.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                GlassText(text = title, fontSizeSp = 18f, fontWeight = FontWeight.SemiBold)
                GlassCircleButton(onClick = onDismiss, contentDescription = "Close", size = 32.dp) {
                    IconCloseX(p.textSecondary, 15.dp)
                }
            }
            Spacer(modifier = Modifier.height(10.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(p.cardSurface)
                    .border(0.8.dp, p.glassBorder, RoundedCornerShape(14.dp))
                    .padding(horizontal = 12.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconSearchLens(p.textSecondary, 16.dp)
                Spacer(modifier = Modifier.width(8.dp))
                BasicTextField(
                    value = query,
                    onValueChange = { query = it },
                    singleLine = true,
                    cursorBrush = SolidColor(p.accent),
                    textStyle = TextStyle(color = p.textPrimary, fontSize = 14.sp),
                    modifier = Modifier.weight(1f),
                    decorationBox = { inner ->
                        if (query.isEmpty()) {
                            GlassText("Search units...", color = p.textMuted, fontSizeSp = 14f)
                        }
                        inner()
                    }
                )
            }
            Spacer(modifier = Modifier.height(10.dp))
            LazyColumn(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                items(filtered, key = { it.id }) { unit ->
                    val active = unit.id == selectedId
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .background(if (active) p.activePillSurface else p.cardSurface)
                            .border(0.8.dp, p.glassBorder, RoundedCornerShape(14.dp))
                            .clickable { onSelect(unit) }
                            .padding(horizontal = 14.dp, vertical = 12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            GlassText(unit.name, fontSizeSp = 15f, fontWeight = FontWeight.Medium)
                            GlassText(unit.symbol, color = p.textSecondary, fontSizeSp = 12.5f)
                        }
                        if (active) {
                            IconCheckmark(p.textPrimary, 17.dp)
                        }
                    }
                }
            }
        }
    }
}
