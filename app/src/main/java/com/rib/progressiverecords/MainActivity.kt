package com.rib.progressiverecords

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import com.rib.progressiverecords.ui.BuilderScreen
import com.rib.progressiverecords.ui.theme.ProgressiveRecordsTheme
import java.util.*

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val settings = StoreSettings(applicationContext)

        setTheme(R.style.Theme_ProgressiveRecords)
        setContent {
            val storedLocale = settings.getLocale.collectAsState(initial = "en").value ?: "en"
            val storedTheme = settings.getThemeState.collectAsState(initial = false).value ?: false

            val newLocale = Locale(storedLocale)

            val configuration = LocalConfiguration.current
            val context = LocalContext.current
            val resources = context.resources

            configuration.setLocale(newLocale)
            resources.updateConfiguration(configuration, resources.displayMetrics)

            ProgressiveRecordsTheme(darkTheme = storedTheme) {

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