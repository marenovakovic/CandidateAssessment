package xyz.argent.candidateassessment.balance

import androidx.compose.animation.animateColorAsState
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
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import java.math.BigDecimal
import kotlinx.collections.immutable.ImmutableList
import xyz.argent.candidateassessment.R
import xyz.argent.candidateassessment.theme.CandidateAssessmentTheme
import xyz.argent.candidateassessment.tokens.Token

@Composable
fun Balances(
    balances: ImmutableList<Balance>,
    modifier: Modifier = Modifier,
) {
    LazyColumn(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp),
        modifier = modifier.fillMaxSize(),
    ) {
        items(
            items = balances,
            key = { it.token.address },
        ) { balance ->
            val color by animateColorAsState(
                label = "Balance card color",
                targetValue = balance.balance.fold(
                    onSuccess = {
                        when {
                            it == null -> Color.White
                            it.compareTo(BigDecimal.ZERO) == 0 -> Color(0xFFE0474C)
                            else -> Color(0xFFB6D5D6)
                        }
                    },
                    onFailure = { Color(0xFFB11A21) },
                ),
            )
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
                        modifier = Modifier.size(32.dp),
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(text = balance.token.name.orEmpty())
                }
                balance.balance.fold(
                    onSuccess = {
                        if (it == null)
                            CircularProgressIndicator(
                                modifier = Modifier.testTag(loadingBalanceTestTag(balance.token))
                            )
                        else
                            Text(
                                text = "${it.toPlainString()} ${balance.token.symbol.orEmpty()}".trim(),
                                textAlign = TextAlign.End,
                                modifier = Modifier
                                    .fillParentMaxWidth(0.5f)
                                    .padding(start = 32.dp),
                            )
                    },
                    onFailure = { Text(text = stringResource(id = R.string.error_occurred)) },
                )
            }
        }
    }
}

@Preview
@Composable
private fun BalancesLayoutPreview() {
    CandidateAssessmentTheme {}
}

fun loadingBalanceTestTag(token: Token) = "test_tag_${token.address}_loading_balance"
