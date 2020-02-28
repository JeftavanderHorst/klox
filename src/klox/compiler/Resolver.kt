package klox.compiler

import klox.compiler.std.Std
import java.util.Stack
import kotlin.collections.ArrayList
import kotlin.collections.HashMap
import kotlin.collections.List
import kotlin.collections.MutableMap
import kotlin.collections.contains
import kotlin.collections.set

data class Variable(val name: Token, val index: Int, var defined: Boolean)

class Resolver : Stmt.Visitor<Stmt>, Expr.Visitor<Expr> {
    private val scopes = Stack<MutableMap<String, Variable>>()
    private val errors = ArrayList<ResolveError>()

    fun resolveRoot(statements: List<Stmt>): Pair<List<Stmt>, List<ResolveError>> {
        beginScope()

        val std = Std().getNativeFunctions()
        std.forEach { (index: Int, name: String, _: LoxCallable) ->
            scopes.peek()[name] =
                Variable(Token(TokenType.IDENTIFIER, name, null, -1), index, true)
        }

        val result = resolve(statements)

        endScope()

        return Pair(result, errors)
    }

    private fun resolve(statements: List<Stmt>): List<Stmt> {
        return statements.map { s -> resolve(s) }
    }

    private fun resolve(statement: Stmt): Stmt {
        return statement.accept(this)
    }

    private fun resolve(expression: Expr): Expr {
        return expression.accept(this)
    }

    override fun visitFunctionStmt(stmt: Stmt.Function): Stmt {
        val index = declare(stmt.name)
        define(stmt.name)

        beginScope()

        for (param in stmt.params) {
            declare(param)
            define(param)
        }

        val body = resolve(stmt.body)

        endScope()

        return Stmt.Function(stmt.name, index, stmt.params, body)
    }

    override fun visitEmptyStmt(stmt: Stmt.Empty): Stmt {
        return stmt
    }

    override fun visitReturnStmt(stmt: Stmt.Return): Stmt {
        if (stmt.value != null) {
            return Stmt.Return(stmt.keyword, resolve(stmt.value))
        }

        return stmt
    }

    override fun visitExpressionStmt(stmt: Stmt.Expression): Stmt {
        return Stmt.Expression(resolve(stmt.expression))
    }

    override fun visitVarStmt(stmt: Stmt.Var): Stmt {
        val index = declare(stmt.name)
        val initializer = if (stmt.initializer != null) resolve(stmt.initializer) else null
        define(stmt.name)
        return Stmt.Var(stmt.name, index, initializer)
    }

    override fun visitBreakStmt(stmt: Stmt.Break): Stmt {
        return stmt
    }

    override fun visitBlockStmt(stmt: Stmt.Block): Stmt {
        beginScope()
        val resolved = resolve(stmt.statements)
        endScope()
        return Stmt.Block(resolved)
    }

    override fun visitWhileStmt(stmt: Stmt.While): Stmt {
        return Stmt.While(
            resolve(stmt.condition),
            resolve(stmt.body)
        )
    }

    override fun visitContinueStmt(stmt: Stmt.Continue): Stmt {
        return stmt
    }

    override fun visitIfStmt(stmt: Stmt.If): Stmt {
        val condition = resolve(stmt.condition)
        val thenBranch = resolve(stmt.thenBranch)
        val elseBranch = if (stmt.elseBranch !is Stmt.Empty) {
            resolve(stmt.elseBranch)
        } else {
            Stmt.Empty()
        }

        return Stmt.If(condition, thenBranch, elseBranch)
    }

    override fun visitCallExpr(expr: Expr.Call): Expr {
        val callee = resolve(expr.callee)
        val arguments = expr.arguments.map { argument -> resolve(argument) }

        return Expr.Call(callee, expr.paren, arguments)
    }

    override fun visitVariableExpr(expr: Expr.Variable): Expr {
        if (scopes.peek()[expr.name.lexeme]?.defined == false) {
            error(expr.name, "Cannot read variable in its own initializer.")
        }

        for (i in (scopes.size - 1) downTo 0) {
            if (scopes[i].contains(expr.name.lexeme)) {
                return Expr.Variable(expr.name, scopes.size - i - 1, scopes[i][expr.name.lexeme]!!.index)
            }
        }

        error(expr.name, "Undefined variable ${expr.name.lexeme}")
        return Expr.Empty()
    }

    override fun visitTernaryExpr(expr: Expr.Ternary): Expr {
        return Expr.Ternary(
            resolve(expr.left),
            expr.operator1,
            resolve(expr.middle),
            expr.operator2,
            resolve(expr.right)
        )
    }

    override fun visitLiteralExpr(expr: Expr.Literal): Expr {
        return expr
    }

    override fun visitLogicalExpr(expr: Expr.Logical): Expr {
        return Expr.Logical(
            resolve(expr.left),
            expr.operator,
            resolve(expr.right)
        )
    }

    override fun visitAssignExpr(expr: Expr.Assign): Expr {
        val value = resolve(expr.value)

        for (i in (scopes.size - 1) downTo 0) {
            if (scopes[i].contains(expr.name.lexeme)) {
                return Expr.Assign(expr.name, value, scopes.size - i - 1, scopes[i][expr.name.lexeme]!!.index)
            }
        }

        error(expr.name, "Undeclared variable ${expr.name.lexeme}")
        return Expr.Empty()
    }

    override fun visitGroupingExpr(expr: Expr.Grouping): Expr {
        return Expr.Grouping(resolve(expr.expr))
    }

    override fun visitBinaryExpr(expr: Expr.Binary): Expr {
        return Expr.Binary(
            resolve(expr.left),
            expr.operator,
            resolve(expr.right)
        )
    }

    override fun visitUnaryExpr(expr: Expr.Unary): Expr {
        return Expr.Unary(expr.operator, resolve(expr.right))
    }

    override fun visitEmptyExpr(expr: Expr.Empty): Expr {
        return Expr.Empty()
    }

    private fun beginScope() {
        scopes.add(HashMap())
    }

    private fun endScope() {
        scopes.pop()
    }

    private fun declare(name: Token): Int {
        val scope = scopes.peek()

        if (scope.containsKey(name.lexeme)) {
            error(name, "Variable named '${name.lexeme}' is already declared in this scope")
        }

        val index = nextIndex()
        scope[name.lexeme] = Variable(name, index, false)
        return index
    }

    private fun define(name: Token) {
        scopes.peek()[name.lexeme]?.defined = true
    }

    private fun nextIndex(): Int {
        return scopes.peek().maxBy { (_, v) -> v.index }?.component2()?.index?.plus(1) ?: 0
    }

    private fun error(token: Token, message: String) {
        errors.add(ResolveError(token.line, message))
    }
}
