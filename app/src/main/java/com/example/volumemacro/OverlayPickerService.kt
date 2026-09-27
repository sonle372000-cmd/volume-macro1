package com.example.volumemacro

import android.app.Service
import android.content.Intent
import android.graphics.Color
import android.graphics.PixelFormat
import android.os.Build
import android.os.IBinder
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import android.widget.Button

/**
 * Hiển thị một chấm tròn nổi (kéo được) cùng nút "Xác nhận điểm".
 * Khi người dùng bấm Xác nhận, tọa độ tâm chấm sẽ được lưu vào SharedPreferences
 * để MacroAccessibilityService dùng làm điểm chạm (tap).
 */
class OverlayPickerService : Service() {

    private lateinit var windowManager: WindowManager
    private var markerView: View? = null
    private var confirmView: View? = null
    private lateinit var markerParams: WindowManager.LayoutParams

    private val markerSizePx = 70

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        windowManager = getSystemService(WINDOW_SERVICE) as WindowManager
        addMarker()
        addConfirmButton()
    }

    private fun overlayType(): Int =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
        } else {
            @Suppress("DEPRECATION")
            WindowManager.LayoutParams.TYPE_PHONE
        }

    private fun addMarker() {
        val marker = View(this).apply {
            setBackgroundColor(Color.parseColor("#CC2196F3"))
        }
        markerView = marker

        val savedX = getSharedPreferences(MainActivity.PREFS_NAME, MODE_PRIVATE).getInt(MainActivity.KEY_X, 400)
        val savedY = getSharedPreferences(MainActivity.PREFS_NAME, MODE_PRIVATE).getInt(MainActivity.KEY_Y, 800)

        markerParams = WindowManager.LayoutParams(
            markerSizePx, markerSizePx,
            overlayType(),
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP or Gravity.START
            x = (savedX - markerSizePx / 2).coerceAtLeast(0)
            y = (savedY - markerSizePx / 2).coerceAtLeast(0)
        }

        var touchStartRawX = 0f
        var touchStartRawY = 0f
        var initialX = 0
        var initialY = 0

        marker.setOnTouchListener { _, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    initialX = markerParams.x
                    initialY = markerParams.y
                    touchStartRawX = event.rawX
                    touchStartRawY = event.rawY
                    true
                }
                MotionEvent.ACTION_MOVE -> {
                    markerParams.x = initialX + (event.rawX - touchStartRawX).toInt()
                    markerParams.y = initialY + (event.rawY - touchStartRawY).toInt()
                    runCatching { windowManager.updateViewLayout(markerView, markerParams) }
                    true
                }
                else -> false
            }
        }

        windowManager.addView(marker, markerParams)
    }

    private fun addConfirmButton() {
        val button = Button(this).apply {
            text = "Xác nhận điểm"
            setOnClickListener { confirmPoint() }
        }
        confirmView = button

        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            overlayType(),
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.BOTTOM or Gravity.CENTER_HORIZONTAL
            y = 120
        }

        windowManager.addView(button, params)
    }

    private fun confirmPoint() {
        val centerX = markerParams.x + markerSizePx / 2
        val centerY = markerParams.y + markerSizePx / 2

        getSharedPreferences(MainActivity.PREFS_NAME, MODE_PRIVATE)
            .edit()
            .putInt(MainActivity.KEY_X, centerX)
            .putInt(MainActivity.KEY_Y, centerY)
            .apply()

        sendBroadcast(
            Intent(MainActivity.ACTION_POINT_PICKED)
                .setPackage(packageName)
                .putExtra(MainActivity.EXTRA_X, centerX)
                .putExtra(MainActivity.EXTRA_Y, centerY)
        )

        stopSelf()
    }

    override fun onDestroy() {
        super.onDestroy()
        markerView?.let { runCatching { windowManager.removeView(it) } }
        confirmView?.let { runCatching { windowManager.removeView(it) } }
    }
}
