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
                        initialDirection = Pair(0, -1),  // Đi lên
                        speed = 1.0f
                    ),
                    MonsterData(
                        type = MonsterType.STRAIGHT,
                        startRow = 1,
                        startColumn = 5,
                        initialDirection = Pair(1, 0),   // Đi xuống
                        speed = 1.0f
                    ),



                )
            )
        )

        // Level 2 - Easy với BOUNCE Monster
        levels.add(
            Level(
                id = 2,
                name = "Khởi đầu với Bounce",
                difficulty = EASY,
                description = "Thử thách đầu tiên với monster nảy tường",
                map = arrayOf(
                    charArrayOf('#', '#', '#', '#', '#', '#', '#', '#'),
                    charArrayOf('#', '.', '.', '.', '.', '.', '.', '#'),
                    charArrayOf('#', '@', '.', '.', '.', '.', '.', '#'),
                    charArrayOf('#', '.', '#', '.', '.', '#', '.', '#'),
                    charArrayOf('#', '.', '.', 'B', 'G', '.', '.', '#'),
                    charArrayOf('#', '.', '.', '.', '.', '.', '.', '#'),
                    charArrayOf('#', '#', '#', '#', '#', '#', '#', '#')
                ),
                monsters = listOf(
                    // BOUNCE Monster - bắt đầu đi sang phải
                    MonsterData(
                        type = MonsterType.BOUNCE,
                        startRow = 1,
                        startColumn = 1,
                        patrolPoints = listOf(Pair(0, 1)),  // Hướng ban đầu: đi sang phải
                        speed = 1.5f
                    ),
                    // BOUNCE Monster khác - bắt đầu đi xuống  
                    MonsterData(
                        type = MonsterType.BOUNCE,
                        startRow = 1,
                        startColumn = 6,
                        patrolPoints = listOf(Pair(1, 0)),  // Hướng ban đầu: đi xuống
                        speed = 1.2f
                    )
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

        // Level 6 - BOUNCE Monster Playground 
        levels.add(
            Level(
                id = 6,
                name = "Thế giới Bounce",
                difficulty = MEDIUM,
                description = "Khám phá sức mạnh của BOUNCE monsters",
                map = arrayOf(
                    charArrayOf('#', '#', '#', '#', '#', '#', '#', '#', '#', '#'),
                    charArrayOf('#', '.', '.', '.', '#', '.', '.', '.', '.', '#'),
                    charArrayOf('#', '.', '#', '.', '.', '.', '#', '.', '.', '#'),
                    charArrayOf('#', '@', '.', '.', '.', '.', '.', '.', '.', '#'),
                    charArrayOf('#', '.', '.', '#', '.', '.', '#', '.', '.', '#'),
                    charArrayOf('#', '.', '.', '.', '.', '.', '.', '.', 'B', '#'),
                    charArrayOf('#', '.', '#', '.', '.', '.', '#', '.', 'G', '#'),
                    charArrayOf('#', '.', '.', '.', '#', '.', '.', '.', '.', '#'),
                    charArrayOf('#', '#', '#', '#', '#', '#', '#', '#', '#', '#')
                ),
                monsters = listOf(
                    // BOUNCE Monster với random direction
                    MonsterData(
                        type = MonsterType.BOUNCE,
                        startRow = 1,
                        startColumn = 1,
                        patrolPoints = emptyList(),  // Random hướng ban đầu
                        speed = 1.8f
                    ),
                    // BOUNCE Monster đi chéo lên-phải  
                    MonsterData(
                        type = MonsterType.BOUNCE,
                        startRow = 7,
                        startColumn = 1,
                        patrolPoints = listOf(Pair(-1, 1)),  // Đi chéo lên-phải
                        speed = 1.3f
                    ),
                    // BOUNCE Monster nhanh
                    MonsterData(
                        type = MonsterType.BOUNCE,
                        startRow = 3,
                        startColumn = 8,
                        patrolPoints = listOf(Pair(0, -1)),  // Đi sang trái
                        speed = 2.2f
                    )
                )
            )
        )
        
        // Level 7 - Hard Sokoban Challenge với PATROL và BOUNCE monsters
        levels.add(
            Level(
                id = 7,
                name = "Thách thức tối thượng",
                difficulty = HARD,
                description = "Map khó với monster patrol và bounce",
                isUnlocked = true, // Mở khóa để test
                map = arrayOf(
                    charArrayOf('#', '#', '#', '#', '#', '#', '#', '#', '#', '#', '#', '#'),
                    charArrayOf('#', '.', '.', '.', '#', '.', '.', '.', '.', '.', '.', '#'),
                    charArrayOf('#', '.', '#', '.', '.', '.', '#', '.', '#', '.', '.', '#'),
                    charArrayOf('#', '@', '.', '.', '.', '.', '.', '.', '.', '.', '.', '#'),
                    charArrayOf('#', '.', '#', '.', 'B', '.', '#', '.', 'B', '.', '#', '.'),
                    charArrayOf('#', '.', '.', '.', '.', '.', '.', '.', '.', '.', '.', '#'),
                    charArrayOf('#', '#', '.', '#', '.', '.', '#', '.', '.', '#', '.', '#'),
                    charArrayOf('#', '.', '.', '.', '.', 'B', '.', 'B', '.', '.', '.', '#'),
                    charArrayOf('#', '.', '#', '.', '.', '.', '.', '.', '.', '#', '.', '#'),
                    charArrayOf('#', 'G', '.', 'G', '.', '.', '#', '.', 'G', '.', 'G', '#'),
                    charArrayOf('#', '.', '.', '.', '.', '.', '.', '.', '.', '.', '.', '#'),
                    charArrayOf('#', '#', '#', '#', '#', '#', '#', '#', '#', '#', '#', '#')
                ),
                monsters = listOf(
                    // PATROL Monster 1 - Di chuyển ngang phía trên
                    MonsterData(
                        type = MonsterType.PATROL,
                        startRow = 1,
                        startColumn = 1,
                        initialDirection = Pair(0, 1),  // Di chuyển sang phải
                        speed = 2.2f
                    ),
                    // PATROL Monster 2 - Di chuyển dọc bên phải
                    MonsterData(
                        type = MonsterType.PATROL,
                        startRow = 5,
                        startColumn = 10,
                        initialDirection = Pair(1, 0),  // Di chuyển xuống
                        speed = 3.4f
                    ),
                    // BOUNCE Monster 1 - Bắt đầu từ giữa trái
                    MonsterData(
                        type = MonsterType.BOUNCE,
                        startRow = 3,
                        startColumn = 2,
                        initialDirection = Pair(0, 1),  // Hướng ban đầu: xuống
                        speed = 2.6f
                    ),
                    // BOUNCE Monster 2 - Khu vực phía dưới
                    MonsterData(
                        type = MonsterType.BOUNCE,
                        startRow = 7,
                        startColumn = 8,
                        initialDirection = Pair(-1, 0),  // Hướng ban đầu: lên
                        speed = 2.3f
                    ),

                    MonsterData(
                        type = MonsterType.BOUNCE,
                        startRow = 6,
                        startColumn = 8,
                        initialDirection = Pair(-1, 0),  // Hướng ban đầu: lên
                        speed = 4.0f
                    )
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