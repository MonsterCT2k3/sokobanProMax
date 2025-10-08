package com.example.myapplication

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.SeekBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.myapplication.managers.MusicManager
import kotlin.random.Random

/**
 * 🎨 CustomizeActivity - Chế độ tùy chỉnh tạo màn chơi
 *
 * Cho phép người chơi tùy chỉnh:
 * - Kích thước bản đồ (width x height)
 * - Số lượng hộp cần đẩy
 * - Số lượng quái vật
 * - Số lượng ô safe zone
 * - Tạo level ngẫu nhiên từ các tham số này
 */
class CustomizeActivity : AppCompatActivity() {

    private lateinit var musicManager: MusicManager

    // UI Elements
    private lateinit var sbMapWidth: SeekBar
    private lateinit var sbMapHeight: SeekBar
    private lateinit var sbBoxCount: SeekBar
    private lateinit var sbMonsterCount: SeekBar
    private lateinit var sbSafeZoneCount: SeekBar

    private lateinit var tvMapWidth: TextView
    private lateinit var tvMapHeight: TextView
    private lateinit var tvBoxCount: TextView
    private lateinit var tvMonsterCount: TextView
    private lateinit var tvSafeZoneCount: TextView

    private lateinit var btnGenerateLevel: Button
    private lateinit var btnBackToMenu: Button

