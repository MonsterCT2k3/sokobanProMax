package com.example.myapplication.models

data class Level(
    val id: Int,
    val name: String,
    val difficulty: Difficulty,
    val map: Array<CharArray>,
    val description: String = "",
    val isUnlocked: Boolean = true,
    val bestMoves: Int? = null,
    val bestTime: Long? = null
) {
    enum class Difficulty {
        EASY, MEDIUM, HARD, EXPERT
    }

    fun getPlayerStartPosition(): Pair<Int, Int> {
        for (i in map.indices) {
            for (j in map[i].indices) {
                if (map[i][j] == '@') {
                    return Pair(i, j)
                }
            }
        }
        throw IllegalStateException("Player start position not found in level $id")
    }

    fun getGoalPositions(): Set<Pair<Int, Int>> {
        val goals = mutableSetOf<Pair<Int, Int>>()
        for (i in map.indices) {
            for (j in map[i].indices) {
                if (map[i][j] == 'G') {
                    goals.add(Pair(i, j))
                }
            }
        }
        return goals
    }

    fun getBoxCount(): Int {
        return map.sumOf { row -> row.count { it == 'B' } }
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        other as Level
        return id == other.id
    }

    override fun hashCode(): Int {
        return id
    }
}