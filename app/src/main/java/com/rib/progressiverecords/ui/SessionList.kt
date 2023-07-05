package com.rib.progressiverecords.ui

import android.annotation.SuppressLint
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.FloatingActionButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.material.Icon
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.rib.progressiverecords.SessionListViewModel
import com.rib.progressiverecords.model.relations.SessionWithRecords

@SuppressLint("UnusedMaterialScaffoldPaddingParameter")
@Composable
fun SessionScreen() {
    Scaffold(
        floatingActionButton = { SessionActionButton() },
        content = {SessionList()}
    )
}

@Preview
@Composable
fun SessionActionButton() {
    FloatingActionButton(
        onClick = { /*TODO*/}
    ) {
        Icon(Icons.Default.Add, contentDescription = "Create new session")
    }
}

@Composable
fun SessionList(
    modifier: Modifier = Modifier,
    viewModel: SessionListViewModel = viewModel()
) {
    val sessions = viewModel.sessions
    LazyColumn(
        modifier = modifier
    ) {
        items(sessions.value) {session ->
            SessionItem(session = session)
        }
    }
}

@Composable
private fun SessionItem(
    modifier: Modifier = Modifier,
    session: SessionWithRecords
) {
    val records = session.records

    Column (
        modifier = modifier
            .padding(16.dp)
    ) {
        Text(
            text = session.session.sessionName,
            style = MaterialTheme.typography.h4
        )

        records.forEach {record ->
            val exerciseName = record.exerciseName
            Row (
                modifier = modifier
                    ) {
                Text(
                    text = "$exerciseName x ",
                    style = MaterialTheme.typography.h6
                )
                Text(
                    text = "${record.repetitions}",
                    style = MaterialTheme.typography.h6
                )
            }
        }
    }
}