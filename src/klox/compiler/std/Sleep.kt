package klox.compiler.std

import klox.compiler.Interpreter
import klox.compiler.LoxCallable
import klox.compiler.Nil
import klox.compiler.Type

class Sleep : LoxCallable {
    override fun arity() = 1
    override fun native() = true
    override fun toString() = "<native fn>"
    override fun type() = Type.FunType(listOf(Type.NumberType()), Type.NilType())

    override fun call(interpreter: Interpreter, arguments: List<Any>): Any {
        Thread.sleep((arguments[0] as Double).toLong())
        return Nil.Nil
    }
}
