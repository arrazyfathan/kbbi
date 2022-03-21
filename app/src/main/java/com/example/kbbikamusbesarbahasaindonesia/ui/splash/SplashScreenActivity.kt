package com.example.kbbikamusbesarbahasaindonesia.ui.splash

import android.annotation.SuppressLint
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.WindowManager
import android.view.animation.LinearInterpolator
import com.example.kbbikamusbesarbahasaindonesia.databinding.ActivitySplashScreenBinding
import com.example.kbbikamusbesarbahasaindonesia.ui.home.MainActivity

@SuppressLint("CustomSplashScreen")
@Suppress("DEPRECATION")
class SplashScreenActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySplashScreenBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySplashScreenBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        setFullScreen()
        animateLogo()
        beginSplash()

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
            WindowManager.LayoutParams.FLAG_FULLSCREEN
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