package klox.compiler

interface LoxCallable {
    fun call(interpreter: Interpreter, arguments: List<Any>): Any
    fun arity(): Int
    fun native(): Boolean
    fun type(): Type
}
