package com.arrazyfathan.kbbi.presentation.splash

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.animation.LinearInterpolator
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.arrazyfathan.kbbi.R
import com.arrazyfathan.kbbi.databinding.ActivitySplashScreenBinding
import com.arrazyfathan.kbbi.presentation.home.MainActivity
import com.arrazyfathan.kbbi.utils.applySystemBarPadding
import com.arrazyfathan.kbbi.utils.enableEdgeToEdgeSystemBars
import com.arrazyfathan.kbbi.utils.updateSystemBarStyle
import com.arrazyfathan.kbbi.utils.viewBinding

@SuppressLint("CustomSplashScreen")
class SplashScreenActivity : AppCompatActivity() {

    private val binding by viewBinding(ActivitySplashScreenBinding::inflate)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdgeSystemBars()
        setContentView(binding.root)
        binding.root.applySystemBarPadding(applyTop = true, applyBottom = true)
        updateSystemBarStyle(ContextCompat.getColor(this, R.color.blue_primary))

        animateLogo()
        beginSplash()

        binding.version.text = "Version ${getString(R.string.version_name)}"
    }

    private fun animateLogo() {
        binding.readingAnimation.animate().apply {
            duration = 2000
            translationY(-80f)
            alpha(1f)
            interpolator = LinearInterpolator()
        }.start()

        binding.logo.animate().apply {
            duration = 2000
            translationY(100f)
            interpolator = LinearInterpolator()
        }.start()

        binding.loadingAnimation.animate().apply {
            duration = 2000
            translationY(100f)
            interpolator = LinearInterpolator()
        }.start()
    }

    private fun beginSplash() {
        Handler(Looper.getMainLooper()).postDelayed({
            Intent(this, MainActivity::class.java).also {
                startActivity(it)
            }
            finish()
        }, 3000)
    }
}
