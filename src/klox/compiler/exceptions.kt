package klox.compiler

abstract class KLoxError : Throwable() {
    abstract val line: Int
    abstract override val message: String
}

abstract class CompileError : KLoxError()

abstract class ParseError : CompileError()

class UnclosedParenthesesError(override val line: Int) : ParseError() {
    override val message: String
        get() = "Expected ')' after expression"
}

class NoExpressionProvided(override val line: Int) : ParseError() {
    override val message: String
        get() = "Expected expression"
}

abstract class ScanError : CompileError()

class UnexpectedCharacterError(override val line: Int, private val char: Char?) : ScanError() {
    override val message: String
        get() = "Unexpected character '$char'"
}

class UnterminatedStringError(override val line: Int) : ScanError() {
    override val message: String
        get() = "Unterminated string"
}

abstract class RuntimeError : KLoxError()

class IncompatibleTypesForAdditionError(override val line: Int) : RuntimeError() {
    override val message: String
        get() = "Cannot add values of different types"
}

class OperandNotDoubleError(override val line: Int) : RuntimeError() {
    override val message: String
        get() = "Operand must be a number"
}

class OperandsNotDoubleError(override val line: Int) : RuntimeError() {
    override val message: String
        get() = "Both operands must be numbers"
}

class AdditionNotSupportedForTypeError(override val line: Int) : RuntimeError() {
    override val message: String
        get() = "Cannot perform addition on values of this type"
}
