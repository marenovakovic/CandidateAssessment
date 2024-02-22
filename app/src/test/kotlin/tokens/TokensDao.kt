package tokens

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import xyz.argent.candidateassessment.tokens.Token
import xyz.argent.candidateassessment.tokens.persistence.TokenEntity
import xyz.argent.candidateassessment.tokens.persistence.TokensDao
import xyz.argent.candidateassessment.tokens.persistence.toTokenEntity

class TokensDaoFake(initialTokens: List<Token> = emptyList()) : TokensDao {
    private val _tokens = MutableStateFlow(initialTokens.map { it.toTokenEntity() })

    override fun getAllTokens(): Flow<List<TokenEntity>> = _tokens

    override suspend fun getToken(address: String) =
        _tokens.value.singleOrNull { it.address == address }

    override suspend fun saveTokens(tokens: List<TokenEntity>) = _tokens.emit(tokens)
}
