package com.rib.progressiverecords.ui

import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Create
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.Modifier
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.rib.progressiverecords.BottomNavItem

@Preview
@Composable
fun BuilderScreen(
    modifier: Modifier = Modifier
) {
    val navController = rememberNavController()

    Scaffold(
        bottomBar = {
            val navBackStackEntry by navController.currentBackStackEntryAsState()

            if (navBackStackEntry?.destination?.route != "session_detail") {
                BottomNavigationBar(
                    items = listOf(
                        BottomNavItem(
                            name = "Sessions",
                            route = "session_screen",
                            icon = Icons.Default.Add
                        ),

                        BottomNavItem(
                            name = "Exercises",
                            route = "exercise",
                            icon = Icons.Default.Create
                        )
                    ),
                    navController = navController,
                    onItemClick = {
                        navController.navigate(it.route)
                    }
                )
            }
        }
    ) { it
        Navigation(navController = navController)
    }
}