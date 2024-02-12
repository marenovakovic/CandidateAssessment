import androidx.activity.ComponentActivity
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
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
import xyz.argent.candidateassessment.balance.BalancesState
import xyz.argent.candidateassessment.balance.TEST_TAG_BALANCES_SCREEN_LOADING
import xyz.argent.candidateassessment.tokens.TEST_TAG_TOKENS_SCREEN_BACK_BUTTON
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
            Content(tokensState = tokensState)
        }

        composeTestRule
            .onNodeWithText(composeTestRule.activity.getString(R.string.tokens))
            .assertIsDisplayed()
        composeTestRule
            .onNodeWithTag(TEST_TAG_TOKENS_SCREEN_BACK_BUTTON)
            .assertIsDisplayed()
    }

    @Test
    fun loading() {
        val tokensState = TokensState.Loading

        composeTestRule.setContent {
            Content(tokensState = tokensState)
        }

        composeTestRule
            .onNodeWithText(composeTestRule.activity.getString(R.string.search_tokens))
            .assertIsDisplayed()
            .assertIsNotEnabled()
        composeTestRule
            .onNodeWithTag(TEST_TAG_TOKENS_SCREEN_LOADING)
            .assertIsDisplayed()
    }

    @Test
    fun error() {
        val tokensState = TokensState.Error

        composeTestRule.setContent {
            Content(tokensState = tokensState)
        }

        composeTestRule
            .onNodeWithText(composeTestRule.activity.getString(R.string.search_tokens))
            .assertIsDisplayed()
            .assertIsNotEnabled()
        composeTestRule
            .onNodeWithText(composeTestRule.activity.getString(R.string.error_occurred))
            .assertIsDisplayed()
        composeTestRule
            .onNodeWithText(composeTestRule.activity.getString(R.string.retry))
            .assertIsDisplayed()
    }

    @Test
    fun connectivity_error() {
        val tokensState = TokensState.ConnectivityError

        composeTestRule.setContent {
            Content(tokensState = tokensState)
        }

        composeTestRule
            .onNodeWithText(composeTestRule.activity.getString(R.string.search_tokens))
            .assertIsDisplayed()
            .assertIsNotEnabled()
        composeTestRule
            .onNodeWithText(composeTestRule.activity.getString(R.string.internet_not_available))
            .assertIsDisplayed()
    }

    @Test
    fun tokens_initial_balances() {
        val tokensState = TokensState.Tokens("", emptyList(), BalancesState.Initial)

        composeTestRule.setContent {
            Content(tokensState = tokensState)
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
        val tokensState = TokensState.Tokens("", emptyList(), BalancesState.Loading)

        composeTestRule.setContent {
            Content(tokensState = tokensState)
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
            TokensState.Tokens("", emptyList(), BalancesState.Success(listOf(balance)))

        composeTestRule.setContent {
            Content(tokensState = tokensState)
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

    @Composable
    fun Content(tokensState: TokensState) {
        MaterialTheme {
            TokensScreen(
                tokensState = tokensState,
                onQueryChanged = {},
                onRetry = {},
                onBackPressed = {},
            )
        }
    }
}
