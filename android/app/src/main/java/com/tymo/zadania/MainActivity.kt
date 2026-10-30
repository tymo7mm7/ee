package com.tymo.zadania

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.tymo.zadania.ui.TaskScreen
import com.tymo.zadania.ui.ZadaniaTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ZadaniaTheme {
                TaskScreen()
            }
        }
    }
}
