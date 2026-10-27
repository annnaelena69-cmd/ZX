package com.nexusx6.launcher

import android.app.Service
import android.content.Intent
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.PixelFormat
import android.graphics.Color as AndroidColor
import android.os.IBinder
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import android.widget.ScrollView
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.content.Context
import android.view.ViewGroup

/**
 * User-enabled in-game overlay. Tap the floating N bubble to open quick settings.
 * Overlay controls are intentionally simple and do not hook or modify game memory.
 */
class CrosshairService : Service() {
    private var windowManager: WindowManager? = null
    private var crosshairView: CrosshairView? = null
    private var bubble: TextView? = null
    private var menu: View? = null
    private var bubbleParams: WindowManager.LayoutParams? = null
    private var menuParams: WindowManager.LayoutParams? = null
    private var shape = "Plus"
    private var colorName = "Cyan"
    private var size = 1
    private var smoothPreset = "Balanced"
    private var clarityPreset = "Balanced"
    private var crosshairEnabled = true

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        shape = intent?.getStringExtra("shape") ?: shape
        colorName = intent?.getStringExtra("color") ?: colorName
        size = (intent?.getIntExtra("size", size) ?: size).coerceIn(1, 4)
        smoothPreset = intent?.getStringExtra("smoothPreset") ?: smoothPreset
        windowManager = getSystemService(WINDOW_SERVICE) as WindowManager
        if (crosshairView == null) {
            addCrosshair()
            addBubble()
        } else {
            refreshCrosshair()
            bubble?.text = "N"
        }
        return START_STICKY
    }

    private fun selectedColor(): Int = when (colorName) {
        "Lime" -> AndroidColor.rgb(165, 255, 77)
        "Pink" -> AndroidColor.rgb(255, 104, 203)
        "Red" -> AndroidColor.rgb(255, 82, 82)
        "White" -> AndroidColor.WHITE
        "Yellow" -> AndroidColor.rgb(255, 230, 80)
        "Purple" -> AndroidColor.rgb(190, 130, 255)
        else -> AndroidColor.rgb(98, 245, 229)
    }

    private fun addCrosshair() {
        crosshairView = CrosshairView(this, shape, selectedColor(), size)
        val params = WindowManager.LayoutParams(120, 120, WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE or WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
            PixelFormat.TRANSLUCENT).apply { gravity = Gravity.CENTER }
        try { windowManager?.addView(crosshairView, params) } catch (_: Exception) {}
    }

    private fun refreshCrosshair() {
        try {
            crosshairView?.shape = shape
            crosshairView?.color = selectedColor()
            crosshairView?.sizeScale = size
            crosshairView?.invalidate()
        } catch (_: Exception) {}
    }

    private fun addBubble() {
        val button = TextView(this).apply {
            text = "N"
            textSize = 16f
            setTextColor(AndroidColor.BLACK)
            typeface = Typeface.DEFAULT_BOLD
            gravity = Gravity.CENTER
            background = rounded(CYAN, 48f)
            elevation = 10f
            contentDescription = "Buka pengaturan cepat NEXUS"
        }
        val p = WindowManager.LayoutParams(dp(48), dp(48), WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
            PixelFormat.TRANSLUCENT).apply {
                gravity = Gravity.TOP or Gravity.END
                x = dp(14); y = dp(130)
            }
        button.setOnTouchListener(object : View.OnTouchListener {
            var startX = 0; var startY = 0; var downX = 0f; var downY = 0f; var moved = false
            override fun onTouch(v: View, e: MotionEvent): Boolean {
                when (e.action) {
                    MotionEvent.ACTION_DOWN -> {
                        startX = p.x; startY = p.y; downX = e.rawX; downY = e.rawY; moved = false
                        return true
                    }
                    MotionEvent.ACTION_MOVE -> {
                        val dx = (e.rawX - downX).toInt(); val dy = (e.rawY - downY).toInt()
                        if (kotlin.math.abs(dx) + kotlin.math.abs(dy) > dp(5)) moved = true
                        p.x = (startX - dx).coerceAtLeast(0); p.y = (startY + dy).coerceAtLeast(0)
                        try { windowManager?.updateViewLayout(button, p) } catch (_: Exception) {}
                        return true
                    }
                    MotionEvent.ACTION_UP -> {
                        if (!moved) toggleMenu()
                        return true
                    }
                }
                return false
            }
        })
        bubble = button
        bubbleParams = p
        try { windowManager?.addView(button, p) } catch (_: Exception) {}
    }

    private fun toggleMenu() {
        if (menu != null) { removeMenu(); return }
        val panel = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(12), dp(10), dp(12), dp(10))
            background = rounded(AndroidColor.rgb(15, 24, 38), 18f)
            elevation = 14f
        }
        val title = TextView(this).apply {
            text = "NEXUS QUICK SETTINGS"
            textSize = 13f; setTextColor(CYAN); typeface = Typeface.DEFAULT_BOLD
            setPadding(0, 0, 0, dp(6))
        }
        panel.addView(title)
        val summary = TextView(this).apply {
            text = "Crosshair: $shape • $colorName • ukuran $size"
            textSize = 11f; setTextColor(AndroidColor.WHITE)
        }
        panel.addView(summary)
        addMenuButton(panel, "Bentuk: $shape  ↻") {
            shape = next(shape, listOf("Plus", "Dot", "Circle", "X", "T", "Cross"))
            refreshCrosshair(); summary.text = "Crosshair: $shape • $colorName • ukuran $size"
        }
        addMenuButton(panel, "Warna: $colorName  ↻") {
            colorName = next(colorName, listOf("Cyan", "Lime", "Pink", "Red", "White", "Yellow", "Purple"))
            refreshCrosshair(); summary.text = "Crosshair: $shape • $colorName • ukuran $size"
        }
        addMenuButton(panel, "Ukuran: $size  − / +") {
            size = if (size >= 4) 1 else size + 1
            refreshCrosshair(); summary.text = "Crosshair: $shape • $colorName • ukuran $size"
        }
        addMenuButton(panel, "Smooth preset: $smoothPreset  ↻") {
            smoothPreset = next(smoothPreset, listOf("Balanced", "Smooth Feel", "Battery Saver"))
            summary.text = "Preset $smoothPreset dipilih. Lihat tips di bawah."
            Toast.makeText(this, smoothAdvice(), Toast.LENGTH_LONG).show()
        }
        addMenuButton(panel, "Crosshair: ${if (crosshairEnabled) "ON" else "OFF"}  ◉") {
            crosshairEnabled = !crosshairEnabled
            crosshairView?.visibility = if (crosshairEnabled) View.VISIBLE else View.GONE
            summary.text = "Crosshair ${if (crosshairEnabled) "aktif" else "disembunyikan"}"
        }
        addMenuButton(panel, "Visual preset: $clarityPreset  ↻") {
            clarityPreset = next(clarityPreset, listOf("Balanced", "Clarity", "Vivid", "Eye Care"))
            summary.text = "Preset visual: $clarityPreset"
            val advice = when (clarityPreset) {
                "Clarity" -> "Untuk gambar lebih jelas, coba sharpness/resolusi di menu game jika tersedia. Overlay tidak mengubah render game."
                "Vivid" -> "Coba mode warna vivid di pengaturan layar bila tersedia; cek agar warna tidak terlalu menyilaukan."
                "Eye Care" -> "Gunakan kenyamanan mata saat jeda dan sesuaikan kecerahan dengan ruangan."
                else -> "Utamakan FPS stabil dan suhu perangkat."
            }
            Toast.makeText(this, advice, Toast.LENGTH_LONG).show()
        }
        addMenuButton(panel, "Touch response tips") {
            Toast.makeText(this, "Atur sensitivitas dan ukuran tombol dari pengaturan kontrol di dalam game. Tes di Training Ground; perubahan ini tidak memaksa nilai game.", Toast.LENGTH_LONG).show()
        }
        addMenuButton(panel, "Network / memory tips") {
            Toast.makeText(this, "Tutup unduhan latar, gunakan koneksi stabil, sisakan ruang penyimpanan, dan hindari perangkat terlalu panas. Ping tidak bisa diturunkan oleh overlay.", Toast.LENGTH_LONG).show()
        }
        addMenuButton(panel, "Tips stabilkan frame") {
            Toast.makeText(this, smoothAdvice(), Toast.LENGTH_LONG).show()
        }
        addMenuButton(panel, "Tutup menu") { removeMenu() }

        val scroller = ScrollView(this).apply {
            isFillViewport = true
            isVerticalScrollBarEnabled = true
            addView(panel, ScrollView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT))
        }
        val maxHeightPx = (resources.displayMetrics.heightPixels * 0.58f).toInt()
        val p = WindowManager.LayoutParams(dp(260), maxHeightPx,
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
            PixelFormat.TRANSLUCENT).apply {
                gravity = Gravity.TOP or Gravity.END
                x = dp(10); y = dp(80)
            }
        menu = scroller
        menuParams = p
        try { windowManager?.addView(scroller, p) } catch (_: Exception) { menu = null; menuParams = null }
    }

    private fun addMenuButton(parent: LinearLayout, label: String, action: () -> Unit) {
        val button = Button(this).apply {
            text = label; textSize = 11f; isAllCaps = false
            setTextColor(AndroidColor.WHITE)
            background = rounded(AndroidColor.rgb(34, 51, 70), 10f)
            setOnClickListener { action() }
        }
        parent.addView(button, LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dp(42)).apply { topMargin = dp(4) })
    }

    private fun smoothAdvice(): String = when (smoothPreset) {
        "Smooth Feel" -> "Pilih refresh rate tertinggi yang tersedia, tutup aplikasi latar, matikan Battery Saver, jaga suhu, dan turunkan grafik jika FPS tidak stabil."
        "Battery Saver" -> "Mode hemat daya bisa menurunkan performa. Untuk stabilitas, kurangi efek grafis dan hindari perangkat terlalu panas."
        else -> "Pilih refresh rate tertinggi yang tersedia, gunakan grafik sesuai kestabilan FPS, tutup aplikasi latar, dan jaga suhu perangkat."
    }

    private fun removeMenu() {
        try { menu?.let { windowManager?.removeView(it) } } catch (_: Exception) {}
        menu = null; menuParams = null
    }

    private fun <T> next(current: T, values: List<T>): T {
        val i = values.indexOf(current)
        return values[(i + 1).mod(values.size)]
    }

    private fun rounded(color: Int, radius: Float) = GradientDrawable().apply {
        setColor(color); cornerRadius = dp(radius.toInt()).toFloat()
        setStroke(dp(1), AndroidColor.rgb(60, 83, 105))
    }
    private fun dp(v: Int): Int = (v * resources.displayMetrics.density).toInt()
    private fun dp(v: Float): Int = (v * resources.displayMetrics.density).toInt()
    override fun onDestroy() {
        removeMenu()
        try { bubble?.let { windowManager?.removeView(it) } } catch (_: Exception) {}
        try { crosshairView?.let { windowManager?.removeView(it) } } catch (_: Exception) {}
        bubble = null; crosshairView = null
        super.onDestroy()
    }
    override fun onBind(intent: Intent?): IBinder? = null

    companion object { private const val CYAN = 0xFF62F5E5.toInt() }
}

