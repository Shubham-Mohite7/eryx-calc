package com.liquidglass.calculator.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.liquidglass.calculator.CalculatorViewModel
import com.liquidglass.calculator.data.GraphFunctionItem
import com.liquidglass.calculator.engine.AngleMode
import com.liquidglass.calculator.engine.MathEngine
import com.liquidglass.calculator.ui.components.GlassCard
import com.liquidglass.calculator.ui.components.GlassCircleButton
import com.liquidglass.calculator.ui.components.GlassModalCardDialog
import com.liquidglass.calculator.ui.components.GlassSegmentedTextBar
import com.liquidglass.calculator.ui.components.GlassSubpageTopBar
import com.liquidglass.calculator.ui.components.GlassText
import com.liquidglass.calculator.ui.components.IconCloseX
import com.liquidglass.calculator.ui.components.IconEditPencil
import com.liquidglass.calculator.ui.components.IconExpandCorners
import com.liquidglass.calculator.ui.components.IconGraphWave
import com.liquidglass.calculator.ui.components.IconMoreDots
import com.liquidglass.calculator.ui.components.IconTrashBin
import com.liquidglass.calculator.ui.theme.LocalGlassPalette
import java.util.Locale
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.floor
import kotlin.math.pow

@Composable
fun GraphScreen(
    viewModel: CalculatorViewModel,
    functions: List<GraphFunctionItem>,
    initialExpression: String? = null,
    onBack: () -> Unit
) {
    val p = LocalGlassPalette.current
    var centerX by remember { mutableDoubleStateOf(0.0) }
    var centerY by remember { mutableDoubleStateOf(0.0) }
    var xSpan by remember { mutableDoubleStateOf(4.2 * PI) } // matches -2π .. 2π
    var ySpan by remember { mutableDoubleStateOf(5.2) }
    var activeTab by remember { mutableIntStateOf(0) } // 0=Graph, 1=Table, 2=Params
    var paramA by remember { mutableFloatStateOf(1.0f) }
    var paramB by remember { mutableFloatStateOf(1.0f) }

    var tracePoint by remember { mutableStateOf<Pair<Double, Double>?>(null) }
    var showAddDialog by remember { mutableStateOf(false) }
    var editingFn by remember { mutableStateOf<GraphFunctionItem?>(null) }
    var menuFn by remember { mutableStateOf<GraphFunctionItem?>(null) }

    val paramsMap = remember(paramA, paramB) {
        mapOf("a" to paramA.toDouble(), "b" to paramB.toDouble())
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .navigationBarsPadding()
    ) {
        GlassSubpageTopBar(
            title = "Graph",
            onBack = onBack,
            trailing = {
                GlassCircleButton(
                    onClick = {
                        centerX = 0.0
                        centerY = 0.0
                        xSpan = 4.2 * PI
                        ySpan = 5.2
                        tracePoint = null
                    },
                    contentDescription = "Reset graph view",
                    size = 40.dp
                ) {
                    IconExpandCorners(p.textPrimary, 17.dp)
                }
            }
        )

        // Main Graph / Table / Params Viewport
        GlassCard(
            modifier = Modifier
                .fillMaxWidth()
                .weight(0.54f)
                .padding(horizontal = 16.dp),
            shape = RoundedCornerShape(26.dp),
            containerColor = p.displaySurface
        ) {
            when (activeTab) {
                0 -> {
                    InteractivePlotCanvas(
                        functions = functions,
                        params = paramsMap,
                        centerX = centerX,
                        centerY = centerY,
                        xSpan = xSpan,
                        ySpan = ySpan,
                        tracePoint = tracePoint,
                        onTransform = { dxFrac, dyFrac, zoom ->
                            val newXSpan = (xSpan / zoom).coerceIn(0.4, 200.0)
                            val newYSpan = (ySpan / zoom).coerceIn(0.4, 200.0)
                            centerX -= dxFrac * newXSpan
                            centerY += dyFrac * newYSpan
                            xSpan = newXSpan
                            ySpan = newYSpan
                            tracePoint = null
                        },
                        onTapCoordinate = { xWorld ->
                            val firstVisible = functions.firstOrNull { it.isVisible }
                            if (firstVisible != null) {
                                val yWorld = MathEngine.evaluateForGraph(
                                    firstVisible.expression,
                                    xWorld,
                                    paramsMap,
                                    AngleMode.RAD
                                )
                                tracePoint = if (!yWorld.isNaN() && !yWorld.isInfinite()) {
                                    xWorld to yWorld
                                } else null
                            }
                        }
                    )
                }
                1 -> {
                    GraphValueTable(
                        functions = functions,
                        params = paramsMap,
                        centerX = centerX
                    )
                }
                else -> {
                    GraphParametersView(
                        paramA = paramA,
                        paramB = paramB,
                        onParamAChange = { paramA = it },
                        onParamBChange = { paramB = it },
                        onResetView = {
                            centerX = 0.0
                            centerY = 0.0
                            xSpan = 4.2 * PI
                            ySpan = 5.2
                            paramA = 1f
                            paramB = 1f
                        }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Functions List + Add Function (matching Image 2 top-4th)
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .weight(0.34f)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(functions, key = { it.id }) { fn ->
                GlassCard(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    onClick = { editingFn = fn }
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 13.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(11.dp)
                                    .clip(CircleShape)
                                    .background(
                                        when {
                                            !fn.isVisible -> p.textMuted.copy(alpha = 0.35f)
                                            p.isDark && fn.colorHex == 0xFF22252A -> p.textPrimary
                                            else -> Color(fn.colorHex)
                                        }
                                    )
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            GlassText(
                                text = "y = ${fn.expression}",
                                color = if (fn.isVisible) p.textPrimary else p.textMuted,
                                fontSizeSp = 15.5f,
                                fontWeight = FontWeight.Medium
                            )
                        }
                        Box(
                            modifier = Modifier
                                .size(30.dp)
                                .clip(CircleShape)
                                .clickable { menuFn = fn },
                            contentAlignment = Alignment.Center
                        ) {
                            IconMoreDots(p.textSecondary, 16.dp)
                        }
                    }
                }
            }

            item {
                GlassCard(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    onClick = { showAddDialog = true }
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 13.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        GlassText(
                            text = "+ Add Function",
                            fontSizeSp = 15f,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }

        // Bottom 3-tab bar matching Image 2 top-4th
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 10.dp)
        ) {
            GlassSegmentedTextBar(
                options = listOf("Graph", "Table", "Parameters"),
                selectedIndex = activeTab,
                onSelect = { activeTab = it },
                height = 42.dp
            )
        }
    }

    if (showAddDialog) {
        FunctionEditorDialog(
            title = "Add Function",
            initial = initialExpression ?: "",
            onConfirm = { expr ->
                viewModel.addGraphFunction(expr)
                showAddDialog = false
            },
            onDismiss = { showAddDialog = false }
        )
    }

    editingFn?.let { fn ->
        FunctionEditorDialog(
            title = "Edit Function",
            initial = fn.expression,
            onConfirm = { expr ->
                viewModel.updateGraphFunction(fn.id, expr)
                editingFn = null
            },
            onDismiss = { editingFn = null }
        )
    }

    menuFn?.let { fn ->
        GlassModalCardDialog(onDismiss = { menuFn = null }) {
            Column {
                GlassText("y = ${fn.expression}", fontSizeSp = 17f, fontWeight = FontWeight.SemiBold)
                Spacer(modifier = Modifier.height(12.dp))
                ActionSheetRow("Edit Expression", { IconEditPencil(p.textPrimary, 18.dp) }) {
                    editingFn = fn
                    menuFn = null
                }
                ActionSheetRow(
                    if (fn.isVisible) "Hide Curve" else "Show Curve",
                    { IconGraphWave(p.textPrimary, 18.dp) }
                ) {
                    viewModel.toggleGraphFunctionVisibility(fn.id)
                    menuFn = null
                }
                ActionSheetRow("Delete Function", { IconTrashBin(p.danger, 18.dp) }, textColor = p.danger) {
                    viewModel.deleteGraphFunction(fn.id)
                    menuFn = null
                }
            }
        }
    }
}

@Composable
private fun InteractivePlotCanvas(
    functions: List<GraphFunctionItem>,
    params: Map<String, Double>,
    centerX: Double,
    centerY: Double,
    xSpan: Double,
    ySpan: Double,
    tracePoint: Pair<Double, Double>?,
    onTransform: (dxFrac: Double, dyFrac: Double, zoom: Double) -> Unit,
    onTapCoordinate: (xWorld: Double) -> Unit
) {
    val p = LocalGlassPalette.current
    val textMeasurer = rememberTextMeasurer()

    Canvas(
        modifier = Modifier
            .fillMaxSize()
            .pointerInput(xSpan, ySpan, centerX, centerY) {
                detectTransformGestures { _, pan, zoom, _ ->
                    val dxFrac = pan.x.toDouble() / size.width.coerceAtLeast(1)
                    val dyFrac = pan.y.toDouble() / size.height.coerceAtLeast(1)
                    onTransform(dxFrac, dyFrac, zoom.toDouble())
                }
            }
            .pointerInput(xSpan, centerX) {
                detectTapGestures { offset ->
                    val minX = centerX - xSpan / 2.0
                    val xWorld = minX + (offset.x / size.width.coerceAtLeast(1)) * xSpan
                    onTapCoordinate(xWorld)
                }
            }
    ) {
        val w = size.width
        val h = size.height
        val minX = centerX - xSpan / 2.0
        val maxX = centerX + xSpan / 2.0
        val minY = centerY - ySpan / 2.0
        val maxY = centerY + ySpan / 2.0

        fun worldToPxX(x: Double): Float = (((x - minX) / xSpan) * w).toFloat()
        fun worldToPxY(y: Double): Float = (h - ((y - minY) / ySpan) * h).toFloat()

        // Grid & Axis Ticks
        val xStep = niceStep(xSpan / 6.0)
        val yStep = niceStep(ySpan / 5.0)
        val gridColor = p.subtleDivider
        val axisColor = p.textSecondary.copy(alpha = 0.55f)

        var gx = floor(minX / xStep) * xStep
        while (gx <= maxX) {
            val px = worldToPxX(gx)
            drawLine(gridColor, Offset(px, 0f), Offset(px, h), strokeWidth = 1f)
            if (abs(gx) > xStep * 0.1) {
                val label = formatTick(gx)
                val measured = textMeasurer.measure(
                    label,
                    TextStyle(color = p.textMuted, fontSize = 10.sp)
                )
                val labelY = worldToPxY(0.0).coerceIn(4f, h - measured.size.height - 4f)
                drawText(measured, topLeft = Offset((px - measured.size.width / 2f).coerceIn(2f, w - measured.size.width - 2f), labelY + 3f))
            }
            gx += xStep
        }

        var gy = floor(minY / yStep) * yStep
        while (gy <= maxY) {
            val py = worldToPxY(gy)
            drawLine(gridColor, Offset(0f, py), Offset(w, py), strokeWidth = 1f)
            if (abs(gy) > yStep * 0.1) {
                val label = formatTick(gy)
                val measured = textMeasurer.measure(
                    label,
                    TextStyle(color = p.textMuted, fontSize = 10.sp)
                )
                val labelX = worldToPxX(0.0).coerceIn(4f, w - measured.size.width - 4f)
                drawText(measured, topLeft = Offset(labelX + 4f, (py - measured.size.height / 2f).coerceIn(2f, h - measured.size.height - 2f)))
            }
            gy += yStep
        }

        // Main Axes
        val zeroX = worldToPxX(0.0)
        val zeroY = worldToPxY(0.0)
        if (zeroX in 0f..w) drawLine(axisColor, Offset(zeroX, 0f), Offset(zeroX, h), strokeWidth = 1.6f)
        if (zeroY in 0f..h) drawLine(axisColor, Offset(0f, zeroY), Offset(w, zeroY), strokeWidth = 1.6f)

        // Plot Functions
        val samples = 280
        functions.filter { it.isVisible }.forEach { fn ->
            val curveColor = if (p.isDark && fn.colorHex == 0xFF22252A) p.textPrimary else Color(fn.colorHex)
            val path = Path()
            var penDown = false
            var prevY = 0f

            for (i in 0..samples) {
                val frac = i.toDouble() / samples
                val x = minX + frac * xSpan
                val y = MathEngine.evaluateForGraph(fn.expression, x, params, AngleMode.RAD)
                if (y.isNaN() || y.isInfinite() || abs(y) > ySpan * 20) {
                    penDown = false
                    continue
                }
                val px = (frac * w).toFloat()
                val py = worldToPxY(y)
                if (!penDown || abs(py - prevY) > h * 0.65f) {
                    path.moveTo(px, py)
                    penDown = true
                } else {
                    path.lineTo(px, py)
                }
                prevY = py
            }
            drawPath(path, curveColor, style = Stroke(width = 3.2f, cap = StrokeCap.Round))
        }

        // Trace Point + Dark Pill Callout (matching Image 2 top-4th "(1.57, 1.00)")
        tracePoint?.let { (tx, ty) ->
            val px = worldToPxX(tx)
            val py = worldToPxY(ty)
            if (px in 0f..w && py in 0f..h) {
                drawLine(p.textMuted.copy(alpha = 0.5f), Offset(px, 0f), Offset(px, h), strokeWidth = 1.2f)
                drawCircle(p.accent, radius = 6.5f, center = Offset(px, py))
                drawCircle(Color.White, radius = 3.2f, center = Offset(px, py))

                val text = String.format(Locale.US, "(%.2f, %.2f)", tx, ty)
                val measured = textMeasurer.measure(
                    text,
                    TextStyle(color = p.onAccent, fontSize = 11.5.sp, fontWeight = FontWeight.SemiBold)
                )
                val padH = 10f
                val padV = 5f
                val boxW = measured.size.width + padH * 2
                val boxH = measured.size.height + padV * 2
                val boxX = (px - boxW / 2f).coerceIn(6f, w - boxW - 6f)
                val boxY = (py - boxH - 14f).coerceIn(6f, h - boxH - 6f)
                drawRoundRect(
                    color = p.accent,
                    topLeft = Offset(boxX, boxY),
                    size = Size(boxW, boxH),
                    cornerRadius = CornerRadius(boxH / 2f, boxH / 2f)
                )
                drawText(measured, topLeft = Offset(boxX + padH, boxY + padV))
            }
        }
    }
}

@Composable
private fun GraphValueTable(
    functions: List<GraphFunctionItem>,
    params: Map<String, Double>,
    centerX: Double
) {
    val p = LocalGlassPalette.current
    val first = functions.firstOrNull { it.isVisible } ?: functions.firstOrNull()
    val start = floor(centerX) - 5.0
    val rows = remember(first, params, start) {
        (0..10).map { i ->
            val x = start + i
            val y = if (first != null) MathEngine.evaluateForGraph(first.expression, x, params, AngleMode.RAD) else Double.NaN
            x to y
        }
    }

    Column(modifier = Modifier.fillMaxSize().padding(14.dp)) {
        GlassText(
            text = if (first != null) "Table for y = ${first.expression}" else "No function",
            fontSizeSp = 14f,
            fontWeight = FontWeight.SemiBold
        )
        Spacer(modifier = Modifier.height(8.dp))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(p.secondaryKeySurface, RoundedCornerShape(10.dp))
                .padding(horizontal = 12.dp, vertical = 7.dp)
        ) {
            GlassText("x", fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
            GlassText("y", fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
        }
        LazyColumn(modifier = Modifier.fillMaxSize()) {
            items(rows) { (x, y) ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 7.dp)
                ) {
                    GlassText(String.format(Locale.US, "%.2f", x), color = p.textSecondary, modifier = Modifier.weight(1f))
                    val yText = if (y.isNaN() || y.isInfinite()) "Undefined" else String.format(Locale.US, "%.4f", y)
                    GlassText(yText, fontWeight = FontWeight.Medium, modifier = Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
private fun GraphParametersView(
    paramA: Float,
    paramB: Float,
    onParamAChange: (Float) -> Unit,
    onParamBChange: (Float) -> Unit,
    onResetView: () -> Unit
) {
    val p = LocalGlassPalette.current
    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        GlassText("Parameters (use a, b in expressions)", fontSizeSp = 14f, fontWeight = FontWeight.SemiBold)
        ParameterAdjustRow("a", paramA, onParamAChange)
        ParameterAdjustRow("b", paramB, onParamBChange)
        Spacer(modifier = Modifier.weight(1f))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(42.dp)
                .clip(RoundedCornerShape(14.dp))
                .background(p.secondaryKeySurface)
                .border(0.8.dp, p.glassBorder, RoundedCornerShape(14.dp))
                .clickable(onClick = onResetView),
            contentAlignment = Alignment.Center
        ) {
            GlassText("Reset Viewport & Parameters", fontSizeSp = 14f, fontWeight = FontWeight.SemiBold)
        }
    }
}

@Composable
private fun ParameterAdjustRow(name: String, value: Float, onChange: (Float) -> Unit) {
    val p = LocalGlassPalette.current
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        GlassText("$name = ${String.format(Locale.US, "%.1f", value)}", fontSizeSp = 15f, fontWeight = FontWeight.Medium)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(10.dp))
                    .background(p.secondaryKeySurface)
                    .clickable { onChange((value - 0.5f).coerceAtLeast(-10f)) }
                    .padding(horizontal = 14.dp, vertical = 6.dp)
            ) { GlassText("−0.5", fontWeight = FontWeight.SemiBold) }
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(10.dp))
                    .background(p.secondaryKeySurface)
                    .clickable { onChange((value + 0.5f).coerceAtMost(10f)) }
                    .padding(horizontal = 14.dp, vertical = 6.dp)
            ) { GlassText("+0.5", fontWeight = FontWeight.SemiBold) }
        }
    }
}

