package klox.compiler.std

import klox.compiler.Interpreter
import klox.compiler.LoxCallable
import klox.compiler.Nil

class Sleep : LoxCallable {
    override fun arity(): Int {
        return 1
    }

    override fun call(interpreter: Interpreter, arguments: List<Any>): Any {
        Thread.sleep((arguments[0] as Double).toLong())
        return Nil.Nil
    }

    override fun toString(): String {
        return "<native fn>"
    }
}
