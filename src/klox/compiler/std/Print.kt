package klox.compiler.std

import klox.compiler.Interpreter
import klox.compiler.LoxCallable
import klox.compiler.Nil

class Print : LoxCallable {
    override fun arity(): Int = 1
    override fun toString(): String = "<native fn>"

    override fun call(interpreter: Interpreter, arguments: List<Any>): Any {
        when (arguments[0]) {
            Nil.Nil -> println("nil")
            else -> println(arguments[0])
        }
        return Nil.Nil
    }
}
