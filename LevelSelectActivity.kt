package com.example.quizapp

import android.content.Intent
import android.os.Bundle
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class LevelSelectActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_level_select)

        val language = intent.getStringExtra("LANGUAGE")

        if (language == null) {
            finish()
            return
        }

        val tvTitle = findViewById<TextView>(R.id.tvTitle)

        // Seçilen dile göre başlığı güncelle
        tvTitle.text = when (language) {
            "EN" -> "İngilizce Seviyesi"
            "DE" -> "Almanca Seviyesi"
            "FR" -> "Fransızca Seviyesi"
            else -> "Seviye Seçimi"
        }

        // Tasarımdaki yumuşak kartlarımızı (LinearLayout) tanımlıyoruz
        val cardBeginner = findViewById<LinearLayout>(R.id.cardBeginner)
        val cardIntermediate = findViewById<LinearLayout>(R.id.cardIntermediate)
        val cardAdvanced = findViewById<LinearLayout>(R.id.cardAdvanced)

        // Tıklama olayları
        cardBeginner.setOnClickListener {
            goToQuiz(language, "A1")
        }

        cardIntermediate.setOnClickListener {
            goToQuiz(language, "B1")
        }

        cardAdvanced.setOnClickListener {
            goToQuiz(language, "C1")
        }
    }

    private fun goToQuiz(language: String, level: String) {
        val intent = Intent(this, QuizActivity::class.java)
        intent.putExtra("LANGUAGE", language)
        intent.putExtra("LEVEL", level)
        startActivity(intent)
    }
}