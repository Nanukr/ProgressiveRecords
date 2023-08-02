package com.rib.progressiverecords.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.rib.progressiverecords.R
import com.rib.progressiverecords.SessionViewModel
import com.rib.progressiverecords.model.Record
import com.rib.progressiverecords.model.relations.SessionWithRecords

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
            modifier = Modifier
                .fillMaxSize()
                .background(color = MaterialTheme.colors.background),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = stringResource(R.string.empty_session_list_message),
                color = MaterialTheme.colors.onPrimary
            )
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
            .background(color = MaterialTheme.colors.primary, RoundedCornerShape(16.dp))
            .fillMaxWidth()
            .clickable { onSelectSession(session) }
    ) {
        Column (
            modifier = Modifier
                .padding(16.dp)
                ) {
            Text(
                text = session.session.sessionName,
                style = MaterialTheme.typography.h5
            )

            records.forEach {record ->
                val exerciseName = record.exerciseName
                Text(
                    text = stringResource(R.string.session_list_item, exerciseName, record.repetitions),
                    style = MaterialTheme.typography.h6,
                    color = MaterialTheme.colors.onPrimary,
                    modifier = Modifier.padding(4.dp)
                )
            }
        }
    }
}