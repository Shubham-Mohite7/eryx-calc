package com.liquidglass.calculator.engine

import java.math.BigDecimal
import java.math.MathContext
import java.math.RoundingMode
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Locale
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.acos
import kotlin.math.acosh
import kotlin.math.asin
import kotlin.math.asinh
import kotlin.math.atan
import kotlin.math.atanh
import kotlin.math.cbrt
import kotlin.math.ceil
import kotlin.math.cos
import kotlin.math.cosh
import kotlin.math.exp
import kotlin.math.floor
import kotlin.math.ln
import kotlin.math.log10
import kotlin.math.log2
import kotlin.math.pow
import kotlin.math.round
import kotlin.math.sign
import kotlin.math.sin
import kotlin.math.sinh
import kotlin.math.sqrt
import kotlin.math.tan
import kotlin.math.tanh

enum class AngleMode(val label: String, val shortLabel: String) {
    DEG("Degrees (°)", "Deg"),
    RAD("Radians (rad)", "Rad"),
    GRAD("Gradians (gon)", "Grad")
}

enum class DecimalPrecisionMode(val label: String, val digits: Int?) {
    AUTO("Auto (Smart)", null),
    FIXED_2("2 decimals", 2),
    FIXED_4("4 decimals", 4),
    FIXED_6("6 decimals", 6),
    FIXED_8("8 decimals", 8),
    FIXED_10("10 decimals", 10),
    FIXED_12("12 decimals", 12)
}

sealed interface EvalResult {
    data class Success(
        val numericValue: BigDecimal,
        val doubleValue: Double,
        val formatted: String
    ) : EvalResult

    data class Error(val message: String) : EvalResult
}

object MathEngine {
    private val MC = MathContext.DECIMAL128
    private val BD_100 = BigDecimal("100")

    fun evaluate(
        expression: String,
        angleMode: AngleMode = AngleMode.DEG,
        ans: BigDecimal = BigDecimal.ZERO,
        variables: Map<String, Double> = emptyMap(),
        autoCloseBrackets: Boolean = true,
        precisionMode: DecimalPrecisionMode = DecimalPrecisionMode.AUTO,
        useGrouping: Boolean = true
    ): EvalResult {
        val trimmed = expression.trim()
        if (trimmed.isEmpty()) {
            return EvalResult.Success(BigDecimal.ZERO, 0.0, "0")
        }

        val prepared = if (autoCloseBrackets) autoCloseParens(trimmed) else trimmed
        return try {
            val tokens = tokenize(prepared)
            if (tokens.isEmpty()) {
                return EvalResult.Success(BigDecimal.ZERO, 0.0, "0")
            }
            val parser = Parser(tokens, angleMode, ans, variables)
            val value = parser.parseExpression()
            if (!parser.isAtEnd()) {
                return EvalResult.Error("Syntax error")
            }
            val bd = value.toCleanBigDecimal()
            val dbl = bd.toDouble()
            if (dbl.isNaN() || dbl.isInfinite()) {
                return EvalResult.Error("Math error")
            }
            val formatted = formatNumber(bd, precisionMode, useGrouping)
            EvalResult.Success(bd, dbl, formatted)
        } catch (e: MathEvalException) {
            EvalResult.Error(e.message ?: "Error")
        } catch (_: ArithmeticException) {
            EvalResult.Error("Divide by zero")
        } catch (_: NumberFormatException) {
            EvalResult.Error("Number format error")
        } catch (_: Exception) {
            EvalResult.Error("Syntax error")
        }
    }

    /**
     * Fast evaluation returning a Double for interactive 2D graphing at 60fps.
     */
    fun evaluateForGraph(
        expression: String,
        x: Double,
        params: Map<String, Double> = emptyMap(),
        angleMode: AngleMode = AngleMode.RAD
    ): Double {
        if (expression.isBlank()) return Double.NaN
        return try {
            val vars = HashMap<String, Double>(params.size + 1)
            vars.putAll(params)
            vars["x"] = x
            val tokens = tokenize(autoCloseParens(expression.trim()))
            if (tokens.isEmpty()) return Double.NaN
            val parser = Parser(tokens, angleMode, BigDecimal.ZERO, vars)
            val result = parser.parseExpression()
            if (!parser.isAtEnd()) return Double.NaN
            result.asDouble()
        } catch (_: Exception) {
            Double.NaN
        }
    }

