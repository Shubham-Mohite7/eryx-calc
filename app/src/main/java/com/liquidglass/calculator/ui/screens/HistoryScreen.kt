package com.liquidglass.calculator.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicText
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.liquidglass.calculator.CalculatorViewModel
import com.liquidglass.calculator.data.HistoryFilter
import com.liquidglass.calculator.data.HistoryItem
import com.liquidglass.calculator.ui.components.GlassCard
import com.liquidglass.calculator.ui.components.GlassCircleButton
import com.liquidglass.calculator.ui.components.GlassModalCardDialog
import com.liquidglass.calculator.ui.components.GlassSubpageTopBar
import com.liquidglass.calculator.ui.components.GlassText
import com.liquidglass.calculator.ui.components.IconCalculatorBadge
import com.liquidglass.calculator.ui.components.IconCheckmark
import com.liquidglass.calculator.ui.components.IconCloseX
import com.liquidglass.calculator.ui.components.IconCopySheets
import com.liquidglass.calculator.ui.components.IconEditPencil
import com.liquidglass.calculator.ui.components.IconFilterLines
import com.liquidglass.calculator.ui.components.IconMoreDots
import com.liquidglass.calculator.ui.components.IconPin
import com.liquidglass.calculator.ui.components.IconSearchLens
import com.liquidglass.calculator.ui.components.IconShareArrow
import com.liquidglass.calculator.ui.components.IconStar
import com.liquidglass.calculator.ui.components.IconTrashBin
import com.liquidglass.calculator.ui.theme.LocalGlassPalette
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import kotlin.math.roundToInt

