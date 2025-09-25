package com.example.myapplication.entities

enum class BulletDirection {
    UP, DOWN, LEFT, RIGHT
}

data class Bullet(
    val id:String,
    var currentX: Float,
    var currentY: Float,
    val targetX: Float,
    val targetY: Float,
    val direction: BulletDirection,  // Hướng của bullet
    val speed: Float = 800.0f,  // Tăng speed mặc định
    var isActive: Boolean = true
) {
    fun getDirection():Pair<Float, Float> {
        val dx = targetX - currentX
        val dy = targetY - currentY
        val distance = kotlin.math.sqrt((dx * dx + dy * dy).toDouble()).toFloat()
        if(distance == 0f) return Pair(0f,0f)
        return Pair(dx / distance, dy / distance)
    }

    fun collidesWith(monsterX: Float, monsterY: Float, threshold: Float = 40f): Boolean {
        val dx = currentX - monsterX
        val dy = currentY - monsterY-77
        val distance = kotlin.math.sqrt(dx * dx + dy * dy)
        // Trong Bullet.collidesWith
        println("🔍 Bullet at (${currentX.toInt()}, ${currentY.toInt()}) checking monster at (${monsterX.toInt()}, ${monsterY.toInt()}), distance: $distance")

        return distance < threshold
    }

    fun isOutOfBounds(screenWidth: Float, screenHeight: Float): Boolean {
        val margin = 200f // Margin lớn hơn để bullet bay xa hơn

        val outOfBounds = currentX < -margin || currentX > screenWidth + margin ||
                         currentY < -margin || currentY > screenHeight + margin

        return outOfBounds
    }
}