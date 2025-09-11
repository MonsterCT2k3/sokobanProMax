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

/**
 * 🎮 GameView - Main game view class
 * 
 * Đây là lớp chính quản lý toàn bộ game Sokoban.
 * Nó hoạt động như một coordinator, điều phối các component:
 * - GameLogic: Xử lý logic game (di chuyển, win condition)
 * - GameRenderer: Vẽ game board và UI
 * - BackgroundManager: Quản lý background animation
 * - InputHandler: Xử lý touch input và swipe gestures
 * 
 * @param context Android context
 * @param attrs XML attributes
 * @param defStyleAttr Default style attributes
 */
class GameView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr),
    GameLogic.GameStateListener,        // Lắng nghe thay đổi trạng thái game
    InputHandler.PlayerMoveListener {   // Lắng nghe input từ user

    // ===== CORE COMPONENTS =====
    // Mỗi component có nhiệm vụ riêng biệt, tách biệt trách nhiệm
    private val gameLogic = GameLogic()                    // 🎯 Xử lý logic game
    private val gameRenderer = GameRenderer(context)       // 🖼️ Vẽ game board và UI
    private val backgroundManager = BackgroundManager(context) // 🎨 Quản lý background
    private val inputHandler = InputHandler()              // 👆 Xử lý touch input
    
    // ===== GAME THREAD MANAGEMENT =====
    // Game chạy trên thread riêng để không block UI thread
    private var gameThread: Thread? = null                 // Thread chạy game loop
    private var isGameRunning = false                      // Trạng thái game đang chạy
    private var targetFPS = 60                             // Mục tiêu 60 FPS
    private var frameTimeMillis = 1000L / targetFPS        // Thời gian mỗi frame (≈16.67ms)
    private var gameStateChanged = false                   // Flag báo cần redraw
    private var frameCount = 0                             // Đếm frame để tính FPS
    private var lastFPSTime = 0L                           // Thời gian lần cuối tính FPS
    
    // ===== ANIMATION =====
    private var animationTime = 0f                         // Thời gian để tính animation

    init {
        initGame()
    }

    /**
     * 🔧 Khởi tạo game
     * Setup các listener để các component có thể giao tiếp với nhau
     */
    private fun initGame() {
        // Setup listeners để tạo communication giữa các component
        gameLogic.setGameStateListener(this)        // GameView lắng nghe thay đổi từ GameLogic
        inputHandler.setPlayerMoveListener(this)    // GameView lắng nghe input từ InputHandler
    }

    // ===== PUBLIC API METHODS =====
    // Các method public để Activity/Fragment có thể điều khiển game

    fun loadLevel(levelId: Int) {
        gameLogic.loadLevel(levelId)
    }

    fun setBackgroundImage(resourceId: Int, scrollType: BackgroundManager.BackgroundScrollType = BackgroundManager.BackgroundScrollType.PARALLAX_HORIZONTAL) {
        backgroundManager.setBackgroundImage(resourceId, scrollType)
        gameStateChanged = true  // Báo cần redraw
    }
    

    fun setBackgroundImageFromAssets(fileName: String, scrollType: BackgroundManager.BackgroundScrollType = BackgroundManager.BackgroundScrollType.PARALLAX_HORIZONTAL) {
        backgroundManager.setBackgroundImageFromAssets(fileName, scrollType)
        gameStateChanged = true  // Báo cần redraw
    }
    
    /**
     * ⚡ Điều chỉnh tốc độ animation background
     * @param speed Tốc độ (VD: 0.5 = chậm, 2.0 = nhanh)
     */
    fun setBackgroundSpeed(speed: Float) {
        backgroundManager.setBackgroundSpeed(speed)
    }
    
    /**
     * 🔄 Thay đổi kiểu animation background
     * @param type Loại animation (PARALLAX, ZOOM, ROTATING, ...)
     */
    fun setBackgroundScrollType(type: BackgroundManager.BackgroundScrollType) {
        backgroundManager.setBackgroundScrollType(type)
        gameStateChanged = true  // Báo cần redraw
    }

    fun pauseBackgroundAnimation() {
        backgroundManager.pauseBackgroundAnimation()
    }

    fun resumeBackgroundAnimation(speed: Float = 0.5f) {
        backgroundManager.resumeBackgroundAnimation(speed)
    }


    fun startGame() {
        if (!isGameRunning) {
            isGameRunning = true
            gameThread = GameThread()   // Tạo thread mới
            gameThread?.start()         // Bắt đầu game loop
        }
    }


    fun stopGame() {
        isGameRunning = false           // Set flag để thread thoát loop
        gameThread?.interrupt()         // Interrupt thread
        try {
            gameThread?.join()          // Đợi thread kết thúc
        } catch (e: InterruptedException) {
            Thread.currentThread().interrupt()
        }
        gameThread = null               // Clean up
    }


    fun pauseGame() {
        isGameRunning = false
    }

    fun resumeGame() {
        if (!isGameRunning) {
            startGame()
        }
    }

    /**
     * 🔄 GameThread - Thread chạy game loop
     * 
     * Đây là "trái tim" của game, chạy liên tục với 60 FPS:
     * 1. Update game logic
     * 2. Check nếu cần redraw → gọi invalidate()
     * 3. Sleep để maintain 60 FPS
     */
    private inner class GameThread : Thread() {
        override fun run() {
            while (isGameRunning && !isInterrupted) {
                try {
                    val startTime = System.currentTimeMillis()
                    
                    // 1. Update game (animation, logic, etc.)
                    updateGame()
                    
                    // 2. Nếu có thay đổi → trigger redraw trên UI thread
                    if (gameStateChanged) {
                        post { invalidate() }        // Schedule onDraw() trên UI thread
                        gameStateChanged = false     // Reset flag
                    }

                    // 3. Sleep để maintain 60 FPS (16.67ms/frame)
                    val frameTime = System.currentTimeMillis() - startTime
                    if (frameTime < frameTimeMillis) {
                        sleep(frameTimeMillis - frameTime)  // Sleep phần thời gian còn lại
                    }
                } catch (e: InterruptedException) {
                    break  // Thread bị interrupt → thoát loop
                }
            }
        }
    }

    /**
     * 🔄 Update game mỗi frame
     * 
     * Method này được gọi 60 lần/giây từ GameThread:
     * 1. Tính FPS để debug
     * 2. Update animation time
     * 3. Update background animation
     */
    private fun updateGame() {
        // ===== DEBUG FPS =====
        frameCount++  // Đếm số frame
        val currentTime = System.currentTimeMillis()

        // Mỗi giây in ra FPS để debug
        if (currentTime - lastFPSTime >= 1000) {
            println("🎮 Game FPS: $frameCount")  // Should be ~60
            frameCount = 0
            lastFPSTime = currentTime
        }

        // ===== UPDATE ANIMATION =====
        animationTime = currentTime.toFloat()  // Thời gian cho background animation
        
        // Update background animation và check cần redraw không
        if (backgroundManager.updateAnimation()) {
            gameStateChanged = true  // Background có animation → cần redraw
        }
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        
        // Thông báo kích thước mới cho các component
        gameRenderer.setScreenSize(w, h)       // Để tính toán tile size và layout
        backgroundManager.setScreenSize(w, h)  // Để scale background cho phù hợp
    }

    /**
     * 🎨 Vẽ toàn bộ game lên Canvas
     * 
     * Method này được gọi mỗi khi cần redraw (khi gọi invalidate()).
     * Thứ tự vẽ rất quan trọng: Background → Game Board → UI
     * 
     * @param canvas Canvas để vẽ lên
     */
    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        // 1. 🎨 Vẽ background trước (nền)
        backgroundManager.drawBackground(canvas, animationTime)

        // 2. 🎮 Vẽ game board (tiles: wall, box, player, goal)
        //    Chỉ vẽ nếu đã load level
        if (!gameLogic.isMapEmpty()) {
            gameRenderer.drawGameBoard(canvas, gameLogic.getMap(), gameLogic.getPlayerDirection())
        }
        
        // 3. 🖼️ Vẽ UI elements cuối cùng (trên cùng)
        //    Title, instructions, score, etc.
        gameRenderer.drawGameUI(canvas)
    }


    override fun onTouchEvent(event: MotionEvent): Boolean {
        return inputHandler.handleTouchEvent(event) || super.onTouchEvent(event)
    }


    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        startGame()  // Bắt đầu game loop
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        stopGame()   // Dừng game loop và clean up
    }

    // ===== CALLBACK IMPLEMENTATIONS =====
    
    /**
     * 🎯 GameLogic.GameStateListener - Lắng nghe thay đổi từ GameLogic
     */
    
    /**
     * 🔄 Được gọi khi game state thay đổi
     * VD: Player di chuyển, box được đẩy, level reset
     */
    override fun onGameStateChanged() {
        gameStateChanged = true // Báo cần redraw
        
        if (!isGameRunning) {
            post { invalidate() }
        }
        // Nếu game thread đang chạy → nó sẽ tự động redraw
    }


    override fun onGameWon() {
        isGameRunning = false  // Dừng game loop
        post {
            // TODO: Hiển thị thông báo chiến thắng
            // Toast.makeText(context, "🎉 You Win!", Toast.LENGTH_LONG).show()
            // Hoặc show dialog chuyển level tiếp theo
        }
    }

    override fun onPlayerMove(dx: Int, dy: Int) {
        gameLogic.movePlayer(dx, dy)  // Delegate cho GameLogic xử lý
    }

    fun resetLevel() {
        gameLogic.resetLevel()
    }

    fun isGameWon(): Boolean = gameLogic.isGameWon()
    

    fun getProgressPercentage(): Float = gameLogic.getProgressPercentage()

    fun getCurrentLevel() = gameLogic.getCurrentLevel()
}