package klox.compiler.std

import klox.compiler.Interpreter
import klox.compiler.LoxCallable
import klox.compiler.Type

class Clock : LoxCallable {
    override fun arity() = 0
    override fun native() = true
    override fun toString() = "<native fn>"
    override fun type() = Type.FunType(emptyList(), Type.NumberType())

    override fun call(interpreter: Interpreter, arguments: List<Any>): Any {
        return System.currentTimeMillis().toDouble()
    }
}
