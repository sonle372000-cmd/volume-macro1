package com.example.volumemacro

import android.app.Activity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.Toast

class TriggerActivity : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val prefs = getSharedPreferences(MainActivity.PREFS_NAME, MODE_PRIVATE)
        val x = prefs.getInt(MainActivity.KEY_X, 500)
        val y = prefs.getInt(MainActivity.KEY_Y, 800)

        moveTaskToBack(true)

        Thread {
            val success = tapViaRoot(x, y)
            if (!success) {
                Handler(Looper.getMainLooper()).post {
                    Toast.makeText(
                        applicationContext,
                        "Không chạm được: cần quyền ROOT nhưng không lấy được quyền root trên máy này.",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
            Handler(Looper.getMainLooper()).post { finish() }
        }.start()
    }

    private fun tapViaRoot(x: Int, y: Int): Boolean {
        return try {
            val process = Runtime.getRuntime().exec(arrayOf("su", "-c", "input tap $x $y"))
            val exitCode = process.waitFor()
            exitCode == 0
        } catch (e: Exception) {
            false
        }
    }
}
