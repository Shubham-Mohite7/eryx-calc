package com.liquidglass.calculator.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.liquidglass.calculator.engine.AngleMode
import com.liquidglass.calculator.engine.DecimalPrecisionMode
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.settingsDataStore: DataStore<Preferences> by preferencesDataStore(name = "calculator_settings")

class SettingsRepository(private val context: Context) {

    private object Keys {
        val THEME_MODE = stringPreferencesKey("theme_mode")
        val ACCENT = stringPreferencesKey("accent_option")
        val APP_ICON = stringPreferencesKey("app_icon_style")
        val LAYOUT = stringPreferencesKey("calculator_layout")
        val ONE_HANDED = stringPreferencesKey("one_handed_mode")
        val TRANSLUCENT_UI = booleanPreferencesKey("translucent_ui")
        val REDUCE_TRANSPARENCY = booleanPreferencesKey("reduce_transparency")
        val HAPTIC_FEEDBACK = booleanPreferencesKey("haptic_feedback")
        val HAPTIC_STRENGTH = stringPreferencesKey("haptic_strength")
        val SOUND_ENABLED = booleanPreferencesKey("sound_enabled")
        val DECIMAL_PRECISION = stringPreferencesKey("decimal_precision")
        val ANGLE_MODE = stringPreferencesKey("angle_mode")
        val MEMORY_BEHAVIOR = stringPreferencesKey("memory_behavior")
        val THOUSAND_SEPARATOR = booleanPreferencesKey("thousand_separator")
        val AUTO_CLOSE_BRACKETS = booleanPreferencesKey("auto_close_brackets")
        val EXPR_IN_MAIN = booleanPreferencesKey("expr_in_main")
        val COPY_WITH_UNITS = booleanPreferencesKey("copy_with_units")
        val TEXT_SCALE = floatPreferencesKey("text_scale")
        val HIGH_CONTRAST = booleanPreferencesKey("high_contrast")
        val BUTTON_LABELS = booleanPreferencesKey("button_labels")
        val VOICE_RESULTS = booleanPreferencesKey("voice_results")
        val LAST_MODE = stringPreferencesKey("last_mode")
        val MEMORY_VALUE = stringPreferencesKey("memory_value")
        val MEMORY_SLOTS = stringPreferencesKey("memory_slots")
        val LAST_ANSWER = stringPreferencesKey("last_answer")
        val CURRENCY_RATES_JSON = stringPreferencesKey("currency_rates_json")
        val CURRENCY_UPDATED_AT = longPreferencesKey("currency_updated_at")
        val AUTO_CHECK_UPDATES = booleanPreferencesKey("auto_check_updates")
        val LAST_UPDATE_CHECK = longPreferencesKey("last_update_check")
    }

