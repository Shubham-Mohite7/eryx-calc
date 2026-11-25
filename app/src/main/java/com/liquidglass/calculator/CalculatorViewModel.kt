package com.liquidglass.calculator

import android.app.Application
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.liquidglass.calculator.data.AccentOption
import com.liquidglass.calculator.data.AppIconStyle
import com.liquidglass.calculator.data.AppSettings
import com.liquidglass.calculator.data.CalculatorLayoutStyle
import com.liquidglass.calculator.data.CalculatorMode
import com.liquidglass.calculator.data.CurrencyState
import com.liquidglass.calculator.data.GraphFunctionItem
import com.liquidglass.calculator.data.HapticStrength
import com.liquidglass.calculator.data.HistoryFilter
import com.liquidglass.calculator.data.HistoryItem
import com.liquidglass.calculator.data.MemoryBehavior
import com.liquidglass.calculator.data.OneHandedMode
import com.liquidglass.calculator.data.ThemeMode
import com.liquidglass.calculator.engine.AngleMode
import com.liquidglass.calculator.engine.DecimalPrecisionMode
import com.liquidglass.calculator.engine.EvalResult
import com.liquidglass.calculator.engine.FinancialTool
import com.liquidglass.calculator.engine.MathEngine
import com.liquidglass.calculator.engine.UnitCategory
import com.liquidglass.calculator.engine.UnitConverter
import com.liquidglass.calculator.engine.UnitDef
import com.liquidglass.calculator.util.FeedbackManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.math.BigDecimal

enum class AppDestination {
    HOME,
    CALCULATOR,
    GRAPH,
    FUNCTIONS,
    FINANCIAL,
    HISTORY,
    SETTINGS,
    SETTINGS_APPEARANCE,
    SETTINGS_HAPTICS,
    SETTINGS_CALCULATION,
    SETTINGS_MEMORY,
    SETTINGS_ACCESSIBILITY,
    SETTINGS_ABOUT
}

data class CalculatorUiState(
    val expressionField: TextFieldValue = TextFieldValue(""),
    val liveResult: String = "0",
    val errorMessage: String? = null,
    val justEvaluated: Boolean = false,
    val isSecondFunction: Boolean = false,
    val canUndo: Boolean = false,
    val canRedo: Boolean = false,
    val toastMessage: String? = null
)

data class ConverterUiState(
    val category: UnitCategory = UnitCategory.LENGTH,
    val fromUnit: UnitDef = UnitConverter.unitsFor(UnitCategory.LENGTH)[0],
    val toUnit: UnitDef = UnitConverter.unitsFor(UnitCategory.LENGTH)[8], // ft
    val inputText: String = "1",
    val outputText: String = "3.28084",
    val showCategoryList: Boolean = false
)

class CalculatorViewModel(application: Application) : AndroidViewModel(application) {

    private val app = application as CalculatorApplication
    private val settingsRepo = app.settingsRepository
    private val historyDb = app.historyDatabase
    private val currencyRepo = app.currencyRepository
    private val feedback = app.feedbackManager

    val settings: StateFlow<AppSettings> = settingsRepo.settingsFlow
        .stateIn(viewModelScope, SharingStarted.Eagerly, AppSettings())

    val history: StateFlow<List<HistoryItem>> = historyDb.historyFlow
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    val graphFunctions: StateFlow<List<GraphFunctionItem>> = historyDb.graphFunctionsFlow
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    private val _destination = MutableStateFlow(AppDestination.CALCULATOR)
    val destination: StateFlow<AppDestination> = _destination.asStateFlow()

    private val backStack = ArrayDeque<AppDestination>()

    private val _calcState = MutableStateFlow(CalculatorUiState())
    val calcState: StateFlow<CalculatorUiState> = _calcState.asStateFlow()

    private val _converterState = MutableStateFlow(ConverterUiState())
    val converterState: StateFlow<ConverterUiState> = _converterState.asStateFlow()

    private val _currencyState = MutableStateFlow(CurrencyState())
    val currencyState: StateFlow<CurrencyState> = _currencyState.asStateFlow()

    private val _selectedFinancialTool = MutableStateFlow(FinancialTool.GST)
    val selectedFinancialTool: StateFlow<FinancialTool> = _selectedFinancialTool.asStateFlow()

