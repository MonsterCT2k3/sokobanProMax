package com.example.myapplication

import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Lấy tham chiếu đến GameView
        val gameView: GameView = findViewById(R.id.gameView)

        // Gán sự kiện cho các nút điều khiển
        findViewById<Button>(R.id.buttonUp).setOnClickListener { gameView.movePlayer(-1, 0) }
        findViewById<Button>(R.id.buttonDown).setOnClickListener { gameView.movePlayer(1, 0) }
        findViewById<Button>(R.id.buttonLeft).setOnClickListener { gameView.movePlayer(0, -1) }
        findViewById<Button>(R.id.buttonRight).setOnClickListener { gameView.movePlayer(0, 1) }
    }
}