package klox.compiler.std

import klox.compiler.Interpreter
import klox.compiler.LoxCallable
import klox.compiler.Nil

class Sleep : LoxCallable {
    override fun arity(): Int = 1
    override fun toString(): String = "<native fn>"

    override fun call(interpreter: Interpreter, arguments: List<Any>): Any {
        Thread.sleep((arguments[0] as Double).toLong())
        return Nil.Nil
    }
}
