package com.example.walkingmetor

import StepCounterService
import android.app.AlertDialog
import android.content.Context
import android.Manifest
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.lifecycleScope
import com.example.walkingmetor.R.layout.metor_win
import com.example.walkingmetor.R.layout.start_win
import com.example.walkingmetor.ui.theme.WalkingMetorTheme
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    val stepCounter = StepCounterService()
    val context: Context = this


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        EgdeToEdge();
        StartWin()
        checkOfPermission()

    }

    override fun onResume() {
        super.onResume()

        setContentView(metor_win)
    }

    override fun onStop() {
        super.onStop()
    }

    fun checkOfPermission(){
        while (stepCounter.start(context)){
            AlertDialog.Builder(this)
                .setTitle("Требуется разрешение")
                .setMessage("Для подсчёта шагов необходимо разрешение на отслеживание физической активности. Без него приложение может работать некорректно.")
                .setPositiveButton("OK") { _, _ ->
                    requestPermissions(
                        arrayOf(Manifest.permission.ACTIVITY_RECOGNITION),
                        1
                    )
                }
                .show()
        }
    }

    fun StartWin(){
        setContentView(start_win)
        lifecycleScope.launch {
            delay(30000)
        }
    }

    fun EgdeToEdge(){
        enableEdgeToEdge()
        setContent {
            WalkingMetorTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Greeting(
                        name = "Android",
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    WalkingMetorTheme {
        Greeting("Android")
    }
}