package com.rib.progressiverecords.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.material.BottomNavigation
import androidx.compose.material.BottomNavigationItem
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment.Companion.CenterHorizontally
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.navigation
import com.rib.progressiverecords.BottomNavItem
import com.rib.progressiverecords.SessionViewModel

@Composable
fun Navigation(navController: NavHostController) {
    NavHost(navController = navController, startDestination = "session_screen") {
        navigation(
            startDestination = "session_list",
            route = "session_screen"
        ) {
            composable("session_list") {
                val viewModel = it.sharedViewModel<SessionViewModel>(navController)
                SessionListScreen(viewModel, navController)
            }

            composable("session_detail") {
                val viewModel = it.sharedViewModel<SessionViewModel>(navController)
                SessionDetailScreen(navController = navController, viewModel = viewModel)
            }
        }

        composable("exercise") {
            ExerciseScreen()
        }
    }
}

@Composable
fun BottomNavigationBar(
    items: List<BottomNavItem>,
    navController: NavController,
    modifier: Modifier = Modifier,
    onItemClick: (BottomNavItem) -> Unit
) {
    val backStackEntry = navController.currentBackStackEntryAsState()
    BottomNavigation (
        modifier = modifier,
        backgroundColor = Color.DarkGray,
        elevation = 5.dp
    ) {
        items.forEach{ item ->
            val selected = if (item.name == "Sessions") {
                item.route == backStackEntry.value?.destination?.parent?.route
            } else {
                item.route == backStackEntry.value?.destination?.route
            }

            BottomNavigationItem(
                selected = selected,
                onClick = { onItemClick(item) },
                selectedContentColor = Color.White,
                unselectedContentColor = Color.Gray,
                icon = {
                    Column(horizontalAlignment = CenterHorizontally) {
                        Icon(imageVector = item.icon, contentDescription = item.name)

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

@Composable
inline fun <reified T : ViewModel> NavBackStackEntry.sharedViewModel(navController: NavController): T {
    val navGraphRoute = destination.parent?.route ?: return viewModel()
    val parentEntry = remember(this) {
        navController.getBackStackEntry(navGraphRoute)
    }
    return viewModel(parentEntry)
}