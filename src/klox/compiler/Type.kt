package klox.compiler

sealed class Type {
    // TODO document what the javaClass thing is for

    class NumberType : Type() {
        override fun toString() = "number"
        override fun hashCode() = javaClass.hashCode()
        override fun equals(other: Any?) = other is NumberType
    }

    class BoolType : Type() {
        override fun toString() = "bool"
        override fun hashCode() = javaClass.hashCode()
        override fun equals(other: Any?) = other is BoolType
    }

    class StringType : Type() {
        override fun toString() = "string"
        override fun hashCode() = javaClass.hashCode()
        override fun equals(other: Any?) = other is StringType
    }

    class NilType : Type() {
        override fun toString() = "nil"
        override fun hashCode() = javaClass.hashCode()
        override fun equals(other: Any?) = other is NilType
    }

    class FunType(val argTypes: List<Type>, val returnType: Type) : Type() {
        override fun toString(): String {
            if (argTypes.size == 1) {
                return "(${argTypes[0]} -> $returnType)"
            }
            return "((${argTypes.joinToString(", ")}) -> $returnType)"
        }
    }

    class TypeVar(val name: String) : Type() {
        override fun toString() = name
        override fun hashCode() = name.hashCode()
        override fun equals(other: Any?) = other is TypeVar && this.name == other.name
    }
}