    private val _historySearch = MutableStateFlow("")
    val historySearch: StateFlow<String> = _historySearch.asStateFlow()

    private val _historyFilter = MutableStateFlow(HistoryFilter.ALL)
    val historyFilter: StateFlow<HistoryFilter> = _historyFilter.asStateFlow()

    private val undoStack = ArrayDeque<TextFieldValue>()
    private val redoStack = ArrayDeque<TextFieldValue>()

    val filteredHistory: StateFlow<List<HistoryItem>> = combine(
        history,
        _historySearch,
        _historyFilter
    ) { items, query, filter ->
        val now = System.currentTimeMillis()
        val cal = java.util.Calendar.getInstance().apply {
            set(java.util.Calendar.HOUR_OF_DAY, 0)
            set(java.util.Calendar.MINUTE, 0)
            set(java.util.Calendar.SECOND, 0)
            set(java.util.Calendar.MILLISECOND, 0)
        }
        val startOfToday = cal.timeInMillis
        val startOfYesterday = startOfToday - 86_400_000L
        val last7Days = now - 7L * 86_400_000L
        val last30Days = now - 30L * 86_400_000L

        items.filter { item ->
            val matchesFilter = when (filter) {
                HistoryFilter.ALL -> true
                HistoryFilter.TODAY -> item.timestamp >= startOfToday
                HistoryFilter.YESTERDAY -> item.timestamp in startOfYesterday until startOfToday
                HistoryFilter.LAST_7_DAYS -> item.timestamp >= last7Days
                HistoryFilter.LAST_30_DAYS -> item.timestamp >= last30Days
                HistoryFilter.PINNED -> item.isPinned
                HistoryFilter.FAVORITES -> item.isFavorite
            }
            val matchesQuery = query.isBlank() ||
                item.expression.contains(query, ignoreCase = true) ||
                item.result.contains(query, ignoreCase = true)
            matchesFilter && matchesQuery
        }
    }.stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    init {
        viewModelScope.launch {
            settingsRepo.currencyCacheFlow.collectLatest { (json, updatedAt) ->
                val parsed = currencyRepo.parseCachedRates(json)
                _currencyState.update {
                    it.copy(
                        rates = parsed,
                        lastUpdatedMillis = updatedAt,
                        isLive = updatedAt > 0L
                    )
                }
                recomputeConversion()
            }
        }
        // Re-evaluate whenever precision, thousand separators, or angle mode change
        viewModelScope.launch {
            settings.collectLatest {
                evaluateLive(_calcState.value.expressionField.text)
                recomputeConversion()
            }
        }
    }

    // ---- Navigation ----

    fun navigateTo(dest: AppDestination) {
        if (_destination.value == dest) return
        backStack.addLast(_destination.value)
        _destination.value = dest
    }

    fun navigateBack(): Boolean {
        if (backStack.isEmpty()) {
            if (_destination.value != AppDestination.CALCULATOR) {
                _destination.value = AppDestination.CALCULATOR
                return true
            }
            return false
        }
        _destination.value = backStack.removeLast()
        return true
    }

    fun selectMode(mode: CalculatorMode, alsoNavigateToCalculator: Boolean = true) {
        triggerFeedback()
        viewModelScope.launch {
            settingsRepo.update { it.copy(lastMode = mode) }
        }
        if (alsoNavigateToCalculator) {
            backStack.clear()
            _destination.value = AppDestination.CALCULATOR
        }
    }

    fun openFinancialTool(tool: FinancialTool) {
        triggerFeedback()
        _selectedFinancialTool.value = tool
        navigateTo(AppDestination.FINANCIAL)
    }

    // ---- Feedback ----

    fun triggerFeedback() {
        val s = settings.value
        feedback.performKeyFeedback(s.hapticFeedback, s.hapticStrength, s.soundEnabled)
    }

    fun clearToast() {
        _calcState.update { it.copy(toastMessage = null) }
    }

    private fun showToast(msg: String) {
        _calcState.update { it.copy(toastMessage = msg) }
    }

    // ---- Expression Editing, Undo/Redo, Evaluation ----

