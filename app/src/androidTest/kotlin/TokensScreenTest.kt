import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import org.junit.Rule
import org.junit.Test
import xyz.argent.candidateassessment.balance.BalanceState
import xyz.argent.candidateassessment.tokens.TEST_TAG_TOKENS_SCREEN_LOADING
import xyz.argent.candidateassessment.tokens.TokensScreen
import xyz.argent.candidateassessment.tokens.TokensState

class TokensScreenTest {

    @get: Rule
    val composeTestRule = createComposeRule()

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
}
