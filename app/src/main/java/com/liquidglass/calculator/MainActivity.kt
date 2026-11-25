package com.liquidglass.calculator

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.isSystemInDarkTheme
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
import androidx.compose.foundation.clickable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.liquidglass.calculator.data.ThemeMode
import com.liquidglass.calculator.data.UpdateStatus
import com.liquidglass.calculator.ui.components.GlassCard
import com.liquidglass.calculator.ui.components.GlassModalCardDialog
import com.liquidglass.calculator.ui.components.GlassText
import com.liquidglass.calculator.ui.components.LiquidGlassBackground
import com.liquidglass.calculator.ui.screens.AboutSettingsScreen
import com.liquidglass.calculator.ui.screens.AccessibilitySettingsScreen
import com.liquidglass.calculator.ui.screens.AppearanceSettingsScreen
import com.liquidglass.calculator.ui.screens.CalculationSettingsScreen
import com.liquidglass.calculator.ui.screens.CalculatorScreen
import com.liquidglass.calculator.ui.screens.ConverterContent
import com.liquidglass.calculator.ui.screens.FinancialScreen
import com.liquidglass.calculator.ui.screens.FunctionsScreen
import com.liquidglass.calculator.ui.screens.GraphScreen
import com.liquidglass.calculator.ui.screens.HapticsSoundSettingsScreen
import com.liquidglass.calculator.ui.screens.HistoryScreen
import com.liquidglass.calculator.ui.screens.HomeScreen
import com.liquidglass.calculator.ui.screens.MemorySettingsScreen
import com.liquidglass.calculator.ui.screens.SettingsMainScreen
import com.liquidglass.calculator.ui.theme.LiquidGlassTheme
import com.liquidglass.calculator.ui.theme.LocalGlassPalette
import kotlinx.coroutines.delay

class MainActivity : ComponentActivity() {

    private val viewModel: CalculatorViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            val settings by viewModel.settings.collectAsStateWithLifecycle()
            val systemDark = isSystemInDarkTheme()
            val isDark = when (settings.themeMode) {
                ThemeMode.LIGHT -> false
                ThemeMode.DARK -> true
                ThemeMode.SYSTEM -> systemDark
            }

            val view = LocalView.current
            SideEffect {
                val controller = WindowCompat.getInsetsController(window, view)
                controller.isAppearanceLightStatusBars = !isDark
                controller.isAppearanceLightNavigationBars = !isDark
            }

            LiquidGlassTheme(settings = settings) {
                CalculatorRoot(viewModel = viewModel)
            }
        }
    }
}

