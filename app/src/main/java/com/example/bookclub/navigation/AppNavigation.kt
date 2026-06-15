package com.example.bookclub.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.bookclub.BookClubApplication
import com.example.bookclub.ui.screens.BookSearchScreen
import com.example.bookclub.ui.screens.CreateRoomScreen
import com.example.bookclub.ui.screens.HomeScreen
import com.example.bookclub.ui.screens.LoginScreen
import com.example.bookclub.ui.screens.RegisterScreen
import com.example.bookclub.ui.screens.RoomAdminScreen
import com.example.bookclub.ui.screens.RoomDetailsScreen

@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    val app = LocalContext.current.applicationContext as BookClubApplication

    val startDestination = remember {
        if (app.sessionManager.isLoggedIn()) Routes.Home else Routes.Login
    }

    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        composable(Routes.Login) {
            LoginScreen(
                onLoginSuccess = {
                    navController.navigate(Routes.Home) {
                        popUpTo(Routes.Login) {
                            inclusive = true
                        }
                    }
                },
                onRegisterClick = {
                    navController.navigate(Routes.Register)
                }
            )
        }

        composable(Routes.Register) {
            RegisterScreen(
                onRegisterSuccess = {
                    navController.navigate(Routes.Home) {
                        popUpTo(Routes.Login) {
                            inclusive = true
                        }
                    }
                },
                onLoginClick = {
                    navController.popBackStack()
                }
            )
        }

        composable(Routes.Home) {
            HomeScreen(
                onCreateRoomClick = {
                    navController.navigate(Routes.createRoom())
                },
                onBookSearchClick = {
                    navController.navigate(Routes.BookSearch)
                },
                onRoomClick = { roomId ->
                    navController.navigate(Routes.roomDetails(roomId))
                },
                onLogout = {
                    navController.navigate(Routes.Login) {
                        popUpTo(navController.graph.findStartDestination().id) {
                            inclusive = true
                        }
                    }
                }
            )
        }

        composable(Routes.BookSearch) {
            BookSearchScreen(
                onBack = {
                    navController.popBackStack()
                },
                onBookSelected = { bookId ->
                    navController.navigate(Routes.createRoom(bookId))
                }
            )
        }

        composable(
            route = Routes.CreateRoom,
            arguments = listOf(
                navArgument("bookId") {
                    type = NavType.LongType
                    defaultValue = -1L
                }
            )
        ) { backStackEntry ->
            val rawBookId = backStackEntry.arguments?.getLong("bookId") ?: -1L
            val bookId = if (rawBookId == -1L) null else rawBookId

            CreateRoomScreen(
                bookId = bookId,
                onBack = {
                    navController.popBackStack()
                },
                onSearchBook = {
                    navController.navigate(Routes.BookSearch)
                },
                onRoomCreated = {
                    navController.navigate(Routes.Home) {
                        popUpTo(Routes.Home) {
                            inclusive = true
                        }
                    }
                }
            )
        }

        composable(
            route = Routes.RoomDetails,
            arguments = listOf(
                navArgument("roomId") {
                    type = NavType.LongType
                }
            )
        ) { backStackEntry ->
            val roomId = backStackEntry.arguments?.getLong("roomId") ?: return@composable

            RoomDetailsScreen(
                roomId = roomId,
                onBack = {
                    navController.popBackStack()
                },
                onAdminClick = {
                    navController.navigate(Routes.roomAdmin(roomId))
                }
            )
        }

        composable(
            route = Routes.RoomAdmin,
            arguments = listOf(
                navArgument("roomId") {
                    type = NavType.LongType
                }
            )
        ) { backStackEntry ->
            val roomId = backStackEntry.arguments?.getLong("roomId") ?: return@composable

            RoomAdminScreen(
                roomId = roomId,
                onBack = {
                    navController.popBackStack()
                },
                onRoomDeleted = {
                    navController.navigate(Routes.Home) {
                        popUpTo(Routes.Home) {
                            inclusive = true
                        }
                    }
                }
            )
        }
    }
}