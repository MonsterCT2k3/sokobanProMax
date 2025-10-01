package com.example.myapplication.controllers

import android.graphics.RectF
import android.view.MotionEvent
import com.example.myapplication.entities.BulletType
import com.example.myapplication.game.GameLogic
import com.example.myapplication.managers.SoundManager
import com.example.myapplication.rendering.GameRenderer

/**
 * 🎛️ UIManager - Quản lý UI elements và touch handling
 *
 * Tách từ GameView để tập trung logic UI interaction
 */
class UIManager(
    private val gameLogic: GameLogic,
    private val gameRenderer: GameRenderer,
    private val soundManager: SoundManager,
    private val audioController: AudioController,
    private val bulletController: BulletController,
    private val screenWidth: Int,
    private val screenHeight: Int
) {

    // ===== TOUCH HANDLING =====

    /**
     * 🔘 Kiểm tra xem touch có nằm trên nút toggle không
     */
    fun isTouchOnToggleButton(x: Float, y: Float, buttonType: String): Boolean {
        val map = gameLogic.getMap()
        val tileSize = gameRenderer.calculateTileSize(map)
        val boardWidth = map[0].size * tileSize
        val boardHeight = map.size * tileSize
        val offsetX = (screenWidth - boardWidth) / 2f
        val offsetY = (screenHeight - boardHeight) / 2f

        val buttonY = offsetY - 140f  // Cập nhật cho khớp với GameRenderer
        val buttonSize = 120f         // Cập nhật cho khớp với GameRenderer

        val buttonRect = when (buttonType) {
            "music" -> RectF(20f, buttonY, 20f + buttonSize, buttonY + buttonSize)
            "sound" -> RectF(
                screenWidth - buttonSize - 20f, buttonY,
                screenWidth - 20f, buttonY + buttonSize
            )
            else -> return false
        }

        return buttonRect.contains(x, y)
    }

    /**
     * 🎯 Kiểm tra touch trên nút chọn loại đạn
     */
    fun isTouchOnBulletTypeButton(x: Float, y: Float, buttonType: String): Boolean {
        val buttonWidth = 150f  // Cập nhật kích thước mới cho 3 nút
        val buttonHeight = 120f
        val buttonSpacing = 20f
        val bottomMargin = 150f

        val buttonRect = when (buttonType) {
            "normal" -> RectF(
                screenWidth / 2f - buttonWidth * 1.5f - buttonSpacing,
                screenHeight - buttonHeight - bottomMargin,
                screenWidth / 2f - buttonWidth * 0.5f - buttonSpacing / 2,
                screenHeight - bottomMargin
            )
            "pierce" -> RectF(
                screenWidth / 2f - buttonWidth * 0.5f,
                screenHeight - buttonHeight - bottomMargin,
                screenWidth / 2f + buttonWidth * 0.5f,
                screenHeight - bottomMargin
            )
            "stun" -> RectF(
                screenWidth / 2f + buttonWidth * 0.5f + buttonSpacing / 2,
                screenHeight - buttonHeight - bottomMargin,
                screenWidth / 2f + buttonWidth * 1.5f + buttonSpacing,
                screenHeight - bottomMargin
            )
            "build" -> RectF(
                screenWidth / 2f + buttonWidth * 1.5f + buttonSpacing * 1.5f,
                screenHeight - buttonHeight - bottomMargin,
                screenWidth / 2f + buttonWidth * 2.5f + buttonSpacing * 2,
                screenHeight - bottomMargin
            )
            else -> return false
        }

        return buttonRect.contains(x, y)
    }

    /**
     * 👆 Xử lý touch event trên UI buttons
     */
    fun handleUITouch(event: MotionEvent): Boolean {
        if (event.action != MotionEvent.ACTION_UP) return false

        val touchX = event.x
        val touchY = event.y

        // Kiểm tra nút Music (trái)
        if (isTouchOnToggleButton(touchX, touchY, "music")) {
            audioController.toggleMusic()
            return true
        }

        // Kiểm tra nút Sound (phải)
        if (isTouchOnToggleButton(touchX, touchY, "sound")) {
            audioController.toggleSound()
            return true
        }

        // Kiểm tra nút Normal Bullet
        if (isTouchOnBulletTypeButton(touchX, touchY, "normal")) {
            bulletController.setBulletType(BulletType.NORMAL)
            return true
        }

        // Kiểm tra nút Pierce Bullet
        if (isTouchOnBulletTypeButton(touchX, touchY, "pierce")) {
            bulletController.setBulletType(BulletType.PIERCE)
            return true
        }

        // Kiểm tra nút Stun Bullet
        if (isTouchOnBulletTypeButton(touchX, touchY, "stun")) {
            bulletController.setBulletType(BulletType.STUN)
            return true
        }

        // Kiểm tra nút Build Wall
        if (isTouchOnBulletTypeButton(touchX, touchY, "build")) {
            bulletController.toggleBuildMode()
            return true
        }

        return false
    }

    /**
     * 🎮 Xử lý game action (shoot hoặc build)
     */
    fun handleGameAction(): Boolean {
        return if (bulletController.buildMode) {
            bulletController.buildWallInFront()
        } else {
            bulletController.fireBullet()
        }
    }

    /**
     * 📊 Get UI state for rendering
     */
    fun getUIState(): UIState {
        val (normalAmmo, pierceAmmo, stunAmmo) = bulletController.getAmmoCounts()
        return UIState(
            musicEnabled = audioController.isMusicEnabled(),
            soundEnabled = !audioController.isSoundMuted(),
            normalAmmo = normalAmmo,
            pierceAmmo = pierceAmmo,
            stunAmmo = stunAmmo,
            currentBulletType = bulletController.currentBulletType,
            buildMode = bulletController.buildMode
        )
    }

    /**
     * 📊 Data class chứa trạng thái UI
     */
    data class UIState(
        val musicEnabled: Boolean,
        val soundEnabled: Boolean,
        val normalAmmo: Int,
        val pierceAmmo: Int,
        val stunAmmo: Int,
        val currentBulletType: BulletType,
        val buildMode: Boolean
    )

    /**
     * 📏 Update screen size
     */
    fun updateScreenSize(width: Int, height: Int) {
        // Note: In this implementation, we create a new instance when screen size changes
        // This is because screenWidth/screenHeight are final in the constructor
    }
}
