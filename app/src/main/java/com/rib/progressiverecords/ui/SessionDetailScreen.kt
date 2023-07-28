package com.rib.progressiverecords.ui

import android.text.format.DateFormat
import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.Card
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Scaffold
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.*
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
    val addingExercise = rememberSaveable { mutableStateOf(false) }

    val changingDate = rememberSaveable { mutableStateOf(false) }

    val savingSession = rememberSaveable { mutableStateOf(false) }

    var session = viewModel.detailedSession.collectAsState().value

    if (session == null) {
        session = SessionWithRecords(Session(id = UUID.randomUUID(), date = Date(), sessionName = "New session"), emptyList())
        viewModel.changeDetailedSession(session)
    }

    val sessionId = session.session.id

    var records by remember { mutableStateOf((session.records)) }
    records = records.sortedWith(
        compareBy<Record> { it.exerciseName }
            .thenBy { it.setNumber }
    )
    val exerciseSetsList = ExerciseSetsList().organizeRecords(records)

    Scaffold (
        topBar = { SessionDetailTopBar(onClick = { savingSession.value = true })}
            ) { it
        SetList(
            session = session.session,
            exerciseSetsList = exerciseSetsList,
            onUpdateRecords = { record ->
                val foundRecord = records.find { it.exerciseName == record.exerciseName && it.setNumber == record.setNumber }
                records = if (foundRecord == null) {
                    records + record
                } else {
                    records - foundRecord + record
                }
                              },
            onOpenDateDialog = { changingDate.value = true },
            onUpdateSessionName = {
                viewModel.changeDetailedSession(
                    session.copy(session = Session(
                        id = session.session.id,
                        sessionName = it,
                        date = session.session.date
                    ))
                )
            },
            selectExercise = { addingExercise.value = true },
            viewModel = viewModel,
            navController = navController
        )

        if (changingDate.value) {
            SessionDatePickerDialog(
                onDismissRequest = { changingDate.value = false },
                onSelectDate = {
                    if (it != null) {
                        viewModel.changeDetailedSession(
                            session.copy(session = Session(
                                id = session.session.id,
                                sessionName = session.session.sessionName,
                                date = Date(it)
                            ))
                        )
                    }
                }
            )
        }

        if (savingSession.value) {
            SaveSessionDialog(
                onDismissRequest = { savingSession.value = false },
                onSessionSaved = { saveSessionToDb(
                    viewModel = viewModel,
                    navController = navController,
                    records = records,
                    session = session.session
                ) }
            )
        }

        if (addingExercise.value) {
            SelectExerciseDialog(
                onDismissRequest = { addingExercise.value = false },
                onExerciseSelected = { exerciseName ->
                    addingExercise.value = false
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
    session: Session,
    exerciseSetsList: ExerciseSetsList,
    onUpdateRecords: (Record) -> Unit,
    onUpdateSessionName: (String) -> Unit,
    onOpenDateDialog: () -> Unit,
    selectExercise: () -> Unit,
    viewModel: SessionViewModel,
    navController: NavController
) {
    val sessionId = session.id

    if (exerciseSetsList.totalSets.isEmpty()) {
        Column (modifier = Modifier.fillMaxSize()) {
            SessionNameAndDate(
                session = session,
                onOpenDateDialog = { onOpenDateDialog() },
                onUpdateSessionName = { onUpdateSessionName(it) }
            )

            AddExerciseButton(selectExercise = { selectExercise() })

            Spacer (modifier = Modifier.weight(1f))

            CancelButton(viewModel = viewModel, navController = navController)
        }

    } else {
        LazyColumn {
            item {
                SessionNameAndDate(
                    session = session,
                    onOpenDateDialog = { onOpenDateDialog() },
                    onUpdateSessionName = { onUpdateSessionName(it) }
                )
            }

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
    session: Session,
    onOpenDateDialog: () -> Unit,
    onUpdateSessionName: (String) -> Unit
) {
    var name by rememberSaveable(stateSaver = TextFieldValue.Saver) {
        mutableStateOf(TextFieldValue(text = session.sessionName))
    }

    Row (
        modifier = Modifier.padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        TextField(
            value = name,
            onValueChange = {
                name = it
                onUpdateSessionName(name.annotatedString.toString())
                            },
            singleLine = true,
            modifier = Modifier.weight(1.5f)
        )

        Spacer(modifier = Modifier.weight(0.4f))

        Button(
            onClick = { onOpenDateDialog() },
            colors = ButtonDefaults.buttonColors(backgroundColor = Color.Gray),
            modifier = Modifier.weight(1f)
        ) {
            Text(text = DateFormat.format("dd / MMM / yyyy", session.date).toString(), color = Color.White)
        }
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
            val recordIsSaved = rememberSaveable { mutableStateOf (false) }
            SetItem(
                record,
                onChangeRecordState = {
                    recordIsSaved.value = ! recordIsSaved.value
                    if (recordIsSaved.value) {
                        onUpdateRecords(it)
                    }
                                      },
                recordIsSaved = recordIsSaved.value
            )
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
    onChangeRecordState: (Record) -> Unit,
    recordIsSaved: Boolean
) {
    var repetitions by rememberSaveable(stateSaver = TextFieldValue.Saver) {
        mutableStateOf(TextFieldValue(text = record.repetitions.toString()))
    }

    var weight by rememberSaveable(stateSaver = TextFieldValue.Saver) {
        mutableStateOf(TextFieldValue(text = record.weight.toString()))
    }

    val currentRecord by remember { mutableStateOf(record) }

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
                singleLine = true,
                enabled = !recordIsSaved
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
                singleLine = true,
                enabled = !recordIsSaved
            )
        }

        IconButton(
            onClick = {
                if (!recordIsSaved) {
                    currentRecord.weight = weight.annotatedString.toString().toInt()
                    currentRecord.repetitions = repetitions.annotatedString.toString().toInt()
                }
                onChangeRecordState(currentRecord)
            }
        ) {
            Icon(Icons.Filled.Check,
                contentDescription = "Finish set",
                modifier = Modifier.background(if (recordIsSaved) { Color.Green } else { Color.Transparent }))
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
            .padding(horizontal = 16.dp),
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
    onDismissRequest: () -> Unit,
    onSessionSaved: () -> Unit
) {
    Dialog (onDismissRequest = { onDismissRequest() } ) {
        Card {
            Column (modifier = Modifier.padding(16.dp)) {
                Text(text = "Are you sure you want to save this session? Only checked records will be updated")
                Row (
                    modifier = Modifier.padding(horizontal = 8.dp).fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Button(
                        onClick = { onDismissRequest() },
                        colors = ButtonDefaults.buttonColors(backgroundColor = Color.Red),
                        modifier = Modifier.padding(horizontal = 8.dp)
                    ) {
                        Text(text = "No", color = Color.White)
                    }

                    Button(
                        onClick = { onSessionSaved() },
                        colors = ButtonDefaults.buttonColors(backgroundColor = Color.Gray),
                        modifier = Modifier.padding(8.dp)
                    ) {
                        Text(text = "Yes", color = Color.White)
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SessionDatePickerDialog(
    onDismissRequest: () -> Unit,
    onSelectDate: (Long?) -> Unit
) {
    val datePickerState = rememberDatePickerState()
    val confirmEnabled = remember { derivedStateOf { datePickerState.selectedDateMillis != null } }

    DatePickerDialog(
        onDismissRequest = { onDismissRequest() },

        confirmButton = {
            TextButton(
                onClick = {
                    onDismissRequest()
                    onSelectDate(datePickerState.selectedDateMillis)
                },
                enabled = confirmEnabled.value
            ) {
                Text("Ok")
            }
        },

        dismissButton = {
            TextButton(
                onClick = { onDismissRequest() }
            ) {
                Text("Cancel")
            }
        }
    ) {
        DatePicker(state = datePickerState)
    }
}

private fun saveSessionToDb (
    viewModel: SessionViewModel,
    navController: NavController,
    records: List<Record>,
    session: Session
) {
    viewModel.viewModelScope.launch {
        viewModel.addSession(session)

        records.forEach {record ->
            viewModel.addRecord(record)
        }
    }

    viewModel.changeDetailedSession(null)
    navController.navigate("session_list")
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