package klox.compiler.std

import klox.compiler.Interpreter
import klox.compiler.LoxCallable

class Clock : LoxCallable {
    override fun arity(): Int {
        return 0
    }

    override fun call(interpreter: Interpreter, arguments: List<Any>): Any {
        return System.currentTimeMillis().toDouble()
    }

    override fun toString(): String {
        return "<native fn>"
    }
}
