package klox.compiler.std

import klox.compiler.Interpreter
import klox.compiler.LoxCallable
import klox.compiler.Nil

class Input : LoxCallable {
    override fun arity(): Int = 0
    override fun toString(): String = "<native fn>"

    override fun call(interpreter: Interpreter, arguments: List<Any>): Any {
        return readLine() ?: Nil.Nil
    }
}
