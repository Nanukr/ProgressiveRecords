package com.rib.progressiverecords.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier

@Composable
fun ExerciseScreen() {
    Scaffold(
        topBar = {
            TopBar(
                onClick = { /*TODO*/ }
            )
        }
    ) { it
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text(text = "Exercise screen")
        }
    }

}