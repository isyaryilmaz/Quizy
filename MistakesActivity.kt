package com.example.quizapp

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth

class MistakesActivity : AppCompatActivity() {

    private lateinit var adapter: GroupedMistakesAdapter
    private var mistakesList = mutableListOf<String>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_mistakes)

        val rvMistakes = findViewById<RecyclerView>(R.id.rvMistakes)
        val btnClear = findViewById<Button>(R.id.btnClearMistakes)

        // 1. GİRİŞ YAPAN KULLANICININ UID'SİNİ ALIYORUZ
        val currentUser = FirebaseAuth.getInstance().currentUser
        val uid = currentUser?.uid ?: "default" // Eğer giriş yapılmamışsa default açar (güvenlik için)

        // 2. DOSYA ADINI HESABA ÖZEL YAPIYORUZ
        // Artık "Mistakes" değil, "Mistakes_KullanıcıID" dosyası açılacak
        val sharedPrefs = getSharedPreferences("Mistakes_$uid", Context.MODE_PRIVATE)
        val mistakesSet = sharedPrefs.getStringSet("words", null)?.toMutableSet()

        mistakesList = mistakesSet?.toList()?.sorted()?.toMutableList() ?: mutableListOf()

        rvMistakes.layoutManager = LinearLayoutManager(this)
        adapter = GroupedMistakesAdapter(mistakesList)
        rvMistakes.adapter = adapter

        val itemTouchHelperCallback = object : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT) {
            override fun onMove(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder, target: RecyclerView.ViewHolder): Boolean = false

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val position = viewHolder.adapterPosition
                val removedItem = mistakesList[position]

                mistakesList.removeAt(position)
                adapter.notifyItemRemoved(position)

                // SharedPreferences'tan silerken doğru dosyadan sildiğine emin oluyoruz
                val currentSet = sharedPrefs.getStringSet("words", mutableSetOf())?.toMutableSet() ?: mutableSetOf()
                currentSet.remove(removedItem)
                sharedPrefs.edit().putStringSet("words", currentSet).apply()

                Toast.makeText(this@MistakesActivity, "Kelime silindi", Toast.LENGTH_SHORT).show()
            }
        }

        ItemTouchHelper(itemTouchHelperCallback).attachToRecyclerView(rvMistakes)

        btnClear.setOnClickListener {
            sharedPrefs.edit().clear().apply()
            mistakesList.clear()
            adapter.notifyDataSetChanged()
            Toast.makeText(this, "Tüm liste temizlendi", Toast.LENGTH_SHORT).show()
        }
    }
}

// Adapter kısmında değişiklik yapmana gerek yok, aynen kalabilir.
class GroupedMistakesAdapter(private val items: MutableList<String>) :
    RecyclerView.Adapter<GroupedMistakesAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val textView: TextView = view.findViewById(android.R.id.text1)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(android.R.layout.simple_list_item_1, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val rawText = items[position]
        val spannable = SpannableString(rawText)
        val bracketEnd = rawText.indexOf("]") + 1

        if (bracketEnd > 0) {
            spannable.setSpan(ForegroundColorSpan(Color.parseColor("#DC2626")), 0, bracketEnd, 0)
        }
        holder.textView.text = spannable

        holder.itemView.setOnClickListener {
            try {
                val parts = rawText.substring(1, rawText.indexOf("]")).split(" - ")
                val intent = Intent(holder.itemView.context, QuizActivity::class.java).apply {
                    putExtra("LANGUAGE", parts[0])
                    putExtra("LEVEL", parts[1])
                    putExtra("IS_MISTAKE_TEST", true)
                }
                holder.itemView.context.startActivity(intent)
            } catch (e: Exception) {
                Toast.makeText(holder.itemView.context, "Hata!", Toast.LENGTH_SHORT).show()
            }
        }
    }
    override fun getItemCount() = items.size
}