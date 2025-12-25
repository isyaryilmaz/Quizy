package com.example.quizapp

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

class ScoreHistoryActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_score_history)

        val recyclerView = findViewById<RecyclerView>(R.id.recyclerScores)
        recyclerView.layoutManager = LinearLayoutManager(this)

        val scoreList = mutableListOf<ScoreItem>()
        val adapter = ScoreAdapter(scoreList)
        recyclerView.adapter = adapter

        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return

        FirebaseDatabase.getInstance()
            .reference
            .child("scores")
            .child(userId)
            .get()
            .addOnSuccessListener { snapshot ->
                scoreList.clear()

                for (child in snapshot.children) {
                    val scoreItem = ScoreItem(
                        score = child.child("score").getValue(Int::class.java) ?: 0,
                        correct = child.child("correct").getValue(Int::class.java) ?: 0,
                        wrong = child.child("wrong").getValue(Int::class.java) ?: 0,
                        language = child.child("language").getValue(String::class.java) ?: "",
                        level = child.child("level").getValue(String::class.java) ?: "",
                        timestamp = child.child("timestamp").getValue(Long::class.java) ?: 0
                    )
                    scoreList.add(scoreItem)
                }

                // Skora göre sırala
                scoreList.sortByDescending { it.score }

                // Derecelendirme atamaları
                scoreList.forEachIndexed { index, item ->
                    item.isTopScore = (index == 0 && item.score > 0)
                    item.rank = index + 1
                }

                adapter.notifyDataSetChanged()
            }
    }
}