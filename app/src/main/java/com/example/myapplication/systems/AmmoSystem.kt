package com.example.myapplication.systems

import com.example.myapplication.entities.AmmoPickup

class AmmoSystem {
    private val ammoPickups = mutableListOf<AmmoPickup>()
    private var nextAmmoId = 0

    // Tạo ammo pickup ngẫu nhiên trên map
    fun spawnRandomAmmo(map: Array<CharArray>, count: Int = 3, excludePositions: List<Pair<Int, Int>> = emptyList()) {
        ammoPickups.clear()

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

        println("🎯 Found ${validPositions.size} valid positions for ammo")
        println("🎯 Map size: ${map.size} rows x ${map[0].size} cols")

        // Chọn ngẫu nhiên các vị trí
        validPositions.shuffle()
        val selectedPositions = validPositions.take(count.coerceAtMost(validPositions.size))

        println("🎯 Selected ${selectedPositions.size} positions for ammo spawn")

        // Tạo ammo pickups
        for ((gridX, gridY) in selectedPositions) {
            val ammo = AmmoPickup(
                id = "ammo_${nextAmmoId++}",
                gridX = gridX,
                gridY = gridY
            )
            ammoPickups.add(ammo)
            println("📦 Spawned ammo at (${gridX}, ${gridY})")
        }

        println("✅ Total ammo spawned: ${ammoPickups.size}")
    }

    // Kiểm tra player có thu thập ammo không
    fun checkAmmoCollection(playerX: Int, playerY: Int): Boolean {
        println("🔍 Checking ammo collection at player position (${playerX}, ${playerY})")
        println("📦 Active ammo count: ${getActiveAmmoPickups().size}")

        val collectedAmmo = ammoPickups.find { ammo ->
            !ammo.isCollected && ammo.gridX == playerX && ammo.gridY == playerY
        }

        if (collectedAmmo != null) {
            collectedAmmo.isCollected = true
            ammoPickups.remove(collectedAmmo)
            println("🎁 Player collected ammo at (${playerX}, ${playerY})!")
            println("📦 Remaining ammo: ${getActiveAmmoPickups().size}")
            return true
        }

        return false
    }

    fun getActiveAmmoPickups(): List<AmmoPickup> {
        return ammoPickups.filter { !it.isCollected }
    }

    fun clearAmmo() {
        ammoPickups.clear()
    }
}