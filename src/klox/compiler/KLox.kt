package klox.compiler

import java.io.File
import kotlin.system.exitProcess

class KLox {
    private val errorReporter = ErrorReporter(System.err)
    private val interpreter = Interpreter(errorReporter)

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

        val (parsedStatements, parseErrors) = Parser(tokens).parse()
        if (parseErrors.isNotEmpty()) {
            errorReporter.display(parseErrors)
            return RunResult.STATIC_ERROR
        }

        val (resolvedStatements, resolveErrors, resolveWarnings) = Resolver().resolveRoot(parsedStatements)
        if (resolveErrors.isNotEmpty()) {
            errorReporter.display(resolveErrors)
            return RunResult.STATIC_ERROR
        }

        if (resolveWarnings.isNotEmpty()) {
            errorReporter.display(resolveWarnings)
            println()
        }

//        AstPrinter().print(resolvedStatements)
        interpreter.interpret(resolvedStatements)

        return RunResult.SUCCESS
    }
}
