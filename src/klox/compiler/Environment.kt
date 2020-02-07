package klox.compiler

class Environment(
    private val parent: Environment? = null
) {
    private val values = HashMap<String, Any>()

    fun define(name: String, value: Any) {
        values[name] = value
    }

    fun get(token: Token): Any {
        if (values.containsKey(token.lexeme))
            return values[token.lexeme]!!

        if (parent != null) return parent.get(token)

        throw RuntimeError(token.line, "Variable '${token.lexeme}' is undefined")
    }

    fun assign(token: Token, value: Any) {
        if (values.containsKey(token.lexeme)) {
            values[token.lexeme] = value
            return
        }

        if (parent != null) {
            parent.assign(token, value)
            return
        }

        throw RuntimeError(token.line, "Variable '${token.lexeme}' is undefined")
    }
}