@Composable
fun HistoryScreen(
    viewModel: CalculatorViewModel,
    items: List<HistoryItem>,
    searchQuery: String,
    activeFilter: HistoryFilter,
    onBack: () -> Unit
) {
    val p = LocalGlassPalette.current
    val context = LocalContext.current
    var showFilterModal by remember { mutableStateOf(false) }
    var showClearConfirm by remember { mutableStateOf(false) }
    var selectedItem by remember { mutableStateOf<HistoryItem?>(null) }

    val grouped = remember(items) { groupHistoryBySection(items) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .navigationBarsPadding()
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            GlassSubpageTopBar(
                title = "History",
                onBack = onBack,
                trailing = {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        GlassCircleButton(
                            onClick = { showFilterModal = true },
                            contentDescription = "Filter history",
                            size = 40.dp
                        ) {
                            IconFilterLines(p.textPrimary, 18.dp)
                        }
                        if (items.isNotEmpty()) {
                            GlassCircleButton(
                                onClick = { showClearConfirm = true },
                                contentDescription = "Clear all history",
                                size = 40.dp
                            ) {
                                IconTrashBin(p.danger, 18.dp)
                            }
                        }
                    }
                }
            )

            // Search bar (matching Image 3 top-4th)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(p.cardSurface)
                    .border(0.9.dp, p.glassBorder, RoundedCornerShape(16.dp))
                    .padding(horizontal = 14.dp, vertical = 11.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconSearchLens(p.textSecondary, 17.dp)
                Spacer(modifier = Modifier.width(10.dp))
                BasicTextField(
                    value = searchQuery,
                    onValueChange = viewModel::setHistorySearch,
                    singleLine = true,
                    cursorBrush = SolidColor(p.accent),
                    textStyle = TextStyle(color = p.textPrimary, fontSize = (15f * p.textScale).sp),
                    modifier = Modifier.weight(1f),
                    decorationBox = { inner ->
                        if (searchQuery.isEmpty()) {
                            GlassText("Search calculations", color = p.textMuted, fontSizeSp = 15f)
                        }
                        inner()
                    }
                )
                if (searchQuery.isNotEmpty()) {
                    Box(
                        modifier = Modifier
                            .size(22.dp)
                            .clickable { viewModel.setHistorySearch("") },
                        contentAlignment = Alignment.Center
                    ) {
                        IconCloseX(p.textSecondary, 14.dp)
                    }
                }
            }

            if (activeFilter != HistoryFilter.ALL) {
                Row(
                    modifier = Modifier.padding(horizontal = 18.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    GlassText(
                        text = "Filter: ${activeFilter.label}",
                        color = p.accent,
                        fontSizeSp = 12.5f,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    GlassText(
                        text = "Reset",
                        color = p.textSecondary,
                        fontSizeSp = 12.5f,
                        modifier = Modifier.clickable { viewModel.setHistoryFilter(HistoryFilter.ALL) }
                    )
                }
            }

            if (items.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize().padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        GlassText(
                            text = if (searchQuery.isNotEmpty()) "No matching calculations" else "No calculation history yet",
                            fontSizeSp = 17f,
                            fontWeight = FontWeight.SemiBold
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        GlassText(
                            text = "Completed calculations appear here automatically.",
                            color = p.textSecondary,
                            fontSizeSp = 13.5f
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 88.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    grouped.forEach { (sectionTitle, sectionItems) ->
                        item(key = "header_$sectionTitle") {
                            GlassText(
                                text = sectionTitle,
                                color = p.textSecondary,
                                fontSizeSp = 13f,
                                fontWeight = FontWeight.SemiBold,
                                modifier = Modifier.padding(start = 4.dp, top = 8.dp, bottom = 2.dp)
                            )
                        }
                        items(sectionItems, key = { it.id }) { item ->
                            SwipeableHistoryRow(
                                item = item,
                                onTap = { selectedItem = item },
                                onCopy = { viewModel.copyResult(context, item.result) },
                                onEdit = { viewModel.useHistoryItem(item, editMode = true) },
                                onDelete = { viewModel.deleteHistoryItem(item.id) }
                            )
                        }
                    }
                }
            }
        }

        // Bottom floating pill button (matching Image 3 top-4th)
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 16.dp, start = 24.dp, end = 24.dp)
                .fillMaxWidth()
                .height(50.dp)
                .clip(RoundedCornerShape(999.dp))
                .background(p.accent)
                .clickable(onClick = onBack),
            contentAlignment = Alignment.Center
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconCalculatorBadge(p.onAccent, 18.dp)
                Spacer(modifier = Modifier.width(8.dp))
                GlassText(
                    text = "Back to Calculator",
                    color = p.onAccent,
                    fontSizeSp = 15.5f,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }

    // Filter Modal (matching Image 3 bottom-2nd)
    if (showFilterModal) {
        GlassModalCardDialog(onDismiss = { showFilterModal = false }) {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    GlassText("History Filter", fontSizeSp = 18f, fontWeight = FontWeight.SemiBold)
                    GlassCircleButton(onClick = { showFilterModal = false }, contentDescription = "Close", size = 32.dp) {
                        IconCloseX(p.textSecondary, 15.dp)
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
                HistoryFilter.entries.forEach { filter ->
                    val selected = filter == activeFilter
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 3.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(if (selected) p.activePillSurface else p.cardSurface)
                            .border(0.8.dp, p.glassBorder, RoundedCornerShape(14.dp))
                            .clickable {
                                viewModel.setHistoryFilter(filter)
                                showFilterModal = false
                            }
                            .padding(horizontal = 14.dp, vertical = 12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        GlassText(filter.label, fontSizeSp = 15f, fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal)
                        if (selected) IconCheckmark(p.textPrimary, 17.dp)
                    }
                }
            }
        }
    }

    // Row Action Sheet (matching Image 3 bottom-4th)
    selectedItem?.let { item ->
        GlassModalCardDialog(onDismiss = { selectedItem = null }) {
            Column {
                GlassText(item.expression, color = p.textSecondary, fontSizeSp = 14f)
                Spacer(modifier = Modifier.height(2.dp))
                GlassText(item.result, fontSizeSp = 22f, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(14.dp))

                ActionSheetRow("Copy Result", { IconCopySheets(p.textPrimary, 18.dp) }) {
                    viewModel.copyResult(context, item.result)
                    selectedItem = null
                }
                ActionSheetRow("Use in Calculator", { IconCalculatorBadge(p.textPrimary, 18.dp) }) {
                    selectedItem = null
                    viewModel.useHistoryItem(item, editMode = false)
                }
                ActionSheetRow("Edit Equation", { IconEditPencil(p.textPrimary, 18.dp) }) {
                    selectedItem = null
                    viewModel.useHistoryItem(item, editMode = true)
                }
                ActionSheetRow(
                    if (item.isPinned) "Unpin Calculation" else "Pin to Top",
                    { IconPin(p.textPrimary, filled = item.isPinned, size = 18.dp) }
                ) {
                    viewModel.toggleHistoryPin(item.id)
                    selectedItem = null
                }
                ActionSheetRow(
                    if (item.isFavorite) "Remove Favorite" else "Add to Favorites",
                    { IconStar(p.textPrimary, filled = item.isFavorite, size = 18.dp) }
                ) {
                    viewModel.toggleHistoryFavorite(item.id)
                    selectedItem = null
                }
                ActionSheetRow("Share", { IconShareArrow(p.textPrimary, 18.dp) }) {
                    viewModel.shareCalculation(context, item.expression, item.result)
                    selectedItem = null
                }
                ActionSheetRow("Delete", { IconTrashBin(p.danger, 18.dp) }, textColor = p.danger) {
                    viewModel.deleteHistoryItem(item.id)
                    selectedItem = null
                }
            }
        }
    }

    // Clear All Confirmation
    if (showClearConfirm) {
        GlassModalCardDialog(onDismiss = { showClearConfirm = false }) {
            Column {
                GlassText("Clear All History?", fontSizeSp = 18f, fontWeight = FontWeight.SemiBold)
                Spacer(modifier = Modifier.height(6.dp))
                GlassText(
                    "This permanently removes all saved and pinned calculations.",
                    color = p.textSecondary,
                    fontSizeSp = 14f
                )
                Spacer(modifier = Modifier.height(16.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    GlassCard(
                        modifier = Modifier.weight(1f).height(44.dp),
                        shape = RoundedCornerShape(14.dp),
                        onClick = { showClearConfirm = false }
                    ) {
                        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            GlassText("Cancel", fontWeight = FontWeight.SemiBold)
                        }
                    }
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(44.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(p.danger)
                            .clickable {
                                viewModel.clearAllHistory()
                                showClearConfirm = false
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        GlassText("Clear All", color = Color.White, fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }
    }
}

@Composable
private fun SwipeableHistoryRow(
    item: HistoryItem,
    onTap: () -> Unit,
    onCopy: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    val p = LocalGlassPalette.current
    val density = LocalDensity.current
    val maxSwipePx = with(density) { 156.dp.toPx() }
    var offsetX by remember { mutableFloatStateOf(0f) }

    val timeStr = remember(item.timestamp) {
        SimpleDateFormat("h:mm a", Locale.getDefault()).format(Date(item.timestamp))
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
    ) {
        // Revealed Swipe-Left Action Buttons (only shown while swiped left to avoid bleed-through on translucent glass)
        if (offsetX < -4f) {
            Row(
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .height(62.dp)
                    .padding(end = 2.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .width(46.dp)
                        .fillMaxHeight()
                        .clip(RoundedCornerShape(14.dp))
                        .background(p.operatorKeySurface)
                        .clickable {
                            offsetX = 0f
                            onCopy()
                        },
                    contentAlignment = Alignment.Center
                ) { IconCopySheets(p.textPrimary, 17.dp) }

                Box(
                    modifier = Modifier
                        .width(46.dp)
                        .fillMaxHeight()
                        .clip(RoundedCornerShape(14.dp))
                        .background(p.operatorKeySurface)
                        .clickable {
                            offsetX = 0f
                            onEdit()
                        },
                    contentAlignment = Alignment.Center
                ) { IconEditPencil(p.textPrimary, 17.dp) }

                Box(
                    modifier = Modifier
                        .width(46.dp)
                        .fillMaxHeight()
                        .clip(RoundedCornerShape(14.dp))
                        .background(p.danger)
                        .clickable {
                            offsetX = 0f
                            onDelete()
                        },
                    contentAlignment = Alignment.Center
                ) { IconTrashBin(Color.White, 17.dp) }
            }
        }

        // Foreground Glass Row
        GlassCard(
            modifier = Modifier
                .fillMaxWidth()
                .offset { IntOffset(offsetX.roundToInt(), 0) }
                .pointerInput(item.id) {
                    detectHorizontalDragGestures(
                        onHorizontalDrag = { _, dragAmount ->
                            offsetX = (offsetX + dragAmount).coerceIn(-maxSwipePx, 0f)
                        },
                        onDragEnd = {
                            offsetX = if (offsetX < -maxSwipePx * 0.4f) -maxSwipePx else 0f
                        }
                    )
                },
            shape = RoundedCornerShape(20.dp),
            onClick = {
                if (offsetX < -10f) offsetX = 0f else onTap()
            }
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 13.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f).padding(end = 12.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        if (item.isPinned) IconPin(p.accent, filled = true, size = 13.dp)
                        if (item.isFavorite) IconStar(p.accent, filled = true, size = 13.dp)
                        BasicText(
                            text = item.expression,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            style = TextStyle(
                                color = p.textPrimary,
                                fontSize = (14.5f * p.textScale).sp,
                                fontWeight = FontWeight.Medium
                            )
                        )
                    }
                    Spacer(modifier = Modifier.height(3.dp))
                    GlassText(text = timeStr, color = p.textMuted, fontSizeSp = 11.5f)
                }

                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    BasicText(
                        text = item.result,
                        maxLines = 1,
                        style = TextStyle(
                            color = p.textPrimary,
                            fontSize = (17f * p.textScale).sp,
                            fontWeight = FontWeight.Bold,
                            fontFeatureSettings = "tnum"
                        )
                    )
                    IconMoreDots(p.textSecondary, 16.dp)
                }
            }
        }
    }
}

private fun groupHistoryBySection(items: List<HistoryItem>): List<Pair<String, List<HistoryItem>>> {
    val pinned = items.filter { it.isPinned }
    val unpinned = items.filter { !it.isPinned }

    val cal = Calendar.getInstance().apply {
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }
    val todayStart = cal.timeInMillis
    val yesterdayStart = todayStart - 86_400_000L
    val dateFmt = SimpleDateFormat("MMM d", Locale.getDefault())

    val sections = linkedMapOf<String, MutableList<HistoryItem>>()
    if (pinned.isNotEmpty()) {
        sections["Pinned"] = pinned.toMutableList()
    }
    unpinned.forEach { item ->
        val label = when {
            item.timestamp >= todayStart -> "Today"
            item.timestamp >= yesterdayStart -> "Yesterday"
            else -> dateFmt.format(Date(item.timestamp))
        }
        sections.getOrPut(label) { mutableListOf() }.add(item)
    }
    return sections.entries.map { it.key to it.value }
}
