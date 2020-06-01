package klox.compiler

sealed class Stmt {
    interface Visitor<R> {
        fun visitReturnStmt(stmt: Return): R
        fun visitVarStmt(stmt: Var): R
        fun visitConstStmt(stmt: Const): R
        fun visitBreakStmt(stmt: Break): R
        fun visitWhileStmt(stmt: While): R
        fun visitContinueStmt(stmt: Continue): R
        fun visitFunctionStmt(stmt: Function): R
        fun visitEmptyStmt(stmt: Empty): R
        fun visitExpressionStmt(stmt: Expression): R
        fun visitBlockStmt(stmt: Block): R
        fun visitIfStmt(stmt: If): R
        fun visitDebugStmt(stmt: Debug): R
    }

    abstract fun <R> accept(visitor: Visitor<R>): R

    class Return(
        val keyword: Token, val value: Expr
    ) : Stmt() {
        override fun <R> accept(visitor: Visitor<R>): R {
            return visitor.visitReturnStmt(this)
        }
    }

    class Var(
        val name: Token, val index: Int? = null, val initializer: Expr? = null, val type: Type? = null
    ) : Stmt() {
        override fun <R> accept(visitor: Visitor<R>): R {
            return visitor.visitVarStmt(this)
        }
    }

    class Const(
        val name: Token, val index: Int? = null, val initializer: Expr, val type: Type? = null
    ) : Stmt() {
        override fun <R> accept(visitor: Visitor<R>): R {
            return visitor.visitConstStmt(this)
        }
    }

    class Break : Stmt() {
        override fun <R> accept(visitor: Visitor<R>): R {
            return visitor.visitBreakStmt(this)
        }
    }

    class While(
        val condition: Expr, val body: Stmt
    ) : Stmt() {
        override fun <R> accept(visitor: Visitor<R>): R {
            return visitor.visitWhileStmt(this)
        }
    }

    class Continue : Stmt() {
        override fun <R> accept(visitor: Visitor<R>): R {
            return visitor.visitContinueStmt(this)
        }
    }

    class Function(
        val name: Token,
        val index: Int? = null,
        val params: List<Token>,
        val body: List<Stmt>,
        val paramTypes: LinkedHashMap<String, Type>? = null, // A LinkedHasHap will maintain internal order
        val type: Type? = null
    ) : Stmt() {
        override fun <R> accept(visitor: Visitor<R>): R {
            return visitor.visitFunctionStmt(this)
        }
    }

    class Empty : Stmt() {
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

    class Block(
        val statements: List<Stmt>
    ) : Stmt() {
        override fun <R> accept(visitor: Visitor<R>): R {
            return visitor.visitBlockStmt(this)
        }
    }

    class If(
        val condition: Expr, val thenBranch: Stmt, val elseBranch: Stmt
    ) : Stmt() {
        override fun <R> accept(visitor: Visitor<R>): R {
            return visitor.visitIfStmt(this)
        }
    }

    class Debug(
        val line: Int
    ) : Stmt() {
        override fun <R> accept(visitor: Visitor<R>): R {
            return visitor.visitDebugStmt(this)
        }
    }
}
