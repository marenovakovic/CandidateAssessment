import androidx.activity.ComponentActivity
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import org.junit.Rule
import org.junit.Test
import xyz.argent.candidateassessment.R
import xyz.argent.candidateassessment.balance.Balance
import xyz.argent.candidateassessment.balance.Balances
import xyz.argent.candidateassessment.balance.TEST_TAG_BALANCES_SCREEN_LOADING
import xyz.argent.candidateassessment.tokens.TEST_TAG_TOKENS_SCREEN_LOADING
import xyz.argent.candidateassessment.tokens.TokensScreen
import xyz.argent.candidateassessment.tokens.TokensState
import xyz.argent.candidateassessment.tokens.tokens

class TokensScreenTest {

    @get: Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun toolbar_is_always_present() {
        val tokensState = TokensState.Initial

        composeTestRule.setContent {
            MaterialTheme {
                TokensScreen(
                    tokensState = tokensState,
                    onQueryChanged = {},
                    onBackPressed = {},
                )
            }
        }

        composeTestRule
            .onNodeWithText(composeTestRule.activity.getString(R.string.tokens))
            .assertIsDisplayed()
    }

    @Test
    fun loading() {
        val tokensState = TokensState.Loading

        composeTestRule.setContent {
            MaterialTheme {
                TokensScreen(
                    tokensState = tokensState,
                    onQueryChanged = {},
                    onBackPressed = {},
                )
            }
        }

        composeTestRule
            .onNodeWithTag(TEST_TAG_TOKENS_SCREEN_LOADING)
            .assertIsDisplayed()
        composeTestRule
            .onNodeWithText(composeTestRule.activity.getString(R.string.search_tokens))
            .assertIsDisplayed()
            .assertIsNotEnabled()
    }

    @Test
    fun error() {
        val tokensState = TokensState.Error

        composeTestRule.setContent {
            MaterialTheme {
                TokensScreen(
                    tokensState = tokensState,
                    onQueryChanged = {},
                    onBackPressed = {},
                )
            }
        }

        composeTestRule
            .onNodeWithText(composeTestRule.activity.getString(R.string.error))
            .assertIsDisplayed()
        composeTestRule
            .onNodeWithText(composeTestRule.activity.getString(R.string.search_tokens))
            .assertIsDisplayed()
            .assertIsNotEnabled()
    }

    @Test
    fun connectivity_error() {
        val tokensState = TokensState.ConnectivityError

        composeTestRule.setContent {
            MaterialTheme {
                TokensScreen(
                    tokensState = tokensState,
                    onQueryChanged = {},
                    onBackPressed = {},
                )
            }
        }

        composeTestRule
            .onNodeWithText(composeTestRule.activity.getString(R.string.internet_not_available))
            .assertIsDisplayed()
        Thread.sleep(1_000)
        composeTestRule
            .onNodeWithText(composeTestRule.activity.getString(R.string.search_tokens))
            .assertIsDisplayed()
            .assertIsNotEnabled()
    }

    @Test
    fun tokens_initial_balances() {
        val tokensState = TokensState.Tokens("", emptyList(), Balances.Initial)

        composeTestRule.setContent {
            MaterialTheme {
                TokensScreen(
                    tokensState = tokensState,
                    onQueryChanged = {},
                    onBackPressed = {},
                )
            }
        }

        composeTestRule
            .onNodeWithText(composeTestRule.activity.getString(R.string.search_tokens))
            .assertIsDisplayed()
            .assertIsEnabled()
        composeTestRule
            .onNodeWithText(composeTestRule.activity.getString(R.string.search_tokens_in_order_to_see_balance))
            .assertIsDisplayed()
    }

    @Test
    fun tokens_loading_balances() {
        val tokensState = TokensState.Tokens("", emptyList(), Balances.Loading)

        composeTestRule.setContent {
            MaterialTheme {
                TokensScreen(
                    tokensState = tokensState,
                    onQueryChanged = {},
                    onBackPressed = {},
                )
            }
        }

        composeTestRule
            .onNodeWithText(composeTestRule.activity.getString(R.string.search_tokens))
            .assertIsDisplayed()
            .assertIsEnabled()
        composeTestRule
            .onNodeWithTag(TEST_TAG_BALANCES_SCREEN_LOADING)
            .assertIsDisplayed()
    }

    @Test
    fun tokens_success_balances() {
        val balance = Balance(tokens.first(), Result.success(0.0))
        val tokensState =
            TokensState.Tokens("", emptyList(), Balances.Success(listOf(balance)))

        composeTestRule.setContent {
            MaterialTheme {
                TokensScreen(
                    tokensState = tokensState,
                    onQueryChanged = {},
                    onBackPressed = {},
                )
            }
        }

        composeTestRule
            .onNodeWithText(composeTestRule.activity.getString(R.string.search_tokens))
            .assertIsDisplayed()
            .assertIsEnabled()
        composeTestRule
            .onNodeWithText(balance.token.name!!)
            .assertIsDisplayed()
        composeTestRule
            .onNodeWithText(balance.balance.getOrThrow().toString())
            .assertIsDisplayed()
    }
}
