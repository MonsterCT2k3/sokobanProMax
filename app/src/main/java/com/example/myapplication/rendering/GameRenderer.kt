package com.example.myapplication.rendering

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.drawable.Drawable
import androidx.core.content.ContextCompat
import com.example.myapplication.R
import com.example.myapplication.game.PlayerDirection
import kotlin.math.min

class GameRenderer(private val context: Context) {
    
    // Drawable resources
    private lateinit var wall: Drawable
    private lateinit var box: Drawable
    private lateinit var goal: Drawable
    private lateinit var floor: Drawable
    private lateinit var playerUp: Drawable
    private lateinit var playerDown: Drawable
    private lateinit var playerLeft: Drawable
    private lateinit var playerRight: Drawable
    
    // Paint objects
    private lateinit var tilePaint: Paint
    private lateinit var textPaint: Paint
    private lateinit var shadowPaint: Paint
    
    // Cached bitmaps for performance
    private var cachedBitmaps: Map<Char, Bitmap>? = null
    private var cachedTileSize: Int = 0
    
    // Screen dimensions
    private var screenWidth = 0
    private var screenHeight = 0
    
    init {
        initResources()
        initPaints()
    }
    
    private fun initResources() {
        wall = ContextCompat.getDrawable(context, R.drawable.wall)
            ?: throw IllegalStateException("wall drawable not found")
        box = ContextCompat.getDrawable(context, R.drawable.box)
            ?: throw IllegalStateException("box drawable not found")
        goal = ContextCompat.getDrawable(context, R.drawable.goal)
            ?: throw IllegalStateException("goal drawable not found")
        playerUp = ContextCompat.getDrawable(context, R.drawable.hero_up)
            ?: throw IllegalStateException("player drawable not found")
        playerDown = ContextCompat.getDrawable(context, R.drawable.hero_down)
            ?: throw IllegalStateException("player drawable not found")
        playerLeft = ContextCompat.getDrawable(context, R.drawable.hero_left)
            ?: throw IllegalStateException("player drawable not found")
        playerRight = ContextCompat.getDrawable(context, R.drawable.hero_right)
            ?: throw IllegalStateException("player drawable not found")
        floor = ContextCompat.getDrawable(context, R.drawable.floor)
            ?: throw IllegalStateException("floor drawable not found")
    }

    // Thêm method mới vào GameRenderer:
    private fun getCurrentPlayerDrawable(direction: PlayerDirection): Drawable {
        return when (direction) {
            PlayerDirection.UP -> playerUp
            PlayerDirection.DOWN -> playerDown
            PlayerDirection.LEFT -> playerLeft
            PlayerDirection.RIGHT -> playerRight
        }
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
    
    fun setScreenSize(width: Int, height: Int) {
        screenWidth = width
        screenHeight = height
    }
    
    fun drawGameBoard(canvas: Canvas, map: Array<CharArray>, playerDirection: PlayerDirection) {
        if (map.isEmpty() || map[0].isEmpty()) return

        val tileSize = min(screenWidth / map[0].size, screenHeight / map.size)

        // Update cached bitmaps if needed
        if (cachedBitmaps == null || cachedTileSize != tileSize) {
            cachedTileSize = tileSize
            cachedBitmaps = mapOf(
                '#' to drawableToBitmap(wall, tileSize),
                'B' to drawableToBitmap(box, tileSize),
                'G' to drawableToBitmap(goal, tileSize),
                '.' to drawableToBitmap(floor, tileSize)
            )
        }
        val bitmaps = cachedBitmaps!!

        // Tính toán để center game board
        val boardWidth = map[0].size * tileSize
        val boardHeight = map.size * tileSize
        val offsetX = (screenWidth - boardWidth) / 2f
        val offsetY = (screenHeight - boardHeight) / 2f

        // Vẽ game board ở chính giữa
        for (i in map.indices) {
            for (j in map[i].indices) {
                val x = offsetX + j * tileSize.toFloat()
                val y = offsetY + i * tileSize.toFloat()

                // Vẽ bóng
                if (map[i][j] != '.') {
                    canvas.drawRect(x + 3, y + 3, x + tileSize + 3, y + tileSize + 3, shadowPaint)
                }
                // Vẽ người chơi với hướng hiện tại
                if (map[i][j] == '@') {
                    val playerDrawable = getCurrentPlayerDrawable(playerDirection)
                    val playerBitmap = drawableToBitmap(playerDrawable, tileSize)
                    canvas.drawBitmap(playerBitmap, x, y, tilePaint)
                }else{
                    val bitmap = bitmaps[map[i][j]] ?: bitmaps['.']!!
                    canvas.drawBitmap(bitmap, x, y, tilePaint)
                }
            }
        }
    }
    
    fun drawGameUI(canvas: Canvas) {
        // Vẽ tiêu đề game
        canvas.drawText("Sokoban Game", screenWidth / 2f, 120f, textPaint)

        // Vẽ hướng dẫn
        val instructionsPaint = Paint().apply {
            textSize = 40f
            color = Color.LTGRAY
            textAlign = Paint.Align.CENTER
        }
        canvas.drawText(
            "Vuốt để di chuyển - Đẩy hộp vào mục tiêu", 
            screenWidth / 2f, 
            screenHeight - 60f, 
            instructionsPaint
        )
    }
    
    private fun drawableToBitmap(drawable: Drawable, size: Int): Bitmap {
        val bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        drawable.setBounds(0, 0, size, size)
        drawable.draw(canvas)
        return bitmap
    }
    
    fun getTileDrawable(tile: Char): Drawable {
        return when (tile) {
            '#' -> wall
            'B' -> box
            'G' -> goal
            else -> floor
        }
    }
    
    fun calculateTileSize(map: Array<CharArray>): Int {
        if (map.isEmpty() || map[0].isEmpty()) return 0
        return min(screenWidth / map[0].size, screenHeight / map.size)
    }
    
    fun calculateBoardOffset(map: Array<CharArray>): Pair<Float, Float> {
        if (map.isEmpty() || map[0].isEmpty()) return Pair(0f, 0f)
        
        val tileSize = calculateTileSize(map)
        val boardWidth = map[0].size * tileSize
        val boardHeight = map.size * tileSize
        val offsetX = (screenWidth - boardWidth) / 2f
        val offsetY = (screenHeight - boardHeight) / 2f
        
        return Pair(offsetX, offsetY)
    }
}