    fun onExpressionFieldChanged(newValue: TextFieldValue) {
        val current = _calcState.value.expressionField
        if (newValue.text != current.text) {
            pushUndo(current)
            _calcState.update {
                it.copy(
                    expressionField = newValue,
                    justEvaluated = false,
                    canUndo = undoStack.isNotEmpty(),
                    canRedo = redoStack.isNotEmpty()
                )
            }
            evaluateLive(newValue.text)
        } else {
            _calcState.update { it.copy(expressionField = newValue) }
        }
    }

    fun insertToken(token: String) {
        triggerFeedback()
        val state = _calcState.value
        val current = state.expressionField
        val isOperatorStart = token in listOf("+", "−", "×", "÷", "^", "%", "²", "³", "!")

        val baseField = if (state.justEvaluated && !isOperatorStart) {
            TextFieldValue("")
        } else if (state.justEvaluated && isOperatorStart) {
            // Continue from previous result cleanly
            val prevClean = state.liveResult.replace(",", "")
            val text = if (prevClean == "Error" || state.errorMessage != null) "" else prevClean
            TextFieldValue(text, TextRange(text.length))
        } else {
            current
        }

        pushUndo(current)
        val selStart = baseField.selection.start.coerceIn(0, baseField.text.length)
        val selEnd = baseField.selection.end.coerceIn(0, baseField.text.length)
        val min = minOf(selStart, selEnd)
        val max = maxOf(selStart, selEnd)

        val newText = StringBuilder(baseField.text).replace(min, max, token).toString()
        val newCursor = min + token.length
        val updated = TextFieldValue(newText, TextRange(newCursor))

        _calcState.update {
            it.copy(
                expressionField = updated,
                justEvaluated = false,
                canUndo = undoStack.isNotEmpty(),
                canRedo = redoStack.isNotEmpty()
            )
        }
        evaluateLive(newText)
    }

    fun insertFunction(funcPrefix: String) {
        // e.g. "sin(" -> cursor inside
        insertToken(funcPrefix)
    }

    fun toggleSign() {
        triggerFeedback()
        val current = _calcState.value.expressionField
        val text = current.text
        pushUndo(current)
        if (text.isEmpty()) {
            val updated = TextFieldValue("−", TextRange(1))
            _calcState.update {
                it.copy(expressionField = updated, justEvaluated = false, canUndo = undoStack.isNotEmpty())
            }
            return
        }
        // Toggle unary minus on last number/group or whole expression
        val newText = if (text.startsWith("−(") && text.endsWith(")")) {
            text.substring(2, text.length - 1)
        } else if (text.startsWith("−") && text.drop(1).all { it.isDigit() || it == '.' || it == ',' }) {
            text.drop(1)
        } else if (text.all { it.isDigit() || it == '.' || it == ',' }) {
            "−$text"
        } else {
            "−($text)"
        }
        val updated = TextFieldValue(newText, TextRange(newText.length))
        _calcState.update {
            it.copy(
                expressionField = updated,
                justEvaluated = false,
                canUndo = undoStack.isNotEmpty(),
                canRedo = redoStack.isNotEmpty()
            )
        }
        evaluateLive(newText)
    }

    fun backspace() {
        triggerFeedback()
        val current = _calcState.value.expressionField
        val text = current.text
        if (text.isEmpty()) return
        pushUndo(current)

        val selStart = current.selection.start.coerceIn(0, text.length)
        val selEnd = current.selection.end.coerceIn(0, text.length)

        val (newText, newCursor) = if (selStart != selEnd) {
            val min = minOf(selStart, selEnd)
            val max = maxOf(selStart, selEnd)
            text.removeRange(min, max) to min
        } else if (selStart > 0) {
            // Smart delete function tokens like "sin(", "cos⁻¹(", "Ans", "log₁₀("
            val before = text.substring(0, selStart)
            val smartSuffixes = listOf(
                "sin⁻¹(", "cos⁻¹(", "tan⁻¹(", "sinh⁻¹(", "cosh⁻¹(", "tanh⁻¹(",
                "sinh(", "cosh(", "tanh(", "log₁₀(", "log₂(", "sin(", "cos(", "tan(",
                "ln(", "log(", "³√(", "√(", "abs(", "EXP", "Ans", "mod"
            )
            val matched = smartSuffixes.firstOrNull { before.endsWith(it) }
            val delLen = matched?.length ?: 1
            val startDel = (selStart - delLen).coerceAtLeast(0)
            text.removeRange(startDel, selStart) to startDel
        } else {
            text to 0
        }

        val updated = TextFieldValue(newText, TextRange(newCursor))
        _calcState.update {
            it.copy(
                expressionField = updated,
                justEvaluated = false,
                canUndo = undoStack.isNotEmpty(),
                canRedo = redoStack.isNotEmpty()
            )
        }
        evaluateLive(newText)
    }

