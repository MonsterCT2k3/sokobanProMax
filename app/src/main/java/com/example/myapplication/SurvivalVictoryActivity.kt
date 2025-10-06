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
        val tvSubtitle = findViewById<TextView>(R.id.tvSurvivalVictorySubtitle)
        val tvTotalTime = findViewById<TextView>(R.id.tvSurvivalTotalTime)
        val tvCompletedLevels = findViewById<TextView>(R.id.tvSurvivalCompletedLevels)
        val tvPerformance = findViewById<TextView>(R.id.tvSurvivalPerformance)
        val btnPlayAgain = findViewById<Button>(R.id.btnSurvivalPlayAgain)
        val btnBackToMenu = findViewById<Button>(R.id.btnSurvivalVictoryBackToMenu)

        if (session != null) {
            // Tính tổng thời gian thực tế (bao gồm cả thời gian level cuối)
            val currentTime = System.currentTimeMillis()
            val sessionDuration = currentTime - session.sessionStartTime
            val totalPlayedTime = session.totalTimeMs + sessionDuration
            
            tvTitle.text = "🏆 SURVIVAL MASTER!"
            tvSubtitle.text = "Congratulations! You conquered all levels!"
            tvTotalTime.text = "⏱️ Total Time: ${formatTime(totalPlayedTime)}"
            tvCompletedLevels.text = "🎯 Completed: ${session.completedLevels.joinToString(" → ")} (${session.totalLevels}/3)"
            
            // Đánh giá performance dựa trên thời gian
            val performanceText = when {
                totalPlayedTime < 300000 -> "⚡ LIGHTNING FAST! Amazing speed!"
                totalPlayedTime < 600000 -> "🔥 EXCELLENT! Great performance!"
                totalPlayedTime < 900000 -> "👍 GOOD JOB! Well done!"
                else -> "🎉 COMPLETED! You did it!"
            }
            tvPerformance.text = performanceText
        } else {
            tvTitle.text = "🏆 VICTORY!"
            tvSubtitle.text = "Session data not available"
            tvTotalTime.text = ""
            tvCompletedLevels.text = ""
            tvPerformance.text = ""
        }

        btnPlayAgain.setOnClickListener {
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
