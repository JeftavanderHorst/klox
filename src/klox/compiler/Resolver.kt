package klox.compiler

import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap
import kotlin.collections.List
import kotlin.collections.MutableMap
import kotlin.collections.contains
import kotlin.collections.set

// TODO: either synchronize or throw

class Resolver(private val interpreter: Interpreter) : Stmt.Visitor<Void?>, Expr.Visitor<Void?> {
    private val scopes = Stack<MutableMap<String, Boolean>>()
    private val errors = ArrayList<ResolveError>()

    fun resolve(statements: List<Stmt>): List<ResolveError> {
        for (statement in statements) {
            resolve(statement)
        }

        return errors
    }

    private fun resolve(statement: Stmt) {
        statement.accept(this)
    }

    private fun resolve(expression: Expr) {
        expression.accept(this)
    }

    override fun visitFunctionStmt(stmt: Stmt.Function): Void? {
        declare(stmt.name)
        define(stmt.name)
        resolveFunction(stmt)
        return null
    }

    override fun visitEmptyStmt(stmt: Stmt.Empty): Void? {
        return null
    }

    override fun visitReturnStmt(stmt: Stmt.Return): Void? {
        if (stmt.value != null) {
            resolve(stmt.value)
        }

        return null
    }

    override fun visitExpressionStmt(stmt: Stmt.Expression): Void? {
        resolve(stmt.expression)
        return null
    }

    override fun visitVarStmt(stmt: Stmt.Var): Void? {
        declare(stmt.name)
        if (stmt.initializer != null) {
            resolve(stmt.initializer)
        }
        define(stmt.name)
        return null
    }


    override fun visitBreakStmt(stmt: Stmt.Break): Void? {
        return null
    }

    override fun visitBlockStmt(stmt: Stmt.Block): Void? {
        beginScope()
        resolve(stmt.statements)
        endScope()
        return null
    }

    override fun visitWhileStmt(stmt: Stmt.While): Void? {
        resolve(stmt.condition)
        resolve(stmt.body)
        return null
    }

    override fun visitContinueStmt(stmt: Stmt.Continue): Void? {
        return null
    }

    override fun visitIfStmt(stmt: Stmt.If): Void? {
        resolve(stmt.condition)
        resolve(stmt.thenBranch)
        if (stmt.elseBranch !is Stmt.Empty) {
            resolve(stmt.elseBranch)
        }

        return null
    }

    override fun visitCallExpr(expr: Expr.Call): Void? {
        resolve(expr.callee)

        for (argument in expr.arguments) {
            resolve(argument)
        }

        return null
    }

    override fun visitVariableExpr(expr: Expr.Variable): Void? {
        if (!scopes.isEmpty() && scopes.peek()[expr.name.lexeme] == false) {
            error(expr.name, "Cannot read local variable in its own initializer.")
            // TODO: should this return?
        }

        resolveLocal(expr, expr.name)
        return null
    }

    override fun visitTernaryExpr(expr: Expr.Ternary): Void? {
        return null
    }

    override fun visitLiteralExpr(expr: Expr.Literal): Void? {
        return null
    }

    override fun visitLogicalExpr(expr: Expr.Logical): Void? {
        resolve(expr.left)
        resolve(expr.right)

        return null
    }

    override fun visitAssignExpr(expr: Expr.Assign): Void? {
        resolve(expr.value)
        resolveLocal(expr, expr.name)
        return null
    }

    override fun visitGroupingExpr(expr: Expr.Grouping): Void? {
        resolve(expr.expr)
        return null
    }

    override fun visitBinaryExpr(expr: Expr.Binary): Void? {
        resolve(expr.left)
        resolve(expr.right)
        return null
    }

    override fun visitUnaryExpr(expr: Expr.Unary): Void? {
        resolve(expr.right)
        return null
    }

    private fun beginScope() {
        scopes.add(HashMap())
    }

    private fun endScope() {
        scopes.pop()
    }

    private fun declare(name: Token) {
        if (scopes.isEmpty()) {
            return
        }

        val scope = scopes.peek()

        if (scope.containsKey(name.lexeme)) {
            error(name, "Variable named '${name.lexeme}' is already declared in this scope")
        }

        scope[name.lexeme] = false
    }

    private fun define(name: Token) {
        if (scopes.isEmpty()) {
            return
        }

        scopes.peek()[name.lexeme] = true
    }

    private fun resolveLocal(expr: Expr, name: Token) {
        for (i in (scopes.size - 1) downTo 0) {
            if (scopes[i].contains(name.lexeme)) {
                interpreter.resolve(expr, scopes.size - 1 - i)
                return
            }
        }

        // Not found, assume it's global
    }

    private fun resolveFunction(function: Stmt.Function) {
        beginScope()

        for (param in function.params) {
            declare(param)
            define(param)
        }

        resolve(function.body)

        endScope()
    }

    private fun error(token: Token, message: String) {
        errors.add(ResolveError(token.line, message))
    }
}
