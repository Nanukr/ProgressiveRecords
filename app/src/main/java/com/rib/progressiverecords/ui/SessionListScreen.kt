package com.rib.progressiverecords.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.rib.progressiverecords.SessionViewModel
import com.rib.progressiverecords.model.relations.SessionWithRecords

@Composable
fun SessionListScreen(
    modifier: Modifier = Modifier
) {
    Scaffold(
        topBar = { TopBar(
            onClick = { /*TODO*/ }
        ) }
    ) { it
        SessionList()
    }
}

@Composable
fun SessionList(
    modifier: Modifier = Modifier,
    viewModel: SessionViewModel = viewModel()
) {
    val sessions = viewModel.sessions.collectAsState(initial = emptyList())

    if (sessions.value.isEmpty()) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text(text = "No recorded sessions")
        }
    } else {
        LazyColumn(
            modifier = modifier
        ) {
            items(sessions.value) {session ->
                SessionItem(session = session)
            }
        }
    }
}

@Composable
private fun SessionItem(
    modifier: Modifier = Modifier,
    session: SessionWithRecords
) {
    val records = session.records

    Column (
        modifier = modifier
            .padding(16.dp)
    ) {
        Text(
            text = session.session.sessionName,
            style = MaterialTheme.typography.h4
        )

        records.forEach {record ->
            val exerciseName = record.exerciseName
            Row (
                modifier = modifier
                    ) {
                Text(
                    text = "$exerciseName x ",
                    style = MaterialTheme.typography.h6
                )
                Text(
                    text = "${record.repetitions}",
                    style = MaterialTheme.typography.h6
                )
            }
        }
    }
}