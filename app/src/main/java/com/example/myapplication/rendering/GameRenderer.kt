package com.example.myapplication.rendering

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Typeface
import android.graphics.drawable.Drawable
import androidx.core.content.ContextCompat
import com.example.myapplication.R
import com.example.myapplication.entities.AmmoPickup
import com.example.myapplication.entities.AmmoType
import com.example.myapplication.entities.Bullet
import com.example.myapplication.entities.BulletDirection
import com.example.myapplication.entities.BulletType
import com.example.myapplication.entities.LivesPickup
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
    private lateinit var itemBullet: Drawable  // 🆕 THÊM ITEM BULLET
    private lateinit var rocket: Drawable     // 🆕 THÊM ROCKET

    private lateinit var musicOnIcon: Drawable
    private lateinit var musicOffIcon: Drawable
    private lateinit var soundOnIcon: Drawable
    private lateinit var soundOffIcon: Drawable
    
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
        itemBullet = ContextCompat.getDrawable(context, R.drawable.item_bullet)
            ?: throw IllegalStateException("item_bullet drawable not found")  // 🆕 LOAD ITEM BULLET
        rocket = ContextCompat.getDrawable(context, R.drawable.rocket)
            ?: throw IllegalStateException("rocket drawable not found")  // 🆕 LOAD ROCKET

        musicOnIcon = ContextCompat.getDrawable(context, R.drawable.music_on)
            ?: throw IllegalStateException("music_on drawable not found")
        musicOffIcon = ContextCompat.getDrawable(context, R.drawable.music_off)
            ?: throw IllegalStateException("music_off drawable not found")
        soundOnIcon = ContextCompat.getDrawable(context, R.drawable.sound_on)
            ?: throw IllegalStateException("sound_on drawable not found")
        soundOffIcon = ContextCompat.getDrawable(context, R.drawable.sound_off)
            ?: throw IllegalStateException("sound_off drawable not found")
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
                'S' to drawableToBitmap(goal, tileSize),  // Safe zone dùng goal drawable
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

                // 🆕 Overlay cho safe zone (ô 'S')
                if (map[i][j] == 'S') {
                    val safeZonePaint = Paint().apply {
                        color = Color.argb(120, 0, 150, 255)  // Màu xanh dương trong suốt
                        style = Paint.Style.FILL
                    }
                    canvas.drawRect(x, y, x + tileSize, y + tileSize, safeZonePaint)

                    // Vẽ viền xanh dương
                    val borderPaint = Paint().apply {
                        color = Color.rgb(0, 100, 200)
                        style = Paint.Style.STROKE
                        strokeWidth = 2f
                    }
                    canvas.drawRect(x, y, x + tileSize, y + tileSize, borderPaint)
                }
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
                // 🆕 Effect cho monster bị stun
                if (monster.isStunned()) {
                    // Vẽ hiệu ứng stun (vòng tròn tím xung quanh)
                    val stunPaint = Paint().apply {
                        color = Color.MAGENTA
                        style = Paint.Style.STROKE
                        strokeWidth = 3f
                        alpha = 180
                    }
                    val stunRadius = tileSize * 0.6f
                    val stunX = offsetX + monster.currentY * tileSize.toFloat()
                    val stunY = offsetY + monster.currentX * tileSize.toFloat()
                    canvas.drawCircle(stunX, stunY, stunRadius, stunPaint)
                }

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
                // 🎯 Lấy drawable theo hướng của bullet (cùng cho cả normal và pierce)
                val bulletDrawable = getBulletDrawable(bullet.direction)

                bulletDrawable?.let { drawable ->
                    // Vẽ bullet tại vị trí hiện tại với scale bằng cách set bounds
                    val bulletSize = 64 * bullet.scale
                    val halfSize = bulletSize / 2
                    val left = (bullet.currentX - halfSize).toInt()
                    val top = (bullet.currentY - halfSize).toInt()
                    val right = (bullet.currentX + halfSize).toInt()
                    val bottom = (bullet.currentY + halfSize).toInt()

                    drawable.setBounds(left, top, right, bottom)
                    drawable.draw(canvas)
                }

                // Vẽ trail effect theo loại bullet
                val trailColor = when (bullet.bulletType) {
                    BulletType.NORMAL -> android.graphics.Color.YELLOW
                    BulletType.PIERCE -> android.graphics.Color.CYAN
                    BulletType.STUN -> android.graphics.Color.MAGENTA
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

    /**
     * 🎛️ Vẽ các nút toggle nhạc/sound phía trên map
     */
    fun drawToggleButtons(canvas: Canvas, map: Array<CharArray>, 
                         musicEnabled: Boolean, soundEnabled: Boolean) {
        if (map.isEmpty() || map[0].isEmpty()) return

        // Tính vị trí các nút phía trên map
        val tileSize = min(screenWidth / map[0].size, screenHeight / map.size)
        val boardWidth = map[0].size * tileSize
        val boardHeight = map.size * tileSize
        val offsetX = (screenWidth - boardWidth) / 2f
        val offsetY = (screenHeight - boardHeight) / 2f

        // Vị trí nút: phía trên map, cách top 20px
        val buttonY = offsetY - 140f  // Tăng khoảng cách để nút lớn hơn không bị che
        val buttonSize = 120f         // Tăng kích thước nút từ 80f lên 120f
        
        // Nút trái: Toggle Music (bên trái màn hình)
        val musicButtonX = 20f
        val musicIcon = if (musicEnabled) musicOnIcon else musicOffIcon
        drawToggleButton(canvas, musicIcon, musicButtonX, buttonY, buttonSize)
        
        // Nút phải: Toggle Sound (bên phải màn hình) 
        val soundButtonX = screenWidth - buttonSize - 20f
        val soundIcon = if (soundEnabled) soundOnIcon else soundOffIcon
        drawToggleButton(canvas, soundIcon, soundButtonX, buttonY, buttonSize)
    }

    /**
     * 🎨 Vẽ một nút toggle đơn lẻ
     */
    private fun drawToggleButton(canvas: Canvas, icon: Drawable, x: Float, y: Float, size: Float) {
        // 1️⃣ Vẽ shadow trước (phía sau nút)
        val shadowPaint = Paint().apply {
            color = Color.argb(120, 0, 0, 0)  // Shadow đen nhạt
            style = Paint.Style.FILL
        }
        canvas.drawRoundRect(x + 4, y + 4, x + size + 4, y + size + 4, 15f, 15f, shadowPaint)
        
        // 2️⃣ Vẽ background trắng cho nút để dễ nhìn
        val buttonPaint = Paint().apply {
            color = Color.WHITE  // Nền trắng
            style = Paint.Style.FILL
        }
        canvas.drawRoundRect(x, y, x + size, y + size, 15f, 15f, buttonPaint)
        
        // 3️⃣ Vẽ icon với padding
        val iconPadding = size * 0.15f  // 15% padding
        val iconLeft = x + iconPadding
        val iconTop = y + iconPadding
        val iconRight = x + size - iconPadding
        val iconBottom = y + size - iconPadding
        
        icon.setBounds(iconLeft.toInt(), iconTop.toInt(), iconRight.toInt(), iconBottom.toInt())
        icon.draw(canvas)
        
        // 4️⃣ Vẽ border xám đậm cho contrast
        val borderPaint = Paint().apply {
            color = Color.DKGRAY  // Viền xám đậm
            style = Paint.Style.STROKE
            strokeWidth = 4f      // Viền dày hơn cho nút lớn
        }
        canvas.drawRoundRect(x, y, x + size, y + size, 15f, 15f, borderPaint)
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

    fun drawAmmoUI(canvas: Canvas, normalAmmo: Int, pierceAmmo: Int, screenWidth: Float, screenHeight: Float) {
        val maxAmmoPerType = 5

        // 🆕 NORMAL AMMO UI (hàng trên)
        val normalRect = RectF(
            screenWidth - 180f,  // Bên phải màn hình
            220f,                 // Vị trí Y
            screenWidth - 40f,    // Cách lề phải 40px
            280f                  // Chiều cao
        )

        // Vẽ nền cho normal ammo
        val normalUiPaint = Paint().apply {
            color = Color.parseColor("#DD333333")  // Nền đỏ
            style = Paint.Style.FILL
        }
        canvas.drawRoundRect(normalRect, 15f, 15f, normalUiPaint)

        // Vẽ viền vàng
        val borderPaint = Paint().apply {
            color = Color.YELLOW
            style = Paint.Style.STROKE
            strokeWidth = 3f
        }
        canvas.drawRoundRect(normalRect, 15f, 15f, borderPaint)

        // Vẽ text cho normal ammo
        val textPaint = Paint().apply {
            color = Color.WHITE
            textSize = 28f
            textAlign = Paint.Align.LEFT
            isAntiAlias = true
            style = Paint.Style.FILL_AND_STROKE
            strokeWidth = 1f
        }

        // Vẽ icon normal ammo
        itemBullet?.let { drawable ->
            val bulletSize = 28f
            val iconLeft = normalRect.left + 8f
            val iconTop = normalRect.centerY() - bulletSize / 2
            drawable.setBounds(
                iconLeft.toInt(),
                iconTop.toInt(),
                (iconLeft + bulletSize).toInt(),
                (iconTop + bulletSize).toInt()
            )
            drawable.draw(canvas)
        }

        // Vẽ số lượng normal ammo
        val textX = normalRect.left + 44f
        val textY = normalRect.centerY() + 8f
        canvas.drawText("$normalAmmo/$maxAmmoPerType", textX, textY, textPaint)

        // 🆕 PIERCE AMMO UI (hàng dưới)
        val pierceRect = RectF(
            screenWidth - 180f,  // Bên phải màn hình
            290f,                 // Dưới normal ammo
            screenWidth - 40f,    // Cách lề phải 40px
            350f                  // Chiều cao
        )

        // Vẽ nền cho pierce ammo (màu xanh dương)
        val pierceUiPaint = Paint().apply {
            color = Color.parseColor("#DD333366")  // Nền xanh dương
            style = Paint.Style.FILL
        }
        canvas.drawRoundRect(pierceRect, 15f, 15f, pierceUiPaint)

        // Vẽ viền xanh dương
        val pierceBorderPaint = Paint().apply {
            color = Color.CYAN
            style = Paint.Style.STROKE
            strokeWidth = 3f
        }
        canvas.drawRoundRect(pierceRect, 15f, 15f, pierceBorderPaint)

        // Vẽ icon pierce ammo
        itemBullet?.let { drawable ->
            val bulletSize = 28f
            val iconLeft = pierceRect.left + 8f
            val iconTop = pierceRect.centerY() - bulletSize / 2
            drawable.setBounds(
                iconLeft.toInt(),
                iconTop.toInt(),
                (iconLeft + bulletSize).toInt(),
                (iconTop + bulletSize).toInt()
            )
            drawable.draw(canvas)
        }

        // Vẽ số lượng pierce ammo
        canvas.drawText("$pierceAmmo/$maxAmmoPerType", textX, pierceRect.centerY() + 8f, textPaint)
    }

    // 🆕 VẼ LIVES UI Ở GIỮA MÀN HÌNH
    fun drawLivesUI(canvas: Canvas, lives: Int, maxLives: Int, screenWidth: Float, screenHeight: Float) {
        // Vẽ ở giữa màn hình, phía trên ammo buttons
        val uiWidth = 150f
        val uiHeight = 100f
        val uiRect = RectF(
            screenWidth / 2 - uiWidth / 2,  // Căn giữa ngang
            200f,                           // Cách top 250px (thấp xuống thêm 100px)
            screenWidth / 2 + uiWidth / 2,  // Căn giữa ngang
            250f + uiHeight                 // Chiều cao
        )

        // Vẽ nền
        val uiPaint = Paint().apply {
            color = Color.parseColor("#FFFF99")  // Nền đỏ cho lives
            style = Paint.Style.FILL
        }
        canvas.drawRoundRect(uiRect, 15f, 15f, uiPaint)

        // Vẽ viền
        val borderPaint = Paint().apply {
            color = Color.RED
            style = Paint.Style.STROKE
            strokeWidth = 3f
        }
        canvas.drawRoundRect(uiRect, 15f, 15f, borderPaint)

        // Vẽ text
        val textPaint = Paint().apply {
            color = Color.BLACK
            textSize = 62f
            textAlign = Paint.Align.CENTER
            isAntiAlias = true
            style = Paint.Style.FILL_AND_STROKE
            strokeWidth = 2f
        }

        val centerX = uiRect.centerX()
        val centerY = uiRect.centerY() + 8f

        // Vẽ "❤️" emoji
        val shadowPaint = Paint().apply {
            color = Color.BLACK
            textSize = 50f
            textAlign = Paint.Align.CENTER
            isAntiAlias = true
            style = Paint.Style.FILL_AND_STROKE
            strokeWidth = 1f
        }
        canvas.drawText("❤️", centerX + 1f, centerY + 1f, shadowPaint)
        canvas.drawText("❤️", centerX, centerY, textPaint)

        // Vẽ số lives
        val numberPaint = Paint().apply {
            color = Color.BLACK
            textSize = 40f
            textAlign = Paint.Align.CENTER
            isAntiAlias = true
            style = Paint.Style.FILL_AND_STROKE
            strokeWidth = 1f
        }
        canvas.drawText("$lives/$maxLives", centerX, centerY + 50f, numberPaint)
    }

    // 🆕 VẼ NÚT CHỌN LOẠI ĐẠN Ở PHÍA DƯỚI (to và dễ ấn)
    fun drawBulletTypeButtons(canvas: Canvas, normalAmmo: Int, pierceAmmo: Int, stunAmmo: Int, screenWidth: Float, screenHeight: Float, selectedType: BulletType, buildMode: Boolean) {
        val buttonWidth = 150f  // Giảm kích thước cho 3 nút
        val buttonHeight = 120f
        val buttonSpacing = 20f
        val bottomMargin = 150f

        // Nút normal ammo (bên trái)
        val normalButtonRect = RectF(
            screenWidth / 2 - buttonWidth * 1.5f - buttonSpacing,
            screenHeight - buttonHeight - bottomMargin,
            screenWidth / 2 - buttonWidth * 0.5f - buttonSpacing / 2,
            screenHeight - bottomMargin
        )

        // Nút pierce ammo (giữa)
        val pierceButtonRect = RectF(
            screenWidth / 2 - buttonWidth * 0.5f,
            screenHeight - buttonHeight - bottomMargin,
            screenWidth / 2 + buttonWidth * 0.5f,
            screenHeight - bottomMargin
        )

        // Nút stun ammo (bên phải)
        val stunButtonRect = RectF(
            screenWidth / 2 + buttonWidth * 0.5f + buttonSpacing / 2,
            screenHeight - buttonHeight - bottomMargin,
            screenWidth / 2 + buttonWidth * 1.5f + buttonSpacing,
            screenHeight - bottomMargin
        )

        // Nút build wall (cạnh bên phải stun)
        val buildButtonRect = RectF(
            screenWidth / 2 + buttonWidth * 1.5f + buttonSpacing * 1.5f,
            screenHeight - buttonHeight - bottomMargin,
            screenWidth / 2 + buttonWidth * 2.5f + buttonSpacing * 2,
            screenHeight - bottomMargin
        )

        val buttonPaint = Paint().apply { style = Paint.Style.FILL }
        val borderPaint = Paint().apply { style = Paint.Style.STROKE; strokeWidth = 4f } // 🆕 Tăng border từ 3f lên 4f
        val textPaint = Paint().apply {
            color = Color.WHITE
            textSize = 48f  // 🆕 Tăng từ 24f lên 32f
            textAlign = Paint.Align.CENTER
            isAntiAlias = true
        }

        // Vẽ nút normal ammo
        buttonPaint.color = if (selectedType == BulletType.NORMAL) Color.parseColor("#FF6B35") else Color.parseColor("#DD333333")
        borderPaint.color = if (selectedType == BulletType.NORMAL) Color.YELLOW else Color.GRAY
        canvas.drawRoundRect(normalButtonRect, 10f, 10f, buttonPaint)
        canvas.drawRoundRect(normalButtonRect, 10f, 10f, borderPaint)

        // Vẽ icon normal ammo
        itemBullet?.let { drawable ->
            val iconSize = 62f  // 🆕 Tăng từ 32f lên 50f
            val iconLeft = normalButtonRect.left + 15f  // 🆕 Tăng margin từ 10f lên 15f
            val iconTop = normalButtonRect.centerY() - iconSize / 2
            drawable.setBounds(
                iconLeft.toInt(),
                iconTop.toInt(),
                (iconLeft + iconSize).toInt(),
                (iconTop + iconSize).toInt()
            )
            drawable.draw(canvas)
        }

        // Vẽ số lượng normal ammo
        canvas.drawText(
            "$normalAmmo",
            normalButtonRect.centerX() + 25f,  // 🆕 Tăng từ 15f lên 25f
            normalButtonRect.centerY() + 10f,  // 🆕 Tăng từ 8f lên 10f
            textPaint
        )

        // Vẽ nút pierce ammo
        buttonPaint.color = if (selectedType == BulletType.PIERCE) Color.parseColor("#4A90E2") else Color.parseColor("#DD333366")
        borderPaint.color = if (selectedType == BulletType.PIERCE) Color.CYAN else Color.GRAY
        canvas.drawRoundRect(pierceButtonRect, 10f, 10f, buttonPaint)
        canvas.drawRoundRect(pierceButtonRect, 10f, 10f, borderPaint)

        // Vẽ icon pierce ammo (dùng rocket)
        rocket?.let { drawable ->
            val iconSize = 62f  // 🆕 Tăng từ 32f lên 50f
            val iconLeft = pierceButtonRect.left + 15f  // 🆕 Tăng margin từ 10f lên 15f
            val iconTop = pierceButtonRect.centerY() - iconSize / 2
            drawable.setBounds(
                iconLeft.toInt(),
                iconTop.toInt(),
                (iconLeft + iconSize).toInt(),
                (iconTop + iconSize).toInt()
            )
            drawable.draw(canvas)
        }

        // Vẽ số lượng pierce ammo
        canvas.drawText(
            "$pierceAmmo",
            pierceButtonRect.centerX() + 15f,
            pierceButtonRect.centerY() + 10f,
            textPaint
        )

        // Vẽ nút stun ammo
        buttonPaint.color = if (selectedType == BulletType.STUN) Color.parseColor("#9933FF") else Color.parseColor("#DD6600CC")  // Màu tím cho stun
        borderPaint.color = if (selectedType == BulletType.STUN) Color.MAGENTA else Color.GRAY
        canvas.drawRoundRect(stunButtonRect, 10f, 10f, buttonPaint)
        canvas.drawRoundRect(stunButtonRect, 10f, 10f, borderPaint)

        // Vẽ icon stun ammo (⚡) ở bên trái
        val stunIconPaint = Paint().apply {
            color = Color.WHITE
            textSize = 52f
            textAlign = Paint.Align.LEFT
            isAntiAlias = true
            style = Paint.Style.FILL_AND_STROKE
            strokeWidth = 2f
        }
        canvas.drawText(
            "⚡",
            stunButtonRect.left + 30f,
            stunButtonRect.centerY() + 10f,
            stunIconPaint
        )

        // Vẽ số lượng stun ammo ở bên phải (cùng dòng)
        val stunNumberPaint = Paint().apply {
            color = Color.WHITE
            textSize = 48f
            textAlign = Paint.Align.RIGHT
            isAntiAlias = true
            style = Paint.Style.FILL_AND_STROKE
            strokeWidth = 1f
        }
        canvas.drawText(
            "$stunAmmo",
            stunButtonRect.right - 40f,
            stunButtonRect.centerY() + 10f,
            stunNumberPaint
        )

        // Vẽ nút build wall
        buttonPaint.color = if (buildMode) Color.parseColor("#FF6600") else Color.parseColor("#DD444444")  // Màu cam cho build
        borderPaint.color = if (buildMode) Color.parseColor("#FFAA00") else Color.GRAY
        canvas.drawRoundRect(buildButtonRect, 10f, 10f, buttonPaint)
        canvas.drawRoundRect(buildButtonRect, 10f, 10f, borderPaint)

        // Vẽ icon build wall (🧱 hoặc wall symbol)
        val buildIconPaint = Paint().apply {
            color = Color.WHITE
            textSize = 32f
            textAlign = Paint.Align.CENTER
            isAntiAlias = true
            style = Paint.Style.FILL_AND_STROKE
            strokeWidth = 2f
        }
        canvas.drawText(
            "🧱",
            buildButtonRect.centerX(),
            buildButtonRect.centerY() + 10f,
            buildIconPaint
        )
    }

    // 🆕 VẼ LIVES PICKUPS TRÊN MAP
    fun drawLivesPickups(canvas: Canvas, livesPickups: List<LivesPickup>, tileSize: Float, offsetX: Float, offsetY: Float) {
        livesPickups.forEach { lives ->
            val (screenX, screenY) = lives.getScreenPosition(tileSize, offsetX, offsetY)

            // Vẽ nền tim đỏ
            val backgroundPaint = Paint().apply {
                color = Color.RED
                style = Paint.Style.FILL
            }
            val backgroundSize = tileSize * 0.6f
            val backgroundRect = RectF(
                screenX - backgroundSize / 2,
                screenY - backgroundSize / 2,
                screenX + backgroundSize / 2,
                screenY + backgroundSize / 2
            )
            canvas.drawRoundRect(backgroundRect, backgroundSize * 0.2f, backgroundSize * 0.2f, backgroundPaint)

            // Vẽ viền vàng
            val borderPaint = Paint().apply {
                color = Color.YELLOW
                style = Paint.Style.STROKE
                strokeWidth = 2f
            }
            canvas.drawRoundRect(backgroundRect, backgroundSize * 0.2f, backgroundSize * 0.2f, borderPaint)

            // Vẽ text "❤️" nhỏ
            val textPaint = Paint().apply {
                color = Color.WHITE
                textSize = tileSize * 0.4f
                textAlign = Paint.Align.CENTER
                isAntiAlias = true
                style = Paint.Style.FILL_AND_STROKE
                strokeWidth = 1f
            }
            canvas.drawText("❤️", screenX, screenY + textPaint.textSize * 0.3f, textPaint)
        }
    }

    // Thêm method vẽ ammo pickups:

    fun drawAmmoPickups(canvas: Canvas, ammoPickups: List<AmmoPickup>, tileSize: Float, offsetX: Float, offsetY: Float) {
        ammoPickups.forEach { ammo ->
            val (screenX, screenY) = ammo.getScreenPosition(tileSize, offsetX, offsetY)

            // Vẽ nền trắng vuông
            val backgroundPaint = Paint().apply {
                color = Color.WHITE
                style = Paint.Style.FILL
            }
            val backgroundSize = tileSize * 0.6f
            val backgroundRect = RectF(
                screenX - backgroundSize / 2,
                screenY - backgroundSize / 2,
                screenX + backgroundSize / 2,
                screenY + backgroundSize / 2
            )
            canvas.drawRect(backgroundRect, backgroundPaint)

            // Vẽ viền với màu khác nhau theo type
            val borderColor = when (ammo.ammoType) {
                AmmoType.NORMAL -> Color.BLACK
                AmmoType.PIERCE -> Color.CYAN
                AmmoType.STUN -> Color.MAGENTA  // Tím cho stun ammo
            }
            val borderPaint = Paint().apply {
                color = borderColor
                style = Paint.Style.STROKE
                strokeWidth = 2f
            }
            canvas.drawRect(backgroundRect, borderPaint)

            // Vẽ hình ammo theo type - normal dùng item_bullet, pierce dùng rocket, stun dùng item_bullet với border khác
            val ammoDrawable = when (ammo.ammoType) {
                AmmoType.NORMAL -> itemBullet
                AmmoType.PIERCE -> rocket
                AmmoType.STUN -> itemBullet  // STUN dùng item_bullet nhưng border khác
            }

            ammoDrawable?.let { drawable ->
                val ammoSize = tileSize * 0.4f
                val left = (screenX - ammoSize / 2).toInt()
                val top = (screenY - ammoSize / 2).toInt()
                val right = (screenX + ammoSize / 2).toInt()
                val bottom = (screenY + ammoSize / 2).toInt()

                drawable.setBounds(left, top, right, bottom)
                drawable.draw(canvas)
            }
        }
    }
}
