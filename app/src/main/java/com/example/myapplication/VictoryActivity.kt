package com.example.myapplication

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.managers.HighScoreManager
import java.text.SimpleDateFormat
import java.util.*

class VictoryActivity : AppCompatActivity() {

    private lateinit var highScoreManager: HighScoreManager
    private lateinit var recyclerView: RecyclerView
    private lateinit var btnNextLevel: Button
    private lateinit var btnSelectLevel: Button
    private lateinit var tvLevelTitle: TextView
    private lateinit var tvYourTime: TextView

    private var currentLevelId = 1
    private var yourTime: Long = 0
    private var isNewRecord = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_victory)

        // Get data from intent
        currentLevelId = intent.getIntExtra("level_id", 1)
        yourTime = intent.getLongExtra("your_time", 0)
        isNewRecord = intent.getBooleanExtra("is_new_record", false)

        highScoreManager = HighScoreManager(this)

        setupViews()
        loadVictoryData()
    }

    private fun setupViews() {
        recyclerView = findViewById(R.id.recyclerViewVictory)
        recyclerView.layoutManager = LinearLayoutManager(this)

        btnNextLevel = findViewById(R.id.btnNextLevel)
        btnSelectLevel = findViewById(R.id.btnSelectLevel)
        tvLevelTitle = findViewById(R.id.tvLevelTitle)
        tvYourTime = findViewById(R.id.tvYourTime)

        btnNextLevel.setOnClickListener {
            // Next level
            val nextLevelId = currentLevelId + 1
            val intent = Intent(this, GameButtonActivity::class.java)
            intent.putExtra("LEVEL_ID", nextLevelId)
            startActivity(intent)
            finish()
        }

        btnSelectLevel.setOnClickListener {
            // Back to level selection - clear back stack để tránh chồng activity
            val intent = Intent(this, LevelSelectionActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            startActivity(intent)
            finish()
        }
    }

    private fun loadVictoryData() {
        // Set level title
        tvLevelTitle.text = "🎉 CHIẾN THẮNG LEVEL $currentLevelId! 🎉"

        // Set your time
        val timeFormatter = SimpleDateFormat("mm:ss", Locale.getDefault())
        timeFormatter.timeZone = TimeZone.getTimeZone("UTC")
        val timeStr = timeFormatter.format(Date(yourTime))

        val recordText = if (isNewRecord) " 🏆 KỶ LỤC MỚI!" else ""
        tvYourTime.text = "⏱️ Thời gian của bạn: $timeStr$recordText"

        // Load high scores for this level
        val highScores = highScoreManager.getHighScores(currentLevelId)
        recyclerView.adapter = VictoryAdapter(highScores, highScoreManager, yourTime)
    }
}

class VictoryAdapter(
    private val highScores: List<Long>,
    private val highScoreManager: HighScoreManager,
    private val yourTime: Long
) : RecyclerView.Adapter<VictoryAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        // Top 3 elements
        val top3Background: LinearLayout = view.findViewById(R.id.top3Background)
        val tvRank: TextView = view.findViewById(R.id.tvRank)
        val tvTime: TextView = view.findViewById(R.id.tvTime)
        val tvFormattedTime: TextView = view.findViewById(R.id.tvFormattedTime)
        val tvIsYourTime: TextView = view.findViewById(R.id.tvIsYourTime)

        // Regular elements
        val regularBackground: LinearLayout = view.findViewById(R.id.regularBackground)
        val tvRankRegular: TextView = view.findViewById(R.id.tvRankRegular)
        val tvTimeRegular: TextView = view.findViewById(R.id.tvTimeRegular)
        val tvFormattedTimeRegular: TextView = view.findViewById(R.id.tvFormattedTimeRegular)
        val tvIsYourTimeRegular: TextView = view.findViewById(R.id.tvIsYourTimeRegular)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = android.view.LayoutInflater.from(parent.context)
            .inflate(R.layout.item_victory_score, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val rank = position + 1
        val timeMillis = highScores[position]
        val isTop3 = rank <= 3

        // Show appropriate background
        if (isTop3) {
            holder.top3Background.visibility = View.VISIBLE
            holder.regularBackground.visibility = View.GONE

            holder.tvRank.text = "#$rank"
            holder.tvTime.text = "${timeMillis}ms"
            holder.tvFormattedTime.text = highScoreManager.formatTime(timeMillis)

            // Check if this is the player's time
            if (timeMillis == yourTime) {
                holder.tvIsYourTime.text = "← BẠN!"
                holder.tvIsYourTime.visibility = View.VISIBLE
            } else {
                holder.tvIsYourTime.visibility = View.GONE
            }
        } else {
            holder.top3Background.visibility = View.GONE
            holder.regularBackground.visibility = View.VISIBLE

            holder.tvRankRegular.text = "#$rank"
            holder.tvTimeRegular.text = "${timeMillis}ms"
            holder.tvFormattedTimeRegular.text = highScoreManager.formatTime(timeMillis)

            // Check if this is the player's time
            if (timeMillis == yourTime) {
                holder.tvIsYourTimeRegular.text = "← BẠN!"
                holder.tvIsYourTimeRegular.visibility = View.VISIBLE
            } else {
                holder.tvIsYourTimeRegular.visibility = View.GONE
            }
        }
    }

    override fun getItemCount() = highScores.size
}
