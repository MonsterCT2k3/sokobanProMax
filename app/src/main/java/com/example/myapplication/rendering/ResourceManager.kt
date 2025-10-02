package com.example.myapplication.rendering

import android.content.Context
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.drawable.Drawable
import androidx.core.content.ContextCompat
import com.example.myapplication.R
import com.example.myapplication.entities.BulletDirection
import com.example.myapplication.entities.MonsterType

/**
 * 🎨 ResourceManager - Quản lý tất cả drawables và paints
 *
 * Nhiệm vụ:
 * - Load và cache các drawable resources
 * - Khởi tạo và quản lý Paint objects
 * - Cung cấp getter cho các resources
 */
class ResourceManager(private val context: Context) {

    // ===== TILE DRAWABLES =====
    lateinit var wall: Drawable
        private set
    lateinit var box: Drawable
        private set
    lateinit var goal: Drawable
        private set
    lateinit var safeZone: Drawable
        private set
    lateinit var floor: Drawable
        private set

    // ===== PLAYER DRAWABLES =====
    lateinit var playerUp: Drawable
        private set
    lateinit var playerDown: Drawable
        private set
    lateinit var playerLeft: Drawable
        private set
    lateinit var playerRight: Drawable
        private set

    // ===== MONSTER DRAWABLES =====
    lateinit var monsterPatrol: Drawable
        private set
    lateinit var monsterStraight: Drawable
        private set

    // ===== BULLET DRAWABLES =====
    lateinit var bulletUp: Drawable
        private set
    lateinit var bulletDown: Drawable
        private set
    lateinit var bulletLeft: Drawable
        private set
    lateinit var bulletRight: Drawable
        private set
    lateinit var stunBullet: Drawable
        private set
    lateinit var itemBullet: Drawable
        private set
    lateinit var rocket: Drawable
        private set

    // ===== UI ICON DRAWABLES =====
    lateinit var musicOnIcon: Drawable
        private set
    lateinit var musicOffIcon: Drawable
        private set
    lateinit var soundOnIcon: Drawable
        private set
    lateinit var boxIcon: Drawable
        private set
    lateinit var timeIcon: Drawable
        private set
    lateinit var soundOffIcon: Drawable
        private set

    // ===== PAINT OBJECTS =====
    lateinit var tilePaint: Paint
        private set
    lateinit var textPaint: Paint
        private set
    lateinit var shadowPaint: Paint
        private set
    lateinit var monsterPaint: Paint
        private set

    init {
        loadDrawables()
        initPaints()
    }

    /**
     * Load tất cả drawable resources
     */
    private fun loadDrawables() {
        // Tiles
        wall = getDrawable(R.drawable.wall)
        box = getDrawable(R.drawable.box)
        goal = getDrawable(R.drawable.goal)
        safeZone = getDrawable(R.drawable.safe_zone)
        floor = getDrawable(R.drawable.floor)

        // Player
        playerUp = getDrawable(R.drawable.hero_up)
        playerDown = getDrawable(R.drawable.hero_down)
        playerLeft = getDrawable(R.drawable.hero_left)
        playerRight = getDrawable(R.drawable.hero_right)

        // Monsters
        monsterPatrol = getDrawable(R.drawable.monster_patrol)
        monsterStraight = getDrawable(R.drawable.zombie)

        // Bullets
        bulletUp = getDrawable(R.drawable.bullet_up)
        bulletDown = getDrawable(R.drawable.bullet_down)
        bulletLeft = getDrawable(R.drawable.bullet_left)
        bulletRight = getDrawable(R.drawable.bullet_right)
        stunBullet = getDrawable(R.drawable.stun)
        itemBullet = getDrawable(R.drawable.item_bullet)
        rocket = getDrawable(R.drawable.rocket)

        // UI Icons
        musicOnIcon = getDrawable(R.drawable.music_on)
        musicOffIcon = getDrawable(R.drawable.music_off)
        soundOnIcon = getDrawable(R.drawable.sound_on)
        soundOffIcon = getDrawable(R.drawable.sound_off)
        boxIcon = getDrawable(R.drawable.box)
        timeIcon = getDrawable(R.drawable.time)
    }

    /**
     * Helper function để load drawable với error handling
     */
    private fun getDrawable(resourceId: Int): Drawable {
        return ContextCompat.getDrawable(context, resourceId)
            ?: throw IllegalStateException("Drawable resource $resourceId not found")
    }

    /**
     * Khởi tạo các Paint objects
     */
    private fun initPaints() {
        tilePaint = Paint().apply {
            isAntiAlias = true
            isFilterBitmap = true
        }

        textPaint = Paint().apply {
            isAntiAlias = true
            textAlign = Paint.Align.CENTER
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }

        shadowPaint = Paint().apply {
            isAntiAlias = true
            style = Paint.Style.STROKE
            strokeWidth = 3f
            textAlign = Paint.Align.CENTER
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }

        monsterPaint = Paint().apply {
            isAntiAlias = true
            isFilterBitmap = true
        }
    }

    /**
     * 👹 Lấy drawable cho monster theo type
     */
    fun getMonsterDrawable(type: MonsterType): Drawable {
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
    fun getBulletDrawable(direction: BulletDirection): Drawable {
        return when (direction) {
            BulletDirection.UP -> bulletUp
            BulletDirection.DOWN -> bulletDown
            BulletDirection.LEFT -> bulletLeft
            BulletDirection.RIGHT -> bulletRight
        }
    }
}
