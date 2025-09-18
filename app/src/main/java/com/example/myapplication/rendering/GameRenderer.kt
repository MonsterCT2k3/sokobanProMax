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
import com.example.myapplication.entities.Bullet
import com.example.myapplication.entities.BulletDirection
import com.example.myapplication.game.PlayerDirection
import kotlin.math.min
import com.example.myapplication.entities.Monster
import com.example.myapplication.entities.MonsterType

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
    private lateinit var monsterPatrol: Drawable
    private lateinit var monsterStraight: Drawable
    private lateinit var bulletUp: Drawable
    private lateinit var bulletDown: Drawable
    private lateinit var bulletLeft: Drawable
    private lateinit var bulletRight: Drawable
    
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
        monsterPatrol = ContextCompat.getDrawable(context, R.drawable.monster_patrol)
            ?: throw IllegalStateException("monster drawable not found")
        monsterStraight = ContextCompat.getDrawable(context, R.drawable.monster_straight)
            ?: throw IllegalStateException("monster drawable not found")

        // Load bullet drawables for each direction
        bulletUp = ContextCompat.getDrawable(context, R.drawable.bullet_up)
            ?: throw IllegalStateException("bullet_up drawable not found")
        bulletDown = ContextCompat.getDrawable(context, R.drawable.bullet_down)
            ?: throw IllegalStateException("bullet_down drawable not found")
        bulletLeft = ContextCompat.getDrawable(context, R.drawable.bullet_left)
            ?: throw IllegalStateException("bullet_left drawable not found")
        bulletRight = ContextCompat.getDrawable(context, R.drawable.bullet_right)
            ?: throw IllegalStateException("bullet_right drawable not found")
    }

    /**
     * 👹 Lấy drawable cho monster theo type
     */
    private fun getMonsterDrawable(type: MonsterType): Drawable {
        return when (type) {
            MonsterType.PATROL -> monsterPatrol
            MonsterType.CIRCLE -> monsterPatrol  // Tạm dùng chung
            MonsterType.RANDOM -> monsterPatrol  // Tạm dùng chung
            MonsterType.CHASE -> monsterPatrol   // Tạm dùng chung
            MonsterType.STRAIGHT -> monsterStraight
        }
    }

    /**
     * 🎯 Lấy drawable cho bullet theo hướng
     */
    private fun getBulletDrawable(direction: BulletDirection): Drawable {
        return when (direction) {
            BulletDirection.UP -> bulletUp
            BulletDirection.DOWN -> bulletDown
            BulletDirection.LEFT -> bulletLeft
            BulletDirection.RIGHT -> bulletRight
        }
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
    
    fun drawGameBoard(canvas: Canvas, map: Array<CharArray>, playerDirection: PlayerDirection, monsters: List<Monster>) {
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
                // Vẽ quái vật nếu có
                drawMonsters(canvas, monsters, tileSize, offsetX, offsetY)
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

    /**
     * 👹 Vẽ tất cả monsters lên canvas
     */
    private fun drawMonsters(canvas: Canvas, monsters: List<Monster>, tileSize: Int, offsetX: Float, offsetY: Float) {
        monsters.forEach { monster ->
            if (monster.isActive) {
                // 1️⃣ Lấy drawable cho monster
                val monsterDrawable = getMonsterDrawable(monster.type)
                val monsterBitmap = drawableToBitmap(monsterDrawable, tileSize)

                // 2️⃣ Tính vị trí render (smooth position)
                val x = offsetX + monster.currentY * tileSize  // ✅ SỬA: currentY là column
                val y = offsetY + monster.currentX * tileSize  // ✅ SỬA: currentX là row

                // 3️⃣ Vẽ shadow
                canvas.drawRect(x + 2, y + 2, x + tileSize + 2, y + tileSize + 2, shadowPaint)

                // 4️⃣ Vẽ monster
                canvas.drawBitmap(monsterBitmap, x, y, tilePaint)

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

    /**
     * 🎯 Vẽ tất cả bullets lên canvas
     *
     * @param canvas Canvas để vẽ
     * @param bullets Danh sách bullets cần vẽ
     */
    fun drawBullets(canvas: Canvas, bullets: List<Bullet>) {
        bullets.forEach { bullet ->
            if (bullet.isActive) {
                // 🎯 Lấy drawable theo hướng của bullet
                val bulletDrawable = getBulletDrawable(bullet.direction)
                val bulletBitmap = drawableToBitmap(bulletDrawable, 64)

                // Vẽ bullet tại vị trí hiện tại
                canvas.drawBitmap(
                    bulletBitmap,
                    bullet.currentX - 32,  // Center bullet (64/2 = 32)
                    bullet.currentY - 32,  // Center bullet (64/2 = 32)
                    tilePaint
                )

                // Vẽ trail effect theo màu của hướng
                val trailColor = when (bullet.direction) {
                    BulletDirection.UP -> android.graphics.Color.BLUE
                    BulletDirection.DOWN -> android.graphics.Color.RED
                    BulletDirection.LEFT -> android.graphics.Color.GREEN
                    BulletDirection.RIGHT -> android.graphics.Color.YELLOW
                }

                val trailPaint = Paint().apply {
                    color = trailColor
                    alpha = 150  // Tăng độ trong suốt
                }
                canvas.drawCircle(bullet.currentX, bullet.currentY, 5f, trailPaint)
            }
        }
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
