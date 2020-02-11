package klox.compiler

// Correctly display whitespace in blocks

class AstPrinter : Stmt.Visitor<String>, Expr.Visitor<String> {
    fun print(statements: List<Stmt>) {
        for (stmt in statements) {
            println(stmt.accept(this))
        }
    }

    private fun parenthesize(name: String, vararg expressions: Expr): String {
        return "($name, " + expressions.joinToString { expression -> " " + expression.accept(this) } + ")"
    }

    override fun visitBlockStmt(stmt: Stmt.Block): String {
        return "block: " + stmt.statements.joinToString { statement -> statement.accept(this) }
    }

    override fun visitExpressionStmt(stmt: Stmt.Expression): String {
        return "expr: " + stmt.expression.accept(this)
    }

    override fun visitPrintStmt(stmt: Stmt.Print): String {
        return "print: " + stmt.expression.accept(this)
    }

    override fun visitIfStmt(stmt: Stmt.If): String {
        return "if:\n\t${stmt.condition.accept(this)}" +
                "\n\t[ ${stmt.thenBranch.accept(this)} ]" +
                "\n\t[ ${stmt.elseBranch.accept(this)} ]"
    }

    override fun visitWhileStmt(stmt: Stmt.While): String {
        return "while:\n\t${stmt.condition.accept(this)}" +
                "\n\t[ ${stmt.body.accept(this)} ]"
    }

    override fun visitBreakStmt(stmt: Stmt.Break): String {
        return "break"
    }

    override fun visitContinueStmt(stmt: Stmt.Continue): String {
        return "continue"
    }

    override fun visitVarStmt(stmt: Stmt.Var): String {
        return "var: " + stmt.name + " = " + stmt.initializer?.accept(this)
    }

    override fun visitEmptyStmt(stmt: Stmt.Empty): String {
        return "empty"
    }

    override fun visitGroupingExpr(expr: Expr.Grouping): String {
        return parenthesize("group", expr.expr)
    }

    override fun visitBinaryExpr(expr: Expr.Binary): String {
        return parenthesize(expr.operator.lexeme, expr.left, expr.right)
    }

    override fun visitTernaryExpr(expr: Expr.Ternary): String {
        return parenthesize("${expr.operator1.lexeme} ${expr.operator2.lexeme}", expr.left, expr.middle, expr.right)
    }

    override fun visitUnaryExpr(expr: Expr.Unary): String {
        return parenthesize(expr.operator.lexeme, expr.right)
    }

    override fun visitLiteralExpr(expr: Expr.Literal): String {
        return expr.value.toString()
    }

    override fun visitAssignExpr(expr: Expr.Assign): String {
        return "assign: " + expr.name + " = " + expr.value.accept(this)
    }

    override fun visitVariableExpr(expr: Expr.Variable): String {
        return "variable: " + expr.name
    }

    override fun visitLogicalExpr(expr: Expr.Logical): String {
        return parenthesize(expr.operator.lexeme, expr.left, expr.right)
    }
}
