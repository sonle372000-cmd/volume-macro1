package com.example.volumemacro

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.widget.Toast

class TriggerActivity : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val service = MacroAccessibilityService.instance
        if (service == null) {
            Toast.makeText(
                this,
                "Chưa bật Dịch vụ Trợ năng cho Volume Macro. Vui lòng bật trước rồi thử lại.",
                Toast.LENGTH_LONG
            ).show()
            startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))
            finish()
            return
        }

        moveTaskToBack(true)

        Handler(Looper.getMainLooper()).postDelayed({
            service.performTapNow()
            finish()
        }, 200)
    }
}
