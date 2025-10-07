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
 * - Tạo level ngẫu nhiên từ các tham số này
 */
class CustomizeActivity : AppCompatActivity() {

    private lateinit var musicManager: MusicManager

    // UI Elements
    private lateinit var sbMapWidth: SeekBar
    private lateinit var sbMapHeight: SeekBar
    private lateinit var sbBoxCount: SeekBar
    private lateinit var sbMonsterCount: SeekBar

    private lateinit var tvMapWidth: TextView
    private lateinit var tvMapHeight: TextView
    private lateinit var tvBoxCount: TextView
    private lateinit var tvMonsterCount: TextView

    private lateinit var btnGenerateLevel: Button
    private lateinit var btnBackToMenu: Button

    // Default values
    private var mapWidth = 15
    private var mapHeight = 15
    private var boxCount = 3
    private var monsterCount = 2

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

        // TextViews
        tvMapWidth = findViewById(R.id.tvMapWidth)
        tvMapHeight = findViewById(R.id.tvMapHeight)
        tvBoxCount = findViewById(R.id.tvBoxCount)
        tvMonsterCount = findViewById(R.id.tvMonsterCount)

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

        // Monster Count: 0-5
        sbMonsterCount.max = 5 // 0-5 range
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

        // Kiểm tra có đủ không gian cho monster
        val totalElements = boxCount + monsterCount + 3 // boxes + monsters + player + safe zone
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

        // Khởi tạo map với tường bao quanh
        for (y in 0 until mapHeight) {
            for (x in 0 until mapWidth) {
                map[y][x] = if (x == 0 || x == mapWidth - 1 || y == 0 || y == mapHeight - 1) '#' else ' '
            }
        }

        // Danh sách vị trí trống ban đầu
        val emptyPositions = mutableListOf<Pair<Int, Int>>()
        for (y in 1 until mapHeight - 1) {
            for (x in 1 until mapWidth - 1) {
                if (map[y][x] == ' ') {
                    emptyPositions.add(Pair(x, y))
                }
            }
        }

        // Đảm bảo có đủ không gian cho gameplay
        val requiredSpace = boxCount + monsterCount + 8  // boxes + monsters + goals + safe zone + pickups
        if (emptyPositions.size < requiredSpace) {
            throw Exception("Not enough space for all elements. Need at least $requiredSpace empty cells, but only ${emptyPositions.size} available")
        }

        println("🎯 Available positions: ${emptyPositions.size}, required: $requiredSpace")

        // 1. ĐẶT PLAYER TRƯỚC TIÊN
        val playerPos = emptyPositions.random()
        map[playerPos.second][playerPos.first] = '@'
        emptyPositions.remove(playerPos)

        // Track vị trí đã chiếm
        val occupiedPositions = mutableSetOf<Pair<Int, Int>>()
        occupiedPositions.add(playerPos)

        // 2. ĐẶT MONSTERS
        val monsterData = mutableListOf<Triple<Int, Int, String>>()
        for (i in 0 until monsterCount) {
            val monsterPos = emptyPositions.random()
            occupiedPositions.add(monsterPos)
            emptyPositions.remove(monsterPos)

            // Chọn loại monster ngẫu nhiên
            val monsterType = when (Random.nextInt(3)) {
                0 -> "PATROL"
                1 -> "BOUNCE"
                else -> "PATROL"
            }

            monsterData.add(Triple(monsterPos.first, monsterPos.second, monsterType))
        }

        // 3. TẠO GẠCH RẢI RÁC (walls) - tránh vị trí đã chiếm
        val wallCount = Random.nextInt(mapWidth * mapHeight / 25, mapWidth * mapHeight / 10) // 4% - 10% diện tích

        // Tạo các cụm tường lớn để tạo "phòng" và "hành lang"
        val clusterCount = Random.nextInt(2, 6)
        for (cluster in 0 until clusterCount) {
            val clusterX = Random.nextInt(3, mapWidth - 6)
            val clusterY = Random.nextInt(3, mapHeight - 6)
            val clusterSize = Random.nextInt(2, 5)

            // Tạo cụm tường hình vuông hoặc chữ L
            for (dy in 0 until clusterSize) {
                for (dx in 0 until clusterSize) {
                    val x = clusterX + dx
                    val y = clusterY + dy
                    val pos = Pair(x, y)
                    if (x >= 2 && x < mapWidth - 2 && y >= 2 && y < mapHeight - 2 &&
                        map[y][x] == ' ' && pos !in occupiedPositions) {
                        // Tạo hình dạng ngẫu nhiên (không phải tất cả ô trong cụm)
                        if (Random.nextFloat() < 0.5f) { // 50% chance tạo tường
                            map[y][x] = '#'
                        }
                    }
                }
            }
        }

