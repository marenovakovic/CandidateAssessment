package xyz.argent.candidateassessment

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import xyz.argent.candidateassessment.tokens.TokensScreen
import xyz.argent.candidateassessment.welcome.WelcomeScreen

@Suppress("ConstPropertyName")
object Destination {
    const val Welcome = "welcome"
    const val Tokens = "tokens"
}

@Composable
fun NavGraph() {
    val navController = rememberNavController()

    NavHost(navController, Destination.Welcome) {
        composable(Destination.Welcome) {
            WelcomeScreen(onTokensClick = { navController.navigate(Destination.Tokens) })
        }
        composable(Destination.Tokens) {
            TokensScreen(onBackPressed = { navController.popBackStack() })
        }
    }
}
