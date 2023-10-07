package com.rib.progressiverecords.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.AlertDialog
import androidx.compose.material.Card
import androidx.compose.material.CheckboxDefaults
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Scaffold
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.DropdownMenu
import androidx.compose.material.DropdownMenuItem
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.material3.*
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavController
import com.rib.progressiverecords.*
import com.rib.progressiverecords.R
import com.rib.progressiverecords.model.Exercise
import com.rib.progressiverecords.model.Record
import com.rib.progressiverecords.model.Session
import com.rib.progressiverecords.ui.theme.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun SessionCreationScreen(
    viewModel: SessionViewModel,
    navController: NavController
) {
    val addingExercise = rememberSaveable { mutableStateOf(false) }

    val swappingExercise = rememberSaveable { mutableStateOf(false) }

    var deletingExercise by rememberSaveable { mutableStateOf(false) }

    val changingDate = rememberSaveable { mutableStateOf(false) }

    val savingSession = rememberSaveable { mutableStateOf(false) }

    val exitingSession = rememberSaveable { mutableStateOf(false) }

    var session = viewModel.createdSession ?: Session(
        id = UUID.randomUUID(),
        date = Date(),
        sessionName = stringResource(R.string.new_session_name)
    )
    viewModel.createdSession = session

    var records by remember { mutableStateOf((viewModel.newRecords)) }
    records = records.sortedWith(
        compareBy<Record> { it.sessionPosition }
            .thenBy { it.setNumber }
    )

    val exerciseSetsList = ExerciseSetsList().organizeRecords(records)

    if (viewModel.checkedRecords == null) {
        viewModel.checkedRecords = emptyList()
    }

    Scaffold (
        topBar = { TopBar(
            onClick = { savingSession.value = true },
            icon = painterResource(R.drawable.ic_save),
            contentDescription = stringResource(R.string.save_session_icon_description)
        )}
            ) { it
        SetList(
            session = session,
            exerciseSetsList = exerciseSetsList,
            viewModel = viewModel,
            onUpdateRecords = { record ->
                val foundRecord = records.find { it.exerciseName == record.exerciseName && it.setNumber == record.setNumber }
                records = if (foundRecord == null) {
                    records + record
                } else {
                    records - foundRecord + record
                }
                viewModel.newRecords = records
                              },
            onOpenDateDialog = { changingDate.value = true },
            onUpdateSessionName = {
                session = session.copy(
                    id = session.id,
                    sessionName = it,
                    date = session.date
                )
                viewModel.createdSession = session
            },
            selectExercise = { addingExercise.value = true },
            onChangeExercise = { swappingExercise.value = true },
            onDeleteExercise = { deletingExercise = true },
            onMoveExerciseUp = {
                viewModel.positionBeingModified = it
                viewModel.newRecords = moveSetInPositionBefore(
                    records = viewModel.newRecords,
                    position = viewModel.positionBeingModified!!
                )
                records = viewModel.newRecords
                viewModel.checkedRecords = moveSetInPositionBefore(
                    records = viewModel.checkedRecords?: emptyList(),
                    position = viewModel.positionBeingModified!!
                )

                viewModel.positionBeingModified = null
            },
            onMoveExerciseDown = {
                viewModel.positionBeingModified = it
                viewModel.newRecords = moveSetInPositionAfter(
                    records = viewModel.newRecords,
                    position = viewModel.positionBeingModified!!
                )
                records = viewModel.newRecords
                viewModel.checkedRecords = moveSetInPositionAfter(
                    records = viewModel.checkedRecords?: emptyList(),
                    position = viewModel.positionBeingModified!!
                )

                viewModel.positionBeingModified = null
            },
            onCancelSession = { exitingSession.value = true }
        )

        if (changingDate.value) {
            SessionDatePickerDialog(
                onDismissRequest = { changingDate.value = false },
                onSelectDate = {
                    if (it != null) {
                        session = session.copy(
                            id = session.id,
                            sessionName = session.sessionName,
                            date = Date(it)
                        )
                        viewModel.createdSession = session
                    }
                }
            )
        }

        if (savingSession.value) {
            SaveSessionDialog(
                onDismissRequest = { savingSession.value = false },
                onSessionSaved = {
                    saveSessionToDb(
                        viewModel = viewModel,
                        navController = navController,
                        session = session
                    )
                    savingSession.value = false
                },
                viewModel = viewModel
            )
        }

        if (addingExercise.value) {
            SelectExerciseDialog(
                isSwapping = false,
                onDismissRequest = { addingExercise.value = false },
                onExerciseSelected = { exercises ->
                    addingExercise.value = false

                    if (exercises.isNotEmpty()) {
                        exercises.forEach { exercise ->
                            val lastExercisePosition = if (records.isEmpty()) { 0 } else { records[records.lastIndex].sessionPosition }

                            records = records + createNewRecord(
                                sessionId = session.id,
                                previousSet = 0,
                                exerciseName = exercise.exerciseName,
                                sessionPosition = lastExercisePosition + 1,
                                category = exercise.category
                            )
                        }
                        viewModel.newRecords = records
                    }
                }
            )
        }

        if (swappingExercise.value) {
            SelectExerciseDialog(
                isSwapping = true,
                onDismissRequest = {
                    swappingExercise.value = false
                },
                onExerciseSelected = { exercises ->
                    swappingExercise.value = false

                    val previousSets = viewModel.previousSets!!

                    var currentSet = 0

                    val sessionPosition = viewModel.positionBeingModified!!

                    if (exercises.isNotEmpty()) {
                        deleteAllRecordsInPosition(viewModel = viewModel, moveExercises = false)
                        records = viewModel.newRecords

                        while (currentSet < previousSets) {
                            records = records + createNewRecord(
                                sessionId = session.id,
                                previousSet = currentSet,
                                exerciseName = exercises[0].exerciseName,
                                sessionPosition = sessionPosition,
                                category = exercises[0].category
                            )

                            currentSet ++
                        }
                        viewModel.newRecords = records
                    }
                }
            )
        }

        if (deletingExercise) {
            DeleteExerciseDialog(
                onDismissRequest = {
                    deletingExercise = false
                    viewModel.positionBeingModified = null
                },
                onExerciseDeleted = {
                    deletingExercise = false

                    deleteAllRecordsInPosition(viewModel = viewModel)
                    records = viewModel.newRecords
                }
            )
        }

        if (exitingSession.value) {
            ExitSessionDialog(
                onExitSession = {
                    cancelAndDelete(viewModel = viewModel, navController = navController)
                },
                onDismissRequest = { exitingSession.value = false }
            )
        }
    }

    BackHandler {
        cancelAndDelete(viewModel = viewModel, navController = navController)
    }
}

