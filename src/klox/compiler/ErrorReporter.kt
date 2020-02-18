package klox.compiler

import java.io.PrintStream

class ErrorReporter(private val writer: PrintStream) {
    fun display(error: KLoxError) {
        val errorType = when (error) {
            is ScanError -> "Scan Error"
            is ParseError -> "Parse Error"
            is RuntimeError -> "Runtime Error"
            is ResolveError -> "Resolve Error"
            else -> "Error"
        }

        writer.println("[$errorType] Line ${error.line}: ${error.message}")
    }

    fun display(errors: List<KLoxError>) {
        for (error in errors) {
            this.display(error)
        }
    }
}
