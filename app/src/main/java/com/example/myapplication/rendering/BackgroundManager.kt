package com.example.myapplication.rendering

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.RectF
import kotlin.math.*

class BackgroundManager(private val context: Context) {
    
    // Background state
    private var backgroundBitmap: Bitmap? = null
    private var scaledBackgroundBitmap: Bitmap? = null
    private var backgroundScrollX = 0f
    private var backgroundScrollY = 0f
    private var backgroundSpeed = 0.8f
    private var backgroundType = BackgroundScrollType.PARALLAX_HORIZONTAL
    
    // Screen dimensions - sẽ được set từ GameView
    private var screenWidth = 0
    private var screenHeight = 0
    
    enum class BackgroundScrollType {
        STATIC,                    // Ảnh tĩnh
        PARALLAX_HORIZONTAL,       // Cuộn ngang
        PARALLAX_VERTICAL,         // Cuộn dọc
        PARALLAX_DIAGONAL,         // Cuộn chéo
        ZOOM_IN_OUT,              // Phóng to/thu nhỏ
        ROTATING,                 // Xoay
        WAVE_DISTORTION          // Méo sóng
    }
    
    fun setScreenSize(width: Int, height: Int) {
        screenWidth = width
        screenHeight = height
        if (backgroundBitmap != null) {
            scaleBackgroundToScreen()
        }
    }
    