private class CrosshairView(context: Context, var shape: String, var color: Int, var sizeScale: Int) : View(context) {
    private val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        strokeWidth = 4f
        style = Paint.Style.STROKE
        strokeCap = Paint.Cap.ROUND
    }
    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        paint.color = color
        paint.strokeWidth = 2f + sizeScale
        paint.setShadowLayer(3f + sizeScale, 0f, 0f, color)
        val cx = width / 2f; val cy = height / 2f
        val r = 10f + sizeScale * 4f
        when (shape) {
            "Dot" -> { paint.style = Paint.Style.FILL; canvas.drawCircle(cx, cy, 2f + sizeScale, paint); paint.style = Paint.Style.STROKE }
            "Circle" -> canvas.drawCircle(cx, cy, r, paint)
            "X" -> { canvas.drawLine(cx - r, cy - r, cx + r, cy + r, paint); canvas.drawLine(cx + r, cy - r, cx - r, cy + r, paint) }
            "T" -> { canvas.drawLine(cx, cy - r, cx, cy + r, paint); canvas.drawLine(cx - r, cy - r, cx + r, cy - r, paint) }
            "Cross" -> { canvas.drawLine(cx, cy - r, cx, cy + r, paint); canvas.drawLine(cx - r, cy, cx + r, cy, paint) }
            else -> { canvas.drawLine(cx, cy - r, cx, cy + r, paint); canvas.drawLine(cx - r, cy, cx + r, cy, paint) }
        }
    }
}
