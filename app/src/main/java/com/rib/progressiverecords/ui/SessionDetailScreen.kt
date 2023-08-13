package com.rib.progressiverecords.ui

import android.text.format.DateFormat
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
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
        SetList(viewModel = viewModel)
    }

    BackHandler {
        viewModel.detailedSession = null
        navController.navigate("session_list")
    }
}

@Composable
private fun SetList(
    viewModel: SessionViewModel
) {
    var sessionBeingDeleted by rememberSaveable { mutableStateOf(false) }

    val session = viewModel.detailedSession?.session

    var records by remember { mutableStateOf((viewModel.detailedSession?.records ?: emptyList())) }
    records = records.sortedWith(
        compareBy<Record> { it.exerciseName }
            .thenBy { it.setNumber }
    )

    val exerciseSetsList = ExerciseSetsList().organizeRecords(records).totalSets

    Column (
        modifier = Modifier.padding(16.dp),
            ) {
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

            Icon(
                Icons.Filled.MoreVert,
                tint = MaterialTheme.colors.onBackground,
                contentDescription = stringResource(R.string.edit_detailed_session_icon_description)
            )
        }

        Divider()

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
                        viewModel = viewModel
                    )
                },
                onDismissRequest = { sessionBeingDeleted = false }
            )
        }
    }
}

@Composable
private fun SetItem (
    set: List<Record>
) {
    Column (
        modifier = Modifier
            .padding(vertical = 16.dp)
            .background(color = MaterialTheme.colors.primary, RoundedCornerShape(16.dp))
            .fillMaxWidth()
            ) {
        Column (
            modifier = Modifier
                .padding(8.dp)
        ) {
            Text(
                text = stringResource(R.string.exercise_label, set[0].exerciseName),
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
                modifier = Modifier.background(color = MaterialTheme.colors.primaryVariant)
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
                        text = stringResource(R.string.delete_session_dialog_bottom_message),
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
    viewModel: SessionViewModel
) {
    viewModel.viewModelScope.launch {
        viewModel.deleteRecordsInSession(session.id)
        viewModel.deleteSession(session)
    }
}