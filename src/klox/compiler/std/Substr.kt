package klox.compiler.std

import klox.compiler.*
import kotlin.math.roundToInt

class Substr : LoxCallable {
    override fun arity() = 3
    override fun native() = true
    override fun toString() = "<native fn>"
    override fun type() =
        Type.FunType(listOf(Type.StringType(), Type.NumberType(), Type.NumberType()), Type.StringType())

    override fun call(interpreter: Interpreter, arguments: List<Any>): Any {
        val string = arguments[0]
        val start = arguments[1]
        val end = arguments[2]

        if (string !is String || start !is Double || end !is Double) {
            // todo line number
            throw RuntimeError(0, "Invalid types passed to substr(string, double, double)")
        }

        if (start < 0 || end >= string.length) {
            // todo line number
            throw RuntimeError(
                0,
                "String indexes are out of bounds " +
                        "(start ${start.roundToInt()}, end ${end.roundToInt()}, length ${string.length})"
            )
        }

        return string.substring(start.toInt(), end.toInt())
    }
}
