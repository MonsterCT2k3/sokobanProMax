package com.example.myapplication.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.R
import com.example.myapplication.models.Level

class LevelAdapter(
    private var levels: List<Level>,
    private val onLevelClick: (Level) -> Unit
) : RecyclerView.Adapter<LevelAdapter.LevelViewHolder>() {

    class LevelViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val levelNumber: TextView = itemView.findViewById(R.id.textLevelNumber)
        val levelName: TextView = itemView.findViewById(R.id.textLevelName)
        val levelDifficulty: TextView = itemView.findViewById(R.id.textDifficulty)
        val levelRecord: TextView = itemView.findViewById(R.id.textRecord)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LevelViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_level, parent, false)
        return LevelViewHolder(view)
    }

    override fun onBindViewHolder(holder: LevelViewHolder, position: Int) {
        val level = levels[position]

        holder.levelNumber.text = level.id.toString()
        holder.levelName.text = level.name
        holder.levelDifficulty.text = level.difficulty.name

        // Record text
        if (level.bestMoves != null) {
            holder.levelRecord.text = "Tốt nhất: ${level.bestMoves} bước"
            holder.levelRecord.visibility = View.VISIBLE
        } else {
            holder.levelRecord.visibility = View.GONE
        }

        // Styling based on state
        val context = holder.itemView.context
        if (level.isUnlocked) {
            if (level.bestMoves != null) {
                // Completed
                holder.itemView.setBackgroundColor(ContextCompat.getColor(context, R.color.level_completed))
                holder.levelNumber.setTextColor(ContextCompat.getColor(context, R.color.text_completed))
            } else {
                // Available
                holder.itemView.setBackgroundColor(ContextCompat.getColor(context, R.color.level_available))
                holder.levelNumber.setTextColor(ContextCompat.getColor(context, R.color.text_available))
            }
            holder.itemView.alpha = 1.0f
        } else {
            // Locked
            holder.itemView.setBackgroundColor(ContextCompat.getColor(context, R.color.level_locked))
            holder.levelNumber.setTextColor(ContextCompat.getColor(context, R.color.text_locked))
            holder.itemView.alpha = 0.5f
        }

        // Click listener
        holder.itemView.setOnClickListener {
            onLevelClick(level)
        }
    }

    override fun getItemCount(): Int = levels.size

    fun updateLevels(newLevels: List<Level>) {
        levels = newLevels
        notifyDataSetChanged()
    }
}