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
    private lateinit var wall: Drawable
    private lateinit var box: Drawable
    private lateinit var goal: Drawable
    private lateinit var player: Drawable
    private lateinit var floor: Drawable

    init {
        initGame()
    }

    private fun initGame() {
        // Khởi tạo bản đồ mẫu
        map = arrayOf(
            charArrayOf('#', '#', '#', '#', '#', '#', '#', '#', '#'),
            charArrayOf('#', '.', '.', '.', '#', '.', '.', '.', '#'),
            charArrayOf('#', '.', 'B', '.', '#', '.', 'G', '.', '#'),
            charArrayOf('#', '.', '.', '.', '.', '.', '.', '.', '#'),
            charArrayOf('#', '#', '#', '.', '#', '.', '#', '#', '#'),
            charArrayOf('#', '.', '.', 'B', '.', '.', '.', '.', '#'),
            charArrayOf('#', '.', 'G', '.', '#', '.', '@', '.', '#'),
            charArrayOf('#', '.', '.', '.', '#', '.', '.', '.', '#'),
            charArrayOf('#', '#', '#', '#', '#', '#', '#', '#', '#')
        )
        // Tìm vị trí người chơi
        for (i in map.indices) {
            for (j in map[i].indices) {
                if (map[i][j] == '@') {
                    playerX = i
                    playerY = j
                }
            }
        }
        // Tải tài nguyên hình ảnh
        wall = ContextCompat.getDrawable(context, R.drawable.wall) ?: throw IllegalStateException("wall drawable not found")
        box = ContextCompat.getDrawable(context, R.drawable.box) ?: throw IllegalStateException("box drawable not found")
        goal = ContextCompat.getDrawable(context, R.drawable.goal) ?: throw IllegalStateException("goal drawable not found")
        player = ContextCompat.getDrawable(context, R.drawable.player) ?: throw IllegalStateException("player drawable not found")
        floor = ContextCompat.getDrawable(context, R.drawable.floor) ?: throw IllegalStateException("floor drawable not found")
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        if (map.isEmpty() || map[0].isEmpty()) return // Kiểm tra map rỗng

        // Tính kích thước ô dựa trên kích thước màn hình
        val tileSize = min(width / map[0].size, height / map.size)
        // Vẽ từng ô trên bản đồ
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
                    map[playerX][playerY] = if (map[playerX][playerY] == 'G') 'G' else '.'
                    playerX = newX
                    playerY = newY
                }
            } else {
                // Di chuyển người chơi
                map[newX][newY] = '@'
                map[playerX][playerY] = if (map[playerX][playerY] == 'G') 'G' else '.'
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
            val touchX = y / tileSize // Tọa độ hàng
            val touchY = x / tileSize // Tọa độ cột
            // Kiểm tra xem chạm vào ô lân cận của người chơi
            if (kotlin.math.abs(touchX - playerX) + kotlin.math.abs(touchY - playerY) == 1) {
                movePlayer(touchX - playerX, touchY - playerY)
            }
        }
        return true
    }
}