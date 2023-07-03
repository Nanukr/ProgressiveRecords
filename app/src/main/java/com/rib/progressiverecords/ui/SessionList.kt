package com.rib.progressiverecords.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.modifier.modifierLocalConsumer
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.rib.progressiverecords.SessionListViewModel
import com.rib.progressiverecords.model.Record
import com.rib.progressiverecords.model.Session
import kotlinx.coroutines.flow.forEach

@Composable
fun SessionList(
    modifier: Modifier = Modifier,
    viewModel: SessionListViewModel
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
    session: Session
) {
    val records = session.recordId

    Column (
        modifier = modifier
            .padding(16.dp)
    ) {
        Text(
            text = session.sessionName,
            style = MaterialTheme.typography.h4
        )
    }
}