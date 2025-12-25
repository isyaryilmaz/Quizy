package com.example.quizapp

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth

class LanguageSelectActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_language_select)

        val user = FirebaseAuth.getInstance().currentUser
        val tvWelcome = findViewById<TextView>(R.id.tvWelcome)
        val btnLogout = findViewById<Button>(R.id.btnLogout)
        val btnGoToMistakes = findViewById<Button>(R.id.btnGoToMistakes)
        val btnGoToLeaderboard = findViewById<Button>(R.id.btnGoToLeaderboard)

        val nameToDisplay = when {
            !user?.displayName.isNullOrEmpty() -> user?.displayName
            !user?.email.isNullOrEmpty() -> user?.email?.substringBefore("@")
            else -> "Gezgin"
        }
        tvWelcome.text = "Hoş geldin, $nameToDisplay!"

        val cardEnglish = findViewById<LinearLayout>(R.id.cardEnglish)
        val cardGerman = findViewById<LinearLayout>(R.id.cardGerman)
        val cardFrench = findViewById<LinearLayout>(R.id.cardFrench)

        cardEnglish.setOnClickListener { goToLevel("EN") }
        cardGerman.setOnClickListener { goToLevel("DE") }
        cardFrench.setOnClickListener { goToLevel("FR") }

        btnGoToMistakes.setOnClickListener {
            startActivity(Intent(this, MistakesActivity::class.java))
        }

        // --- DÜNYA SIRALAMASI BUTONU İŞLEMİ (DÜZELTİLDİ) ---
        btnGoToLeaderboard.setOnClickListener {
            val intent = Intent(this, GlobalLeaderboardActivity::class.java)
            // ARTIK EXTRA GÖNDERMİYORUZ!
            // Böylece GlobalLeaderboardActivity "Dünya Sıralaması" moduna geçecek.
            startActivity(intent)
        }

        btnLogout.setOnClickListener {
            FirebaseAuth.getInstance().signOut()
            val intent = Intent(this, AuthActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        }
    }

    private fun goToLevel(languageCode: String) {
        val intent = Intent(this, LevelSelectActivity::class.java)
        intent.putExtra("LANGUAGE", languageCode)
        startActivity(intent)
    }
}