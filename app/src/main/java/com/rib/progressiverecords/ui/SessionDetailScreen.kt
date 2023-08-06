package com.rib.progressiverecords.ui

import android.text.format.DateFormat
import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.material.Card
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Scaffold
import androidx.compose.material.TextFieldDefaults
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.*
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.listSaver
import androidx.compose.runtime.saveable.mapSaver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavController
import com.rib.progressiverecords.ExerciseSetsList
import com.rib.progressiverecords.ExerciseViewModel
import com.rib.progressiverecords.R
import com.rib.progressiverecords.SessionViewModel
import com.rib.progressiverecords.model.Record
import com.rib.progressiverecords.model.Session
import com.rib.progressiverecords.model.relations.SessionWithRecords
import com.rib.progressiverecords.ui.theme.StandardButton
import com.rib.progressiverecords.ui.theme.StandardOutlinedButton
import com.rib.progressiverecords.ui.theme.StandardTextField
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

    var records by remember { mutableStateOf((viewModel.detailedRecords)) }
    records = records.sortedWith(
        compareBy<Record> { it.exerciseName }
            .thenBy { it.setNumber }
    )
    viewModel.detailedRecords = records

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
    Log.d("Detail", exerciseSetsList.totalSets.toString())
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
    var name by rememberSaveable { mutableStateOf(session.sessionName) }

    Row (
        modifier = Modifier
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        StandardTextField(
            entryValue = name,
            onValueChange = {
                            name = it
                onUpdateSessionName(name)
            },
            isNumeric = false,
            modifier = Modifier
                .weight(1f)
                .padding(8.dp)
        )

        StandardButton(
            onClick = { onOpenDateDialog() },
            text = DateFormat.format("dd / MMM / yyyy", session.date).toString(),
            textAlign = TextAlign.Right
        )
    }
}

