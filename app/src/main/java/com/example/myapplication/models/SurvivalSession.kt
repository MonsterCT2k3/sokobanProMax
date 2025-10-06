package com.example.myapplication.models

/**
 * 🏃 SurvivalSession - Quản lý trạng thái của một phiên Survival Mode
 * 
 * Lưu trữ tất cả thông tin cần thiết cho một phiên chơi Survival:
 * - Levels cần chơi (1, 3, 4)
 * - Trạng thái hiện tại (level nào, còn bao nhiêu mạng)
 * - Thời gian chơi tổng cộng
 * - Số đạn các loại
 */
data class SurvivalSession(
    val sessionId: String,
    val levels: List<Int> = listOf(1, 3, 4),  // Fixed levels for Survival mode
    var currentLevelIndex: Int = 0,
    var lives: Int = 3,
    var totalTimeMs: Long = 0L,
    val sessionStartTime: Long = System.currentTimeMillis(),
    var normalAmmo: Int = 0,
    var pierceAmmo: Int = 0,
    var stunAmmo: Int = 3,
    var isCompleted: Boolean = false,
    var isFailed: Boolean = false,
    val completedLevels: MutableList<Int> = mutableListOf()
) {
    /**
     * Get current level ID
     */
    val currentLevelId: Int
        get() = if (currentLevelIndex < levels.size) levels[currentLevelIndex] else -1

    /**
     * Check if this is the last level
     */
    val isLastLevel: Boolean
        get() = currentLevelIndex >= levels.size - 1

    /**
     * Get total number of levels
     */
    val totalLevels: Int
        get() = levels.size
}