package com.example.myapplication

import android.content.Context
import android.content.Intent
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.util.AttributeSet
import android.util.Log
import androidx.appcompat.app.AlertDialog
import android.view.MotionEvent
import android.view.View
import com.example.myapplication.game.GameLogic
import com.example.myapplication.game.PlayerDirection
import com.example.myapplication.input.InputHandler
import com.example.myapplication.managers.HighScoreManager
import com.example.myapplication.managers.MusicManager
import com.example.myapplication.managers.SoundManager
import com.example.myapplication.VictoryActivity
import com.example.myapplication.controllers.AudioController
import com.example.myapplication.controllers.BulletController
import com.example.myapplication.controllers.UIManager
import com.example.myapplication.entities.AmmoPickup
import com.example.myapplication.entities.AmmoType
import com.example.myapplication.entities.BulletType
import com.example.myapplication.entities.LivesPickup
import com.example.myapplication.entities.Monster
import com.example.myapplication.entities.MonsterAIState
import com.example.myapplication.entities.MonsterType
import com.example.myapplication.managers.DialogManager
import com.example.myapplication.rendering.BackgroundManager
import com.example.myapplication.rendering.GameRenderer
import com.example.myapplication.systems.AmmoSystem
import com.example.myapplication.systems.BulletSystem
import com.example.myapplication.systems.LivesSystem
import com.example.myapplication.systems.MonsterSystem
import com.example.myapplication.systems.ParticleSystem
import kotlin.text.toInt

/**
 * 🎮 GameView - Main game view class
 * 
 * Đây là lớp chính quản lý toàn bộ game Sokoban.
 * Nó hoạt động như một coordinator, điều phối các component:
 * - GameLogic: Xử lý logic game (di chuyển, win condition)
 * - GameRenderer: Vẽ game board và UI
 * - BackgroundManager: Quản lý background animation
 * - InputHandler: Xử lý touch input và swipe gestures
 * 
 * @param context Android context
 * @param attrs XML attributes
 * @param defStyleAttr Default style attributes
 */
