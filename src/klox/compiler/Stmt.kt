package klox.compiler

/* Auto-generated using tool/generateAST.kt */

abstract class Stmt {
    interface Visitor<R> {
        fun visitPrintStmt(stmt: Print): R
        fun visitEmptyStmt(stmt: Empty): R
        fun visitExpressionStmt(stmt: Expression): R
        fun visitVarStmt(stmt: Var): R
        fun visitBreakStmt(stmt: Break): R
        fun visitBlockStmt(stmt: Block): R
        fun visitWhileStmt(stmt: While): R
        fun visitContinueStmt(stmt: Continue): R
        fun visitIfStmt(stmt: If): R
    }

    abstract fun <R> accept(visitor: Visitor<R>): R

    class Print(
        val expression: Expr
    ) : Stmt() {
        override fun <R> accept(visitor: Visitor<R>): R {
            return visitor.visitPrintStmt(this)
        }
    }

    class Empty(

    ) : Stmt() {
        override fun <R> accept(visitor: Visitor<R>): R {
            return visitor.visitEmptyStmt(this)
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

    class Break(

    ) : Stmt() {
        override fun <R> accept(visitor: Visitor<R>): R {
            return visitor.visitBreakStmt(this)
        }
    }

    class Block(
        val statements: List<Stmt>
    ) : Stmt() {
        override fun <R> accept(visitor: Visitor<R>): R {
            return visitor.visitBlockStmt(this)
        }
    }

    class While(
        val condition: Expr, val body: Stmt
    ) : Stmt() {
        override fun <R> accept(visitor: Visitor<R>): R {
            return visitor.visitWhileStmt(this)
        }
    }

    class Continue(

    ) : Stmt() {
        override fun <R> accept(visitor: Visitor<R>): R {
            return visitor.visitContinueStmt(this)
        }
    }

    class If(
        val condition: Expr, val thenBranch: Stmt, val elseBranch: Stmt
    ) : Stmt() {
        override fun <R> accept(visitor: Visitor<R>): R {
            return visitor.visitIfStmt(this)
        }
    }
}