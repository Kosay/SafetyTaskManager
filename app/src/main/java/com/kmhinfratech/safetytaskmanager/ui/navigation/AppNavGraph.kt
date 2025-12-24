package com.kmhinfratech.safetytaskmanager.ui.navigation

import androidx.compose.runtime.Composable
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import androidx.navigation.NavHostController
import com.kmhinfratech.safetytaskmanager.ui.screens.home.HomeScreen
import com.kmhinfratech.safetytaskmanager.ui.screens.login.LoginScreen
import com.kmhinfratech.safetytaskmanager.ui.screens.taskdetails.TaskDetailsScreen
import com.kmhinfratech.safetytaskmanager.ui.screens.submit.SubmitTaskScreen
import com.kmhinfratech.safetytaskmanager.ui.screens.splash.SplashRoute

@Composable
fun AppNavGraph(navController: NavHostController) {
    NavHost(navController = navController, startDestination = "splash") {

        composable("splash") {
            SplashRoute(
                onGoLogin = { navController.navigate(Routes.Login) { popUpTo("splash") { inclusive = true } } },
                onGoHome = { navController.navigate(Routes.Home) { popUpTo("splash") { inclusive = true } } }
            )
        }

        composable(Routes.Login) {
            LoginScreen(
                onLoginSuccess = {
                    navController.navigate(Routes.Home) { popUpTo(Routes.Login) { inclusive = true } }
                }
            )
        }

        composable(Routes.Home) {
            HomeScreen(
                onOpenTask = { taskId ->
                    navController.navigate("${Routes.TaskDetails}/$taskId")
                },
                onLogout = {
                    navController.navigate(Routes.Login) { popUpTo(Routes.Home) { inclusive = true } }
                }
            )
        }

        composable(
            route = "${Routes.TaskDetails}/{taskId}",
            arguments = listOf(navArgument("taskId") { type = NavType.StringType })
        ) {
            val taskId = it.arguments?.getString("taskId")!!
            TaskDetailsScreen(
                taskId = taskId,
                onBack = { navController.popBackStack() },
                onSubmit = { navController.navigate("${Routes.SubmitTask}/$taskId") }
            )
        }

        composable(
            route = "${Routes.SubmitTask}/{taskId}",
            arguments = listOf(navArgument("taskId") { type = NavType.StringType })
        ) {
            val taskId = it.arguments?.getString("taskId")!!
            SubmitTaskScreen(
                taskId = taskId,
                onBack = { navController.popBackStack() },
                onSubmitted = {
                    navController.popBackStack() // back to details
                    navController.popBackStack() // back to home (optional)
                    navController.navigate(Routes.Home)
                }
            )
        }
    }
}