class GameView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr),
    GameLogic.GameStateListener,        // Lắng nghe thay đổi trạng thái game
    InputHandler.PlayerMoveListener {   // Lắng nghe input từ user

    // ===== VICTORY NAVIGATION CALLBACK =====
    private var victoryNavigationCallback: (() -> Unit)? = null

    // ===== CORE COMPONENTS =====
    // Mỗi component có nhiệm vụ riêng biệt, tách biệt trách nhiệm
    private val gameLogic = GameLogic()                    // 🎯 Xử lý logic game
    private val gameRenderer = GameRenderer(context)       // 🖼️ Vẽ game board và UI

    // 🏆 High score system
    private val highScoreManager = HighScoreManager(context)
    private var levelStartTime = 0L
    private var currentLevelBestTime: Long? = null

    // 🐛 FIX: Store custom level data for respawn after death and game over screen
    private var customLevelMapString: String? = null
    private var customLevelWidth: Int = 15
    private var customLevelHeight: Int = 15
    private var customLevelBoxCount: Int = 3
    private var customLevelMonsterData: List<Triple<Int, Int, String>>? = null
    private val backgroundManager = BackgroundManager(context) // 🎨 Quản lý background
    private val inputHandler = InputHandler()              // 👆 Xử lý touch input
    private val monsterSystem = MonsterSystem()            // 👾 Xử lý logic monster
    private val bulletSystem = BulletSystem()               // 🎯 Xử lý logic bullet
    private lateinit var musicManager: MusicManager
    private lateinit var soundManager: SoundManager
    private val ammoSystem = AmmoSystem()
    private val livesSystem = LivesSystem()
    private val particleSystem = ParticleSystem()

    // ===== NEW MANAGERS/CONTROLLERS =====
    private lateinit var dialogManager: DialogManager      // 🏆 Quản lý dialogs
    private lateinit var audioController: AudioController  // 🔊 Quản lý âm thanh
    private lateinit var bulletController: BulletController // 🎯 Quản lý logic đạn
    private lateinit var uiManager: UIManager              // 🎛️ Quản lý UI

    // ===== GAME THREAD MANAGEMENT =====
    // Game chạy trên thread riêng để không block UI thread
    private var gameThread: Thread? = null                 // Thread chạy game loop
    private var isGameRunning = false                      // Trạng thái game đang chạy
    private var targetFPS = 60                             // Mục tiêu 60 FPS
    private var frameTimeMillis = 1000L / targetFPS        // Thời gian mỗi frame (≈16.67ms)
    private var gameStateChanged = false                   // Flag báo cần redraw
    private var frameCount = 0                             // Đếm frame để tính FPS
    private var lastFPSTime = 0L                           // Thời gian lần cuối tính FPS
    
    // ===== ANIMATION =====
    private var animationTime = 0f                         // Thời gian để tính animation
    private var animationStartTime = 0L                    // 🆕 Thời gian bắt đầu (để tính relative time)
    private var lastUpdateTime = 0L                        // Thời gian lần cuối update animation

    // 🆕 LIVES SYSTEM
    private var lives = 3
    private val maxLives = 3

    // Survival Mode callbacks
    private var survivalOnLevelComplete: ((Long) -> Unit)? = null
    private var survivalOnPlayerDeath: (() -> Unit)? = null
    private var survivalGetTotalTime: (() -> Long)? = null  // 🆕 Callback để lấy tổng thời gian
    private var isSurvivalMode = false

    // 🆕 Prevent multiple death calls per collision (for Survival mode)
    private var isPlayerDead = false

    init {
        initGame()
    }

    /**
     * 🔧 Khởi tạo game
     * Setup các listener và controllers để các component có thể giao tiếp với nhau
     */
    private fun initGame() {
        soundManager = SoundManager.getInstance()!!
        musicManager = MusicManager.getInstance()!!

        // Khởi tạo controllers/managers mới
        dialogManager = DialogManager(context, soundManager)
        audioController = AudioController(context, musicManager, soundManager)
        bulletController = BulletController(gameLogic, gameRenderer, bulletSystem, soundManager)
        uiManager = UIManager(gameLogic, gameRenderer, soundManager, audioController, bulletController, width, height)

        // Setup listeners để tạo communication giữa các component
        gameLogic.setGameStateListener(this)        // GameView lắng nghe thay đổi từ GameLogic
        inputHandler.setPlayerMoveListener(this)    // GameView lắng nghe input từ InputHandler

        // 🏆 Setup goal effect callbacks
        gameLogic.onGoalReachedEffect = { row, col ->
            val (centerX, centerY) = gameRenderer.calculateTileCenter(gameLogic.getMap(), row, col)
            gameRenderer.addGoalReachedEffect(centerX, centerY)
            // 🔔 Phát âm thanh "ting" khi đẩy hộp vào đích
            soundManager.playSound("ting")
        }
        gameLogic.onGoalLeftEffect = { row, col ->
            val (centerX, centerY) = gameRenderer.calculateTileCenter(gameLogic.getMap(), row, col)
            gameRenderer.removeGoalReachedEffect(centerX, centerY)
        }

        audioController.loadAudioSettings()
        bulletController.resetAmmo()
    }


    private fun resetLives() {
        lives = 3
    }



    // ===== PUBLIC API METHODS =====
    // Các method public để Activity/Fragment có thể điều khiển game

    // 🆕 SETTER CHO VICTORY NAVIGATION CALLBACK
    fun setVictoryNavigationCallback(callback: () -> Unit) {
        victoryNavigationCallback = callback
    }

    // 🆕 GETTER CHO CURRENT LEVEL ID
    fun getCurrentLevelId(): Int {
        return gameLogic.getCurrentLevel()?.id ?: 1
    }

    fun getCurrentElapsedTime(): Long {
        return gameLogic.getLevelElapsedTime()
    }

    fun loadLevel(levelId: Int) {
        // 🐛 FIX: Clear custom level data khi load regular level
        customLevelMapString = null
        customLevelMonsterData = null

        gameLogic.loadLevel(levelId)
        // 🏆 Load kỷ lục cho level hiện tại
        currentLevelBestTime = highScoreManager.getBestHighScore(levelId)
        // ⭐ LOAD MONSTERS từ level data
        monsterSystem.clearMonsters()  // Xóa monsters cũ

        val level = gameLogic.getCurrentLevel()
        level?.monsters?.forEachIndexed { index, monsterData ->
            val monsterId = "monster_${levelId}_${index}"
            val monster = monsterSystem.createMonsterFromData(monsterData, monsterId)
            monsterSystem.addMonster(monster)

            println("🎮 Loaded monster: ${monsterId} type=${monsterData.type} at (${monsterData.startRow}, ${monsterData.startColumn})")
        }

        // 🆕 CLEAR PARTICLES khi load level mới
        particleSystem.clear()

        // 🆕 CLEAR GOAL REACHED EFFECTS khi load level mới
        gameRenderer.clearGoalReachedEffects()

        // 🆕 SPAWN AMMO PICKUPS (loại trừ vị trí player start)
        val playerStartPos = gameLogic.getCurrentLevel()?.getPlayerStartPosition()
        val excludePositions = if (playerStartPos != null) {
            // playerStartPos trả về (row, col), nên dùng luôn
            listOf(Pair(playerStartPos.first, playerStartPos.second)) // (row, col) format
        } else {
            emptyList()
        }
        ammoSystem.spawnRandomAmmo(gameLogic.getMap(), 3, excludePositions)

        // 🆕 RESET AMMO và LIVES mỗi level mới (khi vào màn chơi lần đầu)
        bulletController.resetAmmo()
        resetLives()

        // 🆕 SPAWN LIVES PICKUPS
        livesSystem.spawnRandomLives(gameLogic.getMap(), 1, excludePositions)

        // Safe zones đã được định nghĩa trực tiếp trong map với ký tự 'S'
        
        // DEBUG: In ra map để xem safe zones
        println("🛡️ GameView: Map after spawning safe zones:")
        gameLogic.getMap().forEachIndexed { row, chars ->
            println("Row $row: ${chars.joinToString("")}")
        }

        gameStateChanged = true
    }

    fun setBackgroundImage(resourceId: Int, scrollType: BackgroundManager.BackgroundScrollType = BackgroundManager.BackgroundScrollType.PARALLAX_HORIZONTAL) {
        backgroundManager.setBackgroundImage(resourceId, scrollType)
        gameStateChanged = true  // Báo cần redraw
    }

    fun startGame() {
        if (!isGameRunning) {
            isGameRunning = true
            isPlayerDead = false  // Reset death flag when starting game
            animationStartTime = System.currentTimeMillis() // 🆕 Ghi lại thời điểm bắt đầu
            levelStartTime = System.currentTimeMillis()     // 🏆 Bắt đầu đếm thời gian level
            gameThread = GameThread()   // Tạo thread mới
            gameThread?.start()         // Bắt đầu game loop
        }
    }

    fun stopGame() {
        isGameRunning = false           // Set flag để thread thoát loop
        gameThread?.interrupt()         // Interrupt thread
        try {
            gameThread?.join()          // Đợi thread kết thúc
        } catch (e: InterruptedException) {
            Thread.currentThread().interrupt()
        }
        gameThread = null               // Clean up
    }


    fun pauseGame() {
        isGameRunning = false
    }

    fun resumeGame() {
        if (!isGameRunning) {
            startGame()
        }
    }

    /**
     * 🔄 GameThread - Thread chạy game loop
     * 
     * Đây là "trái tim" của game, chạy liên tục với 60 FPS:
     * 1. Update game logic
     * 2. Check nếu cần redraw → gọi invalidate()
     * 3. Sleep để maintain 60 FPS
     */
    private inner class GameThread : Thread() {
        override fun run() {
            while (isGameRunning && !isInterrupted) {
                try {
                    val startTime = System.currentTimeMillis()
                    
                    // 1. Update game (animation, logic, etc.)
                    updateGame()
                    
                    // 2. Luôn redraw để animation mượt (đặc biệt cho shield animation)
                    post { invalidate() }        // Schedule onDraw() trên UI thread mỗi frame
                    gameStateChanged = false     // Reset flag

                    // 3. Sleep để maintain 60 FPS (16.67ms/frame)
                    val frameTime = System.currentTimeMillis() - startTime
                    if (frameTime < frameTimeMillis) {
                        sleep(frameTimeMillis - frameTime)  // Sleep phần thời gian còn lại
                    }
                } catch (e: InterruptedException) {
                    break  // Thread bị interrupt → thoát loop
                }
            }
        }
    }

    // ===== SURVIVAL MODE METHODS =====
    
    /**
     * 🏃 Enable/Disable Survival Mode
     */
    fun setSurvivalMode(enabled: Boolean) {
        isSurvivalMode = enabled
        println("🏃 Survival mode: $enabled")
    }

    /**
     * 🏃 Set callbacks for Survival Mode
     */
    fun setSurvivalCallbacks(
        onLevelComplete: (Long) -> Unit,
        onPlayerDeath: () -> Unit,
        getTotalTime: () -> Long
    ) {
        survivalOnLevelComplete = onLevelComplete
        survivalOnPlayerDeath = onPlayerDeath
        survivalGetTotalTime = getTotalTime
        println("🏃 Survival callbacks set")
    }

    /**
     * 🏃 Reset current level (for Survival mode when player loses a life)
     */
    fun resetCurrentLevel() {
        try {
            val currentLevelId = getCurrentLevelId()
            println("🏃 Survival: Starting reset of level $currentLevelId")

            loadLevel(currentLevelId)
            isPlayerDead = false  // Reset death flag
            startGame()

            println("🏃 Survival: Successfully reset level $currentLevelId")
        } catch (e: Exception) {
            println("🏃 Survival: ERROR in resetCurrentLevel: ${e.message}")
            e.printStackTrace()
        }
    }

    /**
     * 🏃 Get bullet controller (for Survival mode ammo management)
     */
    fun getBulletController(): BulletController {
        return bulletController
    }

    /**
     * 🔄 Update game mỗi frame
     * 
     * Method này được gọi 60 lần/giây từ GameThread:
     * 1. Tính FPS để debug
     * 2. Update animation time
     * 3. Update background animation
     */
    private fun updateGame() {
        // ===== DEBUG FPS =====
        frameCount++  // Đếm số frame
        val currentTime = System.currentTimeMillis()

        // Mỗi giây in ra FPS để debug
        if (currentTime - lastFPSTime >= 1000) {
            println("🎮 Game FPS: $frameCount")  // Should be ~60
            frameCount = 0
            lastFPSTime = currentTime
        }

        val rawDeltaTime = if(lastUpdateTime==0L){
            0.016f
        }else{
            (currentTime - lastUpdateTime).toFloat() / 1000f
        }
        lastUpdateTime = currentTime

        // Đảm bảo deltaTime hợp lý
        val deltaTime = rawDeltaTime.coerceIn(0.01f, 0.1f)

        // ===== UPDATE ANIMATION =====
        animationTime = currentTime.toFloat()  // Thời gian cho background animation

        // update monsters
        val (playerX, playerY) = gameLogic.getPlayerPosition()
        monsterSystem.updateMonsters(deltaTime, playerX, playerY, gameLogic.getMap())

        //check collision between player and monsters
        if(monsterSystem.checkPlayerCollision(playerX, playerY) && !isPlayerDead){
            isPlayerDead = true
            println("💀 COLLISION DETECTED: Player died")
            onPlayerDied()
        }

        // ===== CHECK AMMO COLLECTION =====
        val collectedType = ammoSystem.checkAmmoCollection(playerX, playerY)
        if (collectedType != null) {
            // Update ammo based on type
            when (collectedType) {
                AmmoType.NORMAL -> {
                    bulletController.normalAmmo = minOf(bulletController.normalAmmo + 1, bulletController.maxAmmoPerType)
                }
                AmmoType.PIERCE -> {
                    bulletController.pierceAmmo = minOf(bulletController.pierceAmmo + 1, bulletController.maxAmmoPerType)
                    println("🔫 Collected pierce ammo!")
                }
                AmmoType.STUN -> {
                    bulletController.stunAmmo = minOf(bulletController.stunAmmo + 1, bulletController.maxAmmoPerType)
                    println("🔫 Collected stun ammo!")
                }
            }
            soundManager.playSound("ammo_pickup")
        }

        // ===== CHECK LIVES COLLECTION =====
        println("🩸 Checking lives collection at (row=$playerX, col=$playerY)")
        val collectedLives = livesSystem.checkLivesCollection(playerX, playerY)
        if (collectedLives) {
            if (isSurvivalMode) {
                // 🏃 SURVIVAL MODE: Cập nhật lives trong SurvivalManager
                val session = survivalGetTotalTime?.let { 
                    // Sử dụng callback để lấy session thông qua SurvivalManager
                    com.example.myapplication.managers.SurvivalManager.getCurrentSession()
                }
                if (session != null && session.lives < maxLives) {
                    session.lives++
                    println("🏃 Survival Lives increased to ${session.lives}/$maxLives")
                } else {
                    println("🏃 Survival Lives already at max ($maxLives)")
                }
            } else {
                // 🎯 CLASSIC MODE: Cập nhật lives trong GameView
                if (lives < maxLives) {
                    lives++
                    println("❤️ Lives increased to $lives/$maxLives")
                } else {
                    println("❤️ Lives already at max ($maxLives)")
                }
            }
            soundManager.playSound("victory")  // Hoặc sound khác cho lives pickup
        }

        // Update bullets
        // Update vị trí bullets và cleanup
        val tileSize = gameRenderer.calculateTileSize(gameLogic.getMap()).toFloat()
        val (offsetX, offsetY) = gameRenderer.calculateBoardOffset(gameLogic.getMap())
        bulletSystem.updateBullets(deltaTime, width.toFloat(), height.toFloat(), gameLogic.getMap(), tileSize, offsetX, offsetY)

        // ===== CHECK BULLET COLLISIONS =====
        // Kiểm tra bullets có chạm monsters không
        val monsterIds = monsterSystem.getActiveMonsters().map { it.id }
        val monsterPositions = monsterSystem.getActiveMonsters().map { monster: Monster ->
            // Convert grid coordinates to screen coordinates
            val screenX = offsetX + monster.currentY * tileSize  // currentY là column
            val screenY = offsetY + monster.currentX * tileSize  // currentX là row
            Pair(screenX, screenY)
        }

        val collisions = bulletSystem.checkCollisions(monsterPositions, monsterIds)

        // DEBUG: Log monster positions
        println("🎯 Checking ${monsterPositions.size} monsters for collisions")
        monsterPositions.forEachIndexed { index: Int, pair: Pair<Float, Float> ->
            val (x, y) = pair
            println("👹 Monster $index at screen pos (${x.toInt()}, ${y.toInt()})")
        }

        // DEBUG: Log collisions found
        println("💥 Found ${collisions.size} collisions")
        collisions.forEach { (bullet, monsterIndex) ->
            println("🎯 Processing collision: ${bullet.bulletType} bullet ${bullet.id} hit monster $monsterIndex")

            when (bullet.bulletType) {
                BulletType.NORMAL, BulletType.PIERCE -> {
                    // NORMAL/PIERCE: XÓA MONSTER
                    monsterSystem.removeMonster(monsterIndex)

                    // Tạo explosion
                    val monsterPos = monsterPositions[monsterIndex]
                    particleSystem.createExplosion(monsterPos.first, monsterPos.second)

                    // Phát âm thanh
                    soundManager.playSound("monster_hit")
                }
                BulletType.STUN -> {
                    // STUN: CHOÁNG VÁNG MONSTER 5 GIÂY
                    monsterSystem.stunMonster(monsterIndex, 5.0f)

                    // Tạo hiệu ứng stun (có thể tạo particle khác hoặc effect khác)
                    val monsterPos = monsterPositions[monsterIndex]

                    // Phát âm thanh khác cho stun
                    soundManager.playSound("monster_hit")  // Có thể dùng sound khác sau
                }
            }
        }

        // 🆕 UPDATE PARTICLES
        particleSystem.update(deltaTime)
        // Debug: log particle count mỗi frame
        if (particleSystem.getParticleCount() > 0) {
            println("🎆 Active particles: ${particleSystem.getParticleCount()}")
        }

        val bulletsHitWall = bulletSystem.getBulletsHitWall()
        if (bulletsHitWall.isNotEmpty()) {
            soundManager.playSound("bullet_wall")
            println("💥 ${bulletsHitWall.size} bullets hit wall!")
        }

        // Update background animation và check cần redraw không
        if (backgroundManager.updateAnimation()) {
            gameStateChanged = true  // Background có animation → cần redraw
        }

        // Monsters cũng cần redraw
        if (monsterSystem.getActiveMonsters().isNotEmpty()) {
            gameStateChanged = true
        }

        // 🛡️ Shield animation cần redraw liên tục khi player ở safe zone
        if (gameLogic.isPlayerOnSafeZone()) {
            gameStateChanged = true  // Shield có animation → cần redraw
        }
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)

        // Thông báo kích thước mới cho các component
        gameRenderer.setScreenSize(w, h)       // Để tính toán tile size và layout
        backgroundManager.setScreenSize(w, h)  // Để scale background cho phù hợp
        uiManager.updateScreenSize(w, h)       // Để tính toán touch detection cho buttons
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        // 1. 🎨 Vẽ background trước (nền)
        backgroundManager.drawBackground(canvas, animationTime)

        // 2. 🎮 Vẽ game board (tiles: wall, box, player, goal)
        //    Chỉ vẽ nếu đã load level
        if (!gameLogic.isMapEmpty()) {
            val monsters = monsterSystem.getActiveMonsters()
            val (playerRow, playerCol) = gameLogic.getPlayerPosition()
            gameRenderer.drawGameBoard(canvas, gameLogic.getMap(), playerRow, playerCol, gameLogic.getPlayerDirection(), monsters, gameLogic.getSafeZonePositions())
        }

        // 🆕 DRAW AMMO PICKUPS
        if (!gameLogic.isMapEmpty()) {
            val tileSize = gameRenderer.calculateTileSize(gameLogic.getMap()).toFloat()
            val (offsetX, offsetY) = gameRenderer.calculateBoardOffset(gameLogic.getMap())
            val ammoPickups = ammoSystem.getActiveAmmoPickups()
            gameRenderer.drawAmmoPickups(canvas, ammoPickups, tileSize, offsetX, offsetY)

            // 🆕 DRAW LIVES PICKUPS
            val livesPickups = livesSystem.getActiveLivesPickups()
            gameRenderer.drawLivesPickups(canvas, livesPickups, tileSize, offsetX, offsetY)
        }

        // 🆕 DRAW MAIN UI (lives + goal counter + timer)
        val currentGoalCount = gameLogic.getBoxesInGoal()
        val totalGoalCount = gameLogic.getGoalPositions().size
        
        // Tính thời gian và lives hiển thị dựa trên chế độ
        val (displayTime, displayLives) = if (isSurvivalMode) {
            // Survival Mode: Hiển thị tổng thời gian + lives từ SurvivalManager
            val sessionTotalTime = survivalGetTotalTime?.invoke() ?: 0L
            val currentLevelTime = System.currentTimeMillis() - levelStartTime
            val survivalLives = com.example.myapplication.managers.SurvivalManager.getCurrentSession()?.lives ?: lives
            Pair(sessionTotalTime + currentLevelTime, survivalLives)
        } else {
            // Classic Mode: Chỉ hiển thị thời gian level hiện tại + lives từ GameView
            Pair(System.currentTimeMillis() - levelStartTime, lives)
        }
        
        gameRenderer.drawMainUI(canvas, displayLives, maxLives, currentGoalCount, totalGoalCount, displayTime, isSurvivalMode)

        // 🎛️ Vẽ nút toggle phía trên map
        val uiState = uiManager.getUIState()
        gameRenderer.drawToggleButtons(canvas, gameLogic.getMap(), uiState.musicEnabled, uiState.soundEnabled)

        // Vẽ bullets
        val activeBullets = bulletSystem.getActiveBullets()
        gameRenderer.drawBullets(canvas, activeBullets)

        // 🆕 DRAW PARTICLES (sau khi vẽ game objects)
        particleSystem.draw(canvas)

        // 🏆 DRAW GOAL REACHED EFFECTS (sao chổi vàng)
        gameRenderer.drawGoalReachedEffects(canvas, System.currentTimeMillis())

        // 3. 🖼️ Vẽ UI elements cuối cùng (trên cùng)
        //    Title, instructions, score, etc.
        val currentLevelId = gameLogic.getCurrentLevel()?.id ?: 1
        gameRenderer.drawGameUI(canvas, currentLevelId)

        // 🆕 DRAW BULLET TYPE BUTTONS (ở phía dưới)
        gameRenderer.drawBulletTypeButtons(canvas, uiState.normalAmmo, uiState.pierceAmmo, uiState.stunAmmo,
                                         width.toFloat(), height.toFloat(), uiState.currentBulletType, uiState.buildMode)

        // 🛡️ DRAW PLAYER SHIELD (TRÊN CÙNG - cuối cùng để không bị che)
        if (!gameLogic.isMapEmpty()) {
            val (playerRow, playerCol) = gameLogic.getPlayerPosition()
            // 🆕 Tính RELATIVE TIME (thời gian kể từ khi bắt đầu game, tính bằng milliseconds)
            val relativeTime = (System.currentTimeMillis() - animationStartTime).toFloat()
            gameRenderer.drawPlayerShield(canvas, playerRow, playerCol, gameLogic, relativeTime)
        }
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        // 🔄 DELEGATE CHO UI MANAGER XỬ LÝ TRƯỚC
        val uiHandled = uiManager.handleUITouch(event)
        if (uiHandled) {
            gameStateChanged = true  // Trigger redraw cho UI changes
            return true
        }

        // 🔄 DELEGATE CHO INPUT HANDLER
        val inputHandled = inputHandler.handleTouchEvent(event)

        // Nếu InputHandler đã xử lý (swipe), return luôn
        if (inputHandled) {
            return true
        }

        // Nếu InputHandler không xử lý (tap), thì xử lý game action
        when (event.action) {
            MotionEvent.ACTION_UP -> {
                uiManager.handleGameAction()
                return true
            }
        }
        return super.onTouchEvent(event)
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        startGame()  // Bắt đầu game loop
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        stopGame()   // Dừng game loop và clean up
        livesSystem.clearLives()  // 🆕 Cleanup lives system
        // KHÔNG cleanup SoundManager vì đây là Singleton dùng chung cho tất cả activities
        // soundManager.cleanup()
    }

    // ===== CALLBACK IMPLEMENTATIONS =====
    
    /**
     * 🎯 GameLogic.GameStateListener - Lắng nghe thay đổi từ GameLogic
     */

    /**
     * 🔄 Được gọi khi game state thay đổi
     * VD: Player di chuyển, box được đẩy, level reset
     */
    override fun onGameStateChanged() {
        gameStateChanged = true // Báo cần redraw
        
        if (!isGameRunning) {
            post { invalidate() }
        }
        // Nếu game thread đang chạy → nó sẽ tự động redraw
    }

    override fun onGameWon() {
        isGameRunning = false  // Dừng game loop

        // 🏆 Tính thời gian hoàn thành level
        val levelEndTime = System.currentTimeMillis()
        val levelTime = levelEndTime - levelStartTime
        val currentLevelId = gameLogic.getCurrentLevel()?.id ?: 1

        if (isSurvivalMode) {
            // 🏃 SURVIVAL MODE: Gọi callback thay vì hiển thị victory screen
            println("🏃 Survival: Level $currentLevelId completed in ${levelTime}ms")
            survivalOnLevelComplete?.invoke(levelTime)
        } else if (customLevelMonsterData != null) {
            // 🎨 CUSTOM LEVEL: Mở CustomVictoryActivity
            println("🎨 Custom level completed in ${levelTime}ms")

            // 🆕 PHÁT ÂM THANH CHIẾN THẮNG
            soundManager.playSound("victory")

            post {
                // 🔔 Thông báo cho activity rằng sắp chuyển sang CustomVictoryActivity
                victoryNavigationCallback?.invoke()

                // 🎉 MỞ MÀN CUSTOM VICTORY SCREEN
                val intent = Intent(context, com.example.myapplication.CustomVictoryActivity::class.java).apply {
                    putExtra("completion_time", levelTime)
                    putExtra("map_size", "${customLevelWidth}x${customLevelHeight}")
                    putExtra("box_count", customLevelBoxCount)
                    putExtra("monster_count", customLevelMonsterData?.size ?: 0)
                }
                context.startActivity(intent)

                // Kết thúc activity hiện tại
                (context as? android.app.Activity)?.finish()
            }
        } else {
            // 🎯 CLASSIC MODE: Logic cũ - lưu kỷ lục và mở victory screen
            val isNewRecord = highScoreManager.isNewHighScore(currentLevelId, levelTime)

            // Lưu kỷ lục nếu là thời gian tốt hơn
            highScoreManager.saveHighScore(currentLevelId, levelTime)

            // 🆕 LƯU PROGRESS: Cập nhật level đã hoàn thành
            val sharedPreferences = context.getSharedPreferences("game_progress", Context.MODE_PRIVATE)
            val lastCompletedLevel = sharedPreferences.getInt("last_completed_level", 0)

            // Chỉ cập nhật nếu level hiện tại cao hơn level đã hoàn thành trước đó
            if (currentLevelId > lastCompletedLevel) {
                sharedPreferences.edit().putInt("last_completed_level", currentLevelId).apply()
                Log.d("GameView", "Progress updated: completed level $currentLevelId")
            }

            // 🆕 PHÁT ÂM THANH CHIẾN THẮNG
            soundManager.playSound("victory")

            post {
                // 🔔 Thông báo cho activity rằng sắp chuyển sang VictoryActivity
                victoryNavigationCallback?.invoke()

                // 🎉 MỞ MÀN VICTORY SCREEN với BXH
                val intent = Intent(context, VictoryActivity::class.java).apply {
                    putExtra("level_id", currentLevelId)
                    putExtra("your_time", levelTime)
                    putExtra("is_new_record", isNewRecord)
                }
                context.startActivity(intent)

                // Kết thúc activity hiện tại
                (context as? android.app.Activity)?.finish()
            }
        }
    }

    override fun onPlayerMove(dx: Int, dy: Int) {
        var moved = gameLogic.movePlayer(dx, dy)  // Delegate cho GameLogic xử lý
        if (moved) {
            // 🆕 DI CHUYỂN THÀNH CÔNG - Phát âm thanh di chuyển
            soundManager.playSound("move")
        } else {
            // 🆕 ĐẬP VÀO TƯỜNG - Phát âm thanh bump
            soundManager.playSound("bump_wall")
        }
    }

    private fun onPlayerDied() {
        if (isSurvivalMode) {
            // 🏃 SURVIVAL MODE: Gọi callback để SurvivalManager xử lý
            println("💀 Survival: Player died")
            
            // Phát âm thanh mất mạng (SurvivalManager sẽ quyết định game over hay không)
            soundManager.playSound("loose_health")
            
            // Reset monsters, bullets, particles ngay lập tức để tránh spam death
            resetGameElementsAfterDeath()
            
            // Gọi callback để SurvivalManager xử lý lives
            survivalOnPlayerDeath?.invoke()
        } else {
            // 🎯 CLASSIC MODE: Logic cũ với lives system riêng
            lives--  // Giảm 1 mạng

            if (lives <= 0) {
                // HẾT MẠNG - GAME OVER
                isGameRunning = false
                soundManager.playSound("game_over")

                // 🐛 FIX: Check if custom level and handle differently
                if (customLevelMonsterData != null) {
                    // CUSTOM LEVEL: Navigate to custom game over screen
                    post {
                        val intent = Intent(context, com.example.myapplication.CustomGameOverActivity::class.java)
                        intent.putExtra("map_size", "${customLevelWidth}x${customLevelHeight}")
                        intent.putExtra("box_count", customLevelBoxCount)
                        intent.putExtra("monster_count", customLevelMonsterData?.size ?: 0)
                        intent.putExtra("failure_reason", "No lives remaining")
                        // Pass custom level data for "try again"
                        intent.putExtra("customLevelMap", customLevelMapString)
                        intent.putExtra("customLevelWidth", customLevelWidth)
                        intent.putExtra("customLevelHeight", customLevelHeight)
                        intent.putExtra("customBoxCount", customLevelBoxCount)
                        intent.putExtra("customMonsterData", ArrayList(customLevelMonsterData?.toMutableList() ?: mutableListOf()))
                        context.startActivity(intent)
                    }
                } else {
                    // REGULAR LEVEL: Show lose dialog
                    post {
                        dialogManager.showLoseDialog(gameLogic, { levelId ->
                            loadLevel(levelId)
                            startGame()
                        })
                    }
                }
            } else {
                // VẪN CÒN MẠNG - CHỈ TRỪ MẠNG, TIẾP TỤC CHƠI
                soundManager.playSound("loose_health")
                println("💔 Lost a life! Lives remaining: $lives/$maxLives")

                // Reset game elements và death flag
                resetGameElementsAfterDeath()
                
                // Thông báo game state changed để redraw
                gameStateChanged = true
            }
        }
    }

    /**
     * 🎨 Load custom level từ Customize mode
     */
    fun loadCustomLevelData(mapString: String, width: Int, height: Int, boxCount: Int, monsterData: List<Triple<Int, Int, String>>) {
        println("🎨 Loading custom level data: ${width}x${height}, $boxCount boxes, ${monsterData.size} monsters")

        // 🐛 FIX: Store all custom level data for respawn after death and game over screen
        customLevelMapString = mapString
        customLevelWidth = width
        customLevelHeight = height
        customLevelBoxCount = boxCount
        customLevelMonsterData = monsterData.toList()

        // Load level từ custom data
        gameLogic.loadCustomLevelData(mapString, width, height, boxCount)

        // Load kỷ lục (custom level không có kỷ lục)
        currentLevelBestTime = null

        // Load monsters từ custom data
        monsterSystem.clearMonsters()
        for ((x, y, type) in monsterData) {
            val monsterType = when (type) {
                "PATROL" -> MonsterType.PATROL
                "BOUNCE" -> MonsterType.BOUNCE
                else -> MonsterType.PATROL
            }
            val monsterId = "custom_monster_${x}_${y}"

            // Create monster with appropriate AI state
            val aiState = when (monsterType) {
                MonsterType.PATROL -> {
                    MonsterAIState.PatrolState(
                        startPosition = Pair(x, y),
                        currentDirection = Pair(0, 1) // Default to moving down
                    )
                }
                MonsterType.BOUNCE -> {
                    MonsterAIState.BounceState(
                        currentDirection = Pair(0, 1) // Default to moving down
                    )
                }
                else -> {
                    MonsterAIState.PatrolState(
                        startPosition = Pair(x, y),
                        currentDirection = Pair(0, 1)
                    )
                }
            }

            val monster = Monster(
                id = monsterId,
                type = monsterType,
                currentX = x.toFloat(),
                currentY = y.toFloat(),
                targetX = x,
                targetY = y,
                aiState = aiState
            )

            monsterSystem.addMonster(monster)
        }

        // Spawn pickups từ map data (custom levels có pickups embedded)
        ammoSystem.clearAmmoPickups()
        livesSystem.clearLivesPickups()

        val tempMap = gameLogic.getMap()
        for (y in tempMap.indices) {
            for (x in tempMap[y].indices) {
                when (tempMap[y][x]) {
                    'N' -> {
                        val ammo = AmmoPickup(
                            id = "custom_ammo_N_${x}_${y}",
                            gridX = x,
                            gridY = y,
                            ammoType = AmmoType.NORMAL
                        )
                        ammoSystem.addAmmoPickup(ammo)
                    }
                    'P' -> {
                        val ammo = AmmoPickup(
                            id = "custom_ammo_P_${x}_${y}",
                            gridX = x,
                            gridY = y,
                            ammoType =AmmoType.PIERCE
                        )
                        ammoSystem.addAmmoPickup(ammo)
                    }
                    'S' -> {
                        val ammo = AmmoPickup(
                            id = "custom_ammo_S_${x}_${y}",
                            gridX = x,
                            gridY = y,
                            ammoType = AmmoType.STUN
                        )
                        ammoSystem.addAmmoPickup(ammo)
                    }
                    'L' -> {
                        val lives = LivesPickup(
                            id = "custom_lives_${x}_${y}",
                            gridX = x,
                            gridY = y
                        )
                        livesSystem.addLivesPickup(lives)
                    }
                }
            }
        }

        // Reset timers và start game
        animationTime = 0f
        animationStartTime = System.currentTimeMillis()
        lastUpdateTime = System.currentTimeMillis()

        // Reset game state
        gameStateChanged = true

        // Reset bullet controller cho custom level
        bulletController.resetAmmo()

        // Clear effects
        gameRenderer.clearGoalReachedEffects()
        particleSystem.clear()

        // Reset lives cho Classic mode
        if (!isSurvivalMode) {
            lives = 3
        }

        println("🎨 Custom level loaded successfully")
    }

    /**
     * 🔄 Reset game elements sau khi player chết (dùng chung cho cả 2 mode)
     */
    private fun resetGameElementsAfterDeath() {
        // Reset monsters về vị trí ban đầu (để tránh bị spawn trap)
        monsterSystem.clearMonsters()

        // 🐛 FIX: Handle custom levels vs regular levels differently
        if (customLevelMonsterData != null) {
            // CUSTOM LEVEL: Respawn từ stored monster data
            println("🔄 Custom level: Respawning ${customLevelMonsterData!!.size} monsters")
            for ((x, y, type) in customLevelMonsterData!!) {
                val monsterType = when (type) {
                    "PATROL" -> MonsterType.PATROL
                    "BOUNCE" -> MonsterType.BOUNCE
                    else -> MonsterType.PATROL
                }
                val monsterId = "custom_monster_${x}_${y}"

                // Create monster with appropriate AI state
                val aiState = when (monsterType) {
                    MonsterType.PATROL -> {
                        MonsterAIState.PatrolState(
                            startPosition = Pair(x, y),
                            currentDirection = Pair(0, 1)
                        )
                    }
                    MonsterType.BOUNCE -> {
                        MonsterAIState.BounceState(
                            currentDirection = Pair(0, 1)
                        )
                    }
                    else -> {
                        MonsterAIState.PatrolState(
                            startPosition = Pair(x, y),
                            currentDirection = Pair(0, 1)
                        )
                    }
                }

                val monster = Monster(
                    id = monsterId,
                    type = monsterType,
                    currentX = x.toFloat(),
                    currentY = y.toFloat(),
                    targetX = x,
                    targetY = y,
                    aiState = aiState
                )

                monsterSystem.addMonster(monster)
            }
        } else {
            // REGULAR LEVEL: Respawn từ level data
            val level = gameLogic.getCurrentLevel()
            level?.monsters?.forEachIndexed { index, monsterData ->
                val monsterId = "monster_${level.id}_${index}"
                val monster = monsterSystem.createMonsterFromData(monsterData, monsterId)
                monsterSystem.addMonster(monster)
            }
        }

        // Reset bullets và particles
        bulletSystem.clearBullets()
        particleSystem.clear()

        // Reset death flag để có thể chết lần nữa
        isPlayerDead = false

        println("🔄 Game elements reset after death")
    }
}
