package tokens

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import xyz.argent.candidateassessment.tokens.Token
import xyz.argent.candidateassessment.tokens.persistence.TokenEntity
import xyz.argent.candidateassessment.tokens.persistence.TokensDao
import xyz.argent.candidateassessment.tokens.persistence.toTokenEntity

class TokensDaoFake(initialTokens: List<Token> = emptyList()) : TokensDao {
    val tokens = MutableStateFlow(initialTokens.map { it.toTokenEntity() })

    override suspend fun getAllTokens() = tokens.value

    override fun observeAllTokens(): Flow<List<TokenEntity>> = tokens

    override suspend fun getToken(address: String) =
        tokens.value.singleOrNull { it.address == address }

    override suspend fun saveTokens(tokens: List<TokenEntity>) = this.tokens.emit(tokens)
}
