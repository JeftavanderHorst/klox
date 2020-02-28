package klox.compiler

class Environment(
    private val parent: Environment? = null
) {
    private class Value(val name: String, val value: Any)

    private val values = HashMap<Int, Value>() // todo this can probably be an array of index -> (name, value)

    fun define(index: Int, name: String, value: Any) {
        values[index] = Value(name, value)
    }

    fun get(distance: Int, index: Int): Any {
        return ancestor(distance).values[index]!!.value
    }

    fun assign(distance: Int, index: Int, name: String, value: Any) {
        ancestor(distance).values[index] = Value(name, value)
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

    // TODO test this after indexes hit
    private fun toString(indent: Int): String {
        // TODO: indicate mutability
        // TODO: indicate shadowed symbols
        // TODO: don't hide native fn's outside of root scope

        val tabs = "\t".repeat(indent)

        val symbols = values
            .filter { (_, v) -> v.toString() != "<native fn>" }
            .map { (k, v) -> "$k: $v" }
            .joinToString("\n$tabs")

        val parent = if (parent != null) {
            "parent:" + parent.toString(indent + 1)
        } else {
            "no parent"
        }

        return "\n$tabs$symbols\n" +
                "$tabs$parent"
    }
}
