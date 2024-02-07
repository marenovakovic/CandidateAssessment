package xyz.argent.candidateassessment.tokens

fun EthExplorerApi.TokenResponse.toToken() =
    Token(address, name, symbol, decimals, image)