@Composable
private fun SetList(
    session: Session,
    exerciseSetsList: ExerciseSetsList,
    viewModel: SessionViewModel,
    onUpdateRecords: (Record) -> Unit,
    onUpdateSessionName: (String) -> Unit,
    onOpenDateDialog: () -> Unit,
    selectExercise: () -> Unit,
    onChangeExercise: () -> Unit,
    onDeleteExercise: () -> Unit,
    onMoveExerciseUp: (Int) -> Unit,
    onMoveExerciseDown: (Int) -> Unit,
    onCancelSession: () -> Unit
) {
    val setSize = exerciseSetsList.totalSets.size

    Column (
        modifier = Modifier.fillMaxSize()
    ) {
        SessionNameAndDate(
            session = session,
            onOpenDateDialog = { onOpenDateDialog() },
            onUpdateSessionName = { onUpdateSessionName(it) }
        )

        if (exerciseSetsList.totalSets.isEmpty()) {
            AddExerciseButton(selectExercise = { selectExercise() })

            CancelButton(onCancelSession = { onCancelSession() })
        } else {
            LazyColumn {
                items(exerciseSetsList.totalSets) {setList  ->
                    if (setList.isNotEmpty()) {
                        ExerciseSets(
                            sets = setList,
                            sessionId = session.id,
                            setSize = setSize,
                            checkedRecords = viewModel.checkedRecords ?: emptyList(),
                            onUpdateRecords = { onUpdateRecords(it) },
                            onChangeExercise = { previousSets, sessionPosition ->
                                viewModel.previousSets = previousSets
                                viewModel.positionBeingModified = sessionPosition
                                onChangeExercise()
                            },
                            onDeleteExercise = {
                                viewModel.positionBeingModified = it
                                onDeleteExercise()
                            },
                            onMoveExerciseUp = { onMoveExerciseUp(it) },
                            onMoveExerciseDown = { onMoveExerciseDown(it) },
                            onAddCheckedRecord = {
                                viewModel.checkedRecords = viewModel.checkedRecords?.plus(it)
                            },
                            onRemoveCheckedRecord = {
                                viewModel.checkedRecords = viewModel.checkedRecords?.minus(it)
                            }
                        )
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(16.dp))
                }

                item {
                    AddExerciseButton(selectExercise = { selectExercise() })
                }

                item {
                    CancelButton(onCancelSession = { onCancelSession() })
                }
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
            .padding(horizontal = 16.dp, vertical = 8.dp),
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
                .shadow(5.dp, RoundedCornerShape(8.dp))
                .background(MaterialTheme.colors.primary)
        )

        StandardButton(
            onClick = { onOpenDateDialog() },
            text = SimpleDateFormat
                .getDateInstance(SimpleDateFormat.DEFAULT, Locale.getDefault())
                .format(session.date).toString(),
            textAlign = TextAlign.Right
        )
    }
}

@Composable
private fun ExerciseHeader (
    exerciseName: String,
    sessionPosition: Int,
    category: String,
    setSize: Int,
    onChangeExercise: () -> Unit,
    onDeleteExercise: () -> Unit,
    onMoveExerciseUp: () -> Unit,
    onMoveExerciseDown: () -> Unit
) {
    var dropdownMenuExpanded by rememberSaveable { mutableStateOf(false) }

    Column (
        modifier = Modifier
            .padding(vertical = 4.dp)
            ) {
        Row (
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                modifier = Modifier
                    .padding(4.dp)
                    .weight(1f),
                text = "$sessionPosition: $exerciseName",
                color = MaterialTheme.colors.onBackground,
                style = MaterialTheme.typography.h6,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Box(modifier = Modifier
                .wrapContentSize(Alignment.TopStart)) {
                IconButton(onClick = { dropdownMenuExpanded = true }) {
                    Icon(
                        Icons.Filled.MoreVert,
                        tint = MaterialTheme.colors.onBackground,
                        contentDescription = ""
                    )
                }

                DropdownMenu(
                    expanded = dropdownMenuExpanded,
                    onDismissRequest = { dropdownMenuExpanded = false }
                ) {
                    DropdownMenuItem(
                        onClick = {
                            onMoveExerciseUp()
                            dropdownMenuExpanded = false
                        },
                        enabled = (sessionPosition != 1)
                    ) {
                        Icon(
                            modifier = Modifier.padding(4.dp),
                            painter = painterResource(id = R.drawable.ic_arrow_up),
                            contentDescription = "",
                            tint = MaterialTheme.colors.onPrimary
                        )

                        Text(
                            text = stringResource(R.string.move_exercise_up_button),
                            color = MaterialTheme.colors.onPrimary
                        )
                    }

                    DropdownMenuItem(
                        onClick = {
                            onMoveExerciseDown()
                            dropdownMenuExpanded = false
                        },
                        enabled = (sessionPosition != setSize)
                    ) {
                        Icon(
                            modifier = Modifier.padding(4.dp),
                            painter = painterResource(id = R.drawable.ic_arrow_down),
                            contentDescription = "",
                            tint = MaterialTheme.colors.onPrimary
                        )

                        Text(
                            text = stringResource(R.string.move_exercise_down_button),
                            color = MaterialTheme.colors.onPrimary
                        )
                    }

                    DropdownMenuItem(onClick = {
                        onChangeExercise()
                        dropdownMenuExpanded = false
                    }) {
                        Icon(
                            modifier = Modifier.padding(4.dp),
                            painter = painterResource(id = R.drawable.ic_swap),
                            contentDescription = "",
                            tint = MaterialTheme.colors.onPrimary
                        )

                        Text(
                            text = stringResource(R.string.change_exercise_button),
                            color = MaterialTheme.colors.onPrimary
                        )
                    }

                    DropdownMenuItem(onClick = {
                        onDeleteExercise()
                        dropdownMenuExpanded = false
                    }) {
                        Icon(
                            modifier = Modifier.padding(4.dp),
                            painter = painterResource(id = R.drawable.ic_delete),
                            contentDescription = "",
                            tint = MaterialTheme.colors.onPrimary
                        )

                        Text(
                            text = stringResource(R.string.delete_exercise_icon_description),
                            color = MaterialTheme.colors.onPrimary
                        )
                    }
                }
            }
        }
        var string1 = ""

        var string2 = ""

        when (category) {
            "General" -> {
                string1 = stringResource(R.string.weight_label)
                string2 = stringResource(R.string.repetitions_label)
            }
            "Cardio" -> {
                string1 = stringResource(R.string.distance_label)
                string2 = stringResource(R.string.duration_label)
            }
            "Reps only" -> {
                string1 = stringResource(R.string.repetitions_label)
            }
            "Duration" -> {
                string1 = stringResource(R.string.duration_label)
            }
        }

        Row (
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            verticalAlignment = Alignment.CenterVertically
                ) {
            Text (
                modifier = Modifier
                    .weight(0.5f)
                    .padding(4.dp),
                text = stringResource(R.string.set_label),
                color = MaterialTheme.colors.onBackground,
                textAlign = TextAlign.Center
            )

            Text (
                text = string1,
                color = MaterialTheme.colors.onBackground,
                modifier = Modifier
                    .weight(1f)
                    .padding(4.dp),
                textAlign = TextAlign.Center
            )

            if (category != "Reps only" && category != "Duration") {
                Text(
                    text = string2,
                    color = MaterialTheme.colors.onBackground,
                    modifier = Modifier
                        .weight(1f)
                        .padding(4.dp),
                    textAlign = TextAlign.Center
                )
            }

            Spacer(modifier = Modifier.weight(0.5f))
        }
    }
}

@Composable
private fun ExerciseSets(
    sets: List<Record>,
    sessionId: UUID,
    setSize: Int,
    checkedRecords: List<Record>,
    onUpdateRecords: (Record) -> Unit,
    onChangeExercise: (previousSets: Int, sessionPosition: Int) -> Unit,
    onDeleteExercise: (Int) -> Unit,
    onMoveExerciseUp: (Int) -> Unit,
    onMoveExerciseDown: (Int) -> Unit,
    onAddCheckedRecord: (Record) -> Unit,
    onRemoveCheckedRecord: (Record) -> Unit
) {
    val exerciseName = sets[0].exerciseName

    val category = getCategoryWithRecord(sets[0])

    Column (
        modifier = Modifier
            .fillMaxWidth()
            .padding(4.dp)
            .background(color = MaterialTheme.colors.primary)
    ) {
        ExerciseHeader(
            exerciseName = exerciseName,
            sessionPosition = sets[0].sessionPosition,
            category = category,
            setSize = setSize,
            onChangeExercise = { onChangeExercise(sets[sets.lastIndex].setNumber, sets[0].sessionPosition) },
            onDeleteExercise = { onDeleteExercise(sets[0].sessionPosition) },
            onMoveExerciseUp = { onMoveExerciseUp(sets[0].sessionPosition) },
            onMoveExerciseDown = { onMoveExerciseDown(sets[0].sessionPosition) }
        )

        Divider(modifier = Modifier.padding(8.dp))

        sets.forEach { record ->
            var recordIsSaved by rememberSaveable { mutableStateOf (checkedRecords.any {it.id == record.id}) }

            LaunchedEffect(record) {
                recordIsSaved = checkedRecords.any {it.id == record.id}
            }

            SetItem(
                record = record,
                category = category,
                onUpdateRecord = {
                    onUpdateRecords(it)
                },
                onChangeRecordState = {
                    recordIsSaved = ! recordIsSaved
                    if (recordIsSaved) {
                        onAddCheckedRecord(it)
                    } else {
                        onRemoveCheckedRecord(it)
                    }
                                      },
                recordIsSaved = recordIsSaved
            )
        }

        AddSetButton(
            sessionId = sessionId,
            previousSet = sets.last().setNumber,
            exerciseName = exerciseName,
            sessionPosition = sets[0].sessionPosition,
            previousCategory = getCategoryWithRecord(sets[0]),
            onExerciseAdded = { onUpdateRecords(it) }
        )
    }
}

@Composable
private fun SetItem(
    record: Record,
    category: String,
    onUpdateRecord: (Record) -> Unit,
    onChangeRecordState: (Record) -> Unit,
    recordIsSaved: Boolean
) {
    var currentRecord = record

    val default1 = when (category) {
        "General" -> currentRecord.weight.toString()
        "Cardio" -> currentRecord.distance.toString()
        "Reps only" -> currentRecord.repetitions.toString()
        else -> ""
    }

    val default2 = when (category) {
        "General" -> currentRecord.repetitions.toString()
        "Cardio" -> currentRecord.exerciseDuration.toString()
        "Duration" -> currentRecord.exerciseDuration.toString()
        else -> ""
    }

    var value1 by rememberSaveable { mutableStateOf(default1) }
    var value2 by rememberSaveable { mutableStateOf(default2) }

    LaunchedEffect(currentRecord) {
        value1 = default1
        value2 = default2
    }

    Row (
        modifier = Modifier
            .padding(horizontal = 8.dp)
            .fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
            ) {
        Text (
            modifier = Modifier
                .fillMaxSize()
                .weight(0.5f)
                .padding(4.dp),
            text = currentRecord.setNumber.toString(),
            color = MaterialTheme.colors.onBackground,
            textAlign = TextAlign.Center,
        )

        if (category != "Duration") {
            StandardTextField(
                entryValue = value1,
                onValueChange = {
                    value1 = it
                    currentRecord = updateRecordWithCategory(
                        record = currentRecord,
                        value1 = value1,
                        value2 = value2,
                        category = category
                    )
                    onUpdateRecord(currentRecord)
                },
                modifier = Modifier
                    .weight(1f),
                isNumeric = true,
                isEnabled = !recordIsSaved,
                backgroundColor = MaterialTheme.colors.background,
                textColor = MaterialTheme.colors.onPrimary
            )
        }

        if (category != "Reps only") {
            StandardTextField(
                entryValue = value2,
                onValueChange = {
                    value2 = it
                    currentRecord = updateRecordWithCategory(
                        record = currentRecord,
                        value1 = value1,
                        value2 = value2,
                        category = category
                    )
                    onUpdateRecord(currentRecord)
                },
                modifier = Modifier
                    .weight(1f),
                isNumeric = true,
                isEnabled = !recordIsSaved,
                backgroundColor = MaterialTheme.colors.background,
                textColor = MaterialTheme.colors.onPrimary
            )
        }

        Checkbox(
            checked = recordIsSaved,
            onCheckedChange = {
                onChangeRecordState(currentRecord)
            },
            colors = CheckboxDefaults.colors(checkedColor = MaterialTheme.colors.secondary),
            modifier = Modifier.weight(0.5f)
        )
    }
}

//Buttons
@Composable
private fun AddSetButton(
    sessionId: UUID,
    previousSet: Int,
    exerciseName: String,
    sessionPosition: Int,
    previousCategory: String,
    onExerciseAdded: (Record) -> Unit
) {
    StandardButton (
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp),
        onClick = { onExerciseAdded(
            createNewRecord(
                sessionId = sessionId,
                previousSet = previousSet,
                exerciseName = exerciseName,
                sessionPosition = sessionPosition,
                category = previousCategory
            )
        ) },
        text = stringResource(R.string.add_set_button)
    )
}

