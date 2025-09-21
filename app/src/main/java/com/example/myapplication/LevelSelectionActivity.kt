package com.example.myapplication

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.myapplication.managers.LevelManager
import com.example.myapplication.managers.MusicManager

class LevelSelectionActivity : AppCompatActivity() {

    private lateinit var musicManager: MusicManager
    private var isNavigatingToGame = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Khởi tạo MusicManager
        musicManager = MusicManager.getInstance(this)

        try {
            Log.d("LevelSelection", "Creating level selection layout")
            createSimpleLayout()
        } catch (e: Exception) {
            Log.e("LevelSelection", "Error in onCreate", e)
            Toast.makeText(this, "Lỗi: ${e.message}", Toast.LENGTH_LONG).show()
            startGameDirectly()
        }
    }
    
    private fun createSimpleLayout() {
        // Tạo layout chính
        val mainLayout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(40, 80, 40, 40)
            setBackgroundColor(0xFF263238.toInt())
        }
        
        // Header
        val headerLayout = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            setPadding(0, 0, 0, 40)
        }
        
        // Back button
        val backButton = Button(this).apply {
            text = "← Quay lại"
            setTextColor(0xFFFFFFFF.toInt())
            setBackgroundColor(0xFF37474F.toInt())
            setPadding(30, 20, 30, 20)
            setOnClickListener { finish() }
        }
        
        // Title
        val titleText = TextView(this).apply {
            text = "CHỌN LEVEL"
            textSize = 24f
            setTextColor(0xFFFFFFFF.toInt())
            setPadding(40, 20, 0, 20)
            layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
        }
        
        headerLayout.addView(backButton)
        headerLayout.addView(titleText)
        mainLayout.addView(headerLayout)
        
        // Progress text
        val progressText = TextView(this).apply {
            text = "Chọn level để bắt đầu chơi!"
            textSize = 16f
            setTextColor(0xFFCCCCCC.toInt())
            setPadding(0, 0, 0, 30)
        }
        mainLayout.addView(progressText)
        
        // Load levels
        val levels = LevelManager.getAllLevels()
        Log.d("LevelSelection", "Found ${levels.size} levels")
        
        if (levels.isEmpty()) {
            val errorText = TextView(this).apply {
                text = "Không tìm thấy level nào! Sẽ chạy level mặc định."
                setTextColor(0xFFFF5722.toInt())
                textSize = 16f
                setPadding(0, 20, 0, 20)
            }
            mainLayout.addView(errorText)
            
            // Default level button
            val defaultButton = createLevelButton("Level Mặc Định", "Dễ") {
                startGameDirectly()
            }
            mainLayout.addView(defaultButton)
            
        } else {
            // Create level buttons
            levels.forEach { level ->
                val levelButton = createLevelButton(
                    "Level ${level.id}: ${level.name}", 
                    level.difficulty.name
                ) {
                    startGameWithLevel(level.id)
                }
                mainLayout.addView(levelButton)
            }
        }
        
        setContentView(mainLayout)
    }
    
    private fun createLevelButton(title: String, difficulty: String, onClick: () -> Unit): Button {
        return Button(this).apply {
            text = "$title\n($difficulty)"
            textSize = 16f
            setTextColor(0xFFFFFFFF.toInt())
            setBackgroundColor(0xFF4CAF50.toInt())
            setPadding(40, 30, 40, 30)
            
            // Set margins
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                setMargins(0, 15, 0, 15)
            }
            
            setOnClickListener { 
                Log.d("LevelSelection", "Level button clicked: $title")
                onClick() 
            }
        }
    }
    
    private fun startGameWithLevel(levelId: Int) {
        Log.d("LevelSelection", "Starting game with level $levelId")
        isNavigatingToGame = true
        val intent = Intent(this, GameButtonActivity::class.java).apply {
            putExtra("LEVEL_ID", levelId)
        }
        startActivity(intent)
    }
    
    private fun startGameDirectly() {
        Log.d("LevelSelection", "Starting game with default level")
        isNavigatingToGame = true
        val intent = Intent(this, GameButtonActivity::class.java).apply {
            putExtra("LEVEL_ID", 1)
        }
        startActivity(intent)
        finish()
    }

    override fun onResume() {
        super.onResume()
        // Reset flag và tiếp tục phát nhạc khi quay lại từ game
        isNavigatingToGame = false
        musicManager.resumeMusic()
    }

    override fun onPause() {
        super.onPause()
        // Chỉ tạm dừng nhạc khi không chuyển sang game
        if (!isNavigatingToGame) {
            musicManager.pauseMusic()
        }
    }
}