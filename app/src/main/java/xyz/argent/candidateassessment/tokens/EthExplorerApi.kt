package xyz.argent.candidateassessment.tokens

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
