package com.rib.progressiverecords.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.Column
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment.Companion.CenterHorizontally
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.*
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.navigation
import com.rib.progressiverecords.BottomNavItem
import com.rib.progressiverecords.R
import com.rib.progressiverecords.StoreSettings
import com.rib.progressiverecords.ui.exercise.ExerciseListScreen
import com.rib.progressiverecords.ui.session.SessionCreationScreen
import com.rib.progressiverecords.ui.session.SessionDetailScreen
import com.rib.progressiverecords.ui.session.SessionHistoryScreen
import com.rib.progressiverecords.ui.session.SessionTemplatesScreen
import com.rib.progressiverecords.viewModel.SessionViewModel

@Composable
fun Navigation(
    navController: NavHostController,
    settings: StoreSettings
) {
    NavHost(navController = navController, startDestination = "session_screen") {
        navigation(
            startDestination = "session_templates",
            route = "session_screen"
        ) {
            composable("session_templates") {
                val viewModel = it.sharedViewModel<SessionViewModel>(navController)
                SessionTemplatesScreen(viewModel, navController)
            }

            composable("session_list") {
                val viewModel = it.sharedViewModel<SessionViewModel>(navController)
                SessionHistoryScreen(viewModel, navController)
            }

            composable("session_creation") {
                val viewModel = it.sharedViewModel<SessionViewModel>(navController)
                SessionCreationScreen(navController = navController, viewModel = viewModel)
            }

            composable("session_detail") {
                val viewModel = it.sharedViewModel<SessionViewModel>(navController)
                SessionDetailScreen(navController = navController, viewModel = viewModel)
            }
        }

        composable("exercise") {
            ExerciseListScreen(isBeingSelected = false, isSwapping = false, onExercisesSelected = {})
        }

        composable("settings") {
            SettingsScreen(settings = settings)
        }
    }
}

@Composable
fun BottomNavigationBar(
    items: List<BottomNavItem>,
    navController: NavController,
    bottomBarState: MutableState<Boolean>,
    onItemClick: (BottomNavItem) -> Unit
) {
    val backStackEntry = navController.currentBackStackEntryAsState()

    val itemNames = listOf(
        stringResource(R.string.history_nav_item_text),
        stringResource(R.string.train_nav_item_text),
        stringResource(R.string.exercise_nav_item_text),
        stringResource(R.string.settings_nav_item)
    )
    
    AnimatedVisibility(
        visible = bottomBarState.value,
        enter = slideInVertically(initialOffsetY = { it }),
        exit = slideOutVertically(targetOffsetY = { it }),
    ) {
        BottomNavigation (
            backgroundColor = MaterialTheme.colors.primary,
            elevation = 12.dp
        ) {
            items.forEach{ item ->
                val selected = when (item.name) {
                    itemNames[0] -> backStackEntry.value?.destination?.route == "session_list"
                    itemNames[1] -> backStackEntry.value?.destination?.route == "session_templates"
                    itemNames[2] -> backStackEntry.value?.destination?.route == "exercise"
                    itemNames[3] -> backStackEntry.value?.destination?.route == "settings"
                    else -> { false }
                }

                BottomNavigationItem(
                    selected = selected,
                    onClick = { onItemClick(item) },
                    selectedContentColor = MaterialTheme.colors.secondary,
                    unselectedContentColor = MaterialTheme.colors.onPrimary,
                    icon = {
                        Column(horizontalAlignment = CenterHorizontally) {
                            Icon(
                                painter = item.icon,
                                contentDescription = item.name
                            )

                            if (selected) {
                                Text (
                                    text = item.name,
                                    textAlign = TextAlign.Center,
                                    fontSize = 10.sp
                                )
                            }
                        }
                    }
                )
            }
        }
    }
}

@Composable
inline fun <reified T : ViewModel> NavBackStackEntry.sharedViewModel(navController: NavController): T {
    val navGraphRoute = destination.parent?.route ?: return viewModel()
    val parentEntry = remember(this) {
        navController.getBackStackEntry(navGraphRoute)
    }
    return viewModel(parentEntry)
}