@Composable
private fun FunctionEditorDialog(
    title: String,
    initial: String,
    onConfirm: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val p = LocalGlassPalette.current
    var expr by remember { mutableStateOf(initial) }
    val presets = listOf("sin(x)", "cos(x)", "x^2 - 2", "x^3 - 3x", "ln(x)", "a*sin(b*x)")

    GlassModalCardDialog(onDismiss = onDismiss) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                GlassText(title, fontSizeSp = 18f, fontWeight = FontWeight.SemiBold)
                GlassCircleButton(onClick = onDismiss, contentDescription = "Close", size = 32.dp) {
                    IconCloseX(p.textSecondary, 15.dp)
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(p.displaySurface)
                    .border(0.8.dp, p.glassBorder, RoundedCornerShape(14.dp))
                    .padding(horizontal = 14.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                GlassText("y = ", color = p.textSecondary, fontSizeSp = 16f, fontWeight = FontWeight.SemiBold)
                BasicTextField(
                    value = expr,
                    onValueChange = { expr = it },
                    singleLine = true,
                    cursorBrush = SolidColor(p.accent),
                    textStyle = TextStyle(color = p.textPrimary, fontSize = 16.sp, fontWeight = FontWeight.Medium),
                    modifier = Modifier.weight(1f),
                    decorationBox = { inner ->
                        if (expr.isEmpty()) GlassText("e.g. sin(x) + 0.5x", color = p.textMuted, fontSizeSp = 15f)
                        inner()
                    }
                )
            }
            Spacer(modifier = Modifier.height(10.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                presets.take(3).forEach { chip ->
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(10.dp))
                            .background(p.secondaryKeySurface)
                            .clickable { expr = chip }
                            .padding(vertical = 7.dp),
                        contentAlignment = Alignment.Center
                    ) { GlassText(chip, fontSizeSp = 12.5f, fontWeight = FontWeight.Medium) }
                }
            }
            Spacer(modifier = Modifier.height(6.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                presets.drop(3).forEach { chip ->
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(10.dp))
                            .background(p.secondaryKeySurface)
                            .clickable { expr = chip }
                            .padding(vertical = 7.dp),
                        contentAlignment = Alignment.Center
                    ) { GlassText(chip, fontSizeSp = 12.5f, fontWeight = FontWeight.Medium) }
                }
            }
            Spacer(modifier = Modifier.height(14.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(44.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(p.accent)
                    .clickable { if (expr.isNotBlank()) onConfirm(expr) },
                contentAlignment = Alignment.Center
            ) {
                GlassText("Plot Function", color = p.onAccent, fontSizeSp = 15f, fontWeight = FontWeight.SemiBold)
            }
        }
    }
}

private fun niceStep(raw: Double): Double {
    val exp = floor(kotlin.math.log10(raw.coerceAtLeast(1e-6)))
    val pow10 = 10.0.pow(exp)
    val f = raw / pow10
    return when {
        f < 1.5 -> pow10
        f < 3.5 -> 2.0 * pow10
        f < 7.5 -> 5.0 * pow10
        else -> 10.0 * pow10
    }
}

private fun formatTick(v: Double): String {
    return if (abs(v - kotlin.math.round(v)) < 1e-6) {
        kotlin.math.round(v).toInt().toString()
    } else {
        String.format(Locale.US, "%.1f", v)
    }
}
