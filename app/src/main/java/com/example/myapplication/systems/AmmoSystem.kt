package com.example.myapplication.systems

import com.example.myapplication.entities.AmmoPickup
import com.example.myapplication.entities.AmmoType
import kotlin.random.Random

class AmmoSystem {
    private val ammoPickups = mutableListOf<AmmoPickup>()
    private var nextAmmoId = 0

    fun clearAmmoPickups() {
        ammoPickups.clear()
    }

    fun addAmmoPickup(ammoPickup: AmmoPickup) {
        ammoPickups.add(ammoPickup)
    }

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

        val ammoTypes = listOf(AmmoType.NORMAL, AmmoType.NORMAL, AmmoType.PIERCE, AmmoType.STUN)  // 50% normal, 25% pierce, 25% stun

        // Chọn ngẫu nhiên các vị trí
        validPositions.shuffle()
        val selectedPositions = validPositions.take(count.coerceAtMost(validPositions.size))

        // Tạo ammo pickups
        for ((gridX, gridY) in selectedPositions) {
            val randomType = ammoTypes[Random.nextInt(ammoTypes.size)]
            val ammo = AmmoPickup(id = "ammo_${nextAmmoId++}", gridX = gridX, gridY = gridY, ammoType = randomType)
            ammoPickups.add(ammo)
        }
    }

    // Kiểm tra player có thu thập ammo không
    fun checkAmmoCollection(playerX: Int, playerY: Int): AmmoType? {
        val collectedAmmo = ammoPickups.find { ammo ->
            !ammo.isCollected && ammo.gridX == playerY && ammo.gridY == playerX
        }

        if (collectedAmmo != null) {
            collectedAmmo.isCollected = true
            ammoPickups.remove(collectedAmmo)
            return collectedAmmo.ammoType
        }

        return null
    }

    fun getAmmoCountByType(type: AmmoType): Int {
        return ammoPickups.count { !it.isCollected && it.ammoType == type }
    }

    fun getActiveAmmoPickups(): List<AmmoPickup> {
        return ammoPickups.filter { !it.isCollected }
    }

    fun clearAmmo() {
        ammoPickups.clear()
    }
}