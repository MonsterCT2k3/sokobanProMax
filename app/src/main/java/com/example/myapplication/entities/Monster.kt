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
    var isActive: Boolean = true
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
}

sealed class MonsterAIState {
    data class PatrolState(
        val patrolPoints: List<Pair<Int, Int>>,
        var currentPointIndex: Int = 0
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
}
