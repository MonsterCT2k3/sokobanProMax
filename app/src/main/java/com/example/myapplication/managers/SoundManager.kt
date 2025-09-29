package com.example.myapplication.managers

import android.content.Context
import android.media.SoundPool
import android.util.Log
import com.example.myapplication.R

class SoundManager private constructor(private val context: Context) {

    private var soundPool: SoundPool? = null
    private var soundIds = mutableMapOf<String, Int>()
    private var isMuted = false
    private var volume = 1.0f  // Volume cho sound effects (0.0 - 1.0)

    companion object{
        @Volatile
        private var INSTANCE: SoundManager? = null

        fun getInstance(context: Context): SoundManager {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: SoundManager(context.applicationContext).also { INSTANCE = it }
            }
        }

        fun getInstance(): SoundManager? {
            return INSTANCE
        }
    }

    init {
        initSoundPool()
        loadSounds()
        // KHÔNG load settings trong init, để activity tự load khi cần
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
        soundIds["ammo_pickup"] = soundPool?.load(context, R.raw.ammo_pickup, 1) ?: 0
        soundIds["pierce_ammo_pickup"] = soundPool?.load(context, R.raw.ammo_pickup, 1) ?: 0  // Tạm dùng cùng sound
        soundIds["monster_hit"] = soundPool?.load(context, R.raw.monster_hit, 1) ?: 0  // 🆕 THÊM ÂM THANH KHI BẮN TRÚNG MONSTER
        soundIds["victory"] = soundPool?.load(context, R.raw.victory, 1) ?: 0  // 🆕 THÊM ÂM THANH CHIẾN THẮNG
        soundIds["game_over"] = soundPool?.load(context, R.raw.game_over, 1) ?: 0  // 🆕 THÊM ÂM THANH THUA
        soundIds["loose_health"] = soundPool?.load(context, R.raw.loose_health, 1) ?: 0  // 🆕 THÊM ÂM THANH MẤT MÁU

        println("🎵 SoundManager loaded sounds: ${soundIds.keys}")
    }


    fun playSound(soundName: String, customVolume: Float = -1.0f) {
        if (isMuted) {
            println("🔇 Sound muted, not playing: $soundName")
            return
        }

        soundIds[soundName]?.let { soundId ->
            if (soundId != 0) {
                // Sử dụng customVolume nếu được truyền, nếu không thì dùng volume mặc định
                val finalVolume = if (customVolume >= 0.0f) customVolume else volume
                soundPool?.play(soundId, finalVolume, finalVolume, 1, 0, 1.0f)
                println("🔊 Playing sound: $soundName (id: $soundId, volume: $finalVolume)")
            } else {
                println("❌ Sound ID is 0 for: $soundName")
            }
        } ?: println("❌ Sound not found: $soundName")
    }

    fun setMuted(muted: Boolean) {
        isMuted = muted
    }

    fun isMuted(): Boolean = isMuted

    // 🆕 SETTER CHO VOLUME SOUND EFFECTS
    fun setVolume(newVolume: Float) {
        volume = newVolume.coerceIn(0.0f, 1.0f) // Giới hạn từ 0.0 đến 1.0
    }

    // 🆕 GETTER CHO VOLUME SOUND EFFECTS
    fun getVolume(): Float = volume

    fun cleanup() {
        soundPool?.release()
        soundPool = null
    }
}