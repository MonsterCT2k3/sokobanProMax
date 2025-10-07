package com.example.myapplication.game

import com.example.myapplication.managers.LevelManager
import com.example.myapplication.models.Level
import kotlin.math.min

class GameLogic {
    
    // Game state
    private var map: Array<CharArray> = arrayOf()
    private var playerX: Int = 0
    private var playerY: Int = 0
    private val goalPositions = mutableSetOf<Pair<Int, Int>>()
    private val safeZonePositions = mutableSetOf<Pair<Int, Int>>()
    private var currentLevel: Level? = null
    private var playerDirection = PlayerDirection.DOWN
    
    // Game status
    private var isGameWon = false
    private var boxesInGoal = 0  // Số hộp đang ở trong goal

    // Timer for level completion tracking
    private var levelStartTime = 0L
    private var levelElapsedTime = 0L  // milliseconds
    
    // Callback interfaces
    interface GameStateListener {
        fun onGameStateChanged()
        fun onGameWon()
    }

    // Callback for goal reached effect - pass row/col instead of screen coordinates
    var onGoalReachedEffect: ((row: Int, col: Int) -> Unit)? = null

    // Callback for goal left effect (when box is removed from goal) - pass row/col
    var onGoalLeftEffect: ((row: Int, col: Int) -> Unit)? = null

    // Callback for goal count update
    var onGoalCountChanged: ((count: Int, total: Int) -> Unit)? = null

    // Callback for timer update
    var onTimerUpdate: ((elapsedTime: Long) -> Unit)? = null

    // Screen dimensions for effect positioning
    private var screenWidth = 1080f
    private var screenHeight = 1920f

    fun setScreenSize(width: Int, height: Int) {
        screenWidth = width.toFloat()
        screenHeight = height.toFloat()
    }

    private var gameStateListener: GameStateListener? = null

    fun setGameStateListener(listener: GameStateListener) {
        gameStateListener = listener
    }

    // Get current goal count
    fun getBoxesInGoal(): Int = boxesInGoal
    fun getTotalGoals(): Int = goalPositions.size

    // Timer methods
    fun startLevelTimer() {
        levelStartTime = System.currentTimeMillis()
        levelElapsedTime = 0L
    }

    fun updateLevelTimer() {
        if (levelStartTime > 0) {
            levelElapsedTime = System.currentTimeMillis() - levelStartTime
            onTimerUpdate?.invoke(levelElapsedTime)
        }
    }

    fun getLevelElapsedTime(): Long = levelElapsedTime

