package com.rib.progressiverecords.ui

import android.text.format.DateFormat
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
        modifier = Modifier.padding(16.dp),
            ) {
        SessionHeader(
            session = session,
            onDeleteSession = { sessionBeingDeleted = true }
        )

        LazyColumn {
            items(exerciseSetsList) { set ->
                SetItem(set = set)
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

    Row (
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(
                modifier = Modifier.padding(8.dp),
                text = session?.sessionName ?: "",
                style = MaterialTheme.typography.h6
            )

            Text(
                modifier = Modifier.padding(4.dp),
                text = DateFormat.format("dd / MMM / yyyy", session?.date ?: Date()).toString(),
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
    set: List<Record>
) {
    Card (
        modifier = Modifier
            .padding(vertical = 8.dp)
            .fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        backgroundColor = MaterialTheme.colors.primary,
        elevation = 4.dp
            ) {
        Column (
            modifier = Modifier
                .padding(8.dp)
        ) {
            Text(
                text = "${set[0].sessionPosition}: ${set[0].exerciseName}",
                color = MaterialTheme.colors.onBackground,
                modifier = Modifier
                    .padding(8.dp)
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
                    text = stringResource(R.string.weight_label),
                    color = MaterialTheme.colors.onBackground,
                    modifier = Modifier
                        .weight(1f)
                        .padding(4.dp),
                    textAlign = TextAlign.Center
                )

                Text(
                    text = stringResource(R.string.repetitions_label),
                    color = MaterialTheme.colors.onBackground,
                    modifier = Modifier
                        .weight(1f)
                        .padding(4.dp),
                    textAlign = TextAlign.Center
                )
            }

            Divider()

            set.forEach { record ->
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
                        text = record.weight.toString(),
                        color = MaterialTheme.colors.onPrimary,
                        textAlign = TextAlign.Center
                    )

                    Text(
                        modifier = Modifier
                            .padding(2.dp)
                            .weight(1f),
                        text = "${record.repetitions} ${stringResource(R.string.reps_label)}",
                        color = MaterialTheme.colors.onPrimary,
                        textAlign = TextAlign.Center
                    )
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