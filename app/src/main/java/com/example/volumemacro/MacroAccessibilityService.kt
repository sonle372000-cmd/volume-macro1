package com.example.volumemacro

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.AccessibilityServiceInfo
import android.accessibilityservice.GestureDescription
import android.graphics.Path
import android.os.Handler
import android.os.Looper
import android.view.KeyEvent
import android.view.accessibility.AccessibilityEvent

/**
 * Dịch vụ Trợ năng: bắt sự kiện phím Volume Down.
 * Nếu phím được GIỮ đủ lâu (holdMs, mặc định 500ms) thì thực hiện một cú chạm (tap)
 * tại tọa độ (x, y) đã lưu trong SharedPreferences.
 */
class MacroAccessibilityService : AccessibilityService() {

    private val handler = Handler(Looper.getMainLooper())
    private var pendingTrigger: Runnable? = null
    private var isKeyDown = false

    override fun onServiceConnected() {
        super.onServiceConnected()
        serviceInfo = AccessibilityServiceInfo().apply {
            eventTypes = AccessibilityEvent.TYPES_ALL_MASK
            feedbackType = AccessibilityServiceInfo.FEEDBACK_GENERIC
            flags = AccessibilityServiceInfo.FLAG_REQUEST_FILTER_KEY_EVENTS
            notificationTimeout = 100
        }
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        // Không cần xử lý sự kiện màn hình cho tính năng này.
    }

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
                    val runnable = Runnable { performTap() }
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

        // Trả về true để CHẶN hành vi giảm âm lượng mặc định khi giữ phím.
        // Nếu muốn âm lượng vẫn thay đổi bình thường, đổi dòng dưới thành:
        // return super.onKeyEvent(event)
        return true
    }

    private fun performTap() {
        val prefs = getSharedPreferences(MainActivity.PREFS_NAME, MODE_PRIVATE)
        val x = prefs.getInt(MainActivity.KEY_X, 500).toFloat()
        val y = prefs.getInt(MainActivity.KEY_Y, 800).toFloat()

        val path = Path().apply { moveTo(x, y) }
        val stroke = GestureDescription.StrokeDescription(path, 0, 50)
        val gesture = GestureDescription.Builder().addStroke(stroke).build()
        dispatchGesture(gesture, null, null)
    }
}
