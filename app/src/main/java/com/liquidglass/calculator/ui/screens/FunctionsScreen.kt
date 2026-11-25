package com.liquidglass.calculator.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.liquidglass.calculator.ui.components.GlassCard
import com.liquidglass.calculator.ui.components.GlassSubpageTopBar
import com.liquidglass.calculator.ui.components.GlassText
import com.liquidglass.calculator.ui.components.IconChevronRight
import com.liquidglass.calculator.ui.theme.LocalGlassPalette

private enum class FunctionCategory(val label: String) {
    ALL("All"),
    TRIG("Trigonometry"),
    LOG("Logarithm"),
    ALGEBRA("Algebra"),
    HYPERBOLIC("Hyperbolic")
}

private data class MathFunctionDoc(
    val badge: String,
    val title: String,
    val description: String,
    val insertToken: String,
    val category: FunctionCategory
)

private val functionCatalog = listOf(
    MathFunctionDoc("sin", "sin(x)", "Sine — ratio of opposite to hypotenuse", "sin(", FunctionCategory.TRIG),
    MathFunctionDoc("cos", "cos(x)", "Cosine — ratio of adjacent to hypotenuse", "cos(", FunctionCategory.TRIG),
    MathFunctionDoc("tan", "tan(x)", "Tangent — sin(x) ÷ cos(x)", "tan(", FunctionCategory.TRIG),
    MathFunctionDoc("sin⁻¹", "sin⁻¹(x)", "Inverse sine (arcsin)", "sin⁻¹(", FunctionCategory.TRIG),
    MathFunctionDoc("cos⁻¹", "cos⁻¹(x)", "Inverse cosine (arccos)", "cos⁻¹(", FunctionCategory.TRIG),
    MathFunctionDoc("tan⁻¹", "tan⁻¹(x)", "Inverse tangent (arctan)", "tan⁻¹(", FunctionCategory.TRIG),
    MathFunctionDoc("log", "log(x)", "Common logarithm — base 10", "log₁₀(", FunctionCategory.LOG),
    MathFunctionDoc("ln", "ln(x)", "Natural logarithm — base e", "ln(", FunctionCategory.LOG),
    MathFunctionDoc("log₂", "log₂(x)", "Binary logarithm — base 2", "log₂(", FunctionCategory.LOG),
    MathFunctionDoc("eˣ", "eˣ", "Natural exponential function", "e^(", FunctionCategory.LOG),
    MathFunctionDoc("xʸ", "xʸ", "Power — x raised to y", "^", FunctionCategory.ALGEBRA),
    MathFunctionDoc("√x", "√x", "Principal square root of x", "√(", FunctionCategory.ALGEBRA),
    MathFunctionDoc("³√x", "³√x", "Cube root of x", "³√(", FunctionCategory.ALGEBRA),
    MathFunctionDoc("|x|", "|x|", "Absolute magnitude of x", "abs(", FunctionCategory.ALGEBRA),
    MathFunctionDoc("x!", "x!", "Factorial (and Gamma for non-integers)", "!", FunctionCategory.ALGEBRA),
    MathFunctionDoc("sinh", "sinh(x)", "Hyperbolic sine", "sinh(", FunctionCategory.HYPERBOLIC),
    MathFunctionDoc("cosh", "cosh(x)", "Hyperbolic cosine", "cosh(", FunctionCategory.HYPERBOLIC),
    MathFunctionDoc("tanh", "tanh(x)", "Hyperbolic tangent", "tanh(", FunctionCategory.HYPERBOLIC)
)

@Composable
fun FunctionsScreen(
    onInsertFunction: (String) -> Unit,
    onBack: () -> Unit
) {
    val p = LocalGlassPalette.current
    var selectedCategory by remember { mutableStateOf(FunctionCategory.ALL) }

    val filtered = remember(selectedCategory) {
        if (selectedCategory == FunctionCategory.ALL) functionCatalog
        else functionCatalog.filter { it.category == selectedCategory }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .navigationBarsPadding()
    ) {
        GlassSubpageTopBar(title = "Functions", onBack = onBack)

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            FunctionCategory.entries.forEach { cat ->
                val active = cat == selectedCategory
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(999.dp))
                        .background(if (active) p.accent else p.cardSurface)
                        .border(0.8.dp, p.glassBorder, RoundedCornerShape(999.dp))
                        .clickable { selectedCategory = cat }
                        .padding(horizontal = 14.dp, vertical = 7.dp)
                ) {
                    GlassText(
                        text = cat.label,
                        color = if (active) p.onAccent else p.textPrimary,
                        fontSizeSp = 13.5f,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(filtered, key = { it.title }) { fn ->
                GlassCard(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    onClick = { onInsertFunction(fn.insertToken) }
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 13.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(p.secondaryKeySurface)
                                .border(0.8.dp, p.glassBorder, RoundedCornerShape(12.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            GlassText(fn.badge, fontSizeSp = 13.5f, fontWeight = FontWeight.Bold)
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            GlassText(fn.title, fontSizeSp = 15.5f, fontWeight = FontWeight.SemiBold)
                            GlassText(fn.description, color = p.textSecondary, fontSizeSp = 12.5f)
                        }
                        IconChevronRight(p.textSecondary, 16.dp)
                    }
                }
            }
        }
    }
}
