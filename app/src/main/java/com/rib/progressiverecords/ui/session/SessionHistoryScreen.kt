package com.rib.progressiverecords.ui.session

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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.rib.progressiverecords.ExerciseSetsList
import com.rib.progressiverecords.R
import com.rib.progressiverecords.viewModel.SessionViewModel
import com.rib.progressiverecords.model.Record
import com.rib.progressiverecords.model.Session
import com.rib.progressiverecords.model.relations.SessionWithRecords
import com.rib.progressiverecords.ui.TopBar
import com.rib.progressiverecords.ui.theme.EditOrDeleteDropdownMenu
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun SessionHistoryScreen(
    viewModel: SessionViewModel,
    navController: NavController
) {
    Scaffold(
        topBar = { TopBar(
            onClick = {},
            title = stringResource(R.string.history_nav_item_text),
            contentDescription = stringResource(R.string.create_session_icon_description)
        ) }
    ) { it
        SessionList(
            viewModel = viewModel,
            areTemplates = false,
            onSelectSession = {
                viewModel.detailedSession = it
                navController.navigate("session_detail")
            }
        )
    }
    BackHandler {}
}

@Composable
fun SessionList(
    viewModel: SessionViewModel,
    areTemplates: Boolean,
    onSelectSession: (SessionWithRecords) -> Unit,
    onEditTemplate: (SessionWithRecords) -> Unit = {},
    onDeleteTemplate: (Session) -> Unit = {},
) {
    val emptyListMessage = when (areTemplates) {
        true -> stringResource(R.string.empty_template_list_message)
        false -> stringResource(R.string.empty_session_list_message)
    }

    val sessions = viewModel.sessions.collectAsState().value
        .filter {
            it.session.isTemplate == if (areTemplates) { 1 } else { 0 }
        }
        .sortedWith(compareByDescending { it.session.date })

    if (sessions.isEmpty()) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(color = MaterialTheme.colors.background),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = emptyListMessage,
                color = MaterialTheme.colors.onPrimary
            )
        }
    } else {
        LazyColumn(
            modifier = Modifier.padding(8.dp)
        ) {
            items(sessions) {session ->
                SessionItem(
                    session = session,
                    areTemplates = areTemplates,
                    onSelectSession = {
                        onSelectSession(it)
                    },
                    onEditTemplate = { onEditTemplate(it) },
                    onDeleteTemplate = { onDeleteTemplate(it) }
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
    areTemplates: Boolean,
    onSelectSession: (SessionWithRecords) -> Unit,
    onEditTemplate: (SessionWithRecords) -> Unit,
    onDeleteTemplate: (Session) -> Unit,
) {
    var records by remember { mutableStateOf((session.records)) }
    records = records.sortedWith(
        compareBy<Record> { it.sessionPosition }
            .thenBy { it.setNumber }
    )

    LaunchedEffect(session) {
        records = session.records
    }

    val exerciseSetsList = ExerciseSetsList().organizeRecords(records).totalSets

    var currentSet = 0

    Card (
        modifier = Modifier
            .padding(8.dp)
            .fillMaxWidth()
            .clickable { onSelectSession(session) },
        shape = RoundedCornerShape(8.dp),
        backgroundColor = MaterialTheme.colors.primary,
        elevation = 4.dp
    ) {
        Column (
            modifier = Modifier
                .padding(8.dp)
                ) {

            Row (
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column (
                    modifier = Modifier.weight(1f)
                        ) {
                    Text(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        text = session.session.sessionName,
                        style = MaterialTheme.typography.h6,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    Text(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        text = "${exerciseSetsList.size} " + stringResource(R.string.exercise_nav_item_text),
                        style = MaterialTheme.typography.body2,
                        fontStyle = FontStyle.Italic,
                    )
                }

                if (!areTemplates) {
                    Text(
                        modifier = Modifier.padding(8.dp),
                        text = SimpleDateFormat
                            .getDateInstance(SimpleDateFormat.DEFAULT, Locale.getDefault())
                            .format(session.session.date).toString(),
                        style = MaterialTheme.typography.body1
                    )
                } else {
                    EditOrDeleteDropdownMenu(
                        onEdit = { onEditTemplate(session) },
                        onDelete = { onDeleteTemplate(session.session) }
                    )
                }
            }

            Divider()

            exerciseSetsList.forEach {set ->
                if (set.isNotEmpty() && currentSet != 5) {
                    val lastSet = set[set.lastIndex]
                    Row {
                        Text(
                            modifier = Modifier.padding(4.dp).weight(1f),
                            text = lastSet.exerciseName,
                            style = MaterialTheme.typography.body1,
                            color = MaterialTheme.colors.onPrimary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )

                        Text(
                            modifier = Modifier.padding(4.dp),
                            text = "x ${lastSet.setNumber}",
                            style = MaterialTheme.typography.body1,
                            color = MaterialTheme.colors.onPrimary,
                        )
                    }
                    currentSet ++
                }
            }

            if (exerciseSetsList.size > 5) {
                Text(
                    modifier = Modifier.padding(8.dp),
                    text = "+ ${exerciseSetsList.size - 5}",
                    color = MaterialTheme.colors.onPrimary,
                    style = MaterialTheme.typography.body2,
                    fontStyle = FontStyle.Italic
                )
            }
        }
    }
}