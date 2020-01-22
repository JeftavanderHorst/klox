package klox.compiler

class ConsoleErrorReporter : ErrorReporter {
    override fun display(errors: List<CompileError>) {
        for (error in errors) {
            val errorType = when (error) {
                is Scanner.ScanError -> "Scan Error"
                is Parser.ParseError -> "Parse Error"
                else -> "Error"
            }

            println("[$errorType] Line ${error.line}: ${error.message}")
        }
    }
}
