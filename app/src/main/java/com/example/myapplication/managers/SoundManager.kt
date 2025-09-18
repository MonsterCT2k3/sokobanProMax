package com.example.myapplication.managers

import android.content.Context
import android.media.SoundPool
import com.example.myapplication.R

class SoundManager(private val context: Context) {

    private var soundPool: SoundPool? = null
    private var soundIds = mutableMapOf<String, Int>()
    private var isMuted = false

    init {
        initSoundPool()
        loadSounds()
    }

    private fun initSoundPool() {
        soundPool = SoundPool.Builder()
            .setMaxStreams(3)  // Đủ cho 4 âm thanh của chúng ta
            .setAudioAttributes(
                android.media.AudioAttributes.Builder()
                    .setUsage(android.media.AudioAttributes.USAGE_GAME)
                    .setContentType(android.media.AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .build()
            )
            .build()
    }

    private fun loadSounds() {
        soundIds["move"] = soundPool?.load(context, R.raw.move, 1) ?: 0
        soundIds["shoot"] = soundPool?.load(context, R.raw.shoot, 1) ?: 0
        soundIds["bullet_wall"] = soundPool?.load(context, R.raw.bullet_wall, 1) ?: 0
        soundIds["bump_wall"] = soundPool?.load(context, R.raw.bump_wall, 1) ?: 0
    }

    fun playSound(soundName: String, volume: Float = 1.0f) {
        if (isMuted) return

        soundIds[soundName]?.let { soundId ->
            if (soundId != 0) {
                soundPool?.play(soundId, volume, volume, 1, 0, 1.0f)
            }
        }
    }

    fun setMuted(muted: Boolean) {
        isMuted = muted
    }

    fun isMuted(): Boolean = isMuted

    fun cleanup() {
        soundPool?.release()
        soundPool = null
    }
}