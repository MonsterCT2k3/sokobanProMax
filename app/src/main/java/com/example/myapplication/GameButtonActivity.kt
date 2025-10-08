package com.example.myapplication

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.example.myapplication.managers.MusicManager
import com.example.myapplication.rendering.BackgroundManager

class GameButtonActivity : AppCompatActivity() {

    private lateinit var gameView: GameView
    private lateinit var musicManager: MusicManager
    private var isNavigatingToVictory = false

    // Store custom level data for reset functionality
    private var customLevelMap: String? = null
    private var customWidth: Int = 15
    private var customHeight: Int = 15
    private var customBoxCount: Int = 3
    private var customMonsterData: ArrayList<Triple<Int, Int, String>>? = null
    private var isCustomLevel: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)


        musicManager = MusicManager.getInstance(this)
        musicManager.resumeMusic()
        // Lấy tham chiếu đến GameView
        gameView = findViewById(R.id.gameView)

        // Lấy level ID từ intent và load level
        val levelId = intent.getIntExtra("LEVEL_ID", 1)

        // Check if this is a custom level
        customLevelMap = intent.getStringExtra("customLevelMap")
        customWidth = intent.getIntExtra("customLevelWidth", 15)
        customHeight = intent.getIntExtra("customLevelHeight", 15)
        customBoxCount = intent.getIntExtra("customBoxCount", 3)
        customMonsterData = intent.getSerializableExtra("customMonsterData") as? ArrayList<Triple<Int, Int, String>>

        println("🎮 GameButtonActivity received custom level data:")
        println("   Map: ${if (customLevelMap != null) "YES (${customLevelMap!!.length} chars)" else "NO"}")
        println("   Size: ${customWidth}x${customHeight}")
        println("   Boxes: $customBoxCount")
        println("   Monster data: ${customMonsterData?.size ?: 0} monsters")
        customMonsterData?.forEachIndexed { index, (x, y, type) ->
            println("      ${index + 1}. ($x, $y) - $type")
        }

        // Set background dựa trên chế độ chơi
        if (customLevelMap != null) {
            // Custom level: sử dụng bg6
            gameView.setBackgroundImage(
                R.drawable.bg2, // Background đặc biệt cho custom mode
                BackgroundManager.BackgroundScrollType.PARALLAX_HORIZONTAL
            )
            // Load custom level
            isCustomLevel = true
            
            val monsterList = customMonsterData ?: emptyList()
            println("🎮 Passing ${monsterList.size} monsters to GameView.loadCustomLevelData")
            
            gameView.loadCustomLevelData(customLevelMap!!, customWidth, customHeight, customBoxCount, monsterList)
        } else {
            // Classic level: sử dụng bg2
            gameView.setBackgroundImage(
                R.drawable.bg2, // Background thông thường
                BackgroundManager.BackgroundScrollType.PARALLAX_HORIZONTAL
            )
            // Load regular level
            isCustomLevel = false
            gameView.loadLevel(levelId)
        }

        // Setup callback từ GameView để xử lý navigation
        gameView.setVictoryNavigationCallback {
            isNavigatingToVictory = true

            // Handle victory navigation based on level type
            if (isCustomLevel) {
                // Custom level victory
                val intent = Intent(this, CustomVictoryActivity::class.java)
                intent.putExtra("map_size", "${customWidth}x${customHeight}")
                intent.putExtra("box_count", customBoxCount)
                intent.putExtra("monster_count", customMonsterData?.size ?: 0)
                intent.putExtra("completion_time", gameView.getCurrentElapsedTime())
                // Pass custom level data for "play again"
                intent.putExtra("customLevelMap", customLevelMap)
                intent.putExtra("customLevelWidth", customWidth)
                intent.putExtra("customLevelHeight", customHeight)
                intent.putExtra("customMonsterData", customMonsterData)
                startActivity(intent)
                finish()
            } else {
                // Regular level victory - let GameView handle it
                // (GameView will start VictoryActivity)
            }
        }

        
        // Nút quay lại Level Selection
        findViewById<Button>(R.id.buttonBack).setOnClickListener {
            showExitGameDialog()
        }
        
        // Nút Reset level
        findViewById<Button>(R.id.buttonReset).setOnClickListener {
            if (isCustomLevel && customLevelMap != null) {
                // Reset custom level: reload with stored data
                gameView.loadCustomLevelData(customLevelMap!!, customWidth, customHeight, customBoxCount, customMonsterData ?: emptyList())
            } else {
                // Reset regular level
                val currentLevelId = gameView.getCurrentLevelId()
                gameView.loadLevel(currentLevelId)
            }
        }
    }



    override fun onResume() {
        super.onResume()
        // Reset navigation flag
        isNavigatingToVictory = false
        if (::gameView.isInitialized) {
            gameView.resumeGame()
        }
        musicManager.resumeMusic()
    }

    override fun onPause() {
        super.onPause()
        if (::gameView.isInitialized) {
            gameView.pauseGame()
        }
        // Chỉ tạm dừng nhạc khi không chuyển sang VictoryActivity
        if (!isNavigatingToVictory) {
            musicManager.pauseMusic()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (::gameView.isInitialized) {
            gameView.stopGame()
        }
    }

    private fun showExitGameDialog() {
        AlertDialog.Builder(this)
            .setTitle("Thoát Game")
            .setMessage("Bạn có chắc muốn thoát game? Tiến độ hiện tại sẽ không được lưu.")
            .setPositiveButton("Thoát") { _, _ ->
                finish() // Quay lại LevelSelectionActivity
            }
            .setNegativeButton("Tiếp tục") { dialog, _ ->
                dialog.dismiss() // Đóng dialog, tiếp tục chơi
            }
            .setCancelable(true)
            .show()
    }

    override fun onBackPressed() {
        super.onBackPressed()
        // Khi nhấn nút back của hệ thống cũng hiện dialog
        showExitGameDialog()
    }
} 