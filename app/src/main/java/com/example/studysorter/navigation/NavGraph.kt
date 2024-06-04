package com.example.studysorter.navigation

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.studysorter.screens.DetailScreen
import com.example.studysorter.screens.HomeScreen
import com.example.studysorter.screens.ProfileScreen
import com.example.studysorter.screens.PrzedmiotyScreen
import com.example.studysorter.screens.UlubioneScreen


@Composable
fun SetUpNavGraph(
    navController: NavHostController,
    innerPadding: PaddingValues
) {
    NavHost(navController = navController,
        startDestination = Screens.Home.route){
        composable(Screens.Home.route){
            HomeScreen(innerPadding = innerPadding)
        }
        composable(Screens.Profile.route){
            ProfileScreen()
        }
        composable(Screens.Przedmioty.route){
            PrzedmiotyScreen(innerPadding = innerPadding, navController = navController)
        }
        composable(Screens.Ulubione.route){
            UlubioneScreen(innerPadding = innerPadding, navController = navController)
        }
        composable(
            route = "${Screens.Przedmioty.route}/{subjectId}",
            arguments = listOf(navArgument("subjectId") { type = NavType.StringType })
        ) { backStackEntry ->
            val subjectId = backStackEntry.arguments?.getString("subjectId")
            DetailScreen(subjectId, navController,innerPadding)
        }


    }
}