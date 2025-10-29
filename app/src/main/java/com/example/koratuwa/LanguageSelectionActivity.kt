package com.example.koratuwa

import android.content.Intent
import android.os.Build
import android.view.WindowInsets
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.RadioButton
import androidx.cardview.widget.CardView

class LanguageSelectionActivity : BaseActivity() {

    private lateinit var cardSinhala: CardView
    private lateinit var cardTamil: CardView
    private lateinit var cardEnglish: CardView

    private lateinit var rbSinhala: RadioButton
    private lateinit var rbTamil: RadioButton
    private lateinit var rbEnglish: RadioButton

    private lateinit var btnNext: Button
    private lateinit var back_arrow: ImageView  // Back button

    private fun selectLanguage(selectedRadio: RadioButton) {
        rbSinhala.isChecked = selectedRadio == rbSinhala
        rbTamil.isChecked = selectedRadio == rbTamil
        rbEnglish.isChecked = selectedRadio == rbEnglish
    }





    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_language_selection)

        cardSinhala = findViewById(R.id.card_sinhala)
        cardTamil = findViewById(R.id.card_tamil)
        cardEnglish = findViewById(R.id.card_english)

        rbSinhala = findViewById(R.id.rb_sinhala)
        rbTamil = findViewById(R.id.rb_tamil)
        rbEnglish = findViewById(R.id.rb_english)

        btnNext = findViewById(R.id.btnNext)

        back_arrow = findViewById(R.id.back_arrow)  // Initialize back ImageView
        back_arrow.setOnClickListener {
            // Navigate back to SettingsActivity
            val backIntent = Intent(this, SettingsActivity::class.java)
            backIntent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            startActivity(backIntent)
            finish()
        }
        back_arrow.visibility = View.GONE

        val isChangeLanguage = intent.getBooleanExtra("change_button_text", false)

        if (isChangeLanguage) {
            btnNext.text = getString(R.string.done)
            resetLanguageChangeFlag()
        } else {
            btnNext.text = getString(R.string.next)
        }

        when (getCurrentLanguage()) {
            "si" -> selectLanguage(rbSinhala)
            "ta" -> selectLanguage(rbTamil)
            else -> selectLanguage(rbEnglish)
        }

        cardSinhala.setOnClickListener { selectLanguage(rbSinhala) }
        cardTamil.setOnClickListener { selectLanguage(rbTamil) }
        cardEnglish.setOnClickListener { selectLanguage(rbEnglish) }

        rbSinhala.setOnClickListener { selectLanguage(rbSinhala) }
        rbTamil.setOnClickListener { selectLanguage(rbTamil) }
        rbEnglish.setOnClickListener { selectLanguage(rbEnglish) }

        btnNext.setOnClickListener {
            val newLanguage = when {
                rbSinhala.isChecked -> "si"
                rbTamil.isChecked -> "ta"
                else -> "en"
            }

            saveLanguage(newLanguage)
            LocaleHelper.setLocale(this, newLanguage)

            val prefs = getSharedPreferences("app_prefs", MODE_PRIVATE)
            prefs.edit().putBoolean("language_selected", true).apply()

            if (!isChangeLanguage) {
                // First-time flow: navigate to LoginActivity
                val intent = Intent(this, LoginActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
                finish()
            } else {
                // Language changed from settings: restart app and go to MainActivity
                val intent = Intent(this, MainActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
                finish()
            }
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


}
