package klox.compiler

interface ErrorReporter {
    fun display(errors: List<CompileError>)
}
