package com.example.myapplication.managers

import com.example.myapplication.entities.MonsterType
import com.example.myapplication.models.Level
import com.example.myapplication.models.Level.Difficulty.*
import com.example.myapplication.models.MonsterData

/**
 * LevelManager - Quản lý tất cả levels trong game
 *
 * Chức năng chính:
 * - Khởi tạo và lưu trữ tất cả levels
 * - Quản lý việc unlock levels
 * - Cập nhật kỷ lục của từng level
 * - Theo dõi tiến độ hoàn thành game
 */
object LevelManager {
    private val levels = mutableListOf<Level>()

    init {
        initializeLevels()
    }

    /**
     * Khởi tạo tất cả levels của game
     */
    private fun initializeLevels() {
        // ===== LEVEL 1: HƯỚNG DẪN =====
        levels.add(
            Level(
                id = 1,
                name = "Hướng dẫn",
                difficulty = EASY,
                description = "Học cách chơi cơ bản và tránh quái",
                map = arrayOf(
                    charArrayOf('#', '#', '#', '#', '#', '#', '#'),
                    charArrayOf('#', '@', 'S', '.', '.', '.', '#'),
                    charArrayOf('#', '.', '.', '.', '.', '.', '#'),
                    charArrayOf('#', '.', 'B', '.', 'G', 'S', '#'),
                    charArrayOf('#', '.', '.', '.', '.', '.', '#'),
                    charArrayOf('#', '#', '#', '#', '#', '#', '#')
                ),
                monsters = listOf(
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
                    )
                )
            )
        )