    fun autoCloseParens(expr: String): String {
        var open = 0
        for (ch in expr) {
            if (ch == '(') open++
            else if (ch == ')' && open > 0) open--
        }
        return if (open > 0) expr + ")".repeat(open) else expr
    }

    fun formatNumber(
        value: BigDecimal,
        precisionMode: DecimalPrecisionMode = DecimalPrecisionMode.AUTO,
        useGrouping: Boolean = true
    ): String {
        val cleaned = if (value.compareTo(BigDecimal.ZERO) == 0) BigDecimal.ZERO else value.stripTrailingZeros()
        val absVal = cleaned.abs()

        if (precisionMode == DecimalPrecisionMode.AUTO) {
            if (cleaned.compareTo(BigDecimal.ZERO) != 0 &&
                (absVal >= BigDecimal("1e12") || absVal < BigDecimal("1e-9"))
            ) {
                val sci = DecimalFormat("0.########E0", DecimalFormatSymbols(Locale.US))
                return sci.format(cleaned)
            }
            val rounded = cleaned.setScale(10, RoundingMode.HALF_UP).stripTrailingZeros()
            val plain = if (rounded.compareTo(BigDecimal.ZERO) == 0) BigDecimal.ZERO else rounded
            val symbols = DecimalFormatSymbols(Locale.US)
            val pattern = if (useGrouping) "#,##0.##########" else "0.##########"
            return DecimalFormat(pattern, symbols).format(plain)
        }

        val scale = precisionMode.digits ?: 6
        if (cleaned.compareTo(BigDecimal.ZERO) != 0 &&
            (absVal >= BigDecimal("1e12") || absVal < BigDecimal("1e-12"))
        ) {
            val sci = DecimalFormat("0.${"0".repeat(scale.coerceAtMost(8))}E0", DecimalFormatSymbols(Locale.US))
            return sci.format(cleaned)
        }
        val rounded = cleaned.setScale(scale, RoundingMode.HALF_UP)
        val symbols = DecimalFormatSymbols(Locale.US)
        val frac = "0".repeat(scale)
        val pattern = if (useGrouping) "#,##0.$frac" else "0.$frac"
        return DecimalFormat(pattern, symbols).format(rounded)
    }

    // ---- Internal Number Representation (exact BigDecimal when possible, Double for transcendentals) ----

    private sealed interface NumVal {
        fun asBigDecimal(): BigDecimal
        fun asDouble(): Double
        fun toCleanBigDecimal(): BigDecimal

        data class Exact(val bd: BigDecimal) : NumVal {
            override fun asBigDecimal(): BigDecimal = bd
            override fun asDouble(): Double = bd.toDouble()
            override fun toCleanBigDecimal(): BigDecimal =
                if (bd.compareTo(BigDecimal.ZERO) == 0) BigDecimal.ZERO else bd.stripTrailingZeros()
        }

        data class Approx(val d: Double) : NumVal {
            override fun asBigDecimal(): BigDecimal {
                if (d.isNaN() || d.isInfinite()) throw MathEvalException("Math error")
                return BigDecimal.valueOf(d)
            }

            override fun asDouble(): Double = d
            override fun toCleanBigDecimal(): BigDecimal {
                if (d.isNaN()) throw MathEvalException("Domain error")
                if (d.isInfinite()) throw MathEvalException("Overflow")
                // Snap near-zero values from trig like sin(π) or cos(90°)
                if (abs(d) < 1e-13) return BigDecimal.ZERO
                val bd = BigDecimal.valueOf(d).round(MathContext(13, RoundingMode.HALF_UP))
                // Snap values extremely close to integers or clean tenths like 0.9999999999999
                val nearestInt = bd.setScale(0, RoundingMode.HALF_UP)
                if (bd.subtract(nearestInt).abs() < BigDecimal("1e-11")) {
                    return nearestInt
                }
                return bd.stripTrailingZeros()
            }
        }
    }

    private class MathEvalException(message: String) : RuntimeException(message)

    // ---- Tokenizer ----

