package com.rib.progressiverecords.ui

import android.text.format.DateFormat
import android.util.Log
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavController
import com.rib.progressiverecords.ExerciseSetsList
import com.rib.progressiverecords.R
import com.rib.progressiverecords.SessionViewModel
import com.rib.progressiverecords.model.Record
import com.rib.progressiverecords.model.Session
import com.rib.progressiverecords.model.relations.SessionWithRecords
import kotlinx.coroutines.launch

@Composable
fun SessionListScreen(
    viewModel: SessionViewModel,
    navController: NavController
) {
    Scaffold(
        topBar = { TopBar(
            onClick = {
                navController.navigate("session_creation")
            },
            icon = painterResource(R.drawable.ic_add),
            contentDescription = stringResource(R.string.create_session_icon_description)
        ) }
    ) { it
        SessionList(viewModel = viewModel, navController = navController)
    }
    BackHandler {}
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
                        viewModel.detailedSession = it
                        navController.navigate("session_detail")
                    }
                )
            }

            item {
                Spacer(modifier = Modifier.height(56.dp))
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

    val exerciseSetsList = ExerciseSetsList().organizeRecords(records).totalSets

    Card (
        modifier = Modifier
            .padding(16.dp)
            .fillMaxWidth()
            .clickable { onSelectSession(session) },
        shape = RoundedCornerShape(16.dp),
        backgroundColor = MaterialTheme.colors.primary,
        elevation = 4.dp
    ) {
        Column (
            modifier = Modifier
                .padding(8.dp)
                ) {

            Row {
                Text(
                    modifier = Modifier.padding(8.dp),
                    text = session.session.sessionName,
                    style = MaterialTheme.typography.h6
                )

                Spacer(modifier = Modifier.weight(1f))

                Text(
                    modifier = Modifier.padding(8.dp),
                    text = DateFormat.format("dd / MMM / yyyy", session.session.date).toString(),
                    style = MaterialTheme.typography.body1
                )
            }

            Divider()

            exerciseSetsList.forEach {set ->
                if (set.isNotEmpty()) {
                    val lastSet = set[set.lastIndex]
                    Row {
                        Text(
                            text = lastSet.exerciseName,
                            style = MaterialTheme.typography.body1,
                            color = MaterialTheme.colors.onPrimary,
                            modifier = Modifier.padding(4.dp)
                        )

                        Spacer(modifier = Modifier.weight(1f))

                        Text(
                            text = stringResource(R.string.session_list_exercise_item, lastSet.setNumber),
                            style = MaterialTheme.typography.body1,
                            color = MaterialTheme.colors.onPrimary,
                            modifier = Modifier.padding(4.dp)
                        )
                    }
                }
            }
        }
    }
}