        // ===== LEVEL 2: KHỞI ĐẦU VỚI BOUNCE =====
        levels.add(
            Level(
                id = 2,
                name = "Khởi đầu với Bounce",
                difficulty = EASY,
                description = "Thử thách đầu tiên với monster nảy tường",
                map = arrayOf(
                    charArrayOf('#', '#', '#', '#', '#', '#', '#', '#'),
                    charArrayOf('#', '.', 'S', '.', '.', '.', '.', '#'),
                    charArrayOf('#', '@', '.', '.', '.', '.', '.', '#'),
                    charArrayOf('#', '.', '#', '.', '.', '#', '.', '#'),
                    charArrayOf('#', '.', '.', 'B', 'G', '.', '.', '#'),
                    charArrayOf('#', '.', '.', '.', '.', '.', 'S', '#'),
                    charArrayOf('#', '#', '#', '#', '#', '#', '#', '#')
                ),
                monsters = listOf(
                    MonsterData(
                        type = MonsterType.BOUNCE,
                        startRow = 1,
                        startColumn = 1,
                        patrolPoints = listOf(Pair(0, 1)),  // Hướng ban đầu: đi sang phải
                        speed = 1.5f
                    ),
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

        // ===== LEVEL 3: THỬ THÁCH =====
        levels.add(
            Level(
                id = 3,
                name = "Thử thách",
                difficulty = MEDIUM,
                description = "Nhiều hộp hơn",
                map = arrayOf(
                    charArrayOf('#', '#', '#', '#', '#', '#', '#'),
                    charArrayOf('#', 'S', '.', '.', '.', '.', '#'),
                    charArrayOf('#', '@', 'B', '.', 'B', '.', '#'),
                    charArrayOf('#', '.', 'G', '.', 'G', '.', '#'),
                    charArrayOf('#', '.', '.', '.', '.', 'S', '#'),
                    charArrayOf('#', '#', '#', '#', '#', '#', '#')
                )
            )
        )

        // ===== LEVEL 4: CỔ ĐIỂN =====
        levels.add(
            Level(
                id = 4,
                name = "Cổ điển",
                difficulty = MEDIUM,
                description = "Level từ phiên bản gốc với monsters",
                map = arrayOf(
                    charArrayOf('#', '#', '#', '#', '#', '#', '#', '#', '#', '#'),
                    charArrayOf('#', '.', '.', '.', '.', '.', '.', '.', '.', '#'),
                    charArrayOf('#', '.', 'B', '.', '.', 'G', '.', '.', '.', '#'),
                    charArrayOf('#', '.', '.', '#', '.', '.', '#', '.', '.', '#'),
                    charArrayOf('#', '.', '#', '.', '#', '.', '.', 'B', '.', '#'),
                    charArrayOf('#', '.', '.', '.', '.', '.', '.', '.', '.', '#'),
                    charArrayOf('#', '@', '.', 'G', '.', 'B', '.', 'G', '.', '#'),
                    charArrayOf('#', '.', '.', '.', '#', '.', '.', '.', '.', '#'),
                    charArrayOf('#', '.', '#', '.', '.', '.', '#', 'G', '.', '#'),
                    charArrayOf('#', '.', '.', 'B', '.', '.', '.', '.', '.', '#'),
                    charArrayOf('#', '.', '.', '.', '.', '.', '.', '.', '.', '#'),
                    charArrayOf('#', '#', '#', '#', '#', '#', '#', '#', '#', '#')
                ),
                monsters = listOf(
                    MonsterData(
                        type = MonsterType.PATROL,
                        startRow = 1,
                        startColumn = 1,
                        initialDirection = Pair(0, 1),  // Di chuyển sang phải
                        speed = 1.5f
                    ),
                    MonsterData(
                        type = MonsterType.PATROL,
                        startRow = 3,
                        startColumn = 8,
                        initialDirection = Pair(1, 0),  // Di chuyển xuống
                        speed = 1.8f
                    ),
                    MonsterData(
                        type = MonsterType.BOUNCE,
                        startRow = 5,
                        startColumn = 4,
                        initialDirection = Pair(0, 1),  // Hướng ban đầu: sang phải
                        speed = 2.0f
                    )
                )
            )
        )
        // ===== LEVEL 5: THÁCH THỨC TỐI THƯỢNG =====
        levels.add(
            Level(
                id = 5,
                name = "Thách thức tối thượng",
                difficulty = HARD,
                description = "Map khó với monster patrol và bounce",
                isUnlocked = true, // Mở khóa để test
                map = arrayOf(
                    charArrayOf('#', '#', '#', '#', '#', '#', '#', '#', '#', '#', '#', '#'),
                    charArrayOf('#', '.', '.', '.', '#', '.', '.', '.', '.', '.', 'S', '#'),
                    charArrayOf('#', '.', '#', '.', '.', '.', '#', '.', '#', '.', '.', '#'),
                    charArrayOf('#', '@', '.', '.', '.', '.', '.', '.', 'S', '.', '.', '#'),
                    charArrayOf('#', '.', '#', '.', 'B', '.', '#', '.', 'B', '.', '#', '.'),
                    charArrayOf('#', '.', '.', '.', '.', '.', '.', '.', '.', '.', '.', '#'),
                    charArrayOf('#', '#', '.', '#', '.', '.', '#', '.', '.', '#', '.', '#'),
                    charArrayOf('#', '.', '.', '.', '.', 'B', '.', 'B', '.', '.', '.', '#'),
                    charArrayOf('#', '.', '#', 'S', '.', '.', '.', '.', '.', '#', '.', '#'),
                    charArrayOf('#', 'G', '.', 'G', '.', '.', '#', '.', 'G', '.', 'G', '#'),
                    charArrayOf('#', '.', '.', '.', '.', '.', '.', '.', '.', '.', '.', '#'),
                    charArrayOf('#', '#', '#', '#', '#', '#', '#', '#', '#', '#', '#', '#')
                ),
                monsters = listOf(
                    MonsterData(
                        type = MonsterType.PATROL,
                        startRow = 1,
                        startColumn = 1,
                        initialDirection = Pair(0, 1),  // Di chuyển sang phải
                        speed = 2.2f
                    ),
                    MonsterData(
                        type = MonsterType.PATROL,
                        startRow = 5,
                        startColumn = 10,
                        initialDirection = Pair(1, 0),  // Di chuyển xuống
                        speed = 3.4f
                    ),
                    MonsterData(
                        type = MonsterType.BOUNCE,
                        startRow = 3,
                        startColumn = 2,
                        initialDirection = Pair(0, 1),  // Hướng ban đầu: xuống
                        speed = 2.6f
                    ),
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


        // Level 6 - BOUNCE Monster Playground
        levels.add(
            Level(
                id = 6,
                name = "Hành lang tử thần",
                difficulty = HARD,
                description = "Map dọc với các hành lang hẹp, monster tuần tra và nảy bất ngờ tạo thử thách cao",
                isUnlocked = true,
                map = arrayOf(
                    charArrayOf(
                        '#',
                        '#',
                        '#',
                        '#',
                        '#',
                        '#',
                        '#',
                        '#',
                        '#',
                        '#',
                        '#',
                        '#',
                        '#',
                        '#',
                        '#'
                    ),
                    charArrayOf(
                        '#',
                        '.',
                        '.',
                        '.',
                        '.',
                        '.',
                        '.',
                        '.',
                        '.',
                        '.',
                        '.',
                        '.',
                        '.',
                        '.',
                        '#'
                    ),
                    charArrayOf(
                        '#',
                        '.',
                        '#',
                        '.',
                        '#',
                        '.',
                        '.',
                        '#',
                        '.',
                        '#',
                        '.',
                        '.',
                        '#',
                        '.',
                        '#'
                    ),
                    charArrayOf(
                        '#',
                        '@',
                        '.',
                        '.',
                        '.',
                        'B',
                        '.',
                        '.',
                        '.',
                        '.',
                        '.',
                        '.',
                        '.',
                        '.',
                        '#'
                    ),
                    charArrayOf(
                        '#',
                        '.',
                        '.',
                        '#',
                        '.',
                        '.',
                        '.',
                        '.',
                        '#',
                        '.',
                        '.',
                        '#',
                        '.',
                        '.',
                        '#'
                    ),
                    charArrayOf(
                        '#',
                        '.',
                        '.',
                        '.',
                        '.',
                        '.',
                        'S',
                        '.',
                        '.',
                        '.',
                        '.',
                        '.',
                        '.',
                        '.',
                        '#'
                    ),
                    charArrayOf(
                        '#',
                        '.',
                        '#',
                        '.',
                        '#',
                        '.',
                        '.',
                        '#',
                        '.',
                        '#',
                        '.',
                        '.',
                        '#',
                        '.',
                        '#'
                    ),
                    charArrayOf(
                        '#',
                        '.',
                        '.',
                        '.',
                        '.',
                        'B',
                        '.',
                        '.',
                        '.',
                        '.',
                        '.',
                        '.',
                        '.',
                        '.',
                        '#'
                    ),
                    charArrayOf(
                        '#',
                        '.',
                        '.',
                        '#',
                        '.',
                        '.',
                        '.',
                        '.',
                        '#',
                        '.',
                        '.',
                        '#',
                        '.',
                        '.',
                        '#'
                    ),
                    charArrayOf(
                        '#',
                        '.',
                        '.',
                        '.',
                        '.',
                        '.',
                        '.',
                        '.',
                        '.',
                        '.',
                        'B',
                        '.',
                        '.',
                        '.',
                        '#'
                    ),
                    charArrayOf(
                        '#',
                        '.',
                        '#',
                        '.',
                        '#',
                        '.',
                        '.',
                        '#',
                        '.',
                        '#',
                        '.',
                        '.',
                        '#',
                        '.',
                        '#'
                    ),
                    charArrayOf(
                        '#',
                        '.',
                        '.',
                        '.',
                        '.',
                        'B',
                        '.',
                        '.',
                        '.',
                        '.',
                        '.',
                        '.',
                        '.',
                        '.',
                        '#'
                    ),
                    charArrayOf(
                        '#',
                        '.',
                        '.',
                        '#',
                        '.',
                        '.',
                        '.',
                        '.',
                        '#',
                        '.',
                        '.',
                        '#',
                        '.',
                        '.',
                        '#'
                    ),
                    charArrayOf(
                        '#',
                        '.',
                        '.',
                        '.',
                        '.',
                        '.',
                        'S',
                        '.',
                        '.',
                        '.',
                        '.',
                        '.',
                        '.',
                        '.',
                        '#'
                    ),
                    charArrayOf(
                        '#',
                        '.',
                        '#',
                        '.',
                        '#',
                        '.',
                        '.',
                        '#',
                        '.',
                        '#',
                        '.',
                        '.',
                        '#',
                        '.',
                        '#'
                    ),
                    charArrayOf(
                        '#',
                        '.',
                        '.',
                        '.',
                        '.',
                        'B',
                        '.',
                        '.',
                        '.',
                        '.',
                        '.',
                        '.',
                        '.',
                        '.',
                        '#'
                    ),
                    charArrayOf(
                        '#',
                        '.',
                        '.',
                        '#',
                        '.',
                        '.',
                        '.',
                        '.',
                        '#',
                        '.',
                        '.',
                        '#',
                        '.',
                        '.',
                        '#'
                    ),
                    charArrayOf(
                        '#',
                        '.',
                        '.',
                        '.',
                        '.',
                        '.',
                        '.',
                        '.',
                        '.',
                        '.',
                        'B',
                        '.',
                        '.',
                        '.',
                        '#'
                    ),
                    charArrayOf(
                        '#',
                        '.',
                        '#',
                        '.',
                        '#',
                        '.',
                        '.',
                        '#',
                        '.',
                        '#',
                        '.',
                        '.',
                        '#',
                        '.',
                        '#'
                    ),
                    charArrayOf(
                        '#',
                        'G',
                        'G',
                        'G',
                        'G',
                        'G',
                        'G',
                        '#',
                        '.',
                        '.',
                        '.',
                        '.',
                        '.',
                        '.',
                        '#'
                    ),
                    charArrayOf(
                        '#',
                        '#',
                        '#',
                        '#',
                        '#',
                        '#',
                        '#',
                        '#',
                        '#',
                        '#',
                        '#',
                        '#',
                        '#',
                        '#',
                        '#'
                    )
                ),
                monsters = listOf(
                    // PATROL Monster 1 - Tuần tra ngang ở hành lang trên
                    MonsterData(
                        type = MonsterType.PATROL,
                        startRow = 1,
                        startColumn = 1,
                        initialDirection = Pair(0, 1),  // Sang phải
                        speed = 2.5f
                    ),
                    // PATROL Monster 2 - Tuần tra dọc qua các lối giữa
                    MonsterData(
                        type = MonsterType.PATROL,
                        startRow = 4,
                        startColumn = 13,
                        initialDirection = Pair(1, 0),  // Xuống
                        speed = 2.8f
                    ),
                    // PATROL Monster 3 - Tuần tra ngang ở khu vực dưới
                    MonsterData(
                        type = MonsterType.PATROL,
                        startRow = 17,
                        startColumn = 1,
                        initialDirection = Pair(0, 1),  // Sang phải
                        speed = 3.0f
                    ),
                    // BOUNCE Monster 1 - Nảy ngẫu nhiên ở khu vực trên, gây bất ngờ
                    MonsterData(
                        type = MonsterType.BOUNCE,
                        startRow = 3,
                        startColumn = 6,
                        initialDirection = Pair(1, 0),  // Xuống
                        speed = 2.6f
                    ),
                    // BOUNCE Monster 2 - Khu vực giữa, di chuyển linh hoạt
                    MonsterData(
                        type = MonsterType.BOUNCE,
                        startRow = 9,
                        startColumn = 4,
                        initialDirection = Pair(0, -1),  // Sang trái
                        speed = 2.9f
                    ),
                    // BOUNCE Monster 3 - Khu vực dưới, chặn đường đẩy hộp
                    MonsterData(
                        type = MonsterType.BOUNCE,
                        startRow = 15,
                        startColumn = 10,
                        initialDirection = Pair(-1, 0),  // Lên
                        speed = 2.7f
                    )
                )
            )
        )

        // ===== LEVEL 7: MÊ CUNG QUÁI VẬT =====
        levels.add(
            Level(
                id = 7,
                name = "Mê cung quái vật",
                difficulty = HARD,
                description = "Map lớn với nhiều monster patrol và bounce gây khó khăn",
                isUnlocked = true,
                map = arrayOf(
                    charArrayOf(
                        '#',
                        '#',
                        '#',
                        '#',
                        '#',
                        '#',
                        '#',
                        '#',
                        '#',
                        '#',
                        '#',
                        '#',
                        '#',
                        '#',
                        '#'
                    ),
                    charArrayOf(
                        '#',
                        '@',
                        '.',
                        '.',
                        '.',
                        '#',
                        '.',
                        '.',
                        '.',
                        '.',
                        '.',
                        '.',
                        '.',
                        '.',
                        '#'
                    ),
                    charArrayOf(
                        '#',
                        '.',
                        '#',
                        'B',
                        '#',
                        '.',
                        '.',
                        '#',
                        '.',
                        '#',
                        '.',
                        '.',
                        '#',
                        '.',
                        '#'
                    ),
                    charArrayOf(
                        '#',
                        '.',
                        '.',
                        '.',
                        '.',
                        '.',
                        '.',
                        '.',
                        '.',
                        '.',
                        '.',
                        '.',
                        '.',
                        '.',
                        '#'
                    ),
                    charArrayOf(
                        '#',
                        '#',
                        '.',
                        '#',
                        '.',
                        '.',
                        '.',
                        '.',
                        '#',
                        '.',
                        '.',
                        '#',
                        '.',
                        '#',
                        '#'
                    ),
                    charArrayOf(
                        '#',
                        '.',
                        'S',
                        '.',
                        '.',
                        '#',
                        '.',
                        '.',
                        '.',
                        '.',
                        '.',
                        '.',
                        '.',
                        '.',
                        '#'
                    ),
                    charArrayOf(
                        '#',
                        '.',
                        '#',
                        '.',
                        '#',
                        '.',
                        '.',
                        '#',
                        '.',
                        '#',
                        '.',
                        '.',
                        '#',
                        '.',
                        '#'
                    ),
                    charArrayOf(
                        '#',
                        '.',
                        '.',
                        '.',
                        '.',
                        '.',
                        '.',
                        '.',
                        '.',
                        '.',
                        '.',
                        'B',
                        '.',
                        '.',
                        '#'
                    ),
                    charArrayOf(
                        '#',
                        '#',
                        '.',
                        '#',
                        '.',
                        '.',
                        '.',
                        '.',
                        '#',
                        '.',
                        '.',
                        '#',
                        '.',
                        '#',
                        '#'
                    ),
                    charArrayOf(
                        '#',
                        '.',
                        '.',
                        '.',
                        '.',
                        '#',
                        '.',
                        '.',
                        '.',
                        '.',
                        '.',
                        '.',
                        '.',
                        '.',
                        '#'
                    ),
                    charArrayOf(
                        '#',
                        '.',
                        '#',
                        '.',
                        '#',
                        '.',
                        'B',
                        '#',
                        '.',
                        '#',
                        '.',
                        '.',
                        '#',
                        '.',
                        '#'
                    ),
                    charArrayOf(
                        '#',
                        '.',
                        '.',
                        '.',
                        '.',
                        '.',
                        '.',
                        '.',
                        '.',
                        '.',
                        '.',
                        'S',
                        '.',
                        '.',
                        '#'
                    ),
                    charArrayOf(
                        '#',
                        '#',
                        '.',
                        '#',
                        '.',
                        '.',
                        '.',
                        '.',
                        '#',
                        '.',
                        '.',
                        '#',
                        '.',
                        '#',
                        '#'
                    ),
                    charArrayOf(
                        '#',
                        '.',
                        '.',
                        '.',
                        '.',
                        '#',
                        '.',
                        'S',
                        '.',
                        '.',
                        '.',
                        '.',
                        '.',
                        '.',
                        '#'
                    ),
                    charArrayOf(
                        '#',
                        '.',
                        '#',
                        '.',
                        '#',
                        '.',
                        '.',
                        '#',
                        '.',
                        '#',
                        'B',
                        '.',
                        '#',
                        '.',
                        '#'
                    ),
                    charArrayOf(
                        '#',
                        '.',
                        '.',
                        '.',
                        '.',
                        '.',
                        'B',
                        '.',
                        '.',
                        '.',
                        '.',
                        '.',
                        '.',
                        '.',
                        '#'
                    ),
                    charArrayOf(
                        '#',
                        '#',
                        '.',
                        '#',
                        '.',
                        '.',
                        '.',
                        '.',
                        '#',
                        '.',
                        '.',
                        '#',
                        '.',
                        '#',
                        '#'
                    ),
                    charArrayOf(
                        '#',
                        '.',
                        '.',
                        '.',
                        '.',
                        '#',
                        '.',
                        '.',
                        '.',
                        '.',
                        '.',
                        '.',
                        '.',
                        '.',
                        '#'
                    ),
                    charArrayOf(
                        '#',
                        '.',
                        '#',
                        '.',
                        '#',
                        '.',
                        '.',
                        '#',
                        '.',
                        '#',
                        '.',
                        '.',
                        '#',
                        '.',
                        '#'
                    ),
                    charArrayOf(
                        '#',
                        '.',
                        '.',
                        '.',
                        '.',
                        '.',
                        '.',
                        'B',
                        '.',
                        '.',
                        '.',
                        '.',
                        '.',
                        '.',
                        '#'
                    ),
                    charArrayOf(
                        '#',
                        '#',
                        '.',
                        '#',
                        'G',
                        '.',
                        'G',
                        '.',
                        'G',
                        '.',
                        'G',
                        '#',
                        'G',
                        '.',
                        '#'
                    ),
                    charArrayOf(
                        '#',
                        '.',
                        '.',
                        '.',
                        'G',
                        '.',
                        '.',
                        '.',
                        '.',
                        '.',
                        '.',
                        '.',
                        '.',
                        '.',
                        '#'
                    ),
                    charArrayOf(
                        '#',
                        '#',
                        '#',
                        '#',
                        '#',
                        '#',
                        '#',
                        '#',
                        '#',
                        '#',
                        '#',
                        '#',
                        '#',
                        '#',
                        '#'
                    )
                ),
                monsters = listOf(
// PATROL Monster 1 - Di chuyển ngang ở khu vực trên
                    MonsterData(
                        type = MonsterType.PATROL,
                        startRow = 3,
                        startColumn = 2,
                        initialDirection = Pair(0, 1),  // Sang phải
                        speed = 2.5f
                    ),
// PATROL Monster 2 - Di chuyển dọc ở giữa map
                    MonsterData(
                        type = MonsterType.PATROL,
                        startRow = 9,
                        startColumn = 6,
                        initialDirection = Pair(-1, 0),  // lên
                        speed = 3.0f
                    ),
// PATROL Monster 3 - Di chuyển ngang ở khu vực dưới
                    MonsterData(
                        type = MonsterType.PATROL,
                        startRow = 19,
                        startColumn = 1,
                        initialDirection = Pair(0, 1),  // Sang phải
                        speed = 2.8f
                    ),
// BOUNCE Monster 1 - Khu vực trên, bắt đầu từ trái
                    MonsterData(
                        type = MonsterType.BOUNCE,
                        startRow = 3,
                        startColumn = 1,
                        initialDirection = Pair(0, 1),  // Sang phải
                        speed = 2.7f
                    ),
// BOUNCE Monster 2 - Khu vực giữa, bắt đầu từ phải
                    MonsterData(
                        type = MonsterType.BOUNCE,
                        startRow = 11,
                        startColumn = 13,
                        initialDirection = Pair(0, -1),  // Sang trái
                        speed = 3.2f
                    ),
// BOUNCE Monster 3 - Khu vực dưới, bắt đầu từ giữa
                    MonsterData(
                        type = MonsterType.BOUNCE,
                        startRow = 17,
                        startColumn = 6,
                        initialDirection = Pair(1, 0),  // Xuống
                        speed = 2.9f
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

            val newBestMoves =
                if (currentBestMoves == null || moves < currentBestMoves) moves else currentBestMoves
            val newBestTime =
                if (currentBestTime == null || time < currentBestTime) time else currentBestTime

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