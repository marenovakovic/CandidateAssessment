package tokens

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import xyz.argent.candidateassessment.tokens.Token
import xyz.argent.candidateassessment.tokens.TokensDao

class TokensDaoFake(initialTokens: List<Token> = emptyList()) : TokensDao {
    private val _tokens = MutableStateFlow(initialTokens)
    override val tokens: Flow<List<Token>> = _tokens

    override suspend fun saveTokens(tokens: List<Token>) = _tokens.emit(tokens)
}
