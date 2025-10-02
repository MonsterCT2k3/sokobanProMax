package com.example.myapplication.rendering

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import com.example.myapplication.entities.AmmoPickup
import com.example.myapplication.entities.AmmoType
import com.example.myapplication.entities.Bullet
import com.example.myapplication.entities.BulletType
import com.example.myapplication.entities.LivesPickup
import com.example.myapplication.game.GameLogic
import kotlin.math.min

/**
 * 🎯 Hiệu ứng khi hộp đạt goal
 */
data class GoalReachedEffect(
    val centerX: Float,
    val centerY: Float,
    val startTime: Long,
    val duration: Long = Long.MAX_VALUE  // Vô hạn - chỉ xóa khi box ra khỏi goal
)

/**
 * ✨ EffectRenderer - Vẽ các hiệu ứng và effects
 *
 * Nhiệm vụ:
 * - Vẽ bullets (normal, pierce, stun)
 * - Vẽ ammo pickups
 * - Vẽ lives pickups
 * - Vẽ player shield effect
 * - Vẽ ammo UI (deprecated)
 * - Vẽ lives UI
 */
class EffectRenderer(private val resourceManager: ResourceManager) {

    private var screenWidth = 0
    private var screenHeight = 0

    // 🎯 Danh sách các hiệu ứng khi hộp đạt goal
    private val goalReachedEffects = mutableListOf<GoalReachedEffect>()

    fun setScreenSize(width: Int, height: Int) {
        screenWidth = width
        screenHeight = height
    }

    /**
     * 🎯 Thêm hiệu ứng khi hộp đạt goal
     */
    fun addGoalReachedEffect(centerX: Float, centerY: Float) {
        val effect = GoalReachedEffect(centerX, centerY, System.currentTimeMillis())
        goalReachedEffects.add(effect)
    }

    /**
     * 🎯 Xóa hiệu ứng khi hộp ra khỏi goal
     */
    fun removeGoalReachedEffect(centerX: Float, centerY: Float) {
        goalReachedEffects.removeAll { effect ->
            // Check nếu vị trí gần giống nhau (cho phép sai số nhỏ)
            Math.abs(effect.centerX - centerX) < 5f && Math.abs(effect.centerY - centerY) < 5f
        }
    }

    /**
     * 🎯 Vẽ hiệu ứng khi hộp đạt goal - Star Burst Effect
     */
    fun drawGoalReachedEffects(canvas: Canvas, currentTime: Long) {
        val iterator = goalReachedEffects.iterator()
        while (iterator.hasNext()) {
            val effect = iterator.next()
            val elapsed = currentTime - effect.startTime

            // Sau duration thì xóa effect
            if (elapsed >= effect.duration) {
                iterator.remove()
                continue
            }

            // Tính progress và scale
            val progress = min(elapsed.toFloat() / 1500f, 1.0f)  // Scale trong 1.5 giây đầu
            val scale = progress * 1.2f  // Scale từ 0 đến 1.2x

            // Vẽ center glow effect
            val glowPaint = Paint().apply {
                color = Color.parseColor("#FFFF00")  // Bright yellow
                style = Paint.Style.FILL
                alpha = (200 * (1.0f - progress * 0.3f)).toInt()  // Fade slightly
                isAntiAlias = true
            }
            canvas.drawCircle(effect.centerX, effect.centerY, 20f * scale, glowPaint)

            // Vẽ star burst rays
            val rayPaint = Paint().apply {
                color = Color.parseColor("#FFD700")  // Gold
                style = Paint.Style.STROKE
                strokeWidth = 3f
                alpha = (255 * (1.0f - progress * 0.2f)).toInt()
                strokeCap = Paint.Cap.ROUND
                isAntiAlias = true
            }

            val rayLength = 40f * scale
            val rayCount = 8

            for (i in 0 until rayCount) {
                val angle = (i * 360f / rayCount) + (elapsed * 0.002f)  // Rotate slowly
                val radian = Math.toRadians(angle.toDouble())

                val startX = effect.centerX + Math.cos(radian).toFloat() * 15f
                val startY = effect.centerY + Math.sin(radian).toFloat() * 15f
                val endX = effect.centerX + Math.cos(radian).toFloat() * (15f + rayLength)
                val endY = effect.centerY + Math.sin(radian).toFloat() * (15f + rayLength)

                canvas.drawLine(startX, startY, endX, endY, rayPaint)
            }

            // Vẽ sparkle particles
            val sparklePaint = Paint().apply {
                style = Paint.Style.FILL
                isAntiAlias = true
            }

            val sparkleCount = 12
            for (i in 0 until sparkleCount) {
                val angle = (i * 360f / sparkleCount) + (elapsed * 0.003f)
                val distance = 35f + Math.sin((elapsed * 0.008f + i).toDouble()).toFloat() * 10f
                val radian = Math.toRadians(angle.toDouble())

                val sparkleX = effect.centerX + Math.cos(radian).toFloat() * distance * scale
                val sparkleY = effect.centerY + Math.sin(radian).toFloat() * distance * scale

                // Alternate colors for sparkles
                sparklePaint.color = if (i % 3 == 0) Color.parseColor("#FFFF00")  // Yellow
                                   else if (i % 3 == 1) Color.parseColor("#FFD700") // Gold
                                   else Color.parseColor("#FFFFFF") // White

                sparklePaint.alpha = (200 * (1.0f - progress * 0.1f)).toInt()

                canvas.drawCircle(sparkleX, sparkleY, 3f, sparklePaint)
            }

            // Vẽ center star emoji
            val starPaint = Paint().apply {
                textSize = 30f * scale
                textAlign = Paint.Align.CENTER
                alpha = (255 * (1.0f - progress * 0.1f)).toInt()
                isAntiAlias = true
            }
            canvas.drawText("⭐", effect.centerX, effect.centerY + starPaint.textSize * 0.35f, starPaint)
        }
    }