@Composable
private fun CalculatorRoot(viewModel: CalculatorViewModel) {
    val destination by viewModel.destination.collectAsStateWithLifecycle()
    val settings by viewModel.settings.collectAsStateWithLifecycle()
    val calcState by viewModel.calcState.collectAsStateWithLifecycle()
    val converterState by viewModel.converterState.collectAsStateWithLifecycle()
    val currencyState by viewModel.currencyState.collectAsStateWithLifecycle()
    val history by viewModel.history.collectAsStateWithLifecycle()
    val filteredHistory by viewModel.filteredHistory.collectAsStateWithLifecycle()
    val historySearch by viewModel.historySearch.collectAsStateWithLifecycle()
    val historyFilter by viewModel.historyFilter.collectAsStateWithLifecycle()
    val graphFunctions by viewModel.graphFunctions.collectAsStateWithLifecycle()
    val selectedFinancial by viewModel.selectedFinancialTool.collectAsStateWithLifecycle()
    val updateStatus by viewModel.updateStatus.collectAsStateWithLifecycle()
    val showUpdateDialog by viewModel.showUpdateDialog.collectAsStateWithLifecycle()
    val context = LocalContext.current

    BackHandler(enabled = destination != AppDestination.CALCULATOR) {
        viewModel.navigateBack()
    }

    LaunchedEffect(calcState.toastMessage) {
        if (calcState.toastMessage != null) {
            delay(1800)
            viewModel.clearToast()
        }
    }

    LiquidGlassBackground {
        AnimatedContent(
            targetState = destination,
            transitionSpec = { fadeIn(tween(140)) togetherWith fadeOut(tween(110)) },
            label = "rootNav"
        ) { dest ->
            when (dest) {
                AppDestination.HOME -> HomeScreen(
                    recentHistory = history,
                    onSelectMode = { mode -> viewModel.selectMode(mode, true) },
                    onNavigate = viewModel::navigateTo,
                    onOpenFinancial = viewModel::openFinancialTool,
                    onUseHistoryItem = { item -> viewModel.useHistoryItem(item, editMode = false) }
                )

                AppDestination.CALCULATOR -> CalculatorScreen(
                    viewModel = viewModel,
                    settings = settings,
                    calcState = calcState,
                    converterContent = {
                        ConverterContent(
                            viewModel = viewModel,
                            settings = settings,
                            state = converterState,
                            currencyState = currencyState
                        )
                    }
                )

                AppDestination.GRAPH -> GraphScreen(
                    viewModel = viewModel,
                    functions = graphFunctions,
                    onBack = { viewModel.navigateBack() }
                )

                AppDestination.FUNCTIONS -> FunctionsScreen(
                    onInsertFunction = { token ->
                        viewModel.selectMode(com.liquidglass.calculator.data.CalculatorMode.SCIENTIFIC)
                        viewModel.insertFunction(token)
                    },
                    onBack = { viewModel.navigateBack() }
                )

                AppDestination.FINANCIAL -> FinancialScreen(
                    selectedTool = selectedFinancial,
                    viewModel = viewModel,
                    onSelectTool = viewModel::openFinancialTool,
                    onBack = { viewModel.navigateBack() }
                )

                AppDestination.HISTORY -> HistoryScreen(
                    viewModel = viewModel,
                    items = filteredHistory,
                    searchQuery = historySearch,
                    activeFilter = historyFilter,
                    onBack = { viewModel.navigateBack() }
                )

                AppDestination.SETTINGS -> SettingsMainScreen(
                    settings = settings,
                    updateStatus = updateStatus,
                    onNavigate = viewModel::navigateTo,
                    onBack = { viewModel.navigateBack() }
                )

                AppDestination.SETTINGS_APPEARANCE -> AppearanceSettingsScreen(
                    settings = settings,
                    viewModel = viewModel,
                    onBack = { viewModel.navigateBack() }
                )

                AppDestination.SETTINGS_HAPTICS -> HapticsSoundSettingsScreen(
                    settings = settings,
                    viewModel = viewModel,
                    onBack = { viewModel.navigateBack() }
                )

                AppDestination.SETTINGS_CALCULATION -> CalculationSettingsScreen(
                    settings = settings,
                    viewModel = viewModel,
                    onBack = { viewModel.navigateBack() }
                )

                AppDestination.SETTINGS_MEMORY -> MemorySettingsScreen(
                    settings = settings,
                    viewModel = viewModel,
                    onBack = { viewModel.navigateBack() }
                )

                AppDestination.SETTINGS_ACCESSIBILITY -> AccessibilitySettingsScreen(
                    settings = settings,
                    viewModel = viewModel,
                    onBack = { viewModel.navigateBack() }
                )

                AppDestination.SETTINGS_ABOUT -> AboutSettingsScreen(
                    settings = settings,
                    updateStatus = updateStatus,
                    viewModel = viewModel,
                    onBack = { viewModel.navigateBack() }
                )
            }
        }

        // In-App Update Modal
        if (showUpdateDialog) {
            val p = LocalGlassPalette.current
            when (val st = updateStatus) {
                is UpdateStatus.Available,
                is UpdateStatus.Downloading,
                is UpdateStatus.ReadyToInstall -> {
                    val info = when (st) {
                        is UpdateStatus.Available -> st.info
                        is UpdateStatus.Downloading -> st.info
                        is UpdateStatus.ReadyToInstall -> st.info
                        else -> null
                    }
                    if (info != null) {
                        GlassModalCardDialog(onDismiss = viewModel::dismissUpdateDialog) {
                            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                GlassText("Update Available • v${info.versionName}", fontSizeSp = 18f, fontWeight = FontWeight.SemiBold)
                                GlassText(info.releaseNotes, color = p.textSecondary, fontSizeSp = 13.5f)
                                if (st is UpdateStatus.Downloading) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(6.dp)
                                            .clip(RoundedCornerShape(999.dp))
                                            .background(p.switchTrackOff)
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth(st.progress.coerceIn(0.05f, 1f))
                                                .height(6.dp)
                                                .clip(RoundedCornerShape(999.dp))
                                                .background(p.accent)
                                        )
                                    }
                                    GlassText("Downloading ${(st.progress * 100).toInt()}%...", color = p.textSecondary, fontSizeSp = 12.5f)
                                }
                                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                    GlassCard(
                                        modifier = Modifier.weight(1f).height(44.dp),
                                        shape = RoundedCornerShape(14.dp),
                                        onClick = viewModel::dismissUpdateDialog
                                    ) {
                                        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                            GlassText("Later", fontWeight = FontWeight.SemiBold)
                                        }
                                    }
                                    val actionLabel = when (st) {
                                        is UpdateStatus.Downloading -> "Downloading..."
                                        is UpdateStatus.ReadyToInstall -> "Install Now"
                                        else -> "Update Now"
                                    }
                                    val canTap = st !is UpdateStatus.Downloading
                                    Box(
                                        modifier = Modifier
                                            .weight(1.3f)
                                            .height(44.dp)
                                            .clip(RoundedCornerShape(14.dp))
                                            .background(if (canTap) p.accent else p.secondaryKeySurface)
                                            .clickable(enabled = canTap) {
                                                when (st) {
                                                    is UpdateStatus.Available -> viewModel.downloadAndInstallUpdate(context)
                                                    is UpdateStatus.ReadyToInstall -> viewModel.installReadyApk(context)
                                                    else -> Unit
                                                }
                                            },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        GlassText(
                                            text = actionLabel,
                                            color = if (canTap) p.onAccent else p.textSecondary,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
                else -> Unit
            }
        }

        // Subtle Glass Toast Pill
        val p = LocalGlassPalette.current
        AnimatedVisibility(
            visible = calcState.toastMessage != null,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .navigationBarsPadding()
                .padding(bottom = 78.dp)
        ) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(999.dp))
                    .background(p.modalSurface)
                    .border(0.9.dp, p.glassBorder, RoundedCornerShape(999.dp))
                    .padding(horizontal = 18.dp, vertical = 10.dp)
            ) {
                GlassText(
                    text = calcState.toastMessage ?: "",
                    fontSizeSp = 13.5f,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}
