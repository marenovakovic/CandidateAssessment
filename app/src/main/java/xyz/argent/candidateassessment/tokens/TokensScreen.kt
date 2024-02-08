package xyz.argent.candidateassessment.tokens

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.CardDefaults
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
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import xyz.argent.candidateassessment.R
import xyz.argent.candidateassessment.balance.Balance
import xyz.argent.candidateassessment.balance.BalanceState
import xyz.argent.candidateassessment.balance.BalanceViewModel
import xyz.argent.candidateassessment.balance.Balances

@Composable
fun TokensScreen(
    balanceViewModel: BalanceViewModel = hiltViewModel<BalanceViewModel>(),
    onBackPressed: () -> Unit,
) {
    val state by balanceViewModel.state.collectAsState()

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
                    is Balances.Success -> Balances(targetState.balances)
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

@Composable
private fun Balances(balances: List<Balance>) {
    LazyColumn(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.fillMaxSize(),
    ) {
        items(balances) { balance ->
            val color = remember(balance.balance) {
                balance.balance.fold(
                    onSuccess = {
                        when {
                            it > 0 -> Color(0xFFB6D5D6)
                            it < 0 -> Color(0xFFE0474C)
                            else -> Color(0xFFEFEBE9)
                        }
                    },
                    onFailure = { Color(0xFFB11A21) },
                )
            }
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(CardDefaults.shape)
                    .background(color)
                    .padding(16.dp),
            ) {
                Column(verticalArrangement = Arrangement.Center) {
                    AsyncImage(
                        model = balance.token.image,
                        contentDescription = null,
                        contentScale = ContentScale.FillBounds,
                        modifier = Modifier.size(32.dp)
                    )
                    Text(text = balance.token.name.orEmpty())
                }
                balance.balance.fold(
                    onSuccess = { Text(text = it.toString()) },
                    onFailure = { Text(text = "Error occurred") },
                )
            }
        }
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
