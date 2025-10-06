package com.example.myapplication.managers

import com.example.myapplication.models.SurvivalSession
import java.util.UUID

/**
 * 🏃 SurvivalManager - Singleton quản lý logic Survival Mode
 * 
 * Chịu trách nhiệm:
 * - Tạo và quản lý SurvivalSession
 * - Xử lý logic mất mạng và hoàn thành level
 * - Theo dõi tiến độ qua các level
 * - Quản lý trạng thái ammo
 */
object SurvivalManager {
    
    private var currentSession: SurvivalSession? = null

    /**
     * Bắt đầu phiên Survival mới
     */
    fun startNewSession(): SurvivalSession {
        val sessionId = UUID.randomUUID().toString()
        currentSession = SurvivalSession(sessionId = sessionId)
        println("🏃 SurvivalManager: Started new session $sessionId")
        return currentSession!!
    }

    /**
     * Lấy phiên hiện tại
     */
    fun getCurrentSession(): SurvivalSession? {
        return currentSession
    }

    /**
     * Cập nhật ammo trong session
     */
    fun updateSessionAmmo(normalAmmo: Int, pierceAmmo: Int, stunAmmo: Int) {
        currentSession?.let { session ->
            session.normalAmmo = normalAmmo
            session.pierceAmmo = pierceAmmo
            session.stunAmmo = stunAmmo
            println("🏃 SurvivalManager: Updated ammo - N:$normalAmmo, P:$pierceAmmo, S:$stunAmmo")
        }
    }

    /**
     * Mất 1 mạng
     * @return true nếu game over (hết mạng), false nếu còn mạng
     */
    fun loseLife(): Boolean {
        currentSession?.let { session ->
            session.lives--
            println("🏃 SurvivalManager: Lost life! Lives remaining: ${session.lives}")
            
            if (session.lives <= 0) {
                session.isFailed = true
                println("🏃 SurvivalManager: GAME OVER - No lives left!")
                return true
            }
        }
        return false
    }

    /**
     * Hoàn thành level hiện tại
     * @param levelTimeMs Thời gian hoàn thành level này
     * @return true nếu đã hoàn thành tất cả levels, false nếu còn level tiếp theo
     */
    fun completeLevel(levelTimeMs: Long): Boolean {
        currentSession?.let { session ->
            // Cập nhật thời gian tổng
            session.totalTimeMs += levelTimeMs
            
            // Thêm level vào danh sách đã hoàn thành
            val completedLevelId = session.currentLevelId
            if (!session.completedLevels.contains(completedLevelId)) {
                session.completedLevels.add(completedLevelId)
            }
            
            println("🏃 SurvivalManager: Completed level $completedLevelId in ${levelTimeMs}ms")
            println("🏃 SurvivalManager: Total time so far: ${session.totalTimeMs}ms")
            
            // Chuyển sang level tiếp theo
            session.currentLevelIndex++
            
            // Kiểm tra xem đã hoàn thành tất cả levels chưa
            if (session.currentLevelIndex >= session.levels.size) {
                session.isCompleted = true
                println("🏃 SurvivalManager: ALL LEVELS COMPLETED! Total time: ${session.totalTimeMs}ms")
                return true
            } else {
                val nextLevelId = session.currentLevelId
                println("🏃 SurvivalManager: Moving to next level: $nextLevelId")
                return false
            }
        }
        return false
    }

    /**
     * Kết thúc phiên hiện tại
     */
    fun endSession() {
        currentSession?.let { session ->
            println("🏃 SurvivalManager: Ending session ${session.sessionId}")
        }
        currentSession = null
    }

    /**
     * Lấy số mạng hiện tại
     */
    fun getLivesCount(): Int {
        return currentSession?.lives ?: 0
    }
}