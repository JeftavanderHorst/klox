package klox.compiler

import java.io.File
import kotlin.system.exitProcess

class KLox {
    private val errorReporter = ErrorReporter()
    private val interpreter = Interpreter()

    fun runFile(path: String) = when (run(File(path).readText())) {
        RunResult.STATIC_ERROR -> exitProcess(65)
        RunResult.RUNTIME_ERROR -> exitProcess(70)
        else -> null
    }

    fun runPrompt() {
        println("KLox REPL")
        println("---------")

        while (true) {
            print("> ")
            val line = readLine()
            if (line != null)
                run(line)
        }
    }

    enum class RunResult { SUCCESS, STATIC_ERROR, RUNTIME_ERROR }

    private fun run(source: String): RunResult {
        val (tokens, scanErrors) = Scanner(source).scanTokens()

        if (scanErrors.isNotEmpty()) {
            errorReporter.display(scanErrors)
            return RunResult.STATIC_ERROR
        }

        val (expression, parseErrors) = Parser(tokens).parse()

        if (parseErrors.isNotEmpty()) {
            errorReporter.display(parseErrors)
            return RunResult.STATIC_ERROR
        }

        val (result, runtimeErrors) = interpreter.interpret(expression.first())

        if (runtimeErrors.isNotEmpty()) {
            errorReporter.display(runtimeErrors)
            return RunResult.RUNTIME_ERROR
        }

        println(result)

        return RunResult.SUCCESS
    }
}
