package com.example.quizapp

import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

class ResultActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_result)

        // 1. Verileri Intent'ten alıyoruz
        val score = intent.getIntExtra("SCORE", 0)
        val correct = intent.getIntExtra("CORRECT", 0)
        val wrong = intent.getIntExtra("WRONG", 0)
        val level = intent.getStringExtra("LEVEL") ?: "A1"
        val language = intent.getStringExtra("LANGUAGE") ?: "EN"

        // 2. View'ları Tanımlıyoruz
        val tvScore = findViewById<TextView>(R.id.tvScore)
        val tvDetail = findViewById<TextView>(R.id.tvDetail)
        val tvHighScore = findViewById<TextView>(R.id.tvHighScore)
        val btnGlobal = findViewById<MaterialButton>(R.id.btnGlobalLeaderboard)
        val btnRestart = findViewById<MaterialButton>(R.id.btnRestart)
        val btnExit = findViewById<MaterialButton>(R.id.btnExit)

        // 3. Mevcut Test Sonuçlarını Ekrana Yazıyoruz
        tvScore.text = score.toString()
        tvDetail.text = "Doğru: $correct  •  Yanlış: $wrong"

        // 4. Firebase İşlemleri
        val user = FirebaseAuth.getInstance().currentUser
        if (user != null) {
            val realName = user.displayName ?: "Bilinmeyen Kullanıcı"

            // Skoru ve dil/seviye bilgisini veritabanına yüklüyoruz
            uploadScoreWithLimit(user.uid, realName, score, language, level)

            // Toplam puanı anlık olarak çekip gösteriyoruz
            FirebaseDatabase.getInstance().reference.child("leaderboard").child(user.uid)
                .get().addOnSuccessListener { snapshot ->
                    val total = snapshot.child("score").getValue(Int::class.java) ?: 0
                    tvHighScore.text = "🏆 Toplam Puanın: $total"
                }
        }

        // 5. BUTON: Kategoriye Özel Liderlik Tablosu (Önemli Kısım)
        btnGlobal.setOnClickListener {
            val intent = Intent(this, GlobalLeaderboardActivity::class.java)
            // GlobalLeaderboardActivity'ye hangi dili filtreleyeceğini söylüyoruz
            intent.putExtra("FILTER_LANG", language)
            intent.putExtra("FILTER_LEVEL", level)
            startActivity(intent)
        }

        // 6. BUTON: Tekrar Başla
        btnRestart.setOnClickListener {
            val intent = Intent(this, LanguageSelectActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
            startActivity(intent)
            finish()
        }

        // 7. BUTON: Çıkış Yap
        btnExit.setOnClickListener {
            FirebaseAuth.getInstance().signOut()
            val intent = Intent(this, AuthActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        }
    }

    /**
     * Kullanıcının puanını Firebase'e güncelleyerek kaydeder.
     */
// ... (onCreate içindeki kısımlar aynı kalabilir, sadece upload fonksiyonunu değiştiriyoruz)

    private fun uploadScoreWithLimit(uid: String, name: String, newScore: Int, lang: String, lvl: String) {
        // categoryKey artık uid_EN_A1 gibi standart bir yapıda
        val categoryKey = "${uid}_${lang}_${lvl}"
        val dbRef = FirebaseDatabase.getInstance().reference.child("leaderboard").child(categoryKey)

        dbRef.get().addOnSuccessListener { snapshot ->
            val currentTotal = snapshot.child("score").getValue(Int::class.java) ?: 0
            val userData = mapOf(
                "name" to name,
                "score" to currentTotal + newScore,
                "language" to lang,
                "level" to lvl,
                "lang_lvl" to "${lang}_${lvl}"
            )
            dbRef.setValue(userData)
        }
    }
}