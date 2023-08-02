package com.rib.progressiverecords.ui

import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun TopBar(
    onClick: () -> Unit
) {
    TopAppBar(
        title = { Text(text="") },
        backgroundColor = MaterialTheme.colors.primaryVariant,
        contentColor = MaterialTheme.colors.onPrimary,
        elevation = 5.dp,
        actions = {
            IconButton(onClick = { onClick() }) {
                Icon(Icons.Filled.Add, contentDescription = "Create session")
            }
        }
    )
}

@Composable
fun SessionDetailTopBar (
    onClick: () -> Unit
) {
    TopAppBar(
        title = { Text(text="") },
        backgroundColor = MaterialTheme.colors.primaryVariant,
        contentColor = MaterialTheme.colors.onPrimary,
        elevation = 5.dp,
        actions = {
            TextButton(onClick = { onClick() }) {
                Text(text = "Exit and save", color = Color.White)
            }
        }
    )
}