    // Default values
    private var mapWidth = 15
    private var mapHeight = 15
    private var boxCount = 3
    private var monsterCount = 2
    private var safeZoneCount = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_customize)

        musicManager = MusicManager.getInstance()!!

        initializeViews()
        setupSeekBars()
        setupButtons()
    }

    private fun initializeViews() {
        // SeekBars
        sbMapWidth = findViewById(R.id.sbMapWidth)
        sbMapHeight = findViewById(R.id.sbMapHeight)
        sbBoxCount = findViewById(R.id.sbBoxCount)
        sbMonsterCount = findViewById(R.id.sbMonsterCount)
        sbSafeZoneCount = findViewById(R.id.sbSafeZoneCount)

        // TextViews
        tvMapWidth = findViewById(R.id.tvMapWidth)
        tvMapHeight = findViewById(R.id.tvMapHeight)
        tvBoxCount = findViewById(R.id.tvBoxCount)
        tvMonsterCount = findViewById(R.id.tvMonsterCount)
        tvSafeZoneCount = findViewById(R.id.tvSafeZoneCount)

        // Buttons
        btnGenerateLevel = findViewById(R.id.btnGenerateLevel)
        btnBackToMenu = findViewById(R.id.btnBackToMenu)
    }

    private fun setupSeekBars() {
        // Map Width: 10-25
        sbMapWidth.max = 15 // 10-25 range
        sbMapWidth.progress = mapWidth - 10
        sbMapWidth.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                mapWidth = progress + 10
                tvMapWidth.text = "Map Width: $mapWidth"
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })
        tvMapWidth.text = "Map Width: $mapWidth"

        // Map Height: 10-25
        sbMapHeight.max = 15 // 10-25 range
        sbMapHeight.progress = mapHeight - 10
        sbMapHeight.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                mapHeight = progress + 10
                tvMapHeight.text = "Map Height: $mapHeight"
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })
        tvMapHeight.text = "Map Height: $mapHeight"

        // Box Count: 1-8
        sbBoxCount.max = 7 // 1-8 range
        sbBoxCount.progress = boxCount - 1
        sbBoxCount.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                boxCount = progress + 1
                tvBoxCount.text = "Box Count: $boxCount"
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })
        tvBoxCount.text = "Box Count: $boxCount"

        // Monster Count: 0-8
        sbMonsterCount.max = 8 // 0-8 range
        sbMonsterCount.progress = monsterCount
        sbMonsterCount.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                monsterCount = progress
                tvMonsterCount.text = "Monster Count: $monsterCount"
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })
        tvMonsterCount.text = "Monster Count: $monsterCount"

        // Safe Zone Count: 0-3
        sbSafeZoneCount.max = 3 // 0-3 range
        sbSafeZoneCount.progress = safeZoneCount
        sbSafeZoneCount.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                safeZoneCount = progress
                tvSafeZoneCount.text = "Safe Zone Count: $safeZoneCount"
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })
        tvSafeZoneCount.text = "Safe Zone Count: $safeZoneCount"
    }

    private fun setupButtons() {
        btnGenerateLevel.setOnClickListener {
            if (validateSettings()) {
                generateAndStartLevel()
            }
        }

        btnBackToMenu.setOnClickListener {
            val intent = Intent(this, GameModeSelectionActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            startActivity(intent)
            finish()
        }
    }

    private fun validateSettings(): Boolean {
        // Kiểm tra số hộp không vượt quá số ô trống hợp lý
        val maxBoxes = (mapWidth * mapHeight) / 4 // Tối đa 25% diện tích
        if (boxCount > maxBoxes) {
            Toast.makeText(this, "Too many boxes for this map size! Max: $maxBoxes", Toast.LENGTH_LONG).show()
            return false
        }

        // Kiểm tra có đủ không gian cho monster và safe zones
        val totalElements = boxCount + monsterCount + safeZoneCount + 1 // boxes + monsters + safe zones + player
        val availableSpace = (mapWidth * mapHeight) * 0.6 // 60% diện tích khả dụng
        if (totalElements > availableSpace) {
            Toast.makeText(this, "Too many elements for this map size!", Toast.LENGTH_LONG).show()
            return false
        }

        return true
    }

    private fun generateAndStartLevel() {
        try {
            // Tạo level ngẫu nhiên từ settings
            val (mapString, monsterData) = generateRandomLevel()

            println("🚀 ========== STARTING GAME WITH CUSTOM LEVEL ==========")
            println("📦 Map size: ${mapWidth}x${mapHeight}")
            println("📦 Box count: $boxCount")
            println("🐺 Monster data size: ${monsterData.size}")
            println("🐺 Monster details:")
            monsterData.forEachIndexed { index, (x, y, type) ->
                println("   ${index + 1}. Position: ($x, $y), Type: $type")
            }
            println("=======================================================")

            // Chuyển đến GameButtonActivity với level tùy chỉnh
            val intent = Intent(this, GameButtonActivity::class.java)
            intent.putExtra("LEVEL_ID", -1) // -1 để đánh dấu là custom level
            // Truyền thông tin custom level qua các extra riêng biệt
            intent.putExtra("customLevelMap", mapString)
            intent.putExtra("customLevelWidth", mapWidth)
            intent.putExtra("customLevelHeight", mapHeight)
            intent.putExtra("customBoxCount", boxCount)
            intent.putExtra("customMonsterData", ArrayList(monsterData))
            startActivity(intent)

        } catch (e: Exception) {
            Toast.makeText(this, "Failed to generate level: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    private fun generateRandomLevel(): Pair<String, List<Triple<Int, Int, String>>> {
        val map = Array(mapHeight) { CharArray(mapWidth) }

        println("🎮 ========== BẮT ĐẦU TẠO MAP ==========")
        println("📐 Kích thước MAP: width=$mapWidth, height=$mapHeight")
        println("📐 Array size: map[0..${mapHeight-1}][0..${mapWidth-1}]")
        println("📦 Boxes: $boxCount, 🐺 Monsters: $monsterCount, 🛡️ Safe zones: $safeZoneCount")

        // Khởi tạo map với tường bao quanh
        for (y in 0 until mapHeight) {
            for (x in 0 until mapWidth) {
                map[y][x] = if (x == 0 || x == mapWidth - 1 || y == 0 || y == mapHeight - 1) '#' else ' '
            }
        }

        // Helper function để lấy tất cả vị trí trống thực sự trên map
        fun getEmptyPositions(): List<Pair<Int, Int>> {
            val positions = mutableListOf<Pair<Int, Int>>()
            for (y in 1 until mapHeight - 1) {
                for (x in 1 until mapWidth - 1) {
                    if (map[y][x] == ' ') {
                        positions.add(Pair(x, y))
                    }
                }
            }
            return positions
        }

        // Helper function để đặt một phần tử lên map
        fun placeElement(char: Char, name: String): Pair<Int, Int>? {
            val emptyPos = getEmptyPositions()
            if (emptyPos.isEmpty()) {
                println("⚠️ Không còn vị trí trống cho $name")
                return null
            }
            val pos = emptyPos.random()
            map[pos.second][pos.first] = char
            println("✅ Đặt $name tại (${pos.first}, ${pos.second})")
            return pos
        }

        // 1. ĐẶT PLAYER
        println("\n👤 BƯỚC 1: Đặt player...")
        val playerPos = placeElement('@', "player") ?: throw Exception("Cannot place player")

        // 2. TẠO WALLS
        println("\n🧱 BƯỚC 2: Tạo walls...")
        val targetWallCount = Random.nextInt(mapWidth * mapHeight / 25, mapWidth * mapHeight / 10)
        var actualWallCount = 0

        // Tạo wall clusters
        val clusterCount = Random.nextInt(2, 5)
        for (cluster in 0 until clusterCount) {
            val centerX = Random.nextInt(3, mapWidth - 3)
            val centerY = Random.nextInt(3, mapHeight - 3)
            val size = Random.nextInt(2, 4)

            for (dy in -size..size) {
                for (dx in -size..size) {
                    val x = centerX + dx
                    val y = centerY + dy
                    if (x >= 2 && x < mapWidth - 2 && y >= 2 && y < mapHeight - 2) {
                        if (map[y][x] == ' ' && Random.nextFloat() < 0.4f) {
                            map[y][x] = '#'
                            actualWallCount++
                        }
                    }
                }
            }
        }

        // Thêm single walls
        while (actualWallCount < targetWallCount) {
            val emptyPos = getEmptyPositions()
            if (emptyPos.isEmpty()) break
            
            val pos = emptyPos.random()
            map[pos.second][pos.first] = '#'
            actualWallCount++
        }
        
        println("✅ Đã tạo $actualWallCount walls")

        // 3. ĐẶT BOXES - tránh sát tường
        println("\n📦 BƯỚC 3: Đặt $boxCount boxes...")
        var boxesPlaced = 0
        
        for (i in 0 until boxCount) {
            val emptyPos = getEmptyPositions()
            if (emptyPos.isEmpty()) {
                println("⚠️ Không đủ chỗ cho box ${i + 1}/${boxCount}")
                break
            }

            // Ưu tiên vị trí không sát tường
            var bestPos: Pair<Int, Int>? = null
            for (pos in emptyPos.shuffled()) {
                if (!isNearWall(pos, map, mapWidth, mapHeight)) {
                    bestPos = pos
                    break
                }
            }
            
            // Nếu không tìm được, dùng vị trí bất kỳ
            val finalPos = bestPos ?: emptyPos.random()
            map[finalPos.second][finalPos.first] = 'B'
            boxesPlaced++
        }
        println("✅ Đã đặt $boxesPlaced/$boxCount boxes")

        // 4. ĐẶT GOALS - bằng số boxes
        println("\n🎯 BƯỚC 4: Đặt $boxCount goals...")
        var goalsPlaced = 0
        
        for (i in 0 until boxCount) {
            val pos = placeElement('G', "goal #${i + 1}")
            if (pos != null) goalsPlaced++
        }
        println("✅ Đã đặt $goalsPlaced/$boxCount goals")

        // 5. ĐẶT SAFE ZONES
        println("\n🛡️ BƯỚC 5: Đặt $safeZoneCount safe zones...")
        var safeZonesPlaced = 0
        
        for (i in 0 until safeZoneCount) {
            val pos = placeElement('S', "safe zone #${i + 1}")
            if (pos != null) safeZonesPlaced++
        }
        println("✅ Đã đặt $safeZonesPlaced/$safeZoneCount safe zones")

        // 6. ĐẶT MONSTERS - DÙNG LOGIC GIỐNG BOXES ĐỂ ĐẢM BẢO ĐỦ SỐ LƯỢNG
        println("\n🐺 BƯỚC 6: Đặt $monsterCount monsters (phân tán đều)...")
        val monsterData = mutableListOf<Triple<Int, Int, String>>()
        val monsterPositions = mutableListOf<Pair<Int, Int>>()
        
        for (i in 0 until monsterCount) {
            val emptyPos = getEmptyPositions()
            if (emptyPos.isEmpty()) {
                println("⚠️ Không đủ chỗ cho monster ${i + 1}/${monsterCount}")
                break
            }

            // Tìm vị trí phân tán đều - xa player và xa monsters khác
            var bestPos = emptyPos.random()
            var maxMinDist = 0
            
            // Duyệt qua một số vị trí ngẫu nhiên để tìm vị trí tốt nhất
            val samplesToCheck = minOf(emptyPos.size, 50) // Chỉ check tối đa 50 vị trí để tối ưu
            for (sample in emptyPos.shuffled().take(samplesToCheck)) {
                val distToPlayer = Math.abs(sample.first - playerPos.first) + Math.abs(sample.second - playerPos.second)
                
                var minDistToMonsters = 999
                for (monsterPos in monsterPositions) {
                    val dist = Math.abs(sample.first - monsterPos.first) + Math.abs(sample.second - monsterPos.second)
                    if (dist < minDistToMonsters) {
                        minDistToMonsters = dist
                    }
                }
                
                // Kết hợp khoảng cách: 40% player, 60% monsters khác (ưu tiên phân tán)
                val combinedDist = (distToPlayer * 0.4 + minDistToMonsters * 0.6).toInt()
                
                if (combinedDist > maxMinDist) {
                    maxMinDist = combinedDist
                    bestPos = sample
                }
            }

            val monsterType = if (Random.nextBoolean()) "PATROL" else "BOUNCE"
            
            // QUAN TRỌNG: 
            // bestPos = Pair(x=col, y=row) từ getEmptyPositions
            // Nhưng GameView (khi for ((x, y, type))) expect x=row, y=col (theo cách GameLogic parse)
            // VÌ KHÔNG SỬA GameLogic (ảnh hưởng chế độ khác), phải ĐỔI ở đây
            val monsterX = bestPos.first  // col từ bestPos
            val monsterY = bestPos.second // row từ bestPos
            
            // VALIDATE: Đảm bảo monster trong biên map
            if (monsterX >= mapWidth || monsterY >= mapHeight || monsterX < 0 || monsterY < 0) {
                println("   ⚠️ MONSTER ${i + 1} NGOÀI BIÊN! col=$monsterX (max=${mapWidth-1}), row=$monsterY (max=${mapHeight-1}) - BỎ QUA!")
                continue
            }
            
            // CHỈ thêm vào monsterPositions SAU KHI validate thành công
            monsterPositions.add(bestPos)
            
            // ĐỔI THỨ TỰ: Lưu (row, col) thay vì (col, row) để khớp với GameView
            monsterData.add(Triple(monsterY, monsterX, monsterType))  // (row, col, type)
            
            val nearestDist = if (monsterPositions.size > 1) {
                monsterPositions.dropLast(1).minOf { 
                    Math.abs(bestPos.first - it.first) + Math.abs(bestPos.second - it.second) 
                }
            } else 0
            
            println("   🐺 Monster ${monsterData.size}: col=$monsterX, row=$monsterY (max: col<$mapWidth, row<$mapHeight) → Triple($monsterY, $monsterX, $monsterType) [ĐỔI row,col] - nearest: ${if (nearestDist > 0) "${nearestDist} tiles" else "first"}")
        }
        println("✅ Đã đặt ${monsterData.size}/$monsterCount monsters")

        // 7. ĐẶT AMMO PICKUPS
        println("\n💣 BƯỚC 7: Đặt ammo pickups...")
        val ammoCount = Random.nextInt(2, 5)
        var ammoPlaced = 0
        
        for (i in 0 until ammoCount) {
            val emptyPos = getEmptyPositions()
            if (emptyPos.isEmpty()) break
            
            val pos = emptyPos.random()
            val ammoType = when (Random.nextInt(3)) {
                0 -> 'N'  // Normal
                1 -> 'P'  // Pierce
                else -> 'T'  // sTun (đổi từ S để tránh trùng Safe zone)
            }
            map[pos.second][pos.first] = ammoType
            ammoPlaced++
        }
        println("✅ Đã đặt $ammoPlaced ammo pickups")

        // 8. ĐẶT LIVES PICKUPS
        println("\n❤️ BƯỚC 8: Đặt lives pickups...")
        val livesCount = Random.nextInt(1, 3)
        var livesPlaced = 0
        
        for (i in 0 until livesCount) {
            val pos = placeElement('L', "lives #${i + 1}")
            if (pos != null) livesPlaced++
        }
        println("✅ Đã đặt $livesPlaced lives pickups")

        // 9. DEBUG - ĐẾM FINAL STATS
        println("\n📊 ========== FINAL MAP STATS ==========")
        var actualPlayer = 0
        var actualWalls = 0
        var actualBoxes = 0
        var actualGoals = 0
        var actualSafeZones = 0
        var actualEmpty = 0
        var actualPickups = 0

        for (y in 0 until mapHeight) {
            for (x in 0 until mapWidth) {
                when (map[y][x]) {
                    '@' -> actualPlayer++
                    '#' -> actualWalls++
                    'B' -> actualBoxes++
                    'G' -> actualGoals++
                    'S' -> actualSafeZones++
                    ' ' -> actualEmpty++
                    'N', 'P', 'T', 'L' -> actualPickups++
                }
            }
        }

        println("👤 Player: $actualPlayer")
        println("🐺 Monsters: ${monsterData.size} (expected: $monsterCount)")
        println("🧱 Walls: $actualWalls")
        println("📦 Boxes: $actualBoxes (expected: $boxCount)")
        println("🎯 Goals: $actualGoals (expected: $boxCount)")
        println("🛡️ Safe Zones: $actualSafeZones (expected: $safeZoneCount)")
        println("💣 Pickups: $actualPickups")
        println("⬜ Empty: $actualEmpty")
        println("========================================")

        // Chuyển map thành string
        val mapString = map.joinToString("\n") { it.joinToString("") }

        return Pair(mapString, monsterData)
    }

    private fun getNearbyPositions(pos: Pair<Int, Int>, availablePositions: List<Pair<Int, Int>>): List<Pair<Int, Int>> {
        val nearby = mutableListOf<Pair<Int, Int>>()
        val directions = listOf(
            Pair(-1, -1), Pair(-1, 0), Pair(-1, 1),
            Pair(0, -1),           Pair(0, 1),
            Pair(1, -1),  Pair(1, 0),  Pair(1, 1)
        )

        for (dir in directions) {
            val newPos = Pair(pos.first + dir.first, pos.second + dir.second)
            if (newPos in availablePositions) {
                nearby.add(newPos)
            }
        }

        return nearby
    }

    /**
     * Check xem vị trí có cạnh tường (#) không
     * Box không nên đặt cạnh tường để tránh kẹt
     */
    private fun isNearWall(pos: Pair<Int, Int>, map: Array<CharArray>, mapWidth: Int, mapHeight: Int): Boolean {
        val (x, y) = pos
        val directions = listOf(
            Pair(0, -1),  // Trên
            Pair(0, 1),   // Dưới
            Pair(-1, 0),  // Trái
            Pair(1, 0)    // Phải
        )

        for ((dx, dy) in directions) {
            val checkX = x + dx
            val checkY = y + dy

            // Nếu ô kề là tường hoặc ngoài biên, coi là near wall
            if (checkX < 0 || checkX >= mapWidth || checkY < 0 || checkY >= mapHeight ||
                map[checkY][checkX] == '#') {
                return true
            }
        }

        return false
    }

    override fun onResume() {
        super.onResume()
        musicManager.resumeMusic()
    }

    override fun onPause() {
        super.onPause()
        musicManager.pauseMusic()
    }

    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        super.onBackPressed()
        val intent = Intent(this, GameModeSelectionActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
        startActivity(intent)
        finish()
    }
}
