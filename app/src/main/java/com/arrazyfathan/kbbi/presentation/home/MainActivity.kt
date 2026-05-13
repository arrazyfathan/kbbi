package com.arrazyfathan.kbbi.presentation.home

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.arrazyfathan.kbbi.R
import com.arrazyfathan.kbbi.databinding.ActivityMainBinding
import com.arrazyfathan.kbbi.utils.applyBottomNavigationInsets
import com.arrazyfathan.kbbi.utils.enableEdgeToEdgeSystemBars
import com.arrazyfathan.kbbi.utils.updateSystemBarStyle
import com.arrazyfathan.kbbi.utils.viewBinding

class MainActivity : AppCompatActivity() {
    private val binding by viewBinding(ActivityMainBinding::inflate)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdgeSystemBars()
        setContentView(binding.root)
        binding.bottomNavigationView.applyBottomNavigationInsets()
        updateSystemBarStyle(ContextCompat.getColor(this, R.color.blue_primary))
        setupBottomNavigationView()
    }

    private fun setupBottomNavigationView() {
        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.nav_host_fragment_container) as NavHostFragment
        val navController = navHostFragment.navController
        binding.bottomNavigationView.apply {
            setupWithNavController(navController)
            itemIconTintList = null
        }
    }
}
