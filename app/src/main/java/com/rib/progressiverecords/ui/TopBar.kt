package com.rib.progressiverecords.ui

import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.Text
import androidx.compose.material.TopAppBar
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
        backgroundColor = Color.DarkGray,
        contentColor = Color.White,
        elevation = 5.dp,
        actions = {
            IconButton(onClick = { onClick() }) {
                Icon(Icons.Filled.Add, contentDescription = "Create session")
            }
        }
    )
}