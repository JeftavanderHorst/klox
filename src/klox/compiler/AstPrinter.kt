package klox.compiler


class AstPrinter : Expression.Visitor<String> {
    fun print(expression: Expression): String {
        return expression.accept(this)
    }

    private fun parenthesize(name: String, vararg expressions: Expression): String {
        return "($name " + expressions.joinToString { expression -> " " + expression.accept(this) } + ")"
    }

    override fun visitGroupingExpression(expression: Expression.Grouping): String {
        return parenthesize("group", expression.expression)
    }

    override fun visitBinaryExpression(expression: Expression.Binary): String {
        return parenthesize(expression.operator.lexeme, expression.left, expression.right)
    }

    override fun visitUnaryExpression(expression: Expression.Unary): String {
        return parenthesize(expression.operator.lexeme, expression.right)
    }

    override fun visitLiteralExpression(expression: Expression.Literal): String {
        return expression.value.toString()
    }
}
