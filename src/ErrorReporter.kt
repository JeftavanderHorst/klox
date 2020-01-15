package klox

interface ErrorReporter {
    fun error(line: Int, message: String)
}
