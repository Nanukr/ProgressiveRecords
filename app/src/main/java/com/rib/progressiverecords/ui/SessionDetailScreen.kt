package com.rib.progressiverecords.ui

import android.util.Log
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavController
import com.rib.progressiverecords.ExerciseSetsList
import com.rib.progressiverecords.ExerciseViewModel
import com.rib.progressiverecords.SessionViewModel
import com.rib.progressiverecords.model.Record
import com.rib.progressiverecords.model.Session
import com.rib.progressiverecords.model.relations.SessionWithRecords
import kotlinx.coroutines.launch
import java.util.*

@Composable
fun SessionDetailScreen(
    viewModel: SessionViewModel,
    navController: NavController
) {
    var addingExercise by rememberSaveable { mutableStateOf(false) }

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
    val exerciseSetsList = ExerciseSetsList().organizeRecords(records)

    Scaffold (
        topBar = { SessionDetailTopBar(onClick = { /*TODO*/ })}
            ) { it
        SetList(
            sessionId = sessionId,
            exerciseSetsList = exerciseSetsList,
            onUpdateRecords = { record ->
                val foundRecord = records.find { it.exerciseName == record.exerciseName && it.setNumber == record.setNumber }
                records = if (foundRecord == null) {
                    records + record
                } else {
                    records - foundRecord + record
                }
                              },
            selectExercise = { addingExercise = true },
            viewModel = viewModel,
            navController = navController
        )

        if (addingExercise) {
            SelectExerciseDialog(
                onDismissRequest = { addingExercise = false },
                onExerciseSelected = { exerciseName ->
                    addingExercise = false
                    if (!records.any{ it.exerciseName == exerciseName }) {
                        records = records + createNewRecord(
                            sessionId = sessionId,
                            previousSet = 0,
                            exerciseName = exerciseName
                        )
                    }
                }
            )
        }
    }
}

@Composable
private fun SetList(
    sessionId: UUID,
    exerciseSetsList: ExerciseSetsList,
    onUpdateRecords: (Record) -> Unit,
    selectExercise: () -> Unit,
    viewModel: SessionViewModel,
    navController: NavController
) {

    if (exerciseSetsList.totalSets.isEmpty()) {
        Column (modifier = Modifier.fillMaxSize()) {
            AddExerciseButton(selectExercise = { selectExercise() })

            Spacer (modifier = Modifier.weight(1f))

            CancelButton(viewModel = viewModel, navController = navController)
        }

    } else {
        LazyColumn {

            items(exerciseSetsList.totalSets) {setList  ->
                if (setList.isNotEmpty()) {
                    ExerciseSets(
                        sets = setList,
                        sessionId = sessionId,
                        onUpdateRecords = { onUpdateRecords(it) }
                    )
                }
            }

            item {
                AddExerciseButton(selectExercise = { selectExercise() })
            }

            item {
                CancelButton(viewModel = viewModel, navController = navController)
            }
        }
    }
}

@Composable
private fun SessionNameAndDate (
    session: Session
) {
    Row {

    }
}

@Composable
private fun ExerciseName (
    selectExercise: () -> Unit,
    exerciseName: String
) {
    Row (
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .clickable { selectExercise() }
    ) {
        Text(text = exerciseName, color = Color.Black, style = MaterialTheme.typography.h6)

        Spacer(modifier = Modifier.weight(1f))

        Icon(Icons.Filled.MoreVert, contentDescription = "Change exercise")
    }
}

@Composable
private fun ExerciseSets(
    sets: List<Record>,
    sessionId: UUID,
    onUpdateRecords: (Record) -> Unit
) {
    val exerciseName = sets[0].exerciseName
    Column (
        modifier = Modifier.padding(8.dp)
            ) {
        ExerciseName(
            selectExercise = { /*TODO*/ },
            exerciseName = exerciseName
        )

        sets.forEach { record ->
            SetItem(record, onFinishSet = { onUpdateRecords(it) } )
        }

        AddSetButton(
            sessionId = sessionId,
            previousSet = sets.last().setNumber,
            exerciseName = exerciseName,
            onExerciseAdded = { onUpdateRecords(it) }
        )
    }
}

@Composable
private fun SetItem(
    record: Record,
    onFinishSet: (Record) -> Unit
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

        IconButton(onClick = { onFinishSet(record) }) {
            Icon(Icons.Filled.Check, contentDescription = "Finish set")
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
    selectExercise: () -> Unit
) {
    Button(
        onClick = { selectExercise() },
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        colors = ButtonDefaults.buttonColors(backgroundColor = Color.Gray)
    ) {
        Text(text = "Add exercise", color = Color.White)
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
private fun SelectExerciseDialog (
    onDismissRequest: () -> Unit,
    onExerciseSelected: (String) -> Unit
) {
    Dialog (
        properties = DialogProperties(usePlatformDefaultWidth = false),
        onDismissRequest = { onDismissRequest() }
    ) {
        Surface (modifier = Modifier.fillMaxSize()) {
            ExerciseScreen(
                viewModel = ExerciseViewModel(),
                isBeingSelected = true,
                onExerciseSelected = { onExerciseSelected(it) }
            )
        }
    }
}

@Composable
private fun SaveSessionDialog (
    records: List<Record>,
    onDismissRequest: () -> Unit,
    onSessionSaved: () -> Unit
) {
    Dialog (onDismissRequest = { onDismissRequest() } ) {
        Card {
            Column (modifier = Modifier.padding(8.dp)) {
                Text(text = "Are you sure you want to save this session? All records with missing information will be deleted")
                Row (modifier = Modifier.padding(8.dp)) {
                    Button(
                        onClick = { onDismissRequest() },
                        colors = ButtonDefaults.buttonColors(backgroundColor = Color.Red)
                    ) {
                        Text(text = "No", color = Color.White)
                    }

                    Button(
                        onClick = { onSessionSaved() },
                        colors = ButtonDefaults.buttonColors(backgroundColor = Color.Gray)
                    ) {
                        Text(text = "Yes", color = Color.White)
                    }
                }
            }
        }
    }
}

private fun saveSessionToDb (
    viewModel: SessionViewModel,
    records: List<Record>,
    session: Session
) {
    viewModel.viewModelScope.launch {
        viewModel.addSession(session)

        records.forEach {record ->
            if (record.repetitions != 0) {
                viewModel.addRecord(record)
            }
        }
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