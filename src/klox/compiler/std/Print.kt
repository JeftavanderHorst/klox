package klox.compiler.std

import klox.compiler.Interpreter
import klox.compiler.LoxCallable
import klox.compiler.Nil
import klox.compiler.Purity

class Print : LoxCallable {
    override fun arity() = 1
    override fun native() = true
    override fun purity() = Purity.IMPURE
    override fun toString() = "<native fn>"

    override fun call(interpreter: Interpreter, arguments: List<Any>): Any {
        when (arguments[0]) {
            Nil.Nil -> println("nil")
            else -> println(arguments[0])
        }
        return Nil.Nil
    }
}