@Composable
private fun AddExerciseButton(
    selectExercise: () -> Unit
) {
    StandardButton(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp),
        onClick = { selectExercise() },
        text = stringResource(R.string.add_exercise_button)
    )
}

@Composable
private fun CancelButton (
    onCancelSession: () -> Unit
) {
    StandardButton(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp),
        onClick = { onCancelSession() },
        text = stringResource(R.string.cancel_session_button),
        backgroundColor = Color.LightGray,
        textColor = Color.Black
    )
}

//Dialogs
@Composable
private fun SelectExerciseDialog (
    isSwapping: Boolean,
    onDismissRequest: () -> Unit,
    onExerciseSelected: (List<Exercise>) -> Unit
) {
    Dialog (
        properties = DialogProperties(usePlatformDefaultWidth = false),
        onDismissRequest = { onDismissRequest() }
    ) {
        Surface (modifier = Modifier.fillMaxSize()) {
            ExerciseListScreen(
                viewModel = ExerciseViewModel(),
                isBeingSelected = true,
                isSwapping = isSwapping,
                onExercisesSelected = { onExerciseSelected(it) }
            )
        }
    }
}

@Composable
private fun SaveSessionDialog (
    onDismissRequest: () -> Unit,
    onSessionSaved: () -> Unit,
    viewModel: SessionViewModel
) {
    if (viewModel.checkedRecords?.isNotEmpty() == true) {
        StandardTwoButtonsDialog(
            title = stringResource(R.string.save_session_top_message),
            text = stringResource(R.string.save_session_bottom_message),
            onConfirm = { onSessionSaved() },
            onDismissRequest = { onDismissRequest() }
        )
    } else {
        StandardAlertDialog(
            title = stringResource(R.string.save_session_error_message),
            onDismissRequest = { onDismissRequest() }
        )
    }
}

