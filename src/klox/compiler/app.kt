package klox.compiler

import kotlin.system.exitProcess

fun main(args: Array<String>) {
    val klox = KLox()

    when (args.size) {
        0 -> klox.runPrompt()
        1 -> klox.runFile(args[0])
        else -> {
            println("Usage: klox [script]")
            exitProcess(64)
        }
    }
}
