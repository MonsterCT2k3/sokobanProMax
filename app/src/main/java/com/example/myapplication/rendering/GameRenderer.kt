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

    // ===== SUB-RENDERERS =====
    private val resourceManager = ResourceManager(context)
    private val boardRenderer = BoardRenderer(context, this)

    // Monster size constants - dễ dàng điều chỉnh
    companion object {
        private const val MONSTER_WIDTH_RATIO = 1.0f   // 70% chiều rộng tile
        private const val MONSTER_HEIGHT_RATIO = 1.0f  // 100% chiều cao tile (giữ nguyên)
    }
    
    // Screen dimensions
    private var screenWidth = 0
    private var screenHeight = 0
    
    
    fun setScreenSize(width: Int, height: Int) {
        screenWidth = width
        screenHeight = height
        boardRenderer.setScreenSize(width, height)
    }

    // ===== GETTER METHODS FOR SUB-RENDERERS =====

    internal fun getWall(): Drawable = resourceManager.wall
    internal fun getBox(): Drawable = resourceManager.box
    internal fun getGoal(): Drawable = resourceManager.goal
    internal fun getFloor(): Drawable = resourceManager.floor
    internal fun getMonsterPaint(): Paint = resourceManager.monsterPaint
    internal fun getTilePaint(): Paint = resourceManager.tilePaint
    internal fun getShadowPaint(): Paint = resourceManager.shadowPaint
    internal fun getTextPaint(): Paint = resourceManager.textPaint

    internal fun getMonsterDrawable(type: MonsterType): Drawable = resourceManager.getMonsterDrawable(type)
    internal fun getCurrentPlayerDrawable(direction: PlayerDirection): Drawable {
        return when (direction) {
            PlayerDirection.UP -> resourceManager.playerUp
            PlayerDirection.DOWN -> resourceManager.playerDown
            PlayerDirection.LEFT -> resourceManager.playerLeft
            PlayerDirection.RIGHT -> resourceManager.playerRight
        }
    }

    fun drawGameBoard(canvas: Canvas, map: Array<CharArray>, playerRow: Int, playerCol: Int,
                     playerDirection: PlayerDirection, monsters: List<Monster>) {
        boardRenderer.drawGameBoard(canvas, map, playerRow, playerCol, playerDirection, monsters)
    }

    
    fun drawGameUI(canvas: Canvas) {
        // Vẽ tiêu đề game
        canvas.drawText("Sokoban Game", screenWidth / 2f, 120f, resourceManager.textPaint)

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
                val bulletDrawable = resourceManager.getBulletDrawable(bullet.direction)

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
        val musicIcon = if (musicEnabled) resourceManager.musicOnIcon else resourceManager.musicOffIcon
        drawToggleButton(canvas, musicIcon, musicButtonX, buttonY, buttonSize)

        // Nút phải: Toggle Sound (bên phải màn hình)
        val soundButtonX = screenWidth - buttonSize - 20f
        val soundIcon = if (soundEnabled) resourceManager.soundOnIcon else resourceManager.soundOffIcon
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
        resourceManager.itemBullet.let { drawable ->
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
        resourceManager.itemBullet.let { drawable ->
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
        resourceManager.itemBullet.let { drawable ->
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
        resourceManager.rocket.let { drawable ->
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
                AmmoType.NORMAL -> resourceManager.itemBullet
                AmmoType.PIERCE -> resourceManager.rocket
                AmmoType.STUN -> resourceManager.itemBullet  // STUN dùng item_bullet nhưng border khác
            }

            ammoDrawable.let { drawable ->
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

    /**
     * 🛡️ Draw player shield effect when on safe zone
     */
    fun drawPlayerShield(canvas: Canvas, playerRow: Int, playerCol: Int, gameLogic: com.example.myapplication.game.GameLogic, animationTime: Float) {
        // Check bằng map character trực tiếp
        val mapChar = gameLogic.getMap().getOrNull(playerRow)?.getOrNull(playerCol)
        if (mapChar != 'S') return

        val tileSize = min(screenWidth / gameLogic.getMap()[0].size, screenHeight / gameLogic.getMap().size)
        val boardWidth = gameLogic.getMap()[0].size * tileSize
        val boardHeight = gameLogic.getMap().size * tileSize
        val offsetX = (screenWidth - boardWidth) / 2f
        val offsetY = (screenHeight - boardHeight) / 2f
        
        // Calculate player center position
        val playerCenterX = offsetX + playerCol * tileSize + tileSize / 2f
        val playerCenterY = offsetY + playerRow * tileSize + tileSize / 2f

        // Shield radius with pulsing animation - TĂNG TỐC ĐỘ RÕ RÀNG
        val baseShieldRadius = tileSize * 0.6f
        val pulseSpeed = 0.003f  // Chu kỳ: ~2 giây
        val pulseFactor = (Math.sin((animationTime * pulseSpeed).toDouble()) * 0.2f + 1f).toFloat()
        val shieldRadius = baseShieldRadius * pulseFactor

        // Draw shield glow effect
        val shieldPaint = android.graphics.Paint().apply {
            color = android.graphics.Color.parseColor("#00BFFF") // Deep sky blue
            style = android.graphics.Paint.Style.STROKE
            strokeWidth = 4f
            alpha = 220
            setShadowLayer(12f, 0f, 0f, android.graphics.Color.parseColor("#00BFFF"))
        }

        // Draw outer glow with animation - NHẤP NHÁY NHANH HƠN
        val alphaSpeed1 = 0.005f
        shieldPaint.alpha = (150 + Math.sin((animationTime * alphaSpeed1).toDouble()) * 70).toInt().coerceIn(100, 220)
        shieldPaint.strokeWidth = 16f
        canvas.drawCircle(playerCenterX, playerCenterY, shieldRadius + 8f, shieldPaint)

        // Draw main shield ring - NHẤP NHÁY NHANH HƠN
        val alphaSpeed2 = 0.006f
        shieldPaint.alpha = (200 + Math.sin((animationTime * alphaSpeed2).toDouble()) * 55).toInt().coerceIn(180, 255)
        shieldPaint.strokeWidth = 8f
        canvas.drawCircle(playerCenterX, playerCenterY, shieldRadius, shieldPaint)

        // Draw inner shield ring
        shieldPaint.alpha = 255
        shieldPaint.strokeWidth = 3f
        canvas.drawCircle(playerCenterX, playerCenterY, shieldRadius - 4f, shieldPaint)

        // Draw shield particles (small dots around the circle) with rotation - QUAY NHANH HƠN
        val particlePaint = android.graphics.Paint().apply {
            color = android.graphics.Color.parseColor("#FFFFFF")
            style = android.graphics.Paint.Style.FILL
            alpha = (220 + Math.sin((animationTime * 0.008f).toDouble()) * 55).toInt().coerceIn(200, 275)
            setShadowLayer(4f, 0f, 0f, android.graphics.Color.WHITE)
        }

        val particleCount = 12
        val particleRadius = 4f
        val particleDistance = shieldRadius + 10f
        val rotationSpeed = 0.05f  // TĂNG TỐC ĐỘ QUAY x50 lần! (1 vòng trong ~4 giây)
        val rotationOffset = animationTime * rotationSpeed

        for (i in 0 until particleCount) {
            val angle = ((i * 360f / particleCount) + rotationOffset) * Math.PI / 180f
            val particleX = playerCenterX + particleDistance * Math.cos(angle).toFloat()
            val particleY = playerCenterY + particleDistance * Math.sin(angle).toFloat()
            canvas.drawCircle(particleX, particleY, particleRadius, particlePaint)
        }

        // Draw energy arcs (lightning-like effects) - XOAY NHANH HƠN
        val arcPaint = android.graphics.Paint().apply {
            color = android.graphics.Color.parseColor("#FFFFFF")
            style = android.graphics.Paint.Style.STROKE
            strokeWidth = 3f
            alpha = (180 + Math.sin((animationTime * 0.01f).toDouble()) * 75).toInt().coerceIn(150, 255)
            strokeCap = android.graphics.Paint.Cap.ROUND
            setShadowLayer(6f, 0f, 0f, android.graphics.Color.WHITE)
        }

        // Draw 3 energy arcs - QUAY NGƯỢC CHIỀU VÀ NHANH HƠN
        val arcRotationSpeed = 0.08f  // TĂNG TỐC ĐỘ QUAY x53 lần! (1 vòng trong ~2.5 giây)
        for (j in 0 until 3) {
            val arcAngle = (j * 120f - animationTime * arcRotationSpeed) * Math.PI / 180f
            val arcStartAngle = arcAngle - Math.PI / 6
            val arcEndAngle = arcAngle + Math.PI / 6
            val arcRadius = shieldRadius - 2f

            val startX = playerCenterX + arcRadius * Math.cos(arcStartAngle).toFloat()
            val startY = playerCenterY + arcRadius * Math.sin(arcStartAngle).toFloat()
            val endX = playerCenterX + arcRadius * Math.cos(arcEndAngle).toFloat()
            val endY = playerCenterY + arcRadius * Math.sin(arcEndAngle).toFloat()

            canvas.drawLine(startX, startY, endX, endY, arcPaint)
        }
    }

    // ==================== BACKWARD COMPATIBILITY METHODS ====================

    /**
     * Delegate to BoardRenderer for backward compatibility
     */
    fun calculateTileSize(map: Array<CharArray>): Int {
        return boardRenderer.calculateTileSize(map)
    }

    /**
     * Delegate to BoardRenderer for backward compatibility
     */
    fun calculateBoardOffset(map: Array<CharArray>): Pair<Float, Float> {
        return boardRenderer.calculateBoardOffset(map)
    }
}