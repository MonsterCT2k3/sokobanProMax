package com.example.myapplication.rendering

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.drawable.Drawable
import com.example.myapplication.entities.Monster
import com.example.myapplication.game.PlayerDirection
import kotlin.math.min

/**
 * 🗺️ BoardRenderer - Vẽ game board và các entities
 *
 * Nhiệm vụ:
 * - Vẽ map tiles (tường, sàn, hộp, mục tiêu)
 * - Vẽ player với depth sorting
 * - Vẽ monsters với depth sorting
 * - Vẽ safe zones
 */
class BoardRenderer(private val context: Context, private val gameRenderer: GameRenderer) {

    // Monster size constants - dễ dàng điều chỉnh
    companion object {
        private const val MONSTER_WIDTH_RATIO = 1.0f   // 70% chiều rộng tile
        private const val MONSTER_HEIGHT_RATIO = 1.0f  // 100% chiều cao tile (giữ nguyên)
    }

    // Cached bitmaps for performance
    private var cachedBitmaps: Map<Char, Bitmap>? = null
    private var cachedTileSize: Int = 0

    // Screen dimensions
    private var screenWidth = 0
    private var screenHeight = 0

    fun setScreenSize(width: Int, height: Int) {
        screenWidth = width
        screenHeight = height
    }

    /**
     * 🗺️ Vẽ toàn bộ game board
     */
    fun drawGameBoard(canvas: Canvas, map: Array<CharArray>, playerRow: Int, playerCol: Int,
                     playerDirection: PlayerDirection, monsters: List<Monster>, safeZonePositions: Set<Pair<Int, Int>>) {
        if (map.isEmpty() || map[0].isEmpty()) return

        val tileSize = min(screenWidth / map[0].size, screenHeight / map.size)

        // Update cached bitmaps if needed
        if (cachedBitmaps == null || cachedTileSize != tileSize) {
            cachedTileSize = tileSize
            cachedBitmaps = mapOf(
                '#' to drawableToBitmap(gameRenderer.getWall(), tileSize),
                'G' to drawableToBitmap(gameRenderer.getGoal(), tileSize),
                'S' to drawableToBitmap(gameRenderer.getSafeZone(), tileSize),
                '.' to drawableToBitmap(gameRenderer.getFloor(), tileSize)
            )
        }
        val bitmaps = cachedBitmaps!!

        // Tính toán để center game board
        val boardWidth = map[0].size * tileSize
        val boardHeight = map.size * tileSize
        val offsetX = (screenWidth - boardWidth) / 2f
        val offsetY = (screenHeight - boardHeight) / 2f

        // BƯỚC 1: Vẽ tất cả tiles trước (nền, tường, hộp, mục tiêu)
        drawTiles(canvas, map, bitmaps, tileSize, offsetX, offsetY, safeZonePositions)

        // BƯỚC 2: Vẽ entities (player + monsters) theo thứ tự depth (Y-coordinate)
        drawEntitiesWithDepthSort(canvas, map, playerRow, playerCol, monsters, playerDirection, tileSize, offsetX, offsetY)
    }

