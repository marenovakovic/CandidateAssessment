package xyz.argent.candidateassessment.tokens

import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import retrofit2.http.GET

interface EthExplorerApi {

    @GET("/getTopTokens?limit=100&apiKey=freekey")
    suspend fun getTopTokens(): TopTokensResponse

    data class TopTokensResponse(val tokens: List<TokenResponse>)

    data class TokenResponse(
        val address: String,
        val name: String?,
        val symbol: String?,
        val decimals: Double?,
        val image: String?,
    )
}

val tokensResponse =
    Moshi
        .Builder()
        .add(KotlinJsonAdapterFactory())
        .build()
        .adapter(EthExplorerApi.TopTokensResponse::class.java).fromJson(topTokensJson)!!

val tokens = tokensResponse.tokens.map(EthExplorerApi.TokenResponse::toToken)

object EthExplorerApiMock : EthExplorerApi {
    override suspend fun getTopTokens() = tokensResponse
}
