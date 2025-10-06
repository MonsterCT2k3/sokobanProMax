package com.example.myapplication

import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.example.myapplication.managers.MusicManager
import com.example.myapplication.managers.SurvivalManager

/**
 * 🏃 SurvivalGameActivity - Activity chính cho Survival Mode
 * 
 * Quản lý game flow cho Survival Mode:
 * - Khởi tạo SurvivalSession với 3 levels (1, 3, 4)
 * - Theo dõi trạng thái qua các level (lives, ammo, time)
 * - Xử lý chuyển level và game over
 */
class SurvivalGameActivity : AppCompatActivity() {

    private lateinit var gameView: GameView
    private lateinit var tvSurvivalHeader: TextView
    private lateinit var musicManager: MusicManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_survival_game)

        // Initialize components
        musicManager = MusicManager.getInstance()!!
        tvSurvivalHeader = findViewById(R.id.tvSurvivalHeader)
        gameView = findViewById(R.id.gameView)

        // Start new Survival session
        val session = SurvivalManager.startNewSession()
        println("🏃 SurvivalGameActivity: Starting with session ${session.sessionId}")

        // Setup GameView for Survival mode
        setupGameView()
        
        // Setup bullet controller with session ammo
        setupBulletController()
        
        // Setup Survival callbacks
        setupSurvivalCallbacks()
        
        // Load first level and start
        gameView.loadLevel(session.currentLevelId)
        updateSurvivalUI()
        gameView.startGame()
    }

    private fun setupGameView() {
        // Enable Survival mode
        gameView.setSurvivalMode(true)
        
        // Set background (use default background)
        // gameView.setBackgroundImage(R.drawable.game_background_1)
        
        println("🏃 SurvivalGameActivity: GameView setup completed")
    }

    private fun setupBulletController() {
        val session = SurvivalManager.getCurrentSession()!!
        val bulletController = gameView.getBulletController()
        
        // Restore ammo from session
        bulletController.normalAmmo = session.normalAmmo
        bulletController.pierceAmmo = session.pierceAmmo
        bulletController.stunAmmo = session.stunAmmo
        
        // Set callback to update SurvivalManager when ammo changes
        bulletController.onAmmoChanged = { normalAmmo, pierceAmmo, stunAmmo ->
            SurvivalManager.updateSessionAmmo(normalAmmo, pierceAmmo, stunAmmo)
        }
        
        println("🏃 SurvivalGameActivity: BulletController setup - N:${session.normalAmmo}, P:${session.pierceAmmo}, S:${session.stunAmmo}")
    }

    private fun setupSurvivalCallbacks() {
        gameView.setSurvivalCallbacks(
            onLevelComplete = { levelTimeMs ->
                handleLevelComplete(levelTimeMs)
            },
            onPlayerDeath = {
                handlePlayerDeath()
            },
            getTotalTime = {
                // Trả về tổng thời gian đã chơi từ SurvivalSession
                SurvivalManager.getCurrentSession()?.totalTimeMs ?: 0L
            }
        )
        println("🏃 SurvivalGameActivity: Survival callbacks setup completed")
    }

    private fun handleLevelComplete(levelTimeMs: Long) {
        try {
            println("🏃 SurvivalGameActivity: Level completed in ${levelTimeMs}ms")
            
            val allLevelsComplete = SurvivalManager.completeLevel(levelTimeMs)
            
            if (allLevelsComplete) {
                // All levels completed - Victory!
                navigateToSurvivalVictory()
            } else {
                // Move to next level
                val session = SurvivalManager.getCurrentSession()!!
                val nextLevelId = session.currentLevelId
                
                println("🏃 SurvivalGameActivity: Loading next level: $nextLevelId")
                
                // Load next level
                gameView.loadLevel(nextLevelId)
                
                // Restore ammo from session (important!)
                val bulletController = gameView.getBulletController()
                bulletController.normalAmmo = session.normalAmmo
                bulletController.pierceAmmo = session.pierceAmmo
                bulletController.stunAmmo = session.stunAmmo
                
                // Update UI and restart game
                updateSurvivalUI()
                gameView.startGame()
                
                println("🏃 SurvivalGameActivity: Successfully loaded level $nextLevelId")
            }
        } catch (e: Exception) {
            println("🏃 SurvivalGameActivity: ERROR in handleLevelComplete: ${e.message}")
            e.printStackTrace()
        }
    }

    private fun handlePlayerDeath() {
        try {
            println("🏃 SurvivalGameActivity: Player died")
            
            val gameOver = SurvivalManager.loseLife()
            
            if (gameOver) {
                // No lives left - Game Over
                println("🏃 SurvivalGameActivity: Game Over - No lives left")
                
                // Stop game và phát âm thanh game over
                gameView.stopGame()
                // soundManager.playSound("game_over") // Có thể thêm nếu cần
                
                navigateToSurvivalGameOver()
            } else {
                // Still have lives - Continue playing (GameView đã reset game elements)
                val livesLeft = SurvivalManager.getLivesCount()
                println("🏃 SurvivalGameActivity: Lives remaining: $livesLeft")
                
                // Chỉ cần update UI, không cần reset level vì GameView đã xử lý
                updateSurvivalUI()
                
                // Play sound effect for losing life
                // soundManager.playSound("loose_health") // Đã được phát trong GameView
                
                println("🏃 SurvivalGameActivity: Player death handled, continuing with $livesLeft lives")
            }
        } catch (e: Exception) {
            println("🏃 SurvivalGameActivity: ERROR in handlePlayerDeath: ${e.message}")
            e.printStackTrace()
        }
    }

    private fun updateSurvivalUI() {
        val session = SurvivalManager.getCurrentSession()
        if (session != null) {
            val currentLevel = session.currentLevelIndex + 1
            val totalLevels = session.totalLevels
            val lives = session.lives
            
            tvSurvivalHeader.text = "SURVIVAL - LEVEL $currentLevel/$totalLevels"
        }
    }

    private fun navigateToSurvivalVictory() {
        val intent = Intent(this, SurvivalVictoryActivity::class.java)
        startActivity(intent)
        finish()
    }

    private fun navigateToSurvivalGameOver() {
        val intent = Intent(this, SurvivalGameOverActivity::class.java)
        startActivity(intent)
        finish()
    }

    override fun onResume() {
        super.onResume()
        musicManager.resumeMusic()
    }

    override fun onPause() {
        super.onPause()
        musicManager.pauseMusic()
    }

    override fun onDestroy() {
        super.onDestroy()
        gameView.stopGame()
    }

    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        // Show exit confirmation dialog
        AlertDialog.Builder(this)
            .setTitle("Thoát Survival Mode?")
            .setMessage("Bạn có chắc muốn thoát? Tiến độ sẽ bị mất!")
            .setPositiveButton("Thoát") { _, _ ->
                SurvivalManager.endSession()
                super.onBackPressed()
            }
            .setNegativeButton("Tiếp tục", null)
            .show()
    }
}