package com.example.myapplication

import android.content.Intent
import android.os.Bundle
import android.view.animation.AnimationUtils
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.myapplication.managers.MusicManager
import com.example.myapplication.managers.SoundManager

class MenuActivity : AppCompatActivity() {
    private lateinit var musicManager: MusicManager
    private lateinit var soundManager: SoundManager
    private var isNavigatingToMusicSettings = false
    private var isNavigatingToGame = false
    private var isFirstResume = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_menu)

        // Khởi tạo Managers Singleton
        musicManager = MusicManager.getInstance(this)
        soundManager = SoundManager.getInstance(this)

        // Thiết lập các sự kiện cho các nút
        setupButtons()

        // Thêm animation
        setupAnimations()
    }

    private fun loadMusicSettings() {
        val sharedPreferences = getSharedPreferences("audio_settings", MODE_PRIVATE)

        // Load music settings
        val musicEnabled = sharedPreferences.getBoolean("music_enabled", true)
        val musicVolume = sharedPreferences.getFloat("music_volume", 0.5f)
        val selectedMusic = sharedPreferences.getInt("selected_music", MusicManager.MUSIC_MENU)

        musicManager.setEnabled(musicEnabled)
        musicManager.setVolume(musicVolume)

        // Load sound effects settings
        val soundEnabled = sharedPreferences.getBoolean("sound_enabled", true)
        val soundVolume = sharedPreferences.getFloat("sound_volume", 0.5f)

        soundManager.setMuted(!soundEnabled)
        soundManager.setVolume(soundVolume)

        // Phát nhạc khi khởi động app lần đầu (chỉ khi music enabled)
        if (musicEnabled) {
            musicManager.playMusic(selectedMusic, true)
        }
    }

    private fun setupButtons() {
        // Nút Play - chuyển đến màn hình game, giữ nhạc phát tiếp
        findViewById<Button>(R.id.btnPlay).setOnClickListener {
            isNavigatingToGame = true
            val intent = Intent(this, LevelSelectionActivity::class.java)
            startActivity(intent)
        }

        // Nút History - hiển thị lịch sử game
        findViewById<Button>(R.id.btnHistory).setOnClickListener {
            showToast("Lịch sử game sẽ được hiển thị ở đây!")
        }

        // Nút Record - hiển thị kỷ lục
        findViewById<Button>(R.id.btnRecord).setOnClickListener {
            showToast("Kỷ lục cao nhất sẽ được hiển thị ở đây!")
        }

        // MUSIC - giữ nhạc phát tiếp
        findViewById<Button>(R.id.btnMusic).setOnClickListener {
            isNavigatingToMusicSettings = true
            val intent = Intent(this, MusicSelectionActivity::class.java)
            startActivity(intent)
        }
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
    
    private fun setupAnimations() {
        // Animation cho title
        val titleAnimation = AnimationUtils.loadAnimation(this, R.anim.fade_in)
        findViewById<TextView>(R.id.titleText).startAnimation(titleAnimation)
        
        // Animation cho menu buttons
        val buttonAnimation = AnimationUtils.loadAnimation(this, R.anim.slide_up)
        findViewById<LinearLayout>(R.id.menuButtonsContainer).startAnimation(buttonAnimation)
    }

    override fun onResume() {
        super.onResume()
        // Reset flags
        isNavigatingToMusicSettings = false
        isNavigatingToGame = false

        if (isFirstResume) {
            // Lần đầu vào app: load settings và phát nhạc
            isFirstResume = false
            loadMusicSettings()
        } else {
            // Các lần sau: chỉ tiếp tục phát nhạc
            musicManager.resumeMusic()
        }
    }

    override fun onPause() {
        super.onPause()
        // Chỉ tạm dừng nhạc khi không chuyển sang Music Settings hoặc Game
        if (!isNavigatingToMusicSettings && !isNavigatingToGame) {
            musicManager.pauseMusic()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        musicManager.release()
    }

    override fun onBackPressed() {
        super.onBackPressed()
        // Thoát ứng dụng khi nhấn nút back ở màn hình menu
        finishAffinity()
    }
} 