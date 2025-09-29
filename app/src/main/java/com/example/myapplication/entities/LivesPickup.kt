package com.example.myapplication.entities

data class LivesPickup(
    val id: String,
    val gridX: Int,
    val gridY: Int,
    var isCollected: Boolean = false
) {
    fun getScreenPosition(tileSize: Float, offsetX: Float, offsetY: Float): Pair<Float, Float> {
        val screenX = offsetX + gridX * tileSize + tileSize / 2
        val screenY = offsetY + gridY * tileSize + tileSize / 2
        return Pair(screenX, screenY)
    }
}
