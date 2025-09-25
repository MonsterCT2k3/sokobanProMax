package com.example.myapplication.systems

import com.example.myapplication.entities.Monster
import com.example.myapplication.entities.MonsterAIState
import com.example.myapplication.entities.MonsterDirection
import com.example.myapplication.entities.MonsterType
import com.example.myapplication.models.MonsterData
import kotlin.math.abs
import kotlin.math.sqrt

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
                updateMonsterMovement(monster, deltaTime, map)
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
            MonsterType.PATROL -> updatePatrolAI(monster, map)
            MonsterType.CIRCLE -> updateCircleAI(monster)
            MonsterType.RANDOM -> updateRandomAI(monster, map)
            MonsterType.CHASE -> updateChaseAI(monster, playerX, playerY, map)
            MonsterType.STRAIGHT -> updateStraightAI(monster, map)
            MonsterType.BOUNCE -> updateBounceAI(monster, map)
        }
    }

    private fun updatePatrolAI(monster: Monster, map: Array<CharArray>) {
        val patrolState = monster.aiState as? MonsterAIState.PatrolState ?: return

        // Chỉ xử lý khi monster đã đến target hiện tại
        if (monster.hasReachedTarget()) {
            // Kiểm tra có thể tiếp tục di chuyển theo hướng hiện tại không
            val nextX = monster.currentX.toInt() + patrolState.currentDirection.first
            val nextY = monster.currentY.toInt() + patrolState.currentDirection.second

            if (isValidPosition(nextX, nextY, map, monster)) {
                // Có thể tiếp tục đi, set target mới
                monster.targetX = nextX
                monster.targetY = nextY
                println("➡️ ${monster.id} continuing in direction: (${monster.targetX}, ${monster.targetY})")
            } else {
                // Không thể tiếp tục, quay đầu
                patrolState.currentDirection = Pair(
                    -patrolState.currentDirection.first,
                    -patrolState.currentDirection.second
                )

                // Set target mới theo hướng ngược lại
                val reverseTargetX = monster.currentX.toInt() + patrolState.currentDirection.first
                val reverseTargetY = monster.currentY.toInt() + patrolState.currentDirection.second

                if (isValidPosition(reverseTargetX, reverseTargetY, map, monster)) {
                    monster.targetX = reverseTargetX
                    monster.targetY = reverseTargetY
                    println("🔄 ${monster.id} hit obstacle, reversing to: (${monster.targetX}, ${monster.targetY})")
                } else {
                    // Nếu hướng ngược lại cũng không hợp lệ, dừng lại tại chỗ
                    monster.targetX = monster.currentX.toInt()
                    monster.targetY = monster.currentY.toInt()
                    println("🔄 ${monster.id} stuck, stopping at current position")
                }
            }
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
    private fun updateMonsterMovement(monster: Monster, deltaTime: Float, map: Array<CharArray>) {
        // Kiểm tra target position có hợp lệ không trước khi di chuyển
        if (!isValidPosition(monster.targetX, monster.targetY, map, monster)) {
            println("❌ ${monster.id} target (${monster.targetX}, ${monster.targetY}) is invalid! Not moving.")
            return
        }

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
     * 🎾 BOUNCE AI: Đi thẳng cho đến khi gặp chướng ngại vật thì chuyển hướng ngẫu nhiên
     */
    private fun updateBounceAI(monster: Monster, map: Array<CharArray>) {
        val bounceState = monster.aiState as? MonsterAIState.BounceState ?: return
        
        // Nếu monster đã đến target, tính target tiếp theo
        if (monster.hasReachedTarget()) {
            val currentX = monster.currentX.toInt()
            val currentY = monster.currentY.toInt()
            
            // Thử tiếp tục đi theo hướng hiện tại
            val nextX = currentX + bounceState.currentDirection.first
            val nextY = currentY + bounceState.currentDirection.second
            
            // Nếu có thể tiếp tục đi thẳng
            if (isValidPosition(nextX, nextY, map, monster)) {
                monster.targetX = nextX
                monster.targetY = nextY
                println("🎾 ${monster.id} continues straight to ($nextX, $nextY)")
            } else {
                // Gặp chướng ngại vật - chuyển hướng ngẫu nhiên
                val newDirection = getRandomValidDirection(currentX, currentY, map, monster)
                if (newDirection != null) {
                    bounceState.currentDirection = newDirection
                    bounceState.lastDirectionChange = System.currentTimeMillis()
                    
                    monster.targetX = currentX + newDirection.first
                    monster.targetY = currentY + newDirection.second
                    
                    println("🎾 ${monster.id} bounced! New direction: $newDirection, target: (${monster.targetX}, ${monster.targetY})")
                } else {
                    // Không có hướng nào hợp lệ - dừng lại
                    monster.targetX = currentX
                    monster.targetY = currentY
                    println("🎾 ${monster.id} stuck! No valid directions")
                }
            }
        }
    }
    
    /**
     * 🎲 Lấy hướng ngẫu nhiên hợp lệ (không bao gồm hướng ngược lại)
     */
    private fun getRandomValidDirection(x: Int, y: Int, map: Array<CharArray>, monster: Monster): Pair<Int, Int>? {
        val bounceState = monster.aiState as? MonsterAIState.BounceState ?: return null
        val currentDir = bounceState.currentDirection
        
        // Tất cả hướng có thể
        val allDirections = listOf(
            Pair(-1, 0), // UP
            Pair(1, 0),  // DOWN
            Pair(0, -1), // LEFT
            Pair(0, 1)   // RIGHT
        )
        
        // Loại bỏ hướng ngược lại để tránh oscillation
        val oppositeDir = Pair(-currentDir.first, -currentDir.second)
        val validDirections = allDirections.filter { direction ->
            direction != oppositeDir && // Không quay ngược lại
            isValidPosition(x + direction.first, y + direction.second, map, monster)
        }
        
        // Nếu không có hướng nào khác, cho phép quay ngược lại
        return if (validDirections.isNotEmpty()) {
            validDirections.random()
        } else {
            allDirections.find { direction ->
                isValidPosition(x + direction.first, y + direction.second, map, monster)
            }
        }
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
                // Sử dụng initialDirection từ MonsterData (đơn giản hóa)
                MonsterAIState.PatrolState(
                    startPosition = Pair(monsterData.startRow, monsterData.startColumn),
                    currentDirection = monsterData.initialDirection
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
                // Sử dụng initialDirection từ MonsterData
                MonsterAIState.StraightState(
                    startPosition = Pair(monsterData.startRow, monsterData.startColumn),
                    direction = monsterData.initialDirection,
                    isReturning = false
                )
            }
            MonsterType.BOUNCE -> {
                // Sử dụng initialDirection từ MonsterData
                MonsterAIState.BounceState(
                    currentDirection = monsterData.initialDirection,
                    lastDirectionChange = System.currentTimeMillis()
                )
            }
        }
        val initialTarget = when (monsterData.type) {
            MonsterType.STRAIGHT -> {
                // Với STRAIGHT, target ban đầu là vị trí hiện tại + direction
                val straightState = aiState as MonsterAIState.StraightState
                Pair(
                    monsterData.startRow + straightState.direction.first,
                    monsterData.startColumn + straightState.direction.second
                )
            }
            MonsterType.BOUNCE -> {
                // Với BOUNCE, target ban đầu là vị trí hiện tại + direction
                val bounceState = aiState as MonsterAIState.BounceState
                Pair(
                    monsterData.startRow + bounceState.currentDirection.first,
                    monsterData.startColumn + bounceState.currentDirection.second
                )
            }
            MonsterType.PATROL -> {
                // Với PATROL, target ban đầu là vị trí hiện tại + hướng di chuyển
                val patrolState = aiState as MonsterAIState.PatrolState
                Pair(
                    monsterData.startRow + patrolState.currentDirection.first,
                    monsterData.startColumn + patrolState.currentDirection.second
                )
            }
            else -> {
                if (monsterData.patrolPoints.isNotEmpty()) {
                    monsterData.patrolPoints[0]
                } else {
                    Pair(monsterData.startRow, monsterData.startColumn)
                }
            }
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

    // 🆕 THÊM METHOD XÓA MONSTER THEO INDEX
    fun removeMonster(index: Int) {
        if (index >= 0 && index < monsters.size) {
            val monsterToRemove = monsters[index]
            monsters.removeAt(index)
            println("💀 Monster ${monsterToRemove.id} (index $index) removed!")
        } else {
            println("❌ Invalid monster index $index, cannot remove!")
        }
    }

}