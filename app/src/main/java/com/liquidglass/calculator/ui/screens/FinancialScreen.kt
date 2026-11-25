package com.liquidglass.calculator.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.liquidglass.calculator.CalculatorViewModel
import com.liquidglass.calculator.engine.FinancialEngine
import com.liquidglass.calculator.engine.FinancialTool
import com.liquidglass.calculator.engine.MathEngine
import com.liquidglass.calculator.ui.components.GlassCard
import com.liquidglass.calculator.ui.components.GlassSegmentedTextBar
import com.liquidglass.calculator.ui.components.GlassSubpageTopBar
import com.liquidglass.calculator.ui.components.GlassText
import com.liquidglass.calculator.ui.theme.LocalGlassPalette
import java.math.BigDecimal

@Composable
fun FinancialScreen(
    selectedTool: FinancialTool,
    viewModel: CalculatorViewModel,
    onSelectTool: (FinancialTool) -> Unit,
    onBack: () -> Unit
) {
    val p = LocalGlassPalette.current
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .navigationBarsPadding()
    ) {
        GlassSubpageTopBar(
            title = "Financial Calculators",
            onBack = onBack
        )

        // Horizontal Tool Selector Pills
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            FinancialTool.entries.forEach { tool ->
                val active = tool == selectedTool
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(999.dp))
                        .background(if (active) p.accent else p.cardSurface)
                        .border(0.8.dp, p.glassBorder, RoundedCornerShape(999.dp))
                        .clickable { onSelectTool(tool) }
                        .padding(horizontal = 14.dp, vertical = 8.dp)
                ) {
                    GlassText(
                        text = tool.title,
                        color = if (active) p.onAccent else p.textPrimary,
                        fontSizeSp = 13.5f,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            when (selectedTool) {
                FinancialTool.GST -> GstCalculatorView(onCopy = { viewModel.copyResult(context, it) })
                FinancialTool.EMI -> EmiCalculatorView(onCopy = { viewModel.copyResult(context, it) })
                FinancialTool.DISCOUNT -> DiscountCalculatorView(onCopy = { viewModel.copyResult(context, it) })
                FinancialTool.PERCENTAGE -> PercentageCalculatorView(onCopy = { viewModel.copyResult(context, it) })
                FinancialTool.TIP_SPLIT -> TipSplitCalculatorView(onCopy = { viewModel.copyResult(context, it) })
                FinancialTool.COMPOUND -> CompoundInterestView(onCopy = { viewModel.copyResult(context, it) })
            }
        }
    }
}

@Composable
private fun GstCalculatorView(onCopy: (String) -> Unit) {
    val p = LocalGlassPalette.current
    var amount by remember { mutableStateOf("1000") }
    var rate by remember { mutableStateOf("18") }
    var inclusive by remember { mutableStateOf(false) }

    val res = FinancialEngine.calculateGst(
        amount.toDoubleOrNull() ?: 0.0,
        rate.toDoubleOrNull() ?: 0.0,
        inclusive
    )

    GlassCard(shape = RoundedCornerShape(24.dp)) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            GlassSegmentedTextBar(
                options = listOf("Exclusive (+GST)", "Inclusive (Incl.)"),
                selectedIndex = if (inclusive) 1 else 0,
                onSelect = { inclusive = it == 1 }
            )
            FinanceInputField("Amount", amount) { amount = it }
            FinanceInputField("GST Rate (%)", rate) { rate = it }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf("5", "12", "18", "28").forEach { preset ->
                    val sel = rate == preset
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (sel) p.accent else p.secondaryKeySurface)
                            .clickable { rate = preset }
                            .padding(vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        GlassText("$preset%", color = if (sel) p.onAccent else p.textPrimary, fontSizeSp = 13.5f, fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }
    }

    FinanceBreakdownCard(
        title = "GST Summary",
        primaryLabel = "Total Amount",
        primaryValue = fmtMoney(res.totalAmount),
        rows = listOf(
            "Net Amount" to fmtMoney(res.netAmount),
            "Total GST" to fmtMoney(res.gstAmount),
            "CGST (50%)" to fmtMoney(res.cgstAmount),
            "SGST (50%)" to fmtMoney(res.sgstAmount)
        ),
        onCopy = { onCopy(fmtMoney(res.totalAmount)) }
    )
}