@Composable
private fun ExerciseName (
    selectExercise: () -> Unit,
    exerciseName: String
) {
    Column {
        Row (
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .clickable { selectExercise() }
        ) {
            Text(
                text = exerciseName,
                color = MaterialTheme.colors.onBackground,
                style = MaterialTheme.typography.h6
            )

            Spacer(modifier = Modifier.weight(1f))

            Icon(
                Icons.Filled.MoreVert,
                tint = MaterialTheme.colors.onBackground,
                contentDescription = stringResource(R.string.change_exercise_icon_description)
            )
        }

        Row (
            modifier = Modifier
                .padding(8.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
                ) {
            Text (
                modifier = Modifier
                    .weight(0.5f)
                    .padding(4.dp)
                    .fillMaxWidth(),
                text = stringResource(R.string.set_label),
                color = MaterialTheme.colors.onBackground,
                textAlign = TextAlign.Center
            )

            Text(
                text = stringResource(R.string.repetitions_label),
                color = MaterialTheme.colors.onBackground,
                modifier = Modifier
                    .weight(1f)
                    .padding(4.dp)
            )

            Text (
                text = stringResource(R.string.weight_label),
                color = MaterialTheme.colors.onBackground,
                modifier = Modifier
                    .weight(1f)
                    .padding(4.dp)
            )

            Spacer(modifier = Modifier.weight(0.5f))
        }
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
        modifier = Modifier
            .padding(8.dp)
            ) {
        ExerciseName(
            selectExercise = { /*TODO*/ },
            exerciseName = exerciseName
        )

        sets.forEach { record ->
            val recordIsSaved = rememberSaveable { mutableStateOf (false) }
            SetItem(
                record = record,
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
    var repetitions by rememberSaveable{ mutableStateOf(record.repetitions.toString()) }

    var weight by rememberSaveable { mutableStateOf(record.weight.toString()) }

    val currentRecord by remember { mutableStateOf(record) }

    Row (
        modifier = Modifier
            .padding(8.dp)
            .fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
            ) {
        Text (
            modifier = Modifier
                .fillMaxSize()
                .weight(0.5f)
                .padding(4.dp),
            text = record.setNumber.toString(),
            color = MaterialTheme.colors.onBackground,
            textAlign = TextAlign.Center,
        )

        StandardTextField(
            entryValue = repetitions,
            onValueChange = { repetitions = it },
            modifier = Modifier
                .weight(1f),
            isNumeric = true,
            isEnabled = !recordIsSaved
        )

        StandardTextField(
            entryValue = weight,
            onValueChange = { weight = it },
            modifier = Modifier
                .weight(1f),
            isNumeric = true,
            isEnabled = !recordIsSaved
        )

        IconButton (
            onClick = {
                if (!recordIsSaved) {
                    currentRecord.weight = weight.toInt()
                    currentRecord.repetitions = repetitions.toInt()
                }
                onChangeRecordState(currentRecord)
            },
            modifier = Modifier.weight(0.5f)
        ) {
            Icon (Icons.Filled.Check,
                contentDescription = stringResource(R.string.finish_set_icon_description),
                tint = MaterialTheme.colors.onBackground,
                modifier = Modifier.background(if (recordIsSaved) { Color.Green } else { Color.Transparent })
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
    StandardOutlinedButton (
        onClick = { onExerciseAdded(createNewRecord(sessionId = sessionId, previousSet = previousSet, exerciseName = exerciseName)) },
        text = stringResource(R.string.add_set_button),
        modifier = Modifier
            .fillMaxWidth()
    )
}

@Composable
private fun AddExerciseButton(
    selectExercise: () -> Unit
) {
    StandardOutlinedButton(
        onClick = { selectExercise() },
        text = stringResource(R.string.add_exercise_button),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp)
    )
}

@Composable
private fun CancelButton (
    viewModel: SessionViewModel,
    navController: NavController
) {
    StandardOutlinedButton(
        onClick = { cancelAndDelete(viewModel, navController) },
        text = stringResource(R.string.cancel_session_button),
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        textColor = Color.Red
    )
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
        Card (
            modifier = Modifier.background(color = MaterialTheme.colors.primary)
                ) {
            Column (modifier = Modifier.padding(16.dp)) {
                Text (
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(4.dp),
                    text = stringResource(R.string.confirm_save_session_message),
                    color = MaterialTheme.colors.onPrimary,
                    textAlign = TextAlign.Center
                )

                Row (
                    modifier = Modifier
                        .padding(horizontal = 8.dp)
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton (
                        onClick = { onDismissRequest() },
                        modifier = Modifier.padding(horizontal = 8.dp)
                    ) {
                        Text(
                            text = stringResource(R.string.cancel_button),
                            color = MaterialTheme.colors.secondary
                        )
                    }

                    TextButton (
                        onClick = { onSessionSaved() },
                        modifier = Modifier.padding(8.dp)
                    ) {
                        Text(
                            text = stringResource(R.string.confirm_button),
                            color = MaterialTheme.colors.secondary
                        )
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
        modifier = Modifier.padding(16.dp),

        onDismissRequest = { onDismissRequest() },

        confirmButton = {
            TextButton(
                onClick = {
                    onDismissRequest()
                    onSelectDate(datePickerState.selectedDateMillis)
                },
                enabled = confirmEnabled.value
            ) {
                Text(
                    text = stringResource(R.string.confirm_button),
                    color = MaterialTheme.colors.secondary
                )
            }
        },

        dismissButton = {
            TextButton(
                onClick = { onDismissRequest() }
            ) {
                Text(
                    text = stringResource(R.string.cancel_button),
                    color = MaterialTheme.colors.secondary
                )
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
    viewModel.detailedRecords = emptyList()
    navController.navigate("session_list")
}

private fun cancelAndDelete(
    viewModel: SessionViewModel,
    navController: NavController
) {
    viewModel.changeDetailedSession(null)
    viewModel.detailedRecords = emptyList()
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