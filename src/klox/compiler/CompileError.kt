package klox.compiler

abstract class CompileError : Throwable() {
    abstract val line: Int
    abstract override val message: String
}