@Composable
private fun EmiCalculatorView(onCopy: (String) -> Unit) {
    var principal by remember { mutableStateOf("500000") }
    var annualRate by remember { mutableStateOf("8.5") }
    var tenureYears by remember { mutableStateOf("5") }
    var inMonths by remember { mutableStateOf(false) }

    val rawTenure = tenureYears.toDoubleOrNull() ?: 0.0
    val months = if (inMonths) rawTenure.toInt() else (rawTenure * 12.0).toInt()
    val res = FinancialEngine.calculateEmi(
        principal.toDoubleOrNull() ?: 0.0,
        annualRate.toDoubleOrNull() ?: 0.0,
        months
    )

    GlassCard(shape = RoundedCornerShape(24.dp)) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            FinanceInputField("Loan Principal", principal) { principal = it }
            FinanceInputField("Annual Interest Rate (%)", annualRate) { annualRate = it }
            GlassSegmentedTextBar(
                options = listOf("Years", "Months"),
                selectedIndex = if (inMonths) 1 else 0,
                onSelect = { inMonths = it == 1 }
            )
            FinanceInputField(if (inMonths) "Tenure (Months)" else "Tenure (Years)", tenureYears) { tenureYears = it }
        }
    }

    FinanceBreakdownCard(
        title = "Loan Breakdown",
        primaryLabel = "Monthly EMI",
        primaryValue = fmtMoney(res.monthlyEmi),
        rows = listOf(
            "Principal" to fmtMoney(principal.toDoubleOrNull() ?: 0.0),
            "Total Interest" to fmtMoney(res.totalInterest),
            "Total Payable" to fmtMoney(res.totalPayment),
            "Principal Share" to "${(res.principalShare * 100).toInt()}%"
        ),
        onCopy = { onCopy(fmtMoney(res.monthlyEmi)) }
    )
}

@Composable
private fun DiscountCalculatorView(onCopy: (String) -> Unit) {
    var price by remember { mutableStateOf("1200") }
    var discount by remember { mutableStateOf("25") }
    var tax by remember { mutableStateOf("0") }

    val res = FinancialEngine.calculateDiscount(
        price.toDoubleOrNull() ?: 0.0,
        discount.toDoubleOrNull() ?: 0.0,
        tax.toDoubleOrNull() ?: 0.0
    )

    GlassCard(shape = RoundedCornerShape(24.dp)) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            FinanceInputField("Original Price", price) { price = it }
            FinanceInputField("Discount (%)", discount) { discount = it }
            FinanceInputField("Sales Tax (%)", tax) { tax = it }
        }
    }

    FinanceBreakdownCard(
        title = "Price Breakdown",
        primaryLabel = "Final Price",
        primaryValue = fmtMoney(res.finalPrice),
        rows = listOf(
            "You Save" to fmtMoney(res.discountAmount),
            "After Discount" to fmtMoney(res.priceAfterDiscount),
            "Tax Added" to fmtMoney(res.taxAmount)
        ),
        onCopy = { onCopy(fmtMoney(res.finalPrice)) }
    )
}

@Composable
private fun PercentageCalculatorView(onCopy: (String) -> Unit) {
    var modeIdx by remember { mutableIntStateOf(0) }
    var a by remember { mutableStateOf("15") }
    var b by remember { mutableStateOf("12000") }

    val x = a.toDoubleOrNull() ?: 0.0
    val y = b.toDoubleOrNull() ?: 0.0
    val (labelA, labelB, outTitle, outVal) = when (modeIdx) {
        0 -> listOf("Percentage (%)", "Of Value", "$a% of $b", fmtMoney(y * (x / 100.0)))
        1 -> listOf("Part Value", "Total Value", "$a of $b is", "${fmtMoney(if (y != 0.0) (x / y) * 100.0 else 0.0)}%")
        else -> listOf("From Value", "To Value", "% Change", "${fmtMoney(if (x != 0.0) ((y - x) / x) * 100.0 else 0.0)}%")
    }

    GlassCard(shape = RoundedCornerShape(24.dp)) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            GlassSegmentedTextBar(
                options = listOf("X% of Y", "X / Y %", "% Change"),
                selectedIndex = modeIdx,
                onSelect = { modeIdx = it }
            )
            FinanceInputField(labelA, a) { a = it }
            FinanceInputField(labelB, b) { b = it }
        }
    }

    FinanceBreakdownCard(
        title = "Result",
        primaryLabel = outTitle,
        primaryValue = outVal,
        rows = emptyList(),
        onCopy = { onCopy(outVal) }
    )
}

