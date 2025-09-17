package com.example.myapplication.systems

import com.example.myapplication.entities.Bullet

class BulletSystem {
    private val bullets = mutableListOf<Bullet>()
    private var nextBulletId = 0

    //public method to add a bullet
    fun addBullet(startX: Float, startY: Float, targetX: Float, targetY: Float) {
        // Đảm bảo có khoảng cách tối thiểu để bullet có hướng rõ ràng
        val dx = targetX - startX
        val dy = targetY - startY
        val distance = kotlin.math.sqrt(dx * dx + dy * dy)

        // Nếu quá gần, tăng khoảng cách
        val finalTargetX = if (distance < 50f) {
            startX + (dx / distance) * 200f  // Tăng lên 200 pixels
        } else {
            targetX
        }

        val finalTargetY = if (distance < 50f) {
            startY + (dy / distance) * 200f  // Tăng lên 200 pixels
        } else {
            targetY
        }

        val bullet = Bullet(
            id = "bullet_${nextBulletId++}",
            currentX = startX,
            currentY = startY,
            targetX = finalTargetX,
            targetY = finalTargetY,
            speed = 800.0f,  // Tăng tốc độ để bullet bay nhanh hơn
            isActive = true
        )

        bullets.add(bullet)
        println("🎯 Bullet created: ${bullet.id}")
    }

    fun updateBullets(deltaTime: Float, screenWidth: Float, screenHeight: Float, map: Array<CharArray>, tileSize: Float, offsetX: Float, offsetY: Float) {
        // Đảm bảo deltaTime hợp lý (tránh quá nhỏ)
        val safeDeltaTime = deltaTime.coerceIn(0.01f, 0.1f) // Giới hạn 10ms - 100ms

        val bulletsToRemove = mutableListOf<Bullet>()
        bullets.forEach { bullet ->
            if (bullet.isActive) {
                // Di chuyển bullet
                val direction = bullet.getDirection()
                val movementX = direction.first * bullet.speed * safeDeltaTime
                val movementY = direction.second * bullet.speed * safeDeltaTime

                bullet.currentX += movementX
                bullet.currentY += movementY

                // Debug cho bullet đầu tiên (chỉ mỗi 10 frame để tránh spam)
                if (bullet.id == "bullet_0" && bullets.size == 1) {
                    println("🚀 Bullet ${bullet.id}: Pos(${bullet.currentX.toInt()}, ${bullet.currentY.toInt()})")
                }

                // Kiểm tra chạm tường hoặc ra khỏi màn hình
                if (isBulletHitWall(bullet, map, tileSize, offsetX, offsetY) || bullet.isOutOfBounds(screenWidth, screenHeight)) {
                    bulletsToRemove.add(bullet)
                    println("💥 Bullet ${bullet.id} hit wall or out of bounds at (${bullet.currentX.toInt()}, ${bullet.currentY.toInt()})")
                }
            } else {
                // Bullet inactive -> remove ngay
                bulletsToRemove.add(bullet)
            }
        }
        bullets.removeAll(bulletsToRemove)
    }

    private fun isBulletHitWall(bullet: Bullet, map: Array<CharArray>, tileSize: Float, offsetX: Float, offsetY: Float): Boolean {
        // Convert screen position to grid position
        val gridX = ((bullet.currentX - offsetX) / tileSize).toInt()
        val gridY = ((bullet.currentY - offsetY) / tileSize).toInt()

        // Kiểm tra bounds của map
        if (gridX < 0 || gridX >= map[0].size || gridY < 0 || gridY >= map.size) {
            return true // Ra khỏi map bounds = hit wall
        }

        // Kiểm tra có phải tường không ('#' hoặc 'B' - box)
        val cell = map[gridY][gridX]
        return cell == '#' || cell == 'B'
    }

    fun checkCollisions(monsterPositions: List<Pair<Float, Float>>): List<Pair<Bullet, Int>> {
        val collisions = mutableListOf<Pair<Bullet, Int>>()
        bullets.forEach { bullet ->
            if (bullet.isActive) {  // Chỉ check collision cho active bullets
                monsterPositions.forEachIndexed { monsterIndex, (monsterX, monsterY) ->
                    if (bullet.collidesWith(monsterX, monsterY)) {
                        collisions.add(Pair(bullet, monsterIndex))
                        bullet.isActive = false
                        println("💥 Bullet ${bullet.id} hit monster ${monsterIndex} at (${monsterX.toInt()}, ${monsterY.toInt()}) - Bullet position: (${bullet.currentX.toInt()}, ${bullet.currentY.toInt()})")
                    }
                }
            }
        }
        return collisions
    }

    fun getActiveBullets(): List<Bullet> {
        return bullets.filter { it.isActive }
    }

    fun clearBullets() {
        bullets.clear()
    }

    fun  getBulletCount(): Int {
        return bullets.size
    }
    fun getActiveBulletCount(): Int = bullets.count { it.isActive }
}