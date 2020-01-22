package klox.compiler

class Interpreter : Expression.Visitor<Any> {
    class RuntimeError(private val token: Token, message: String) : RuntimeException(message)

    fun interpret(expression: Expression) {
        try {
            val v = evaluate(expression)
            println(stringify(v))
        } catch (e: RuntimeError) {
            println("error: $e")
        }
    }

    private fun stringify(obj: Any?): String {
        if (obj == null) return "nil"

        val text = obj.toString()
        if (obj is Double && text.endsWith(".0")) {
            return text.substring(0, text.length - 2)
        }

        return text
    }

    override fun visitGroupingExpression(expression: Expression.Grouping): Any {
        return evaluate(expression.expression)
    }

    override fun visitBinaryExpression(expression: Expression.Binary): Any {
        val left = evaluate(expression.left)
        val right = evaluate(expression.right)

        return when (expression.operator.type) {
            TokenType.MINUS -> {
                checkNumberOperands(expression.operator, left, right)
                (left as Double) - (right as Double)
            }
            TokenType.SLASH -> {
                checkNumberOperands(expression.operator, left, right)
                (left as Double) / (right as Double)
            }
            TokenType.STAR -> {
                checkNumberOperands(expression.operator, left, right)
                (left as Double) * (right as Double)
            }
            TokenType.PLUS -> {
                if (left is Double && right is Double) {
                    left + right
                } else if (left is String && right is String) {
                    left + right
                } else {
                    throw RuntimeError(expression.operator, "Operands must be of the same type")
                }
            }
            TokenType.GREATER -> {
                checkNumberOperands(expression.operator, left, right)
                (left as Double) > (right as Double)
            }
            TokenType.GREATER_EQUAL -> {
                checkNumberOperands(expression.operator, left, right)
                (left as Double) >= (right as Double)
            }
            TokenType.LESS -> {
                checkNumberOperands(expression.operator, left, right)
                (left as Double) < (right as Double)
            }
            TokenType.LESS_EQUAL -> {
                checkNumberOperands(expression.operator, left, right)
                (left as Double <= right as Double)
            }
            TokenType.BANG_EQUAL -> !isEqual(left, right)
            TokenType.EQUAL_EQUAL -> isEqual(left, right)
            else -> throw Exception("Should be unreachable")
        }
    }

    override fun visitUnaryExpression(expression: Expression.Unary): Any {
        val right = evaluate(expression.right)

        return when (expression.operator.type) {
            TokenType.MINUS -> {
                checkNumberOperand(expression.operator, right)
                -(right as Double)
            }
            TokenType.BANG -> !isTruthy(right)
            else -> throw Exception("Should be unreachable")
        }
    }

    override fun visitLiteralExpression(expression: Expression.Literal): Any {
        return expression.value
    }

    private fun evaluate(expression: Expression): Any {
        return expression.accept(this)
    }

    private fun isTruthy(obj: Any?) = when (obj) {
        is Boolean -> obj
        null -> false
        else -> true
    }

    private fun isEqual(a: Any?, b: Any?): Boolean {
        if (a == null && b == null) return true
        if (a == null) return false
        return a == b
    }

    private fun checkNumberOperand(operator: Token, operand: Any) {
        if (operand is Double) return
        throw RuntimeError(operator, "Operand must be a number")
    }

    private fun checkNumberOperands(operator: Token, left: Any, right: Any) {
        if (left is Double && right is Double) return
        throw RuntimeError(operator, "Operand must be numbers")
    }
}