    fun stopLevelTimer() {
        if (levelStartTime > 0) {
            levelElapsedTime = System.currentTimeMillis() - levelStartTime
        }
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

            // Scan safe zone positions ('S' characters) from map
            safeZonePositions.clear()
            for (i in map.indices) {
                for (j in map[i].indices) {
                    if (map[i][j] == 'S') {
                        safeZonePositions.add(Pair(i, j))
                    }
                }
            }

            isGameWon = false
            boxesInGoal = 0  // Reset counter khi load level mới
            levelStartTime = 0L  // Reset timer
            levelElapsedTime = 0L
            gameStateListener?.onGameStateChanged()
        }
    }

    /**
     * 🎨 Load custom level từ Customize mode
     */
    fun loadCustomLevelData(mapString: String, width: Int, height: Int, expectedBoxCount: Int) {
        println("🎨 GameLogic: Loading custom level data: ${width}x${height}, $expectedBoxCount boxes")

        // Parse map từ string - split by newlines and convert to char arrays
        val mapLines = mapString.split("\n")
        map = Array(height) { row ->
            if (row < mapLines.size) {
                val line = mapLines[row]
                CharArray(width) { col ->
                    if (col < line.length) line[col] else ' '
                }
            } else {
                CharArray(width) { ' ' }
            }
        }

        // Tìm vị trí player
        var playerPos: Pair<Int, Int>? = null
        for (i in map.indices) {
            for (j in map[i].indices) {
                if (map[i][j] == '@') {
                    playerPos = Pair(i, j)
                    break
                }
            }
            if (playerPos != null) break
        }

        if (playerPos != null) {
            playerX = playerPos.first
            playerY = playerPos.second
        } else {
            // Fallback nếu không tìm thấy player
            playerX = 1
            playerY = 1
        }

        // Parse goal positions từ map
        goalPositions.clear()
        safeZonePositions.clear()
        for (i in map.indices) {
            for (j in map[i].indices) {
                when (map[i][j]) {
                    '.', 'G' -> goalPositions.add(Pair(i, j))
                    'S' -> safeZonePositions.add(Pair(i, j))
                }
            }
        }

        // Reset game state
        isGameWon = false
        boxesInGoal = 0
        levelStartTime = 0L
        levelElapsedTime = 0L

        // Thông báo game state changed
        gameStateListener?.onGameStateChanged()

        println("🎨 GameLogic: Custom level loaded successfully")
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
                    // 🔒 NGĂN CHẶN ĐẨY 2 HỘP LIỀN NHAU
                    // Nếu ô đích của hộp cũng là hộp thì không cho phép đẩy
                    if (map[boxNewX][boxNewY] == 'B') {
                        return false  // Không thể đẩy vì có 2 hộp liền nhau
                    }

                    // 🎯 CHECK IF BOX IS LEAVING GOAL - tính vị trí center của hộp cũ
                    if (Pair(newX, newY) in goalPositions) {
                        val tileSize = min(screenWidth / map[0].size.toFloat(), screenHeight / map.size.toFloat())
                        val boardWidth = map[0].size.toFloat() * tileSize
                        val boardHeight = map.size.toFloat() * tileSize
                        val offsetX = (screenWidth - boardWidth) / 2f
                        val offsetY = (screenHeight - boardHeight) / 2f

                        onGoalLeftEffect?.invoke(newX, newY)
                        boxesInGoal--  // Giảm counter khi box ra khỏi goal
                        onGoalCountChanged?.invoke(boxesInGoal, goalPositions.size)
                    }

                    // Di chuyển hộp - xóa hộp cũ và đặt hộp mới
                    // Khôi phục ký tự gốc của vị trí hộp cũ
                    val originalChar = when {
                        Pair(newX, newY) in goalPositions -> 'G'
                        Pair(newX, newY) in safeZonePositions -> 'S'
                        else -> '.'
                    }
                    map[newX][newY] = originalChar

                    // Đặt hộp mới - luôn là 'B' bất kể có safe zone hay không
                    map[boxNewX][boxNewY] = 'B'

                    // 🎯 TRIGGER GOAL REACHED EFFECT nếu hộp được đẩy vào goal
                    if (Pair(boxNewX, boxNewY) in goalPositions) {
                        onGoalReachedEffect?.invoke(boxNewX, boxNewY)
                        boxesInGoal++  // Tăng counter khi box vào goal
                        onGoalCountChanged?.invoke(boxesInGoal, goalPositions.size)
                    }

                    // Di chuyển player
                    playerX = newX
                    playerY = newY

                    // Kiểm tra win condition
                    checkWinCondition()
                    gameStateListener?.onGameStateChanged()
                    return true
                }
            } else {
                // Di chuyển người chơi - giữ nguyên ký tự gốc trên map
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

    /**
     * 🛡️ Check if player is currently on a safe zone
     */
    fun getSafeZonePositions(): Set<Pair<Int, Int>> = safeZonePositions.toSet()

    fun isPlayerOnSafeZone(): Boolean {
        val result = Pair(playerX, playerY) in safeZonePositions
        println("🛡️ isPlayerOnSafeZone check: player=($playerX,$playerY), safeZones=$safeZonePositions, result=$result")
        println("🛡️ Map at player position: ${map.getOrNull(playerX)?.getOrNull(playerY)}")
        return result
    }
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
