package klox.compiler

abstract class KLoxError(val line: Int, override val message: String) : Throwable()

abstract class CompileError(line: Int, message: String) : KLoxError(line, message)
class ScanError(line: Int, message: String) : CompileError(line, message)
class ParseError(line: Int, message: String) : CompileError(line, message)
class ResolveError(line: Int, message: String) : CompileError(line, message)
class TypeError(line: Int, message: String) : CompileError(line, message)

class RuntimeError(line: Int, message: String) : KLoxError(line, message)

class Warning(line: Int, message: String) : CompileError(line, message)
