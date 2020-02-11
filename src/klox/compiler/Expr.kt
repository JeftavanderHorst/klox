package klox.compiler

/* Auto-generated using tool/generateAST.kt */

abstract class Expr {
    interface Visitor<R> {
        fun visitVariableExpr(expr: Variable): R
        fun visitTernaryExpr(expr: Ternary): R
        fun visitLiteralExpr(expr: Literal): R
        fun visitLogicalExpr(expr: Logical): R
        fun visitAssignExpr(expr: Assign): R
        fun visitGroupingExpr(expr: Grouping): R
        fun visitBinaryExpr(expr: Binary): R
        fun visitUnaryExpr(expr: Unary): R
    }

    abstract fun <R> accept(visitor: Visitor<R>): R

    class Variable(
        val name: Token
    ) : Expr() {
        override fun <R> accept(visitor: Visitor<R>): R {
            return visitor.visitVariableExpr(this)
        }
    }

    class Ternary(
        val left: Expr, val operator1: Token, val middle: Expr, val operator2: Token, val right: Expr
    ) : Expr() {
        override fun <R> accept(visitor: Visitor<R>): R {
            return visitor.visitTernaryExpr(this)
        }
    }

    class Literal(
        val value: Any
    ) : Expr() {
        override fun <R> accept(visitor: Visitor<R>): R {
            return visitor.visitLiteralExpr(this)
        }
    }

    class Logical(
        val left: Expr, val operator: Token, val right: Expr
    ) : Expr() {
        override fun <R> accept(visitor: Visitor<R>): R {
            return visitor.visitLogicalExpr(this)
        }
    }

    class Assign(
        val name: Token, val value: Expr
    ) : Expr() {
        override fun <R> accept(visitor: Visitor<R>): R {
            return visitor.visitAssignExpr(this)
        }
    }

    class Grouping(
        val expr: Expr
    ) : Expr() {
        override fun <R> accept(visitor: Visitor<R>): R {
            return visitor.visitGroupingExpr(this)
        }
    }

    class Binary(
        val left: Expr, val operator: Token, val right: Expr
    ) : Expr() {
        override fun <R> accept(visitor: Visitor<R>): R {
            return visitor.visitBinaryExpr(this)
        }
    }

    class Unary(
        val operator: Token, val right: Expr
    ) : Expr() {
        override fun <R> accept(visitor: Visitor<R>): R {
            return visitor.visitUnaryExpr(this)
        }
    }
}
