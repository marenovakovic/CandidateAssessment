package xyz.argent.candidateassessment.balance

import kotlinx.coroutines.delay
import retrofit2.http.GET
import retrofit2.http.Query
import xyz.argent.candidateassessment.tokens.EthExplorerApiMock

interface EtherscanApi {

    @GET("/api?module=account&action=tokenbalance&tag=latest")
    suspend fun getTokenBalance(
        @Query("contractaddress") contractAddress: String,
        @Query("address") address: String,
        @Query("apiKey") apiKey: String,
    ): TokenBalanceResponse

    data class TokenBalanceResponse(
        val status: Long,
        val message: String,
        val result: String,
    )
}

object EtherscanApiMock : EtherscanApi {
    override suspend fun getTokenBalance(
        contractAddress: String,
        address: String,
        apiKey: String,
    ): EtherscanApi.TokenBalanceResponse {
        val tokens = EthExplorerApiMock.getTopTokens(1, "").tokens
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
        val tokens = EthExplorerApiMock.getTopTokens(1, "").tokens
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
