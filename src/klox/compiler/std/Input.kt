package klox.compiler.std

import klox.compiler.Interpreter
import klox.compiler.LoxCallable
import klox.compiler.Nil
import klox.compiler.Type

class Input : LoxCallable {
    override fun arity() = 0
    override fun native() = true
    override fun toString() = "<native fn>"
    override fun type() = Type.FunType(emptyList(), Type.StringType())

    override fun call(interpreter: Interpreter, arguments: List<Any>): Any {
        return readLine() ?: Nil.Nil
    }
}