    fun clearAll() {
        triggerFeedback()
        val current = _calcState.value.expressionField
        if (current.text.isNotEmpty()) {
            pushUndo(current)
        }
        _calcState.update {
            it.copy(
                expressionField = TextFieldValue(""),
                liveResult = "0",
                errorMessage = null,
                justEvaluated = false,
                canUndo = undoStack.isNotEmpty(),
                canRedo = redoStack.isNotEmpty()
            )
        }
    }

    fun undo() {
        if (undoStack.isEmpty()) return
        triggerFeedback()
        val current = _calcState.value.expressionField
        redoStack.addLast(current)
        val previous = undoStack.removeLast()
        _calcState.update {
            it.copy(
                expressionField = previous,
                justEvaluated = false,
                canUndo = undoStack.isNotEmpty(),
                canRedo = redoStack.isNotEmpty()
            )
        }
        evaluateLive(previous.text)
    }

    fun redo() {
        if (redoStack.isEmpty()) return
        triggerFeedback()
        val current = _calcState.value.expressionField
        undoStack.addLast(current)
        val next = redoStack.removeLast()
        _calcState.update {
            it.copy(
                expressionField = next,
                justEvaluated = false,
                canUndo = undoStack.isNotEmpty(),
                canRedo = redoStack.isNotEmpty()
            )
        }
        evaluateLive(next.text)
    }

    fun toggleSecondFunction() {
        triggerFeedback()
        _calcState.update { it.copy(isSecondFunction = !it.isSecondFunction) }
    }

    fun evaluateEquals() {
        triggerFeedback()
        val expr = _calcState.value.expressionField.text.trim()
        if (expr.isEmpty()) return
        val s = settings.value
        val ansBd = s.lastAnswer.toBigDecimalOrNull() ?: BigDecimal.ZERO
        val result = MathEngine.evaluate(
            expression = expr,
            angleMode = s.angleMode,
            ans = ansBd,
            autoCloseBrackets = s.autoCloseBrackets,
            precisionMode = s.decimalPrecision,
            useGrouping = s.showThousandSeparator
        )
        when (result) {
            is EvalResult.Success -> {
                _calcState.update {
                    it.copy(
                        liveResult = result.formatted,
                        errorMessage = null,
                        justEvaluated = true
                    )
                }
                if (s.voiceResults) {
                    feedback.speakResult(result.formatted)
                }
                val finalExpr = if (s.autoCloseBrackets) MathEngine.autoCloseParens(expr) else expr
                viewModelScope.launch {
                    settingsRepo.update { it.copy(lastAnswer = result.numericValue.toPlainString()) }
                    historyDb.insertHistory(finalExpr, result.formatted, s.lastMode)
                }
            }
            is EvalResult.Error -> {
                _calcState.update {
                    it.copy(
                        errorMessage = result.message,
                        justEvaluated = false
                    )
                }
            }
        }
    }

    private fun evaluateLive(expr: String) {
        if (expr.isBlank()) {
            _calcState.update { it.copy(liveResult = "0", errorMessage = null) }
            return
        }
        val s = settings.value
        val ansBd = s.lastAnswer.toBigDecimalOrNull() ?: BigDecimal.ZERO
        val result = MathEngine.evaluate(
            expression = expr,
            angleMode = s.angleMode,
            ans = ansBd,
            autoCloseBrackets = s.autoCloseBrackets,
            precisionMode = s.decimalPrecision,
            useGrouping = s.showThousandSeparator
        )
        when (result) {
            is EvalResult.Success -> {
                _calcState.update { it.copy(liveResult = result.formatted, errorMessage = null) }
            }
            is EvalResult.Error -> {
                // Keep last valid result silently during incomplete typing, show subtle indicator only if non-incomplete
                _calcState.update {
                    it.copy(
                        errorMessage = if (result.message == "Divide by zero" || result.message == "Domain error" || result.message == "Overflow") {
                            result.message
                        } else null
                    )
                }
            }
        }
    }

