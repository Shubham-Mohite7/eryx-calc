package com.liquidglass.calculator

import android.content.Intent
import android.graphics.drawable.Animatable
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.style.ForegroundColorSpan
import android.view.View
import android.view.animation.DecelerateInterpolator
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.ComponentActivity
import androidx.core.content.ContextCompat
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.core.view.WindowCompat

class SplashActivity : ComponentActivity() {

    private val handler = Handler(Looper.getMainLooper())
    private var navigated = false

    private val navigateRunnable = Runnable { goToMain() }

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)

        WindowCompat.getInsetsController(window, window.decorView).apply {
            isAppearanceLightStatusBars = false
            isAppearanceLightNavigationBars = false
        }

        setContentView(R.layout.activity_splash)

        val logo = findViewById<ImageView>(R.id.logo)
        val wordmark = findViewById<TextView>(R.id.wordmark)
        val tagline = findViewById<TextView>(R.id.tagline)
        val footer = findViewById<View>(R.id.footer)
        val progress = findViewById<View>(R.id.progress)
        val version = findViewById<TextView>(R.id.version)

        // Two-tone wordmark: ERYX (ink) + CALC (ink_soft)
        val partA = getString(R.string.splash_wordmark_a)
        val partB = getString(R.string.splash_wordmark_b)
        val spannable = SpannableStringBuilder(partA + partB).apply {
            setSpan(
                ForegroundColorSpan(ContextCompat.getColor(this@SplashActivity, R.color.eryx_ink)),
                0,
                partA.length,
                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
            )
            setSpan(
                ForegroundColorSpan(ContextCompat.getColor(this@SplashActivity, R.color.eryx_ink_soft)),
                partA.length,
                partA.length + partB.length,
                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
            )
        }
        wordmark.text = spannable
        version.text = getString(R.string.splash_version, BuildConfig.VERSION_NAME)

        val animScale = runCatching {
            Settings.Global.getFloat(contentResolver, Settings.Global.ANIMATOR_DURATION_SCALE, 1f)
        }.getOrDefault(1f)

        if (animScale == 0f) {
            // Animations disabled on device: show finished logo cleanly and advance quickly
            logo.setImageResource(R.drawable.ic_launcher_foreground)
            wordmark.alpha = 1f
            tagline.alpha = 1f
            footer.alpha = 1f
            progress.scaleX = 1f
            handler.postDelayed(navigateRunnable, 600L)
            return
        }

        // Initial hidden states for choreographed reveal
        val dp = resources.displayMetrics.density
        wordmark.alpha = 0f
        wordmark.translationY = 14f * dp
        tagline.alpha = 0f
        tagline.translationY = 10f * dp
        footer.alpha = 0f
        progress.pivotX = 0f
        progress.scaleX = 0f

        // Start AnimatedVectorDrawable build-in
        (logo.drawable as? Animatable)?.start()

        val interp = DecelerateInterpolator(1.6f)

        // Subtle lift on logo as keys pop in
        logo.animate()
            .translationY(-4f * dp)
            .setStartDelay(1600L)
            .setDuration(800L)
            .setInterpolator(interp)
            .start()

        // Wordmark reveal
        wordmark.animate()
            .alpha(1f)
            .translationY(0f)
            .setStartDelay(1650L)
            .setDuration(600L)
            .setInterpolator(interp)
            .start()

        // Tagline reveal
        tagline.animate()
            .alpha(1f)
            .translationY(0f)
            .setStartDelay(1950L)
            .setDuration(550L)
            .setInterpolator(interp)
            .start()

        // Footer & progress line sweep
        footer.animate()
            .alpha(1f)
            .setStartDelay(1800L)
            .setDuration(450L)
            .setInterpolator(interp)
            .start()

        progress.animate()
            .scaleX(1f)
            .setStartDelay(1850L)
            .setDuration(1750L)
            .setInterpolator(interp)
            .start()

        handler.postDelayed(navigateRunnable, 3800L)
    }

    private fun goToMain() {
        if (navigated || isFinishing) return
        navigated = true
        startActivity(Intent(this, MainActivity::class.java))
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            overrideActivityTransition(OVERRIDE_TRANSITION_OPEN, android.R.anim.fade_in, android.R.anim.fade_out)
        } else {
            @Suppress("DEPRECATION")
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
        }
        finish()
    }

    override fun onDestroy() {
        handler.removeCallbacks(navigateRunnable)
        super.onDestroy()
    }
}
