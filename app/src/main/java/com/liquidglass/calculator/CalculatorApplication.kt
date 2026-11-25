package com.liquidglass.calculator

import android.app.Application
import com.liquidglass.calculator.data.CurrencyRepository
import com.liquidglass.calculator.data.HistoryDatabase
import com.liquidglass.calculator.data.SettingsRepository
import com.liquidglass.calculator.data.UpdateRepository
import com.liquidglass.calculator.util.FeedbackManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class CalculatorApplication : Application() {
    val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)

    lateinit var settingsRepository: SettingsRepository
        private set
    lateinit var historyDatabase: HistoryDatabase
        private set
    lateinit var currencyRepository: CurrencyRepository
        private set
    lateinit var updateRepository: UpdateRepository
        private set
    lateinit var feedbackManager: FeedbackManager
        private set

    override fun onCreate() {
        super.onCreate()
        settingsRepository = SettingsRepository(this)
        historyDatabase = HistoryDatabase(this)
        currencyRepository = CurrencyRepository(settingsRepository)
        updateRepository = UpdateRepository(this)
        feedbackManager = FeedbackManager(this)

        applicationScope.launch {
            historyDatabase.loadInitial()
        }
    }
}
