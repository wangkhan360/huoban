package com.growthpet.student

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.growthpet.student.ui.MainViewModel
import com.growthpet.student.ui.auxiliary.ConversationHistoryScreen
import com.growthpet.student.ui.auxiliary.SettingsAndProfileScreen
import com.growthpet.student.ui.auxiliary.SubmittedRecordingsScreen
import com.growthpet.student.ui.chat.MainChatScreen
import com.growthpet.student.ui.onboarding.OnboardingFlowScreen
import com.growthpet.student.ui.theme.GrowthPetTheme
import dagger.hilt.android.AndroidEntryPoint

private object Routes {
    const val ONBOARDING = "onboarding"
    const val MAIN_CHAT = "main_chat"
    const val HISTORY = "history"
    const val RECORDINGS = "recordings"
    const val SETTINGS = "settings"
}

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            GrowthPetTheme {
                val navController = rememberNavController()
                val viewModel: MainViewModel = hiltViewModel()
                val uiState by viewModel.uiState.collectAsState()
                val companionState by viewModel.companionState.collectAsState()

                val startDestination = if (uiState.prefs.isBoundAndOnboarded) {
                    Routes.MAIN_CHAT
                } else {
                    Routes.ONBOARDING
                }

                NavHost(
                    navController = navController,
                    startDestination = startDestination
                ) {
                    composable(Routes.ONBOARDING) {
                        OnboardingFlowScreen(
                            onVerifyCode = viewModel::verifyCodeAndBind,
                            onCompleteOnboarding = { cid, cname, gLevel, gBand, pName, compName, summary ->
                                viewModel.finishOnboarding(cid, cname, gLevel, gBand, pName, compName, summary)
                                navController.navigate(Routes.MAIN_CHAT) {
                                    popUpTo(Routes.ONBOARDING) { inclusive = true }
                                }
                            }
                        )
                    }

                    composable(Routes.MAIN_CHAT) {
                        MainChatScreen(
                            uiState = uiState,
                            companionState = companionState,
                            viewModel = viewModel,
                            onOpenHistory = { navController.navigate(Routes.HISTORY) },
                            onOpenRecordings = { navController.navigate(Routes.RECORDINGS) },
                            onOpenSettings = { navController.navigate(Routes.SETTINGS) }
                        )
                    }

                    composable(Routes.HISTORY) {
                        ConversationHistoryScreen(
                            uiState = uiState,
                            onBack = { navController.popBackStack() }
                        )
                    }

                    composable(Routes.RECORDINGS) {
                        SubmittedRecordingsScreen(
                            uiState = uiState,
                            viewModel = viewModel,
                            onBack = { navController.popBackStack() }
                        )
                    }

                    composable(Routes.SETTINGS) {
                        SettingsAndProfileScreen(
                            uiState = uiState,
                            viewModel = viewModel,
                            onBack = { navController.popBackStack() },
                            onLoggedOut = {
                                navController.navigate(Routes.ONBOARDING) {
                                    popUpTo(Routes.MAIN_CHAT) { inclusive = true }
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}
