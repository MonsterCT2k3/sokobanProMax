package com.example.myapplication

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.myapplication.managers.MusicManager
import com.example.myapplication.managers.SoundManager

class WinScreenActivity : AppCompatActivity() {

    private lateinit var musicManager: MusicManager
    private lateinit var soundManager: SoundManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_win_screen)

        // Khởi tạo managers
        musicManager = MusicManager.getInstance(this)
        soundManager = SoundManager.getInstance(this)

        // Lấy thông tin level từ Intent
        val levelId = intent.getIntExtra("level_id", 1)
        val nextLevelId = levelId + 1

        // Setup UI
        val titleText = findViewById<TextView>(R.id.win_title)
        val messageText = findViewById<TextView>(R.id.win_message)
        val nextButton = findViewById<Button>(R.id.btn_next_level)
        val menuButton = findViewById<Button>(R.id.btn_back_to_menu)

        titleText.text = "🎉 CHÚC MỪNG! 🎉"
        messageText.text = "Bạn đã hoàn thành Level $levelId!"

        // Button Next Level
        nextButton.setOnClickListener {
            soundManager.playSound("move") // Phát âm thanh click

            val intent = Intent(this, GameButtonActivity::class.java)
            intent.putExtra("level_id", nextLevelId)
            startActivity(intent)
            finish() // Đóng win screen
        }

        // Button Back to Menu
        menuButton.setOnClickListener {
            soundManager.playSound("move") // Phát âm thanh click

            val intent = Intent(this, MenuActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP // Clear back stack
            startActivity(intent)
            finish() // Đóng win screen
        }
    }

    override fun onResume() {
        super.onResume()
        // Phát nhạc chiến thắng hoặc tiếp tục nhạc hiện tại
        musicManager.playMusic(MusicManager.MUSIC_GAME_1, false) // Tiếp tục nhạc game
    }

    override fun onPause() {
        super.onPause()
        // Có thể pause nhạc nếu muốn
    }
}
