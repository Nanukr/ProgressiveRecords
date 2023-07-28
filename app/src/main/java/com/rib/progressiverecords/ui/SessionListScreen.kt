package com.rib.progressiverecords.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.navArgument
import com.rib.progressiverecords.SessionViewModel
import com.rib.progressiverecords.model.Record
import com.rib.progressiverecords.model.relations.SessionWithRecords
import java.util.*

@Composable
fun SessionListScreen(
    viewModel: SessionViewModel,
    navController: NavController
) {
    Scaffold(
        topBar = { TopBar(
            onClick = {
                viewModel.changeDetailedSession(null)
                navController.navigate("session_detail")
            }
        ) }
    ) { it
        SessionList(viewModel = viewModel, navController = navController)
    }
}

@Composable
fun SessionList(
    viewModel: SessionViewModel,
    navController: NavController
) {
    val sessions = viewModel.sessions.collectAsState()

    if (sessions.value.isEmpty()) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text(text = "No recorded sessions")
        }
    } else {
        LazyColumn(
            modifier = Modifier.padding(8.dp)
        ) {
            items(sessions.value) {session ->
                SessionItem(
                    session = session,
                    onSelectSession = {
                        viewModel.changeDetailedSession(it)
                        navController.navigate("session_detail")
                    }
                )
            }
        }
    }
}

@Composable
private fun SessionItem(
    session: SessionWithRecords,
    onSelectSession: (SessionWithRecords) -> Unit
) {
    var records by remember { mutableStateOf((session.records)) }
    records = records.sortedWith(
        compareBy<Record> { it.exerciseName }
            .thenBy { it.setNumber }
    )

    Column (
        modifier = Modifier
            .padding(16.dp)
            .fillMaxWidth()
            .clickable { onSelectSession(session) }
    ) {
        Text(
            text = session.session.sessionName,
            style = MaterialTheme.typography.h5
        )

        records.forEach {record ->
            val exerciseName = record.exerciseName
            Row (
                modifier = Modifier.padding(4.dp)
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