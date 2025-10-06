package com.example.myapplication

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.myapplication.managers.MusicManager
import com.example.myapplication.managers.SurvivalManager

/**
 * 🏆 SurvivalVictoryActivity - Màn hình chiến thắng Survival Mode
 * 
 * Hiển thị kết quả khi hoàn thành tất cả 3 levels:
 * - Tổng thời gian chơi
 * - Danh sách levels đã hoàn thành
 * - Nút quay về menu
 */
class SurvivalVictoryActivity : AppCompatActivity() {

    private lateinit var musicManager: MusicManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_survival_victory)

        musicManager = MusicManager.getInstance()!!

        // Get session data
        val session = SurvivalManager.getCurrentSession()
        
        // Setup UI
        setupUI(session)
        
        // End session
        SurvivalManager.endSession()
    }

    private fun setupUI(session: com.example.myapplication.models.SurvivalSession?) {
        val tvTitle = findViewById<TextView>(R.id.tvSurvivalVictoryTitle)
        val tvTotalTime = findViewById<TextView>(R.id.tvSurvivalTotalTime)
        val tvCompletedLevels = findViewById<TextView>(R.id.tvSurvivalCompletedLevels)
        val btnBackToMenu = findViewById<Button>(R.id.btnSurvivalVictoryBackToMenu)

        if (session != null) {
            tvTitle.text = "🏆 SURVIVAL COMPLETE!"
            tvTotalTime.text = "Total Time: ${formatTime(session.totalTimeMs)}"
            tvCompletedLevels.text = "Completed Levels: ${session.completedLevels.joinToString(", ")}"
        } else {
            tvTitle.text = "🏆 VICTORY!"
            tvTotalTime.text = "Session data not available"
            tvCompletedLevels.text = ""
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
