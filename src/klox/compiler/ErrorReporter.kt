package klox.compiler

class ErrorReporter {
    fun display(errors: List<KLoxError>) {
        for (error in errors) {
            val errorType = when (error) {
                is ScanError -> "Scan Error"
                is ParseError -> "Parse Error"
                is RuntimeError -> "Runtime Error"
                else -> "Error"
            }

            println("[$errorType] Line ${error.line}: ${error.message}")
        }
    }
}
