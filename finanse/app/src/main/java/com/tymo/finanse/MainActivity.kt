package com.tymo.finanse

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import com.tymo.finanse.ui.AppRoot
import com.tymo.finanse.ui.AppTheme

class MainActivity : ComponentActivity() {
    private val vm: AppViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent { AppTheme { AppRoot(vm) } }
    }

    override fun onResume() {
        super.onResume()
        vm.refreshRecurring()
    }
}
