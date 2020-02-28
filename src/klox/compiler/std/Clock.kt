package klox.compiler.std

import klox.compiler.Interpreter
import klox.compiler.LoxCallable

class Clock : LoxCallable {
    override fun arity(): Int = 0
    override fun toString(): String = "<native fn>"

    override fun call(interpreter: Interpreter, arguments: List<Any>): Any {
        return System.currentTimeMillis().toDouble()
    }
}
