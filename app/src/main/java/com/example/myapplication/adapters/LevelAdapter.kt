package com.example.myapplication.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.R
import com.example.myapplication.models.Level

class LevelAdapter(
    private val context: Context,
    private val levels: List<Level>,
    private val onLevelClick: (Level) -> Unit
) : RecyclerView.Adapter<LevelAdapter.LevelViewHolder>() {

    private var lastCompletedLevel = 0

    fun setLastCompletedLevel(levelId: Int) {
        lastCompletedLevel = levelId
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LevelViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.item_level_card, parent, false)
        return LevelViewHolder(view)
    }

    override fun onBindViewHolder(holder: LevelViewHolder, position: Int) {
        val level = levels[position]
        holder.bind(level, position)

        // Add animation
        holder.itemView.animation = AnimationUtils.loadAnimation(context, android.R.anim.fade_in)
    }

    override fun getItemCount(): Int = levels.size

    inner class LevelViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val cardView: CardView = itemView.findViewById(R.id.card_view)
        private val levelContent: LinearLayout = itemView.findViewById(R.id.level_content)
        private val lockIcon: ImageView = itemView.findViewById(R.id.lock_icon)
        private val levelNumber: TextView = itemView.findViewById(R.id.level_number)
        private val levelName: TextView = itemView.findViewById(R.id.level_name)
        private val levelDifficulty: TextView = itemView.findViewById(R.id.level_difficulty)
        private val difficultyIndicator: View = itemView.findViewById(R.id.difficulty_indicator)
        private val levelIcon: ImageView = itemView.findViewById(R.id.level_icon)


        fun bind(level: Level, position: Int) {
            val isUnlocked = level.id <= lastCompletedLevel + 1

            if (isUnlocked) {
                // Show level content
                levelContent.visibility = View.VISIBLE
                lockIcon.visibility = View.GONE

                levelNumber.text = "LEVEL ${level.id}"
                levelName.text = level.name
                levelDifficulty.text = when(level.difficulty) {
                    Level.Difficulty.EASY -> "DỄ"
                    Level.Difficulty.MEDIUM -> "TRUNG BÌNH"
                    Level.Difficulty.HARD -> "KHÓ"
                    else -> "UNKNOWN"
                }

                // Set difficulty indicator color and icon
                when(level.difficulty) {
                    Level.Difficulty.EASY -> {
                        difficultyIndicator.setBackgroundResource(R.drawable.circle_green)
                        levelDifficulty.setTextColor(context.getColor(android.R.color.holo_green_light))
                        levelIcon.setImageResource(R.drawable.hero_right)
                    }
                    Level.Difficulty.MEDIUM -> {
                        difficultyIndicator.setBackgroundResource(R.drawable.circle_yellow)
                        levelDifficulty.setTextColor(context.getColor(android.R.color.holo_orange_light))
                        levelIcon.setImageResource(R.drawable.monster_patrol)
                    }
                    Level.Difficulty.HARD -> {
                        difficultyIndicator.setBackgroundResource(R.drawable.circle_red)
                        levelDifficulty.setTextColor(context.getColor(android.R.color.holo_red_light))
                        levelIcon.setImageResource(R.drawable.zombie)
                    }
                    else -> {
                        difficultyIndicator.setBackgroundResource(R.drawable.circle_green)
                        levelDifficulty.setTextColor(context.getColor(android.R.color.holo_green_light))
                        levelIcon.setImageResource(R.drawable.hero_right)
                    }
                }

                // Set click listener on CardView
                cardView.setOnClickListener {
                    println("🎯 Level clicked: ${level.id}, isUnlocked: true")
                    onLevelClick(level)
                }

            } else {
                // Show lock icon
                levelContent.visibility = View.GONE
                lockIcon.visibility = View.VISIBLE
                cardView.setOnClickListener(null)
            }

            // Set card background based on completion status
            when {
                level.id <= lastCompletedLevel -> {
                    // Completed level - bright background
                    cardView.setCardBackgroundColor(context.getColor(R.color.level_completed))
                }
                level.id == lastCompletedLevel + 1 -> {
                    // Next available level - highlight
                    cardView.setCardBackgroundColor(context.getColor(R.color.level_available))
                }
                else -> {
                    // Locked level - dim background
                    cardView.setCardBackgroundColor(context.getColor(R.color.level_locked))
                }
            }
        }
    }
}