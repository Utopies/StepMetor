import android.Manifest
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.core.content.ContextCompat
import com.example.walkingmetor.StepMetor.IStepListener
import com.example.walkingmetor.StepMetor.StepCounterState
import java.time.LocalDate

class StepCounterService : SensorEventListener {
    private val listeners = mutableListOf<IStepListener>()
    private lateinit var sensorManager: SensorManager
    private var currentDate: LocalDate = LocalDate.now()
    private val _state = mutableStateOf(StepCounterState(currentDate, 0))
    val state: State<StepCounterState> = _state

    public var previousStepCount: Int
        public get() = TODO()
        private set(value){
            previousStepCount = value
            listeners.forEach { it.StepListener(previousStepCount)
            }
        }

    fun start(context: Context): Boolean {
        if (!hasStepCounterPermission(context)) {
            return false
        }

        sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
        val stepCounterSensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER)

        stepCounterSensor?.let {
            sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_NORMAL)
            return true
        }
        return false
    }

    fun stop() {
        sensorManager.unregisterListener(this)
    }

    override fun onSensorChanged(event: SensorEvent) {
        if (event.sensor.type == Sensor.TYPE_STEP_COUNTER) {
            val newStepCount = event.values[0].toInt()
            val date = LocalDate.now()

            if (date != currentDate) {
                // Новый день - сброс счётчика
                currentDate = date
                previousStepCount = newStepCount
                _state.value = StepCounterState(date, 0)
            } else {
                val steps = newStepCount - previousStepCount
                _state.value = StepCounterState(date, steps)
            }
        }
    }

    private fun hasStepCounterPermission(context: Context): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.ACTIVITY_RECOGNITION
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            true // Для версий ниже Android 10 разрешение не требуется
        }
    }

    fun subscribeListener(listener: IStepListener) {
        listeners.add(listener)
    }

    fun removeSubscribeListener(listener: IStepListener) {
        listeners.remove(listener)
    }

    override fun onAccuracyChanged(sensor: Sensor, accuracy: Int) {}
}