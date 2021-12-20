package com.android.app.shoppy.ui

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import com.android.app.shoppy.databinding.ActivitySplashBinding

const val DURATION = 3000L
class SplashActivity : AppCompatActivity() {
    private var _binding : ActivitySplashBinding? = null
    private val binding get() = _binding!!
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = ActivitySplashBinding.inflate(layoutInflater)
        setContentView(binding.root)

        Handler().postDelayed({
            Intent(this, SelectionActivity::class.java).apply {
                startActivity(this)
                finish()
            }
        }, DURATION)
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding  = null
    }
}