    fun setBackgroundImage(resourceId: Int, scrollType: BackgroundScrollType = BackgroundScrollType.PARALLAX_HORIZONTAL) {
        try {
            // Load ảnh từ resources
            val originalBitmap = BitmapFactory.decodeResource(context.resources, resourceId)
            backgroundBitmap = originalBitmap
            backgroundType = scrollType

            // Scale ảnh để phù hợp với màn hình
            scaleBackgroundToScreen()

            // Reset scroll position
            backgroundScrollX = 0f
            backgroundScrollY = 0f
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    
    fun setBackgroundImageFromAssets(fileName: String, scrollType: BackgroundScrollType = BackgroundScrollType.PARALLAX_HORIZONTAL) {
        try {
            val inputStream = context.assets.open(fileName)
            val originalBitmap = BitmapFactory.decodeStream(inputStream)
            backgroundBitmap = originalBitmap
            backgroundType = scrollType
            scaleBackgroundToScreen()
            backgroundScrollX = 0f
            backgroundScrollY = 0f
            inputStream.close()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    
    private fun scaleBackgroundToScreen() {
        backgroundBitmap?.let { bitmap ->
            // Kiểm tra kích thước màn hình đã được thiết lập chưa
            if (screenWidth <= 0 || screenHeight <= 0) return
            
            // Tính tỷ lệ scale để ảnh che phủ toàn màn hình
            val scaleX = screenWidth.toFloat() / bitmap.width
            val scaleY = screenHeight.toFloat() / bitmap.height
            val scale = maxOf(scaleX, scaleY) * 1.5f // 1.5x để có thể scroll

            val newWidth = (bitmap.width * scale).toInt()
            val newHeight = (bitmap.height * scale).toInt()

            scaledBackgroundBitmap = Bitmap.createScaledBitmap(bitmap, newWidth, newHeight, true)
        }
    }
    
    fun updateAnimation(): Boolean {
        if (scaledBackgroundBitmap != null) {
            when (backgroundType) {
                BackgroundScrollType.PARALLAX_HORIZONTAL,
                BackgroundScrollType.PARALLAX_VERTICAL,
                BackgroundScrollType.PARALLAX_DIAGONAL -> {
                    // Animation tự động trong drawParallaxBackground()
                    return true
                }
                BackgroundScrollType.ZOOM_IN_OUT,
                BackgroundScrollType.ROTATING,
                BackgroundScrollType.WAVE_DISTORTION -> {
                    // Animation dựa trên animationTime
                    return true
                }
                else -> return false
            }
        }
        return false
    }
    
    fun drawBackground(canvas: Canvas, animationTime: Float) {
        if (scaledBackgroundBitmap != null) {
            when (backgroundType) {
                BackgroundScrollType.STATIC -> {
                    scaledBackgroundBitmap?.let { bitmap ->
                        canvas.drawBitmap(bitmap, 0f, 0f, null)
                    }
                }
                BackgroundScrollType.PARALLAX_HORIZONTAL,
                BackgroundScrollType.PARALLAX_VERTICAL,
                BackgroundScrollType.PARALLAX_DIAGONAL -> {
                    drawParallaxBackground(canvas)
                }
                BackgroundScrollType.ZOOM_IN_OUT,
                BackgroundScrollType.ROTATING -> {
                    drawAnimatedBackground(canvas, animationTime)
                }
                BackgroundScrollType.WAVE_DISTORTION -> {
                    drawWaveDistortionBackground(canvas, animationTime)
                }
            }

            // Overlay tối để game board nổi bật hơn
            val overlayPaint = Paint().apply {
                color = Color.BLACK
                alpha = 100  // Semi-transparent
            }
            canvas.drawRect(0f, 0f, screenWidth.toFloat(), screenHeight.toFloat(), overlayPaint)
        } else {
            // Fallback solid color nếu không có ảnh
            canvas.drawColor(Color.parseColor("#2C3E50"))
        }
    }
    
    private fun drawParallaxBackground(canvas: Canvas) {
        scaledBackgroundBitmap?.let { bitmap ->
            when (backgroundType) {
                BackgroundScrollType.PARALLAX_HORIZONTAL -> {
                    backgroundScrollX -= backgroundSpeed
                    if (backgroundScrollX <= -bitmap.width + screenWidth) {
                        backgroundScrollX = 0f
                    }
                    canvas.drawBitmap(bitmap, backgroundScrollX, 0f, null)
                    canvas.drawBitmap(bitmap, backgroundScrollX + bitmap.width, 0f, null)
                }

                BackgroundScrollType.PARALLAX_VERTICAL -> {
                    backgroundScrollY -= backgroundSpeed
                    if (backgroundScrollY <= -bitmap.height + screenHeight) {
                        backgroundScrollY = 0f
                    }
                    canvas.drawBitmap(bitmap, 0f, backgroundScrollY, null)
                    canvas.drawBitmap(bitmap, 0f, backgroundScrollY + bitmap.height, null)
                }

                BackgroundScrollType.PARALLAX_DIAGONAL -> {
                    backgroundScrollX -= backgroundSpeed * 0.7f
                    backgroundScrollY -= backgroundSpeed * 0.5f

                    if (backgroundScrollX <= -bitmap.width + screenWidth) backgroundScrollX = 0f
                    if (backgroundScrollY <= -bitmap.height + screenHeight) backgroundScrollY = 0f

                    canvas.drawBitmap(bitmap, backgroundScrollX, backgroundScrollY, null)
                    canvas.drawBitmap(bitmap, backgroundScrollX + bitmap.width, backgroundScrollY, null)
                    canvas.drawBitmap(bitmap, backgroundScrollX, backgroundScrollY + bitmap.height, null)
                    canvas.drawBitmap(bitmap, backgroundScrollX + bitmap.width, backgroundScrollY + bitmap.height, null)
                }
                else -> { }
            }
        }
    }
    
    private fun drawAnimatedBackground(canvas: Canvas, animationTime: Float) {
        scaledBackgroundBitmap?.let { bitmap ->
            when (backgroundType) {
                BackgroundScrollType.ZOOM_IN_OUT -> {
                    canvas.save()

                    // Zoom oscillation
                    val time = animationTime * 0.001f
                    val zoomScale: Float = 1f + sin(time * 0.5f) * 0.2f

                    val centerX = screenWidth / 2f
                    val centerY = screenHeight / 2f

                    canvas.scale(zoomScale, zoomScale, centerX, centerY)

                    // Center the image
                    val drawX = centerX - bitmap.width / 2f
                    val drawY = centerY - bitmap.height / 2f
                    canvas.drawBitmap(bitmap, drawX, drawY, null)

                    canvas.restore()
                }

                BackgroundScrollType.ROTATING -> {
                    canvas.save()

                    // Slow rotation
                    val rotation = animationTime * 0.01f % 360f
                    val centerX = screenWidth / 2f
                    val centerY = screenHeight / 2f

                    canvas.rotate(rotation, centerX, centerY)

                    val drawX = centerX - bitmap.width / 2f
                    val drawY = centerY - bitmap.height / 2f
                    canvas.drawBitmap(bitmap, drawX, drawY, null)

                    canvas.restore()
                }

                else -> { }
            }
        }
    }
    
    private fun drawWaveDistortionBackground(canvas: Canvas, animationTime: Float) {
        scaledBackgroundBitmap?.let { bitmap ->
            if (backgroundType == BackgroundScrollType.WAVE_DISTORTION) {
                val waveAmplitude = 20f
                val waveFrequency = 0.01f
                val time = animationTime * 0.001f

                // Tạo bitmap biến dạng sóng
                val distortedBitmap = createWaveDistortedBitmap(bitmap, waveAmplitude, waveFrequency, time)
                canvas.drawBitmap(distortedBitmap, 0f, 0f, null)
            }
        }
    }
    
    private fun createWaveDistortedBitmap(originalBitmap: Bitmap, amplitude: Float, frequency: Float, time: Float): Bitmap {
        val width = originalBitmap.width
        val height = originalBitmap.height
        val distortedBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(distortedBitmap)

        val paint = Paint().apply {
            isAntiAlias = true
            isFilterBitmap = true
        }

        // Vẽ từng strip với offset sóng
        val stripHeight = 2
        for (y in 0 until height step stripHeight) {
            val waveOffset = amplitude * sin(y * frequency + time)
            val srcRect = Rect(0, y, width, min(y + stripHeight, height))
            val dstRect = RectF(waveOffset, y.toFloat(), width + waveOffset, min(y + stripHeight, height).toFloat())
            canvas.drawBitmap(originalBitmap, srcRect, dstRect, paint)
        }

        return distortedBitmap
    }
    
    // Getter/Setter methods
    fun setBackgroundSpeed(speed: Float) {
        backgroundSpeed = speed
    }
    
    fun setBackgroundScrollType(type: BackgroundScrollType) {
        backgroundType = type
    }
    
    fun pauseBackgroundAnimation() {
        backgroundSpeed = 0f
    }
    
    fun resumeBackgroundAnimation(speed: Float = 0.5f) {
        backgroundSpeed = speed
    }
    
    fun hasBackground(): Boolean = scaledBackgroundBitmap != null
    
    // ===== DEBUG & UTILITY =====
    
    /**
     * 📊 Lấy thông tin debug về background
     * 
     * @return String chứa info về background hiện tại
     */
    fun getDebugInfo(): String {
        return if (hasBackground()) {
            val original = backgroundBitmap
            val scaled = scaledBackgroundBitmap
            """Background Info:
            |Type: $backgroundType
            |Speed: $backgroundSpeed
            |Original: ${original?.width}x${original?.height}
            |Scaled: ${scaled?.width}x${scaled?.height}
            |Screen: ${screenWidth}x${screenHeight}
            |Scroll: ($backgroundScrollX, $backgroundScrollY)""".trimMargin()
        } else {
            "No background loaded"
        }
    }
    
    /**
     * 🧹 Clean up resources
     * 
     * Giải phóng memory khi không cần background nữa.
     * Gọi khi Activity/Fragment bị destroy.
     */
    fun cleanup() {
        backgroundBitmap?.recycle()
        scaledBackgroundBitmap?.recycle()
        backgroundBitmap = null
        scaledBackgroundBitmap = null
    }
}