    private fun pushUndo(value: TextFieldValue) {
        if (undoStack.lastOrNull()?.text == value.text) return
        if (undoStack.size >= 40) undoStack.removeFirst()
        undoStack.addLast(value)
        redoStack.clear()
    }

    // ---- Memory Operations ----

    fun memoryClear() {
        triggerFeedback()
        viewModelScope.launch {
            settingsRepo.update { it.copy(memoryValue = "0", memorySlots = emptyList()) }
        }
        showToast("Memory cleared")
    }

    fun memoryRecall() {
        triggerFeedback()
        val mem = settings.value.memoryValue
        if (mem == "0") {
            showToast("Memory is empty")
            return
        }
        val formatted = MathEngine.formatNumber(
            mem.toBigDecimalOrNull() ?: BigDecimal.ZERO,
            settings.value.decimalPrecision,
            false
        )
        insertToken(formatted)
    }

    fun memoryAdd(sign: Int) {
        triggerFeedback()
        val currentVal = currentEvaluatedNumeric() ?: return
        viewModelScope.launch {
            settingsRepo.update { s ->
                val curMem = s.memoryValue.toBigDecimalOrNull() ?: BigDecimal.ZERO
                val updated = if (sign >= 0) curMem.add(currentVal) else curMem.subtract(currentVal)
                s.copy(memoryValue = updated.stripTrailingZeros().toPlainString())
            }
        }
        showToast(if (sign >= 0) "Added to memory" else "Subtracted from memory")
    }

    fun memoryStore() {
        triggerFeedback()
        val currentVal = currentEvaluatedNumeric() ?: return
        val plain = currentVal.stripTrailingZeros().toPlainString()
        viewModelScope.launch {
            settingsRepo.update { s ->
                val slots = if (s.memoryBehavior == MemoryBehavior.MULTI_SLOT) {
                    (listOf(plain) + s.memorySlots).take(10)
                } else {
                    listOf(plain)
                }
                s.copy(memoryValue = plain, memorySlots = slots)
            }
        }
        showToast("Saved to memory")
    }

    fun recallMemorySlot(slot: String) {
        insertToken(slot)
        navigateTo(AppDestination.CALCULATOR)
    }

    fun removeMemorySlot(index: Int) {
        viewModelScope.launch {
            settingsRepo.update { s ->
                val updated = s.memorySlots.toMutableList().apply {
                    if (index in indices) removeAt(index)
                }
                s.copy(
                    memorySlots = updated,
                    memoryValue = updated.firstOrNull() ?: "0"
                )
            }
        }
    }

    private fun currentEvaluatedNumeric(): BigDecimal? {
        val expr = _calcState.value.expressionField.text
        if (expr.isBlank()) return BigDecimal.ZERO
        val s = settings.value
        val ansBd = s.lastAnswer.toBigDecimalOrNull() ?: BigDecimal.ZERO
        return when (val r = MathEngine.evaluate(expr, s.angleMode, ansBd, autoCloseBrackets = s.autoCloseBrackets)) {
            is EvalResult.Success -> r.numericValue
            is EvalResult.Error -> null
        }
    }

    // ---- Clipboard & Sharing ----

    fun copyResult(context: Context, customText: String? = null) {
        val text = customText ?: _calcState.value.liveResult
        val cm = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        cm.setPrimaryClip(ClipData.newPlainText("Calculator Result", text))
        showToast("Copied $text")
    }

    fun pasteFromClipboard(context: Context) {
        val cm = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = cm.primaryClip?.getItemAt(0)?.coerceToText(context)?.toString()?.trim()
        if (!clip.isNullOrBlank()) {
            insertToken(clip)
        } else {
            showToast("Clipboard empty")
        }
    }

