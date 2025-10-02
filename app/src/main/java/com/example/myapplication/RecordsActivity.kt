package com.example.myapplication

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.managers.HighScoreManager
import java.text.SimpleDateFormat
import java.util.*

class RecordsActivity : AppCompatActivity() {

    private lateinit var highScoreManager: HighScoreManager
    private lateinit var recyclerView: RecyclerView
    private lateinit var spinnerLevels: Spinner
    private lateinit var btnBack: Button
    private lateinit var tvSelectedLevel: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_records)

        highScoreManager = HighScoreManager(this)

        setupViews()
        setupLevelSelector()
        loadLevels()
    }

    private lateinit var levelSelectorLayout: LinearLayout

    private fun setupViews() {
        recyclerView = findViewById(R.id.recyclerViewRecords)
        recyclerView.layoutManager = LinearLayoutManager(this)

        spinnerLevels = findViewById(R.id.spinnerLevels)
        tvSelectedLevel = findViewById(R.id.tvSelectedLevel)
        levelSelectorLayout = findViewById(R.id.levelSelectorLayout)

        btnBack = findViewById(R.id.btnBackToMenu)
        btnBack.setOnClickListener {
            finish() // Quay lại menu
        }
    }

    private fun setupLevelSelector() {
        spinnerLevels.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                val selectedLevel = parent?.getItemAtPosition(position) as Int
                loadHighScoresForLevel(selectedLevel)
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                // Do nothing
            }
        }
    }

    private fun loadLevels() {
        val levelsWithScores = highScoreManager.getAllLevelsWithScores().toList().sorted()

        if (levelsWithScores.isEmpty()) {
            // Hiển thị message khi chưa có kỷ lục nào
            findViewById<TextView>(R.id.tvNoRecords).visibility = View.VISIBLE
            levelSelectorLayout.visibility = View.GONE
            tvSelectedLevel.visibility = View.GONE
            recyclerView.visibility = View.GONE
        } else {
            findViewById<TextView>(R.id.tvNoRecords).visibility = View.GONE
            levelSelectorLayout.visibility = View.VISIBLE
            tvSelectedLevel.visibility = View.VISIBLE

            // Setup spinner với các level có kỷ lục
            val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, levelsWithScores)
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            spinnerLevels.adapter = adapter

            // Load kỷ lục cho level đầu tiên
            if (levelsWithScores.isNotEmpty()) {
                loadHighScoresForLevel(levelsWithScores[0])
            }
        }
    }

    private fun loadHighScoresForLevel(levelId: Int) {
        val highScores = highScoreManager.getHighScores(levelId)

        tvSelectedLevel.text = "Level $levelId - Top 10 Kỷ lục"

        if (highScores.isEmpty()) {
            recyclerView.visibility = View.GONE
        } else {
            recyclerView.visibility = View.VISIBLE
            recyclerView.adapter = HighScoreAdapter(highScores, highScoreManager)
        }
    }
}

class HighScoreAdapter(
    private val highScores: List<Long>,
    private val highScoreManager: HighScoreManager
) : RecyclerView.Adapter<HighScoreAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvRank: TextView = view.findViewById(R.id.tvRank)
        val tvTime: TextView = view.findViewById(R.id.tvTime)
        val tvFormattedTime: TextView = view.findViewById(R.id.tvFormattedTime)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_high_score, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val rank = position + 1
        val timeMillis = highScores[position]

        holder.tvRank.text = "#$rank"
        holder.tvTime.text = "${timeMillis}ms"
        holder.tvFormattedTime.text = highScoreManager.formatTime(timeMillis)
    }

    override fun getItemCount() = highScores.size
}
