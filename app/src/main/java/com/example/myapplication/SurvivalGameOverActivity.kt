package com.example.myapplication

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.myapplication.managers.MusicManager
import com.example.myapplication.managers.SurvivalManager

/**
 * 💀 SurvivalGameOverActivity - Màn hình thua Survival Mode
 * 
 * Hiển thị kết quả khi hết mạng:
 * - Số levels đã hoàn thành
 * - Thời gian đã chơi
 * - Nút thử lại và quay về menu
 */
class SurvivalGameOverActivity : AppCompatActivity() {

    private lateinit var musicManager: MusicManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_survival_game_over)

        musicManager = MusicManager.getInstance()!!

        // Get session data
        val session = SurvivalManager.getCurrentSession()
        
        // Setup UI
        setupUI(session)
        
        // End session
        SurvivalManager.endSession()
    }

    private fun setupUI(session: com.example.myapplication.models.SurvivalSession?) {
        val tvTitle = findViewById<TextView>(R.id.tvSurvivalGameOverTitle)
        val tvLevelsCompleted = findViewById<TextView>(R.id.tvSurvivalLevelsCompleted)
        val tvTimePlayed = findViewById<TextView>(R.id.tvSurvivalTimePlayed)
        val tvCurrentLevel = findViewById<TextView>(R.id.tvSurvivalCurrentLevel)
        val btnRetry = findViewById<Button>(R.id.btnSurvivalRetry)
        val btnBackToMenu = findViewById<Button>(R.id.btnSurvivalGameOverBackToMenu)

        if (session != null) {
            // Tính tổng thời gian thực tế
            // session.totalTimeMs chứa thời gian của các level đã hoàn thành
            // Cần thêm thời gian đã chơi ở level hiện tại (chưa hoàn thành)
            val currentTime = System.currentTimeMillis()
            val sessionDuration = currentTime - session.sessionStartTime
            val totalPlayedTime = session.totalTimeMs + sessionDuration
            
            tvTitle.text = "💀 SURVIVAL FAILED!"
            tvCurrentLevel.text = "💥 Failed at Level: ${session.currentLevelId}"
            tvLevelsCompleted.text = "✅ Completed: ${session.completedLevels.size}/${session.totalLevels} levels"
            tvTimePlayed.text = "⏱️ Total Time: ${formatTime(totalPlayedTime)}"
        } else {
            tvTitle.text = "💀 GAME OVER!"
            tvCurrentLevel.text = "Session data not available"
            tvLevelsCompleted.text = ""
            tvTimePlayed.text = ""
        }

        btnRetry.setOnClickListener {
            // Start new Survival session
            val intent = Intent(this, SurvivalGameActivity::class.java)
            startActivity(intent)
            finish()
        }

        btnBackToMenu.setOnClickListener {
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