    fun shareCalculation(context: Context, expr: String? = null, res: String? = null) {
        val e = expr ?: _calcState.value.expressionField.text
        val r = res ?: _calcState.value.liveResult
        val shareBody = if (e.isBlank()) r else "$e = $r"
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, shareBody)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(Intent.createChooser(intent, "Share Calculation").apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        })
    }

    // ---- History Actions ----

    fun setHistorySearch(q: String) {
        _historySearch.value = q
    }

    fun setHistoryFilter(filter: HistoryFilter) {
        _historyFilter.value = filter
    }

    fun useHistoryItem(item: HistoryItem, editMode: Boolean) {
        triggerFeedback()
        val text = if (editMode) item.expression else item.result.replace(",", "")
        pushUndo(_calcState.value.expressionField)
        _calcState.update {
            it.copy(
                expressionField = TextFieldValue(text, TextRange(text.length)),
                justEvaluated = !editMode,
                canUndo = undoStack.isNotEmpty()
            )
        }
        evaluateLive(text)
        backStack.clear()
        _destination.value = AppDestination.CALCULATOR
    }

    fun toggleHistoryPin(id: Long) {
        viewModelScope.launch { historyDb.togglePin(id) }
    }

    fun toggleHistoryFavorite(id: Long) {
        viewModelScope.launch { historyDb.toggleFavorite(id) }
    }

    fun deleteHistoryItem(id: Long) {
        viewModelScope.launch { historyDb.deleteHistory(id) }
    }

    fun clearAllHistory() {
        viewModelScope.launch { historyDb.clearAllHistory(keepPinned = false) }
    }

    // ---- Converter Actions ----

    fun selectConverterCategory(cat: UnitCategory) {
        triggerFeedback()
        val units = UnitConverter.unitsFor(cat, _currencyState.value.rates)
        val from = units.first()
        val to = units.getOrElse(1) { units.first() }
        _converterState.update {
            it.copy(
                category = cat,
                fromUnit = from,
                toUnit = to,
                showCategoryList = false
            )
        }
        recomputeConversion()
    }

    fun setShowConverterCategoryList(show: Boolean) {
        _converterState.update { it.copy(showCategoryList = show) }
    }

    fun selectFromUnit(unit: UnitDef) {
        triggerFeedback()
        _converterState.update { it.copy(fromUnit = unit) }
        recomputeConversion()
    }

    fun selectToUnit(unit: UnitDef) {
        triggerFeedback()
        _converterState.update { it.copy(toUnit = unit) }
        recomputeConversion()
    }

    fun swapConverterUnits() {
        triggerFeedback()
        _converterState.update {
            it.copy(fromUnit = it.toUnit, toUnit = it.fromUnit)
        }
        recomputeConversion()
    }

    fun converterAppend(key: String) {
        triggerFeedback()
        val current = _converterState.value.inputText
        val next = when (key) {
            "AC" -> "0"
            "⌫" -> if (current.length <= 1 || (current.length == 2 && current.startsWith("-"))) "0" else current.dropLast(1)
            "+/-" -> if (current.startsWith("-")) current.drop(1) else if (current == "0") "0" else "-$current"
            "." -> if (current.contains(".")) current else "$current."
            else -> if (current == "0") key else current + key
        }
        _converterState.update { it.copy(inputText = next) }
        recomputeConversion()
    }

    fun setConverterInput(raw: String) {
        _converterState.update { it.copy(inputText = raw) }
        recomputeConversion()
    }

    private fun recomputeConversion() {
        val c = _converterState.value
        val s = settings.value
        val valNum = c.inputText.toDoubleOrNull() ?: 0.0
        val units = UnitConverter.unitsFor(c.category, _currencyState.value.rates)
        val from = units.firstOrNull { it.id == c.fromUnit.id } ?: c.fromUnit
        val to = units.firstOrNull { it.id == c.toUnit.id } ?: c.toUnit
        val out = UnitConverter.convert(valNum, c.category, from, to)
        val formatted = UnitConverter.formatConverted(
            out,
            c.category,
            useGrouping = s.showThousandSeparator,
            precisionMode = s.decimalPrecision
        )
        _converterState.update { it.copy(fromUnit = from, toUnit = to, outputText = formatted) }
    }

    fun refreshCurrencyRates() {
        viewModelScope.launch {
            _currencyState.update { it.copy(isRefreshing = true, statusMessage = null) }
            val result = currencyRepo.fetchLiveRates()
            result.fold(
                onSuccess = { (rates, ts) ->
                    _currencyState.update {
                        it.copy(
                            rates = rates,
                            lastUpdatedMillis = ts,
                            isLive = true,
                            isRefreshing = false,
                            statusMessage = "Rates updated"
                        )
                    }
                    recomputeConversion()
                },
                onFailure = {
                    _currencyState.update {
                        it.copy(
                            isRefreshing = false,
                            statusMessage = "Offline — using cached rates"
                        )
                    }
                }
            )
        }
    }

    // ---- Graph Actions ----

    private val graphPalette = listOf(0xFF22252A, 0xFF3B82F6, 0xFF10B981, 0xFFEC4899, 0xFF8B5CF6)

    fun addGraphFunction(expression: String) {
        if (expression.isBlank()) return
        val color = graphPalette[graphFunctions.value.size % graphPalette.size]
        viewModelScope.launch { historyDb.addGraphFunction(expression.trim(), color) }
    }

    fun updateGraphFunction(id: Long, expression: String) {
        if (expression.isBlank()) return
        viewModelScope.launch { historyDb.updateGraphFunction(id, expression.trim()) }
    }

    fun toggleGraphFunctionVisibility(id: Long) {
        viewModelScope.launch { historyDb.toggleGraphFunctionVisibility(id) }
    }

    fun deleteGraphFunction(id: Long) {
        viewModelScope.launch { historyDb.deleteGraphFunction(id) }
    }

    // ---- Settings Updates ----

    fun setThemeMode(mode: ThemeMode) = updateSettings { it.copy(themeMode = mode) }
    fun setAccentOption(accent: AccentOption) = updateSettings { it.copy(accentOption = accent) }
    fun setAppIconStyle(style: AppIconStyle) {
        updateSettings { it.copy(appIconStyle = style) }
        FeedbackManager.applyAppIconStyle(getApplication(), style)
    }
    fun setCalculatorLayout(layout: CalculatorLayoutStyle) = updateSettings { it.copy(calculatorLayout = layout) }
    fun setOneHandedMode(mode: OneHandedMode) = updateSettings { it.copy(oneHandedMode = mode) }
    fun setTranslucentUi(enabled: Boolean) = updateSettings { it.copy(translucentUi = enabled) }
    fun setReduceTransparency(enabled: Boolean) = updateSettings { it.copy(reduceTransparency = enabled) }
    fun setHapticFeedback(enabled: Boolean) = updateSettings { it.copy(hapticFeedback = enabled) }
    fun setHapticStrength(strength: HapticStrength) = updateSettings { it.copy(hapticStrength = strength) }
    fun setSoundEnabled(enabled: Boolean) = updateSettings { it.copy(soundEnabled = enabled) }
    fun setDecimalPrecision(mode: DecimalPrecisionMode) = updateSettings { it.copy(decimalPrecision = mode) }
    fun setAngleMode(mode: AngleMode) = updateSettings { it.copy(angleMode = mode) }
    fun setMemoryBehavior(behavior: MemoryBehavior) = updateSettings { it.copy(memoryBehavior = behavior) }
    fun setThousandSeparator(enabled: Boolean) = updateSettings { it.copy(showThousandSeparator = enabled) }
    fun setAutoCloseBrackets(enabled: Boolean) = updateSettings { it.copy(autoCloseBrackets = enabled) }
    fun setExpressionInMainDisplay(enabled: Boolean) = updateSettings { it.copy(expressionInMainDisplay = enabled) }
    fun setCopyResultWithUnits(enabled: Boolean) = updateSettings { it.copy(copyResultWithUnits = enabled) }
    fun setTextScale(scale: Float) = updateSettings { it.copy(textScale = scale.coerceIn(0.85f, 1.3f)) }
    fun setHighContrast(enabled: Boolean) = updateSettings { it.copy(highContrast = enabled) }
    fun setButtonLabels(enabled: Boolean) = updateSettings { it.copy(buttonLabels = enabled) }
    fun setVoiceResults(enabled: Boolean) = updateSettings { it.copy(voiceResults = enabled) }

    private fun updateSettings(block: (AppSettings) -> AppSettings) {
        viewModelScope.launch { settingsRepo.update(block) }
    }

    override fun onCleared() {
        super.onCleared()
        feedback.shutdown()
    }
}
