package com.joymower.bottomnav

import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.google.firebase.database.DatabaseReference
import com.joymower.screen.HomeScreen
import com.joymower.screen.LogScreen
import com.joymower.screen.MowingScreen
import com.joymower.screen.ProfileScreen
import com.joymower.screen.ReportScreen

@Composable
fun BottomNavGraph(
    navController2: NavHostController,
    navController: NavHostController,
    dbRef: DatabaseReference,
    robotWorkingStatus: MutableState<Boolean>, grassheight: MutableState<Boolean>, batteryStatus: MutableState<Int>, robotMovementStatus: MutableState<Int>, streamingURL: MutableState<String>
) {
    NavHost(
        navController = navController,
        startDestination = BottomBarScreen.Home.route
    ) {
        composable(route = BottomBarScreen.Home.route)
        {
            HomeScreen(robotWorkingStatus, grassheight, batteryStatus, robotMovementStatus)
        }
        composable(route = BottomBarScreen.Report.route)
        {
            ReportScreen(streamingURL)
        }
        composable(route = BottomBarScreen.Mowing.route)
        {
            MowingScreen()
        }
        composable(route = BottomBarScreen.Log.route) {
            LogScreen(dbRef)
        }
        composable(route = BottomBarScreen.Profile.route)
        {
            ProfileScreen(navController2)
        }
    }
}