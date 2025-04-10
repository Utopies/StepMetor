package com.example.walkingmetor.StepMetor

import StepCounterState
import android.Manifest
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.example.walkingmetor.R
import java.time.LocalDate

class StepCounterService : Service(),  SensorEventListener{
    private lateinit var sensorManager: SensorManager
    private var stepSensor: Sensor? = null
    private var prefs: SharedPreferences = getSharedPreferences("StepPrefs", Context.MODE_PRIVATE)
    private var state: StepCounterState = StepCounterState(LocalDate.now(), 0).apply {
        loadData(prefs, LocalDate.now())
    }

    fun state() : StepCounterState {
        return state
    }

    override fun onCreate() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q &&
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACTIVITY_RECOGNITION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            stopSelf()
            return
        }

        super.onCreate()

        //Инициализация полей
        sensorManager = getSystemService(SENSOR_SERVICE) as SensorManager
        stepSensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER)

        sensorManager.registerListener(
            this,
            stepSensor,
            SensorManager.SENSOR_DELAY_NORMAL
        )

        val notification = NotificationCompat.Builder(this, "step_channel")
            .setContentTitle("Счетчик шагов")
            .setSmallIcon(R.drawable.ic_walk)
            .build()

        startForeground(1, notification)
    }

    override fun onSensorChanged(event: SensorEvent?) {
        event?.let {
            if (it.sensor.type == Sensor.TYPE_STEP_COUNTER) {
                if(state.initialSteps == -1)
                    state.initialSteps = it.values[0].toInt()
                val today = LocalDate.now()

                if (state.date != today) {
                    // Сохраняем предыдущий день
                    state.saveData(prefs)
                    // Сброс для новой даты
                    state.resetCounter(prefs, today)
                    state.date = today
                    state.steps = it.values[0].toInt() // Начинаем с текущего значения
                } else {
                    // Обновляем разницу между текущим и начальным значением
                    state.steps = it.values[0].toInt() - state.initialSteps
                }

                state.saveData(prefs)
            }
        }
    }

    override fun onDestroy() {
        state.saveData(prefs)
        sensorManager.unregisterListener(this)
        super.onDestroy()
    }


    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }
}