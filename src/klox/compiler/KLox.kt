package klox.compiler

import java.io.File
import kotlin.system.exitProcess

class KLox {
    private var hadError = false
    private var hadRuntimeError = false
    private val errorReporter = ConsoleErrorReporter()
    private val interpreter = Interpreter()

    fun runFile(path: String) {
        run(File(path).readText())
        if (hadError)
            exitProcess(65)
        if (hadRuntimeError)
            exitProcess(70)
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
            hadRuntimeError = false
        }
    }

    private fun run(source: String) {
        val (tokens, scanErrors) = Scanner(source).scanTokens()

        if (scanErrors.isNotEmpty()) {
            errorReporter.display(scanErrors)
            hadError = true
            return
        }

        val (expression, parseErrors) = Parser(tokens).parse()

        if (parseErrors.isNotEmpty()) {
            errorReporter.display(parseErrors)
            hadError = true
            return
        }

        interpreter.interpret(expression.first())
    }
}
