package com.rib.progressiverecords.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
fun TopBar(
    onClick: () -> Unit,
    title: String = "",
    icon: Painter? = null,
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
            horizontalArrangement = if (endAlignment) {Arrangement.End} else {Arrangement.Start},
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                modifier = Modifier
                    .then(
                        if (title != "") {
                            Modifier.padding(horizontal = 32.dp).weight(1f)
                        } else {
                            Modifier
                        }
                    ),
                text = title,
                color = MaterialTheme.colors.onPrimary,
                style = MaterialTheme.typography.h5,
                fontWeight = FontWeight.SemiBold
            )

            if (icon != null) {
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
}