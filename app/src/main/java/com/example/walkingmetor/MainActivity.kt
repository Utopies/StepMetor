package com.example.walkingmetor

import android.app.AlertDialog
import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.hardware.Sensor
import android.hardware.SensorManager
import android.os.Build
import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.walkingmetor.R.layout.metor_win
import com.example.walkingmetor.StepMetor.StepCounterService
import java.time.LocalDate


class MainActivity : ComponentActivity() {
    private lateinit var prefs: SharedPreferences
    private lateinit var tv_goal : TextView
    private lateinit var tvStepCount: TextView
    private val prefListener = SharedPreferences.OnSharedPreferenceChangeListener { _, key ->
        if (key == "step_${LocalDate.now()}") {
            updateStepCount()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        prefs = getSharedPreferences("StepPrefs", Context.MODE_PRIVATE)

        if (!hasStepCounterSensor()) {
            showSensorError()
            return
        }

        if (!checkStepCounterPermission()) {
            requestStepCounterPermission()
            return
        }

        startStepCounterService()
    }

    override fun onResume() {
        super.onResume()
        setContentView(metor_win)

        tvStepCount = findViewById(R.id.tv_step_count)
        tv_goal = findViewById(R.id.tv_goal)

        prefs.registerOnSharedPreferenceChangeListener(prefListener)

        updateStepCount()
    }

    override fun onStop() {
        prefs.unregisterOnSharedPreferenceChangeListener(prefListener)
        super.onStop()
    }

    @SuppressLint("SetTextI18n")
    private fun updateStepCount() {
        val currentDate = LocalDate.now()
        val steps = prefs.getInt("step_${currentDate}", 0)
        tvStepCount.text = "Шаги: $steps"

        tv_goal.text = LocalDate.now().toString()
    }

    private fun hasStepCounterSensor(): Boolean {
        val sensorManager = getSystemService(SensorManager::class.java)
        return sensorManager?.getDefaultSensor(Sensor.TYPE_STEP_COUNTER) != null
    }

    private fun checkStepCounterPermission(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACTIVITY_RECOGNITION
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            true
        }
    }

    private fun requestStepCounterPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACTIVITY_RECOGNITION),
                0
            )
        }
    }

    private fun startStepCounterService() {
        val serviceIntent = Intent(this, StepCounterService::class.java)
        ContextCompat.startForegroundService(this, serviceIntent)
    }

    private fun showSensorError() {
        AlertDialog.Builder(this)
            .setTitle("Ошибка")
            .setMessage("Ваше устройство не поддерживает счетчик шагов")
            .setPositiveButton("OK") { _, _ ->
                stopService(Intent(this, StepCounterService::class.java))
                finish() }
            .show()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 0) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startStepCounterService()
            } else {
                Toast.makeText(this, "Разрешение необходимо для работы приложения", Toast.LENGTH_LONG).show()
                finish()
            }
        }
    }
}