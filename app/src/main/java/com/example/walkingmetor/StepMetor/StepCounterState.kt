import android.content.SharedPreferences
import java.time.LocalDate

data class StepCounterState(var date: LocalDate, var steps: Int, var initialSteps: Int = -1) {
    fun saveData(prefs: SharedPreferences) {
        with(prefs.edit()) {
            putInt("step_${date}", steps)
            putInt("initial_step", initialSteps)
            apply()
        }
    }

    fun loadData(prefs: SharedPreferences, date: LocalDate) {
        this.steps = prefs.getInt("step_${date}", 0)
        this.initialSteps = prefs.getInt("initial_step", 0)
        this.date = date
    }

    fun resetCounter(prefs: SharedPreferences, date: LocalDate) {
        with(prefs.edit()) {
            putInt("step_${date}", 0)
            putInt("initial_step", -1)
            apply()
        }
    }
}