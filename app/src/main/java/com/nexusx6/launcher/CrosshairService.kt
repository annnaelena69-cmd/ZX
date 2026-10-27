package com.nexusx6.launcher

import android.app.Service
import android.content.Intent
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.PixelFormat
import android.graphics.Color as AndroidColor
import android.os.IBinder
import android.view.Gravity
import android.view.View
import android.view.WindowManager

class CrosshairService : Service() {
    private var windowManager: WindowManager? = null
    private var crosshairView: CrosshairView? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val shape = intent?.getStringExtra("shape") ?: "Plus"
        val colorName = intent?.getStringExtra("color") ?: "Cyan"
        val color = when (colorName) {
            "Lime" -> AndroidColor.rgb(165, 255, 77)
            "Pink" -> AndroidColor.rgb(255, 104, 203)
            "Red" -> AndroidColor.rgb(255, 82, 82)
            "White" -> AndroidColor.WHITE
            else -> AndroidColor.rgb(98, 245, 229)
        }
        if (crosshairView != null) try { windowManager?.removeView(crosshairView) } catch (_: Exception) {}
        windowManager = getSystemService(WINDOW_SERVICE) as WindowManager
        crosshairView = CrosshairView(this, shape, color)
        val params = WindowManager.LayoutParams(100, 100, WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE or WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
            PixelFormat.TRANSLUCENT).apply { gravity = Gravity.CENTER }
        try { windowManager?.addView(crosshairView, params) } catch (_: Exception) {}
        return START_NOT_STICKY
    }

    override fun onDestroy() {
        try { if (crosshairView != null) windowManager?.removeView(crosshairView) } catch (_: Exception) {}
        crosshairView = null
        super.onDestroy()
    }
    override fun onBind(intent: Intent?): IBinder? = null
}

private class CrosshairView(context: android.content.Context, private val shape: String, private val color: Int) : View(context) {
    private val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        this.color = this@CrosshairView.color
        strokeWidth = 4f
        style = Paint.Style.STROKE
        strokeCap = Paint.Cap.ROUND
        setShadowLayer(4f, 0f, 0f, color)
    }
    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val cx = width / 2f; val cy = height / 2f
        when (shape) {
            "Dot" -> { paint.style = Paint.Style.FILL; canvas.drawCircle(cx, cy, 3f, paint) }
            "Circle" -> canvas.drawCircle(cx, cy, 13f, paint)
            "X" -> { canvas.drawLine(cx - 13, cy - 13, cx + 13, cy + 13, paint); canvas.drawLine(cx + 13, cy - 13, cx - 13, cy + 13, paint) }
            "T" -> { canvas.drawLine(cx, cy - 17, cx, cy + 15, paint); canvas.drawLine(cx - 15, cy - 17, cx + 15, cy - 17, paint) }
            else -> { canvas.drawLine(cx, cy - 17, cx, cy + 17, paint); canvas.drawLine(cx - 17, cy, cx + 17, cy, paint) }
        }
    }
}
