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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import xyz.argent.candidateassessment.R
import xyz.argent.candidateassessment.balance.BalanceState
import xyz.argent.candidateassessment.balance.BalanceViewModel
import xyz.argent.candidateassessment.balance.Balances

@Composable
fun TokensScreen(
    tokensViewModel: TokensViewModel = hiltViewModel(),
    balanceViewModel: BalanceViewModel = hiltViewModel(),
    onBackPressed: () -> Unit,
) {
    rememberSaveable { tokensViewModel.init(); 1 }

    val balanceState by balanceViewModel.state.collectAsState()
    val tokensState by tokensViewModel.state.collectAsState()

    TokensScreen(
        tokensState = tokensState,
        balanceState = balanceState,
        onQueryChanged = balanceViewModel::search,
        onBackPressed = onBackPressed,
    )
}

@Composable
fun TokensScreen(
    tokensState: TokensState,
    balanceState: BalanceState,
    onQueryChanged: (String) -> Unit,
    onBackPressed: () -> Unit,
) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier.fillMaxSize(),
    ) {
        Crossfade(
            targetState = tokensState,
            label = "TokensScreen content Crossfade",
            modifier = Modifier.fillMaxSize(),
        ) { targetState ->
            when (targetState) {
                TokensState.Initial -> Box {}
                TokensState.Loading ->
                    CircularProgressIndicator(
                        modifier = Modifier.testTag(TEST_TAG_TOKENS_SCREEN_LOADING),
                    )
                TokensState.Error -> Text(text = stringResource(R.string.error))
                TokensState.ConnectivityError ->
                    Text(text = stringResource(R.string.internet_not_available))
                is TokensState.Tokens -> TokensScreen(
                    balanceState = balanceState,
                    onQueryChanged = onQueryChanged,
                    onBackPressed = onBackPressed,
                )
            }
        }
    }
}

@Composable
@OptIn(ExperimentalMaterial3Api::class)
private fun TokensScreen(
    balanceState: BalanceState,
    onQueryChanged: (String) -> Unit,
    onBackPressed: () -> Unit,
) {
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
                value = balanceState.query,
                onValueChange = onQueryChanged,
                label = { Text(text = stringResource(R.string.search_tokens)) },
                modifier = Modifier.fillMaxWidth(),
            )
            Spacer(modifier = Modifier.height(32.dp))
            Balances(
                balanceState = balanceState,
                modifier = Modifier.fillMaxSize(),
            )
        }
    }
}

@Preview
@Composable
private fun TokensScreenPreview() {
    MaterialTheme {
        val state = BalanceState.Initial

        TokensScreen(
            balanceState = state,
            onQueryChanged = {},
            onBackPressed = {},
        )
    }
}

const val TEST_TAG_TOKENS_SCREEN_LOADING = "test_tag_tokens_screen_loading"
