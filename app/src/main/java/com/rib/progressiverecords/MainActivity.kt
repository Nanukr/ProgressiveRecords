package com.rib.progressiverecords

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import com.rib.progressiverecords.ui.BuilderScreen
import com.rib.progressiverecords.ui.theme.ProgressiveRecordsTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val settings = StoreSettings(applicationContext)

        setTheme(R.style.Theme_ProgressiveRecords)
        setContent {
            ProgressiveRecordsTheme(darkTheme = settings.getThemeState.collectAsState(initial = false).value ?: false) {

                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colors.background
                ) {
                    BuilderScreen(settings = settings)
                }
            }
        }
    }
}