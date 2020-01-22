package klox.compiler

class Interpreter : Expression.Visitor<Any> {
    fun interpret(expression: Expression): Pair<String?, List<RuntimeError>> {
        return try {
            Pair(stringify(evaluate(expression)), emptyList())
        } catch (e: RuntimeError) {
            Pair(null, listOf(e))
        }
    }

    private fun stringify(obj: Any): String {
        if (obj is Nil) return "nil"

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
                if ((left !is Double && left !is String) || (right !is Double && right !is String)) {
                    throw AdditionNotSupportedForTypeError(expression.operator.line)
                } else if (left is Double && right is Double) {
                    left + right
                } else if (left is String && right is String) {
                    println("${left::class.simpleName}")
                    println("${right::class.simpleName}")
                    left + right
                } else {
                    throw IncompatibleTypesForAdditionError(expression.operator.line)
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
        throw OperandNotDoubleError(operator.line)
    }

    private fun checkNumberOperands(operator: Token, left: Any, right: Any) {
        if (left is Double && right is Double) return
        throw OperandsNotDoubleError(operator.line)
    }
}
