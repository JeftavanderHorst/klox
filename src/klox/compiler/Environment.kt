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
        // TODO: indicate mutability
        // TODO: indicate shadowed symbols
        val symbols = values.map { (i, v) -> "$i: ${v.name}: ${v.value}" }.joinToString("\n")
        val parent = if (parent != null) "\n------------------------------\n$parent" else ""
        return symbols + parent
    }
}
