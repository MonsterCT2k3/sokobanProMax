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
    fun drawGameUI(canvas: Canvas, currentLevelId: Int = 1) {
        // Vẽ tiêu đề game với font lớn và màu trắng
        val titlePaint = Paint().apply {
            color = Color.WHITE
            textSize = 44f  // Tăng từ ~60f (size mặc định) lên 80f
            textAlign = Paint.Align.CENTER
            isAntiAlias = true
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            style = Paint.Style.FILL_AND_STROKE
            strokeWidth = 2f
        }

        // Vẽ shadow cho tiêu đề
        val shadowPaint = Paint().apply {
            color = Color.BLACK
            textSize = 44f
            textAlign = Paint.Align.CENTER
            isAntiAlias = true
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            style = Paint.Style.FILL_AND_STROKE
            strokeWidth = 1f
        }

        canvas.drawText("Sokoban Game", screenWidth / 2f + 2f, 122f, shadowPaint)
        canvas.drawText("Sokoban Game", screenWidth / 2f, 120f, titlePaint)

        // 🆕 Vẽ level display ngay dưới tiêu đề
        val levelPaint = Paint().apply {
            color = Color.YELLOW
            textSize = 32f
            textAlign = Paint.Align.CENTER
            isAntiAlias = true
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            style = Paint.Style.FILL_AND_STROKE
            strokeWidth = 1f
        }

        val levelShadowPaint = Paint().apply {
            color = Color.BLACK
            textSize = 32f
            textAlign = Paint.Align.CENTER
            isAntiAlias = true
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            style = Paint.Style.FILL_AND_STROKE
            strokeWidth = 1f
        }

        val levelText = "LEVEL $currentLevelId"
        canvas.drawText(levelText, screenWidth / 2f + 1f, 162f, levelShadowPaint)
        canvas.drawText(levelText, screenWidth / 2f, 160f, levelPaint)

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
        val buttonWidth = 140f  // Giảm kích thước cho 4 nút
        val buttonHeight = 110f
        val buttonSpacing = 15f
        val bottomMargin = 150f

        // Tổng chiều rộng của 4 nút và 3 khoảng trống
        val totalWidth = buttonWidth * 4 + buttonSpacing * 3

        // Bắt đầu từ giữa màn hình, lùi về bên trái để căn giữa
        val startX = screenWidth / 2 - totalWidth / 2

        // Nút normal ammo (thứ 1 từ trái)
        val normalButtonRect = RectF(
            startX,
            screenHeight - buttonHeight - bottomMargin,
            startX + buttonWidth,
            screenHeight - bottomMargin
        )

        // Nút pierce ammo (thứ 2 từ trái)
        val pierceButtonRect = RectF(
            startX + buttonWidth + buttonSpacing,
            screenHeight - buttonHeight - bottomMargin,
            startX + buttonWidth * 2 + buttonSpacing,
            screenHeight - bottomMargin
        )

        // Nút stun ammo (thứ 3 từ trái)
        val stunButtonRect = RectF(
            startX + buttonWidth * 2 + buttonSpacing * 2,
            screenHeight - buttonHeight - bottomMargin,
            startX + buttonWidth * 3 + buttonSpacing * 2,
            screenHeight - bottomMargin
        )

        // Nút build wall (thứ 4 từ trái)
        val buildButtonRect = RectF(
            startX + buttonWidth * 3 + buttonSpacing * 3,
            screenHeight - buttonHeight - bottomMargin,
            startX + buttonWidth * 4 + buttonSpacing * 3,
            screenHeight - bottomMargin
        )

        val buttonPaint = Paint().apply { style = Paint.Style.FILL }
        val borderPaint = Paint().apply { style = Paint.Style.STROKE; strokeWidth = 4f } // 🆕 Tăng border từ 3f lên 4f
        val textPaint = Paint().apply {
            color = Color.WHITE
            textSize = 42f  // Giảm từ 48f để phù hợp với nút nhỏ hơn
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
            val iconSize = 52f  // Giảm từ 62f để phù hợp với nút nhỏ hơn
            val iconLeft = normalButtonRect.left + 12f  // Giảm margin
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
            normalButtonRect.centerX() + 20f,  // Giảm từ 25f
            normalButtonRect.centerY() + 8f,   // Giảm từ 10f
            textPaint
        )

        // Vẽ nút pierce ammo
        buttonPaint.color = if (selectedType == BulletType.PIERCE) Color.parseColor("#4A90E2") else Color.parseColor("#DD333366")
        borderPaint.color = if (selectedType == BulletType.PIERCE) Color.CYAN else Color.GRAY
        canvas.drawRoundRect(pierceButtonRect, 10f, 10f, buttonPaint)
        canvas.drawRoundRect(pierceButtonRect, 10f, 10f, borderPaint)

        // Vẽ icon pierce ammo (dùng rocket)
        resourceManager.rocket.let { drawable ->
            val iconSize = 52f  // Giảm từ 62f để phù hợp với nút nhỏ hơn
            val iconLeft = pierceButtonRect.left + 12f  // Giảm margin
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
            pierceButtonRect.centerY() + 8f,
            textPaint
        )

        // Vẽ nút stun ammo
        buttonPaint.color = if (selectedType == BulletType.STUN) Color.parseColor("#9933FF") else Color.parseColor("#DD6600CC")  // Màu tím cho stun
        borderPaint.color = if (selectedType == BulletType.STUN) Color.MAGENTA else Color.GRAY
        canvas.drawRoundRect(stunButtonRect, 10f, 10f, buttonPaint)
        canvas.drawRoundRect(stunButtonRect, 10f, 10f, borderPaint)

        // Vẽ icon stun ammo (dùng stun drawable)
        resourceManager.stunBullet.let { drawable ->
            val iconSize = 52f  // Giảm từ 62f để phù hợp với nút nhỏ hơn
            val iconLeft = stunButtonRect.left + 12f  // Giảm margin
            val iconTop = stunButtonRect.centerY() - iconSize / 2
            drawable.setBounds(
                iconLeft.toInt(),
                iconTop.toInt(),
                (iconLeft + iconSize).toInt(),
                (iconTop + iconSize).toInt()
            )
            drawable.draw(canvas)
        }

        // Vẽ số lượng stun ammo
        canvas.drawText(
            "$stunAmmo",
            stunButtonRect.centerX() + 15f,
            stunButtonRect.centerY() + 8f,
            textPaint
        )

        // Vẽ nút build wall
        buttonPaint.color = if (buildMode) Color.parseColor("#FF6600") else Color.parseColor("#DD444444")  // Màu cam cho build
        borderPaint.color = if (buildMode) Color.parseColor("#FFAA00") else Color.GRAY
        canvas.drawRoundRect(buildButtonRect, 10f, 10f, buttonPaint)
        canvas.drawRoundRect(buildButtonRect, 10f, 10f, borderPaint)

        // Vẽ icon build wall (🧱 hoặc wall symbol)
        val buildIconPaint = Paint().apply {
            color = Color.WHITE
            textSize = 28f  // Giảm từ 32f để phù hợp với nút nhỏ hơn
            textAlign = Paint.Align.CENTER
            isAntiAlias = true
            style = Paint.Style.FILL_AND_STROKE
            strokeWidth = 2f
        }
        canvas.drawText(
            "🧱",
            buildButtonRect.centerX(),
            buildButtonRect.centerY() + 8f,  // Giảm từ 10f
            buildIconPaint
        )
    }

    /**
     * ❤️ Vẽ UI chính với lives, goal counter và timer - căn giữa cả ba
     */
    fun drawMainUI(canvas: Canvas, lives: Int, maxLives: Int, currentGoalCount: Int, totalGoalCount: Int, elapsedTime: Long) {
        // Tính toán vị trí để căn giữa cả ba elements
        val elementWidth = 150f  // Width của lives và goal UI
        val timerWidth = 120f    // Width của timer UI
        val gap = 15f           // Khoảng cách giữa các elements

        // Tổng width của cả ba elements và gaps
        val totalWidth = elementWidth + gap + elementWidth + gap + timerWidth
        val startX = screenWidth / 2f - totalWidth / 2f  // Căn giữa toàn bộ nhóm

        // Vẽ từng element với vị trí tính toán
        drawLivesUI(canvas, lives, maxLives, startX)
        drawGoalCounter(canvas, currentGoalCount, totalGoalCount, startX + elementWidth + gap)
        drawTimerUI(canvas, elapsedTime, startX + elementWidth + gap + elementWidth + gap)
    }

    /**
     * ❤️ Vẽ lives UI (di chuyển từ EffectRenderer)
     */
    private fun drawLivesUI(canvas: Canvas, lives: Int, maxLives: Int, startX: Float = screenWidth / 2f - 150f - 10f) {
        // Vẽ lives UI tại vị trí startX
        val uiWidth = 150f
        val uiHeight = 100f
        val uiRect = RectF(
            startX,                           // Vị trí được truyền vào
            200f,                             // Cách top 250px (thấp xuống thêm 100px)
            startX + uiWidth,                 // Width 150px
            250f + uiHeight                   // Chiều cao
        )

        // Vẽ nền
        val uiPaint = Paint().apply {
            color = Color.parseColor("#FFFF99")  // Nền vàng cho lives
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

        // Vẽ số mạng ở dưới dạng current/max
        val livesTextPaint = Paint().apply {
            color = Color.BLACK
            textSize = 40f
            textAlign = Paint.Align.CENTER
            isAntiAlias = true
            style = Paint.Style.FILL_AND_STROKE
            strokeWidth = 1f
        }
        canvas.drawText("$lives/$maxLives", centerX, uiRect.bottom - 20f, livesTextPaint)
    }

    /**
     * 🎯 Vẽ counter hiển thị số hộp đã vào goal (tương tự lives UI)
     */
    fun drawGoalCounter(canvas: Canvas, currentCount: Int, totalCount: Int, startX: Float = screenWidth / 2f + 10f) {
        // Đặt goal counter tại vị trí startX
        val uiWidth = 150f
        val uiHeight = 100f

        val counterRect = RectF(
            startX,                                  // Vị trí được truyền vào
            200f,                                     // Cùng level y với lives UI
            startX + uiWidth,                         // Width 150px (giống lives)
            250f + uiHeight                           // Height 100px (giống lives)
        )

        // Vẽ nền (tương tự lives UI nhưng màu khác)
        val uiPaint = Paint().apply {
            color = Color.parseColor("#99FF99")  // Nền xanh lá cho boxes (tương tự vàng của lives)
            style = Paint.Style.FILL
        }
        canvas.drawRoundRect(counterRect, 15f, 15f, uiPaint)

        // Vẽ viền (tương tự lives UI)
        val borderPaint = Paint().apply {
            color = Color.parseColor("#00AA00")  // Viền xanh lá đậm
            style = Paint.Style.STROKE
            strokeWidth = 3f
        }
        canvas.drawRoundRect(counterRect, 15f, 15f, borderPaint)

        // Vẽ box icon (thay vì text "Boxes:")
        val iconSize = 62f
        val iconLeft = counterRect.centerX() - iconSize / 2
        val iconTop = counterRect.top + 20f
        val iconRight = iconLeft + iconSize
        val iconBottom = iconTop + iconSize

        resourceManager.boxIcon.setBounds(
            iconLeft.toInt(),
            iconTop.toInt(),
            iconRight.toInt(),
            iconBottom.toInt()
        )
        resourceManager.boxIcon.draw(canvas)

        // Vẽ số lượng current/total (ở dưới icon, tương tự lives UI)
        val textPaint = Paint().apply {
            color = if (currentCount == totalCount) Color.GREEN else Color.BLACK
            textSize = 40f  // Nhỏ hơn lives UI (62f) vì có ít số hơn
            textAlign = Paint.Align.CENTER
            isAntiAlias = true
            style = Paint.Style.FILL_AND_STROKE
            strokeWidth = 1f
        }

        val centerX = counterRect.centerX()
        val centerY = counterRect.bottom - 20f  // Cách bottom 20px

        // Shadow effect nhẹ
        val shadowPaint = Paint().apply {
            color = Color.BLACK
            textSize = 40f
            textAlign = Paint.Align.CENTER
            isAntiAlias = true
            style = Paint.Style.FILL_AND_STROKE
            strokeWidth = 0.5f
        }
        val countText = "$currentCount/$totalCount"
        canvas.drawText(countText, centerX + 1f, centerY + 1f, shadowPaint)
        canvas.drawText(countText, centerX, centerY, textPaint)

        // Vẽ star emoji nếu đã hoàn thành (ở góc trên phải)
        if (currentCount == totalCount && totalCount > 0) {
            val starPaint = Paint().apply {
                textSize = 24f
                textAlign = Paint.Align.RIGHT
                isAntiAlias = true
            }
            canvas.drawText(
                "⭐",
                counterRect.right - 10f,
                counterRect.top + 30f,
                starPaint
            )
        }
    }

    /**
     * ⏱️ Vẽ timer hiển thị thời gian level (cạnh lives và goal counter)
     */
    private fun drawTimerUI(canvas: Canvas, elapsedTime: Long, startX: Float = screenWidth / 2f + 150f + 40f) {
        // Đặt timer tại vị trí startX
        val timerWidth = 120f  // Thu nhỏ để vừa cả ba elements
        val timerHeight = 100f  // Cùng height với lives/goal UI

        val timerRect = RectF(
            startX,                          // Vị trí được truyền vào
            200f,                           // Cùng level y với lives/goal UI
            startX + timerWidth,            // Width nhỏ hơn
            250f + timerHeight              // Cùng height
        )

        // Vẽ nền với gradient effect
        val bgPaint = Paint().apply {
            color = Color.parseColor("#CC000000")  // Semi-transparent black
            style = Paint.Style.FILL
        }
        canvas.drawRoundRect(timerRect, 10f, 10f, bgPaint)

        // Vẽ border
        val borderPaint = Paint().apply {
            color = Color.parseColor("#FF4444")  // Red border
            style = Paint.Style.STROKE
            strokeWidth = 2f
        }
        canvas.drawRoundRect(timerRect, 10f, 10f, borderPaint)

        // Convert elapsed time to minutes:seconds format
        val totalSeconds = elapsedTime / 1000
        val minutes = totalSeconds / 60
        val seconds = totalSeconds % 60

        val timeText = String.format("%02d:%02d", minutes, seconds)

        // Vẽ text thời gian
        val timePaint = Paint().apply {
            color = Color.WHITE
            textSize = 32f
            textAlign = Paint.Align.CENTER
            isAntiAlias = true
            typeface = Typeface.create(Typeface.MONOSPACE, Typeface.BOLD)  // Monospace để căn chỉnh số
        }

        val centerX = timerRect.centerX()
        val centerY = timerRect.bottom - 20f  // Ở dòng dưới, cách bottom 20px

        // Shadow effect
        val shadowPaint = Paint().apply {
            color = Color.BLACK
            textSize = 32f
            textAlign = Paint.Align.CENTER
            isAntiAlias = true
            typeface = Typeface.create(Typeface.MONOSPACE, Typeface.BOLD)
        }
        canvas.drawText(timeText, centerX + 1f, centerY + 1f, shadowPaint)
        canvas.drawText(timeText, centerX, centerY, timePaint)

        // Vẽ time icon ở dòng trên
        val iconSize = 62f
        val iconLeft = timerRect.centerX() - iconSize / 2  // Căn giữa ngang
        val iconTop = timerRect.top + 20f                  // Cách top 15px
        val iconRight = iconLeft + iconSize
        val iconBottom = iconTop + iconSize

        resourceManager.timeIcon.setBounds(
            iconLeft.toInt(),
            iconTop.toInt(),
            iconRight.toInt(),
            iconBottom.toInt()
        )
        resourceManager.timeIcon.draw(canvas)
    }
}
