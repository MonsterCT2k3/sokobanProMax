package com.example.myapplication

import android.content.Context
import android.graphics.Canvas
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import androidx.core.content.ContextCompat
import kotlin.math.min

class GameView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private var map: Array<CharArray> = arrayOf() // Bản đồ game
    private var playerX: Int = 0 // Tọa độ X của người chơi
    private var playerY: Int = 0 // Tọa độ Y của người chơi
    private val goalPositions = mutableSetOf<Pair<Int, Int>>() // Lưu các vị trí mục tiêu
    private lateinit var wall: Drawable
    private lateinit var box: Drawable
    private lateinit var goal: Drawable
    private lateinit var player: Drawable
    private lateinit var floor: Drawable

    init {
        initGame()
    }

    private fun initGame() {
        map = arrayOf(
            charArrayOf('#', '#', '#', '#', '#', '#', '#', '#', '#', '#'),
            charArrayOf('#', '.', '.', '.', '.', '.', '.', '.', '.', '#'),
            charArrayOf('#', '.', 'B', '.', '.', 'G', '.', '.', '.', '#'),
            charArrayOf('#', '.', '.', '#', '.', '.', '#', '.', '.', '#'),
            charArrayOf('#', '.', '#', '.', '#', '.', '.', 'B', '.', '#'),
            charArrayOf('#', '.', '.', '.', '.', '.', '.', '.', '.', '#'),
            charArrayOf('#', 'G', '.', '@', '.', 'B', '.', '.', '.', '#'),
            charArrayOf('#', '.', '.', '.', '#', '.', '.', '.', '.', '#'),
            charArrayOf('#', '.', '#', '.', '.', '.', '#', '.', '.', '#'),
            charArrayOf('#', '.', '.', 'B', '.', '.', '.', '.', '.', '#'),
            charArrayOf('#', '.', '.', '.', '.', '.', '.', '.', '.', '#'),
            charArrayOf('#', '#', '#', '#', '#', '#', '#', '#', '#', '#')
        )
        for (i in map.indices) {
            for (j in map[i].indices) {
                if (map[i][j] == '@') {
                    playerX = i
                    playerY = j
                }
                if (map[i][j] == 'G') {
                    goalPositions.add(Pair(i, j))
                }
            }
        }
        wall = ContextCompat.getDrawable(context, R.drawable.wall) ?: throw IllegalStateException("wall drawable not found")
        box = ContextCompat.getDrawable(context, R.drawable.box) ?: throw IllegalStateException("box drawable not found")
        goal = ContextCompat.getDrawable(context, R.drawable.goal) ?: throw IllegalStateException("goal drawable not found")
        player = ContextCompat.getDrawable(context, R.drawable.player) ?: throw IllegalStateException("player drawable not found")
        floor = ContextCompat.getDrawable(context, R.drawable.floor) ?: throw IllegalStateException("floor drawable not found")
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        if (map.isEmpty() || map[0].isEmpty()) return
        val tileSize = min(width / map[0].size, height / map.size)
        for (i in map.indices) {
            for (j in map[i].indices) {
                val x = j * tileSize
                val y = i * tileSize
                val tile = getTileDrawable(map[i][j])
                tile.setBounds(x, y, x + tileSize, y + tileSize)
                tile.draw(canvas)
            }
        }
    }

    private fun getTileDrawable(tile: Char): Drawable {
        return when (tile) {
            '#' -> wall
            'B' -> box
            'G' -> goal
            '@' -> player
            else -> floor
        }
    }

    fun movePlayer(dx: Int, dy: Int) {
        val newX = playerX + dx
        val newY = playerY + dy

        // Kiểm tra di chuyển hợp lệ
        if (isValidMove(newX, newY)) {
            if (map[newX][newY] == 'B') {
                // Nếu ô đích có hộp, kiểm tra ô tiếp theo
                val boxNewX = newX + dx
                val boxNewY = newY + dy
                if (isValidMove(boxNewX, boxNewY)) {
                    // Di chuyển hộp
                    map[boxNewX][boxNewY] = 'B'
                    map[newX][newY] = '@'
                    map[playerX][playerY] = if (goalPositions.contains(Pair(playerX, playerY))) 'G' else '.'
                    playerX = newX
                    playerY = newY
                }
            } else {
                // Di chuyển người chơi
                map[newX][newY] = '@'
                map[playerX][playerY] = if (goalPositions.contains(Pair(playerX, playerY))) 'G' else '.'
                playerX = newX
                playerY = newY
            }
            invalidate() // Yêu cầu vẽ lại giao diện
        }
    }

    private fun isValidMove(x: Int, y: Int): Boolean {
        return x >= 0 && x < map.size && y >= 0 && y < map[0].size && map[x][y] != '#'
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (event.action == MotionEvent.ACTION_DOWN) {
            val x = event.x.toInt()
            val y = event.y.toInt()
            val tileSize = min(width / map[0].size, height / map.size)
            val touchX = y / tileSize
            val touchY = x / tileSize
            if (kotlin.math.abs(touchX - playerX) + kotlin.math.abs(touchY - playerY) == 1) {
                movePlayer(touchX - playerX, touchY - playerY)
            }
        }
        return true
    }
}