import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import xyz.argent.candidateassessment.tokens.EthExplorerApi
import xyz.argent.candidateassessment.tokens.toToken

val tokensResponse =
    Moshi
        .Builder()
        .add(KotlinJsonAdapterFactory())
        .build()
        .adapter(EthExplorerApi.TopTokensResponse::class.java).fromJson(topTokensJson)!!

val tokens = tokensResponse.tokens.map(EthExplorerApi.TokenResponse::toToken)
