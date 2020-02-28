package klox.compiler

class LoxFunction(
    private val declaration: Stmt.Function,
    private val closure: Environment
) : LoxCallable {
    override fun call(interpreter: Interpreter, arguments: List<Any>): Any {
        val environment = Environment(closure)
        for (i in declaration.params.indices) {
            environment.define(i, declaration.params[i].lexeme, arguments[i])
        }

        try {
            interpreter.executeBlock(declaration.body, environment)
        } catch (r: Return) {
            return r.value
        }

        return Nil.Nil
    }

    override fun arity(): Int {
        return declaration.params.size
    }

    override fun toString(): String {
        return "<fn ${declaration.name.lexeme}>"
    }
}
