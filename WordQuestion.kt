package com.example.quizapp

data class WordQuestion(
    val foreignWord: String,
    val correctTurkish: String,
    val wrongOptions: List<String>
)