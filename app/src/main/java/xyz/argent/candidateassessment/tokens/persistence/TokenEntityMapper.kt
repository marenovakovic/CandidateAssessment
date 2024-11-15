package xyz.argent.candidateassessment.tokens.persistence

import xyz.argent.candidateassessment.tokens.Token

fun Token.toTokenEntity() =
    TokenEntity(
        address = address,
        name = name,
        symbol = symbol,
        decimals = decimals,
        image = image,
    )

fun TokenEntity.toToken() =
    Token(
        address = address,
        name = name,
        symbol = symbol,
        decimals = decimals,
        image = image,
    )
