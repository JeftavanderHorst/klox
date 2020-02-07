package klox.compiler

/* Auto-generated using tool/generateAST.kt */

abstract class Stmt {
    interface Visitor<R> {
        fun visitBlockStmt(stmt: Block): R
        fun visitPrintStmt(stmt: Print): R
        fun visitExpressionStmt(stmt: Expression): R
        fun visitVarStmt(stmt: Var): R
        fun visitEmptyStmt(stmt: Empty): R
    }

    abstract fun <R> accept(visitor: Visitor<R>): R

    class Block(
        val statements: List<Stmt>
    ) : Stmt() {
        override fun <R> accept(visitor: Visitor<R>): R {
            return visitor.visitBlockStmt(this)
        }
    }

    class Print(
        val expression: Expr
    ) : Stmt() {
        override fun <R> accept(visitor: Visitor<R>): R {
            return visitor.visitPrintStmt(this)
        }
    }

    class Expression(
        val expression: Expr
    ) : Stmt() {
        override fun <R> accept(visitor: Visitor<R>): R {
            return visitor.visitExpressionStmt(this)
        }
    }

    class Var(
        val name: Token, val initializer: Expr?
    ) : Stmt() {
        override fun <R> accept(visitor: Visitor<R>): R {
            return visitor.visitVarStmt(this)
        }
    }

    class Empty: Stmt() {
        override fun <R> accept(visitor: Visitor<R>): R {
            return visitor.visitEmptyStmt(this)
        }
    }
}
