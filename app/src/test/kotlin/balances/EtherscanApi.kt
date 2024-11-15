package balances

import kotlinx.coroutines.delay
import tokens.EthExplorerApiStub
import xyz.argent.candidateassessment.balance.EtherscanApi

object EtherscanApiMock : EtherscanApi {
    override suspend fun getTokenBalance(
        contractAddress: String,
        address: String,
        apiKey: String,
    ): EtherscanApi.TokenBalanceResponse {
        val tokens = EthExplorerApiStub().getTopTokens().tokens
        val searchesToken = tokens.single { it.address == contractAddress }
        return EtherscanApi.TokenBalanceResponse(1L, "OK", searchesToken.decimals.toString())
    }
}

class EtherscanApiDelay(private val delayMillis: Long) : EtherscanApi {
    override suspend fun getTokenBalance(
        contractAddress: String,
        address: String,
        apiKey: String,
    ): EtherscanApi.TokenBalanceResponse {
        delay(delayMillis)
        val tokens = EthExplorerApiStub().getTopTokens().tokens
        val searchesToken = tokens.single { it.address == contractAddress }
        return EtherscanApi.TokenBalanceResponse(1L, "OK", searchesToken.decimals.toString())
    }
}

object EtherscanApiFail : EtherscanApi {
    override suspend fun getTokenBalance(
        contractAddress: String,
        address: String,
        apiKey: String,
    ): EtherscanApi.TokenBalanceResponse = throw Throwable()
}
