package com.example.myapplication.controllers

import com.example.myapplication.entities.BulletType
import com.example.myapplication.game.GameLogic
import com.example.myapplication.game.PlayerDirection
import com.example.myapplication.managers.SoundManager
import com.example.myapplication.rendering.GameRenderer
import com.example.myapplication.systems.BulletSystem

/**
 * 🎯 BulletController - Quản lý logic đạn và build mode
 *
 * Tách từ GameView để tập trung logic shooting và building
 */
class BulletController(
    private val gameLogic: GameLogic,
    private val gameRenderer: GameRenderer,
    private val bulletSystem: BulletSystem,
    private val soundManager: SoundManager
) {

    // ===== BULLET STATE =====
    var currentBulletType = BulletType.NORMAL
        private set
    var buildMode = false
        private set

    // ===== AMMO STATE =====
    var normalAmmo = 5
    var pierceAmmo = 5
    var stunAmmo = 5

    val maxAmmoPerType = 5

    /**
     * 🔄 Reset ammo về giá trị mặc định
     */
    fun resetAmmo() {
        normalAmmo = 5
        pierceAmmo = 5
        stunAmmo = 5
        currentBulletType = BulletType.NORMAL
        buildMode = false
    }

    /**
     * ➕ Thêm ammo cho loại đạn specified
     */
    fun addAmmo(bulletType: BulletType, amount: Int = 1) {
        when (bulletType) {
            BulletType.NORMAL -> {
                if (normalAmmo < maxAmmoPerType) {
                    normalAmmo = minOf(normalAmmo + amount, maxAmmoPerType)
                }
            }
            BulletType.PIERCE -> {
                if (pierceAmmo < maxAmmoPerType) {
                    pierceAmmo = minOf(pierceAmmo + amount, maxAmmoPerType)
                }
            }
            BulletType.STUN -> {
                if (stunAmmo < maxAmmoPerType) {
                    stunAmmo = minOf(stunAmmo + amount, maxAmmoPerType)
                }
            }
        }
    }

    /**
     * 🔫 Bắn đạn theo hướng player hiện tại
     */
    fun fireBullet(): Boolean {
        // Check xem có đủ ammo cho loại đạn đã chọn không
        val hasAmmo = when (currentBulletType) {
            BulletType.NORMAL -> normalAmmo > 0
            BulletType.PIERCE -> pierceAmmo > 0
            BulletType.STUN -> stunAmmo > 0
        }

        if (!hasAmmo) {
            println("❌ Out of ${currentBulletType} ammo!")
            return false
        }

        // 1️⃣ Lấy vị trí player trên grid và hướng player
        val playerPos = gameLogic.getPlayerPosition()
        val playerDirection = gameLogic.getPlayerDirection()

        // 2️⃣ Convert grid position → screen position
        val tileSize = gameRenderer.calculateTileSize(gameLogic.getMap())
        val (offsetX, offsetY) = gameRenderer.calculateBoardOffset(gameLogic.getMap())

        // 3️⃣ Tính vị trí player trên màn hình (CENTER của tile)
        val playerScreenX = offsetX + playerPos.second * tileSize + tileSize/2  // Center X
        val playerScreenY = offsetY + playerPos.first * tileSize + tileSize/2   // Center Y

        // 4️⃣ Tính target position dựa trên hướng player
        val targetX = when (playerDirection) {
            PlayerDirection.LEFT -> playerScreenX - 2000f
            PlayerDirection.RIGHT -> playerScreenX + 2000f
            PlayerDirection.UP -> playerScreenX
            PlayerDirection.DOWN -> playerScreenX
        }

        val targetY = when (playerDirection) {
            PlayerDirection.LEFT -> playerScreenY
            PlayerDirection.RIGHT -> playerScreenY
            PlayerDirection.UP -> playerScreenY - 800f
            PlayerDirection.DOWN -> playerScreenY + 800f
        }

        // 5️⃣ Bắn đạn theo hướng player
        bulletSystem.addBullet(playerScreenX, playerScreenY, targetX, targetY, currentBulletType)

        // Giảm ammo tương ứng
        when (currentBulletType) {
            BulletType.NORMAL -> normalAmmo--
            BulletType.PIERCE -> pierceAmmo--
            BulletType.STUN -> stunAmmo--
        }

        println("🔫 Fired ${currentBulletType} bullet in direction: $playerDirection")

        // Phát âm thanh bắn đạn
        soundManager.playSound("shoot")

        return true
    }

    /**
     * 🧱 Xây tường ở phía trước player
     */
    fun buildWallInFront(): Boolean {
        val playerPos = gameLogic.getPlayerPosition()
        val playerDirection = gameLogic.getPlayerDirection()

        // Tính vị trí ô phía trước player
        val (frontRow, frontCol) = when (playerDirection) {
            PlayerDirection.UP -> Pair(playerPos.first - 1, playerPos.second)
            PlayerDirection.DOWN -> Pair(playerPos.first + 1, playerPos.second)
            PlayerDirection.LEFT -> Pair(playerPos.first, playerPos.second - 1)
            PlayerDirection.RIGHT -> Pair(playerPos.first, playerPos.second + 1)
        }

        // Kiểm tra bounds và không xây trên player hoặc goal
        val map = gameLogic.getMap()
        if (frontRow in map.indices && frontCol in map[frontRow].indices) {
            val currentCell = map[frontRow][frontCol]
            if (currentCell == '.' || currentCell == ' ') {  // Chỉ xây trên ô trống
                map[frontRow][frontCol] = '#'  // Xây tường
                println("🧱 Built wall at ($frontRow, $frontCol)")
                soundManager.playSound("bump_wall")  // Phát âm thanh xây tường
                return true
            } else {
                println("❌ Cannot build wall at ($frontRow, $frontCol) - cell: $currentCell")
            }
        } else {
            println("❌ Cannot build wall - out of bounds ($frontRow, $frontCol)")
        }

        return false
    }

    /**
     * 🔄 Set loại đạn hiện tại
     */
    fun setBulletType(bulletType: BulletType) {
        currentBulletType = bulletType
        buildMode = false  // Tắt build mode khi chọn đạn
        soundManager.playSound("button_click")
    }

    /**
     * 🔄 Toggle build mode
     */
    fun toggleBuildMode(): Boolean {
        buildMode = !buildMode
        if (buildMode) {
            currentBulletType = BulletType.NORMAL  // Reset về normal bullet
        }
        soundManager.playSound("button_click")
        return buildMode
    }

    /**
     * 📊 Get current ammo counts
     */
    fun getAmmoCounts(): Triple<Int, Int, Int> {
        return Triple(normalAmmo, pierceAmmo, stunAmmo)
    }

    /**
     * 🔍 Check if has ammo for current bullet type
     */
    fun hasAmmoForCurrentType(): Boolean {
        return when (currentBulletType) {
            BulletType.NORMAL -> normalAmmo > 0
            BulletType.PIERCE -> pierceAmmo > 0
            BulletType.STUN -> stunAmmo > 0
        }
    }
}
