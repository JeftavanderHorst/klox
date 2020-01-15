package klox

class ConsoleErrorReporter : ErrorReporter {
    override fun error(line: Int, message: String) {
            println("[Error] Line $line: $message")
    }
}
