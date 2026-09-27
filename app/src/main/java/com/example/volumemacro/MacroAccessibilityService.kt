package com.example.volumemacro

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.AccessibilityServiceInfo
import android.accessibilityservice.GestureDescription
import android.graphics.Path
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.view.KeyEvent
import android.view.accessibility.AccessibilityEvent
import android.widget.Toast

class MacroAccessibilityService : AccessibilityService() {

    private val handler = Handler(Looper.getMainLooper())
    private var pendingTrigger: Runnable? = null
    private var isKeyDown = false

    companion object {
        var instance: MacroAccessibilityService? = null
            private set
    }

    override fun onServiceConnected() {
        super.onServiceConnected()
        instance = this
        serviceInfo = AccessibilityServiceInfo().apply {
            eventTypes = AccessibilityEvent.TYPES_ALL_MASK
            feedbackType = AccessibilityServiceInfo.FEEDBACK_GENERIC
            flags = AccessibilityServiceInfo.FLAG_REQUEST_FILTER_KEY_EVENTS
            notificationTimeout = 100
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (instance === this) instance = null
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {}

    override fun onInterrupt() {}

    override fun onKeyEvent(event: KeyEvent): Boolean {
        if (event.keyCode != KeyEvent.KEYCODE_VOLUME_DOWN) {
            return super.onKeyEvent(event)
        }

        val prefs = getSharedPreferences(MainActivity.PREFS_NAME, MODE_PRIVATE)
        val holdMs = prefs.getInt(MainActivity.KEY_HOLD_MS, 500).toLong()

        when (event.action) {
            KeyEvent.ACTION_DOWN -> {
                if (!isKeyDown) {
                    isKeyDown = true
                    val runnable = Runnable { performTapNow() }
                    pendingTrigger = runnable
                    handler.postDelayed(runnable, holdMs)
                }
            }
            KeyEvent.ACTION_UP -> {
                isKeyDown = false
                pendingTrigger?.let { handler.removeCallbacks(it) }
                pendingTrigger = null
            }
        }

        return true
    }

    fun performTapNow() {
        val prefs = getSharedPreferences(MainActivity.PREFS_NAME, MODE_PRIVATE)
        val x = prefs.getInt(MainActivity.KEY_X, 500)
        val y = prefs.getInt(MainActivity.KEY_Y, 800)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            val path = Path().apply { moveTo(x.toFloat(), y.toFloat()) }
            val stroke = GestureDescription.StrokeDescription(path, 0, 50)
            val gesture = GestureDescription.Builder().addStroke(stroke).build()
            dispatchGesture(gesture, null, null)
        } else {
            tapViaRootOrWarn(x, y)
        }
    }

    private fun tapViaRootOrWarn(x: Int, y: Int) {
        Thread {
            try {
                val process = Runtime.getRuntime().exec(arrayOf("su", "-c", "input tap $x $y"))
                val exitCode = process.waitFor()
                if (exitCode != 0) {
                    notifyNoRoot()
                }
            } catch (e: Exception) {
                notifyNoRoot()
            }
        }.start()
    }

    private fun notifyNoRoot() {
        Handler(Looper.getMainLooper()).post {
            Toast.makeText(
                applicationContext,
                "Máy đang chạy Android 6, cần quyền ROOT để giả lập chạm màn hình nhưng không lấy được quyền root.",
                Toast.LENGTH_LONG
            ).show()
        }
    }
}
