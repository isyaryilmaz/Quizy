package com.example.quizapp

import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import java.text.SimpleDateFormat
import java.util.*

class ScoreAdapter(private val scores: List<ScoreItem>) :
    RecyclerView.Adapter<ScoreAdapter.ScoreViewHolder>() {

    class ScoreViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvItemScore: TextView = view.findViewById(R.id.tvItemScore)
        val tvItemDetail: TextView = view.findViewById(R.id.tvItemDetail)
        val tvItemDate: TextView = view.findViewById(R.id.tvItemDate)
        val cardItem: LinearLayout = view.findViewById(R.id.cardScoreItem) // LinearLayout olarak değiştirdik
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ScoreViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_score, parent, false)
        return ScoreViewHolder(view)
    }

    override fun onBindViewHolder(holder: ScoreViewHolder, position: Int) {
        val item = scores[position]

        val medal = when (item.rank) {
            1 -> "🥇 "
            2 -> "🥈 "
            3 -> "🥉 "
            else -> ""
        }

        holder.tvItemScore.text = "${medal}Skor: ${item.score}"
        holder.tvItemDetail.text = "${item.language} · ${item.level} | D: ${item.correct} Y: ${item.wrong}"

        val sdf = SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault())
        holder.tvItemDate.text = sdf.format(Date(item.timestamp))

        // 🌟 En yüksek skor vurgusu (Kod üzerinden çerçeve ve arka plan yönetimi)
        val shape = GradientDrawable()
        shape.cornerRadius = 12f // Hafif yuvarlatılmış köşeler

        if (item.isTopScore) {
            shape.setColor(Color.parseColor("#E8F5E9")) // Hafif yeşil arka plan
            shape.setStroke(3, Color.parseColor("#4CAF50")) // Yeşil çerçeve
        } else {
            shape.setColor(Color.WHITE)
            shape.setStroke(1, Color.parseColor("#DDDDDD")) // Standart gri çerçeve
        }

        holder.cardItem.background = shape
    }

    override fun getItemCount(): Int = scores.size
}