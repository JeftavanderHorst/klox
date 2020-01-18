package klox.compiler

data class ParseResult(
    val tokens: List<Expression>,
    val errors: List<Error>
)
