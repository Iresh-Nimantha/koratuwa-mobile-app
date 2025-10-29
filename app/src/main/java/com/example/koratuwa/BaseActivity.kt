package com.example.koratuwa

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.res.Configuration
import androidx.appcompat.app.AppCompatActivity
import java.util.Locale

open class BaseActivity : AppCompatActivity() {

    private val prefs: SharedPreferences by lazy {
        getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
    }

    protected fun saveLanguage(language: String) {
        prefs.edit().putString("selected_language", language).apply()
    }

    protected fun resetLanguageChangeFlag() {
        prefs.edit().putBoolean("language_changed_flag", false).apply()
    }

    override fun attachBaseContext(newBase: Context) {
        val language = LocaleHelper.getLanguage(newBase)
        val localContext = LocaleHelper.setLocale(newBase, language)
        super.attachBaseContext(localContext)
    }

    override fun applyOverrideConfiguration(overrideConfiguration: Configuration?) {
        if (overrideConfiguration != null) {
            val language = LocaleHelper.getLanguage(this)
            val locale = Locale(language)
            Locale.setDefault(locale)
            overrideConfiguration.setLocale(locale)
        }
        super.applyOverrideConfiguration(overrideConfiguration)
    }

    protected fun restartActivity() {
        val intent = Intent(this, this::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
        startActivity(intent)
        finish()
    }

    /**
     * Utility function to setup button text based on the stored language change state.
     * Call this from child activities passing the button to update.
     */
    protected fun updateNextButtonText(btnNext: android.widget.Button) {
        val languageChanged = prefs.getBoolean("language_changed_flag", false)
        if (languageChanged) {
            btnNext.text = "Done"  // Or load string resource if preferred
        } else {
            btnNext.text = getString(R.string.next)
        }
    }

    /**
     * Call this when language has been changed to set the flag and restart activity.
     */
    protected fun onLanguageChanged() {
        prefs.edit().putBoolean("language_changed_flag", true).apply()
        restartActivity()
    }

    /**
     * Get the current language setting
     */
    protected fun getCurrentLanguage(): String {
        return LocaleHelper.getLanguage(this)
    }
}