    /**
     * 🎯 Vẽ tất cả bullets lên canvas
     */
    fun drawBullets(canvas: Canvas, bullets: List<Bullet>) {
        bullets.forEach { bullet ->
            if (bullet.isActive) {
                // 🎯 Lấy drawable theo loại bullet
                val bulletDrawable = when (bullet.bulletType) {
                    BulletType.STUN -> resourceManager.stunBullet
                    else -> resourceManager.getBulletDrawable(bullet.direction)
                }

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
                    BulletType.NORMAL -> Color.YELLOW
                    BulletType.PIERCE -> Color.CYAN
                    BulletType.STUN -> Color.MAGENTA
                }

                val trailPaint = Paint().apply {
                    color = trailColor
                    alpha = 150
                }
                canvas.drawCircle(bullet.currentX, bullet.currentY, 5f, trailPaint)
            }
        }
    }

    /**
     * 🔫 Vẽ ammo pickups trên map
     */
    fun drawAmmoPickups(canvas: Canvas, ammoPickups: List<AmmoPickup>, tileSize: Float, offsetX: Float, offsetY: Float) {
        ammoPickups.forEach { ammo ->
            val (screenX, screenY) = ammo.getScreenPosition(tileSize, offsetX, offsetY)

            // Vẽ nền trắng vuông - PHÓNG TO LÊN BẰNG TRÁI TIM (tileSize * 0.6f)
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
                AmmoType.STUN -> Color.MAGENTA
            }
            val borderPaint = Paint().apply {
                color = borderColor
                style = Paint.Style.STROKE
                strokeWidth = 2f
            }
            canvas.drawRect(backgroundRect, borderPaint)

            // Vẽ hình ammo theo type - PHÓNG TO LÊN BẰNG TRÁI TIM
            val ammoDrawable = when (ammo.ammoType) {
                AmmoType.NORMAL -> resourceManager.itemBullet
                AmmoType.PIERCE -> resourceManager.rocket
                AmmoType.STUN -> resourceManager.stunBullet
            }

            ammoDrawable.let { drawable ->
                val ammoSize = tileSize * 0.6f
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
     * ❤️ Vẽ lives pickups trên map
     */
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

    /**
     * 🛡️ Vẽ player shield effect khi ở safe zone
     */
    fun drawPlayerShield(canvas: Canvas, playerRow: Int, playerCol: Int,
                        gameLogic: GameLogic, animationTime: Float,
                        tileSize: Int, offsetX: Float, offsetY: Float) {
        // Check nếu player đang ở safe zone
        val map = gameLogic.getMap()
        val mapChar = map.getOrNull(playerRow)?.getOrNull(playerCol)
        if (mapChar != 'S') return

        val playerCenterX = offsetX + playerCol * tileSize + tileSize / 2f
        val playerCenterY = offsetY + playerRow * tileSize + tileSize / 2f

        // Shield radius với pulsing animation - TĂNG TỐC ĐỘ RÕ RÀNG
        val baseShieldRadius = tileSize * 0.6f
        val pulseSpeed = 0.003f  // Chu kỳ: ~2 giây
        val pulseFactor = (Math.sin((animationTime * pulseSpeed).toDouble()) * 0.2f + 1f).toFloat()
        val shieldRadius = baseShieldRadius * pulseFactor

        // Draw shield glow effect
        val shieldPaint = Paint().apply {
            color = Color.parseColor("#00BFFF") // Deep sky blue
            style = Paint.Style.STROKE
            strokeWidth = 4f
            alpha = 220
            setShadowLayer(12f, 0f, 0f, Color.parseColor("#00BFFF"))
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
        val particlePaint = Paint().apply {
            color = Color.WHITE
            style = Paint.Style.FILL
            alpha = (220 + Math.sin((animationTime * 0.008f).toDouble()) * 55).toInt().coerceIn(200, 275)
            setShadowLayer(4f, 0f, 0f, Color.WHITE)
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
        val arcPaint = Paint().apply {
            color = Color.WHITE
            style = Paint.Style.STROKE
            strokeWidth = 3f
            alpha = (180 + Math.sin((animationTime * 0.01f).toDouble()) * 75).toInt().coerceIn(150, 255)
            strokeCap = Paint.Cap.ROUND
            setShadowLayer(6f, 0f, 0f, Color.WHITE)
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

    /**
     * @deprecated Không còn sử dụng, hiện tại dùng drawBulletTypeButtons
     */
    @Deprecated("Use drawBulletTypeButtons instead")
    fun drawAmmoUI(canvas: Canvas, normalAmmo: Int, pierceAmmo: Int, screenWidth: Float, screenHeight: Float) {
        // Empty - deprecated
    }
}
