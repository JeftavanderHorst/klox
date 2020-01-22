package klox.compiler

interface CompileError {
    val line: Int
    val message: String
}
