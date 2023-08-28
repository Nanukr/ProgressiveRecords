package com.rib.progressiverecords.ui

import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.rib.progressiverecords.BottomNavItem
import com.rib.progressiverecords.R

@Preview
@Composable
fun BuilderScreen(
    modifier: Modifier = Modifier
) {
    val navController = rememberNavController()

    Scaffold(
        bottomBar = {
            val bottomBarState = rememberSaveable { (mutableStateOf(true)) }

            val navBackStackEntry by navController.currentBackStackEntryAsState()

            val currentRoute = navBackStackEntry?.destination?.route

            bottomBarState.value = (currentRoute == "session_list" || currentRoute == "exercise")

            BottomNavigationBar(
                items = listOf(
                    BottomNavItem(
                        name = "Sessions",
                        route = "session_screen",
                        icon = painterResource(R.drawable.ic_storage)
                    ),

                    BottomNavItem(
                        name = "Exercises",
                        route = "exercise",
                        icon = painterResource(R.drawable.ic_dumbbell)
                    )
                ),
                navController = navController,
                bottomBarState = bottomBarState,
                onItemClick = { navController.navigate(it.route) }
            )
        }
    ) { it
        Navigation(navController = navController)
    }
}