    /**
     * 🎨 Vẽ tất cả tiles trên map
     */
    private fun drawTiles(canvas: Canvas, map: Array<CharArray>, bitmaps: Map<Char, Bitmap>,
                         tileSize: Int, offsetX: Float, offsetY: Float, safeZonePositions: Set<Pair<Int, Int>>) {
        for (i in map.indices) {
            for (j in map[i].indices) {
                val x = offsetX + j * tileSize.toFloat()
                val y = offsetY + i * tileSize.toFloat()

                // Xác định nền cần vẽ (floor, wall, goal, safe zone)
                val baseTile = when (map[i][j]) {
                    'B' -> '.'  // Box hiển thị nền floor
                    else -> map[i][j]  // Wall, goal, safe zone, floor giữ nguyên
                }

                // Vẽ bóng cho non-floor tiles (wall, goal, safe zone)
                if (baseTile != '.' && baseTile != 'S') {
                    canvas.drawRect(x + 3, y + 3, x + tileSize + 3, y + tileSize + 3,
                                   gameRenderer.getShadowPaint())
                }

                // Vẽ nền (wall, goal, safe zone, floor)
                val bitmap = bitmaps[baseTile] ?: bitmaps['.']!!
                canvas.drawBitmap(bitmap, x, y, gameRenderer.getTilePaint())

                // 🆕 Vẽ hộp nếu vị trí có hộp ('B')
                if (map[i][j] == 'B') {
                    val boxBitmap = drawableToBitmap(gameRenderer.getBox(), tileSize)
                    canvas.drawBitmap(boxBitmap, x, y, gameRenderer.getTilePaint())

                    // Vẽ bóng cho hộp
                    canvas.drawRect(x + 3, y + 3, x + tileSize + 3, y + tileSize + 3,
                                   gameRenderer.getShadowPaint())
                }
            }
        }
    }

    /**
     * 🎭 Vẽ tất cả entities (player + monsters) theo thứ tự depth sorting
     * Entities ở phía dưới (Y lớn hơn) sẽ được vẽ sau để che entities ở phía trên
     */
    private fun drawEntitiesWithDepthSort(canvas: Canvas, map: Array<CharArray>, playerRow: Int, playerCol: Int,
                                         monsters: List<Monster>, playerDirection: PlayerDirection,
                                         tileSize: Int, offsetX: Float, offsetY: Float) {

        // 1️⃣ Tạo danh sách tất cả entities với thông tin depth
        data class EntityToDraw(
            val type: String,  // "player" hoặc "monster"
            val x: Float,
            val y: Float,
            val depth: Int,    // Y-coordinate cho sorting (row index)
            val monster: Monster? = null
        )

        val entitiesToDraw = mutableListOf<EntityToDraw>()

        // 2️⃣ Thêm player vào danh sách (dùng position trực tiếp)
        val playerScreenX = offsetX + playerCol * tileSize.toFloat()  // playerCol là column
        val playerScreenY = offsetY + playerRow * tileSize.toFloat()  // playerRow là row
        entitiesToDraw.add(EntityToDraw("player", playerScreenX, playerScreenY, playerRow))  // playerRow là row = depth

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
                    val playerDrawable = gameRenderer.getCurrentPlayerDrawable(playerDirection)
                    val playerBitmap = drawableToBitmap(playerDrawable, tileSize)
                    canvas.drawBitmap(playerBitmap, entity.x, entity.y, gameRenderer.getTilePaint())
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
        val monsterDrawable = gameRenderer.getMonsterDrawable(monster.type)

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
        canvas.drawBitmap(monsterBitmap, drawX, y, gameRenderer.getMonsterPaint())
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
                val monsterDrawable = gameRenderer.getMonsterDrawable(monster.type)

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
                canvas.drawRect(drawX + 2, y + 2, drawX + monsterWidth + 2, y + monsterHeight + 2, gameRenderer.getShadowPaint())

                // 6️⃣ Vẽ monster với alpha transparency
                canvas.drawBitmap(monsterBitmap, drawX, y, gameRenderer.getMonsterPaint())
            }
        }
    }

    /**
     * 🖼️ Convert Drawable thành Bitmap
     */
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
     * 📐 Tính tile size - public method for backward compatibility
     */
    fun calculateTileSize(map: Array<CharArray>): Int {
        return min(screenWidth / map[0].size, screenHeight / map.size)
    }

    /**
     * 📍 Tính board offset - public method for backward compatibility
     */
    fun calculateBoardOffset(map: Array<CharArray>): Pair<Float, Float> {
        val tileSize = calculateTileSize(map).toFloat()
        val boardWidth = map[0].size * tileSize
        val boardHeight = map.size * tileSize
        val offsetX = (screenWidth - boardWidth) / 2f
        val offsetY = (screenHeight - boardHeight) / 2f
        return Pair(offsetX, offsetY)
    }
}
