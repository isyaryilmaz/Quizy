package com.example.quizapp

import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen // Kütüphaneyi ekledik
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest

class AuthActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        // 1. Splash Screen'i başlat (Her şeyden önce burası gelmeli)
        val splashScreen = installSplashScreen()

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_auth)

        auth = FirebaseAuth.getInstance()

        // Kullanıcı zaten giriş yapmışsa direkt ana ekrana yönlendir
        if (auth.currentUser != null) {
            startActivity(Intent(this, LanguageSelectActivity::class.java))
            finish()
            return
        }

        // Görünüm elemanlarını bağla
        val layoutLogin = findViewById<View>(R.id.layoutLogin)
        val layoutRegister = findViewById<View>(R.id.layoutRegister)
        val btnShowLogin = findViewById<Button>(R.id.btnShowLogin)
        val btnShowRegister = findViewById<Button>(R.id.btnShowRegister)

        val btnLogin = findViewById<Button>(R.id.btnLogin)
        val etLoginEmail = findViewById<EditText>(R.id.etLoginEmail)
        val etLoginPassword = findViewById<EditText>(R.id.etLoginPassword)

        val btnRegister = findViewById<Button>(R.id.btnRegister)
        val etRegisterUsername = findViewById<EditText>(R.id.etRegisterUsername)
        val etRegisterEmail = findViewById<EditText>(R.id.etRegisterEmail)
        val etRegisterPassword = findViewById<EditText>(R.id.etRegisterPassword)

        // Panel değiştirme fonksiyonları
        fun showLoginPanel() {
            layoutLogin.visibility = View.VISIBLE
            layoutRegister.visibility = View.GONE
            btnShowLogin.setTextColor(Color.parseColor("#DC2626"))
            btnShowLogin.setTypeface(null, Typeface.BOLD)
            btnShowRegister.setTextColor(Color.parseColor("#6B7280"))
            btnShowRegister.setTypeface(null, Typeface.NORMAL)
        }

        fun showRegisterPanel() {
            layoutLogin.visibility = View.GONE
            layoutRegister.visibility = View.VISIBLE
            btnShowRegister.setTextColor(Color.parseColor("#DC2626"))
            btnShowRegister.setTypeface(null, Typeface.BOLD)
            btnShowLogin.setTextColor(Color.parseColor("#6B7280"))
            btnShowLogin.setTypeface(null, Typeface.NORMAL)
        }

        btnShowLogin.setOnClickListener { showLoginPanel() }
        btnShowRegister.setOnClickListener { showRegisterPanel() }

        // Giriş Yap Butonu İşlemi
        btnLogin.setOnClickListener {
            val email = etLoginEmail.text.toString().trim()
            val pass = etLoginPassword.text.toString().trim()
            if (email.isNotEmpty() && pass.isNotEmpty()) {
                auth.signInWithEmailAndPassword(email, pass).addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        startActivity(Intent(this, LanguageSelectActivity::class.java))
                        finish()
                    } else {
                        Toast.makeText(this, "Hata: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }

        // Kayıt Ol Butonu İşlemi
        btnRegister.setOnClickListener {
            val name = etRegisterUsername.text.toString().trim()
            val email = etRegisterEmail.text.toString().trim()
            val pass = etRegisterPassword.text.toString().trim()

            if (email.isNotEmpty() && pass.isNotEmpty() && name.isNotEmpty()) {
                auth.createUserWithEmailAndPassword(email, pass).addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        val user = auth.currentUser
                        val profileUpdates = UserProfileChangeRequest.Builder()
                            .setDisplayName(name).build()

                        user?.updateProfile(profileUpdates)?.addOnCompleteListener {
                            auth.signOut()
                            Toast.makeText(this, "Kayıt Başarılı! Giriş Yapın.", Toast.LENGTH_LONG).show()
                            showLoginPanel()
                        }
                    } else {
                        Toast.makeText(this, "Hata: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                    }
                }
            } else {
                Toast.makeText(this, "Boş alan bırakmayın!", Toast.LENGTH_SHORT).show()
            }
        }
    }
}