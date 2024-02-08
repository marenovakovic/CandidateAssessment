package xyz.argent.candidateassessment.tokens

const val TokenImageBaseUrl = "https://www.ethplorer.io"

fun EthExplorerApi.TokenResponse.toToken() =
    Token(
        address = address,
        name = name,
        symbol = symbol,
        decimals = decimals,
        image = image?.let { "$TokenImageBaseUrl/$it" }.orEmpty(),
    )
