package com.example.myapplication

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity

class GameActivity : AppCompatActivity() {

    private lateinit var gameView: GameView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        gameView = GameView(this)
        setContentView(gameView)

        // Lấy level ID từ intent
        val levelId = intent.getIntExtra("LEVEL_ID", 1)
        gameView.loadLevel(levelId)
    }

    override fun onResume() {
        super.onResume()
        gameView.resumeGame()
    }

    override fun onPause() {
        super.onPause()
        gameView.pauseGame()
    }

    override fun onDestroy() {
        super.onDestroy()
        gameView.stopGame()
    }
}