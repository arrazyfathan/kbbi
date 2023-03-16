package com.example.kbbikamusbesarbahasaindonesia.ui.splash

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.WindowManager
import android.view.animation.LinearInterpolator
import androidx.appcompat.app.AppCompatActivity
import com.example.kbbikamusbesarbahasaindonesia.R
import com.example.kbbikamusbesarbahasaindonesia.databinding.ActivitySplashScreenBinding
import com.example.kbbikamusbesarbahasaindonesia.ui.home.MainActivity
import com.example.kbbikamusbesarbahasaindonesia.utils.viewBinding

@SuppressLint("CustomSplashScreen")
@Suppress("DEPRECATION")
class SplashScreenActivity : AppCompatActivity() {

    private val binding by viewBinding(ActivitySplashScreenBinding::inflate)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        // setFullScreen()
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

    private fun setFullScreen() {
        window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
        )
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
