package com.rib.progressiverecords.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Preview
@Composable
fun BuilderScreen(
    modifier: Modifier = Modifier
) {
    Column {
        ProgramItem()
    }
}

@Composable
private fun ProgramList(
    modifier: Modifier = Modifier
) {

}

@Composable
private fun ProgramItem(
    modifier: Modifier = Modifier
) {
    Column (
        modifier = modifier
            .padding(16.dp)
            ) {
        Row (
            verticalAlignment = Alignment.CenterVertically
                ){
            Text(
                text = "Program name",
                style = MaterialTheme.typography.h5
            )

            Row (
                horizontalArrangement = Arrangement.End,
                modifier = modifier.fillMaxWidth()
                    ) {
                Text(
                    text = "3 / Week",
                    style = MaterialTheme.typography.body1
                )
                Text(
                    text = "15 Exercises",
                    style = MaterialTheme.typography.body1,
                    modifier = modifier.padding(start = 8.dp)
                )
            }
        }
        Text(
            text = "Day 1",
            style = MaterialTheme.typography.h6
        )
    }
}