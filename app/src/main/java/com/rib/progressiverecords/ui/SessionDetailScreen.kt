package com.rib.progressiverecords.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.viewmodel.compose.viewModel
import com.rib.progressiverecords.SessionViewModel
import com.rib.progressiverecords.model.Session
import kotlinx.coroutines.launch
import java.util.*

@Composable
fun SessionDetailScreen(
    id: UUID
) {
    Column {
        SetList(id)

        TextButton( onClick = { /*TODO*/ }) {
            Text(text = "Cancel and delete session", style = MaterialTheme.typography.h4, color = Color.Red)
        }
    }
}

@Composable
private fun SetList(
    id: UUID,
    viewModel: SessionViewModel = viewModel()
) {
    val coroutineScope = rememberCoroutineScope()

    val session = remember { mutableStateOf<Session?>(null) }

    LaunchedEffect (coroutineScope){
        session.let{ viewModel.getSession(id) }
    }
}

@Composable
private fun SetItem() {

}