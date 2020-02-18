package klox.compiler.std

import klox.compiler.Interpreter
import klox.compiler.LoxCallable
import klox.compiler.Nil

class ReadLine : LoxCallable {
    override fun arity(): Int {
        return 0
    }

    override fun call(interpreter: Interpreter, arguments: List<Any>): Any {
        return readLine() ?: Nil.Nil
    }

    override fun toString(): String {
        return "<native fn>"
    }
}
