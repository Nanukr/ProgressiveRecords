package com.rib.progressiverecords.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.unit.dp

@Composable
fun TopBar(
    onClick: () -> Unit,
    icon: Painter,
    contentDescription: String,
    endAlignment: Boolean = true
) {
    TopAppBar(
        backgroundColor = MaterialTheme.colors.primary,
        contentColor = MaterialTheme.colors.onPrimary,
        elevation = 5.dp,
    ) {
        Row (
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = if (endAlignment) {Arrangement.End} else {Arrangement.Start}
        ) {
            IconButton(
                modifier = Modifier.padding(8.dp),
                onClick = { onClick() }
            ) {
                Icon(
                    icon,
                    contentDescription = contentDescription
                )
            }
        }
    }
}