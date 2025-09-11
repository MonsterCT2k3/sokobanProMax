package com.example.myapplication

import android.content.Intent
import android.os.Bundle
import android.view.animation.AnimationUtils
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class MenuActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_menu)

        // Thiết lập các sự kiện cho các nút
        setupButtons()
        
        // Thêm animation
        setupAnimations()
    }

    private fun setupButtons() {
        // Nút Play - chuyển đến màn hình game
        findViewById<Button>(R.id.btnPlay).setOnClickListener {
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

        // Nút Other - các tùy chọn khác
        findViewById<Button>(R.id.btnOther).setOnClickListener {
            showToast("Các tùy chọn khác sẽ được hiển thị ở đây!")
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

    override fun onBackPressed() {
        super.onBackPressed()
        // Thoát ứng dụng khi nhấn nút back ở màn hình menu
        finishAffinity()
    }
} 