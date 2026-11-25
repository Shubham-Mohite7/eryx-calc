package com.liquidglass.calculator.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.liquidglass.calculator.AppDestination
import com.liquidglass.calculator.data.CalculatorMode
import com.liquidglass.calculator.data.HistoryItem
import com.liquidglass.calculator.engine.FinancialTool
import com.liquidglass.calculator.ui.components.GlassCard
import com.liquidglass.calculator.ui.components.GlassCircleButton
import com.liquidglass.calculator.ui.components.GlassText
import com.liquidglass.calculator.ui.components.IconCalculatorBadge
import com.liquidglass.calculator.ui.components.IconChevronRight
import com.liquidglass.calculator.ui.components.IconGraphWave
import com.liquidglass.calculator.ui.components.IconHistoryClock
import com.liquidglass.calculator.ui.components.IconSettingsGear
import com.liquidglass.calculator.ui.components.IconSwapArrows
import com.liquidglass.calculator.ui.theme.LocalGlassPalette

@Composable
fun HomeScreen(
    recentHistory: List<HistoryItem>,
    onSelectMode: (CalculatorMode) -> Unit,
    onNavigate: (AppDestination) -> Unit,
    onOpenFinancial: (FinancialTool) -> Unit,
    onUseHistoryItem: (HistoryItem) -> Unit
) {
    val p = LocalGlassPalette.current
    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .navigationBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 14.dp)
    ) {
        // Header matching Image 4 left
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top
        ) {
            Column(modifier = Modifier.weight(1f)) {
                GlassText(
                    text = "eryx",
                    fontSizeSp = 32f,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))
                GlassText(
                    text = "A simple, powerful calculator",
                    color = p.textSecondary,
                    fontSizeSp = 15f
                )
            }
            GlassCircleButton(
                onClick = { onNavigate(AppDestination.SETTINGS) },
                contentDescription = "Settings",
                size = 44.dp
            ) {
                IconSettingsGear(p.textPrimary, 21.dp)
            }
        }

        Spacer(modifier = Modifier.height(22.dp))

        // 2x2 Mode Grid matching Image 4 left
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            HomeTileCard(
                modifier = Modifier.weight(1f),
                title = "Standard",
                subtitle = "Quick & everyday calculations",
                onClick = { onSelectMode(CalculatorMode.STANDARD) },
                icon = { IconCalculatorBadge(p.textPrimary, 24.dp) }
            )
            HomeTileCard(
                modifier = Modifier.weight(1f),
                title = "Scientific",
                subtitle = "Advanced math functions",
                onClick = { onSelectMode(CalculatorMode.SCIENTIFIC) },
                icon = {
                    GlassText(
                        text = "ƒ(x)",
                        fontSizeSp = 20f,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            HomeTileCard(
                modifier = Modifier.weight(1f),
                title = "Converter",
                subtitle = "Units, currency, length & more",
                onClick = { onSelectMode(CalculatorMode.CONVERTER) },
                icon = { IconSwapArrows(p.textPrimary, 24.dp) }
            )
            HomeTileCard(
                modifier = Modifier.weight(1f),
                title = "History",
                subtitle = "View past calculations",
                onClick = { onNavigate(AppDestination.HISTORY) },
                icon = { IconHistoryClock(p.textPrimary, 24.dp) }
            )
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Extra Tools Row (Graph, Functions, Financial Calculators)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            GlassCard(
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(20.dp),
                onClick = { onNavigate(AppDestination.GRAPH) }
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconGraphWave(p.textPrimary, 20.dp)
                    Spacer(modifier = Modifier.width(8.dp))
                    GlassText(text = "Graph", fontSizeSp = 14f, fontWeight = FontWeight.SemiBold)
                }
            }
            GlassCard(
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(20.dp),
                onClick = { onNavigate(AppDestination.FUNCTIONS) }
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    GlassText(text = "ƒ", fontSizeSp = 17f, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.width(8.dp))
                    GlassText(text = "Functions", fontSizeSp = 14f, fontWeight = FontWeight.SemiBold)
                }
            }
            GlassCard(
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(20.dp),
                onClick = { onOpenFinancial(FinancialTool.GST) }
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    GlassText(text = "%", fontSizeSp = 16f, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.width(8.dp))
                    GlassText(text = "Finance", fontSizeSp = 14f, fontWeight = FontWeight.SemiBold)
                }
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        // Recent Calculations card matching Image 4 left
        GlassCard(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(26.dp)
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                            onClick = { onNavigate(AppDestination.HISTORY) }
                        ),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconHistoryClock(p.textPrimary, 18.dp)
                        Spacer(modifier = Modifier.width(8.dp))
                        GlassText(
                            text = "Recent",
                            fontSizeSp = 16f,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                    IconChevronRight(p.textSecondary, 16.dp)
                }

                Spacer(modifier = Modifier.height(12.dp))

                if (recentHistory.isEmpty()) {
                    GlassText(
                        text = "No calculations yet. Tap Standard or Scientific to start.",
                        color = p.textSecondary,
                        fontSizeSp = 13.5f
                    )
                } else {
                    recentHistory.take(4).forEachIndexed { idx, item ->
                        if (idx > 0) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 8.dp)
                                    .height(0.8.dp)
                                    .background(p.subtleDivider)
                            )
                        }
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .clickable { onUseHistoryItem(item) }
                                .padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            GlassText(
                                text = item.expression,
                                color = p.textSecondary,
                                fontSizeSp = 14f,
                                maxLines = 1,
                                modifier = Modifier.weight(1f).padding(end = 12.dp)
                            )
                            GlassText(
                                text = item.result,
                                color = p.textPrimary,
                                fontSizeSp = 15f,
                                fontWeight = FontWeight.SemiBold,
                                maxLines = 1
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun HomeTileCard(
    title: String,
    subtitle: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: @Composable () -> Unit
) {
    val p = LocalGlassPalette.current
    GlassCard(
        modifier = modifier.height(152.dp),
        shape = RoundedCornerShape(26.dp),
        onClick = onClick
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(18.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(p.secondaryKeySurface)
                    .border(0.8.dp, p.glassBorder, RoundedCornerShape(14.dp)),
                contentAlignment = Alignment.Center
            ) {
                icon()
            }
            Column {
                GlassText(
                    text = title,
                    fontSizeSp = 17f,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(3.dp))
                GlassText(
                    text = subtitle,
                    color = p.textSecondary,
                    fontSizeSp = 12.5f,
                    maxLines = 2
                )
            }
        }
    }
}
