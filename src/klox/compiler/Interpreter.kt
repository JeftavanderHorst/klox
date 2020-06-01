package klox.compiler

import klox.compiler.std.Std

class Interpreter(private val errorReporter: ErrorReporter) : Expr.Visitor<Any>, Stmt.Visitor<Unit> {
    private val globals = Environment()
    private var environment = globals

    class BreakException : Throwable()
    class ContinueException : Throwable()

    init {
        val std = Std().getNativeFunctions()
        std.forEach { (index: Int, name: String, fn: LoxCallable) -> globals.define(index, name, fn) }
    }

    fun interpret(statements: List<Stmt>) {
        try {
            for (statement in statements) {
                execute(statement)
            }
        } catch (error: RuntimeError) {
            errorReporter.display(error)
        }
    }

    private fun execute(statement: Stmt) {
        statement.accept(this)
    }

    fun executeBlock(statements: List<Stmt>, environment: Environment) {
        val previous = this.environment
        try {
            this.environment = environment
            for (statement in statements) {
                execute(statement)
            }
        } finally {
            this.environment = previous
        }
    }

    override fun visitBlockStmt(stmt: Stmt.Block) {
        executeBlock(stmt.statements, Environment(environment))
    }

    override fun visitVarStmt(stmt: Stmt.Var) {
        val value = if (stmt.initializer != null) evaluate(stmt.initializer) else Nil.Nil
        environment.define(stmt.index!!, stmt.name.lexeme, value)
    }

    override fun visitConstStmt(stmt: Stmt.Const) {
        val value = evaluate(stmt.initializer)
        environment.define(stmt.index!!, stmt.name.lexeme, value)
    }

    override fun visitIfStmt(stmt: Stmt.If) {
        if (isTruthy(evaluate(stmt.condition))) {
            execute(stmt.thenBranch)
        } else {
            execute(stmt.elseBranch)
        }
    }

    override fun visitExpressionStmt(stmt: Stmt.Expression) {
        evaluate(stmt.expression)
    }

    override fun visitWhileStmt(stmt: Stmt.While) {
        try {
            while (isTruthy(evaluate(stmt.condition))) {
                try {
                    execute(stmt.body)
                } catch (e: ContinueException) {
                    // Do nothing
                }
            }
        } catch (e: BreakException) {
            // Do nothing
        }
    }

    override fun visitBreakStmt(stmt: Stmt.Break) {
        throw BreakException()
    }

    override fun visitContinueStmt(stmt: Stmt.Continue) {
        throw ContinueException()
    }

    override fun visitFunctionStmt(stmt: Stmt.Function) {
        val function = LoxFunction(stmt, environment)
        environment.define(stmt.index!!, stmt.name.lexeme, function)
    }

    override fun visitReturnStmt(stmt: Stmt.Return) {
        throw Return(evaluate(stmt.value))
    }

    override fun visitDebugStmt(stmt: Stmt.Debug) {
        println("------------------------------")
        println("INTERPRETER DEBUG ON LINE ${stmt.line}")
        println(environment)
        println("------------------------------")
    }

    override fun visitEmptyStmt(stmt: Stmt.Empty) {
        return
    }

    override fun visitAssignExpr(expr: Expr.Assign): Any {
        val value = evaluate(expr.value)
        environment.assign(expr.distance!!, expr.index!!, expr.name.lexeme, value)
        return value
    }

    override fun visitGroupingExpr(expr: Expr.Grouping): Any {
        return evaluate(expr.expr)
    }

    override fun visitBinaryExpr(expr: Expr.Binary): Any {
        val left = evaluate(expr.left)
        val right = evaluate(expr.right)

        return when (expr.operator.type) {
            TokenType.PLUS -> (left as Double) + (right as Double)
            TokenType.MINUS -> (left as Double) - (right as Double)
            TokenType.SLASH -> (left as Double) / (right as Double)
            TokenType.STAR -> (left as Double) * (right as Double)
            TokenType.MODULO -> (left as Double) % (right as Double)
            TokenType.GREATER -> (left as Double) > (right as Double)
            TokenType.GREATER_EQUAL -> (left as Double) >= (right as Double)
            TokenType.LESS -> (left as Double) < (right as Double)
            TokenType.LESS_EQUAL -> (left as Double <= right as Double)
            TokenType.EQUAL_EQUAL -> isEqual(left, right)
            TokenType.BANG_EQUAL -> !isEqual(left, right)
            TokenType.COALESCE -> if (left != Nil.Nil) left else right
            else -> throw unreachable()
        }
    }

    override fun visitTernaryExpr(expr: Expr.Ternary): Any {
        if (expr.operator1.type == TokenType.QUESTION && expr.operator2.type == TokenType.COLON) {
            return if (isTruthy(expr.left)) {
                evaluate(expr.middle)
            } else {
                evaluate(expr.right)
            }
        }

        if (expr.operator1.type == TokenType.BETWEEN && expr.operator2.type == TokenType.BETWEEN_AND) {
            val left = evaluate(expr.left)
            val middle = evaluate(expr.middle)
            val right = evaluate(expr.right)
            if (left !is Double || middle !is Double || right !is Double) {
                throw unreachable()
            }

            return (left >= middle && left <= right) || (left >= right && left <= middle)
        }

        throw unreachable()
    }

    override fun visitLogicalExpr(expr: Expr.Logical): Any {
        val left = evaluate(expr.left)

        // TODO this looks wrong

        if (expr.operator.type == TokenType.OR) {
            if (isTruthy(left)) return left
        } else if (expr.operator.type == TokenType.OR) {
            if (!isTruthy(left)) return left
        } else {
            throw unreachable()
        }

        return evaluate(expr.right)
    }

    override fun visitUnaryExpr(expr: Expr.Unary): Any {
        val right = evaluate(expr.right)

        return when (expr.operator.type) {
            TokenType.MINUS -> -(right as Double)
            TokenType.BANG -> !isTruthy(right)
            else -> throw unreachable()
        }
    }

    override fun visitLiteralExpr(expr: Expr.Literal): Any {
        return expr.value
    }

    override fun visitVariableExpr(expr: Expr.Variable): Any {
        return environment.get(expr.distance!!, expr.index!!)
    }

    override fun visitCallExpr(expr: Expr.Call): Any {
        val callee = evaluate(expr.callee)
        val arguments = expr.arguments.map { argument -> evaluate(argument) }
        if (callee !is LoxCallable) {
            throw unreachable()
        }

        return callee.call(this, arguments)
    }

    override fun visitEmptyExpr(expr: Expr.Empty): Any {
        throw unreachable()
    }

    private fun evaluate(expr: Expr): Any {
        return expr.accept(this)
    }

    private fun isTruthy(obj: Any?) = when (obj) {
        is Boolean -> obj
        Nil.Nil, null -> false
        else -> true
    }

    private fun isEqual(a: Any?, b: Any?): Boolean {
        if (a == null && b == null) return true
        if (a == null) return false
        return a == b
    }

    private fun unreachable(): Exception {
        return Exception("Interpreter reached an unreachable state")
    }
}
