package com.example.myapplication.managers

import android.content.Context
import android.content.SharedPreferences
import org.json.JSONArray
import org.json.JSONObject

/**
 * 🏆 HighScoreManager - Quản lý top 10 kỷ lục thời gian hoàn thành level
 */
class HighScoreManager(private val context: Context) {

    private val prefs: SharedPreferences = context.getSharedPreferences("high_scores", Context.MODE_PRIVATE)

    companion object {
        private const val KEY_PREFIX = "level_high_scores_"
        private const val MAX_SCORES_PER_LEVEL = 10
    }

    /**
     * Lưu kỷ lục cho level (lưu top 10 thời gian tốt nhất)
     */
    fun saveHighScore(levelId: Int, timeMillis: Long) {
        val key = getKeyForLevel(levelId)
        val currentScores = getHighScores(levelId).toMutableList()

        // Thêm thời gian mới
        currentScores.add(timeMillis)

        // Sắp xếp theo thời gian tăng dần (thời gian ngắn nhất lên đầu)
        currentScores.sort()

        // Giữ chỉ top 10
        val topScores = currentScores.take(MAX_SCORES_PER_LEVEL)

        // Lưu dưới dạng JSON array
        val jsonArray = JSONArray()
        topScores.forEach { jsonArray.put(it) }

        prefs.edit().putString(key, jsonArray.toString()).apply()
    }

    /**
     * Lấy danh sách top 10 kỷ lục cho level
     */
    fun getHighScores(levelId: Int): List<Long> {
        val key = getKeyForLevel(levelId)
        val jsonString = prefs.getString(key, null) ?: return emptyList()

        return try {
            val jsonArray = JSONArray(jsonString)
            (0 until jsonArray.length()).map { jsonArray.getLong(it) }
        } catch (e: Exception) {
            emptyList()
        }
    }

    /**
     * Lấy kỷ lục tốt nhất cho level (thời gian ngắn nhất)
     */
    fun getBestHighScore(levelId: Int): Long? {
        val scores = getHighScores(levelId)
        return scores.minOrNull()
    }

    /**
     * Kiểm tra xem thời gian có vào top 10 không
     */
    fun isNewHighScore(levelId: Int, timeMillis: Long): Boolean {
        val currentScores = getHighScores(levelId)

        // Nếu chưa có kỷ lục nào
        if (currentScores.isEmpty()) return true

        // Nếu chưa đủ 10 kỷ lục
        if (currentScores.size < MAX_SCORES_PER_LEVEL) return true

        // Nếu thời gian mới tốt hơn kỷ lục kém nhất hiện tại
        return timeMillis < currentScores.last()
    }

    /**
     * Kiểm tra xem thời gian có phải kỷ lục tốt nhất không
     */
    fun isBestHighScore(levelId: Int, timeMillis: Long): Boolean {
        val bestScore = getBestHighScore(levelId)
        return bestScore == null || timeMillis < bestScore
    }

    /**
     * Format thời gian thành MM:SS
     */
    fun formatTime(timeMillis: Long): String {
        val totalSeconds = timeMillis / 1000
        val minutes = totalSeconds / 60
        val seconds = totalSeconds % 60
        return String.format("%02d:%02d", minutes, seconds)
    }

    /**
     * Xóa tất cả kỷ lục (cho debug)
     */
    fun clearAllHighScores() {
        val editor = prefs.edit()
        val allKeys = prefs.all.keys.filter { it.startsWith(KEY_PREFIX) }
        allKeys.forEach { editor.remove(it) }
        editor.apply()
    }

    /**
     * Lấy danh sách tất cả level có kỷ lục
     */
    fun getAllLevelsWithScores(): Set<Int> {
        return prefs.all
            .keys
            .filter { it.startsWith(KEY_PREFIX) }
            .mapNotNull { key ->
                key.removePrefix(KEY_PREFIX).toIntOrNull()
            }
            .toSet()
    }

    /**
     * Lấy số lượng kỷ lục của level
     */
    fun getScoreCount(levelId: Int): Int {
        return getHighScores(levelId).size
    }

    private fun getKeyForLevel(levelId: Int): String {
        return "$KEY_PREFIX$levelId"
    }
}
