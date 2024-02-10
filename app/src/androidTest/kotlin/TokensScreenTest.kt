import androidx.activity.ComponentActivity
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import org.junit.Rule
import org.junit.Test
import xyz.argent.candidateassessment.R
import xyz.argent.candidateassessment.balance.BalanceState
import xyz.argent.candidateassessment.tokens.TEST_TAG_TOKENS_SCREEN_LOADING
import xyz.argent.candidateassessment.tokens.TokensScreen
import xyz.argent.candidateassessment.tokens.TokensState

class TokensScreenTest {

    @get: Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun loading() {
        val tokensState = TokensState.Loading
        val balanceState = BalanceState.Initial

        composeTestRule.setContent {
            MaterialTheme {
                TokensScreen(
                    tokensState = tokensState,
                    balanceState = balanceState,
                    onQueryChanged = {},
                    onBackPressed = {},
                )
            }
        }

        composeTestRule
            .onNodeWithTag(TEST_TAG_TOKENS_SCREEN_LOADING)
            .assertIsDisplayed()
    }

    @Test
    fun error() {
        val tokensState = TokensState.Error
        val balanceState = BalanceState.Initial

        composeTestRule.setContent {
            MaterialTheme {
                TokensScreen(
                    tokensState = tokensState,
                    balanceState = balanceState,
                    onQueryChanged = {},
                    onBackPressed = {},
                )
            }
        }

        composeTestRule
            .onNodeWithText(composeTestRule.activity.getString(R.string.error))
            .assertIsDisplayed()
    }

    @Test
    fun connectivity_error() {
        val tokensState = TokensState.ConnectivityError
        val balanceState = BalanceState.Initial

        composeTestRule.setContent {
            MaterialTheme {
                TokensScreen(
                    tokensState = tokensState,
                    balanceState = balanceState,
                    onQueryChanged = {},
                    onBackPressed = {},
                )
            }
        }

        composeTestRule
            .onNodeWithText(composeTestRule.activity.getString(R.string.internet_not_available))
            .assertIsDisplayed()
    }
}
