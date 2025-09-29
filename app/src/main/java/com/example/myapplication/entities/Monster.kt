package com.example.myapplication.entities

data class Monster(
    val id:String,
    val type: MonsterType,
    var currentX: Float,
    var currentY: Float,
    var targetX: Int,
    var targetY: Int,
    var direction: MonsterDirection = MonsterDirection.DOWN,
    val speed: Float = 2.0f,
    var aiState: MonsterAIState? = null,
    var isActive: Boolean = true,
    var stunTime: Float = 0.0f  // 🆕 Thời gian còn lại bị stun (giây)
){
    fun hasReachedTarget(): Boolean {
        val threshold = 0.1f
        return kotlin.math.abs(currentX-targetX)<threshold && kotlin.math.abs(currentY-targetY)<threshold
    }

    fun distanceToPlayer(playerX: Float, playerY: Float): Float {
        val dx = currentX - playerX
        val dy = currentY - playerY
        return kotlin.math.sqrt(dx * dx + dy * dy)
    }

    // 🆕 Check xem monster có đang bị stun không
    fun isStunned(): Boolean = stunTime > 0.0f

    // 🆕 Stun monster trong thời gian nhất định
    fun stun(duration: Float) {
        stunTime = duration
    }

    // 🆕 Update stun time (gọi mỗi frame)
    fun updateStun(deltaTime: Float) {
        if (stunTime > 0.0f) {
            stunTime -= deltaTime
            if (stunTime < 0.0f) {
                stunTime = 0.0f
            }
        }
    }
}

sealed class MonsterAIState {
    data class PatrolState(
        val startPosition: Pair<Int, Int>,           // Vị trí bắt đầu
        var currentDirection: Pair<Int, Int> = Pair(0, 1)  // Hướng di chuyển hiện tại
    ): MonsterAIState()

    data class CircleState(
        val  circlePoints: List<Pair<Int, Int>>,
        var currentPointIndex: Int = 0
    ): MonsterAIState()

    data class RandomState(
        var nextMoveTime: Long = 0L,
        var nextMoveInterval: Long = 2000L
    ): MonsterAIState()

    data class ChaseState(
        var lastPlayerX: Int = -1,               // Vị trí player lần cuối
        var lastPlayerY: Int = -1,
        var pathToPlayer: List<Pair<Int, Int>> = emptyList()  // Đường đi đến player
    ) : MonsterAIState()

    data class StraightState(
        val startPosition: Pair<Int, Int>,
        val direction: Pair<Int, Int>,
        var isReturning: Boolean = false
    ) : MonsterAIState()

    data class BounceState(
        var currentDirection: Pair<Int, Int> = Pair(0, 1),  // Default: đi sang phải
        var lastDirectionChange: Long = 0L
    ) : MonsterAIState()
}
