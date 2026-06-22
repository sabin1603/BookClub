package com.example.bookclub.navigation

import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
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
import com.example.bookclub.ui.screens.ProfileScreen
import com.example.bookclub.ui.screens.RegisterScreen
import com.example.bookclub.ui.screens.RoomAdminScreen
import com.example.bookclub.ui.screens.RoomDetailsScreen
import com.example.bookclub.ui.screens.RoomMembersScreen
import com.example.bookclub.viewmodel.ProfileViewModel
import com.example.bookclub.viewmodel.RoomViewModel

@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    val app = LocalContext.current.applicationContext as BookClubApplication

    val startDestination = remember {
        if (app.sessionManager.isLoggedIn()) {
            Routes.Home
        } else {
            Routes.Login
        }
    }

    fun navigateToTopLevel(route: String) {
        navController.navigate(route) {
            popUpTo(Routes.Home) {
                saveState = true
            }

            launchSingleTop = true
            restoreState = true
        }
    }

    fun navigateToLoginAfterLogout() {
        navController.navigate(Routes.Login) {
            popUpTo(Routes.Home) {
                inclusive = true
                saveState = false
            }

            launchSingleTop = true
            restoreState = false
        }
    }

    NavHost(
        navController = navController,
        startDestination = startDestination,
        enterTransition = {
            fadeIn(animationSpec = tween(180))
        },
        exitTransition = {
            fadeOut(animationSpec = tween(180))
        },
        popEnterTransition = {
            fadeIn(animationSpec = tween(180))
        },
        popExitTransition = {
            fadeOut(animationSpec = tween(180))
        }
    ) {
        composable(Routes.Login) {
            LoginScreen(
                onLoginSuccess = {
                    navController.navigate(Routes.Home) {
                        popUpTo(Routes.Login) {
                            inclusive = true
                            saveState = false
                        }

                        launchSingleTop = true
                        restoreState = false
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
                            saveState = false
                        }

                        launchSingleTop = true
                        restoreState = false
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
                    navigateToTopLevel(Routes.BookSearch)
                },
                onProfileClick = {
                    navigateToTopLevel(Routes.Profile)
                },
                onRoomClick = { roomId ->
                    navController.navigate(Routes.roomDetails(roomId))
                },
                onLogout = {
                    navigateToLoginAfterLogout()
                }
            )
        }

        composable(Routes.Profile) { backStackEntry ->
            val currentUserId = app.sessionManager.getUserId()

            if (currentUserId == null) {
                LaunchedEffect(Unit) {
                    navigateToLoginAfterLogout()
                }

                return@composable
            }

            val profileViewModel: ProfileViewModel = viewModel(
                viewModelStoreOwner = backStackEntry,
                key = "profile-view-model-$currentUserId"
            )

            LaunchedEffect(
                currentUserId,
                profileViewModel
            ) {
                profileViewModel.loadProfile()
            }

            ProfileScreen(
                onClubsClick = {
                    navigateToTopLevel(Routes.Home)
                },
                onSearchClick = {
                    navigateToTopLevel(Routes.BookSearch)
                },
                onLogout = {
                    navigateToLoginAfterLogout()
                },
                viewModel = profileViewModel
            )
        }

        composable(Routes.BookSearch) {
            BookSearchScreen(
                onBack = {
                    navController.popBackStack()
                },
                onClubsClick = {
                    navigateToTopLevel(Routes.Home)
                },
                onProfileClick = {
                    navigateToTopLevel(Routes.Profile)
                },
                onBookSelected = { bookId ->
                    navController.navigate(
                        Routes.createRoom(bookId)
                    )
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
            val rawBookId =
                backStackEntry.arguments?.getLong("bookId") ?: -1L

            val bookId =
                if (rawBookId == -1L) null else rawBookId

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
                            saveState = false
                        }

                        launchSingleTop = true
                        restoreState = false
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
            val roomId =
                backStackEntry.arguments?.getLong("roomId")
                    ?: return@composable

            RoomDetailsScreen(
                roomId = roomId,
                onBack = {
                    navController.popBackStack()
                },
                onAdminClick = {
                    navController.navigate(
                        Routes.roomSettings(roomId)
                    )
                }
            )
        }

        composable(
            route = Routes.RoomSettings,
            arguments = listOf(
                navArgument("roomId") {
                    type = NavType.LongType
                }
            )
        ) { backStackEntry ->
            val roomId =
                backStackEntry.arguments?.getLong("roomId")
                    ?: return@composable

            val roomViewModel: RoomViewModel = viewModel(
                viewModelStoreOwner = backStackEntry
            )

            val isAdmin by roomViewModel
                .observeIsCurrentUserAdmin(roomId)
                .collectAsState(initial = false)

            if (isAdmin) {
                RoomAdminScreen(
                    roomId = roomId,
                    onBack = {
                        navController.popBackStack()
                    },
                    onRoomDeleted = {
                        navController.navigate(Routes.Home) {
                            popUpTo(Routes.Home) {
                                inclusive = true
                                saveState = false
                            }

                            launchSingleTop = true
                            restoreState = false
                        }
                    },
                    onLeaveRoomSuccess = {
                        navController.navigate(Routes.Home) {
                            popUpTo(Routes.Home) {
                                inclusive = true
                                saveState = false
                            }

                            launchSingleTop = true
                            restoreState = false
                        }
                    },
                    viewModel = roomViewModel
                )
            } else {
                RoomMembersScreen(
                    roomId = roomId,
                    onBack = {
                        navController.popBackStack()
                    },
                    onLeaveRoomSuccess = {
                        navController.navigate(Routes.Home) {
                            popUpTo(Routes.Home) {
                                inclusive = true
                                saveState = false
                            }

                            launchSingleTop = true
                            restoreState = false
                        }
                    },
                    viewModel = roomViewModel
                )
            }
        }
    }
}