@Composable
private fun DeleteExerciseDialog (
    onDismissRequest: () -> Unit,
    onExerciseDeleted: () -> Unit,
) {
    StandardTwoButtonsDialog(
        title = stringResource(R.string.delete_exercise_dialog_title),
        text = stringResource(R.string.delete_item_dialog_text),
        onConfirm = { onExerciseDeleted() },
        onDismissRequest = { onDismissRequest() }
    )
}

@Composable
private fun ExitSessionDialog(
    onExitSession: () -> Unit,
    onDismissRequest: () -> Unit
) {
    StandardTwoButtonsDialog(
        title = stringResource(R.string.exit_session_dialog_title),
        text = stringResource(R.string.exit_session_dialog_text),
        onConfirm = { onExitSession() },
        onDismissRequest = { onDismissRequest() }
    )
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

//Functions
private fun saveSessionToDb (
    viewModel: SessionViewModel,
    navController: NavController,
    session: Session
) {
    viewModel.viewModelScope.launch {
        viewModel.addSession(session)

        reorderCheckedRecords(viewModel)

        viewModel.checkedRecords!!.forEach { record ->
            viewModel.addRecord(record)
        }

        cancelAndDelete(viewModel, navController)
    }
}

private fun cancelAndDelete(
    viewModel: SessionViewModel,
    navController: NavController
) {
    viewModel.newRecords = emptyList()
    viewModel.createdSession = null
    viewModel.positionBeingModified = null
    viewModel.checkedRecords = null
    navController.navigate("session_list")
}

private fun createNewRecord(
    sessionId: UUID,
    previousSet: Int,
    exerciseName: String,
    sessionPosition: Int,
    category: String
): Record {
    val reps = if (category != "Duration" && category != "Cardio") { 0 } else { null }
    val weight = if (category != "Reps Only" && category != "Duration" && category != "Cardio") { 0f } else { null }
    val duration = if (category == "Duration" || category == "Cardio") { 0.0f } else { null }
    val distance = if (category == "Cardio") { 0.0f } else { null }

    return Record(
        id = UUID.randomUUID(),
        sessionId = sessionId,
        exerciseName = exerciseName,
        sessionPosition = sessionPosition,
        setNumber = previousSet + 1,
        repetitions = reps,
        weight = weight,
        exerciseDuration = duration,
        distance = distance
    )
}

private fun deleteSetInPosition(
    records: List<Record>,
    position: Int,
    moveExercises: Boolean
): List<Record> {
    val newRecords = records.toMutableList()

    var currentPosition = 0

    records.forEach { record ->
        if (record.sessionPosition == position) {
            newRecords.remove(record)
            currentPosition -= 1
        }

        if (record.sessionPosition > position && moveExercises) {
            newRecords[currentPosition] = record.copy(sessionPosition = record.sessionPosition - 1)
        }
        currentPosition ++
    }

    return newRecords.sortedWith(
        compareBy<Record> { it.sessionPosition }
            .thenBy { it.setNumber }
    ).toList()
}

private fun deleteAllRecordsInPosition(
    viewModel: SessionViewModel,
    moveExercises: Boolean = true
) {
    viewModel.newRecords = deleteSetInPosition(
        records = viewModel.newRecords,
        position = viewModel.positionBeingModified!!,
        moveExercises = moveExercises
    )

    viewModel.checkedRecords = deleteSetInPosition(
        records = viewModel.checkedRecords?: emptyList(),
        position = viewModel.positionBeingModified!!,
        moveExercises = moveExercises
    )
}

private fun moveSetInPositionBefore(
    records: List<Record>,
    position: Int
): List<Record> {
    val newRecords = records.toMutableList()
    var currentPosition = 0

    records.forEach { record ->
        if (record.sessionPosition == position) {
            newRecords[currentPosition] = (record.copy(sessionPosition = record.sessionPosition - 1))
        } else if ((position - record.sessionPosition) == 1) {
            newRecords[currentPosition] = (record.copy(sessionPosition = record.sessionPosition + 1))
        }

        currentPosition ++
    }

    return newRecords.sortedWith(
        compareBy<Record> { it.sessionPosition }
            .thenBy { it.setNumber }
    ).toList()
}

private fun moveSetInPositionAfter(
    records: List<Record>,
    position: Int
): List<Record> {
    val newRecords = records.toMutableList()
    var currentPosition = 0

    records.forEach { record ->
        if (record.sessionPosition == position) {
            newRecords[currentPosition] = (record.copy(sessionPosition = record.sessionPosition + 1))
        } else if ((record.sessionPosition - position) == 1) {
            newRecords[currentPosition] = (record.copy(sessionPosition = record.sessionPosition - 1))
        }

        currentPosition ++
    }

    return newRecords.sortedWith(
        compareBy<Record> { it.sessionPosition }
            .thenBy { it.setNumber }
    ).toList()
}

fun getCategoryWithRecord(
    record: Record
): String {
    return if (record.repetitions != null && record.weight != null) { "General" }
    else if (record.exerciseDuration != null && record.distance != null) { "Cardio" }
    else if (record.repetitions != null) { "Reps only" }
    else { "Duration" }

}

fun reorderCheckedRecords(
    viewModel: SessionViewModel
) {
    viewModel.checkedRecords = viewModel.checkedRecords?.sortedWith(
        compareBy<Record> { it.sessionPosition }
            .thenBy { it.setNumber }
    )
    var previousExercise = 1
    var previousSet = 1

    viewModel.checkedRecords!!.forEach { record ->
        if (record.sessionPosition - previousExercise > 1) {
            while (record.sessionPosition - previousExercise != 1) {
                record.sessionPosition -= 1
            }
        }

        if (record.setNumber - previousSet > 1) {
            while (record.setNumber - previousSet != 1) {
                record.setNumber -= 1
            }
        }

        previousSet = if (record.sessionPosition != previousExercise) {
            1
        } else {
            record.setNumber
        }
        previousExercise = record.sessionPosition
    }
}

fun updateRecordWithCategory(
    record: Record,
    value1: String,
    value2: String,
    category: String
): Record {
    when (category) {
        "General" -> {
            if (value1 != "" && value2 != "") {
                record.weight = value1.toFloat()
                record.repetitions = value2.toInt()
            }
        }
        "Cardio" -> {
            if (value1 != "" && value2 != "") {
                record.distance = value1.toFloat()
                record.exerciseDuration = value2.toFloat()
            }
        }
        "Reps only" -> {
            if (value1 != "") {
                record.repetitions = value1.toInt()
            }
        }
        "Duration" -> {
            if (value2 != "") {
                record.exerciseDuration = value2.toFloat()
            }
        }
    }

    return record
}