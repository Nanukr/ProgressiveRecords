package com.rib.progressiverecords.ui.theme

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.rib.progressiverecords.R

@Composable
fun StandardOutlinedButton (
    onClick: () -> Unit,
    text: String,
    modifier: Modifier = Modifier,
    backgroundColor: Color = Color.Transparent,
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
    modifier: Modifier = Modifier,
    entryValue: String,
    onValueChange: (String) -> Unit,
    isNumeric: Boolean,
    isEnabled: Boolean = true,
    readOnly: Boolean = false,
    borderColor: Color = Color.Transparent,
    textAlign: TextAlign = TextAlign.Center,
    trailingIcon: Int = 0
) {
    var textFieldState by rememberSaveable(stateSaver = TextFieldValue.Saver) {
        mutableStateOf(TextFieldValue(text = entryValue))
    }

    OutlinedTextField(
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
        readOnly = readOnly,
        colors = TextFieldDefaults.outlinedTextFieldColors(
            backgroundColor = MaterialTheme.colors.primary,
            textColor = MaterialTheme.colors.onPrimary,
            cursorColor = MaterialTheme.colors.onPrimary,
            focusedBorderColor = borderColor,
            unfocusedBorderColor = borderColor,
            disabledBorderColor = if (isEnabled) {
                MaterialTheme.colors.onSurface.copy(alpha = ContentAlpha.disabled)
            } else { borderColor },
            leadingIconColor = MaterialTheme.colors.onPrimary,
        ),
        textStyle = TextStyle(textAlign = textAlign),
        shape = RoundedCornerShape(4.dp),
        trailingIcon = {
            if(trailingIcon != 0) {
                Icon(
                    painter = painterResource(id = trailingIcon),
                    contentDescription = "",
                    tint = MaterialTheme.colors.onPrimary
                )
            }
        }
    )
}

