package com.example.myapplication.input

import android.view.MotionEvent
import kotlin.math.abs
import kotlin.math.sqrt

class InputHandler {
    
    // Swipe gesture variables
    private var startX = 0f
    private var startY = 0f
    private var endX = 0f
    private var endY = 0f
    private val minSwipeDistance = 100f // Khoảng cách tối thiểu để nhận diện swipe
    
    // Callback interface để thông báo về player movement
    interface PlayerMoveListener {
        fun onPlayerMove(dx: Int, dy: Int)
    }
    
    private var playerMoveListener: PlayerMoveListener? = null
    
    fun setPlayerMoveListener(listener: PlayerMoveListener) {
        playerMoveListener = listener
    }
    
    fun handleTouchEvent(event: MotionEvent): Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                // Lưu vị trí bắt đầu chạm
                startX = event.x
                startY = event.y
                return true
            }

            MotionEvent.ACTION_UP -> {
                // Lưu vị trí kết thúc chạm
                endX = event.x
                endY = event.y

                // Return kết quả từ handleSwipe()
                val isSwipe = handleSwipe()
                return isSwipe // true nếu là swipe, false nếu là tap
            }
        }
        return false
    }
    
    private fun handleSwipe(): Boolean {
        val deltaX = endX - startX
        val deltaY = endY - startY

        // Kiểm tra khoảng cách swipe có đủ dài không
        val distance = sqrt((deltaX * deltaX + deltaY * deltaY).toDouble()).toFloat()
        if (distance < minSwipeDistance) {
            return false // ❌ KHÔNG PHẢI SWIPE → GameView xử lý bắn đạn
        }

        // Xác định hướng swipe
        val absX = abs(deltaX)
        val absY = abs(deltaY)

        if (absX > absY) {
            // Swipe ngang (trái/phải)
            if (deltaX > 0) {
                // Swipe phải
                playerMoveListener?.onPlayerMove(0, 1)
            } else {
                // Swipe trái
                playerMoveListener?.onPlayerMove(0, -1)
            }
        } else {
            // Swipe dọc (lên/xuống)
            if (deltaY > 0) {
                // Swipe xuống
                playerMoveListener?.onPlayerMove(1, 0)
            } else {
                // Swipe lên
                playerMoveListener?.onPlayerMove(-1, 0)
            }
        }

        return true // ✅ ĐÃ XỬ LÝ SWIPE
    }
    
    // Method để set khoảng cách swipe tối thiểu nếu cần
    fun setMinSwipeDistance(distance: Float) {
        // minSwipeDistance = distance (nếu cần thay đổi từ bên ngoài)
    }
    
    // Getter methods cho debugging
    fun getLastSwipeDirection(): SwipeDirection {
        val deltaX = endX - startX
        val deltaY = endY - startY
        val distance = sqrt((deltaX * deltaX + deltaY * deltaY).toDouble()).toFloat()
        
        if (distance < minSwipeDistance) {
            return SwipeDirection.NONE
        }
        
        val absX = abs(deltaX)
        val absY = abs(deltaY)
        
        return if (absX > absY) {
            if (deltaX > 0) SwipeDirection.RIGHT else SwipeDirection.LEFT
        } else {
            if (deltaY > 0) SwipeDirection.DOWN else SwipeDirection.UP
        }
    }
    
    enum class SwipeDirection {
        NONE, UP, DOWN, LEFT, RIGHT
    }
}
