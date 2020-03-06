package klox.compiler

enum class Purity {
    PURE,
    IMPURE
}

interface LoxCallable {
    fun call(interpreter: Interpreter, arguments: List<Any>): Any
    fun arity(): Int
    fun native(): Boolean
    fun purity(): Purity
}
