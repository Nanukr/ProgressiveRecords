package com.rib.progressiverecords.ui

import android.content.res.Configuration
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.rib.progressiverecords.BuildConfig
import com.rib.progressiverecords.R
import com.rib.progressiverecords.StoreSettings
import com.rib.progressiverecords.ui.theme.SingleOptionChoosingDialog
import com.rib.progressiverecords.ui.theme.SplashBackground
import kotlinx.coroutines.launch
import java.util.*

@Composable
fun SettingsScreen(
    settings: StoreSettings
) {
    val configuration = LocalConfiguration.current
    val scope = rememberCoroutineScope()

    val themeState = settings.getThemeState.collectAsState(initial = false)
    val locale = settings.getLocale.collectAsState(initial = "en")
    val topBarTitle = stringResource(R.string.settings_nav_item)

    Scaffold(
        topBar = { TopBar(
            onClick = {},
            title = topBarTitle,
            contentDescription = ""
        ) }
    ) {it
        SettingsContent(
            configuration = configuration,
            themeState = themeState.value!!,
            locale = locale.value!!,
            onChangeThemeState = {
                scope.launch {
                    settings.saveThemeState(it)
                }
            },
            onChangeLocale = { locale ->
                scope.launch {
                    settings.saveLocale(locale)
                }
            }
        )
    }
}

@Composable
private fun SettingsContent(
    configuration: Configuration,
    themeState: Boolean,
    locale: String,
    onChangeThemeState: (Boolean) -> Unit,
    onChangeLocale: (String) -> Unit
) {
    Column (
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colors.primary),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.weight(1f))

        Card (
            modifier = Modifier
                .padding(8.dp),
            backgroundColor = MaterialTheme.colors.primary,
            shape = RoundedCornerShape(8.dp),
            border = BorderStroke(width = 1.dp, color = MaterialTheme.colors.secondary)
        ) {
            Column (
                modifier = Modifier.fillMaxWidth(0.75f),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                ThemeSettings(
                    configuration = configuration,
                    themeState = themeState,
                    onChangeThemeState = { onChangeThemeState(it) }
                )

                LanguageSettings(
                    locale = locale,
                    onChangeLocale = { onChangeLocale(it) }
                )
            }
        }

        Spacer(modifier = Modifier.weight(0.75f))

        AppLogoAndVersion()

        Spacer(modifier = Modifier.weight(0.5f))
    }
}

@Composable
private fun ThemeSettings(
    configuration: Configuration,
    themeState: Boolean,
    onChangeThemeState: (Boolean) -> Unit
) {
    Row (
        modifier = Modifier
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ){
        Text(
            text = stringResource(R.string.theme_label)
        )

        Switch(
            checked = themeState,
            onCheckedChange = {
                val newUiMode =
                    if (themeState) Configuration.UI_MODE_NIGHT_YES
                    else Configuration.UI_MODE_NIGHT_NO
                configuration.uiMode = newUiMode
                onChangeThemeState(!themeState)
            }
        )
    }
}

@Composable
private fun LanguageSettings(
    locale: String,
    onChangeLocale: (String) -> Unit
) {
    var isChoosingLanguage by rememberSaveable { mutableStateOf(false) }

    var selectedLanguage by rememberSaveable { mutableStateOf(getLanguageWithLocale(locale)) }

    LaunchedEffect(locale) {
        selectedLanguage = getLanguageWithLocale(locale)
    }

    val languageList = listOf(
        "English",
        "Español",
        "Català"
    )

    Row (
        modifier = Modifier
            .padding(horizontal = 16.dp)
            .clickable { isChoosingLanguage = true },
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        Text (
            modifier = Modifier.padding(4.dp),
            text = stringResource(R.string.language_label),
            color = MaterialTheme.colors.onPrimary
        )

        Text(
            modifier = Modifier.padding(16.dp),
            text = selectedLanguage,
            color = Color.Gray,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )

        Icon(
            painter = painterResource(id = R.drawable.ic_arrow_down),
            contentDescription = "",
            tint = MaterialTheme.colors.onPrimary
        )
    }

    if (isChoosingLanguage) {
        SingleOptionChoosingDialog(
            options = languageList,
            selectedOption = selectedLanguage,
            title = R.string.select_language_dialog_title,
            height = 260,
            changeSelectedOption = { language ->
                val newLocale = when (language) {
                    "Español" -> Locale("es")
                    "Català" -> Locale("ca")
                    else -> Locale("en")
                }
                onChangeLocale(newLocale.language)
                selectedLanguage = language
            },
            onDismissRequest = { isChoosingLanguage = false }
        )
    }
}

@Composable
private fun AppLogoAndVersion() {
    Column (
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Bottom
    ) {
        Card(
            modifier = Modifier.size(150.dp),
            shape = CircleShape,
            backgroundColor = SplashBackground
        ) {
            Image(
                modifier = Modifier.padding(16.dp),
                painter = painterResource(R.drawable.splash),
                contentDescription = ""
            )
        }

        Text(
            modifier = Modifier.fillMaxWidth(),
            text = "v.${BuildConfig.VERSION_NAME}",
            textAlign = TextAlign.Center
        )
    }
}

private fun getLanguageWithLocale(
    locale: String
): String {
    return when (locale) {
        "es" -> "Español"
        "ca" -> "Català"
        else -> "English"
    }
}