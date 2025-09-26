package com.example.myapplication

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.adapters.LevelAdapter
import com.example.myapplication.managers.LevelManager
import com.example.myapplication.managers.MusicManager
import com.example.myapplication.managers.SoundManager
import com.example.myapplication.models.Level

class LevelSelectionActivity : AppCompatActivity() {

    private lateinit var musicManager: MusicManager
    private lateinit var soundManager: SoundManager
    private var isNavigatingToGame = false
    private lateinit var levelAdapter: LevelAdapter
    private lateinit var sharedPreferences: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_level_selection)

        // Khởi tạo MusicManager
        musicManager = MusicManager.getInstance(this)

        // Khởi tạo SoundManager
        soundManager = SoundManager.getInstance(this)

        // Khởi tạo SharedPreferences
        sharedPreferences = getSharedPreferences("game_progress", MODE_PRIVATE)

        try {
            Log.d("LevelSelection", "Creating level selection layout")
            setupViews()
            loadLevels()
        } catch (e: Exception) {
            Log.e("LevelSelection", "Error in onCreate", e)
            Toast.makeText(this, "Lỗi: ${e.message}", Toast.LENGTH_LONG).show()
            startGameDirectly()
        }
    }

    private fun setupViews() {
        // Setup back button
        findViewById<Button>(R.id.btn_back).setOnClickListener {
            finish()
        }

        // Setup RecyclerView
        val recyclerView = findViewById<RecyclerView>(R.id.levels_recycler_view)
        recyclerView.layoutManager = GridLayoutManager(this, 2)
        recyclerView.setHasFixedSize(true)
    }

    private fun loadLevels() {
        val levels = LevelManager.getAllLevels()
        Log.d("LevelSelection", "Found ${levels.size} levels")

        if (levels.isEmpty()) {
            Toast.makeText(this, "Không tìm thấy level nào!", Toast.LENGTH_LONG).show()
            startGameDirectly()
            return
        }

        // Get last completed level from SharedPreferences
        val lastCompletedLevel = sharedPreferences.getInt("last_completed_level", 0)
        println("📊 Last completed level: $lastCompletedLevel")

        // Setup adapter
        levelAdapter = LevelAdapter(this, levels) { level ->
            println("🎮 Starting game with level ${level.id}")
            startGameWithLevel(level.id)
        }
        levelAdapter.setLastCompletedLevel(lastCompletedLevel)

        findViewById<RecyclerView>(R.id.levels_recycler_view).adapter = levelAdapter

        // Update progress text
        updateProgressText(levels.size, lastCompletedLevel)
    }

    private fun updateProgressText(totalLevels: Int, completedLevels: Int) {
        val progressText = findViewById<TextView>(R.id.progress_text)
        progressText.text = "Hoàn thành: $completedLevels/$totalLevels levels"
    }

    private fun loadSoundSettings() {
        // Load sound settings từ SharedPreferences "audio_settings"
        val audioPrefs = getSharedPreferences("audio_settings", MODE_PRIVATE)
        val soundEnabled = audioPrefs.getBoolean("sound_enabled", true)
        val soundVolume = audioPrefs.getFloat("sound_volume", 0.5f)

        // Apply settings
        soundManager.setMuted(!soundEnabled)
        soundManager.setVolume(soundVolume)

        println("🔊 Sound settings loaded - enabled: $soundEnabled, volume: $soundVolume")
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

        // 🔊 Luôn load sound settings khi resume để đồng bộ với settings đã lưu
        loadSoundSettings()
        println("🔊 LevelSelection resumed - soundManager.isMuted(): ${soundManager.isMuted()}, volume: ${soundManager.getVolume()}")

        // Test sound để xem có hoạt động không
        soundManager.playSound("move", 0.1f)  // Test với volume nhỏ
    }

    override fun onPause() {
        super.onPause()
        // Chỉ tạm dừng nhạc khi không chuyển sang game
        if (!isNavigatingToGame) {
            musicManager.pauseMusic()
        }
    }
}