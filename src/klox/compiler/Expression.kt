package klox.compiler

/* Auto-generated using tool/generateAST.kt */

abstract class Expression {
    interface Visitor<R> {
        fun visitGroupingExpression(expression: Grouping): R
        fun visitBinaryExpression(expression: Binary): R
        fun visitUnaryExpression(expression: Unary): R
        fun visitLiteralExpression(expression: Literal): R
    }

    abstract fun <R> accept(visitor: Visitor<R>): R

    class Grouping(
        val expression: Expression
    ) : Expression() {
        override fun <R> accept(visitor: Visitor<R>): R {
            return visitor.visitGroupingExpression(this)
        }
    }

    class Binary(
        val left: Expression, val operator: Token, val right: Expression
    ) : Expression() {
        override fun <R> accept(visitor: Visitor<R>): R {
            return visitor.visitBinaryExpression(this)
        }
    }

    class Unary(
        val operator: Token, val right: Expression
    ) : Expression() {
        override fun <R> accept(visitor: Visitor<R>): R {
            return visitor.visitUnaryExpression(this)
        }
    }

    class Literal(
        val value: Any
    ) : Expression() {
        override fun <R> accept(visitor: Visitor<R>): R {
            return visitor.visitLiteralExpression(this)
        }
    }
}
