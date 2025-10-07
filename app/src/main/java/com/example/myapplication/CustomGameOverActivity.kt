package com.example.myapplication

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.myapplication.managers.MusicManager

/**
 * 🎨 CustomGameOverActivity - Màn hình thất bại cho chế độ Custom
 *
 * Hiển thị khi thất bại trong custom level:
 * - Thông tin về custom level
 * - Lý do thất bại
 * - Nút thử lại hoặc tạo level mới
 */
class CustomGameOverActivity : AppCompatActivity() {

    private lateinit var musicManager: MusicManager

    // Custom level info
    private var mapSize = ""
    private var boxCount = 0
    private var monsterCount = 0
    private var failureReason = "Player died"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_custom_game_over)

        musicManager = MusicManager.getInstance()!!

        // Get data from intent
        mapSize = intent.getStringExtra("map_size") ?: "15x15"
        boxCount = intent.getIntExtra("box_count", 3)
        monsterCount = intent.getIntExtra("monster_count", 0)
        failureReason = intent.getStringExtra("failure_reason") ?: "Player died"

        setupViews()
    }

    private fun setupViews() {
        val tvTitle = findViewById<TextView>(R.id.tvCustomGameOverTitle)
        val tvLevelInfo = findViewById<TextView>(R.id.tvCustomLevelInfo)
        val tvFailureReason = findViewById<TextView>(R.id.tvCustomFailureReason)
        val btnTryAgain = findViewById<Button>(R.id.btnCustomTryAgain)
        val btnModifyLevel = findViewById<Button>(R.id.btnCustomModifyLevel)
        val btnBackToMenu = findViewById<Button>(R.id.btnCustomGameOverBackToMenu)

        tvTitle.text = "💀 CUSTOM LEVEL FAILED"

        tvLevelInfo.text = "Map Size: $mapSize\nBoxes: $boxCount\nMonsters: $monsterCount"

        tvFailureReason.text = "❌ $failureReason\n\nDon't give up! Try again or modify your level."

        btnTryAgain.setOnClickListener {
            // Try the same custom level again
            val intent = Intent(this, GameButtonActivity::class.java)
            intent.putExtra("LEVEL_ID", -1)
            // Pass the custom level data back
            intent.putExtra("customLevelMap", getIntent().getStringExtra("customLevelMap"))
            intent.putExtra("customLevelWidth", getIntent().getIntExtra("customLevelWidth", 15))
            intent.putExtra("customLevelHeight", getIntent().getIntExtra("customLevelHeight", 15))
            intent.putExtra("customBoxCount", boxCount)
            intent.putExtra("customMonsterData", getIntent().getSerializableExtra("customMonsterData"))
            startActivity(intent)
            finish()
        }

        btnModifyLevel.setOnClickListener {
            // Go back to customize mode to modify the level
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
