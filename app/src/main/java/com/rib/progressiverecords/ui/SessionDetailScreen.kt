package com.rib.progressiverecords.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavController
import com.rib.progressiverecords.ExerciseSetsList
import com.rib.progressiverecords.R
import com.rib.progressiverecords.SessionViewModel
import com.rib.progressiverecords.model.Record
import com.rib.progressiverecords.model.Session
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun SessionDetailScreen(
    viewModel: SessionViewModel,
    navController: NavController
) {
    Scaffold(
        topBar = { TopBar(
            onClick = {
                viewModel.detailedSession = null
                navController.navigate("session_list")
            },
            icon = painterResource(R.drawable.ic_back),
            contentDescription = stringResource(R.string.back_button_icon_description),
            endAlignment = false
        ) }
    ) { it
        SetList(
            viewModel = viewModel,
            navController = navController
        )
    }

    BackHandler {
        viewModel.detailedSession = null
        navController.navigate("session_list")
    }
}

@Composable
private fun SetList(
    viewModel: SessionViewModel,
    navController: NavController
) {
    var sessionBeingDeleted by rememberSaveable { mutableStateOf(false) }

    val session = viewModel.detailedSession?.session

    var records by remember { mutableStateOf((viewModel.detailedSession?.records ?: emptyList())) }
    records = records.sortedWith(
        compareBy<Record> { it.sessionPosition }
            .thenBy { it.setNumber }
    )

    val exerciseSetsList = ExerciseSetsList().organizeRecords(records).totalSets

    Column (
        modifier = Modifier.padding(8.dp),
            ) {
        SessionHeader(
            session = session,
            onDeleteSession = { sessionBeingDeleted = true }
        )

        LazyColumn (
            modifier = Modifier.padding(vertical = 8.dp)
                ) {
            items(exerciseSetsList) { set ->
                val category = getCategoryWithRecord(set[0])
                SetItem(set = set, category = category)
            }
        }

        if (sessionBeingDeleted) {
            DeleteSessionDialog(
                session = session,
                onDeleteSession = {
                    deleteSessionWithRecords(
                        session = it,
                        viewModel = viewModel,
                        navController = navController
                    )
                },
                onDismissRequest = { sessionBeingDeleted = false }
            )
        }
    }
}

@Composable
private fun SessionHeader (
    session: Session?,
    onDeleteSession: () -> Unit
) {
    var dropdownMenuExpanded by rememberSaveable { mutableStateOf(false) }

    val date = session?.date ?: Date()

    Row (
        modifier = Modifier.padding(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column (
            modifier = Modifier.padding(4.dp)
                ) {
            Text(
                text = session?.sessionName ?: "",
                style = MaterialTheme.typography.h6
            )

            Text(
                text = SimpleDateFormat
                    .getDateInstance(SimpleDateFormat.DEFAULT, Locale.getDefault())
                    .format(date).toString(),
                style = MaterialTheme.typography.body1
            )
        }

        Spacer(modifier = Modifier.weight(1f))

        Box(modifier = Modifier
            .wrapContentSize(Alignment.TopStart)) {
            IconButton(onClick = { dropdownMenuExpanded = true }) {
                Icon(Icons.Default.MoreVert,
                    tint = MaterialTheme.colors.onBackground,
                    contentDescription = stringResource(R.string.edit_detailed_session_icon_description)
                )
            }
            DropdownMenu(
                expanded = dropdownMenuExpanded,
                onDismissRequest = { dropdownMenuExpanded = false }
            ) {
                DropdownMenuItem(onClick = { onDeleteSession() }) {
                    Text(stringResource(R.string.delete_session_button_caption))
                }
            }
        }
    }

    Divider()
}

@Composable
private fun SetItem (
    set: List<Record>,
    category: String
) {
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

    Card (
        modifier = Modifier
            .padding(vertical = 4.dp)
            .fillMaxWidth(),
        backgroundColor = MaterialTheme.colors.primary,
        elevation = 4.dp
            ) {
        Column (
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
        ) {
            Text(
                modifier = Modifier
                    .padding(8.dp),
                text = "${set[0].sessionPosition}: ${set[0].exerciseName}",
                color = MaterialTheme.colors.onBackground,
                style = MaterialTheme.typography.h6
            )

            Row (
                modifier = Modifier
                    .padding(8.dp)
                    .fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    modifier = Modifier
                        .weight(1f)
                        .padding(4.dp)
                        .fillMaxWidth(),
                    text = stringResource(R.string.set_label),
                    color = MaterialTheme.colors.onBackground,
                    textAlign = TextAlign.Center
                )

                Text(
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
            }

            Divider()

            set.forEach { record ->
                var value1 = ""

                var value2  = ""

                when (category) {
                    "General" -> {
                        value1 = record.weight.toString()
                        value2 = record.repetitions.toString()
                    }
                    "Cardio" -> {
                        value1 = record.distance.toString()
                        value2 = record.exerciseDuration.toString()
                    }
                    "Reps only" -> {
                        value1 = record.repetitions.toString()
                    }
                    "Duration" -> {
                        value1 = record.exerciseDuration.toString()
                    }
                }

                Row (
                    modifier = Modifier.padding(8.dp)
                ) {
                    Text(
                        modifier = Modifier
                            .padding(2.dp)
                            .weight(1f),
                        text = record.setNumber.toString(),
                        color = MaterialTheme.colors.onPrimary,
                        textAlign = TextAlign.Center
                    )

                    Text(
                        modifier = Modifier
                            .padding(2.dp)
                            .weight(1f),
                        text = value1,
                        color = MaterialTheme.colors.onPrimary,
                        textAlign = TextAlign.Center
                    )

                    if (category != "Reps only" && category != "Duration") {
                        Text(
                            modifier = Modifier
                                .padding(2.dp)
                                .weight(1f),
                            text = value2,
                            color = MaterialTheme.colors.onPrimary,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun DeleteSessionDialog(
    session: Session?,
    onDeleteSession: (Session) -> Unit,
    onDismissRequest: () -> Unit
) {
    if (session != null) {
        Dialog(onDismissRequest = onDismissRequest) {
            Card (
                backgroundColor = MaterialTheme.colors.primary,
                shape = RoundedCornerShape(16.dp)
            ) {
                Column (
                    modifier = Modifier.padding(8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        modifier = Modifier
                            .fillMaxWidth(),
                        text = stringResource(R.string.delete_session_dialog_top_message),
                        color = MaterialTheme.colors.onPrimary,
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.body1
                    )

                    Text(
                        modifier = Modifier
                            .fillMaxWidth(),
                        text = stringResource(R.string.delete_item_dialog_text),
                        color = MaterialTheme.colors.onPrimary,
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.body2
                    )

                    Row (
                        modifier = Modifier.padding(8.dp),
                        horizontalArrangement = Arrangement.Center
                    ) {
                        TextButton (onClick = { onDismissRequest() }) {
                            Text (
                                text = stringResource(R.string.cancel_button),
                                color = MaterialTheme.colors.secondary
                            )
                        }

                        TextButton (onClick = { onDeleteSession(session) }) {
                            Text (
                                text = stringResource(R.string.confirm_button),
                                color = MaterialTheme.colors.secondary
                            )
                        }
                    }
                }
            }
        }
    }
}

private fun deleteSessionWithRecords(
    session: Session,
    viewModel: SessionViewModel,
    navController: NavController
) {
    viewModel.viewModelScope.launch {
        viewModel.deleteRecordsInSession(session.id)
        viewModel.deleteSession(session)
    }

    viewModel.detailedSession = null
    navController.navigate("session_list")
}