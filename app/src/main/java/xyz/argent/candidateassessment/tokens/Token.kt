package xyz.argent.candidateassessment.tokens

data class Token(
    val address: String,
    val name: String?,
    val symbol: String?,
    val decimals: Int?,
    val image: String?,
)
