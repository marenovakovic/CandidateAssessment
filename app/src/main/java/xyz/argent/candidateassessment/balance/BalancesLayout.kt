package xyz.argent.candidateassessment.balance

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage

@Composable
fun BalancesLayout(balances: List<Balance>) {
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
                Column {
                    AsyncImage(
                        model = balance.token.image,
                        contentDescription = null,
                        contentScale = ContentScale.FillBounds,
                        modifier = Modifier.size(32.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
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
private fun BalancesLayoutPreview() {
    MaterialTheme {
    }
}