    val settingsFlow: Flow<AppSettings> = context.settingsDataStore.data.map { prefs ->
        AppSettings(
            themeMode = enumOrDefault(prefs[Keys.THEME_MODE], ThemeMode.SYSTEM),
            accentOption = enumOrDefault(prefs[Keys.ACCENT], AccentOption.GRAPHITE),
            appIconStyle = enumOrDefault(prefs[Keys.APP_ICON], AppIconStyle.GLASS_LIGHT),
            calculatorLayout = enumOrDefault(prefs[Keys.LAYOUT], CalculatorLayoutStyle.CIRCULAR),
            oneHandedMode = enumOrDefault(prefs[Keys.ONE_HANDED], OneHandedMode.FULL),
            translucentUi = prefs[Keys.TRANSLUCENT_UI] ?: true,
            reduceTransparency = prefs[Keys.REDUCE_TRANSPARENCY] ?: false,
            hapticFeedback = prefs[Keys.HAPTIC_FEEDBACK] ?: true,
            hapticStrength = enumOrDefault(prefs[Keys.HAPTIC_STRENGTH], HapticStrength.MEDIUM),
            soundEnabled = prefs[Keys.SOUND_ENABLED] ?: false,
            decimalPrecision = enumOrDefault(prefs[Keys.DECIMAL_PRECISION], DecimalPrecisionMode.AUTO),
            angleMode = enumOrDefault(prefs[Keys.ANGLE_MODE], AngleMode.DEG),
            memoryBehavior = enumOrDefault(prefs[Keys.MEMORY_BEHAVIOR], MemoryBehavior.STANDARD),
            showThousandSeparator = prefs[Keys.THOUSAND_SEPARATOR] ?: true,
            autoCloseBrackets = prefs[Keys.AUTO_CLOSE_BRACKETS] ?: true,
            expressionInMainDisplay = prefs[Keys.EXPR_IN_MAIN] ?: false,
            copyResultWithUnits = prefs[Keys.COPY_WITH_UNITS] ?: false,
            textScale = prefs[Keys.TEXT_SCALE] ?: 1.0f,
            highContrast = prefs[Keys.HIGH_CONTRAST] ?: false,
            buttonLabels = prefs[Keys.BUTTON_LABELS] ?: false,
            voiceResults = prefs[Keys.VOICE_RESULTS] ?: false,
            lastMode = enumOrDefault(prefs[Keys.LAST_MODE], CalculatorMode.STANDARD),
            memoryValue = prefs[Keys.MEMORY_VALUE] ?: "0",
            memorySlots = prefs[Keys.MEMORY_SLOTS]
                ?.split("|")
                ?.filter { it.isNotBlank() }
                ?: emptyList(),
            lastAnswer = prefs[Keys.LAST_ANSWER] ?: "0",
            autoCheckUpdates = prefs[Keys.AUTO_CHECK_UPDATES] ?: true,
            lastUpdateCheckMillis = prefs[Keys.LAST_UPDATE_CHECK] ?: 0L
        )
    }

    val currencyCacheFlow: Flow<Pair<String?, Long>> = context.settingsDataStore.data.map { prefs ->
        prefs[Keys.CURRENCY_RATES_JSON] to (prefs[Keys.CURRENCY_UPDATED_AT] ?: 0L)
    }

