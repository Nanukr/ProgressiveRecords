package com.rib.progressiverecords.ui

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.MoreVert
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
    navController: NavController,
    selectedExercise: String
) {
    var session = viewModel.detailedSession.collectAsState().value

    if (session == null) {
        session = SessionWithRecords(Session(id = UUID.randomUUID(), date = Date(), sessionName = "New session"), emptyList())
        viewModel.changeDetailedSession(session)
    }

    val sessionId = session.session.id

    var records by rememberSaveable { mutableStateOf((session.records)) }
    records = records.sortedWith(
        compareBy<Record> { it.exerciseName }
            .thenBy { it.setNumber }
    )
    Log.d("Detail", records.toString())

    if (selectedExercise != " " && !records.any { it.exerciseName == selectedExercise}) {
        records = records + createNewRecord(sessionId = sessionId, previousSet = 0, exerciseName = selectedExercise)
    }

    Scaffold (
        topBar = { DetailedTopBar(onClick = { /*TODO*/ })}
            ) { it
        Column (
            modifier = Modifier.fillMaxSize()
        ) {
            SetList(
                sessionId = sessionId,
                records = records,
                onUpdateRecords = { records = records + it },
                viewModel = viewModel,
                navController = navController
            )
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
    onUpdateRecords: (Record) -> Unit,
    viewModel: SessionViewModel,
    navController: NavController
) {
    if (records.isEmpty()) {
        Column (modifier = Modifier.fillMaxSize()) {
            AddExerciseButton(navController = navController)

            Spacer (modifier = Modifier.weight(1f))

            CancelButton(viewModel = viewModel, navController = navController)
        }

    } else {
        LazyColumn {
            var currentExercise = ""
            var itemCount = 0

            items(records) {record  ->
                if (record.exerciseName == currentExercise || currentExercise == "") {
                    if (record.setNumber == 1) {
                        ExerciseName(navController = navController, exerciseName = record.exerciseName)
                    }
                    SetItem(record)
                } else {
                    AddSetButton(
                        sessionId = sessionId,
                        previousSet = record.setNumber,
                        exerciseName = record.exerciseName,
                        onExerciseAdded = { onUpdateRecords(it) }
                    )
                    AddExerciseButton(navController = navController)
                    SetItem(record)
                }
                currentExercise = record.exerciseName
                itemCount++

                if (itemCount == records.size) {
                    AddSetButton(
                        sessionId = sessionId,
                        previousSet = record.setNumber,
                        exerciseName = record.exerciseName,
                        onExerciseAdded = { onUpdateRecords(it) }
                    )
                    AddExerciseButton(navController = navController)
                }
            }

            item {
                CancelButton(viewModel = viewModel, navController = navController)
            }
        }
    }
}

@Composable
private fun ExerciseName (
    navController: NavController,
    exerciseName: String
) {
    Row (
        modifier = Modifier.fillMaxWidth().
        padding(16.dp).
        clickable { navController.navigate("exercise/true") }
    ) {
        Text(text = exerciseName, color = Color.Black, style = MaterialTheme.typography.h6)

        Spacer(modifier = Modifier.weight(1f))

        Icon(Icons.Filled.MoreVert, contentDescription = "Change exercise")
    }
}

@Composable
private fun CancelButton (
    viewModel: SessionViewModel,
    navController: NavController
) {
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

@Composable
private fun SetItem(
    record: Record
) {
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
        Text(text = "Set number: ${record.setNumber}", modifier = Modifier.weight(1f))

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
                singleLine = true
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
                singleLine = true
            )
        }
    }
}

@Composable
private fun AddSetButton(
    sessionId: UUID,
    previousSet: Int,
    exerciseName: String,
    onExerciseAdded: (Record) -> Unit
) {
    Button(onClick = {
        onExerciseAdded(createNewRecord(sessionId = sessionId, previousSet = previousSet, exerciseName = exerciseName))
    },
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        colors = ButtonDefaults.buttonColors(backgroundColor = Color.Gray)
    ) {
        Text(text = "Add set", color = Color.White)
    }
}

@Composable
private fun AddExerciseButton(
    navController: NavController
) {
    Button(
        onClick = { navController.navigate("exercise/true") },
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
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
    previousSet: Int,
    exerciseName: String
): Record {
    return Record(
        id = UUID.randomUUID(),
        sessionId = sessionId,
        exerciseName = exerciseName,
        repetitions = 0,
        weight = 0,
        setNumber = previousSet + 1
    )
}