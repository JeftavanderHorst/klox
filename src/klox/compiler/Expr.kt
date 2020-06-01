package klox.compiler

sealed class Expr(
    open val type: Type? = null
) {
    interface Visitor<R> {
        fun visitCallExpr(expr: Call): R
        fun visitEmptyExpr(expr: Empty): R
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

    class Call(
        val callee: Expr, val paren: Token, val arguments: List<Expr>, override val type: Type? = null
    ) : Expr() {
        override fun <R> accept(visitor: Visitor<R>): R {
            return visitor.visitCallExpr(this)
        }
    }

    class Empty(
        override val type: Type? = null
    ) : Expr() {
        override fun <R> accept(visitor: Visitor<R>): R {
            return visitor.visitEmptyExpr(this)
        }
    }

    class Variable(
        val name: Token, val distance: Int? = null, val index: Int? = null, override val type: Type? = null
    ) : Expr() {
        override fun <R> accept(visitor: Visitor<R>): R {
            return visitor.visitVariableExpr(this)
        }
    }

    class Ternary(
        val left: Expr,
        val operator1: Token,
        val middle: Expr,
        val operator2: Token,
        val right: Expr,
        override val type: Type? = null
    ) : Expr() {
        override fun <R> accept(visitor: Visitor<R>): R {
            return visitor.visitTernaryExpr(this)
        }
    }

    class Literal(
        val value: Any, override val type: Type? = null
    ) : Expr() {
        override fun <R> accept(visitor: Visitor<R>): R {
            return visitor.visitLiteralExpr(this)
        }
    }

    class Logical(
        val left: Expr, val operator: Token, val right: Expr, override val type: Type? = null
    ) : Expr() {
        override fun <R> accept(visitor: Visitor<R>): R {
            return visitor.visitLogicalExpr(this)
        }
    }

    class Assign(
        val name: Token,
        val value: Expr,
        val distance: Int? = null,
        val index: Int? = null,
        override val type: Type? = null
    ) : Expr() {
        override fun <R> accept(visitor: Visitor<R>): R {
            return visitor.visitAssignExpr(this)
        }
    }

    class Grouping(
        val expr: Expr, override val type: Type? = null
    ) : Expr() {
        override fun <R> accept(visitor: Visitor<R>): R {
            return visitor.visitGroupingExpr(this)
        }
    }

    class Binary(
        val left: Expr, val operator: Token, val right: Expr, override val type: Type? = null
    ) : Expr() {
        override fun <R> accept(visitor: Visitor<R>): R {
            return visitor.visitBinaryExpr(this)
        }
    }

    class Unary(
        val operator: Token, val right: Expr, override val type: Type? = null
    ) : Expr() {
        override fun <R> accept(visitor: Visitor<R>): R {
            return visitor.visitUnaryExpr(this)
        }
    }
}
