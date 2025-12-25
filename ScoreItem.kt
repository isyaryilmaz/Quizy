package com.example.quizapp

data class ScoreItem(
    val score: Int,
    val correct: Int,
    val wrong: Int,
    val language: String,
    val level: String,
    val timestamp: Long,
    var isTopScore: Boolean = false,
    var rank: Int = 0
)