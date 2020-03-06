package klox.compiler

import klox.compiler.std.Std
import java.util.Stack
import kotlin.collections.ArrayList
import kotlin.collections.HashMap
import kotlin.collections.List
import kotlin.collections.MutableMap
import kotlin.collections.contains
import kotlin.collections.set

enum class VariableType {
    VAR,
    CONST,
    FUN,
    NATIVE,
    PARAMETER
}

data class Variable(
    val name: Token,
    val index: Int,
    val type: VariableType,
    val purity: Purity,
    var defined: Boolean,
    var timesAssigned: Int = 0,
    var timesAccessed: Int = 0
)

data class Scope(
    val symbols: MutableMap<String, Variable>,
    val purity: Purity
)

class Resolver : Stmt.Visitor<Stmt>, Expr.Visitor<Expr> {
    private val scopes = Stack<Scope>()
    private val errors = ArrayList<ResolveError>()
    private val warnings = ArrayList<ResolveWarning>()

    fun resolveRoot(statements: List<Stmt>): Triple<List<Stmt>, List<ResolveError>, List<ResolveWarning>> {
        beginScope()

        val std = Std().getNativeFunctions()
        std.forEach { (index: Int, name: String, fn: LoxCallable) ->
            scopes.peek().symbols[name] =
                Variable(Token(TokenType.IDENTIFIER, name, null, -1), index, VariableType.NATIVE, fn.purity(), true)
        }

        val result = resolve(statements)

        endScope()

        return Triple(result, errors, warnings)
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
        val index = declare(stmt.name, VariableType.FUN, stmt.purity)
        define(stmt.name)

        beginScope(stmt.purity)

        for (param in stmt.params) {
            declare(
                param,
                VariableType.PARAMETER,
                stmt.purity
            ) // todo: this would allow users to access parameters from pure parents
            define(param)
        }

        val body = resolve(stmt.body)

        endScope()

        return Stmt.Function(stmt.name, index, stmt.purity, stmt.params, body)
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
        val index = declare(stmt.name, VariableType.VAR, Purity.IMPURE)
        val initializer = if (stmt.initializer != null) resolve(stmt.initializer) else null
        define(stmt.name)
        return Stmt.Var(stmt.name, index, initializer)
    }

    override fun visitConstStmt(stmt: Stmt.Const): Stmt {
        val index = declare(stmt.name, VariableType.CONST, Purity.PURE)
        val initializer = resolve(stmt.initializer)
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

    override fun visitDebugStmt(stmt: Stmt.Debug): Stmt {
        println("------------------------------")
        println("RESOLVER DEBUG LINE ${stmt.line}")

        for (scope in scopes) {
            println(
                scope.symbols
                    .toList()
                    .sortedBy { (_, b) -> b.index }
                    .joinToString { (name, value) -> "$name=${value.index}" } + " ${scope.purity}"
            )
        }

        println("------------------------------")

        return stmt
    }

    override fun visitCallExpr(expr: Expr.Call): Expr {
        val callee = resolve(expr.callee)
        val arguments = expr.arguments.map { argument -> resolve(argument) }

        return Expr.Call(callee, expr.paren, arguments)
    }

    override fun visitVariableExpr(expr: Expr.Variable): Expr {
        if (scopes.peek().symbols[expr.name.lexeme]?.defined == false) {
            error(expr.name, "Cannot read variable in its own initializer.")
        }

        for (i in (scopes.size - 1) downTo 0) {
            if (scopes[i].symbols.contains(expr.name.lexeme)) {
                if (scopes.peek().purity == Purity.PURE && scopes[i].symbols[expr.name.lexeme]!!.purity != Purity.PURE) {
                    val message = when (scopes[i].symbols[expr.name.lexeme]!!.type) {
                        VariableType.VAR, VariableType.PARAMETER -> "Cannot read impure variable '${expr.name.lexeme}' from pure context"
                        VariableType.FUN, VariableType.NATIVE -> "Cannot call impure function '${expr.name.lexeme}' from pure context"
                        else -> throw Exception("This should never happen")
                    }

                    error(expr.name, message)
                }

                scopes[i].symbols[expr.name.lexeme]!!.timesAccessed += 1
                return Expr.Variable(expr.name, scopes.size - i - 1, scopes[i].symbols[expr.name.lexeme]!!.index)
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
            if (scopes[i].symbols.contains(expr.name.lexeme)) {
                if (scopes[i].symbols[expr.name.lexeme]!!.type == VariableType.CONST) {
                    error(expr.name, "Cannot reassign to const '${expr.name.lexeme}'")
                }

                scopes[i].symbols[expr.name.lexeme]!!.timesAssigned += 1
                return Expr.Assign(expr.name, value, scopes.size - i - 1, scopes[i].symbols[expr.name.lexeme]!!.index)
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

    private fun beginScope(purity: Purity = Purity.IMPURE) {
        var purity2 = purity
        for (i in (scopes.size - 1) downTo 0) {
            if (scopes[i].purity == Purity.PURE) {
                purity2 = Purity.PURE
                break;
            }
        }

        scopes.add(Scope(HashMap(), purity2))
    }

    private fun endScope() {
        for ((name, variable) in scopes.peek().symbols) {
            if (variable.type == VariableType.NATIVE)
                continue

            if (variable.timesAccessed == 0) {
                val message = when (variable.type) {
                    VariableType.VAR -> "Variable '$name' is never used"
                    VariableType.CONST -> "Const '$name' is never used"
                    VariableType.FUN -> "Function '$name' is never called"
                    VariableType.NATIVE -> "Native function '$name' is never called"
                    VariableType.PARAMETER -> "Parameter '$name' is never used"
                }

                warning(variable.name, message)
            } else if (variable.type == VariableType.VAR && variable.timesAssigned == 0) {
                warning(variable.name, "Variable '$name' can be const")
            }
        }

        scopes.pop()
    }

    private fun declare(name: Token, type: VariableType, purity: Purity): Int {
        val scope = scopes.peek().symbols

        if (scope.containsKey(name.lexeme)) {
            error(name, "Variable named '${name.lexeme}' is already declared in this scope")
        }

        val index = nextIndex()
        scope[name.lexeme] = Variable(name, index, type, purity, false)
        return index
    }

    private fun define(name: Token) {
        scopes.peek().symbols[name.lexeme]?.defined = true
    }

    private fun nextIndex(): Int {
        return scopes.peek().symbols.maxBy { (_, v) -> v.index }?.component2()?.index?.plus(1) ?: 0
    }

    private fun error(token: Token, message: String) {
        errors.add(ResolveError(token.line, message))
    }

    private fun warning(token: Token, message: String) {
        warnings.add(ResolveWarning(token.line, message))
    }
}
