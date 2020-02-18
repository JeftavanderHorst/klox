package klox.compiler

class Environment(
    private val parent: Environment? = null
) {
    private val values = HashMap<String, Any>()

    fun define(name: String, value: Any) {
        values[name] = value
    }

    fun get(token: Token): Any {
        if (values.containsKey(token.lexeme)) {
            return values[token.lexeme]!!
        }

        if (parent != null) {
            return parent.get(token)
        }

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

        throw RuntimeError(token.line, "Cannot assign to variable '${token.lexeme}' because it is not declared")
    }

    fun getAt(distance: Int, name: String): Any {
        return ancestor(distance).values[name]!!
    }

    fun assignAt(distance: Int, name: Token, value: Any) {
        ancestor(distance).values[name.lexeme] = value
    }

    private fun ancestor(distance: Int): Environment {
        var environment = this
        for (i in 0 until distance) {
            environment = environment.parent!!
        }

        return environment
    }

    override fun toString(): String {
        return this.toString(0)
    }

    private fun toString(indent: Int): String {
        // TODO: indicate mutability
        // TODO: indicate shadowed symbols

        val tabs = "\t".repeat(indent)
        return "\n$tabs" +
                values.map { (k, v) -> "$k: $v" }.joinToString("\n$tabs") +
                if (parent != null) {
                    "\n${tabs}parent:" + parent.toString(indent + 1)
                } else {
                    "\n${tabs}no parent"
                }
    }
}
