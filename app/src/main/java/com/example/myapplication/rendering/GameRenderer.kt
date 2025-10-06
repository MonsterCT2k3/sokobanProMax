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
    private val effectRenderer = EffectRenderer(resourceManager)
    private val uiRenderer = UIRenderer(resourceManager)

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
        effectRenderer.setScreenSize(width, height)
        uiRenderer.setScreenSize(width, height)
    }

    // ===== GETTER METHODS FOR SUB-RENDERERS =====

    internal fun getWall(): Drawable = resourceManager.wall
    internal fun getBox(): Drawable = resourceManager.box
    internal fun getGoal(): Drawable = resourceManager.goal
    internal fun getSafeZone(): Drawable = resourceManager.safeZone
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
                     playerDirection: PlayerDirection, monsters: List<Monster>, safeZonePositions: Set<Pair<Int, Int>>) {
        boardRenderer.drawGameBoard(canvas, map, playerRow, playerCol, playerDirection, monsters, safeZonePositions)
    }

    
    /**
     * 🎮 Vẽ UI chính của game
     */
    fun drawGameUI(canvas: Canvas, currentLevelId: Int = 1) {
        uiRenderer.drawGameUI(canvas, currentLevelId)
    }

    /**
     * 🎯 Vẽ tất cả bullets lên canvas
     */
    fun drawBullets(canvas: Canvas, bullets: List<Bullet>) {
        effectRenderer.drawBullets(canvas, bullets)
    }
    

    /**
     * 🎛️ Vẽ các nút toggle nhạc/sound phía trên map
     */
    fun drawToggleButtons(canvas: Canvas, map: Array<CharArray>,
                          musicEnabled: Boolean, soundEnabled: Boolean) {
        uiRenderer.drawToggleButtons(canvas, map, musicEnabled, soundEnabled)
    }


    /**
     * @deprecated Không còn sử dụng
     */
    @Deprecated("Use drawBulletTypeButtons instead")
    fun drawAmmoUI(canvas: Canvas, normalAmmo: Int, pierceAmmo: Int, screenWidth: Float, screenHeight: Float) {
        // Empty - deprecated
    }

    /**
     * ❤️ Vẽ lives UI
     */
    fun drawMainUI(canvas: Canvas, lives: Int, maxLives: Int, currentGoalCount: Int, totalGoalCount: Int, elapsedTime: Long, isSurvivalMode: Boolean) {
        uiRenderer.drawMainUI(canvas, lives, maxLives, currentGoalCount, totalGoalCount, elapsedTime, isSurvivalMode)
    }

    /**
     * 🔫 Vẽ nút chọn loại đạn ở phía dưới
     */
    fun drawBulletTypeButtons(canvas: Canvas, normalAmmo: Int, pierceAmmo: Int, stunAmmo: Int, screenWidth: Float, screenHeight: Float, selectedType: BulletType, buildMode: Boolean) {
        uiRenderer.drawBulletTypeButtons(canvas, normalAmmo, pierceAmmo, stunAmmo, selectedType, buildMode)
    }

    /**
     * ❤️ Vẽ lives pickups
     */
    fun drawLivesPickups(canvas: Canvas, livesPickups: List<LivesPickup>, tileSize: Float, offsetX: Float, offsetY: Float) {
        effectRenderer.drawLivesPickups(canvas, livesPickups, tileSize, offsetX, offsetY)
    }

    /**
     * 🔫 Vẽ ammo pickups
     */
    fun drawAmmoPickups(canvas: Canvas, ammoPickups: List<AmmoPickup>, tileSize: Float, offsetX: Float, offsetY: Float) {
        effectRenderer.drawAmmoPickups(canvas, ammoPickups, tileSize, offsetX, offsetY)
    }

    /**
     * 🛡️ Draw player shield effect when on safe zone
     */
    fun drawPlayerShield(canvas: Canvas, playerRow: Int, playerCol: Int, gameLogic: com.example.myapplication.game.GameLogic, animationTime: Float) {
        val tileSize = min(screenWidth / gameLogic.getMap()[0].size, screenHeight / gameLogic.getMap().size)
        val boardWidth = gameLogic.getMap()[0].size * tileSize
        val boardHeight = gameLogic.getMap().size * tileSize
        val offsetX = (screenWidth - boardWidth) / 2f
        val offsetY = (screenHeight - boardHeight) / 2f

        effectRenderer.drawPlayerShield(canvas, playerRow, playerCol, gameLogic, animationTime, tileSize.toInt(), offsetX, offsetY)
    }

    /**
     * 🎯 Vẽ hiệu ứng khi hộp đạt goal
     */
    fun drawGoalReachedEffects(canvas: Canvas, currentTime: Long) {
        effectRenderer.drawGoalReachedEffects(canvas, currentTime)
    }

    /**
     * 🎯 Thêm hiệu ứng khi hộp đạt goal
     */
    fun addGoalReachedEffect(centerX: Float, centerY: Float) {
        effectRenderer.addGoalReachedEffect(centerX, centerY)
    }

    fun removeGoalReachedEffect(centerX: Float, centerY: Float) {
        effectRenderer.removeGoalReachedEffect(centerX, centerY)
    }

    fun clearGoalReachedEffects() {
        effectRenderer.clearGoalReachedEffects()
    }

    fun drawGoalCounter(canvas: Canvas, currentCount: Int, totalCount: Int) {
        uiRenderer.drawGoalCounter(canvas, currentCount, totalCount)
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

    /**
     * 📍 Tính center position của một tile trên màn hình
     */
    fun calculateTileCenter(map: Array<CharArray>, row: Int, col: Int): Pair<Float, Float> {
        val (offsetX, offsetY) = calculateBoardOffset(map)
        val tileSize = calculateTileSize(map).toFloat()

        val centerX = offsetX + col.toFloat() * tileSize + tileSize / 2f
        val centerY = offsetY + row.toFloat() * tileSize + tileSize / 2f

        return Pair(centerX, centerY)
    }
}