    private enum class TokenType {
        NUMBER, IDENT, OP, LPAREN, RPAREN, PIPE, COMMA, POSTFIX
    }

    private data class Token(val type: TokenType, val text: String)

    private fun tokenize(input: String): List<Token> {
        val tokens = ArrayList<Token>(input.length)
        var i = 0
        val n = input.length

        while (i < n) {
            val ch = input[i]
            if (ch.isWhitespace() || ch == ',') {
                // Note: commas inside numbers like 12,500 are stripped unless inside multi-arg function
                if (ch == ',') {
                    // Check if previous was digit and next is 3 digits (thousand separator)
                    val prevDigit = i > 0 && input[i - 1].isDigit()
                    val nextDigit = i + 1 < n && input[i + 1].isDigit()
                    if (!prevDigit || !nextDigit) {
                        tokens.add(Token(TokenType.COMMA, ","))
                    }
                }
                i++
                continue
            }

            // Number literal (including scientific notation like 1.5E-4 or 2E6)
            if (ch.isDigit() || ch == '.') {
                val start = i
                var hasDot = ch == '.'
                i++
                while (i < n) {
                    val c = input[i]
                    if (c.isDigit()) {
                        i++
                    } else if (c == '.' && !hasDot) {
                        hasDot = true
                        i++
                    } else if (c == ',' && i + 1 < n && input[i + 1].isDigit()) {
                        // Thousand separator inside number
                        i++
                    } else if ((c == 'E' || c == 'e') && i + 1 < n &&
                        (input[i + 1].isDigit() || input[i + 1] == '+' || input[i + 1] == '-' || input[i + 1] == '−')
                    ) {
                        i++
                        if (i < n && (input[i] == '+' || input[i] == '-' || input[i] == '−')) {
                            i++
                        }
                        if (i >= n || !input[i].isDigit()) {
                            throw MathEvalException("Invalid exponent")
                        }
                    } else {
                        break
                    }
                }
                val numText = input.substring(start, i)
                    .replace(",", "")
                    .replace('−', '-')
                if (numText == ".") throw MathEvalException("Syntax error")
                tokens.add(Token(TokenType.NUMBER, numText))
                continue
            }

            // Multi-char roots & special symbols
            if (ch == '³' && i + 1 < n && input[i + 1] == '√') {
                tokens.add(Token(TokenType.IDENT, "cbrt"))
                i += 2
                continue
            }
            if (ch == '√') {
                tokens.add(Token(TokenType.IDENT, "sqrt"))
                i++
                continue
            }
            if (ch == 'π') {
                tokens.add(Token(TokenType.IDENT, "pi"))
                i++
                continue
            }
            if (ch == '²') {
                tokens.add(Token(TokenType.POSTFIX, "²"))
                i++
                continue
            }
            if (ch == '³') {
                tokens.add(Token(TokenType.POSTFIX, "³"))
                i++
                continue
            }
            if (ch == '°') {
                tokens.add(Token(TokenType.POSTFIX, "°"))
                i++
                continue
            }
            if (ch == '!') {
                tokens.add(Token(TokenType.POSTFIX, "!"))
                i++
                continue
            }
            if (ch == '%') {
                tokens.add(Token(TokenType.POSTFIX, "%"))
                i++
                continue
            }
            if (ch == '(') {
                tokens.add(Token(TokenType.LPAREN, "("))
                i++
                continue
            }
            if (ch == ')') {
                tokens.add(Token(TokenType.RPAREN, ")"))
                i++
                continue
            }
            if (ch == '|') {
                tokens.add(Token(TokenType.PIPE, "|"))
                i++
                continue
            }
            if (ch in charArrayOf('+', '-', '−', '×', '*', '÷', '/', '^')) {
                val normalized = when (ch) {
                    '−' -> "-"
                    '×' -> "*"
                    '÷' -> "/"
                    else -> ch.toString()
                }
                tokens.add(Token(TokenType.OP, normalized))
                i++
                continue
            }

            // Identifiers: sin, cos, sin⁻¹, log₁₀, log₂, EXP, Ans, mod, x, e, etc.
            if (ch.isLetter()) {
                val start = i
                i++
                while (i < n && (input[i].isLetterOrDigit() || input[i] in "⁻¹₀₁₂")) {
                    i++
                }
                val ident = input.substring(start, i)
                if (ident == "mod") {
                    tokens.add(Token(TokenType.OP, "mod"))
                } else if (ident == "EXP") {
                    // Treat a EXP b as a * 10^b
                    tokens.add(Token(TokenType.OP, "EXP"))
                } else {
                    tokens.add(Token(TokenType.IDENT, ident))
                }
                continue
            }

            throw MathEvalException("Unexpected '$ch'")
        }
        return tokens
    }

