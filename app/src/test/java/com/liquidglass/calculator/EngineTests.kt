package com.liquidglass.calculator

import com.liquidglass.calculator.engine.AngleMode
import com.liquidglass.calculator.engine.DecimalPrecisionMode
import com.liquidglass.calculator.engine.EvalResult
import com.liquidglass.calculator.engine.FinancialEngine
import com.liquidglass.calculator.engine.MathEngine
import com.liquidglass.calculator.engine.UnitCategory
import com.liquidglass.calculator.engine.UnitConverter
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.math.BigDecimal

class EngineTests {

    @Test
    fun standardArithmeticAndPrecedence() {
        val r1 = MathEngine.evaluate("12,500 × 4.5 + 320") as EvalResult.Success
        assertEquals("56,570", r1.formatted)

        val r2 = MathEngine.evaluate("0.1 + 0.2") as EvalResult.Success
        assertEquals("0.3", r2.formatted)

        val r3 = MathEngine.evaluate("2 + 3 × 4^2") as EvalResult.Success
        assertEquals("50", r3.formatted)

        val r4 = MathEngine.evaluate("(850 + 150) ÷ 4") as EvalResult.Success
        assertEquals("250", r4.formatted)
    }

    @Test
    fun percentagesAndImplicitMultiplication() {
        val r1 = MathEngine.evaluate("15% × 12000") as EvalResult.Success
        assertEquals("1,800", r1.formatted)

        val r2 = MathEngine.evaluate("100 + 10%") as EvalResult.Success
        assertEquals("110", r2.formatted)

        val r3 = MathEngine.evaluate("2(3 + 4)") as EvalResult.Success
        assertEquals("14", r3.formatted)
    }

    @Test
    fun scientificTrigAndPrecision() {
        val r1 = MathEngine.evaluate(
            "sin(45°) × √2",
            angleMode = AngleMode.DEG,
            precisionMode = DecimalPrecisionMode.FIXED_6
        ) as EvalResult.Success
        assertEquals("1.000000", r1.formatted)

        val r2 = MathEngine.evaluate("sin(30°) × √144", angleMode = AngleMode.DEG) as EvalResult.Success
        assertEquals("6", r2.formatted)

        val r3 = MathEngine.evaluate(
            "sin(30°) + log₁₀(100) × 2^3",
            angleMode = AngleMode.DEG
        ) as EvalResult.Success
        assertEquals("16.5", r3.formatted)

        val r4 = MathEngine.evaluate("5!") as EvalResult.Success
        assertEquals("120", r4.formatted)

        val r5 = MathEngine.evaluate("|-42.5|") as EvalResult.Success
        assertEquals("42.5", r5.formatted)
    }

    @Test
    fun gracefulErrorHandling() {
        val divZero = MathEngine.evaluate("10 ÷ 0")
        assertTrue(divZero is EvalResult.Error)

        val sqrtNeg = MathEngine.evaluate("√(-9)")
        assertTrue(sqrtNeg is EvalResult.Error)

        val badLog = MathEngine.evaluate("ln(0)")
        assertTrue(badLog is EvalResult.Error)
    }

    @Test
    fun unitConversionsAccurate() {
        val lenUnits = UnitConverter.unitsFor(UnitCategory.LENGTH)
        val m = lenUnits.first { it.id == "m" }
        val ft = lenUnits.first { it.id == "ft" }
        val feet = UnitConverter.convert(1250.0, UnitCategory.LENGTH, m, ft)
        assertEquals(4101.05, feet, 0.01)

        val tempUnits = UnitConverter.unitsFor(UnitCategory.TEMPERATURE)
        val c = tempUnits.first { it.id == "C" }
        val f = tempUnits.first { it.id == "F" }
        assertEquals(212.0, UnitConverter.convert(100.0, UnitCategory.TEMPERATURE, c, f), 1e-6)
        assertEquals(0.0, UnitConverter.convert(32.0, UnitCategory.TEMPERATURE, f, c), 1e-6)
    }

    @Test
    fun financialEnginesAccurate() {
        val gstExcl = FinancialEngine.calculateGst(1000.0, 18.0, isInclusive = false)
        assertEquals(180.0, gstExcl.gstAmount, 1e-6)
        assertEquals(1180.0, gstExcl.totalAmount, 1e-6)

        val gstIncl = FinancialEngine.calculateGst(1180.0, 18.0, isInclusive = true)
        assertEquals(1000.0, gstIncl.netAmount, 1e-6)
        assertEquals(180.0, gstIncl.gstAmount, 1e-6)

        val emi = FinancialEngine.calculateEmi(100_000.0, 12.0, 12)
        assertEquals(8884.88, emi.monthlyEmi, 0.05)

        val disc = FinancialEngine.calculateDiscount(1200.0, 25.0, 10.0)
        assertEquals(300.0, disc.discountAmount, 1e-6)
        assertEquals(900.0, disc.priceAfterDiscount, 1e-6)
        assertEquals(990.0, disc.finalPrice, 1e-6)
    }

    @Test
    fun ansVariableWorks() {
        val r = MathEngine.evaluate("Ans × 2", ans = BigDecimal("25")) as EvalResult.Success
        assertEquals("50", r.formatted)
    }
}
