package com.example.quizapp

import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.database.*

class GlobalLeaderboardActivity : AppCompatActivity() {

    private lateinit var rvLeaderboard: RecyclerView
    private lateinit var progressBar: ProgressBar
    private lateinit var spinnerLang: Spinner
    private lateinit var spinnerLevel: Spinner
    private lateinit var adapter: LeaderboardAdapter
    private val dbRef = FirebaseDatabase.getInstance().reference.child("leaderboard")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_global_leaderboard)

        rvLeaderboard = findViewById(R.id.rvLeaderboard)
        progressBar = findViewById(R.id.progressBar)
        spinnerLang = findViewById(R.id.spinnerLang)
        spinnerLevel = findViewById(R.id.spinnerLevel)

        rvLeaderboard.layoutManager = LinearLayoutManager(this)
        adapter = LeaderboardAdapter(mutableListOf())
        rvLeaderboard.adapter = adapter

        // Spinner Verileri (Veritabanıyla birebir aynı: A1, B1, C1)
        val langs = arrayOf("Tümü", "EN", "DE", "FR")
        val levels = arrayOf("Tümü", "A1", "B1", "C1")

        val langAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, langs)
        val levelAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, levels)

        spinnerLang.adapter = langAdapter
        spinnerLevel.adapter = levelAdapter

        // Intent'ten gelen verileri al ve Spinner'ı konumlandır
        val intentLang = intent.getStringExtra("FILTER_LANG") ?: "Tümü"
        val intentLevel = intent.getStringExtra("FILTER_LEVEL") ?: "Tümü"

        spinnerLang.setSelection(langs.indexOf(intentLang))
        spinnerLevel.setSelection(levels.indexOf(intentLevel))

        val listener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
                updateQuery()
            }
            override fun onNothingSelected(p0: AdapterView<*>?) {}
        }

        spinnerLang.onItemSelectedListener = listener
        spinnerLevel.onItemSelectedListener = listener
    }

    private fun updateQuery() {
        val sLang = spinnerLang.selectedItem.toString()
        val sLvl = spinnerLevel.selectedItem.toString()
        progressBar.visibility = View.VISIBLE

        val query: Query = when {
            sLang != "Tümü" && sLvl != "Tümü" -> dbRef.orderByChild("lang_lvl").equalTo("${sLang}_${sLvl}")
            sLang != "Tümü" -> dbRef.orderByChild("language").equalTo(sLang)
            sLvl != "Tümü" -> dbRef.orderByChild("level").equalTo(sLvl)
            else -> dbRef.orderByChild("score")
        }

        query.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val userList = mutableListOf<LeaderboardItem>()
                for (userSnap in snapshot.children) {
                    val user = userSnap.getValue(LeaderboardItem::class.java)
                    user?.let { userList.add(it) }
                }
                adapter.submitList(userList.sortedByDescending { it.score })
                progressBar.visibility = View.GONE
            }
            override fun onCancelled(error: DatabaseError) { progressBar.visibility = View.GONE }
        })
    }
}