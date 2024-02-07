package xyz.argent.candidateassessment.tokens

import javax.inject.Inject

class GetTokens @Inject constructor() {
    operator fun invoke() = tokens.take(10)
}
