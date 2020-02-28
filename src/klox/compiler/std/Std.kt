package klox.compiler.std

import klox.compiler.LoxCallable

class Std {
    fun getNativeFunctions(): Array<Triple<Int, String, LoxCallable>> = arrayOf(
        Triple(0, "clock", Clock()),
        Triple(1, "print", Print()),
        Triple(2, "input", Input()),
        Triple(3, "sleep", Sleep())
    )
}
