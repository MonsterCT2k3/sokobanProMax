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
import com.example.myapplication.managers.MusicManager
import com.example.myapplication.managers.SoundManager
import com.example.myapplication.entities.AmmoPickup
import com.example.myapplication.rendering.BackgroundManager
import com.example.myapplication.rendering.GameRenderer
import com.example.myapplication.systems.AmmoSystem
import com.example.myapplication.systems.BulletSystem
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
    private val backgroundManager = BackgroundManager(context) // 🎨 Quản lý background
    private val inputHandler = InputHandler()              // 👆 Xử lý touch input
    private val monsterSystem = MonsterSystem()            // 👾 Xử lý logic monster
    private val bulletSystem = BulletSystem()               // 🎯 Xử lý logic bullet
//    private val soundManager = SoundManager(context)
    private lateinit var musicManager: MusicManager
    private lateinit var soundManager: SoundManager
    private val ammoSystem = AmmoSystem()
    private val particleSystem = ParticleSystem()

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
    private var lastUpdateTime = 0L                        // Thời gian lần cuối update animation
    private var playerAmmo = 5  // Bắt đầu với 5 viên đạn
    private val maxAmmo = 5

    init {
        initGame()
    }

    /**
     * 🔧 Khởi tạo game
     * Setup các listener để các component có thể giao tiếp với nhau
     */
    private fun initGame() {
        soundManager = SoundManager.getInstance()!!
        musicManager = MusicManager.getInstance()!!

        // Setup listeners để tạo communication giữa các component
        gameLogic.setGameStateListener(this)        // GameView lắng nghe thay đổi từ GameLogic
        inputHandler.setPlayerMoveListener(this)    // GameView lắng nghe input từ InputHandler

        loadAudioSettings()
    }

    // ===== PUBLIC API METHODS =====
    // Các method public để Activity/Fragment có thể điều khiển game

    // 🆕 GETTER CHO CURRENT LEVEL ID
    fun getCurrentLevelId(): Int {
        return gameLogic.getCurrentLevel()?.id ?: 1
    }

    fun loadLevel(levelId: Int) {
        gameLogic.loadLevel(levelId)
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
            listOf(Pair(playerStartPos.second, playerStartPos.first)) // (x, y) format
        } else {
            emptyList()
        }
        ammoSystem.spawnRandomAmmo(gameLogic.getMap(), 3, excludePositions)

        // 🆕 RESET AMMO (hoặc giữ từ level trước nếu muốn)
        playerAmmo = 5  // Reset về 5 viên mỗi level

        gameStateChanged = true
    }

    fun setBackgroundImage(resourceId: Int, scrollType: BackgroundManager.BackgroundScrollType = BackgroundManager.BackgroundScrollType.PARALLAX_HORIZONTAL) {
        backgroundManager.setBackgroundImage(resourceId, scrollType)
        gameStateChanged = true  // Báo cần redraw
    }
    

    fun setBackgroundImageFromAssets(fileName: String, scrollType: BackgroundManager.BackgroundScrollType = BackgroundManager.BackgroundScrollType.PARALLAX_HORIZONTAL) {
        backgroundManager.setBackgroundImageFromAssets(fileName, scrollType)
        gameStateChanged = true  // Báo cần redraw
    }
    
    /**
     * ⚡ Điều chỉnh tốc độ animation background
     * @param speed Tốc độ (VD: 0.5 = chậm, 2.0 = nhanh)
     */
    fun setBackgroundSpeed(speed: Float) {
        backgroundManager.setBackgroundSpeed(speed)
    }
    
    /**
     * 🔄 Thay đổi kiểu animation background
     * @param type Loại animation (PARALLAX, ZOOM, ROTATING, ...)
     */
    fun setBackgroundScrollType(type: BackgroundManager.BackgroundScrollType) {
        backgroundManager.setBackgroundScrollType(type)
        gameStateChanged = true  // Báo cần redraw
    }

    fun pauseBackgroundAnimation() {
        backgroundManager.pauseBackgroundAnimation()
    }

    fun resumeBackgroundAnimation(speed: Float = 0.5f) {
        backgroundManager.resumeBackgroundAnimation(speed)
    }


    fun startGame() {
        if (!isGameRunning) {
            isGameRunning = true
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
                    
                    // 2. Nếu có thay đổi → trigger redraw trên UI thread
                    if (gameStateChanged) {
                        post { invalidate() }        // Schedule onDraw() trên UI thread
                        gameStateChanged = false     // Reset flag
                    }

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
        val playerPos = gameLogic.getPlayerPosition()
        if (ammoSystem.checkAmmoCollection(playerPos.second, playerPos.first)) {
            // 🆕 TĂNG ĐẠN (TỐI ĐA 5)
            if (playerAmmo < maxAmmo) {
                playerAmmo++
                println("💚 Ammo collected! Current ammo: $playerAmmo/$maxAmmo")

                // 🆕 PHÁT ÂM THANH AMMO PICKUP
                soundManager.playSound("ammo_pickup")
            } else {
                println("💚 Ammo collected but already at max ($maxAmmo)!")
                // Vẫn phát âm thanh ngay cả khi đã max ammo
                soundManager.playSound("ammo_pickup")
            }
        }

        // Update bullets
        // Update vị trí bullets và cleanup
        val tileSize = gameRenderer.calculateTileSize(gameLogic.getMap()).toFloat()
        val (offsetX, offsetY) = gameRenderer.calculateBoardOffset(gameLogic.getMap())
        bulletSystem.updateBullets(deltaTime, width.toFloat(), height.toFloat(), gameLogic.getMap(), tileSize, offsetX, offsetY)

        // ===== CHECK BULLET COLLISIONS =====
        // Kiểm tra bullets có chạm monsters không
        val monsterPositions = monsterSystem.getActiveMonsters().map { monster ->
            // Convert grid coordinates to screen coordinates
            val screenX = offsetX + monster.currentY * tileSize  // currentY là column
            val screenY = offsetY + monster.currentX * tileSize  // currentX là row
            Pair(screenX, screenY)
        }

        val collisions = bulletSystem.checkCollisions(monsterPositions)

        // DEBUG: Log monster positions
        println("🎯 Checking ${monsterPositions.size} monsters for collisions")
        monsterPositions.forEachIndexed { index, (x, y) ->
            println("👹 Monster $index at screen pos (${x.toInt()}, ${y.toInt()})")
        }

        // DEBUG: Log collisions found
        println("💥 Found ${collisions.size} collisions")
        collisions.forEach { (bullet, monsterIndex) ->
            println("🎯 Processing collision: bullet ${bullet.id} hit monster $monsterIndex")

            // 🆕 XỬ LÝ KHI BULLET CHẠM MONSTER
            monsterSystem.removeMonster(monsterIndex)  // Xóa monster

            // 🆕 TẠO EXPLOSION TẠI VỊ TRÍ MONSTER
            val monsterPos = monsterPositions[monsterIndex]
            println("💥 Creating explosion at (${monsterPos.first.toInt()}, ${monsterPos.second.toInt()})")
            particleSystem.createExplosion(monsterPos.first, monsterPos.second)

            // Phát âm thanh
            soundManager.playSound("monster_hit")

            println("💥 Bullet destroyed monster $monsterIndex!")
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
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        
        // Thông báo kích thước mới cho các component
        gameRenderer.setScreenSize(w, h)       // Để tính toán tile size và layout
        backgroundManager.setScreenSize(w, h)  // Để scale background cho phù hợp
    }

    /**
     * 🎨 Vẽ toàn bộ game lên Canvas
     * 
     * Method này được gọi mỗi khi cần redraw (khi gọi invalidate()).
     * Thứ tự vẽ rất quan trọng: Background → Game Board → UI
     * 
     * @param canvas Canvas để vẽ lên
     */
    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        // 1. 🎨 Vẽ background trước (nền)
        backgroundManager.drawBackground(canvas, animationTime)

        // 2. 🎮 Vẽ game board (tiles: wall, box, player, goal)
        //    Chỉ vẽ nếu đã load level
        if (!gameLogic.isMapEmpty()) {
            val monsters = monsterSystem.getActiveMonsters()
            gameRenderer.drawGameBoard(canvas, gameLogic.getMap(), gameLogic.getPlayerDirection(), monsters)
        }

        // 🆕 DRAW AMMO UI
        gameRenderer.drawAmmoUI(canvas, playerAmmo, maxAmmo, width.toFloat(), height.toFloat())

        // 🆕 DRAW AMMO PICKUPS
        if (!gameLogic.isMapEmpty()) {
            val tileSize = gameRenderer.calculateTileSize(gameLogic.getMap()).toFloat()
            val (offsetX, offsetY) = gameRenderer.calculateBoardOffset(gameLogic.getMap())
            val ammoPickups = ammoSystem.getActiveAmmoPickups()
            gameRenderer.drawAmmoPickups(canvas, ammoPickups, tileSize, offsetX, offsetY)
        }

        // 🎛️ Vẽ nút toggle phía trên map
        val musicEnabled = musicManager.isEnabled()
        val soundEnabled = !soundManager.isMuted()  // enabled = true khi không muted
        gameRenderer.drawToggleButtons(canvas, gameLogic.getMap(), musicEnabled, soundEnabled)

        // Vẽ bullets
        val activeBullets = bulletSystem.getActiveBullets()
        gameRenderer.drawBullets(canvas, activeBullets)

        // 🆕 DRAW PARTICLES (sau khi vẽ game objects)
        particleSystem.draw(canvas)
        
        // 3. 🖼️ Vẽ UI elements cuối cùng (trên cùng)
        //    Title, instructions, score, etc.
        gameRenderer.drawGameUI(canvas)
    }


    override fun onTouchEvent(event: MotionEvent): Boolean {
        // 🔄 KIỂM TRA NÚT TOGGLE TRƯỚC
        if (event.action == MotionEvent.ACTION_UP) {
            val touchX = event.x
            val touchY = event.y

            // Kiểm tra nút Music (trái)
            if (isTouchOnToggleButton(touchX, touchY, gameLogic.getMap(), "music")) {
                toggleMusic()
                return true
            }

            // Kiểm tra nút Sound (phải)
            if (isTouchOnToggleButton(touchX, touchY, gameLogic.getMap(), "sound")) {
                toggleSound()
                return true
            }
        }

        // 🔄 DELEGATE CHO INPUT HANDLER TRƯỚC
        val inputHandled = inputHandler.handleTouchEvent(event)

        // Nếu InputHandler đã xử lý (swipe), return luôn
        if (inputHandled) {
            return true
        }

        // Nếu InputHandler không xử lý (tap), thì bắn đạn
        when (event.action) {
            MotionEvent.ACTION_UP -> {
                // 🎯 BẮN ĐẠN THEO HƯỚNG PLAYER

                if (playerAmmo <= 0) {
                    println("❌ Out of ammo!")
                    return true  // Không bắn được
                }

                // 1️⃣ Lấy vị trí player trên grid
                val playerPos = gameLogic.getPlayerPosition()
                val playerDirection = gameLogic.getPlayerDirection()

                // 2️⃣ Convert grid position → screen position
                val tileSize = gameRenderer.calculateTileSize(gameLogic.getMap())
                val (offsetX, offsetY) = gameRenderer.calculateBoardOffset(gameLogic.getMap())

                // 3️⃣ Tính vị trí player trên màn hình (CENTER của tile)
                val playerScreenX = offsetX + playerPos.second * tileSize + tileSize/2  // Center X
                val playerScreenY = offsetY + playerPos.first * tileSize + tileSize/2   // Center Y

                println("🎯 Player position: Grid(${playerPos.first}, ${playerPos.second}) -> Screen(${playerScreenX.toInt()}, ${playerScreenY.toInt()})")

                // 4️⃣ Tính target position dựa trên hướng player (TĂNG KHOẢNG CÁCH!)
                val targetX = when (playerDirection) {
                    PlayerDirection.LEFT -> playerScreenX - 2000f    // Bắn sang trái xa hơn
                    PlayerDirection.RIGHT -> playerScreenX + 2000f   // Bắn sang phải xa hơn
                    PlayerDirection.UP -> playerScreenX             // Giữ nguyên X
                    PlayerDirection.DOWN -> playerScreenX           // Giữ nguyên X
                }

                val targetY = when (playerDirection) {
                    PlayerDirection.LEFT -> playerScreenY           // Giữ nguyên Y
                    PlayerDirection.RIGHT -> playerScreenY          // Giữ nguyên Y
                    PlayerDirection.UP -> playerScreenY - 800f      // Bắn lên trên xa hơn
                    PlayerDirection.DOWN -> playerScreenY + 800f    // Bắn xuống dưới xa hơn
                }

                // 5️⃣ Bắn đạn theo hướng player
                bulletSystem.addBullet(playerScreenX, playerScreenY, targetX, targetY)

                // Giảm ammo và in ra debug
                playerAmmo--
                println("🔫 Player ammo: $playerAmmo/$maxAmmo")

                // 🆕 THÊM ÂM THANH BẮN ĐẠN
                soundManager.playSound("shoot")
                println("🎯 Player fired bullet in direction: $playerDirection")

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

        // 🆕 LƯU PROGRESS: Cập nhật level đã hoàn thành
        val currentLevelId = gameLogic.getCurrentLevel()?.id ?: 1
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
            // 🆕 HIỂN THỊ DIALOG CHIẾN THẮNG (thay vì Activity)
            showWinDialog()
        }
    }

    // 🆕 DIALOG CHIẾN THẮNG
    private fun showWinDialog() {
        var levelId = gameLogic.getCurrentLevel()?.id ?: 1
        val nextLevelId = levelId + 1

        val dialogView = android.view.LayoutInflater.from(context).inflate(R.layout.dialog_win, null)

        val titleText = dialogView.findViewById<android.widget.TextView>(R.id.win_title)
        val messageText = dialogView.findViewById<android.widget.TextView>(R.id.win_message)
        val nextButton = dialogView.findViewById<android.widget.Button>(R.id.btn_next_level)
        val menuButton = dialogView.findViewById<android.widget.Button>(R.id.btn_back_to_menu)

        titleText.text = "🎉 CHÚC MỪNG! 🎉"
        messageText.text = "Bạn đã hoàn thành Level $levelId!"

        val dialog = AlertDialog.Builder(context)
            .setView(dialogView)
            .setCancelable(false) // Không cho phép dismiss bằng back button
            .create()

        // Background mờ
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        nextButton.setOnClickListener {
            soundManager.playSound("move")

            // Load level tiếp theo
            val newLevelId = levelId + 1
            levelId = newLevelId
            loadLevel(newLevelId)
            startGame()  // 🆕 RESTART GAME LOOP
            dialog.dismiss()


        }

        menuButton.setOnClickListener {
            soundManager.playSound("move")

            val intent = Intent(context, MenuActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
            context.startActivity(intent)
            (context as? GameButtonActivity)?.finish()

            dialog.dismiss()
        }

        dialog.show()
    }

    // 🆕 DIALOG THUA
    private fun showLoseDialog() {
        val levelId = gameLogic.getCurrentLevel()?.id ?: 1

        val dialogView = android.view.LayoutInflater.from(context).inflate(R.layout.dialog_lose, null)

        val titleText = dialogView.findViewById<android.widget.TextView>(R.id.lose_title)
        val messageText = dialogView.findViewById<android.widget.TextView>(R.id.lose_message)
        val retryButton = dialogView.findViewById<android.widget.Button>(R.id.btn_retry)
        val menuButton = dialogView.findViewById<android.widget.Button>(R.id.btn_back_to_menu)

        titleText.text = "💀 GAME OVER! 💀"
        messageText.text = "Bạn đã thua ở Level $levelId!"

        val dialog = AlertDialog.Builder(context)
            .setView(dialogView)
            .setCancelable(false) // Không cho phép dismiss bằng back button
            .create()

        // Background mờ
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        retryButton.setOnClickListener {
            soundManager.playSound("move")

            // Retry level hiện tại
            loadLevel(levelId)
            startGame()  // Restart game loop
            dialog.dismiss()
        }

        menuButton.setOnClickListener {
            soundManager.playSound("move")

            val intent = Intent(context, MenuActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
            context.startActivity(intent)
            (context as? GameButtonActivity)?.finish()

            dialog.dismiss()
        }

        dialog.show()
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

    fun resetLevel() {
        gameLogic.resetLevel()
    }

    fun isGameWon(): Boolean = gameLogic.isGameWon()
    

    fun getProgressPercentage(): Float = gameLogic.getProgressPercentage()

    fun getCurrentLevel() = gameLogic.getCurrentLevel()
    /**
     * 💀 Xử lý khi player chết (chạm monster)
     */
    private fun onPlayerDied() {
        isGameRunning = false  // Dừng game loop

        // 🆕 Phát âm thanh thua
        soundManager.playSound("game_over")

        post {
            // 🆕 HIỂN THỊ DIALOG THUA
            showLoseDialog()
        }
    }

    // Thêm vào cuối file GameView.kt
    fun setSoundMuted(muted: Boolean) {
        soundManager.setMuted(muted)
    }

    fun isSoundMuted(): Boolean = soundManager.isMuted()

    /**
     * 🔘 Kiểm tra xem touch có nằm trên nút toggle không
     */
    private fun isTouchOnToggleButton(x: Float, y: Float, map: Array<CharArray>,
                                      buttonType: String): Boolean {
        val tileSize = gameRenderer.calculateTileSize(map)
        val boardWidth = map[0].size * tileSize
        val boardHeight = map.size * tileSize
        val offsetX = (width - boardWidth) / 2f
        val offsetY = (height - boardHeight) / 2f

        val buttonY = offsetY - 140f  // Cập nhật cho khớp với GameRenderer
        val buttonSize = 120f         // Cập nhật cho khớp với GameRenderer

        val buttonRect = when (buttonType) {
            "music" -> android.graphics.RectF(20f, buttonY, 20f + buttonSize, buttonY + buttonSize)
            "sound" -> android.graphics.RectF(width - buttonSize - 20f, buttonY,
                width - 20f, buttonY + buttonSize)
            else -> return false
        }

        return buttonRect.contains(x, y)
    }

    /**
     * 🎵 Toggle nhạc nền
     */
    private fun toggleMusic() {
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

        // Trigger redraw để cập nhật icon
        gameStateChanged = true

        println("🎵 Music toggled: $newState")
    }

    /**
     * 🔊 Toggle âm thanh + nhạc
     */
    private fun toggleSound() {
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

        // Trigger redraw để cập nhật icon
        gameStateChanged = true

        println("🔊 Sound toggled: enabled=$newEnabledState, muted=${!newEnabledState}")
    }

    private fun loadAudioSettings() {
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
}