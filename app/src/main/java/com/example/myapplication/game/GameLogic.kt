package com.example.myapplication.game

import com.example.myapplication.managers.LevelManager
import com.example.myapplication.models.Level

class GameLogic {
    
    // Game state
    private var map: Array<CharArray> = arrayOf()
    private var playerX: Int = 0
    private var playerY: Int = 0
    private val goalPositions = mutableSetOf<Pair<Int, Int>>()
    private val safeZonePositions = mutableSetOf<Pair<Int, Int>>()  // Track safe zones
    private var currentLevel: Level? = null
    private var playerDirection = PlayerDirection.DOWN
    
    // Game status
    private var isGameWon = false
    
    // Callback interfaces
    interface GameStateListener {
        fun onGameStateChanged()
        fun onGameWon()
    }
    
    private var gameStateListener: GameStateListener? = null
    
    fun setGameStateListener(listener: GameStateListener) {
        gameStateListener = listener
    }
    
    // Level management
    fun loadLevel(levelId: Int) {
        currentLevel = LevelManager.getLevel(levelId)
        currentLevel?.let { level ->
            map = level.map.map { it.clone() }.toTypedArray()
            val (x, y) = level.getPlayerStartPosition()
            playerX = x
            playerY = y
            goalPositions.clear()
            goalPositions.addAll(level.getGoalPositions())

            // Scan và lưu vị trí safe zones
            safeZonePositions.clear()
            for (i in map.indices) {
                for (j in map[i].indices) {
                    if (map[i][j] == 'S') {
                        safeZonePositions.add(Pair(i, j))  // (row, col)
                    }
                }
            }

            isGameWon = false
            gameStateListener?.onGameStateChanged()
        }
    }
    
    // Player movement
    fun movePlayer(dx: Int, dy: Int): Boolean {
        playerDirection = PlayerDirection.fromMovement(dx, dy)
        val newX = playerX + dx
        val newY = playerY + dy

        // Kiểm tra di chuyển hợp lệ
        if (isValidMove(newX, newY)) {
            if (map[newX][newY] == 'B') {
                // Nếu ô đích có hộp, kiểm tra ô tiếp theo
                val boxNewX = newX + dx
                val boxNewY = newY + dy
                if (isValidMove(boxNewX, boxNewY)) {
                    // Di chuyển hộp
                    map[boxNewX][boxNewY] = 'B'
                    map[newX][newY] = '@'
                    // Khôi phục ký tự đúng khi player rời khỏi vị trí cũ
                    map[playerX][playerY] = when {
                        goalPositions.contains(Pair(playerX, playerY)) -> 'G'
                        safeZonePositions.contains(Pair(playerX, playerY)) -> 'S'
                        else -> '.'
                    }
                    playerX = newX
                    playerY = newY
                    
                    // Kiểm tra win condition
                    checkWinCondition()
                    gameStateListener?.onGameStateChanged()
                    return true
                }
            } else {
                // Di chuyển người chơi
                map[newX][newY] = '@'
                // Khôi phục ký tự đúng khi player rời khỏi vị trí cũ
                map[playerX][playerY] = when {
                    goalPositions.contains(Pair(playerX, playerY)) -> 'G'
                    safeZonePositions.contains(Pair(playerX, playerY)) -> 'S'
                    else -> '.'
                }
                playerX = newX
                playerY = newY

                // Kiểm tra win condition
                checkWinCondition()
                gameStateListener?.onGameStateChanged()
                return true
            }
        }
        return false
    }
    
    private fun isValidMove(x: Int, y: Int): Boolean {
        return x >= 0 && x < map.size && y >= 0 && y < map[0].size &&
               map[x][y] != '#'  // Player có thể đi vào ô trống '.', 'G' (goal), 'S' (safe zone)
    }
    
    private fun checkWinCondition() {
        if (isGameWon) return // Đã thắng rồi, không cần kiểm tra nữa
        
        var allBoxesOnGoals = true
        for ((goalX, goalY) in goalPositions) {
            if (map[goalX][goalY] != 'B') {
                allBoxesOnGoals = false
                break
            }
        }

        if (allBoxesOnGoals) {
            isGameWon = true
            gameStateListener?.onGameWon()
        }
    }
    
    // Getters
    fun getMap(): Array<CharArray> = map
    fun getPlayerPosition(): Pair<Int, Int> = Pair(playerX, playerY)
    fun getGoalPositions(): Set<Pair<Int, Int>> = goalPositions.toSet()
    fun getCurrentLevel(): Level? = currentLevel
    fun isGameWon(): Boolean = isGameWon
    
    // Game state management
    fun resetLevel() {
        currentLevel?.let { level ->
            map = level.map.map { it.clone() }.toTypedArray()
            val (x, y) = level.getPlayerStartPosition()
            playerX = x
            playerY = y
            isGameWon = false
            gameStateListener?.onGameStateChanged()
        }
    }
    
    // Utility methods
    fun isMapEmpty(): Boolean = map.isEmpty() || map[0].isEmpty()
    
    fun getMapDimensions(): Pair<Int, Int> {
        if (isMapEmpty()) return Pair(0, 0)
        return Pair(map.size, map[0].size)
    }
    
    fun getTileAt(x: Int, y: Int): Char? {
        return if (x >= 0 && x < map.size && y >= 0 && y < map[0].size) {
            map[x][y]
        } else {
            null
        }
    }
    
    fun getBoxCount(): Int {
        var count = 0
        for (row in map) {
            for (tile in row) {
                if (tile == 'B') count++
            }
        }
        return count
    }
    
    fun getBoxesOnGoalsCount(): Int {
        var count = 0
        for ((goalX, goalY) in goalPositions) {
            if (map[goalX][goalY] == 'B') count++
        }
        return count
    }
    
    fun getProgressPercentage(): Float {
        val totalGoals = goalPositions.size
        if (totalGoals == 0) return 100f
        
        val boxesOnGoals = getBoxesOnGoalsCount()
        return (boxesOnGoals.toFloat() / totalGoals.toFloat()) * 100f
    }

    fun getPlayerDirection(): PlayerDirection = playerDirection
}
