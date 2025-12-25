package com.example.quizapp

import android.content.Context
import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.os.CountDownTimer
import android.os.Handler
import android.os.Looper
import android.speech.tts.TextToSpeech
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.quizapp.data.EnglishWords
import com.example.quizapp.data.GermanWords
import com.example.quizapp.data.FrenchWords
import com.google.android.material.button.MaterialButton
import com.google.firebase.auth.FirebaseAuth
import java.util.*

class QuizActivity : AppCompatActivity() {

    private lateinit var tvQuestion: TextView
    private lateinit var tvTimer: TextView
    private lateinit var tvProgress: TextView
    private lateinit var tvLives: TextView
    private lateinit var tvScore: TextView
    private lateinit var btnSpeak: ImageButton
    private lateinit var optionButtons: List<MaterialButton>

    private lateinit var questions: List<WordQuestion>
    private var currentIndex = 0
    private var correctCount = 0
    private var wrongCount = 0
    private var lives = 5
    private var score = 0
    private var correctPoint = 10
    private var wrongPoint = 5

    private var timer: CountDownTimer? = null
    private val questionTime = 30_000L
    private lateinit var tts: TextToSpeech
    private var currentLanguage = "EN"
    private var level = "A1"

    // 1. ADIM: UID'Yİ GLOBALDE TANIMLAYALIM
    private val currentUserUid: String
        get() = FirebaseAuth.getInstance().currentUser?.uid ?: "default"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_quiz)

        currentLanguage = intent.getStringExtra("LANGUAGE") ?: "EN"
        level = intent.getStringExtra("LEVEL") ?: "A1"
        val isMistakeTest = intent.getBooleanExtra("IS_MISTAKE_TEST", false)

        initViews()
        setupTTS()
        setScorePointsByLevel(level)

        if (isMistakeTest) {
            questions = getMistakeQuestions(currentLanguage, level)
            if (questions.isEmpty()) {
                Toast.makeText(this, "Bu grupta kayıtlı yanlışın yok!", Toast.LENGTH_SHORT).show()
                finish()
                return
            }
        } else {
            questions = when (currentLanguage) {
                "EN" -> EnglishWords.getWords(level)
                "DE" -> GermanWords.getWords(level)
                "FR" -> FrenchWords.getWords(level)
                else -> emptyList()
            }
        }

        updateLivesUI()
        updateScoreUI()
        loadQuestion()
    }

    // 2. ADIM: HATALARI OKURKEN KULLANICIYA ÖZEL DOSYADAN OKU
    private fun getMistakeQuestions(lang: String, lvl: String): List<WordQuestion> {
        // SharedPreferences ismine UID ekledik: "Mistakes_$currentUserUid"
        val sharedPrefs = getSharedPreferences("Mistakes_$currentUserUid", Context.MODE_PRIVATE)
        val mistakesSet = sharedPrefs.getStringSet("words", emptySet()) ?: emptySet()
        val filtered = mutableListOf<WordQuestion>()
        val prefix = "[$lang - $lvl]"

        val allMeanings = mistakesSet.map { it.substringAfter(" - ") }

        mistakesSet.forEach { entry ->
            if (entry.startsWith(prefix)) {
                val parts = entry.substringAfter("] ").split(" - ")
                if (parts.size == 2) {
                    val foreignWord = parts[0]
                    val correctTurkish = parts[1]

                    val wrongOptions = allMeanings
                        .filter { it != correctTurkish }
                        .shuffled()
                        .take(3)

                    filtered.add(WordQuestion(foreignWord, correctTurkish, wrongOptions))
                }
            }
        }
        return filtered.shuffled()
    }

    // 3. ADIM: HATALARI KAYDEDERKEN KULLANICIYA ÖZEL DOSYAYA YAZ
    private fun saveMistake(word: String, meaning: String) {
        // SharedPreferences ismine UID ekledik: "Mistakes_$currentUserUid"
        val sharedPrefs = getSharedPreferences("Mistakes_$currentUserUid", Context.MODE_PRIVATE)
        val mistakes = sharedPrefs.getStringSet("words", mutableSetOf())?.toMutableSet() ?: mutableSetOf()
        mistakes.add("[$currentLanguage - $level] $word - $meaning")
        sharedPrefs.edit().putStringSet("words", mistakes).apply()
    }

    // --- GERİ KALAN KODLAR DEĞİŞMEDİ ---

    private fun initViews() {
        tvQuestion = findViewById(R.id.tvQuestion)
        tvTimer = findViewById(R.id.tvTimer)
        tvProgress = findViewById(R.id.tvProgress)
        tvLives = findViewById(R.id.tvLives)
        tvScore = findViewById(R.id.tvScore)
        btnSpeak = findViewById(R.id.btnSpeak)
        optionButtons = listOf(
            findViewById(R.id.btnOpt1), findViewById(R.id.btnOpt2),
            findViewById(R.id.btnOpt3), findViewById(R.id.btnOpt4)
        )
        btnSpeak.setOnClickListener { speakOut(tvQuestion.text.toString()) }
    }

    private fun setupTTS() {
        tts = TextToSpeech(this) { status ->
            if (status != TextToSpeech.ERROR) {
                tts.language = when (currentLanguage) {
                    "DE" -> Locale.GERMAN
                    "FR" -> Locale.FRENCH
                    else -> Locale.ENGLISH
                }
            }
        }
    }

    private fun loadQuestion() {
        if (currentIndex >= questions.size || lives <= 0) {
            finishQuiz()
            return
        }
        resetButtonStyles()
        val q = questions[currentIndex]
        tvQuestion.text = q.foreignWord
        tvProgress.text = "Soru: ${currentIndex + 1} / ${questions.size}"
        Handler(Looper.getMainLooper()).postDelayed({ speakOut(q.foreignWord) }, 500)
        startTimer()

        val options = generateOptions(q.correctTurkish, questions)
        optionButtons.forEachIndexed { i, btn ->
            btn.text = options[i]
            btn.isEnabled = true
            btn.setOnClickListener { checkAnswer(btn, options[i], q.correctTurkish) }
        }
    }

    private fun checkAnswer(btn: MaterialButton, sel: String, cor: String) {
        timer?.cancel()
        disableButtons()
        if (sel == cor) {
            correctCount++; score += correctPoint
            btn.backgroundTintList = ColorStateList.valueOf(Color.parseColor("#4CAF50"))
        } else {
            wrongCount++; lives--; score -= wrongPoint
            if (score < 0) score = 0
            saveMistake(questions[currentIndex].foreignWord, cor)
            btn.backgroundTintList = ColorStateList.valueOf(Color.parseColor("#E53935"))
            highlightCorrectAnswer(cor)
            updateLivesUI()
        }
        updateScoreUI()
        Handler(Looper.getMainLooper()).postDelayed({ currentIndex++; loadQuestion() }, 1200)
    }

    private fun startTimer() {
        timer?.cancel()
        timer = object : CountDownTimer(questionTime, 1000) {
            override fun onTick(ms: Long) { tvTimer.text = "${ms / 1000}s" }
            override fun onFinish() {
                lives--; updateLivesUI()
                highlightCorrectAnswer(questions[currentIndex].correctTurkish)
                Handler(Looper.getMainLooper()).postDelayed({ currentIndex++; loadQuestion() }, 1200)
            }
        }.start()
    }

    private fun setScorePointsByLevel(l: String) {
        when (l) { "A1" -> {correctPoint=10; wrongPoint=5}; "B1" -> {correctPoint=15; wrongPoint=7}; "C1" -> {correctPoint=20; wrongPoint=10} }
    }
    private fun highlightCorrectAnswer(c: String) { optionButtons.forEach { if(it.text == c) it.backgroundTintList = ColorStateList.valueOf(Color.parseColor("#4CAF50")) } }
    private fun resetButtonStyles() { optionButtons.forEach { it.backgroundTintList = ColorStateList.valueOf(Color.WHITE); it.setTextColor(Color.parseColor("#374151")) } }
    private fun updateLivesUI() { tvLives.text = "❤️".repeat(if(lives > 0) lives else 0) }
    private fun updateScoreUI() { tvScore.text = "Skor: $score" }
    private fun disableButtons() { optionButtons.forEach { it.isEnabled = false } }
    private fun generateOptions(cor: String, all: List<WordQuestion>): List<String> {
        val wrong = all.map { it.correctTurkish }.filter { it != cor }.distinct().shuffled().take(3)
        return (wrong + cor).shuffled()
    }
    private fun speakOut(t: String) { if (::tts.isInitialized) tts.speak(t, TextToSpeech.QUEUE_FLUSH, null, null) }
    private fun finishQuiz() {
        timer?.cancel()
        val intent = Intent(this, ResultActivity::class.java).apply {
            putExtra("CORRECT", correctCount); putExtra("WRONG", wrongCount)
            putExtra("SCORE", score); putExtra("LEVEL", level); putExtra("LANGUAGE", currentLanguage)
        }
        startActivity(intent); finish()
    }
    override fun onDestroy() { if(::tts.isInitialized) {tts.stop(); tts.shutdown()}; timer?.cancel(); super.onDestroy() }
}