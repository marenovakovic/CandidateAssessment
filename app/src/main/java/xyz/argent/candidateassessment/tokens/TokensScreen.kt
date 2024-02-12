@file:OptIn(ExperimentalMaterial3Api::class)

package xyz.argent.candidateassessment.tokens

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import xyz.argent.candidateassessment.R
import xyz.argent.candidateassessment.balance.Balances

@Composable
fun TokensScreen(
    tokensViewModel: TokensViewModel = hiltViewModel(),
    onBackPressed: () -> Unit,
) {
    rememberSaveable { tokensViewModel.init(); 1 }

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
                    IconButton(onClick = onBackPressed) {
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
                value = (tokensState as? TokensState.Tokens)?.query.orEmpty(),
                onValueChange = onQueryChanged,
                enabled = tokensState is TokensState.Tokens,
                label = { Text(text = stringResource(R.string.search_tokens)) },
                modifier = Modifier
                    .fillMaxWidth()
                    .focusRequester(focusRequester),
            )
            Spacer(modifier = Modifier.height(32.dp))
            Crossfade(
                targetState = tokensState,
                label = "TokensScreen content Crossfade",
                modifier = Modifier.fillMaxSize(),
            ) { targetState ->
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.fillMaxSize(),
                ) {
                    when (targetState) {
                        TokensState.Initial -> Box {}
                        TokensState.Loading ->
                            CircularProgressIndicator(
                                modifier = Modifier.testTag(TEST_TAG_TOKENS_SCREEN_LOADING),
                            )
                        TokensState.Error -> Text(text = stringResource(R.string.error))
                        TokensState.ConnectivityError ->
                            Text(text = stringResource(R.string.internet_not_available))
                        is TokensState.Tokens ->
                            Balances(
                                balances = targetState.balances,
                                modifier = Modifier.fillMaxSize(),
                            )
                    }
                }
            }
        }
    }
}

@Preview
@Composable
private fun TokensScreenPreview() {
    MaterialTheme {
        val state = TokensState.Tokens("", tokens.take(3), Balances.Initial)

        TokensScreen(
            tokensState = state,
            onQueryChanged = {},
            onBackPressed = {},
        )
    }
}

const val TEST_TAG_TOKENS_SCREEN_LOADING = "test_tag_tokens_screen_loading"
