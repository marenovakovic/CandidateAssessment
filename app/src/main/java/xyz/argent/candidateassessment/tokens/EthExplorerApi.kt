package xyz.argent.candidateassessment.tokens

import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import retrofit2.http.GET
import retrofit2.http.Query

interface EthExplorerApi {

    @GET("/getTopTokens")
    suspend fun getTopTokens(
        @Query("limit") limit: Int,
        @Query("apiKey") apiKey: String,
    ): TopTokensResponse

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
    override suspend fun getTopTokens(limit: Int, apiKey: String) = tokensResponse
}
