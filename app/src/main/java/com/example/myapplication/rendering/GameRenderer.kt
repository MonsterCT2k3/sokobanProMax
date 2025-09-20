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
    
    // Monster size constants - dễ dàng điều chỉnh
    companion object {
        private const val MONSTER_WIDTH_RATIO = 1.0f   // 70% chiều rộng tile
        private const val MONSTER_HEIGHT_RATIO = 1.0f  // 100% chiều cao tile (giữ nguyên)
    }
    
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
    private lateinit var monsterPaint: Paint
    
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
        monsterStraight = ContextCompat.getDrawable(context, R.drawable.zombie)
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
            MonsterType.BOUNCE -> monsterStraight  // Tạm dùng zombie sprite cho bounce
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

        monsterPaint = Paint().apply {
            isAntiAlias = true
            isFilterBitmap = true
            alpha = 255
            // Tối ưu cho transparency hoàn hảo
            isDither = true
            // Về lại SRC_OVER với settings tối ưu
            xfermode = null  // SRC_OVER default - tốt nhất cho PNG với alpha
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

        // BƯỚC 1: Vẽ tất cả tiles trước (nền, tường, hộp, mục tiêu)
        for (i in map.indices) {
            for (j in map[i].indices) {
                val x = offsetX + j * tileSize.toFloat()
                val y = offsetY + i * tileSize.toFloat()

                // Vẽ bóng cho non-floor tiles
                if (map[i][j] != '.') {
                    canvas.drawRect(x + 3, y + 3, x + tileSize + 3, y + tileSize + 3, shadowPaint)
                }

                // Vẽ tile (tường, hộp, mục tiêu, sàn)
                val bitmap = bitmaps[map[i][j]] ?: bitmaps['.']!!
                canvas.drawBitmap(bitmap, x, y, tilePaint)
            }
        }

        // BƯỚC 2: Vẽ entities (player + monsters) theo thứ tự depth (Y-coordinate)
        drawEntitiesWithDepthSort(canvas, map, monsters, playerDirection, tileSize, offsetX, offsetY)
    }

    /**
     * 🎭 Vẽ tất cả entities (player + monsters) theo thứ tự depth sorting
     * Entities ở phía dưới (Y lớn hơn) sẽ được vẽ sau để che entities ở phía trên
     */
    private fun drawEntitiesWithDepthSort(canvas: Canvas, map: Array<CharArray>, monsters: List<Monster>, 
                                        playerDirection: PlayerDirection, tileSize: Int, offsetX: Float, offsetY: Float) {
        
        // 1️⃣ Tạo danh sách tất cả entities với thông tin depth
        data class EntityToDraw(
            val type: String,  // "player" hoặc "monster"
            val x: Float,
            val y: Float, 
            val depth: Int,    // Y-coordinate cho sorting (row index)
            val monster: Monster? = null
        )
        
        val entitiesToDraw = mutableListOf<EntityToDraw>()
        
        // 2️⃣ Thêm player vào danh sách
        for (i in map.indices) {
            for (j in map[i].indices) {
                if (map[i][j] == '@') {
                    val x = offsetX + j * tileSize.toFloat()
                    val y = offsetY + i * tileSize.toFloat()
                    entitiesToDraw.add(EntityToDraw("player", x, y, i))  // i là row = depth
                }
            }
        }
        
        // 3️⃣ Thêm monsters vào danh sách
        monsters.forEach { monster ->
            if (monster.isActive) {
                val x = offsetX + monster.currentY * tileSize.toFloat()  // Convert tileSize to Float
                val y = offsetY + monster.currentX * tileSize.toFloat()  // Convert tileSize to Float
                entitiesToDraw.add(EntityToDraw("monster", x, y, monster.currentX.toInt(), monster))  // Convert currentX to Int for depth
            }
        }
        
        // 4️⃣ Sort theo depth (Y-coordinate): entities ở trên vẽ trước, ở dưới vẽ sau
        entitiesToDraw.sortBy { it.depth }
        
        // 5️⃣ Vẽ theo thứ tự đã sort
        entitiesToDraw.forEach { entity ->
            when (entity.type) {
                "player" -> {
                    val playerDrawable = getCurrentPlayerDrawable(playerDirection)
                    val playerBitmap = drawableToBitmap(playerDrawable, tileSize)
                    canvas.drawBitmap(playerBitmap, entity.x, entity.y, tilePaint)
                }
                "monster" -> {
                    entity.monster?.let { monster ->
                        drawSingleMonster(canvas, monster, entity.x, entity.y, tileSize)
                    }
                }
            }
        }
    }

    /**
     * 👹 Vẽ một monster đơn lẻ tại vị trí cụ thể
     */
    private fun drawSingleMonster(canvas: Canvas, monster: Monster, x: Float, y: Float, tileSize: Int) {
        // 1️⃣ KHÔNG vẽ floor tile - để monster blend trực tiếp với nền đã có
        // Floor tile đã được vẽ trong BƯỚC 1 của drawGameBoard

        // 2️⃣ Lấy drawable cho monster
        val monsterDrawable = getMonsterDrawable(monster.type)
        
        // 3️⃣ Tùy chỉnh kích thước monster theo constants
        val monsterWidth = (tileSize * MONSTER_WIDTH_RATIO).toInt()   // Chiều rộng tùy chỉnh
        val monsterHeight = (tileSize * MONSTER_HEIGHT_RATIO).toInt() // Chiều cao tùy chỉnh
        
        val monsterBitmap = drawableToBitmapCustomSize(monsterDrawable, monsterWidth, monsterHeight)
        
        // 4️⃣ Tính vị trí center để monster không bị lệch
        val centerOffsetX = (tileSize - monsterWidth) / 2f
        val drawX = x + centerOffsetX

        // 5️⃣ Tạm thời tắt shadow để test monster hoàn toàn trong suốt
        // TODO: Có thể bật lại sau khi đã test
        /*
        canvas.drawRect(drawX + 2, y + 2, drawX + monsterWidth + 2, y + monsterHeight + 2, shadowPaint)
        */

        // 6️⃣ Vẽ monster với alpha transparency
        canvas.drawBitmap(monsterBitmap, drawX, y, monsterPaint)
    }

    /**
     * 👹 Vẽ tất cả monsters lên canvas (method cũ - giữ lại để tương thích)
     */
    private fun drawMonsters(canvas: Canvas, monsters: List<Monster>, tileSize: Int, offsetX: Float, offsetY: Float) {
        monsters.forEach { monster ->
            if (monster.isActive) {
                // 1️⃣ Lấy drawable cho monster
                val monsterDrawable = getMonsterDrawable(monster.type)
                
                // 2️⃣ Tính vị trí render (smooth position)
                val x = offsetX + monster.currentY * tileSize.toFloat()  // currentY là column
                val y = offsetY + monster.currentX * tileSize.toFloat()  // currentX là row

                // 3️⃣ Tùy chỉnh kích thước monster theo constants
                val monsterWidth = (tileSize * MONSTER_WIDTH_RATIO).toInt()   // Chiều rộng tùy chỉnh
                val monsterHeight = (tileSize * MONSTER_HEIGHT_RATIO).toInt() // Chiều cao tùy chỉnh
                
                val monsterBitmap = drawableToBitmapCustomSize(monsterDrawable, monsterWidth, monsterHeight)
                
                // 4️⃣ Tính vị trí center để monster không bị lệch
                val centerOffsetX = (tileSize - monsterWidth) / 2f
                val drawX = x + centerOffsetX

                // 5️⃣ Vẽ shadow
                canvas.drawRect(drawX + 2, y + 2, drawX + monsterWidth + 2, y + monsterHeight + 2, shadowPaint)

                // 6️⃣ Vẽ monster với alpha transparency
                canvas.drawBitmap(monsterBitmap, drawX, y, monsterPaint)

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
        
        // Xóa hoàn toàn nền - đảm bảo trong suốt 100%
        canvas.drawColor(Color.TRANSPARENT, android.graphics.PorterDuff.Mode.CLEAR)
        
        drawable.setBounds(0, 0, size, size)
        drawable.draw(canvas)
        return bitmap
    }

    /**
     * 🎨 Tạo bitmap với kích thước custom (width ≠ height)
     * @param drawable Drawable cần chuyển đổi
     * @param width Chiều rộng mong muốn
     * @param height Chiều cao mong muốn
     */
    private fun drawableToBitmapCustomSize(drawable: Drawable, width: Int, height: Int): Bitmap {
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        
        // Đảm bảo nền hoàn toàn trong suốt với nhiều method
        canvas.drawColor(Color.TRANSPARENT, android.graphics.PorterDuff.Mode.CLEAR)
        
        // Tạo Paint đặc biệt cho drawable
        val drawablePaint = Paint().apply {
            isAntiAlias = true
            isFilterBitmap = true
            isDither = true
        }
        
        drawable.setBounds(0, 0, width, height)
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
