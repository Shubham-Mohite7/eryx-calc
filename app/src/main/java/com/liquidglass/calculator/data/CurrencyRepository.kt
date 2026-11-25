package com.liquidglass.calculator.data

import com.liquidglass.calculator.engine.UnitConverter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

data class CurrencyState(
    val rates: Map<String, Double> = UnitConverter.defaultCurrencies,
    val lastUpdatedMillis: Long = 0L,
    val isLive: Boolean = false,
    val isRefreshing: Boolean = false,
    val statusMessage: String? = null
)

class CurrencyRepository(private val settingsRepository: SettingsRepository) {

    fun parseCachedRates(json: String?): Map<String, Double> {
        if (json.isNullOrBlank()) return UnitConverter.defaultCurrencies
        return try {
            val obj = JSONObject(json)
            val result = UnitConverter.defaultCurrencies.toMutableMap()
            val keys = obj.keys()
            while (keys.hasNext()) {
                val code = keys.next()
                val rate = obj.optDouble(code, -1.0)
                if (rate > 0.0) {
                    result[code] = rate
                }
            }
            result
        } catch (_: Exception) {
            UnitConverter.defaultCurrencies
        }
    }

    suspend fun fetchLiveRates(): Result<Pair<Map<String, Double>, Long>> = withContext(Dispatchers.IO) {
        try {
            val url = URL("https://api.frankfurter.app/latest?from=USD")
            val conn = (url.openConnection() as HttpURLConnection).apply {
                connectTimeout = 7000
                readTimeout = 7000
                requestMethod = "GET"
            }
            conn.connect()
            if (conn.responseCode !in 200..299) {
                return@withContext Result.failure(IllegalStateException("HTTP ${conn.responseCode}"))
            }
            val body = conn.inputStream.bufferedReader().use { it.readText() }
            val root = JSONObject(body)
            val ratesObj = root.getJSONObject("rates")
            val merged = UnitConverter.defaultCurrencies.toMutableMap()
            merged["USD"] = 1.0
            val keys = ratesObj.keys()
            while (keys.hasNext()) {
                val code = keys.next()
                val v = ratesObj.optDouble(code, -1.0)
                if (v > 0.0) merged[code] = v
            }
            val jsonToSave = JSONObject().apply {
                merged.forEach { (k, v) -> put(k, v) }
            }.toString()
            val now = System.currentTimeMillis()
            settingsRepository.saveCurrencyRates(jsonToSave, now)
            Result.success(merged to now)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
