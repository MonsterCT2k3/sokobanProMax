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

/**
 * 🎮 GameModeSelectionActivity - Màn hình chọn chế độ chơi
 *
 * Cho phép người chơi lựa chọn giữa:
 * - Chơi theo màn (Classic Mode) - chơi từng màn theo thứ tự
 * - Chế độ sinh tử (Survival Mode) - chế độ mới (sẽ phát triển sau)
 */
class GameModeSelectionActivity : AppCompatActivity() {
    private lateinit var musicManager: MusicManager
    private var isNavigatingToClassic = false
    private var isNavigatingToSurvival = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_game_mode_selection)

        // Khởi tạo Managers Singleton
        musicManager = MusicManager.getInstance(this)

        // Thiết lập các sự kiện cho các nút
        setupButtons()

        // Thêm animation
        setupAnimations()
    }

    private fun setupButtons() {
        // 🎯 Nút Chơi theo màn - chuyển đến LevelSelectionActivity
        findViewById<Button>(R.id.btnClassicMode).setOnClickListener {
            isNavigatingToClassic = true
            val intent = Intent(this, LevelSelectionActivity::class.java)
            startActivity(intent)
        }

        // 🏃 Nút Chế độ sinh tử - chuyển đến SurvivalGameActivity
        findViewById<Button>(R.id.btnSurvivalMode).setOnClickListener {
            isNavigatingToSurvival = true
            val intent = Intent(this, SurvivalGameActivity::class.java)
            startActivity(intent)
        }

        // 🏠 Nút Quay lại - về menu chính
        findViewById<Button>(R.id.btnBackToMenu).setOnClickListener {
            finish() // Quay về MenuActivity
        }
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    private fun setupAnimations() {
        // Animation cho title
        val titleAnimation = AnimationUtils.loadAnimation(this, R.anim.fade_in)
        findViewById<TextView>(R.id.titleGameMode).startAnimation(titleAnimation)

        // Animation cho mode buttons
        val buttonAnimation = AnimationUtils.loadAnimation(this, R.anim.slide_up)
        findViewById<LinearLayout>(R.id.modeButtonsContainer).startAnimation(buttonAnimation)

        // Animation cho back button
        val backButtonAnimation = AnimationUtils.loadAnimation(this, R.anim.slide_up)
        backButtonAnimation.startOffset = 200 // Delay một chút
        findViewById<Button>(R.id.btnBackToMenu).startAnimation(backButtonAnimation)
    }

    override fun onResume() {
        super.onResume()
        // Reset flags
        isNavigatingToClassic = false
        isNavigatingToSurvival = false

        // Tiếp tục phát nhạc menu (nếu không bị tạm dừng)
        musicManager.resumeMusic()
    }

    override fun onPause() {
        super.onPause()
        // Tạm dừng nhạc khi navigate đến classic mode hoặc survival mode
        if (!isNavigatingToClassic && !isNavigatingToSurvival) {
            musicManager.pauseMusic()
        }
    }
}
