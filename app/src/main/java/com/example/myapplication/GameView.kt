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

    // ===== CORE COMPONENTS =====
    // Mỗi component có nhiệm vụ riêng biệt, tách biệt trách nhiệm
    private val gameLogic = GameLogic()                    // 🎯 Xử lý logic game
    private val gameRenderer = GameRenderer(context)       // 🖼️ Vẽ game board và UI

    // 🏆 High score system
    private val highScoreManager = HighScoreManager(context)
    private var levelStartTime = 0L
    private var currentLevelBestTime: Long? = null
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

        audioController.loadAudioSettings()
        bulletController.resetAmmo()
    }


    private fun resetLives() {
        lives = 3
    }


    // 🆕 METHOD SPAWN SAFE ZONES TRÊN MAP
    private fun spawnSafeZones(map: Array<CharArray>, count: Int, excludePositions: List<Pair<Int, Int>> = emptyList()) {
        val validPositions = mutableListOf<Pair<Int, Int>>()

        // Tìm tất cả vị trí hợp lệ (không phải tường, không phải hộp, không phải goal, không phải vị trí loại trừ)
        for (row in map.indices) {
            for (col in map[row].indices) {
                val position = Pair(row, col)  // (row, col) để match với GameLogic
                val cell = map[row][col]
                if (cell == '.' && position !in excludePositions) {  // Chỉ trên ô trống, không phải goal, box, wall
                    validPositions.add(position)
                }
            }
        }

        // Chọn ngẫu nhiên các vị trí
        validPositions.shuffle()
        val selectedPositions = validPositions.take(count.coerceAtMost(validPositions.size))

        // Đặt 'S' tại các vị trí đã chọn
        for ((row, col) in selectedPositions) {
            map[row][col] = 'S'  // row là index đầu tiên, col là index thứ hai
            println("🛡️ GameView: Spawned safe zone at (row=$row, col=$col), char='S'")
        }

        println("✅ GameView: Spawned ${selectedPositions.size} safe zones")
    }

    // ===== PUBLIC API METHODS =====
    // Các method public để Activity/Fragment có thể điều khiển game

    // 🆕 GETTER CHO CURRENT LEVEL ID
    fun getCurrentLevelId(): Int {
        return gameLogic.getCurrentLevel()?.id ?: 1
    }

    fun loadLevel(levelId: Int) {
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

        // 🆕 SPAWN SAFE ZONES (ô 'S' - chỉ player đi vào được)
        println("🛡️ GameView: About to spawn safe zones...")
        spawnSafeZones(gameLogic.getMap(), 2, excludePositions)
        
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
        if(monsterSystem.checkPlayerCollision(playerX, playerY)){
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
            if (lives < maxLives) {
                lives++
                println("❤️ Lives increased to $lives/$maxLives")
            } else {
                println("❤️ Lives already at max ($maxLives)")
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
        val monsterPositions = monsterSystem.getActiveMonsters().map { monster: com.example.myapplication.entities.Monster ->
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
            gameRenderer.drawGameBoard(canvas, gameLogic.getMap(), playerRow, playerCol, gameLogic.getPlayerDirection(), monsters)
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
        gameRenderer.drawMainUI(canvas, lives, maxLives, 0, 0, System.currentTimeMillis() - levelStartTime)

        // 🎛️ Vẽ nút toggle phía trên map
        val uiState = uiManager.getUIState()
        gameRenderer.drawToggleButtons(canvas, gameLogic.getMap(), uiState.musicEnabled, uiState.soundEnabled)

        // Vẽ bullets
        val activeBullets = bulletSystem.getActiveBullets()
        gameRenderer.drawBullets(canvas, activeBullets)

        // 🆕 DRAW PARTICLES (sau khi vẽ game objects)
        particleSystem.draw(canvas)
        
        // 3. 🖼️ Vẽ UI elements cuối cùng (trên cùng)
        //    Title, instructions, score, etc.
        gameRenderer.drawGameUI(canvas)

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

        // 🏆 LƯU KỶ LỤC: Tính thời gian hoàn thành level và lưu kỷ lục
        val levelEndTime = System.currentTimeMillis()
        val levelTime = levelEndTime - levelStartTime
        val currentLevelId = gameLogic.getCurrentLevel()?.id ?: 1
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
        lives--  // Giảm 1 mạng

        if (lives <= 0) {
            // HẾT MẠNG - GAME OVER
            isGameRunning = false
            soundManager.playSound("game_over")
            post {
                dialogManager.showLoseDialog(gameLogic, { levelId ->
                    loadLevel(levelId)
                    startGame()
                })
            }
        } else {
            // VẪN CÒN MẠNG - CHỈ TRỪ MẠNG, TIẾP TỤC CHƠI TỪ VỊ TRÍ HIỆN TẠI
            soundManager.playSound("loose_health")  // Hoặc sound khác cho mất mạng
            println("💔 Lost a life! Lives remaining: $lives/$maxLives")

            // Reset monsters về vị trí ban đầu (để tránh bị spawn trap)
            monsterSystem.clearMonsters()
            val level = gameLogic.getCurrentLevel()
            level?.monsters?.forEachIndexed { index, monsterData ->
                val monsterId = "monster_${level.id}_${index}"
                val monster = monsterSystem.createMonsterFromData(monsterData, monsterId)
                monsterSystem.addMonster(monster)
            }

            // Reset bullets và particles
            bulletSystem.clearBullets()
            particleSystem.clear()

            // Thông báo game state changed để redraw
            gameStateChanged = true
        }
    }
}
