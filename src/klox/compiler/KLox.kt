package klox.compiler

import java.io.File
import kotlin.system.exitProcess

class KLox {
    private var hadError: Boolean = false
    private val errorReporter = ConsoleErrorReporter()

    fun runFile(path: String) {
        run(File(path).readText())
        if (hadError)
            exitProcess(65)
    }

    fun runPrompt() {
        println("KLox REPL")
        println("---------")

        while (true) {
            print("> ")
            val line = readLine()
            if (line != null)
                run(line)
            hadError = false
        }
    }

    private fun run(source: String) {
        val scanner = Scanner(source, errorReporter)
        val (tokens, scanErrors) = scanner.scanTokens()

        if (scanErrors.isNotEmpty()) {
            return
        }

        val parser = Parser(tokens, errorReporter)
        val (expression, parseErrors) = parser.parse()

        if (parseErrors.isNotEmpty()) {
            return
        }

        println(AstPrinter().print(expression.first()))
    }
}
