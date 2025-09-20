package com.example.myapplication.entities

enum class MonsterType {
    PATROL,
    CIRCLE,
    RANDOM,
    CHASE,
    STRAIGHT,
    BOUNCE  // Đi thẳng cho đến khi gặp chướng ngại vật thì chuyển hướng ngẫu nhiên
}

enum class MonsterDirection {
    UP, DOWN, LEFT, RIGHT
}

