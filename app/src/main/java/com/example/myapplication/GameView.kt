package com.example.myapplication

import android.content.Context
import android.graphics.Canvas
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import androidx.core.content.ContextCompat
import kotlin.math.min
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import com.example.myapplication.managers.LevelManager
import com.example.myapplication.models.Level

class GameView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private var map: Array<CharArray> = arrayOf() // Bản đồ game
    private var playerX: Int = 0 // Tọa độ X của người chơi
    private var playerY: Int = 0 // Tọa độ Y của người chơi
    private val goalPositions = mutableSetOf<Pair<Int, Int>>() // Lưu các vị trí mục tiêu
    private lateinit var wall: Drawable
    private lateinit var box: Drawable
    private lateinit var goal: Drawable
    private lateinit var player: Drawable
    private lateinit var floor: Drawable
    private var currentLevel: Level? = null

    private var gameThread: Thread? = null
    private var isGameRunning = false
    private var targetFPS = 60
    private var frameTimeMillis = 1000L / targetFPS
    private var gameStateChanged = false
    private var frameCount = 0
    private var lastFPSTime = 0L

    private lateinit var tilePaint: Paint
    private lateinit var textPaint: Paint
    private lateinit var shadowPaint: Paint

    private var cachedBitmaps: Map<Char, Bitmap>? = null
    private var cachedTileSize: Int = 0

    // Swipe gesture variables
    private var startX = 0f
    private var startY = 0f
    private var endX = 0f
    private var endY = 0f
    private val minSwipeDistance = 100f // Khoảng cách tối thiểu để nhận diện swipe

    init {
        initGame()
    }

    // Thêm method này
    fun loadLevel(levelId: Int) {
        currentLevel = LevelManager.getLevel(levelId)
        currentLevel?.let { level ->
            map = level.map.map { it.clone() }.toTypedArray()
            val (x, y) = level.getPlayerStartPosition()
            playerX = x
            playerY = y
            goalPositions.clear()
            goalPositions.addAll(level.getGoalPositions())
            gameStateChanged = true
        }
    }

    private fun initGame() {
        // Chỉ load drawable, không load map ở đây nữa
        wall = ContextCompat.getDrawable(context, R.drawable.wall) ?: throw IllegalStateException("wall drawable not found")
        box = ContextCompat.getDrawable(context, R.drawable.box) ?: throw IllegalStateException("box drawable not found")
        goal = ContextCompat.getDrawable(context, R.drawable.goal) ?: throw IllegalStateException("goal drawable not found")
        player = ContextCompat.getDrawable(context, R.drawable.player) ?: throw IllegalStateException("player drawable not found")
        floor = ContextCompat.getDrawable(context, R.drawable.floor) ?: throw IllegalStateException("floor drawable not found")

        initPaints()
    }

    private fun initPaints() {
        tilePaint = Paint().apply {
            isAntiAlias = true
            isFilterBitmap = true
        }

        textPaint = Paint().apply {
            color = Color.WHITE
            textSize = 48f
            typeface = Typeface.DEFAULT_BOLD
            isAntiAlias = true
            textAlign = Paint.Align.CENTER
        }

        shadowPaint = Paint().apply {
            color = Color.BLACK
            alpha = 80
        }
    }

    private fun drawableToBitmap(drawable: Drawable, size: Int): Bitmap {
        val bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        drawable.setBounds(0, 0, size, size)
        drawable.draw(canvas)
        return bitmap
    }

    private inner class GameThread : Thread() {
        override fun run() {
            while(isGameRunning&& !isInterrupted) {
                try {
                    val startTime = System.currentTimeMillis()
                    updateGame()
                    if(gameStateChanged) {
                        post { invalidate() }
                        gameStateChanged = false
                    }

                    val frameTime = System.currentTimeMillis() - startTime
                    if(frameTime < frameTimeMillis) {
                        sleep(frameTimeMillis - frameTime)
                    }
                }catch (e: InterruptedException) {
                    break
                }
            }
        }
    }

    private fun updateGame() {

        //debug FPS
        frameCount++
        val currentTime = System.currentTimeMillis()
        if(currentTime - lastFPSTime >= 1000) {
            println("FPS: $frameCount")
            frameCount = 0
            lastFPSTime = currentTime
        }
        checkWinCondition()
        //updateAnimations() // Nếu có animation, cập nhật ở đây
    }

    private fun checkWinCondition() {
        var allBoxesOnGoals = true
        for((goalX, goalY) in goalPositions){
            if(map[goalX][goalY] != 'B') {
                allBoxesOnGoals = false
                break
            }
        }

        if(allBoxesOnGoals) {
            isGameRunning = false
            post{
                // Hiển thị thông báo chiến thắng
                // Toast.makeText(context, "You Win!", Toast.LENGTH_LONG).show()
            }
        }
    }

    fun startGame(){
        if(!isGameRunning) {
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
        }catch (e: InterruptedException) {
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

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        if(map.isEmpty() || map[0].isEmpty()) return

        val tileSize = min(width / map[0].size, height / map.size)

        if(cachedBitmaps== null || cachedTileSize != tileSize) {
            cachedTileSize = tileSize
            cachedBitmaps = mapOf(
                '#' to drawableToBitmap(wall, tileSize),
                'B' to drawableToBitmap(box, tileSize),
                'G' to drawableToBitmap(goal, tileSize),
                '@' to drawableToBitmap(player, tileSize),
                '.' to drawableToBitmap(floor, tileSize)
            )
        }
        val bitmaps = cachedBitmaps!!

        // Vẽ background toàn màn hình
        canvas.drawColor(Color.parseColor("#2C3E50")) // Nền xanh đậm

        // Tính toán để center game board
        val boardWidth = map[0].size * tileSize
        val boardHeight = map.size * tileSize
        val offsetX = (width - boardWidth) / 2f
        val offsetY = (height - boardHeight) / 2f

        // Vẽ game board ở chính giữa
        for(i in map.indices) {
            for(j in map[i].indices) {
                val x = offsetX + j * tileSize.toFloat()
                val y = offsetY + i * tileSize.toFloat()

                // Vẽ bóng
                if(map[i][j]!= '.'){
                    canvas.drawRect(x+3, y+3, x+tileSize+3, y+tileSize+3, shadowPaint)
                }
                val bitmap = bitmaps[map[i][j]] ?: bitmaps['.']!!
                canvas.drawBitmap(bitmap, x, y, tilePaint)
            }
        }
        drawGameUI(canvas)
    }

    private fun drawGameUI(canvas: Canvas) {
        canvas.drawText("Sokoban Game", width /2f, 120f, textPaint)

        val instructionsPaint = Paint().apply {
            textSize = 40f
            color = Color.LTGRAY
        }
        canvas.drawText("Vuốt để di chuyển - Đẩy hộp vào mục tiêu", width /2f-280, height - 60f, instructionsPaint)
    }

    private fun getTileDrawable(tile: Char): Drawable {
        return when (tile) {
            '#' -> wall
            'B' -> box
            'G' -> goal
            '@' -> player
            else -> floor
        }
    }

    fun movePlayer(dx: Int, dy: Int) {
        val newX = playerX + dx
        val newY = playerY + dy

        // Kiểm tra di chuyển hợp lệ
        if (isValidMove(newX, newY)) {
            if (map[newX][newY] == 'B') {
                // Nếu ô đích có hộp, kiểm tra ô tiếp theo
                val boxNewX = newX + dx
                val boxNewY = newY + dy
                if (isValidMove(boxNewX, boxNewY)) {
                    // Di chuyển hộp
                    map[boxNewX][boxNewY] = 'B'
                    map[newX][newY] = '@'
                    map[playerX][playerY] = if (goalPositions.contains(Pair(playerX, playerY))) 'G' else '.'
                    playerX = newX
                    playerY = newY
                    gameStateChanged = true
                }
            } else {
                // Di chuyển người chơi
                map[newX][newY] = '@'
                map[playerX][playerY] = if (goalPositions.contains(Pair(playerX, playerY))) 'G' else '.'
                playerX = newX
                playerY = newY
                gameStateChanged = true
            }

            if (!isGameRunning) {
                // Nếu game đã kết thúc, không cần cập nhật liên tục
                invalidate()
            }
        }
    }

    private fun isValidMove(x: Int, y: Int): Boolean {
        return x >= 0 && x < map.size && y >= 0 && y < map[0].size && map[x][y] != '#'
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                // Lưu vị trí bắt đầu chạm
                startX = event.x
                startY = event.y
                return true
            }

            MotionEvent.ACTION_UP -> {
                // Lưu vị trí kết thúc chạm
                endX = event.x
                endY = event.y
                handleSwipe()
                return true
            }
        }
        return super.onTouchEvent(event)
    }

    private fun handleSwipe() {
        val deltaX = endX - startX
        val deltaY = endY - startY

        // Kiểm tra khoảng cách swipe có đủ dài không
        val distance = kotlin.math.sqrt((deltaX * deltaX + deltaY * deltaY).toDouble()).toFloat()
        if (distance < minSwipeDistance) {
            return // Quá ngắn, không phải swipe
        }

        // Xác định hướng swipe
        val absX = kotlin.math.abs(deltaX)
        val absY = kotlin.math.abs(deltaY)

        if (absX > absY) {
            // Swipe ngang (trái/phải)
            if (deltaX > 0) {
                // Swipe phải
                movePlayer(0, 1)
            } else {
                // Swipe trái
                movePlayer(0, -1)
            }
        } else {
            // Swipe dọc (lên/xuống)
            if (deltaY > 0) {
                // Swipe xuống
                movePlayer(1, 0)
            } else {
                // Swipe lên
                movePlayer(-1, 0)
            }
        }
    }


    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        startGame()
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        stopGame()
    }
}