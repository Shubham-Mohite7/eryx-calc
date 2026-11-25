package com.liquidglass.calculator.widget

import android.content.Context
import android.content.Intent
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.action.ActionParameters
import androidx.glance.action.actionParametersOf
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.action.ActionCallback
import androidx.glance.appwidget.action.actionRunCallback
import androidx.glance.appwidget.action.actionStartActivity
import androidx.glance.appwidget.cornerRadius
import androidx.glance.appwidget.provideContent
import androidx.glance.appwidget.state.updateAppWidgetState
import androidx.glance.background
import androidx.glance.currentState
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.layout.width
import androidx.glance.state.PreferencesGlanceStateDefinition
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import com.liquidglass.calculator.MainActivity
import com.liquidglass.calculator.engine.EvalResult
import com.liquidglass.calculator.engine.MathEngine

private val KEY_EXPR = stringPreferencesKey("widget_expr")
private val KEY_RESULT = stringPreferencesKey("widget_result")
private val PARAM_KEY = ActionParameters.Key<String>("key")

class CalculatorWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = CalculatorWidget()
}

class CalculatorWidget : GlanceAppWidget() {
    override val stateDefinition = PreferencesGlanceStateDefinition

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        provideContent {
            val prefs = currentState<Preferences>()
            val expr = prefs[KEY_EXPR] ?: ""
            val result = prefs[KEY_RESULT] ?: "0"
            WidgetContent(context = context, expr = expr, result = result)
        }
    }
}

@Composable
private fun WidgetContent(context: Context, expr: String, result: String) {
    val bgCard = ColorProvider(Color(0xEBEEF2F6))
    val bgDisplay = ColorProvider(Color(0xF8FFFFFF))
    val bgKey = ColorProvider(Color(0xF2FFFFFF))
    val bgAccent = ColorProvider(Color(0xFF22252A))
    val fgPrimary = ColorProvider(Color(0xFF14171C))
    val fgSecondary = ColorProvider(Color(0xFF5D6570))
    val fgOnAccent = ColorProvider(Color.White)

    val launchMain = Intent(context, MainActivity::class.java).apply {
        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
    }

    Column(
        modifier = GlanceModifier
            .fillMaxSize()
            .cornerRadius(22.dp)
            .background(bgCard)
            .padding(10.dp)
    ) {
        // Display header (tap to open full app)
        Column(
            modifier = GlanceModifier
                .fillMaxWidth()
                .cornerRadius(16.dp)
                .background(bgDisplay)
                .clickable(actionStartActivity(launchMain))
                .padding(horizontal = 12.dp, vertical = 8.dp),
            horizontalAlignment = Alignment.End
        ) {
            Text(
                text = if (expr.isEmpty()) "eryx" else expr,
                maxLines = 1,
                style = TextStyle(color = fgSecondary, fontSize = 12.sp)
            )
            Text(
                text = result,
                maxLines = 1,
                style = TextStyle(color = fgPrimary, fontSize = 24.sp, fontWeight = FontWeight.Bold)
            )
        }

        Spacer(modifier = GlanceModifier.height(8.dp))

        // Interactive Quick Pad
        val rows = listOf(
            listOf("7", "8", "9", "÷"),
            listOf("4", "5", "6", "×"),
            listOf("1", "2", "3", "−"),
            listOf("AC", "0", "=", "+")
        )
        rows.forEachIndexed { rIdx, row ->
            if (rIdx > 0) Spacer(modifier = GlanceModifier.height(5.dp))
            Row(modifier = GlanceModifier.fillMaxWidth().defaultWeight()) {
                row.forEachIndexed { cIdx, label ->
                    if (cIdx > 0) Spacer(modifier = GlanceModifier.width(5.dp))
                    val isEq = label == "="
                    Box(
                        modifier = GlanceModifier
                            .defaultWeight()
                            .height(34.dp)
                            .cornerRadius(12.dp)
                            .background(if (isEq) bgAccent else bgKey)
                            .clickable(
                                actionRunCallback<WidgetKeyAction>(
                                    actionParametersOf(PARAM_KEY to label)
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = label,
                            style = TextStyle(
                                color = if (isEq) fgOnAccent else fgPrimary,
                                fontSize = 15.sp,
                                fontWeight = if (isEq || label == "AC") FontWeight.Bold else FontWeight.Medium
                            )
                        )
                    }
                }
            }
        }
    }
}

class WidgetKeyAction : ActionCallback {
    override suspend fun onAction(
        context: Context,
        glanceId: GlanceId,
        parameters: ActionParameters
    ) {
        val key = parameters[PARAM_KEY] ?: return
        updateAppWidgetState(context, PreferencesGlanceStateDefinition, glanceId) { prefs ->
            val mutable = prefs.toMutablePreferences()
            val curExpr = mutable[KEY_EXPR] ?: ""
            when (key) {
                "AC" -> {
                    mutable[KEY_EXPR] = ""
                    mutable[KEY_RESULT] = "0"
                }
                "=" -> {
                    if (curExpr.isNotBlank()) {
                        when (val res = MathEngine.evaluate(curExpr)) {
                            is EvalResult.Success -> mutable[KEY_RESULT] = res.formatted
                            is EvalResult.Error -> mutable[KEY_RESULT] = res.message
                        }
                    }
                }
                else -> {
                    val nextExpr = curExpr + key
                    mutable[KEY_EXPR] = nextExpr
                    when (val res = MathEngine.evaluate(nextExpr)) {
                        is EvalResult.Success -> mutable[KEY_RESULT] = res.formatted
                        is EvalResult.Error -> Unit
                    }
                }
            }
            mutable
        }
        CalculatorWidget().update(context, glanceId)
    }
}
