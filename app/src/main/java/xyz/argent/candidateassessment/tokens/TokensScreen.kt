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
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import xyz.argent.candidateassessment.R
import xyz.argent.candidateassessment.balance.BalanceState
import xyz.argent.candidateassessment.balance.BalanceViewModel
import xyz.argent.candidateassessment.balance.Balances
import xyz.argent.candidateassessment.balance.BalancesLayout

@Composable
fun TokensScreen(
    tokensViewModel: TokensViewModel = hiltViewModel(),
    balanceViewModel: BalanceViewModel = hiltViewModel(),
    onBackPressed: () -> Unit,
) {
    rememberSaveable { tokensViewModel.init(); 1 }

    val state by balanceViewModel.state.collectAsState()

    val tokensState by tokensViewModel.state.collectAsState()
    LaunchedEffect(tokensState) {
        println(tokensState)
    }

    TokensScreen(state, balanceViewModel::search, onBackPressed)
}

@Composable
@OptIn(ExperimentalMaterial3Api::class)
private fun TokensScreen(
    state: BalanceState,
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
                value = state.query,
                onValueChange = onQueryChanged,
                label = { Text(text = stringResource(R.string.search_tokens)) },
                modifier = Modifier.fillMaxWidth(),
            )
            Spacer(modifier = Modifier.height(32.dp))
            Crossfade(
                targetState = state.balances,
                label = "Balances state crossfade",
            ) { targetState ->
                when (targetState) {
                    Balances.Initial -> InitialContent()
                    Balances.Loading -> Loading()
                    is Balances.Success -> BalancesLayout(targetState.balances)
                }
            }
        }

    }
}

@Composable
private fun InitialContent() {
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier.fillMaxSize(),
    ) {
        Text(
            style = MaterialTheme.typography.bodyMedium,
            text = "Search tokens in order to see balance",
        )
    }
}

@Composable
private fun Loading() {
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier.fillMaxSize(),
    ) {
        CircularProgressIndicator()
    }
}

@Preview
@Composable
private fun TokensScreenPreview() {
    MaterialTheme {
        val state = BalanceState.Initial

        TokensScreen(
            state = state,
            onQueryChanged = {},
            onBackPressed = {},
        )
    }
}
