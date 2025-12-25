package com.example.quizapp

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class LeaderboardAdapter(private val items: MutableList<LeaderboardItem>) :
    RecyclerView.Adapter<LeaderboardAdapter.Holder>() {

    class Holder(view: View) : RecyclerView.ViewHolder(view) {
        val tvLbRank: TextView = view.findViewById(R.id.tvLbRank)
        val tvLbName: TextView = view.findViewById(R.id.tvLbName)
        val tvLbScore: TextView = view.findViewById(R.id.tvLbScore)
        val tvLbDetail: TextView = view.findViewById(R.id.tvLbDetail)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_leaderboard, parent, false)
        return Holder(view)
    }

    override fun onBindViewHolder(holder: Holder, position: Int) {
        val item = items[position]
        holder.tvLbRank.text = when(position) {
            0 -> "🥇" 1 -> "🥈" 2 -> "🥉"
            else -> (position + 1).toString()
        }
        holder.tvLbName.text = item.name
        holder.tvLbScore.text = "Skor: ${item.score}"
        holder.tvLbDetail.text = "${item.language} · ${item.level}"
    }

    override fun getItemCount() = items.size

    fun submitList(newItems: List<LeaderboardItem>) {
        items.clear()
        items.addAll(newItems)
        notifyDataSetChanged()
    }
}