        // Thêm tường đơn lẻ để lấp đầy khoảng trống
        for (i in 0 until wallCount) {
            val x = Random.nextInt(2, mapWidth - 2)
            val y = Random.nextInt(2, mapHeight - 2)
            val pos = Pair(x, y)
            if (map[y][x] == ' ' && pos !in occupiedPositions) {
                map[y][x] = '#'
            }
        }

        // Cập nhật emptyPositions sau khi tạo tường
        emptyPositions.clear()
        for (y in 1 until mapHeight - 1) {
            for (x in 1 until mapWidth - 1) {
                val pos = Pair(x, y)
                if (map[y][x] == ' ' && pos !in occupiedPositions) {
                    emptyPositions.add(pos)
                }
            }
        }

        // 4. ĐẶT BOXES - KHÔNG ĐƯỢC SÁT GẠCH VÀ TƯỜNG
        for (i in 0 until boxCount) {
            // Tìm vị trí không cạnh tường hoặc gạch
            var validBoxPos: Pair<Int, Int>? = null
            val maxAttempts = 50 // Tránh loop vô hạn

            for (attempt in 0 until maxAttempts) {
                if (emptyPositions.isEmpty()) break

                val testPos = emptyPositions.random()
                if (!isNearWall(testPos, map, mapWidth, mapHeight)) {
                    validBoxPos = testPos
                    break
                }
            }

            // Nếu không tìm được vị trí hợp lý, dùng vị trí bất kỳ còn trống
            if (validBoxPos == null && emptyPositions.isNotEmpty()) {
                validBoxPos = emptyPositions.random()
            }

            if (validBoxPos != null) {
                map[validBoxPos.second][validBoxPos.first] = 'B'  // Sokoban standard: 'B' for box
                occupiedPositions.add(validBoxPos)
                emptyPositions.remove(validBoxPos)
            }
        }


        // Đặt safe zone
        if (emptyPositions.isNotEmpty()) {
            val safeZonePos = emptyPositions.random()
            map[safeZonePos.second][safeZonePos.first] = 'S'
            emptyPositions.remove(safeZonePos)
        }

        // Đặt ammo pickups (2-4 viên tùy map size)
        val ammoPickupCount = Random.nextInt(2, minOf(5, emptyPositions.size / 2 + 1))
        for (i in 0 until ammoPickupCount) {
            if (emptyPositions.isNotEmpty()) {
                val ammoPos = emptyPositions.random()
                // Chọn loại ammo ngẫu nhiên: N (normal), P (pierce), S (stun)
                val ammoType = when (Random.nextInt(3)) {
                    0 -> 'N'
                    1 -> 'P'
                    else -> 'S'
                }
                map[ammoPos.second][ammoPos.first] = ammoType
                emptyPositions.remove(ammoPos)
            }
        }

        // Đặt lives pickups (1-2 viên)
        val livesPickupCount = Random.nextInt(1, minOf(3, emptyPositions.size + 1))
        for (i in 0 until livesPickupCount) {
            if (emptyPositions.isNotEmpty()) {
                val livesPos = emptyPositions.random()
                map[livesPos.second][livesPos.first] = 'L'
                emptyPositions.remove(livesPos)
            }
        }

        // 5. TẠO GOALS Ở CUỐI CÙNG - số lượng bằng số boxes
        for (i in 0 until boxCount) {
            if (emptyPositions.isNotEmpty()) {
                val goalPos = emptyPositions.random()
                map[goalPos.second][goalPos.first] = 'G'  // Sokoban standard: 'G' for goal
                emptyPositions.remove(goalPos)
            }
        }

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
        val intent = Intent(this, GameModeSelectionActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
        startActivity(intent)
        finish()
    }
}
