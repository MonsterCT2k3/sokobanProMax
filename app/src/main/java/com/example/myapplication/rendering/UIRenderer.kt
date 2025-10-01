package com.example.myapplication.rendering

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Typeface
import android.graphics.drawable.Drawable
import com.example.myapplication.entities.BulletType
import kotlin.math.min

/**
 * 🖥️ UIRenderer - Vẽ các elements giao diện người dùng
 *
 * Nhiệm vụ:
 * - Vẽ tiêu đề game và hướng dẫn
 * - Vẽ nút toggle (music/sound)
 * - Vẽ nút chọn loại đạn (normal, pierce, stun, build)
 */
class UIRenderer(private val resourceManager: ResourceManager) {

    private var screenWidth = 0
    private var screenHeight = 0

    fun setScreenSize(width: Int, height: Int) {
        screenWidth = width
        screenHeight = height
    }

    /**
     * 🎮 Vẽ UI chính của game (tiêu đề và hướng dẫn)
     */
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

    /**
     * 🔫 Vẽ nút chọn loại đạn ở phía dưới
     */
    fun drawBulletTypeButtons(canvas: Canvas, normalAmmo: Int, pierceAmmo: Int, stunAmmo: Int,
                             selectedType: BulletType, buildMode: Boolean) {
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
}
