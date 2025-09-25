package com.example.myapplication.systems

import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Color
import com.example.myapplication.entities.ExplosionParticle
import kotlin.random.Random

class ParticleSystem {
    private val particles = mutableListOf<ExplosionParticle>()
    private val paint = Paint().apply {
        isAntiAlias = true
        style = Paint.Style.FILL
    }
    private val maxParticles = 100 // Giới hạn số particle để tránh lag

    fun createExplosion(centerX: Float, centerY: Float){
        println("🎆 Creating explosion at (${centerX.toInt()}, ${centerY.toInt()})")
        if (particles.size > maxParticles - 12) {
            println("🎆 Too many particles (${particles.size}), skipping explosion")
            return // Không tạo thêm nếu quá nhiều
        }
        //tao 12 particles bay ra cac huong
        println("🎆 Creating 12 particles for explosion")
        for (i in 0 .. 15){
            val angle = i*30f *Math.PI.toFloat()/180f
            val speed = 150f+ Random.nextFloat()*200f //random speed

            //random color
            val colors = arrayOf(
                Color.rgb(255, 150, 0),   // Orange
                Color.rgb(255, 0, 0),     // Red
                Color.rgb(255, 255, 0),   // Yellow
                Color.rgb(255, 100, 0)    // Dark orange
            )
            val color = colors[Random.nextInt(colors.size)]

            particles.add(ExplosionParticle(
                x = centerX,
                y = centerY,
                vx = kotlin.math.cos(angle) * speed,
                vy = kotlin.math.sin(angle) * speed,
                color = color
            ))
        }
    }

    // cap nhat tat ca particles
    fun update(deltaTime: Float){
        particles.forEach { it.update(deltaTime) }
        particles.removeAll { it.isDead() }
    }

    // Vẽ tất cả particles
    fun draw(canvas: Canvas) {
        particles.forEach { particle ->
            paint.color = Color.argb(
                particle.getAlpha(),
                Color.red(particle.color),
                Color.green(particle.color),
                Color.blue(particle.color)
            )

            val currentSize = particle.getCurrentSize()
            canvas.drawCircle(particle.x, particle.y, currentSize, paint)
        }
    }

    // Xóa tất cả particles (khi reset level)
    fun clear() {
        particles.clear()
    }

    // Lấy số particles hiện tại (debug)
    fun getParticleCount() = particles.size
}