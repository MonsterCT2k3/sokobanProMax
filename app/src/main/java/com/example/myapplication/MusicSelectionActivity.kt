package com.example.myapplication

import android.content.SharedPreferences
import android.os.Bundle
import android.widget.Button
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.SeekBar
import android.widget.Switch
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.myapplication.managers.MusicManager
import com.example.myapplication.managers.SoundManager

class MusicSelectionActivity : AppCompatActivity() {
    private lateinit var musicManager: MusicManager
    private lateinit var soundManager: SoundManager
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var seekBarVolume: SeekBar
    private lateinit var seekBarSoundVolume: SeekBar
    private lateinit var switchMusic: Switch
    private lateinit var switchSoundEffects: Switch
    private lateinit var textVolumeValue: TextView
    private lateinit var textSoundVolumeValue: TextView
    private lateinit var radioGroupMusic: RadioGroup

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_music_selection)

        //Sử dụng Managers Singleton
        musicManager = MusicManager.getInstance() ?: MusicManager.getInstance(this)
        soundManager = SoundManager.getInstance(this)
        sharedPreferences = getSharedPreferences("audio_settings", MODE_PRIVATE)
        setupViews()
        loadSettings()
        setupListeners()
    }

    private fun setupViews(){
        // Music views
        switchMusic = findViewById(R.id.switchMusic)
        seekBarVolume = findViewById(R.id.seekBarVolume)
        textVolumeValue = findViewById(R.id.textVolumeValue)
        radioGroupMusic = findViewById(R.id.radioGroupMusic)

        // Sound effects views
        switchSoundEffects = findViewById(R.id.switchSoundEffects)
        seekBarSoundVolume = findViewById(R.id.seekBarSoundVolume)
        textSoundVolumeValue = findViewById(R.id.textSoundVolumeValue)
    }

    private fun loadSettings(){
        // Load music settings
        val musicEnabled = sharedPreferences.getBoolean("music_enabled", true)
        val musicVolume = sharedPreferences.getFloat("music_volume", 0.5f)
        val selectedMusic = sharedPreferences.getInt("selected_music", MusicManager.MUSIC_GAME_1)

        switchMusic.isChecked = musicEnabled
        seekBarVolume.progress = (musicVolume * 100).toInt()
        textVolumeValue.text = "${(musicVolume * 100).toInt()}%"

        when(selectedMusic){
            MusicManager.MUSIC_MENU -> findViewById<RadioButton>(R.id.radioMusic1).isChecked = true
            MusicManager.MUSIC_GAME_1 -> findViewById<RadioButton>(R.id.radioMusic2).isChecked = true
            MusicManager.MUSIC_GAME_2 -> findViewById<RadioButton>(R.id.radioMusic3).isChecked = true
            MusicManager.MUSIC_GAME_3 -> findViewById<RadioButton>(R.id.radioMusic4).isChecked = true
            MusicManager.MUSIC_GAME_4 -> findViewById<RadioButton>(R.id.radioMusic5).isChecked = true
            MusicManager.MUSIC_GAME_5 -> findViewById<RadioButton>(R.id.radioMusic6).isChecked = true
        }

        musicManager.setEnabled(musicEnabled)
        musicManager.setVolume(musicVolume)

        // Load sound effects settings
        val soundEnabled = sharedPreferences.getBoolean("sound_enabled", true)
        val soundVolume = sharedPreferences.getFloat("sound_volume", 0.5f)

        switchSoundEffects.isChecked = soundEnabled
        seekBarSoundVolume.progress = (soundVolume * 100).toInt()
        textSoundVolumeValue.text = "${(soundVolume * 100).toInt()}%"

        soundManager.setMuted(!soundEnabled)  // Muted = !enabled
        soundManager.setVolume(soundVolume)

        // Phát nhạc ngay để user có thể test volume (chỉ khi music enabled)
        if (musicEnabled) {
            musicManager.playMusic(selectedMusic, true)
        }
    }

    private fun setupListeners(){
        //volume seekbar listener - áp dụng volume ngay lập tức
        seekBarVolume.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener{
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean){
                val volume = progress / 100f
                textVolumeValue.text = "$progress%"
                musicManager.setVolume(volume)
                // Volume được áp dụng ngay lập tức, không cần làm gì thêm
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) {
                // Khi bắt đầu kéo, có thể show feedback
            }
            override fun onStopTrackingTouch(seekBar: SeekBar?) {
                // Khi kết thúc kéo, show toast xác nhận
                val progress = seekBarVolume.progress
                Toast.makeText(this@MusicSelectionActivity, "Âm lượng: ${progress}%", Toast.LENGTH_SHORT).show()
            }
        })

        // Radio group listener - tự động phát nhạc khi chọn
        radioGroupMusic.setOnCheckedChangeListener { _, checkedId ->
            if (switchMusic.isChecked) {
                val selectedMusic = getSelectedMusicFromId(checkedId)
                musicManager.stopMusic() // Dừng bài hiện tại
                musicManager.playMusic(selectedMusic, true) // Phát bài mới
                Toast.makeText(this, "Đang phát: ${getMusicName(selectedMusic)}", Toast.LENGTH_SHORT).show()
            }
        }

        // save button listener - quay về MenuActivity
        findViewById<Button>(R.id.btnSaveMusic).setOnClickListener {
            saveSettings()
            Toast.makeText(this, "Đã lưu cài đặt!", Toast.LENGTH_SHORT).show()
            finish() // Quay về activity trước đó (MenuActivity)
        }

        //music switch listener - bật/tắt nhạc ngay lập tức
        switchMusic.setOnCheckedChangeListener { _, isChecked ->
            musicManager.setEnabled(isChecked)
            if (isChecked) {
                // Bật nhạc - phát bài đang được chọn
                val selectedMusic = getSelectedMusic()
                musicManager.playMusic(selectedMusic, true)
                Toast.makeText(this, "Đã bật nhạc", Toast.LENGTH_SHORT).show()
            } else {
                // Tắt nhạc
                musicManager.stopMusic()
                Toast.makeText(this, "Đã tắt nhạc", Toast.LENGTH_SHORT).show()
            }
        }

        //sound effects volume seekbar listener - áp dụng volume ngay lập tức
        seekBarSoundVolume.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener{
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean){
                val volume = progress / 100f
                textSoundVolumeValue.text = "$progress%"
                soundManager.setVolume(volume)
                // Test sound để user nghe ngay
                if (switchSoundEffects.isChecked) {
                    soundManager.playSound("move")
                }
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) {
                // Khi bắt đầu kéo, có thể show feedback
            }
            override fun onStopTrackingTouch(seekBar: SeekBar?) {
                // Khi kết thúc kéo, show toast xác nhận
                val progress = seekBarSoundVolume.progress
                Toast.makeText(this@MusicSelectionActivity, "Âm lượng sound: ${progress}%", Toast.LENGTH_SHORT).show()
            }
        })

        //sound effects switch listener - bật/tắt sound effects ngay lập tức
        switchSoundEffects.setOnCheckedChangeListener { _, isChecked ->
            soundManager.setMuted(!isChecked)
            if (isChecked) {
                // Bật sound effects - phát test sound
                soundManager.playSound("move")
                Toast.makeText(this, "Đã bật sound effects", Toast.LENGTH_SHORT).show()
            } else {
                // Tắt sound effects
                Toast.makeText(this, "Đã tắt sound effects", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun getSelectedMusic(): Int{
        return when(radioGroupMusic.checkedRadioButtonId){
            R.id.radioMusic1 -> MusicManager.MUSIC_MENU
            R.id.radioMusic2 -> MusicManager.MUSIC_GAME_1
            R.id.radioMusic3 -> MusicManager.MUSIC_GAME_2
            R.id.radioMusic4 -> MusicManager.MUSIC_GAME_3
            R.id.radioMusic5 -> MusicManager.MUSIC_GAME_4
            R.id.radioMusic6 -> MusicManager.MUSIC_GAME_5
            else -> MusicManager.MUSIC_GAME_1
        }
    }

    private fun getSelectedMusicFromId(radioId: Int): Int{
        return when(radioId){
            R.id.radioMusic1 -> MusicManager.MUSIC_MENU
            R.id.radioMusic2 -> MusicManager.MUSIC_GAME_1
            R.id.radioMusic3 -> MusicManager.MUSIC_GAME_2
            R.id.radioMusic4 -> MusicManager.MUSIC_GAME_3
            R.id.radioMusic5 -> MusicManager.MUSIC_GAME_4
            R.id.radioMusic6 -> MusicManager.MUSIC_GAME_5
            else -> MusicManager.MUSIC_GAME_1
        }
    }

    private fun getMusicName(musicId: Int): String{
        return when(musicId){
            MusicManager.MUSIC_MENU -> "We will Rock You"
            MusicManager.MUSIC_GAME_1 -> "Shiverr"
            MusicManager.MUSIC_GAME_2 -> "Asphyxia"
            MusicManager.MUSIC_GAME_3 -> "Else Paris"
            MusicManager.MUSIC_GAME_4 -> "Shirfine"
            MusicManager.MUSIC_GAME_5 -> "Star Sky Remix"
            else -> "Unknown"
        }
    }

    private fun saveSettings(){
        val editor = sharedPreferences.edit()
        // Save music settings
        editor.putBoolean("music_enabled", switchMusic.isChecked)
        editor.putFloat("music_volume", seekBarVolume.progress / 100f)
        editor.putInt("selected_music", getSelectedMusic())
        // Save sound effects settings
        editor.putBoolean("sound_enabled", switchSoundEffects.isChecked)
        editor.putFloat("sound_volume", seekBarSoundVolume.progress / 100f)
        editor.apply()
    }

    override fun onPause() {
        super.onPause()
        // Tạm dừng nhạc khi app bị đưa vào background
        musicManager.pauseMusic()
    }

    override fun onResume() {
        super.onResume()
        // Tiếp tục phát nhạc nếu đã bật
        if (switchMusic.isChecked) {
            val selectedMusic = getSelectedMusic()
            musicManager.playMusic(selectedMusic, true)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        // KHÔNG gọi release() vì đây là Singleton, chỉ MenuActivity mới được release
        // musicManager.release()
    }
}