@Composable
fun SingleOptionChoosingDialog(
    options: List<String>,
    selectedOption: String,
    title: Int,
    changeSelectedOption: (String) -> Unit,
    onDismissRequest: () -> Unit
) {
    Dialog (onDismissRequest = { onDismissRequest() }) {
        Card (
            modifier = Modifier
                .size(width = 250.dp, height = 600.dp),
            backgroundColor = MaterialTheme.colors.primary,
            shape = RoundedCornerShape(16.dp)
        ) {
            Column (
                modifier = Modifier
                    .padding(16.dp)
            ) {
                Text (
                    modifier = Modifier.padding(8.dp),
                    text = stringResource(title),
                    color = MaterialTheme.colors.onPrimary,
                    style = MaterialTheme.typography.h6
                )

                Divider()

                Column (
                    modifier = Modifier
                        .height(490.dp)
                ) {
                    LazyColumn {
                        items(options) { option ->
                            Row (
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .selectable(
                                        selected = (selectedOption == option),
                                        onClick = { changeSelectedOption(option) }
                                    )
                                    .padding(horizontal = 2.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                RadioButton(
                                    selected = (selectedOption == option),
                                    onClick = { changeSelectedOption(option) }
                                )

                                Spacer(modifier = Modifier.width(16.dp))

                                Text(
                                    text = option,
                                    color = MaterialTheme.colors.onPrimary
                                )
                            }
                        }
                    }
                }

                Divider()

                Row {
                    Spacer(modifier = Modifier.weight(1f))

                    TextButton(onClick = { onDismissRequest() }) {
                        Text(
                            text = stringResource(R.string.confirm_button),
                            color = MaterialTheme.colors.secondary
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun MultipleOptionsChoosingDialog(
    title: Int,
    options: List<String>,
    selectedOptions: List<String>,
    addSelectedOption: (String) -> Unit,
    removeSelectedOption: (String) -> Unit,
    onDismissRequest: () -> Unit
) {
    Dialog (onDismissRequest = { onDismissRequest() }) {
        Card (
            modifier = Modifier
                .size(width = 250.dp, height = 600.dp),
            backgroundColor = MaterialTheme.colors.primary,
            shape = RoundedCornerShape(16.dp)
        ) {
            Column (
                modifier = Modifier
                    .padding(16.dp)
            ) {
                Text (
                    modifier = Modifier.padding(8.dp),
                    text = stringResource(title),
                    color = MaterialTheme.colors.onPrimary,
                    style = MaterialTheme.typography.h6
                )

                Divider()

                MultipleOptionsChoosingColumn(
                    options = options,
                    selectedOptions = selectedOptions,
                    addSelectedOption = addSelectedOption,
                    removeSelectedOption = removeSelectedOption
                )

                Divider()

                Row {
                    Spacer(modifier = Modifier.weight(1f))

                    TextButton(onClick = { onDismissRequest() }) {
                        Text(
                            text = stringResource(R.string.confirm_button),
                            color = MaterialTheme.colors.secondary
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun MultipleOptionsChoosingColumn(
    options: List<String>,
    selectedOptions: List<String>,
    addSelectedOption: (String) -> Unit,
    removeSelectedOption: (String) -> Unit
) {
    Column (
        modifier = Modifier
            .height(490.dp)
    ) {
        LazyColumn {
            items(options) { option ->
                val isSelected = selectedOptions.contains(option)
                Row (
                    modifier = Modifier
                        .fillMaxWidth()
                        .selectable(
                            selected = (isSelected),
                            onClick = {
                                if (isSelected) {
                                    removeSelectedOption(option)
                                } else {
                                    addSelectedOption(option)
                                }
                            }
                        )
                        .padding(horizontal = 2.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = isSelected,
                        onCheckedChange = {
                            if (isSelected) {
                                removeSelectedOption(option)
                            } else {
                                addSelectedOption(option)
                            }
                        }
                    )

                    Spacer(modifier = Modifier.width(16.dp))

                    Text(
                        text = option,
                        color = MaterialTheme.colors.onPrimary
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun SearchBar(
    modifier: Modifier = Modifier,
    hint: String = "Search...",
    onSearch: (String) -> Unit
) {
    var text by rememberSaveable { mutableStateOf("") }
    var isTrailingDisplayed by rememberSaveable { mutableStateOf(false) }

    val keyboardController = LocalSoftwareKeyboardController.current
    val focusManager = LocalFocusManager.current

    Box (modifier = modifier) {
        TextField(
            value = text,
            onValueChange = {
                text = it
                onSearch(it)
            },
            maxLines = 1,
            singleLine = true,
            textStyle = TextStyle(color = MaterialTheme.colors.onPrimary),
            modifier = Modifier
                .fillMaxWidth()
                .shadow(5.dp, CircleShape)
                .background(MaterialTheme.colors.primary)
                .padding(horizontal = 8.dp)
                .onFocusChanged {
                    isTrailingDisplayed = it.isFocused
                }
            ,
            colors = TextFieldDefaults.textFieldColors(
                backgroundColor = MaterialTheme.colors.primary,
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent,
                cursorColor = MaterialTheme.colors.onPrimary
            ),
            keyboardOptions = KeyboardOptions(
                imeAction = ImeAction.Search
            ),
            keyboardActions = KeyboardActions(
                onSearch = {
                    isTrailingDisplayed = false
                    keyboardController?.hide()
                    focusManager.clearFocus()
                }
            ),
            label = {
                Text(
                    text = hint,
                    color = Color.LightGray
                )
            },
            leadingIcon = {
                Icon(
                    painter = painterResource(R.drawable.ic_search),
                    contentDescription = "",
                    tint = MaterialTheme.colors.onPrimary
                )
            },
            trailingIcon = {
                if (isTrailingDisplayed) {
                    Icon(
                        modifier = Modifier
                            .clickable{
                                text = ""
                                onSearch(text)
                            },
                        painter = painterResource(R.drawable.ic_close),
                        contentDescription = "",
                        tint = MaterialTheme.colors.onPrimary
                    )
                }
            }
        )
    }
}

@Composable
@Preview
private fun StandardOutlinedButtonPreview() {
    ProgressiveRecordsTheme {
        StandardOutlinedButton(onClick = {}, text = "Example", modifier = Modifier)
    }
}

@Composable
@Preview
private fun StandardButtonPreview() {
    ProgressiveRecordsTheme {
        StandardButton(onClick = {}, text = "Example", modifier = Modifier)
    }
}

@Composable
@Preview
private fun StandardTextFieldPreview() {
    ProgressiveRecordsTheme {
        StandardTextField(entryValue = "Example", onValueChange = {}, isNumeric = false)
    }
}

@Composable
@Preview
private fun MultipleOptionsChoosingDialogPreview() {
    ProgressiveRecordsTheme {
        MultipleOptionsChoosingDialog(
            title = R.string.upsert_secondary_muscles_long_caption,
            options = listOf("Option1", "Option2", "Option3", "Option4"),
            selectedOptions = listOf("Option1", "Option3"),
            addSelectedOption = {},
            removeSelectedOption = {},
            onDismissRequest = {}
        )
    }
}

@Composable
@Preview
private fun SearchBarPreview() {
    ProgressiveRecordsTheme {
        SearchBar(onSearch = {})
    }
}