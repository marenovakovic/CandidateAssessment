package xyz.argent.candidateassessment.welcome

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import xyz.argent.candidateassessment.R
import xyz.argent.candidateassessment.app.Constants

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WelcomeScreen(onTokensClick: () -> Unit) {
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(text = stringResource(R.string.welcome)) },
            )
        }
    ) {
        Column(
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxSize()
                .padding(it)
                .padding(horizontal = 12.dp),
        ) {
            Text(text = stringResource(id = R.string.wallet_address))
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = Constants.walletAddress)
            Spacer(modifier = Modifier.height(16.dp))
            ElevatedButton(onClick = onTokensClick) {
                Text(text = stringResource(id = R.string.erc20_tokens))
            }
        }
    }
}

@Preview
@Composable
private fun WelcomeScreenPreview() {
    MaterialTheme {
        WelcomeScreen {}
    }
}
