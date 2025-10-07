package com.example.myapplication

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.myapplication.managers.MusicManager

/**
 * 🎨 CustomVictoryActivity - Màn hình chiến thắng cho chế độ Custom
 *
 * Hiển thị khi hoàn thành custom level:
 * - Thông tin về custom level đã tạo
 * - Thời gian hoàn thành
 * - Nút chơi lại hoặc tạo level mới
 */
class CustomVictoryActivity : AppCompatActivity() {

    private lateinit var musicManager: MusicManager

    // Custom level info
    private var mapSize = ""
    private var boxCount = 0
    private var monsterCount = 0
    private var completionTime: Long = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_custom_victory)

        musicManager = MusicManager.getInstance()!!

        // Get data from intent
        mapSize = intent.getStringExtra("map_size") ?: "15x15"
        boxCount = intent.getIntExtra("box_count", 3)
        monsterCount = intent.getIntExtra("monster_count", 0)
        completionTime = intent.getLongExtra("completion_time", 0)

        setupViews()
    }

    private fun setupViews() {
        val tvTitle = findViewById<TextView>(R.id.tvCustomVictoryTitle)
        val tvLevelInfo = findViewById<TextView>(R.id.tvCustomLevelInfo)
        val tvStats = findViewById<TextView>(R.id.tvCustomStats)
        val tvTime = findViewById<TextView>(R.id.tvCustomTime)
        val btnPlayAgain = findViewById<Button>(R.id.btnCustomPlayAgain)
        val btnCreateNew = findViewById<Button>(R.id.btnCustomCreateNew)
        val btnBackToMenu = findViewById<Button>(R.id.btnCustomBackToMenu)

        tvTitle.text = "🎨 CUSTOM LEVEL COMPLETED!"

        tvLevelInfo.text = "Map Size: $mapSize\nBoxes: $boxCount\nMonsters: $monsterCount"

        tvStats.text = "🎯 All boxes placed successfully!\n" +
                      "👾 Monsters defeated: $monsterCount\n" +
                      "⭐ Custom level mastered!"

        tvTime.text = "⏱️ Completion Time: ${formatTime(completionTime)}"

        btnPlayAgain.setOnClickListener {
            // Play the same custom level again
            val intent = Intent(this, GameButtonActivity::class.java)
            intent.putExtra("LEVEL_ID", -1)
            // Pass the custom level data back (need to store it)
            intent.putExtra("customLevelMap", getIntent().getStringExtra("customLevelMap"))
            intent.putExtra("customLevelWidth", getIntent().getIntExtra("customLevelWidth", 15))
            intent.putExtra("customLevelHeight", getIntent().getIntExtra("customLevelHeight", 15))
            intent.putExtra("customBoxCount", boxCount)
            intent.putExtra("customMonsterData", getIntent().getSerializableExtra("customMonsterData"))
            startActivity(intent)
            finish()
        }

        btnCreateNew.setOnClickListener {
            // Go back to customize mode to create new level
            val intent = Intent(this, CustomizeActivity::class.java)
            startActivity(intent)
            finish()
        }

        btnBackToMenu.setOnClickListener {
            // Go back to main menu
            val intent = Intent(this, MenuActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            startActivity(intent)
            finish()
        }
    }

    private fun formatTime(timeMs: Long): String {
        val totalSeconds = timeMs / 1000
        val minutes = totalSeconds / 60
        val seconds = totalSeconds % 60
        return String.format("%02d:%02d", minutes, seconds)
    }

    override fun onResume() {
        super.onResume()
        musicManager.resumeMusic()
    }

    override fun onPause() {
        super.onPause()
        musicManager.pauseMusic()
    }

    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        val intent = Intent(this, MenuActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
        startActivity(intent)
        finish()
    }
}
