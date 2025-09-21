package com.example.myapplication.managers

import android.content.Context
import android.media.MediaPlayer
import android.util.Log
import com.example.myapplication.R


class MusicManager private constructor(private val context: Context){
    private var mediaPlayer: MediaPlayer? = null
    private var currentMusicResource: Int = -1
    private var isEnabled: Boolean = true
    private var volume: Float = 0.5f

    companion object{
        const val MUSIC_MENU = 1
        const val MUSIC_GAME_1 = 2
        const val MUSIC_GAME_2 = 3
        const val MUSIC_GAME_3 = 4
        const val MUSIC_GAME_4 = 5
        const val MUSIC_GAME_5 = 6

        @Volatile
        private var INSTANCE: MusicManager? = null

        fun getInstance(context: Context): MusicManager {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: MusicManager(context.applicationContext).also { INSTANCE = it }
            }
        }

        fun getInstance(): MusicManager? {
            return INSTANCE
        }
    }

    private val musicResources = mapOf(
        MUSIC_MENU to R.raw.we_will_rock_you,
        MUSIC_GAME_1 to R.raw.shiverr,
        MUSIC_GAME_2 to R.raw.asphyxia,
        MUSIC_GAME_3 to R.raw.else_paris,
        MUSIC_GAME_4 to R.raw.shirfine,
        MUSIC_GAME_5 to R.raw.star_sky_remix
    )

    fun playMusic(musicId: Int, loop: Boolean = true) {
        if(!isEnabled) return
        val resourceId = musicResources[musicId] ?: return
        if(currentMusicResource == resourceId&& mediaPlayer?.isPlaying == true) return
        stopMusic()
        try {
            mediaPlayer = MediaPlayer.create(context, resourceId)
            mediaPlayer?.apply {
                isLooping = loop
                setVolume(volume, volume)
                start()
                currentMusicResource = resourceId
                Log.d("MusicManager", "Playing music resource: $resourceId")
            }
        }catch (e: Exception) {
            Log.e("MusicManager", "Error playing music resource: $resourceId", e)
        }
    }

    fun stopMusic(){
        mediaPlayer?.apply{
            if(isPlaying) stop()
            release()
        }
        mediaPlayer = null
        currentMusicResource = -1
    }

    fun pauseMusic(){
        mediaPlayer?.pause()
    }

    fun resumeMusic(){
        if(isEnabled) mediaPlayer?.start()
    }

    fun setVolume(newVolume: Float){
        volume = newVolume.coerceIn(0f, 1f)
        mediaPlayer?.setVolume(volume, volume)
    }

    fun setEnabled(enabled: Boolean){
        isEnabled = enabled
        if(!isEnabled) stopMusic()
    }

    fun isEnabled(): Boolean = isEnabled
    fun getVolume(): Float = volume
    fun isPlaying(): Boolean = mediaPlayer?.isPlaying == true

    fun release() {
        stopMusic()
    }
}