@Composable
private fun TipSplitCalculatorView(onCopy: (String) -> Unit) {
    var bill by remember { mutableStateOf("84.50") }
    var tipPct by remember { mutableStateOf("15") }
    var people by remember { mutableStateOf("4") }

    val res = FinancialEngine.calculateTipSplit(
        bill.toDoubleOrNull() ?: 0.0,
        tipPct.toDoubleOrNull() ?: 0.0,
        (people.toIntOrNull() ?: 1).coerceAtLeast(1)
    )

    GlassCard(shape = RoundedCornerShape(24.dp)) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            FinanceInputField("Bill Amount", bill) { bill = it }
            FinanceInputField("Tip (%)", tipPct) { tipPct = it }
            FinanceInputField("Split Between (People)", people) { people = it }
        }
    }

    FinanceBreakdownCard(
        title = "Split Summary",
        primaryLabel = "Per Person",
        primaryValue = fmtMoney(res.perPerson),
        rows = listOf(
            "Tip Amount" to fmtMoney(res.tipAmount),
            "Tip / Person" to fmtMoney(res.tipPerPerson),
            "Total Bill" to fmtMoney(res.totalBill)
        ),
        onCopy = { onCopy(fmtMoney(res.perPerson)) }
    )
}

@Composable
private fun CompoundInterestView(onCopy: (String) -> Unit) {
    var principal by remember { mutableStateOf("10000") }
    var monthly by remember { mutableStateOf("250") }
    var rate by remember { mutableStateOf("7.5") }
    var years by remember { mutableStateOf("10") }

    val res = FinancialEngine.calculateCompoundInterest(
        principal.toDoubleOrNull() ?: 0.0,
        monthly.toDoubleOrNull() ?: 0.0,
        rate.toDoubleOrNull() ?: 0.0,
        years.toDoubleOrNull() ?: 0.0
    )

    GlassCard(shape = RoundedCornerShape(24.dp)) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            FinanceInputField("Initial Principal", principal) { principal = it }
            FinanceInputField("Monthly Contribution", monthly) { monthly = it }
            FinanceInputField("Annual Return (%)", rate) { rate = it }
            FinanceInputField("Duration (Years)", years) { years = it }
        }
    }

    FinanceBreakdownCard(
        title = "Projected Growth",
        primaryLabel = "Future Value",
        primaryValue = fmtMoney(res.futureValue),
        rows = listOf(
            "Total Invested" to fmtMoney(res.totalInvested),
            "Interest Earned" to fmtMoney(res.interestEarned)
        ),
        onCopy = { onCopy(fmtMoney(res.futureValue)) }
    )
}

@Composable
private fun FinanceInputField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit
) {
    val p = LocalGlassPalette.current
    Column {
        GlassText(text = label, color = p.textSecondary, fontSizeSp = 12.5f, fontWeight = FontWeight.Medium)
        Spacer(modifier = Modifier.height(5.dp))
        BasicTextField(
            value = value,
            onValueChange = { raw ->
                val filtered = raw.filter { it.isDigit() || it == '.' || it == '-' }
                onValueChange(filtered)
            },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            cursorBrush = SolidColor(p.accent),
            textStyle = TextStyle(
                color = p.textPrimary,
                fontSize = (18f * p.textScale).sp,
                fontWeight = FontWeight.SemiBold,
                fontFeatureSettings = "tnum"
            ),
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(14.dp))
                .background(p.displaySurface)
                .border(0.8.dp, p.glassBorder, RoundedCornerShape(14.dp))
                .padding(horizontal = 14.dp, vertical = 11.dp)
        )
    }
}

@Composable
private fun FinanceBreakdownCard(
    title: String,
    primaryLabel: String,
    primaryValue: String,
    rows: List<Pair<String, String>>,
    onCopy: () -> Unit
) {
    val p = LocalGlassPalette.current
    GlassCard(
        shape = RoundedCornerShape(24.dp),
        containerColor = p.displaySurface,
        onClick = onCopy
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                GlassText(title, color = p.textSecondary, fontSizeSp = 13f, fontWeight = FontWeight.SemiBold)
                GlassText("Tap to copy", color = p.textMuted, fontSizeSp = 11.5f)
            }
            Spacer(modifier = Modifier.height(6.dp))
            GlassText(primaryLabel, color = p.textSecondary, fontSizeSp = 13.5f)
            GlassText(primaryValue, fontSizeSp = 30f, fontWeight = FontWeight.Bold)

            if (rows.isNotEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp)
                        .height(0.8.dp)
                        .background(p.subtleDivider)
                )
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    rows.forEach { (k, v) ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            GlassText(k, color = p.textSecondary, fontSizeSp = 14f)
                            GlassText(v, fontSizeSp = 14.5f, fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
            }
        }
    }
}

private fun fmtMoney(value: Double): String {
    if (value.isNaN() || value.isInfinite()) return "0"
    val bd = BigDecimal.valueOf(value)
    return MathEngine.formatNumber(
        bd,
        com.liquidglass.calculator.engine.DecimalPrecisionMode.FIXED_2,
        useGrouping = true
    )
}
