package com.example.myapplication.systems

import com.example.myapplication.entities.Monster
import com.example.myapplication.entities.MonsterAIState
import com.example.myapplication.entities.MonsterDirection
import com.example.myapplication.entities.MonsterType
import com.example.myapplication.models.MonsterData
import kotlin.math.abs

class MonsterSystem {
    private val monsters = mutableListOf<Monster>()

    fun addMonster(monster: Monster) {
        monsters.add(monster)
        println("🎮 Added monster: ${monster.id} at (${monster.currentX}, ${monster.currentY})")
    }

    fun clearMonsters() {
        monsters.clear()
        println("🧹 Cleared all monsters")
    }

    fun updateMonsters(deltaTime:Float, playerX: Int, playerY: Int, map:Array<CharArray>) {
        monsters.forEach { monster ->
            if(monster.isActive) {
                updateMonsterAI(monster, playerX, playerY, map)
                updateMonsterMovement(monster, deltaTime)
                updateMonsterDirection(monster)
            }
        }
    }

    fun getActiveMonsters(): List<Monster> {
        return monsters.filter { it.isActive }
    }

    fun checkPlayerCollision(playerX: Int, playerY: Int): Boolean {
        return monsters.any { monster ->
            monster.isActive && isColliding(monster.currentX, monster.currentY, playerX.toFloat(), playerY.toFloat())
        }
    }

    //private methods for AI, movement, direction, collision

    private fun updateMonsterAI(monster: Monster, playerX: Int, playerY: Int, map:Array<CharArray>) {
        when(monster.type) {
            MonsterType.PATROL -> updatePatrolAI(monster)
            MonsterType.CIRCLE -> updateCircleAI(monster)
            MonsterType.RANDOM -> updateRandomAI(monster, map)
            MonsterType.CHASE -> updateChaseAI(monster, playerX, playerY, map)
            MonsterType.STRAIGHT -> updateStraightAI(monster, map)
        }
    }

    private fun updatePatrolAI(monster: Monster) {
        val patrolState = monster.aiState as? MonsterAIState.PatrolState ?: return
        if (monster.hasReachedTarget()){
            val nextIndex = (patrolState.currentPointIndex+1) % patrolState.patrolPoints.size
            patrolState.currentPointIndex = nextIndex
            val nextPoint = patrolState.patrolPoints[nextIndex]
            monster.targetX = nextPoint.first
            monster.targetY = nextPoint.second

            println("🔄 ${monster.id} patrol to point ${nextIndex}: (${monster.targetX}, ${monster.targetY})")
        }
    }

    private fun updateCircleAI(monster: Monster) {
        val circleState = monster.aiState as? MonsterAIState.CircleState ?: return
        if (monster.hasReachedTarget()){
            val nextIndex = (circleState.currentPointIndex+1) % circleState.circlePoints.size
            circleState.currentPointIndex = nextIndex
            val nextPoint = circleState.circlePoints[nextIndex]
            monster.targetX = nextPoint.first
            monster.targetY = nextPoint.second

            println("🔄 ${monster.id} circle to point ${nextIndex}: (${monster.targetX}, ${monster.targetY})")
        }
    }

    private fun updateRandomAI(monster: Monster, map:Array<CharArray>) {
        val randomState = monster.aiState as? MonsterAIState.RandomState ?: return
        val currentTime = System.currentTimeMillis()
        if(currentTime>=randomState.nextMoveTime && monster.hasReachedTarget()){
            val directions = listOf(
                Pair(0, -1), // Up
                Pair(0, 1),  // Down
                Pair(-1, 0), // Left
                Pair(1, 0)   // Right
            )

            val direction = directions.random()
            val newX = monster.targetX + direction.first
            val newY = monster.targetY + direction.second
            if(isValidPosition(newX, newY, map, monster)) {
                monster.targetX = newX
                monster.targetY = newY
            }
            randomState.nextMoveTime = currentTime + randomState.nextMoveInterval
        }
    }

    private fun updateChaseAI(monster:Monster, playerX: Int, playerY: Int, map:Array<CharArray>) {
        // TODO: Implement trong Phase 3 - A* pathfinding
        // Hiện tại chỉ move random
        updateRandomAI(monster, map)
    }

    /**
     * ➡️ AI cho Straight Monster - đi thẳng đến khi chạm tường
     */
    private fun updateStraightAI(monster: Monster, map: Array<CharArray>) {
        val straightState = monster.aiState as? MonsterAIState.StraightState ?: return

        // Debug: In trạng thái hiện tại
        println("🔍 ${monster.id} DEBUG - Current: (${monster.currentX}, ${monster.currentY}) Target: (${monster.targetX}, ${monster.targetY}) Returning: ${straightState.isReturning}")

        // Đã đến đích (target) → tính target tiếp theo
        if (monster.hasReachedTarget()) {
            if (straightState.isReturning) {
                // ✅ ĐÃ VỀ ĐIỂM XUẤT PHÁT → BẮT ĐẦU ĐI THẲNG LẠI
                println("🔄 ${monster.id}: Đã về điểm xuất phát (${straightState.startPosition}), bắt đầu đi thẳng")
                straightState.isReturning = false

                // Set target đầu tiên theo hướng
                val startX = straightState.startPosition.first
                val startY = straightState.startPosition.second
                monster.targetX = startX + straightState.direction.first
                monster.targetY = startY + straightState.direction.second

                println("➡️ ${monster.id}: Target đầu tiên: (${monster.targetX}, ${monster.targetY})")

            } else {
                // ✅ ĐANG ĐI THẲNG → KIỂM TRA CÓ THỂ ĐI TIẾP KHÔNG
                val nextX = monster.targetX + straightState.direction.first
                val nextY = monster.targetY + straightState.direction.second

                if (isValidPosition(nextX, nextY, map, monster)) {
                    // ✅ CÒN ĐI ĐƯỢC → TIẾP TỤC ĐI THẲNG
                    monster.targetX = nextX
                    monster.targetY = nextY
                    println("➡️ ${monster.id}: Tiếp tục đi thẳng đến (${nextX}, ${nextY})")
                } else {
                    // ❌ GẶP TƯỜNG → TELEPORT NGAY VỀ ĐIỂM XUẤT PHÁT
                    println("⚡ ${monster.id}: Gặp tường tại (${nextX}, ${nextY}), TELEPORT về điểm xuất phát!")

                    // 1️⃣ Teleport vị trí hiện tại về điểm xuất phát
                    monster.currentX = straightState.startPosition.first.toFloat()
                    monster.currentY = straightState.startPosition.second.toFloat()

                    // 2️⃣ Reset trạng thái returning
                    straightState.isReturning = false

                    // 3️⃣ Set target mới để bắt đầu đi thẳng lại
                    monster.targetX = straightState.startPosition.first + straightState.direction.first
                    monster.targetY = straightState.startPosition.second + straightState.direction.second

                    println("✨ ${monster.id}: Đã teleport về (${monster.currentX}, ${monster.currentY}) và bắt đầu đi thẳng đến (${monster.targetX}, ${monster.targetY})")
                }
            }
        }
    }

