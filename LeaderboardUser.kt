package com.example.quizapp

// Firebase'den veri çekmek için boş bir constructor (varsayılan değerler) şarttır!
data class LeaderboardUser(
    val name: String = "",
    val score: Int = 0,
    val language: String = "",
    val level: String = "",
    val lang_lvl: String = ""
)