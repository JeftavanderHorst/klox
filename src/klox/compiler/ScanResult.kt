package klox.compiler

data class ScanResult(
    val tokens: List<Token>,
    val errors: List<Scanner.ScanError>
)