    //update movement
    private fun updateMonsterMovement(monster: Monster, deltaTime: Float) {
        val moveDistance = monster.speed*deltaTime
        val dx = monster.targetX - monster.currentX
        val dy = monster.targetY - monster.currentY
        val distance = kotlin.math.sqrt(dx * dx + dy * dy)
        if(distance > 0.05f) {
            //normalize and move
            val normalizedDx = dx / distance
            val normalizedDy = dy / distance

            monster.currentY += normalizedDy * moveDistance
            monster.currentX += normalizedDx * moveDistance
        }else{
            monster.currentX = monster.targetX.toFloat()
            monster.currentY = monster.targetY.toFloat()
        }
    }

    //update direction
    private fun updateMonsterDirection(monster: Monster) {
        val dx = monster.targetX - monster.currentX
        val dy = monster.targetY - monster.currentY
        monster.direction = when {
            abs(dx) > abs(dy) -> if (dx > 0) MonsterDirection.RIGHT else MonsterDirection.LEFT
            dy > 0 -> MonsterDirection.DOWN
            dy < 0 -> MonsterDirection.UP
            else -> monster.direction // No change
        }
    }

    private fun isColliding(x1:Float, y1: Float, x2: Float, y2: Float): Boolean {
        val thresholds = 0.4f // Khoảng cách để coi là va chạm
        return abs(x1 - x2) < thresholds && abs(y1 - y2) < thresholds
    }

    /**
     * ✅ Check vị trí có hợp lệ không (không là tường và trong bounds)
     */
    private fun isValidPosition(x: Int, y: Int, map: Array<CharArray>, monster: Monster): Boolean {
        // Check bounds
        if (x < 0 || x >= map.size || y < 0 || y >= map[0].size) {
            return false
        }

        // Check không phải tường
        if (map[x][y] == '#' || map[x][y] == 'B') {
            return false
        }

        // Có thể thêm check khác: không đi vào box, không đi vào goal, etc.
        return true
    }



    // factory method to create Monster from MonsterData
    fun createMonsterFromData(monsterData: MonsterData, id:String): Monster {
        val aiState = when (monsterData.type) {
            MonsterType.PATROL -> {
                MonsterAIState.PatrolState(
                    patrolPoints = monsterData.patrolPoints,
                    currentPointIndex = 0
                )
            }
            MonsterType.CIRCLE -> {
                MonsterAIState.CircleState(
                    circlePoints = monsterData.patrolPoints,
                    currentPointIndex = 0
                )
            }
            MonsterType.RANDOM -> {
                MonsterAIState.RandomState(
                    nextMoveTime = System.currentTimeMillis()+1000L,
                    nextMoveInterval = 2000L
                )
            }
            MonsterType.CHASE -> {
                MonsterAIState.ChaseState()
            }
            MonsterType.STRAIGHT -> {
                // THÊM MỚI: Lấy direction từ patrolPoints (điểm đầu tiên là vector hướng)
                val direction = if (monsterData.patrolPoints.isNotEmpty()) {
                    monsterData.patrolPoints[0]
                } else {
                    Pair(0, 1)  // Default: đi sang phải
                }

                MonsterAIState.StraightState(
                    startPosition = Pair(monsterData.startRow, monsterData.startColumn),
                    direction = direction,
                    isReturning = false
                )
            }
    }
        val initialTarget = if (monsterData.type == MonsterType.STRAIGHT) {
            // Với STRAIGHT, target ban đầu là vị trí hiện tại + direction
            val straightState = aiState as MonsterAIState.StraightState
            Pair(
                monsterData.startRow + straightState.direction.first,
                monsterData.startColumn + straightState.direction.second
            )
        } else if (monsterData.patrolPoints.isNotEmpty()) {
            monsterData.patrolPoints[0]
        } else {
            Pair(monsterData.startRow, monsterData.startColumn)
        }

        return Monster(
            id = id,
            type = monsterData.type,
            currentX = monsterData.startRow.toFloat(),
            currentY = monsterData.startColumn.toFloat(),
            targetX = initialTarget.first,
            targetY = initialTarget.second,
            speed = monsterData.speed,
            direction = MonsterDirection.DOWN,
            aiState = aiState,
            isActive = true
        )
    }
}