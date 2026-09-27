package com.example.volumemacro

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat

class MainActivity : AppCompatActivity() {

    private lateinit var prefs: android.content.SharedPreferences
    private lateinit var inputX: EditText
    private lateinit var inputY: EditText

    private val pointReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            val x = intent?.getIntExtra(EXTRA_X, -1) ?: -1
            val y = intent?.getIntExtra(EXTRA_Y, -1) ?: -1
            if (x >= 0 && y >= 0) {
                inputX.setText(x.toString())
                inputY.setText(y.toString())
                Toast.makeText(this@MainActivity, "Đã chọn điểm ($x, $y)", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE)

        inputX = findViewById(R.id.inputX)
        inputY = findViewById(R.id.inputY)

        inputX.setText(prefs.getInt(KEY_X, 500).toString())
        inputY.setText(prefs.getInt(KEY_Y, 800).toString())

        findViewById<Button>(R.id.btnSave).setOnClickListener {
            saveValues()
        }

        findViewById<Button>(R.id.btnPickPoint).setOnClickListener {
            requestOverlayAndPick()
        }

        val filter = IntentFilter(ACTION_POINT_PICKED)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.registerReceiver(this, pointReceiver, filter, ContextCompat.RECEIVER_NOT_EXPORTED)
        } else {
            @Suppress("UnspecifiedRegisterReceiverFlag")
            registerReceiver(pointReceiver, filter)
        }
    }

    override fun onResume() {
        super.onResume()
        inputX.setText(prefs.getInt(KEY_X, inputX.text.toString().toIntOrNull() ?: 500).toString())
        inputY.setText(prefs.getInt(KEY_Y, inputY.text.toString().toIntOrNull() ?: 800).toString())
    }

    override fun onDestroy() {
        super.onDestroy()
        runCatching { unregisterReceiver(pointReceiver) }
    }

    private fun saveValues() {
        val x = inputX.text.toString().toIntOrNull()
        val y = inputY.text.toString().toIntOrNull()
        if (x == null || y == null) {
            Toast.makeText(this, "Vui lòng nhập số hợp lệ", Toast.LENGTH_SHORT).show()
            return
        }
        prefs.edit()
            .putInt(KEY_X, x)
            .putInt(KEY_Y, y)
            .apply()
        Toast.makeText(this, "Đã lưu", Toast.LENGTH_SHORT).show()
    }

    private fun requestOverlayAndPick() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.canDrawOverlays(this)) {
            val intent = Intent(
                Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                Uri.parse("package:$packageName")
            )
            startActivity(intent)
            Toast.makeText(
                this,
                "Hãy cấp quyền 'Hiển thị trên các ứng dụng khác', sau đó bấm lại nút này",
                Toast.LENGTH_LONG
            ).show()
            return
        }
        saveValues()
        startService(Intent(this, OverlayPickerService::class.java))
        Toast.makeText(this, "Kéo chấm xanh tới vị trí cần chạm rồi bấm 'Xác nhận điểm'", Toast.LENGTH_LONG).show()
    }

    companion object {
        const val PREFS_NAME = "volume_macro_prefs"
        const val KEY_X = "x"
        const val KEY_Y = "y"
        const val ACTION_POINT_PICKED = "com.example.volumemacro.POINT_PICKED"
        const val EXTRA_X = "extra_x"
        const val EXTRA_Y = "extra_y"
    }
}
