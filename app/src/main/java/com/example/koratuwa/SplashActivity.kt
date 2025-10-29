package com.example.koratuwa

import android.content.Intent
import android.os.Bundle
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch


import android.os.Build
import android.view.WindowInsets
import android.view.View

class SplashActivity : BaseActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        lifecycleScope.launch {
            delay(2000)
            
            // Check if language has been selected before
            val savedLanguage = LocaleHelper.getLanguage(this@SplashActivity)
            
            if (savedLanguage == "en" && !hasLanguageBeenSelected()) {
                // First time user - show language selection
                startActivity(Intent(this@SplashActivity, LanguageSelectionActivity::class.java))
            } else {
                // User has selected language before - go to main activity
                startActivity(Intent(this@SplashActivity, MainActivity::class.java))
            }
            finish()
        }


            // Immersive/fullscreen mode - MOVE THE WHOLE APP UP
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                window.setDecorFitsSystemWindows(false)
                window.insetsController?.hide(
                    WindowInsets.Type.statusBars() or WindowInsets.Type.navigationBars()
                )
            } else {
                window.decorView.systemUiVisibility = (
                        View.SYSTEM_UI_FLAG_LAYOUT_STABLE or
                                View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or
                                View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION or
                                View.SYSTEM_UI_FLAG_FULLSCREEN or
                                View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or
                                View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                        )
            }



    }
    
    private fun hasLanguageBeenSelected(): Boolean {
        val prefs = getSharedPreferences("app_prefs", MODE_PRIVATE)
        return prefs.getBoolean("language_selected", false)
    }


}
