package com.example.studysorter.screens

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.studysorter.navigation.BottomNavigationBar
import com.example.studysorter.navigation.NavigationItem
import com.example.studysorter.navigation.Screens
import com.example.studysorter.navigation.SetUpNavGraph


@Composable
fun MainScreen() {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route ?: ""

    val bottomNavigationItemsList = listOf(
        NavigationItem(
            title = "Home",
            route = "home",
            selectedIcon = Icons.Default.Home,
            unSelectedIcon = Icons.Default.Home
        ),
        NavigationItem(
            title = "Profile",
            route = "profile",
            selectedIcon = Icons.Default.Person,
            unSelectedIcon = Icons.Default.Person
        ),
        NavigationItem(
            title = "Przedmioty",
            route = "przedmioty",
            selectedIcon = Icons.Default.AddCircle,
            unSelectedIcon = Icons.Default.AddCircle
        ),
        NavigationItem(
            title = "Ulubione",
            route = "ulubione",
            selectedIcon = Icons.Default.Favorite,
            unSelectedIcon = Icons.Default.Favorite
        )
    )

    val topBarTitle = deriveTopBarTitle(currentRoute)

    Scaffold(
        topBar = { TopBar(title = topBarTitle) },
        bottomBar = {
            BottomNavigationBar(
                items = bottomNavigationItemsList,
                currentRoute = currentRoute,
                onClick = {  currentNavigationItem ->
                    navController.navigate(currentNavigationItem.route) {
                        popUpTo(navController.graph.startDestinationRoute!!) {
                            saveState = true
                        }
                        launchSingleTop = true
                        restoreState = true
                    }
                }
            )
        }
    ) { innerPadding ->
        SetUpNavGraph(navController = navController, innerPadding = innerPadding)
    }
}

@Composable
private fun deriveTopBarTitle(currentRoute: String): String {
    return when (currentRoute) {
        "home" -> "Home"
        "profile" -> "Profile"
        "przedmioty" -> "Przedmioty"
        "ulubione" -> "Ulubione"
        "${Screens.Przedmioty.route}/{subjectId}" -> "Przedmiot"
        else -> "Unknown"
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TopBar(title: String) {
    TopAppBar(title = { Text(text = title) })
}

