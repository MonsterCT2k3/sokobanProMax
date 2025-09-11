package com.example.myapplication

import android.content.Context
import android.graphics.Canvas
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import com.example.myapplication.game.GameLogic
import com.example.myapplication.input.InputHandler
import com.example.myapplication.rendering.BackgroundManager
import com.example.myapplication.rendering.GameRenderer

class GameView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr),
    GameLogic.GameStateListener,
    InputHandler.PlayerMoveListener {

    // Core components
    private val gameLogic = GameLogic()
    private val gameRenderer = GameRenderer(context)
    private val backgroundManager = BackgroundManager(context)
    private val inputHandler = InputHandler()
    
    // Game thread management
    private var gameThread: Thread? = null
    private var isGameRunning = false
    private var targetFPS = 60
    private var frameTimeMillis = 1000L / targetFPS
    private var gameStateChanged = false
    private var frameCount = 0
    private var lastFPSTime = 0L
    
    // Animation time
    private var animationTime = 0f

    init {
        initGame()
    }

    private fun initGame() {
        // Setup listeners
        gameLogic.setGameStateListener(this)
        inputHandler.setPlayerMoveListener(this)
    }

    // Public API methods
    fun loadLevel(levelId: Int) {
        gameLogic.loadLevel(levelId)
    }
    
    fun setBackgroundImage(resourceId: Int, scrollType: BackgroundManager.BackgroundScrollType = BackgroundManager.BackgroundScrollType.PARALLAX_HORIZONTAL) {
        backgroundManager.setBackgroundImage(resourceId, scrollType)
        gameStateChanged = true
    }
    
    fun setBackgroundImageFromAssets(fileName: String, scrollType: BackgroundManager.BackgroundScrollType = BackgroundManager.BackgroundScrollType.PARALLAX_HORIZONTAL) {
        backgroundManager.setBackgroundImageFromAssets(fileName, scrollType)
        gameStateChanged = true
    }
    
    fun setBackgroundSpeed(speed: Float) {
        backgroundManager.setBackgroundSpeed(speed)
    }
    
    fun setBackgroundScrollType(type: BackgroundManager.BackgroundScrollType) {
        backgroundManager.setBackgroundScrollType(type)
        gameStateChanged = true
    }
    
    fun pauseBackgroundAnimation() {
        backgroundManager.pauseBackgroundAnimation()
    }
    
    fun resumeBackgroundAnimation(speed: Float = 0.5f) {
        backgroundManager.resumeBackgroundAnimation(speed)
    }

    // Game thread management
    fun startGame() {
        if (!isGameRunning) {
            isGameRunning = true
            gameThread = GameThread()
            gameThread?.start()
        }
    }

    fun stopGame() {
        isGameRunning = false
        gameThread?.interrupt()
        try {
            gameThread?.join()
        } catch (e: InterruptedException) {
            Thread.currentThread().interrupt()
        }
        gameThread = null
    }

    fun pauseGame() {
        isGameRunning = false
    }

    fun resumeGame() {
        if (!isGameRunning) {
            startGame()
        }
    }

    // Game thread class
    private inner class GameThread : Thread() {
        override fun run() {
            while (isGameRunning && !isInterrupted) {
                try {
                    val startTime = System.currentTimeMillis()
                    updateGame()
                    if (gameStateChanged) {
                        post { invalidate() }
                        gameStateChanged = false
                    }

                    val frameTime = System.currentTimeMillis() - startTime
                    if (frameTime < frameTimeMillis) {
                        sleep(frameTimeMillis - frameTime)
                    }
                } catch (e: InterruptedException) {
                    break
                }
            }
        }
    }

    private fun updateGame() {
        // Debug FPS
        frameCount++
        val currentTime = System.currentTimeMillis()

        if (currentTime - lastFPSTime >= 1000) {
            println("FPS: $frameCount")
            frameCount = 0
            lastFPSTime = currentTime
        }

        // Update animation time
        animationTime = currentTime.toFloat()
        
        // Update background animation
        if (backgroundManager.updateAnimation()) {
            gameStateChanged = true
        }
    }

    // View lifecycle
    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        gameRenderer.setScreenSize(w, h)
        backgroundManager.setScreenSize(w, h)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        // 1. Draw background first
        backgroundManager.drawBackground(canvas, animationTime)

        // 2. Draw game board if map is loaded
        if (!gameLogic.isMapEmpty()) {
            gameRenderer.drawGameBoard(canvas, gameLogic.getMap())
        }
        
        // 3. Draw UI elements
        gameRenderer.drawGameUI(canvas)
    }

    // Input handling
    override fun onTouchEvent(event: MotionEvent): Boolean {
        return inputHandler.handleTouchEvent(event) || super.onTouchEvent(event)
    }

    // Lifecycle methods
    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        startGame()
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        stopGame()
    }

    // GameLogic.GameStateListener implementation
    override fun onGameStateChanged() {
        gameStateChanged = true
        if (!isGameRunning) {
            // Nếu game đã kết thúc, không cần cập nhật liên tục
            post { invalidate() }
        }
    }

    override fun onGameWon() {
        isGameRunning = false
        post {
            // Hiển thị thông báo chiến thắng
            // Toast.makeText(context, "You Win!", Toast.LENGTH_LONG).show()
        }
    }

    // InputHandler.PlayerMoveListener implementation
    override fun onPlayerMove(dx: Int, dy: Int) {
        gameLogic.movePlayer(dx, dy)
    }
    
    // Utility methods for external access
    fun resetLevel() {
        gameLogic.resetLevel()
    }
    
    fun isGameWon(): Boolean = gameLogic.isGameWon()
    
    fun getProgressPercentage(): Float = gameLogic.getProgressPercentage()
    
    fun getCurrentLevel() = gameLogic.getCurrentLevel()
}