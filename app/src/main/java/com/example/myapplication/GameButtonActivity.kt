package com.example.myapplication

import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.example.myapplication.managers.MusicManager

class GameButtonActivity : AppCompatActivity() {

    private lateinit var gameView: GameView
    private lateinit var musicManager: MusicManager
    private var isNavigatingToVictory = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)


        musicManager = MusicManager.getInstance(this)
        musicManager.resumeMusic()
        // Lấy tham chiếu đến GameView
        gameView = findViewById(R.id.gameView)

        // Set background image
        gameView.setBackgroundImage(
            R.drawable.bg2, // Thử với ảnh forest
            com.example.myapplication.rendering.BackgroundManager.BackgroundScrollType.PARALLAX_HORIZONTAL
        )

        // Lấy level ID từ intent và load level
        val levelId = intent.getIntExtra("LEVEL_ID", 1)
        gameView.loadLevel(levelId)

        // Setup callback từ GameView để xử lý navigation
        gameView.setVictoryNavigationCallback {
            isNavigatingToVictory = true
        }

        
        // Nút quay lại Level Selection
        findViewById<Button>(R.id.buttonBack).setOnClickListener {
            showExitGameDialog()
        }
        
        // Nút Reset level
        findViewById<Button>(R.id.buttonReset).setOnClickListener {
            // 🆕 Lấy level hiện tại từ game thay vì từ intent
            val currentLevelId = gameView.getCurrentLevelId()
            gameView.loadLevel(currentLevelId) // Reset level hiện tại về trạng thái ban đầu
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