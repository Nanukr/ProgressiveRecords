package com.rib.progressiverecords.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
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
    var session = viewModel.detailedSession.collectAsState().value

    if (session == null) {
        session = SessionWithRecords(Session(id = UUID.randomUUID(), date = Date(), sessionName = ""), emptyList())
        viewModel.changeDetailedSession(session)
    }

    val sessionId = session.session.id

    var records = session.records

    Column (
        modifier = Modifier.fillMaxSize()
            ) {
        SetList(sessionId = sessionId, records = records, onUpdateRecords = { records = records + it })

        Spacer(modifier = Modifier.weight(1f))

        TextButton(
            onClick = { cancelAndDelete(viewModel, navController) },
            modifier = Modifier.fillMaxWidth().padding(16.dp)
        ) {
            Text(text = "Cancel and discard changes", color = Color.Red)
        }
    }
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
    Row {
        Text(text = record.exerciseName)
    }
}

@Composable
private fun AddExerciseButton(
    sessionId: UUID,
    onExerciseAdded: (Record) -> Unit
) {
    TextButton(onClick = {
        onExerciseAdded(createNewRecord(sessionId = sessionId, previousSet = 0))
    },
        modifier = Modifier.fillMaxWidth().padding(16.dp)
    ) {
        Text(text = "Add exercise")
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
        exerciseName = "",
        repetitions = 0,
        weight = 0,
        setNumber = previousSet + 1
    )
}