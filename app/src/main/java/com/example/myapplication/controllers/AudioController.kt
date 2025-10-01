package com.example.myapplication.controllers

import android.content.Context
import com.example.myapplication.managers.MusicManager
import com.example.myapplication.managers.SoundManager

/**
 * 🔊 AudioController - Quản lý âm thanh và nhạc trong game
 *
 * Tách từ GameView để tập trung logic âm thanh
 */
class AudioController(
    private val context: Context,
    private val musicManager: MusicManager,
    private val soundManager: SoundManager
) {

    /**
     * 🎵 Toggle nhạc nền
     */
    fun toggleMusic() {
        val currentlyEnabled = musicManager.isEnabled()
        val newState = !currentlyEnabled

        if (newState) {
            // Bật nhạc: cần play music từ setting đã lưu
            val prefs = context.getSharedPreferences("music_settings", Context.MODE_PRIVATE)
            val selectedMusic = prefs.getInt("selected_music", MusicManager.MUSIC_GAME_1)
            musicManager.playMusic(selectedMusic, true)
        } else {
            // Tắt nhạc
            musicManager.setEnabled(false)
        }

        // Lưu setting
        val prefs = context.getSharedPreferences("music_settings", Context.MODE_PRIVATE)
        prefs.edit().putBoolean("music_enabled", newState).apply()

        println("🎵 Music toggled: $newState")
    }

    /**
     * 🔊 Toggle âm thanh + nhạc
     */
    fun toggleSound(): Boolean {
        // Lấy trạng thái hiện tại: nếu sound đang muted thì nghĩa là đang tắt
        val currentlyEnabled = !soundManager.isMuted()

        // Toggle: nếu đang bật thì tắt, nếu đang tắt thì bật
        val newEnabledState = !currentlyEnabled

        // Áp dụng trạng thái mới
        soundManager.setMuted(!newEnabledState)  // muted = true khi newEnabledState = false

        if (newEnabledState) {
            // Bật: play music từ setting đã lưu
            val prefs = context.getSharedPreferences("audio_settings", Context.MODE_PRIVATE)
            val selectedMusic = prefs.getInt("selected_music", MusicManager.MUSIC_GAME_1)
            musicManager.playMusic(selectedMusic, true)
        } else {
            // Tắt music
            musicManager.setEnabled(false)
        }

        // Lưu setting (bao gồm cả volume hiện tại)
        val prefs = context.getSharedPreferences("audio_settings", Context.MODE_PRIVATE)
        prefs.edit()
            .putBoolean("sound_enabled", newEnabledState)
            .putBoolean("music_enabled", newEnabledState)
            .putFloat("sound_volume", soundManager.getVolume())  // Lưu volume hiện tại
            .apply()

        println("🔊 Sound toggled: enabled=$newEnabledState, muted=${!newEnabledState}")

        return newEnabledState
    }

    /**
     * 📥 Load audio settings từ SharedPreferences
     */
    fun loadAudioSettings() {
        val prefs = context.getSharedPreferences("audio_settings", Context.MODE_PRIVATE)

        // Load music setting
        val musicEnabled = prefs.getBoolean("music_enabled", true)
        if (musicEnabled) {
            val selectedMusic = prefs.getInt("selected_music", MusicManager.MUSIC_GAME_1)
            musicManager.playMusic(selectedMusic, true)
        } else {
            musicManager.setEnabled(false)
        }

        // Load sound effects setting
        val soundEnabled = prefs.getBoolean("sound_enabled", true)
        val soundVolume = prefs.getFloat("sound_volume", 0.5f)
        soundManager.setMuted(!soundEnabled)  // muted = false khi soundEnabled = true
        soundManager.setVolume(soundVolume)
    }

    /**
     * 🔇 Set sound muted state
     */
    fun setSoundMuted(muted: Boolean) {
        soundManager.setMuted(muted)
    }

    /**
     * 🔊 Check if sound is muted
     */
    fun isSoundMuted(): Boolean = soundManager.isMuted()

    /**
     * 🎵 Check if music is enabled
     */
    fun isMusicEnabled(): Boolean = musicManager.isEnabled()
}

