package com.example.myapplication.entities

data class ExplosionParticle (
    var x: Float,
    var y: Float,
    var vx: Float,
    var vy: Float,
    var life: Float = 5.0f, // Thời gian sống còn lại của hạt nổ
    var maxLife: Float = 5.0f, // Thời gian sống tối đa của hạt nổ
    var size: Float = 1f,
    var color: Int
){
    fun update(deltaTime: Float){
        x+= vx*deltaTime
        y+= vy*deltaTime
        vy+= 400f*deltaTime // Hiệu ứng trọng lực
        vx*=0.98f // Ma sát
        vy*=0.98f // Ma sát
        life-= deltaTime*2f
    }

    fun isDead() = life <=0f
    fun getAlpha() = (life/maxLife*255).toInt().coerceIn(0, 255)
    fun getCurrentSize() = size * life
}