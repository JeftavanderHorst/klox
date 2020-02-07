package klox.compiler

/* Auto-generated using tool/generateAST.kt */

abstract class Expr {
    interface Visitor<R> {
        fun visitAssignExpr(expr: Assign): R
        fun visitGroupingExpr(expr: Grouping): R
        fun visitBinaryExpr(expr: Binary): R
        fun visitVariableExpr(expr: Variable): R
        fun visitUnaryExpr(expr: Unary): R
        fun visitLiteralExpr(expr: Literal): R
    }

    abstract fun <R> accept(visitor: Visitor<R>): R

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

    class Variable(
        val name: Token
    ) : Expr() {
        override fun <R> accept(visitor: Visitor<R>): R {
            return visitor.visitVariableExpr(this)
        }
    }

    class Unary(
        val operator: Token, val right: Expr
    ) : Expr() {
        override fun <R> accept(visitor: Visitor<R>): R {
            return visitor.visitUnaryExpr(this)
        }
    }

    class Literal(
        val value: Any
    ) : Expr() {
        override fun <R> accept(visitor: Visitor<R>): R {
            return visitor.visitLiteralExpr(this)
        }
    }
}
