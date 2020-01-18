package klox.compiler

interface ErrorReporter {
    fun error(line: Int, message: String)
}
