@file:OptIn(ExperimentalMaterial3Api::class)

package xyz.argent.candidateassessment.tokens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import kotlinx.collections.immutable.persistentListOf
import xyz.argent.candidateassessment.R
import xyz.argent.candidateassessment.balance.Balances
import xyz.argent.candidateassessment.theme.CandidateAssessmentTheme

@Composable
fun TokensScreen(
    tokensViewModel: TokensViewModel = hiltViewModel(),
    onBackPressed: () -> Unit,
) {

    val tokensState by tokensViewModel.state.collectAsState()

    TokensScreen(
        tokensState = tokensState,
        onQueryChanged = tokensViewModel::search,
        onBackPressed = onBackPressed,
    )
}

@Composable
fun TokensScreen(
    tokensState: TokensState,
    onQueryChanged: (String) -> Unit,
    onBackPressed: () -> Unit,
) {
    val focusRequester = remember { FocusRequester() }
//    LaunchedEffect(tokensState) {
//        if (tokensState is TokensState.Tokens)
//            focusRequester.requestFocus()
//    }
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                navigationIcon = {
                    IconButton(
                        onClick = onBackPressed,
                        modifier = Modifier.testTag(TEST_TAG_TOKENS_SCREEN_BACK_BUTTON)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = null,
                        )
                    }
                },
                title = { Text(text = stringResource(R.string.tokens)) },
            )
        },
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxSize()
                .padding(it)
                .padding(8.dp),
        ) {
            OutlinedTextField(
                value = tokensState.query,
                onValueChange = onQueryChanged,
                label = { Text(text = stringResource(R.string.search_tokens)) },
                modifier = Modifier
                    .fillMaxWidth()
                    .focusRequester(focusRequester),
            )
            Spacer(modifier = Modifier.height(32.dp))
            Balances(
                balances = tokensState.balances,
                modifier = Modifier.fillMaxSize(),
            )
        }
    }
}

@Preview
@Composable
private fun TokensScreenPreview() {
    CandidateAssessmentTheme {
        val state = TokensState("", persistentListOf())

        TokensScreen(
            tokensState = state,
            onQueryChanged = {},
            onBackPressed = {},
        )
    }
}

const val TEST_TAG_TOKENS_SCREEN_BACK_BUTTON = "test_tag_tokens_screen_back_button"
const val TEST_TAG_TOKENS_SCREEN_LOADING = "test_tag_tokens_screen_loading"
