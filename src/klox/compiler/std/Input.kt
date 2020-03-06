package klox.compiler.std

import klox.compiler.Interpreter
import klox.compiler.LoxCallable
import klox.compiler.Nil
import klox.compiler.Purity

class Input : LoxCallable {
    override fun arity() = 0
    override fun native() = true
    override fun purity() = Purity.IMPURE
    override fun toString() = "<native fn>"

    override fun call(interpreter: Interpreter, arguments: List<Any>): Any {
        return readLine() ?: Nil.Nil
    }
}
