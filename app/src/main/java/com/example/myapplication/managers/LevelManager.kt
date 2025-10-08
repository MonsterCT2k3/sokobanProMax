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
                name = "Địa hình phức tạp",
                difficulty = MEDIUM,
                description = "Địa hình cản trở và 2 bounce monsters",
                isUnlocked = true,
                map = arrayOf(
                    charArrayOf('#', '#', '#', '#', '#', '#', '#', '#', '#', '#', '#', '#'),
                    charArrayOf('#', '@', '.', '.', '.', '.', '.', '.', '.', '.', '.', '#'),
                    charArrayOf('#', '.', '#', '#', '#', '.', '#', '#', '#', '.', '.', '#'),
                    charArrayOf('#', '.', '#', '.', '.', '.', '.', '.', '#', '.', '.', '#'),
                    charArrayOf('#', '.', '#', '.', 'B', '.', '.', '.', '#', '.', '.', '#'),
                    charArrayOf('#', '.', '.', '.', '.', '.', '.', '.', '.', 'G', '.', '#'),
                    charArrayOf('#', '.', '#', '#', '#', '.', '.', '.', '.', '.', '.', '#'),
                    charArrayOf('#', '.', '.', '.', '#', '.', 'B', '.', 'G', '.', '.', '#'),
                    charArrayOf('#', '.', '.', '.', '.', '.', '.', '.', '#', '#', '#', '#'),
                    charArrayOf('#', '.', '.', 'B', '.', '.', 'G', '.', '.', '.', '.', '#'),
                    charArrayOf('#', '.', '.', '.', '.', '.', '.', '.', '.', '.', '.', '#'),
                    charArrayOf('#', '#', '#', '#', '#', '#', '#', '#', '#', '#', '#', '#')
                ),
                monsters = listOf(
                    MonsterData(
                        type = MonsterType.BOUNCE,
                        startRow = 2,
                        startColumn = 9,
                        initialDirection = Pair(1, 0),  // Hướng ban đầu: đi xuống
                        speed = 2.2f
                    ),
                    MonsterData(
                        type = MonsterType.BOUNCE,
                        startRow = 7,
                        startColumn = 10,
                        initialDirection = Pair(0, -1),  // Hướng ban đầu: đi sang trái
                        speed = 2.5f
                    )
                )
            )
        )

        // ===== LEVEL 3: THỬ THÁCH =====
        levels.add(
            Level(
                id = 3,
                name = "Compact Maze",
                difficulty = MEDIUM,
                description = "A compact 7x7 maze with goals and obstacles",
                isUnlocked = true,
                map = arrayOf(
                    charArrayOf('#', '#', '#', '#', '#', '#', '#'),
                    charArrayOf('#', '.', '.', '.', '.', '.', '#'),
                    charArrayOf('#', '.', 'B', '#', 'G', '.', '#'),
                    charArrayOf('#', '@', 'B', '.', '.', 'G', '#'),
                    charArrayOf('#', '.', 'B', 'B', '.', '.', '#'),
                    charArrayOf('#', '.', 'G', '.', 'G', '.', '#'),
                    charArrayOf('#', '#', '#', '#', '#', '#', '#')
                ),
                monsters = listOf(
                    MonsterData(
                        type = MonsterType.BOUNCE,
                        startRow = 1,
                        startColumn = 5,
                        initialDirection = Pair(0, -1),  // Hướng ban đầu: sang phải
                        speed = 2.0f
                    )
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
                    charArrayOf('#', '#', '#', '#', '#', '#', '#', '#', '#', '#', '#', '#', '#', '#', '#'),
                    charArrayOf('#', '.', '.', '.', '.', '.', '.', '.', '.', '.', '.', '.', '.', '.', '#'),
                    charArrayOf('#', '.', '#', '.', '#', '.', '.', '#', '.', '#', '.', '.', '#', '.', '#'),
                    charArrayOf('#', '@', '.', '.', '.', 'B', '.', '.', '.', '.', '.', '.', '.', '.', '#'),
                    charArrayOf('#', '.', '.', '#', '.', '.', '.', '.', '#', '.', '.', '#', '.', '.', '#'),
                    charArrayOf('#', '.', '.', '.', '.', '.', 'S', '.', '.', '.', '.', '.', '.', '.', '#'),
                    charArrayOf('#', '.', '#', '.', '#', '.', '.', '#', '.', '#', '.', '.', '#', '.', '#'),
                    charArrayOf('#', '.', '.', '.', '.', 'B', '.', '.', '.', '.', '.', '.', '.', '.', '#'),
                    charArrayOf('#', '.', '.', '#', '.', '.', '.', '.', '#', '.', '.', '#', '.', '.', '#'),
                    charArrayOf('#', '.', '.', '.', '.', '.', '.', '.', '.', '.', 'B', '.', '.', '.', '#'),
                    charArrayOf('#', '.', '#', '.', '#', '.', '.', '#', '.', '#', '.', '.', '#', '.', '#'),
                    charArrayOf('#', '.', '.', '.', '.', 'B', '.', '.', '.', '.', '.', '.', '.', '.', '#'),
                    charArrayOf('#', '.', '.', '#', '.', '.', '.', '.', '#', '.', '.', '#', '.', '.', '#'),
                    charArrayOf('#', '.', '.', '.', '.', '.', 'S', '.', '.', '.', '.', '.', '.', '.', '#'),
                    charArrayOf('#', '.', '#', '.', '#', '.', '.', '#', '.', '#', '.', '.', '#', '.', '#'),
                    charArrayOf('#', '.', '.', '.', '.', 'B', '.', '.', '.', '.', '.', '.', '.', '.', '#'),
                    charArrayOf('#', '.', '.', '#', '.', '.', '.', '.', '#', '.', '.', '#', '.', '.', '#'),
                    charArrayOf('#', '.', '.', '.', '.', '.', '.', '.', '.', '.', 'B', '.', '.', '.', '#'),
                    charArrayOf('#', '.', '#', '.', '#', '.', '.', '#', '.', '#', '.', '.', '#', '.', '#'),
                    charArrayOf('#', 'G', 'G', 'G', 'G', 'G', 'G', '#', '.', '.', '.', '.', '.', '.', '#'),
                    charArrayOf('#', '#', '#', '#', '#', '#', '#', '#', '#', '#', '#', '#', '#', '#', '#')
                ),
                monsters = listOf(
                    MonsterData(
                        type = MonsterType.PATROL,
                        startRow = 1,
                        startColumn = 1,
                        initialDirection = Pair(0, 1), // Sang phải
                        speed = 2.5f
                    ),
                    MonsterData(
                        type = MonsterType.PATROL,
                        startRow = 4,
                        startColumn = 13,
                        initialDirection = Pair(1, 0), // Xuống
                        speed = 2.8f
                    ),
                    MonsterData(
                        type = MonsterType.PATROL,
                        startRow = 17,
                        startColumn = 1,
                        initialDirection = Pair(0, 1), // Sang phải
                        speed = 3.0f
                    ),
                    MonsterData(
                        type = MonsterType.BOUNCE,
                        startRow = 3,
                        startColumn = 6,
                        initialDirection = Pair(1, 0), // Xuống
                        speed = 2.6f
                    ),
                    MonsterData(
                        type = MonsterType.BOUNCE,
                        startRow = 9,
                        startColumn = 4,
                        initialDirection = Pair(0, -1), // Sang trái
                        speed = 2.9f
                    ),
                    MonsterData(
                        type = MonsterType.BOUNCE,
                        startRow = 15,
                        startColumn = 10,
                        initialDirection = Pair(-1, 0), // Lên
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
                    charArrayOf('#', '#', '#', '#', '#', '#', '#', '#', '#', '#', '#', '#', '#', '#', '#'),
                    charArrayOf('#', '@', '.', '.', '.', '#', '.', '.', '.', '.', '.', '.', '.', '.', '#'),
                    charArrayOf('#', '.', '#', 'B', '#', '.', '.', '#', '.', '#', '.', '.', '#', '.', '#'),
                    charArrayOf('#', '.', '.', '.', '.', '.', '.', '.', '.', '.', '.', '.', '.', '.', '#'),
                    charArrayOf('#', '#', '.', '#', '.', '.', '.', '.', '#', '.', '.', '#', '.', '#', '#'),
                    charArrayOf('#', '.', 'S', '.', '.', '#', '.', '.', '.', '.', '.', '.', '.', '.', '#'),
                    charArrayOf('#', '.', '#', '.', '#', '.', '.', '#', '.', '#', '.', '.', '#', '.', '#'),
                    charArrayOf('#', '.', '.', '.', '.', '.', '.', '.', '.', '.', '.', 'B', '.', '.', '#'),
                    charArrayOf('#', '#', '.', '#', '.', '.', '.', '.', '#', '.', '.', '#', '.', '#', '#'),
                    charArrayOf('#', '.', '.', '.', '.', '#', '.', '.', '.', '.', '.', '.', '.', '.', '#'),
                    charArrayOf('#', '.', '#', '.', '#', '.', 'B', '#', '.', '#', '.', '.', '#', '.', '#'),
                    charArrayOf('#', '.', '.', '.', '.', '.', '.', '.', '.', '.', '.', 'S', '.', '.', '#'),
                    charArrayOf('#', '#', '.', '#', '.', '.', '.', '.', '#', '.', '.', '#', '.', '#', '#'),
                    charArrayOf('#', '.', '.', '.', '.', '#', '.', 'S', '.', '.', '.', '.', '.', '.', '#'),
                    charArrayOf('#', '.', '#', '.', '#', '.', '.', '#', '.', '#', 'B', '.', '#', '.', '#'),
                    charArrayOf('#', '.', '.', '.', '.', '.', 'B', '.', '.', '.', '.', '.', '.', '.', '#'),
                    charArrayOf('#', '#', '.', '#', '.', '.', '.', '.', '#', '.', '.', '#', '.', '#', '#'),
                    charArrayOf('#', '.', '.', '.', '.', '#', '.', '.', '.', '.', '.', '.', '.', '.', '#'),
                    charArrayOf('#', '.', '#', '.', '#', '.', '.', '#', '.', '#', '.', '.', '#', '.', '#'),
                    charArrayOf('#', '.', '.', '.', '.', '.', '.', 'B', '.', '.', '.', '.', 'G', '.', '#'),
                    charArrayOf('#', '#', '.', '#', 'G', '.', 'G', '.', 'G', '.', 'G', '#', '.', '.', '#'),
                    charArrayOf('#', '.', '.', '.', 'G', '.', '.', '.', '.', '.', '.', '.', '.', '.', '#'),
                    charArrayOf('#', '#', '#', '#', '#', '#', '#', '#', '#', '#', '#', '#', '#', '#', '#')
                ),
                monsters = listOf(
                    MonsterData(
                        type = MonsterType.PATROL,
                        startRow = 3,
                        startColumn = 2,
                        initialDirection = Pair(0, 1), // Sang phải
                        speed = 2.5f
                    ),
                    MonsterData(
                        type = MonsterType.PATROL,
                        startRow = 9,
                        startColumn = 6,
                        initialDirection = Pair(-1, 0), // Lên
                        speed = 3.0f
                    ),
                    MonsterData(
                        type = MonsterType.PATROL,
                        startRow = 19,
                        startColumn = 1,
                        initialDirection = Pair(0, 1), // Sang phải
                        speed = 2.8f
                    ),
                    MonsterData(
                        type = MonsterType.BOUNCE,
                        startRow = 3,
                        startColumn = 1,
                        initialDirection = Pair(0, 1), // Sang phải
                        speed = 2.7f
                    ),
                    MonsterData(
                        type = MonsterType.BOUNCE,
                        startRow = 11,
                        startColumn = 13,
                        initialDirection = Pair(0, -1), // Sang trái
                        speed = 3.2f
                    ),
                    MonsterData(
                        type = MonsterType.BOUNCE,
                        startRow = 17,
                        startColumn = 6,
                        initialDirection = Pair(1, 0), // Xuống
                        speed = 2.9f
                    )
                )
            )
        )

        // ===== LEVEL 8: MÊ CUNG NGUY HIỂM =====
        levels.add(
            Level(
                id = 8,
                name = "Mê cung nguy hiểm",
                difficulty = HARD,
                description = "Map 20x15 với địa hình phức tạp, 2 patrol và 3 bounce monsters",
                isUnlocked = true,
                map = arrayOf(
                    charArrayOf('#', '#', '#', '#', '#', '#', '#', '#', '#', '#', '#', '#', '#', '#', '#'),
                    charArrayOf('#', '@', '.', '.', '.', '#', '.', '.', '.', '.', '.', '.', '.', '.', '#'),
                    charArrayOf('#', '.', '#', '#', '.', '.', '.', '#', '#', '#', '.', '.', '#', '.', '#'),
                    charArrayOf('#', '.', '.', '.', '.', '.', '.', '.', '.', '.', '.', '.', '.', '.', '#'),
                    charArrayOf('#', '#', '.', '#', '#', '.', '.', '.', '#', '#', '#', '.', '#', '#', '#'),
                    charArrayOf('#', '.', '.', '.', '.', '.', 'S', '.', '.', '.', '.', '.', '.', '.', '#'),
                    charArrayOf('#', '.', '#', '.', '#', '#', '.', '#', '.', '#', '.', '#', '#', '.', '#'),
                    charArrayOf('#', '.', '.', 'B', '.', '.', '.', '.', '.', '.', 'B', '.', '.', '.', '#'),
                    charArrayOf('#', '#', '.', '#', '.', '.', '.', '#', '.', '.', '.', '#', '.', '#', '#'),
                    charArrayOf('#', '.', '.', '.', '.', '#', '.', '.', '.', '#', '.', '.', '.', '.', '#'),
                    charArrayOf('#', '.', '#', '#', '.', '.', '.', '.', '.', '.', '.', '#', '#', '.', '#'),
                    charArrayOf('#', '.', '.', '.', '.', '.', '.', '#', '.', '.', '.', '.', '.', '.', '#'),
                    charArrayOf('#', '#', '.', '#', '.', '#', '.', '.', '.', '#', '.', '#', '.', '#', '#'),
                    charArrayOf('#', '.', '.', '.', 'B', '.', '.', '.', '.', '.', 'B', '.', '.', '.', '#'),
                    charArrayOf('#', '.', '#', '.', '.', '.', 'S', '.', '.', '.', '.', '.', '#', '.', '#'),
                    charArrayOf('#', '.', '.', '.', '#', '.', '.', '.', '#', '.', '.', 'B', '.', '.', '#'),
                    charArrayOf('#', '#', '#', '.', '.', '.', '.', '#', '.', '.', '.', '.', '#', '#', '#'),
                    charArrayOf('#', '.', '.', '.', '.', 'B', '.', '.', '.', '.', '.', '.', '.', '.', '#'),
                    charArrayOf('#', '.', 'G', '.', 'G', '.', 'G', '.', 'G', '.', 'G', '.', 'G', '.', '#'),
                    charArrayOf('#', '#', '#', '#', '#', '#', '#', '#', '#', '#', '#', '#', '#', '#', '#')
                ),
                monsters = listOf(
                    MonsterData(
                        type = MonsterType.PATROL,
                        startRow = 3,
                        startColumn = 1,
                        initialDirection = Pair(0, 1),  // Đi sang phải
                        speed = 2.5f
                    ),
                    MonsterData(
                        type = MonsterType.PATROL,
                        startRow = 11,
                        startColumn = 13,
                        initialDirection = Pair(1, 0),  // Đi xuống
                        speed = 2.8f
                    ),
                    MonsterData(
                        type = MonsterType.BOUNCE,
                        startRow = 2,
                        startColumn = 10,
                        initialDirection = Pair(1, 0),  // Hướng ban đầu: đi xuống
                        speed = 2.6f
                    ),
                    MonsterData(
                        type = MonsterType.BOUNCE,
                        startRow = 9,
                        startColumn = 5,
                        initialDirection = Pair(0, 1),  // Hướng ban đầu: sang phải
                        speed = 3.0f
                    ),
                    MonsterData(
                        type = MonsterType.BOUNCE,
                        startRow = 15,
                        startColumn = 11,
                        initialDirection = Pair(-1, 0),  // Hướng ban đầu: đi lên
                        speed = 2.7f
                    )
                )
            )
        )
        // ===== LEVEL 9: ĐẾN ĐÍCH KHẮP NƠI =====
        levels.add(
            Level(
                id = 9,
                name = "Đến đích khắp nơi",
                difficulty = HARD,
                description = "Mê cung rộng lớn với nhiều mục tiêu và quái vật",
                isUnlocked = true,
                map = arrayOf(
                    charArrayOf('#', '#', '#', '#', '#', '#', '#', '#', '#', '#', '#', '#', '#', '#', '#'),
                    charArrayOf('#', '@', '.', '.', '.', '#', '.', '.', '.', '.', '.', 'G', '.', '.', '#'),
                    charArrayOf('#', '.', '#', '#', '.', '.', '.', '#', '#', '#', '.', '.', '#', '.', '#'),
                    charArrayOf('#', '.', '.', 'B', '.', '.', '.', '.', '.', '.', '.', '.', '.', '.', '#'),
                    charArrayOf('#', '#', '.', '#', '#', '.', '.', '.', '#', '#', '#', '.', '#', '#', '#'),
                    charArrayOf('#', '.', '.', '.', '.', '.', 'S', '.', '.', '.', '.', '.', '.', 'G', '#'),
                    charArrayOf('#', '.', '#', '.', '#', '#', '.', '#', '.', '#', '.', '#', '#', '.', '#'),
                    charArrayOf('#', 'G', '.', '.', '.', '.', '.', '.', '.', '.', 'B', '.', '.', '.', '#'),
                    charArrayOf('#', '#', '.', '#', '.', '.', '.', '#', '.', '.', '.', '#', '.', '#', '#'),
                    charArrayOf('#', '.', '.', '.', 'B', '#', '.', '.', '.', '#', '.', '.', '.', '.', '#'),
                    charArrayOf('#', '.', '#', '#', '.', '.', '.', '.', '.', '.', '.', '#', '#', '.', '#'),
                    charArrayOf('#', '.', '.', '.', '.', '.', 'G', '#', '.', '.', '.', '.', '.', '.', '#'),
                    charArrayOf('#', '#', '.', '#', '.', '#', '.', '.', '.', '#', '.', '#', '.', '#', '#'),
                    charArrayOf('#', '.', '.', '.', '.', '.', '.', 'B', '.', '.', 'B', '.', '.', '.', '#'),
                    charArrayOf('#', '.', '#', 'G', '.', '.', 'S', '.', '.', '.', '.', '.', '#', '.', '#'),
                    charArrayOf('#', '.', '.', '.', '#', '.', '.', '.', '#', '.', '.', 'B', '.', '.', '#'),
                    charArrayOf('#', '#', '#', '.', '.', '.', '.', '#', '.', '.', '.', '.', '#', '#', '#'),
                    charArrayOf('#', '.', '.', '.', '.', '.', '.', '.', '.', 'G', '.', '.', '.', '.', '#'),
                    charArrayOf('#', '.', '#', '.', '.', '#', '.', '.', '.', '.', '.', '#', '.', '.', '#'),
                    charArrayOf('#', '#', '#', '#', '#', '#', '#', '#', '#', '#', '#', '#', '#', '#', '#')
                ),
                monsters = listOf(
                    MonsterData(
                        type = MonsterType.PATROL,
                        startRow = 3,
                        startColumn = 1,
                        initialDirection = Pair(0, 1),  // Đi sang phải
                        speed = 2.6f
                    ),
                    MonsterData(
                        type = MonsterType.PATROL,
                        startRow = 11,
                        startColumn = 13,
                        initialDirection = Pair(-1, 0),  // Đi lên
                        speed = 2.9f
                    ),
                    MonsterData(
                        type = MonsterType.BOUNCE,
                        startRow = 2,
                        startColumn = 10,
                        initialDirection = Pair(1, 0),  // Hướng ban đầu: đi xuống
                        speed = 2.7f
                    ),
                    MonsterData(
                        type = MonsterType.BOUNCE,
                        startRow = 9,
                        startColumn = 5,
                        initialDirection = Pair(0, 1),  // Hướng ban đầu: sang phải
                        speed = 3.1f
                    ),
                    MonsterData(
                        type = MonsterType.BOUNCE,
                        startRow = 16,
                        startColumn = 10,
                        initialDirection = Pair(-1, 0),  // Hướng ban đầu: đi lên
                        speed = 2.8f
                    )
                )
            )
        )

        // ===== LEVEL 10: ĐỊA NGỤC QUÁI VẬT =====
        levels.add(
            Level(
                id = 10,
                name = "Địa ngục quái vật",
                difficulty = HARD,
                description = "Thử thách tối thượng!",
                isUnlocked = true,
                map = arrayOf(
                    charArrayOf('#', '#', '#', '#', '#', '#', '#', '#', '#', '#', '#', '#', '#', '#', '#'),
                    charArrayOf('#', '@', '.', '.', '.', '.', '.', '.', '.', '.', '.', '.', '.', '.', '#'),
                    charArrayOf('#', '.', '#', '#', '.', '#', '.', '#', '#', '.', '#', '.', '#', '#', '#'),
                    charArrayOf('#', '.', '.', '.', '.', '.', '.', '.', '.', '.', '.', '.', '.', '.', '#'),
                    charArrayOf('#', '#', '.', '#', '.', '.', '.', '#', '.', '.', '.', '#', '.', '#', '#'),
                    charArrayOf('#', '.', '.', '.', '.', 'S', '.', '.', '.', '.', '.', '.', '.', '.', '#'),
                    charArrayOf('#', '.', '#', '.', '#', '.', '.', '.', '#', '.', '#', '.', '#', '.', '#'),
                    charArrayOf('#', '.', '.', '.', '.', '.', 'B', '.', '.', '.', '.', '.', '.', '.', '#'),
                    charArrayOf('#', '#', '#', '.', '#', '.', '.', '.', '#', '.', '#', '#', '#', '#', '#'),
                    charArrayOf('#', '.', '.', '.', '.', '.', '.', 'B', '.', '.', '.', '.', '.', '.', '#'),
                    charArrayOf('#', '.', '#', '.', '#', '.', '#', '.', '.', '#', '.', '#', '.', '#', '#'),
                    charArrayOf('#', '.', '.', '.', '.', '.', '.', '.', '.', '.', '.', '.', '.', '.', '#'),
                    charArrayOf('#', '#', '.', '#', '.', '.', 'S', '.', '.', '.', '#', '.', '#', '.', '#'),
                    charArrayOf('#', '.', '.', '.', '.', 'B', '.', '.', 'B', '.', '.', '.', '.', '.', '#'),
                    charArrayOf('#', '.', '#', '#', '.', '.', '.', '.', '.', '.', '#', '#', '.', '#', '#'),
                    charArrayOf('#', '.', '.', '.', '.', '.', '.', '.', '.', '.', '.', '.', '.', '.', '#'),
                    charArrayOf('#', '#', '.', '#', '.', 'B', '.', '#', '.', 'B', '.', '#', '.', '#', '#'),
                    charArrayOf('#', '.', '.', '.', '.', '.', '.', '.', '.', '.', '.', '.', '.', '.', '#'),
                    charArrayOf('#', '.', '#', '.', 'G', '.', 'G', '.', 'G', '.', 'G', '.', 'G', 'G', '#'),
                    charArrayOf('#', '#', '#', '#', '#', '#', '#', '#', '#', '#', '#', '#', '#', '#', '#')
                ),
                monsters = listOf(
                    // 3 PATROL MONSTERS - Tuần tra các tuyến chính
                    MonsterData(
                        type = MonsterType.PATROL,
                        startRow = 3,
                        startColumn = 1,
                        initialDirection = Pair(0, 1),  // Đi sang phải
                        speed = 2.5f
                    ),
                    MonsterData(
                        type = MonsterType.PATROL,
                        startRow = 11,
                        startColumn = 13,
                        initialDirection = Pair(0, -1),  // Đi sang trái
                        speed = 2.7f
                    ),
                    MonsterData(
                        type = MonsterType.PATROL,
                        startRow = 17,
                        startColumn = 7,
                        initialDirection = Pair(-1, 0),  // Đi lên
                        speed = 2.9f
                    ),
                    // 4 BOUNCE MONSTERS - Nảy khắp map
                    MonsterData(
                        type = MonsterType.BOUNCE,
                        startRow = 5,
                        startColumn = 10,
                        initialDirection = Pair(1, 0),  // Hướng ban đầu: đi xuống
                        speed = 2.8f
                    ),
                    MonsterData(
                        type = MonsterType.BOUNCE,
                        startRow = 7,
                        startColumn = 3,
                        initialDirection = Pair(0, 1),  // Hướng ban đầu: sang phải
                        speed = 3.0f
                    ),
                    MonsterData(
                        type = MonsterType.BOUNCE,
                        startRow = 11,
                        startColumn = 5,
                        initialDirection = Pair(1, 1),  // Hướng ban đầu: chéo xuống phải
                        speed = 3.2f
                    ),
                    MonsterData(
                        type = MonsterType.BOUNCE,
                        startRow = 15,
                        startColumn = 11,
                        initialDirection = Pair(-1, 0),  // Hướng ban đầu: đi lên
                        speed = 2.6f
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