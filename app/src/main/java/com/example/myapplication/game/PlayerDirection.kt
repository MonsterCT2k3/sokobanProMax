package com.example.myapplication.game

enum class PlayerDirection {
    UP, DOWN, LEFT, RIGHT;
    companion object {
        fun fromMovement(dx: Int, dy: Int): PlayerDirection {
            return when{
                dx == -1 -> UP
                dx == 1 -> DOWN
                dy == -1 -> LEFT
                dy == 1 -> RIGHT
                else -> DOWN
            }
        }
    }
}