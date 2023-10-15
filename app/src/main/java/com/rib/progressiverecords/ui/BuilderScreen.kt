package com.rib.progressiverecords.ui

import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.rib.progressiverecords.BottomNavItem
import com.rib.progressiverecords.R
import com.rib.progressiverecords.StoreSettings
import java.util.*

@Composable
fun BuilderScreen(
    settings: StoreSettings
) {
    val navController = rememberNavController()

    val storedLocale = settings.getLocale.collectAsState(initial = "en").value ?: "en"
    val newLocale = Locale(storedLocale)

    val configuration = LocalConfiguration.current
    val context = LocalContext.current
    val resources = context.resources

    configuration.setLocale(newLocale)
    resources.updateConfiguration(configuration, resources.displayMetrics)

    Scaffold(
        bottomBar = {
            val bottomBarState = rememberSaveable { (mutableStateOf(true)) }

            val navBackStackEntry by navController.currentBackStackEntryAsState()

            val currentRoute = navBackStackEntry?.destination?.route

            val itemNames = listOf(
                stringResource(R.string.history_nav_item_text),
                stringResource(R.string.train_nav_item_text),
                stringResource(R.string.exercise_nav_item_text),
                stringResource(R.string.settings_nav_item)
            )

            bottomBarState.value = (
                    currentRoute == "session_list" ||
                    currentRoute == "exercise" ||
                    currentRoute == "session_templates" ||
                    currentRoute == "settings"
            )

            BottomNavigationBar(
                items = listOf(
                    BottomNavItem(
                        name = itemNames[0],
                        route = "session_list",
                        icon = painterResource(R.drawable.ic_history)
                    ),

                    BottomNavItem(
                        name = itemNames[1],
                        route = "session_templates",
                        icon = painterResource(R.drawable.ic_dumbbell)
                    ),

                    BottomNavItem(
                        name = itemNames[2],
                        route = "exercise",
                        icon = painterResource(R.drawable.ic_storage)
                    ),

                    BottomNavItem(
                        name = itemNames[3],
                        route = "settings",
                        icon = painterResource(R.drawable.ic_settings)
                    )
                ),
                navController = navController,
                bottomBarState = bottomBarState,
                onItemClick = { navController.navigate(it.route) }
            )
        }
    ) { it
        Navigation(navController = navController, settings = settings)
    }
}