    suspend fun update(transform: (AppSettings) -> AppSettings) {
        context.settingsDataStore.edit { prefs ->
            val current = AppSettings(
                themeMode = enumOrDefault(prefs[Keys.THEME_MODE], ThemeMode.SYSTEM),
                accentOption = enumOrDefault(prefs[Keys.ACCENT], AccentOption.GRAPHITE),
                appIconStyle = enumOrDefault(prefs[Keys.APP_ICON], AppIconStyle.GLASS_LIGHT),
                calculatorLayout = enumOrDefault(prefs[Keys.LAYOUT], CalculatorLayoutStyle.CIRCULAR),
                oneHandedMode = enumOrDefault(prefs[Keys.ONE_HANDED], OneHandedMode.FULL),
                translucentUi = prefs[Keys.TRANSLUCENT_UI] ?: true,
                reduceTransparency = prefs[Keys.REDUCE_TRANSPARENCY] ?: false,
                hapticFeedback = prefs[Keys.HAPTIC_FEEDBACK] ?: true,
                hapticStrength = enumOrDefault(prefs[Keys.HAPTIC_STRENGTH], HapticStrength.MEDIUM),
                soundEnabled = prefs[Keys.SOUND_ENABLED] ?: false,
                decimalPrecision = enumOrDefault(prefs[Keys.DECIMAL_PRECISION], DecimalPrecisionMode.AUTO),
                angleMode = enumOrDefault(prefs[Keys.ANGLE_MODE], AngleMode.DEG),
                memoryBehavior = enumOrDefault(prefs[Keys.MEMORY_BEHAVIOR], MemoryBehavior.STANDARD),
                showThousandSeparator = prefs[Keys.THOUSAND_SEPARATOR] ?: true,
                autoCloseBrackets = prefs[Keys.AUTO_CLOSE_BRACKETS] ?: true,
                expressionInMainDisplay = prefs[Keys.EXPR_IN_MAIN] ?: false,
                copyResultWithUnits = prefs[Keys.COPY_WITH_UNITS] ?: false,
                textScale = prefs[Keys.TEXT_SCALE] ?: 1.0f,
                highContrast = prefs[Keys.HIGH_CONTRAST] ?: false,
                buttonLabels = prefs[Keys.BUTTON_LABELS] ?: false,
                voiceResults = prefs[Keys.VOICE_RESULTS] ?: false,
                lastMode = enumOrDefault(prefs[Keys.LAST_MODE], CalculatorMode.STANDARD),
                memoryValue = prefs[Keys.MEMORY_VALUE] ?: "0",
                memorySlots = prefs[Keys.MEMORY_SLOTS]?.split("|")?.filter { it.isNotBlank() } ?: emptyList(),
                lastAnswer = prefs[Keys.LAST_ANSWER] ?: "0",
                autoCheckUpdates = prefs[Keys.AUTO_CHECK_UPDATES] ?: true,
                lastUpdateCheckMillis = prefs[Keys.LAST_UPDATE_CHECK] ?: 0L
            )
            val updated = transform(current)
            prefs[Keys.THEME_MODE] = updated.themeMode.name
            prefs[Keys.ACCENT] = updated.accentOption.name
            prefs[Keys.APP_ICON] = updated.appIconStyle.name
            prefs[Keys.LAYOUT] = updated.calculatorLayout.name
            prefs[Keys.ONE_HANDED] = updated.oneHandedMode.name
            prefs[Keys.TRANSLUCENT_UI] = updated.translucentUi
            prefs[Keys.REDUCE_TRANSPARENCY] = updated.reduceTransparency
            prefs[Keys.HAPTIC_FEEDBACK] = updated.hapticFeedback
            prefs[Keys.HAPTIC_STRENGTH] = updated.hapticStrength.name
            prefs[Keys.SOUND_ENABLED] = updated.soundEnabled
            prefs[Keys.DECIMAL_PRECISION] = updated.decimalPrecision.name
            prefs[Keys.ANGLE_MODE] = updated.angleMode.name
            prefs[Keys.MEMORY_BEHAVIOR] = updated.memoryBehavior.name
            prefs[Keys.THOUSAND_SEPARATOR] = updated.showThousandSeparator
            prefs[Keys.AUTO_CLOSE_BRACKETS] = updated.autoCloseBrackets
            prefs[Keys.EXPR_IN_MAIN] = updated.expressionInMainDisplay
            prefs[Keys.COPY_WITH_UNITS] = updated.copyResultWithUnits
            prefs[Keys.TEXT_SCALE] = updated.textScale
            prefs[Keys.HIGH_CONTRAST] = updated.highContrast
            prefs[Keys.BUTTON_LABELS] = updated.buttonLabels
            prefs[Keys.VOICE_RESULTS] = updated.voiceResults
            prefs[Keys.LAST_MODE] = updated.lastMode.name
            prefs[Keys.MEMORY_VALUE] = updated.memoryValue
            prefs[Keys.MEMORY_SLOTS] = updated.memorySlots.joinToString("|")
            prefs[Keys.LAST_ANSWER] = updated.lastAnswer
            prefs[Keys.AUTO_CHECK_UPDATES] = updated.autoCheckUpdates
            prefs[Keys.LAST_UPDATE_CHECK] = updated.lastUpdateCheckMillis
        }
    }

    suspend fun saveCurrencyRates(json: String, updatedAtMillis: Long) {
        context.settingsDataStore.edit { prefs ->
            prefs[Keys.CURRENCY_RATES_JSON] = json
            prefs[Keys.CURRENCY_UPDATED_AT] = updatedAtMillis
        }
    }

    private inline fun <reified T : Enum<T>> enumOrDefault(raw: String?, default: T): T {
        if (raw == null) return default
        return runCatching { enumValueOf<T>(raw) }.getOrDefault(default)
    }
}
