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

        var (statements, parseErrors) = Parser(tokens).parse()
        if (parseErrors.isNotEmpty()) {
            errorReporter.display(parseErrors)
            return RunResult.STATIC_ERROR
        }

        val passes: List<Pass> = listOf(Resolver(), Typer())
        for (pass in passes) {
            val (result, errors, warnings) = pass.pass(statements)

            if (errors.isNotEmpty()) {
                errorReporter.display(errors)
                return RunResult.STATIC_ERROR
            }

//            errorReporter.display(warnings)

            statements = result
        }

        println("AST:")
        AstPrinter().print(statements)

        println()
        println("Result:")
        interpreter.interpret(statements)

        return RunResult.SUCCESS
    }
}

/* TODO:
 - fun/proc
 - anonymous fun
 - multiple statements in for loop declarations?
 - runtime error when accessing unassigned variables
 - trailing commas
 - lazier evaluation
 - expressions in repl
 - correct indentation in astprinter
 - dead code warnings
 - pipeline operator
 */

/*
 - language: 2/5
 - compiler quality: 3/5
 - error reporting: 3/5
 - documentation: 1/5
 - tests: 1/5
 */
