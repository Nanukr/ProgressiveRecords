package com.rib.progressiverecords.ui.theme

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

@Composable
fun StandardOutlinedButton (
    onClick: () -> Unit,
    text: String,
    modifier: Modifier = Modifier,
    backgroundColor: Color = MaterialTheme.colors.background,
    textColor: Color = MaterialTheme.colors.secondary
) {
    OutlinedButton (
        modifier = modifier.padding(8.dp),
        onClick = { onClick() },
        colors = ButtonDefaults.buttonColors(backgroundColor = backgroundColor),
        border = BorderStroke(width = 0.5.dp, color = textColor)
    ) {
        Text (
            text = text,
            color = textColor
        )
    }
}

@Composable
fun StandardButton (
    onClick: () -> Unit,
    text: String,
    modifier: Modifier = Modifier,
    textAlign: TextAlign = TextAlign.Start,
    backgroundColor: Color = MaterialTheme.colors.primary,
    textColor: Color = MaterialTheme.colors.secondary
) {
    Button (
        modifier = modifier.padding(8.dp),
        onClick = { onClick() },
        colors = ButtonDefaults.buttonColors(backgroundColor = backgroundColor),
    ) {
        Text (
            text = text,
            color = textColor,
            textAlign = textAlign
        )
    }
}

@Composable
fun StandardTextField (
    entryValue: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    isNumeric: Boolean,
    isEnabled: Boolean = true
) {
    var textFieldState by rememberSaveable(stateSaver = TextFieldValue.Saver) {
        mutableStateOf(TextFieldValue(text = entryValue))
    }

    TextField(
        modifier = modifier
            .padding(4.dp),
        value = textFieldState,
        onValueChange = {
            textFieldState = it
            onValueChange(textFieldState.annotatedString.toString())
                        },
        keyboardOptions = KeyboardOptions(keyboardType =
            if (isNumeric) { KeyboardType.Number} else { KeyboardType.Text }),
        singleLine = true,
        enabled = isEnabled,
        colors = TextFieldDefaults.textFieldColors(
            backgroundColor = MaterialTheme.colors.primary,
            textColor = MaterialTheme.colors.onPrimary,
            cursorColor = MaterialTheme.colors.onPrimary,
            focusedIndicatorColor = Color.Transparent,
            unfocusedIndicatorColor = Color.Transparent,
            leadingIconColor = MaterialTheme.colors.onPrimary
        ),
        shape = RoundedCornerShape(4.dp)
    )
}

@Composable
@Preview
private fun StandardOutlinedButtonPreview() {
    StandardOutlinedButton(onClick = {}, text = "Example", modifier = Modifier)
}

@Composable
@Preview
private fun StandardButtonPreview() {
    StandardButton(onClick = {}, text = "Example", modifier = Modifier)
}