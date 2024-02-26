import androidx.activity.ComponentActivity
import androidx.compose.runtime.Composable
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import kotlinx.collections.immutable.persistentListOf
import org.junit.Rule
import org.junit.Test
import xyz.argent.candidateassessment.R
import xyz.argent.candidateassessment.balance.Balance
import xyz.argent.candidateassessment.balance.loadingBalanceTestTag
import xyz.argent.candidateassessment.theme.CandidateAssessmentTheme
import xyz.argent.candidateassessment.tokens.TEST_TAG_TOKENS_SCREEN_BACK_BUTTON
import xyz.argent.candidateassessment.tokens.TokensScreen
import xyz.argent.candidateassessment.tokens.TokensState

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
    fun tokens_loading_balances() {
        val token = tokens.first()
        val balance = Balance(token, Result.success(null))
        val balances = persistentListOf(balance)
        val tokensState = TokensState("", balances)

        composeTestRule.setContent {
            Content(tokensState = tokensState)
        }

        composeTestRule
            .onNodeWithText(composeTestRule.activity.getString(R.string.search_tokens))
            .assertIsDisplayed()
            .assertIsEnabled()
        composeTestRule
            .onNodeWithTag(loadingBalanceTestTag(token))
            .assertIsDisplayed()
    }

    @Test
    fun tokens_success_balances() {
        val token = tokens.first()
        val balanceValue = "1234"
        val balance = Balance(token, Result.success(balanceValue))
        val balances = persistentListOf(balance)
        val tokensState = TokensState("", balances)

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
            .onNodeWithText("${balance.balance.getOrThrow()!!.toPlainString()} ${balance.token.symbol.orEmpty()}".trim())
            .assertIsDisplayed()
    }

    @Composable
    fun Content(tokensState: TokensState) {
        CandidateAssessmentTheme {
            TokensScreen(
                tokensState = tokensState,
                onQueryChanged = {},
                onBackPressed = {},
            )
        }
    }
}
