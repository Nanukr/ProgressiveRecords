package com.rib.progressiverecords.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.rib.progressiverecords.SessionViewModel
import com.rib.progressiverecords.model.Record
import com.rib.progressiverecords.model.Session
import com.rib.progressiverecords.model.relations.SessionWithRecords
import java.util.*

@Composable
fun SessionDetailScreen(
    viewModel: SessionViewModel,
    navController: NavController
) {
    var session = viewModel.detailedSession.collectAsState().value

    if (session == null) {
        session = SessionWithRecords(Session(id = UUID.randomUUID(), date = Date(), sessionName = "New session"), emptyList())
        viewModel.changeDetailedSession(session)
    }

    val sessionId = session.session.id

    var records by rememberSaveable { mutableStateOf(listOf<Record>()) }

    Scaffold (
        topBar = { DetailedTopBar(onClick = { /*TODO*/ })}
            ) { it
        Column (
            modifier = Modifier.fillMaxSize()
        ) {
            SetList(sessionId = sessionId, records = records, onUpdateRecords = { records = records + it })

            Spacer(modifier = Modifier.weight(1f))

            Button(
                onClick = { cancelAndDelete(viewModel, navController) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                colors = ButtonDefaults.buttonColors(backgroundColor = Color.Red)
            ) {
                Text(text = "Cancel and discard changes", color = Color.White)
            }
        }
    }
}

@Composable
private fun DetailedTopBar (
    onClick: () -> Unit
) {
    TopAppBar(
        title = { Text(text="") },
        backgroundColor = Color.DarkGray,
        contentColor = Color.White,
        elevation = 5.dp,
        actions = {
            TextButton(onClick = { onClick() }) {
                Text(text = "Exit and save", color = Color.White)
            }
        }
    )
}

@Composable
private fun SetList(
    sessionId: UUID,
    records: List<Record>,
    onUpdateRecords: (Record) -> Unit
) {
    if (records.isEmpty()) {
        AddExerciseButton(
            sessionId = sessionId,
            onExerciseAdded = { onUpdateRecords(it) }
        )
    } else {
        LazyColumn {
            items(records) {record  ->
                SetItem(record)
            }
        }

        AddExerciseButton(sessionId = sessionId, onExerciseAdded = { onUpdateRecords(it) })
    }
}

@Composable
private fun SetItem(
    record: Record
) {
    var name by rememberSaveable(stateSaver = TextFieldValue.Saver) {
        mutableStateOf(TextFieldValue(text = record.exerciseName))
    }

    var repetitions by rememberSaveable(stateSaver = TextFieldValue.Saver) {
        mutableStateOf(TextFieldValue(text = record.repetitions.toString()))
    }

    var weight by rememberSaveable(stateSaver = TextFieldValue.Saver) {
        mutableStateOf(TextFieldValue(text = record.weight.toString()))
    }

    Row (
        modifier = Modifier
            .padding(8.dp)
            .fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
            ) {
        Column (
            modifier = Modifier
                .weight(2f)
                .fillMaxHeight()
                .padding(4.dp)
                ) {
            Text(text = "Exercise name")
            TextField(
                value = name,
                onValueChange = { name = it },
                maxLines = 1
            )
        }

        Column (
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
                .padding(4.dp)
                ) {
            Text(text = "Repetitions")
            TextField(
                value = repetitions,
                onValueChange = { repetitions = it },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                maxLines = 1
            )
        }

        Column (
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
                .padding(4.dp)
                ) {
            Text(text = "Weight")
            TextField(
                value = weight,
                onValueChange = { weight = it },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                maxLines = 1
            )
        }
    }
}

@Composable
private fun AddExerciseButton(
    sessionId: UUID,
    onExerciseAdded: (Record) -> Unit
) {
    Button(onClick = {
        onExerciseAdded(createNewRecord(sessionId = sessionId, previousSet = 0))
    },
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        colors = ButtonDefaults.buttonColors(backgroundColor = Color.Gray)
    ) {
        Text(text = "Add exercise", color = Color.White)
    }
}

private fun cancelAndDelete(
    viewModel: SessionViewModel,
    navController: NavController
) {
    viewModel.changeDetailedSession(null)
    navController.navigate("session_list")
}

private fun createNewRecord(
    sessionId: UUID,
    previousSet: Int
): Record {
    return Record(
        id = UUID.randomUUID(),
        sessionId = sessionId,
        exerciseName = "Example",
        repetitions = 0,
        weight = 0,
        setNumber = previousSet + 1
    )
}