package com.example.myapplication.managers

import com.example.myapplication.models.Level
import com.example.myapplication.models.Level.Difficulty.*
import com.example.myapplication.entities.MonsterType
import com.example.myapplication.models.MonsterData

object LevelManager{
    private val levels = mutableListOf<Level>()

    init {
        initializeLevels()
    }

    private fun initializeLevels(){
        // Level 1 - Tutorial với Monster
        levels.add(
            Level(
                id = 1,
                name = "Hướng dẫn",
                difficulty = EASY,
                description = "Học cách chơi cơ bản và tránh quái",
                map = arrayOf(
                    charArrayOf('#', '#', '#', '#', '#', '#', '#'),
                    charArrayOf('#', '@', '.', '.', '.', '.', '#'),
                    charArrayOf('#', '.', '.', '.', '.', '.', '#'),
                    charArrayOf('#', '.', 'B', '.', 'G', '.', '#'),
                    charArrayOf('#', '.', '.', '.', '.', '.', '#'),
                    charArrayOf('#', '#', '#', '#', '#', '#', '#')
                ),
                monsters = listOf(
                    // Đi sang phải
                    MonsterData(
                        type = MonsterType.STRAIGHT,
                        startRow = 1,
                        startColumn = 5,
                        patrolPoints = listOf(Pair(0, -1)),
                        speed = 1.0f
                    ),
                    MonsterData(
                        type = MonsterType.STRAIGHT,
                        startRow = 1,
                        startColumn = 5,
                        patrolPoints = listOf(Pair(1, 0)),
                        speed = 1.0f
                    ),



                )
            )
        )

        // Level 2 - Easy
        levels.add(
            Level(
                id = 2,
                name = "Khởi đầu",
                difficulty = EASY,
                description = "Thử thách đầu tiên",
                map = arrayOf(
                    charArrayOf('#', '#', '#', '#', '#', '#'),
                    charArrayOf('#', '.', '.', '.', '.', '#'),
                    charArrayOf('#', '@', 'B', 'G', '.', '#'),
                    charArrayOf('#', '.', '.', '.', '.', '#'),
                    charArrayOf('#', '#', '#', '#', '#', '#')
                )
            )
        )

        // Level 3 - Medium
        levels.add(
            Level(
                id = 3,
                name = "Thử thách",
                difficulty = MEDIUM,
                description = "Nhiều hộp hơn",
                map = arrayOf(
                    charArrayOf('#', '#', '#', '#', '#', '#', '#'),
                    charArrayOf('#', '.', '.', '.', '.', '.', '#'),
                    charArrayOf('#', '@', 'B', '.', 'B', '.', '#'),
                    charArrayOf('#', '.', 'G', '.', 'G', '.', '#'),
                    charArrayOf('#', '.', '.', '.', '.', '.', '#'),
                    charArrayOf('#', '#', '#', '#', '#', '#', '#')
                )
            )
        )

        // Level 4 - Original từ game hiện tại
        levels.add(
            Level(
                id = 4,
                name = "Cổ điển",
                difficulty = MEDIUM,
                description = "Level từ phiên bản gốc",
                map = arrayOf(
                    charArrayOf('#', '#', '#', '#', '#', '#', '#', '#', '#', '#'),
                    charArrayOf('#', '.', '.', '.', '.', '.', '.', '.', '.', '#'),
                    charArrayOf('#', '.', 'B', '.', '.', 'G', '.', '.', '.', '#'),
                    charArrayOf('#', '.', '.', '#', '.', '.', '#', '.', '.', '#'),
                    charArrayOf('#', '.', '#', '.', '#', '.', '.', 'B', '.', '#'),
                    charArrayOf('#', '.', '.', '.', '.', '.', '.', '.', '.', '#'),
                    charArrayOf('#', '@', '.', 'G', '.', 'B', '.', '.', '.', '#'),
                    charArrayOf('#', '.', '.', '.', '#', '.', '.', '.', '.', '#'),
                    charArrayOf('#', '.', '#', '.', '.', '.', '#', '.', '.', '#'),
                    charArrayOf('#', '.', '.', 'B', '.', '.', '.', '.', '.', '#'),
                    charArrayOf('#', '.', '.', '.', '.', '.', '.', '.', '.', '#'),
                    charArrayOf('#', '#', '#', '#', '#', '#', '#', '#', '#', '#')
                )
            )
        )

        // Level 5 - Hard
        levels.add(
            Level(
                id = 5,
                name = "Khó khăn",
                difficulty = HARD,
                description = "Dành cho người chơi giỏi",
                isUnlocked = false, // Khóa ban đầu
                map = arrayOf(
                    charArrayOf('#', '#', '#', '#', '#', '#', '#', '#'),
                    charArrayOf('#', '.', '.', '#', '.', '.', '.', '#'),
                    charArrayOf('#', '.', 'B', '#', 'B', '.', '.', '#'),
                    charArrayOf('#', '@', '.', '.', '.', '.', '.', '#'),
                    charArrayOf('#', 'G', '#', '.', '#', 'G', '.', '#'),
                    charArrayOf('#', '.', '.', 'B', '.', '.', '.', '#'),
                    charArrayOf('#', '.', '.', '#', '.', '.', 'G', '#'),
                    charArrayOf('#', '#', '#', '#', '#', '#', '#', '#')
                )
            )
        )
    }

    // Public methods
    fun getAllLevels(): List<Level> = levels.toList()

    fun getLevel(id: Int): Level? = levels.find { it.id == id }

    fun getLevelsByDifficulty(difficulty: Level.Difficulty): List<Level> {
        return levels.filter { it.difficulty == difficulty }
    }

    fun getUnlockedLevels(): List<Level> = levels.filter { it.isUnlocked }

    fun unlockLevel(id: Int) {
        levels.find { it.id == id }?.let { level ->
            val index = levels.indexOf(level)
            levels[index] = level.copy(isUnlocked = true)
        }
    }

    fun updateLevelRecord(id: Int, moves: Int, time: Long) {
        levels.find { it.id == id }?.let { level ->
            val index = levels.indexOf(level)
            val currentBestMoves = level.bestMoves
            val currentBestTime = level.bestTime

            val newBestMoves = if (currentBestMoves == null || moves < currentBestMoves) moves else currentBestMoves
            val newBestTime = if (currentBestTime == null || time < currentBestTime) time else currentBestTime

            levels[index] = level.copy(bestMoves = newBestMoves, bestTime = newBestTime)

            // Unlock next level nếu hoàn thành
            if (id < levels.size) {
                unlockLevel(id + 1)
            }
        }
    }

    fun getProgress(): Triple<Int, Int, Float> {
        val total = levels.size
        val completed = levels.count { it.bestMoves != null }
        val percentage = if (total > 0) (completed.toFloat() / total) * 100 else 0f
        return Triple(completed, total, percentage)
    }
}