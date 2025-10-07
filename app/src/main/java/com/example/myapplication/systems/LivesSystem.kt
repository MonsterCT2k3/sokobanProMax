package com.example.myapplication.systems

import com.example.myapplication.entities.LivesPickup
import kotlin.random.Random

class LivesSystem {
    private val livesPickups = mutableListOf<LivesPickup>()
    private var nextLivesId = 0

    fun clearLivesPickups() {
        livesPickups.clear()
    }

    fun addLivesPickup(livesPickup: LivesPickup) {
        livesPickups.add(livesPickup)
    }

    fun spawnRandomLives(map: Array<CharArray>, count: Int = 1, excludePositions: List<Pair<Int, Int>> = emptyList()) {
        livesPickups.clear()
        val validPositions = mutableListOf<Pair<Int, Int>>()

        // Tìm tất cả vị trí hợp lệ (không phải tường, không phải hộp, không phải vị trí loại trừ)
        for (y in map.indices) {
            for (x in map[y].indices) {
                val position = Pair(x, y)
                if ((map[y][x] == '.' || map[y][x] == 'G') && position !in excludePositions) {
                    validPositions.add(position)
                }
            }
        }

        // Chọn ngẫu nhiên các vị trí
        validPositions.shuffle()
        val selectedPositions = validPositions.take(count.coerceAtMost(validPositions.size))

        // Tạo lives pickups
        for ((gridX, gridY) in selectedPositions) {
            val lives = LivesPickup(id = "lives_${nextLivesId++}", gridX = gridX, gridY = gridY)
            livesPickups.add(lives)
        }
    }

    // Kiểm tra player có thu thập lives không
    fun checkLivesCollection(playerX: Int, playerY: Int): Boolean {
        val collectedLives = livesPickups.find { lives ->
            val match = !lives.isCollected && lives.gridX == playerY && lives.gridY == playerX  // Fix coordinate order
            if (match) println("🎯 Found lives at (${lives.gridX}, ${lives.gridY}) for player at ($playerX, $playerY)")
            match
        }

        if (collectedLives != null) {
            collectedLives.isCollected = true
            livesPickups.remove(collectedLives)
            println("✅ Collected lives! Lives remaining: ${livesPickups.size}")
            return true
        }

        return false
    }

    fun getActiveLivesPickups(): List<LivesPickup> {
        return livesPickups.filter { !it.isCollected }
    }

    fun clearLives() {
        livesPickups.clear()
    }
}