    // ---- Recursive Descent Parser ----

    private class Parser(
        private val tokens: List<Token>,
        private val angleMode: AngleMode,
        private val ans: BigDecimal,
        private val variables: Map<String, Double>
    ) {
        private var pos = 0

        fun isAtEnd(): Boolean = pos >= tokens.size
        private fun peek(): Token? = tokens.getOrNull(pos)
        private fun peekNext(): Token? = tokens.getOrNull(pos + 1)
        private fun consume(): Token = tokens[pos++]

        fun parseExpression(): NumVal = parseAddSub()

        private fun parseAddSub(): NumVal {
            var left = parseMulDiv()
            while (true) {
                val tok = peek() ?: break
                if (tok.type == TokenType.OP && (tok.text == "+" || tok.text == "-")) {
                    consume()
                    // Smart calculator percentage: e.g. 100 + 10% -> 100 + (100 * 0.10)
                    // Only apply if RHS is a standalone percentage term (number or parenthesized expr followed by %)
                    val rightIsBarePercent = isStandalonePercentAhead()
                    val right = parseMulDiv()
                    left = if (rightIsBarePercent) {
                        val delta = multiply(left, right)
                        if (tok.text == "+") add(left, delta) else subtract(left, delta)
                    } else {
                        if (tok.text == "+") add(left, right) else subtract(left, right)
                    }
                } else {
                    break
                }
            }
            return left
        }

        private fun isStandalonePercentAhead(): Boolean {
            val t0 = peek() ?: return false
            val t1 = peekNext() ?: return false
            return t0.type == TokenType.NUMBER && t1.type == TokenType.POSTFIX && t1.text == "%"
        }

        private fun parseMulDiv(): NumVal {
            var left = parseUnary()
            while (true) {
                val tok = peek() ?: break
                if (tok.type == TokenType.OP && (tok.text == "*" || tok.text == "/" || tok.text == "mod" || tok.text == "EXP")) {
                    consume()
                    val right = parseUnary()
                    left = when (tok.text) {
                        "*" -> multiply(left, right)
                        "/" -> divide(left, right)
                        "mod" -> modulo(left, right)
                        "EXP" -> exp10Mul(left, right)
                        else -> left
                    }
                } else if (shouldImplicitMultiply(tok)) {
                    val right = parseUnary()
                    left = multiply(left, right)
                } else {
                    break
                }
            }
            return left
        }

        private fun shouldImplicitMultiply(next: Token): Boolean {
            return when (next.type) {
                TokenType.LPAREN, TokenType.NUMBER, TokenType.IDENT -> true
                else -> false
            }
        }

        private fun parseUnary(): NumVal {
            val tok = peek()
            if (tok != null && tok.type == TokenType.OP && (tok.text == "+" || tok.text == "-")) {
                consume()
                val operand = parseUnary()
                return if (tok.text == "-") negate(operand) else operand
            }
            return parsePower()
        }

        private fun parsePower(): NumVal {
            val base = parsePostfix()
            val tok = peek()
            if (tok != null && tok.type == TokenType.OP && tok.text == "^") {
                consume()
                val exponent = parseUnary() // Right-associative: 2^3^2 = 2^(3^2) = 512
                return power(base, exponent)
            }
            return base
        }

        private fun parsePostfix(): NumVal {
            var value = parsePrimary()
            while (true) {
                val tok = peek() ?: break
                if (tok.type == TokenType.POSTFIX) {
                    consume()
                    value = when (tok.text) {
                        "!" -> factorial(value)
                        "%" -> divide(value, NumVal.Exact(BD_100))
                        "²" -> power(value, NumVal.Exact(BigDecimal(2)))
                        "³" -> power(value, NumVal.Exact(BigDecimal(3)))
                        "°" -> toRadiansFromDegrees(value)
                        else -> value
                    }
                } else {
                    break
                }
            }
            return value
        }

        private fun parsePrimary(): NumVal {
            val tok = peek() ?: throw MathEvalException("Incomplete expression")
            return when (tok.type) {
                TokenType.NUMBER -> {
                    consume()
                    NumVal.Exact(BigDecimal(tok.text))
                }

                TokenType.LPAREN -> {
                    consume()
                    val inner = parseExpression()
                    val close = peek()
                    if (close == null || close.type != TokenType.RPAREN) {
                        throw MathEvalException("Missing ')'")
                    }
                    consume()
                    inner
                }

                TokenType.PIPE -> {
                    consume()
                    val inner = parseExpression()
                    val close = peek()
                    if (close == null || close.type != TokenType.PIPE) {
                        throw MathEvalException("Missing '|'")
                    }
                    consume()
                    absVal(inner)
                }

                TokenType.IDENT -> {
                    consume()
                    parseIdentifier(tok.text)
                }

                else -> throw MathEvalException("Syntax error")
            }
        }

        private fun parseIdentifier(rawName: String): NumVal {
            val name = rawName.lowercase(Locale.US)
            // Constants & variables
            when (name) {
                "pi" -> return NumVal.Approx(PI)
                "e" -> {
                    // Check if user meant e^(...) or constant e
                    return NumVal.Approx(kotlin.math.E)
                }
                "ans" -> return NumVal.Exact(ans)
            }
            variables[rawName]?.let { return NumVal.Approx(it) }
            variables[name]?.let { return NumVal.Approx(it) }

            // Check for function with optional parentheses, e.g., sin(30) or sin 30 or √144
            val args = if (peek()?.type == TokenType.LPAREN) {
                consume() // '('
                val list = mutableListOf<NumVal>()
                if (peek()?.type != TokenType.RPAREN) {
                    list.add(parseExpression())
                    while (peek()?.type == TokenType.COMMA) {
                        consume()
                        list.add(parseExpression())
                    }
                }
                if (peek()?.type != TokenType.RPAREN) {
                    throw MathEvalException("Missing ')'")
                }
                consume()
                list
            } else {
                // Allow prefix function without parens, e.g. √144, sin30
                listOf(parsePower())
            }

            if (args.isEmpty()) throw MathEvalException("Missing argument for $rawName")
            val x = args[0].asDouble()

            val res: Double = when (name) {
                "sin" -> sin(toRadIfNeeded(x, args[0]))
                "cos" -> cos(toRadIfNeeded(x, args[0]))
                "tan" -> {
                    val rad = toRadIfNeeded(x, args[0])
                    if (abs(cos(rad)) < 1e-14) throw MathEvalException("Undefined (tan)")
                    tan(rad)
                }
                "csc" -> {
                    val s = sin(toRadIfNeeded(x, args[0]))
                    if (abs(s) < 1e-14) throw MathEvalException("Undefined (csc)")
                    1.0 / s
                }
                "sec" -> {
                    val c = cos(toRadIfNeeded(x, args[0]))
                    if (abs(c) < 1e-14) throw MathEvalException("Undefined (sec)")
                    1.0 / c
                }
                "cot" -> {
                    val s = sin(toRadIfNeeded(x, args[0]))
                    if (abs(s) < 1e-14) throw MathEvalException("Undefined (cot)")
                    cos(toRadIfNeeded(x, args[0])) / s
                }
                "asin", "sin⁻¹" -> {
                    if (x < -1.0 || x > 1.0) throw MathEvalException("Domain error")
                    fromRad(asin(x))
                }
                "acos", "cos⁻¹" -> {
                    if (x < -1.0 || x > 1.0) throw MathEvalException("Domain error")
                    fromRad(acos(x))
                }
                "atan", "tan⁻¹" -> fromRad(atan(x))
                "sinh" -> sinh(x)
                "cosh" -> cosh(x)
                "tanh" -> tanh(x)
                "asinh", "sinh⁻¹" -> asinh(x)
                "acosh", "cosh⁻¹" -> {
                    if (x < 1.0) throw MathEvalException("Domain error")
                    acosh(x)
                }
                "atanh", "tanh⁻¹" -> {
                    if (x <= -1.0 || x >= 1.0) throw MathEvalException("Domain error")
                    atanh(x)
                }
                "ln" -> {
                    if (x <= 0.0) throw MathEvalException("Domain error")
                    ln(x)
                }
                "log", "log₁₀", "log10" -> {
                    if (x <= 0.0) throw MathEvalException("Domain error")
                    log10(x)
                }
                "log₂", "log2" -> {
                    if (x <= 0.0) throw MathEvalException("Domain error")
                    log2(x)
                }
                "sqrt" -> {
                    if (x < 0.0) throw MathEvalException("Domain error")
                    return exactOrApproxSqrt(args[0])
                }
                "cbrt" -> cbrt(x)
                "abs" -> return absVal(args[0])
                "exp" -> exp(x)
                "floor" -> floor(x)
                "ceil" -> ceil(x)
                "round" -> round(x)
                "fact" -> return factorial(args[0])
                "pow" -> {
                    val y = args.getOrNull(1) ?: throw MathEvalException("pow needs 2 args")
                    return power(args[0], y)
                }
                else -> throw MathEvalException("Unknown '$rawName'")
            }
            if (res.isNaN()) throw MathEvalException("Domain error")
            if (res.isInfinite()) throw MathEvalException("Overflow")
            return NumVal.Approx(res)
        }

        private fun toRadIfNeeded(rawDouble: Double, num: NumVal): Double {
            // If user wrote `sin(45°)`, the postfix `°` already converted 45 -> radians in radians-neutral form!
            // Wait: we mark explicit degree conversion via DegreeRad wrapper? Let's handle it cleanly:
            if (num is DegreeConverted) return num.radians
            return when (angleMode) {
                AngleMode.DEG -> rawDouble * PI / 180.0
                AngleMode.RAD -> rawDouble
                AngleMode.GRAD -> rawDouble * PI / 200.0
            }
        }

        private fun fromRad(rad: Double): Double = when (angleMode) {
            AngleMode.DEG -> rad * 180.0 / PI
            AngleMode.RAD -> rad
            AngleMode.GRAD -> rad * 200.0 / PI
        }

        private fun toRadiansFromDegrees(num: NumVal): NumVal {
            return DegreeConverted(num.asDouble() * PI / 180.0)
        }

        private data class DegreeConverted(val radians: Double) : NumVal {
            override fun asBigDecimal(): BigDecimal = BigDecimal.valueOf(radians)
            override fun asDouble(): Double = radians
            override fun toCleanBigDecimal(): BigDecimal = BigDecimal.valueOf(radians)
        }

        private fun add(a: NumVal, b: NumVal): NumVal =
            if (a is NumVal.Exact && b is NumVal.Exact) {
                NumVal.Exact(a.bd.add(b.bd, MC))
            } else {
                NumVal.Approx(a.asDouble() + b.asDouble())
            }

        private fun subtract(a: NumVal, b: NumVal): NumVal =
            if (a is NumVal.Exact && b is NumVal.Exact) {
                NumVal.Exact(a.bd.subtract(b.bd, MC))
            } else {
                NumVal.Approx(a.asDouble() - b.asDouble())
            }

        private fun multiply(a: NumVal, b: NumVal): NumVal =
            if (a is NumVal.Exact && b is NumVal.Exact) {
                NumVal.Exact(a.bd.multiply(b.bd, MC))
            } else {
                NumVal.Approx(a.asDouble() * b.asDouble())
            }

        private fun divide(a: NumVal, b: NumVal): NumVal {
            if (abs(b.asDouble()) == 0.0 && b.asBigDecimal().compareTo(BigDecimal.ZERO) == 0) {
                throw MathEvalException("Divide by zero")
            }
            return if (a is NumVal.Exact && b is NumVal.Exact) {
                NumVal.Exact(a.bd.divide(b.bd, MC))
            } else {
                val bd = b.asDouble()
                if (bd == 0.0) throw MathEvalException("Divide by zero")
                NumVal.Approx(a.asDouble() / bd)
            }
        }

        private fun modulo(a: NumVal, b: NumVal): NumVal {
            if (b.asDouble() == 0.0) throw MathEvalException("Divide by zero")
            return if (a is NumVal.Exact && b is NumVal.Exact) {
                NumVal.Exact(a.bd.remainder(b.bd, MC))
            } else {
                NumVal.Approx(a.asDouble() % b.asDouble())
            }
        }

        private fun exp10Mul(a: NumVal, b: NumVal): NumVal {
            val expInt = if (b is NumVal.Exact && b.bd.stripTrailingZeros().scale() <= 0 && b.bd.abs() <= BigDecimal(1000)) {
                b.bd.toInt()
            } else null
            return if (a is NumVal.Exact && expInt != null) {
                NumVal.Exact(a.bd.scaleByPowerOfTen(expInt))
            } else {
                NumVal.Approx(a.asDouble() * 10.0.pow(b.asDouble()))
            }
        }

        private fun negate(a: NumVal): NumVal =
            if (a is NumVal.Exact) NumVal.Exact(a.bd.negate()) else NumVal.Approx(-a.asDouble())

        private fun absVal(a: NumVal): NumVal =
            if (a is NumVal.Exact) NumVal.Exact(a.bd.abs()) else NumVal.Approx(abs(a.asDouble()))

        private fun exactOrApproxSqrt(a: NumVal): NumVal {
            val d = a.asDouble()
            if (d < 0.0) throw MathEvalException("Domain error")
            val r = sqrt(d)
            val rounded = round(r)
            if (a is NumVal.Exact && abs(r - rounded) < 1e-12 && rounded * rounded == d) {
                return NumVal.Exact(BigDecimal.valueOf(rounded.toLong()))
            }
            return NumVal.Approx(r)
        }

        private fun power(base: NumVal, exp: NumVal): NumVal {
            if (base is NumVal.Exact && exp is NumVal.Exact) {
                val stripped = exp.bd.stripTrailingZeros()
                if (stripped.scale() <= 0 && stripped.abs() <= BigDecimal(500)) {
                    val intExp = stripped.intValueExact()
                    return if (intExp >= 0) {
                        NumVal.Exact(base.bd.pow(intExp, MC))
                    } else {
                        if (base.bd.compareTo(BigDecimal.ZERO) == 0) throw MathEvalException("Divide by zero")
                        NumVal.Exact(BigDecimal.ONE.divide(base.bd.pow(-intExp, MC), MC))
                    }
                }
            }
            val b = base.asDouble()
            val e = exp.asDouble()
            if (b < 0.0 && e != floor(e)) throw MathEvalException("Domain error")
            val res = b.pow(e)
            if (res.isNaN()) throw MathEvalException("Domain error")
            if (res.isInfinite()) throw MathEvalException("Overflow")
            return NumVal.Approx(res)
        }

        private fun factorial(a: NumVal): NumVal {
            val d = a.asDouble()
            if (d < 0.0 && d == floor(d)) throw MathEvalException("Domain error")
            if (d > 170.0) throw MathEvalException("Overflow")
            if (d == floor(d)) {
                val n = d.toInt()
                if (n <= 20) {
                    var acc = 1L
                    for (k in 2..n) acc *= k
                    return NumVal.Exact(BigDecimal(acc))
                }
                var acc = BigDecimal.ONE
                for (k in 2..n) acc = acc.multiply(BigDecimal(k), MC)
                return NumVal.Exact(acc)
            }
            // Lanczos approximation for Gamma(d + 1)
            return NumVal.Approx(gamma(d + 1.0))
        }

        private fun gamma(z: Double): Double {
            val g = 7.0
            val p = doubleArrayOf(
                0.99999999999980993,
                676.5203681218851,
                -1259.1392167224028,
                771.32342877765313,
                -176.61502916214059,
                12.507343278686905,
                -0.13857109526572012,
                9.9843695780195716e-6,
                1.5056327351493116e-7
            )
            if (z < 0.5) {
                return PI / (sin(PI * z) * gamma(1.0 - z))
            }
            val zm1 = z - 1.0
            var x = p[0]
            for (i in 1 until p.size) {
                x += p[i] / (zm1 + i)
            }
            val t = zm1 + g + 0.5
            return sqrt(2.0 * PI) * t.pow(zm1 + 0.5) * exp(-t) * x
        }
    }
}
