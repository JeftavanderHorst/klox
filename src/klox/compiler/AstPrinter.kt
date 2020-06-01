package klox.compiler

// TODO: Correctly display whitespace in blocks

class AstPrinter {
    val ANSI_RESET = "\u001B[0m"
    val ANSI_BLACK = "\u001B[30m"
    val ANSI_RED = "\u001B[31m"
    val ANSI_GREEN = "\u001B[32m"
    val ANSI_YELLOW = "\u001B[33m"
    val ANSI_BLUE = "\u001B[34m"
    val ANSI_PURPLE = "\u001B[35m"
    val ANSI_CYAN = "\u001B[36m"
    val ANSI_WHITE = "\u001B[37m"

    private fun red(string: String) = "$ANSI_RED$string$ANSI_RESET"
    private fun yellow(string: String) = "$ANSI_YELLOW$string$ANSI_RESET"
    private fun cyan(string: String) = "$ANSI_CYAN$string$ANSI_RESET"

    fun print(statements: List<Stmt>) {
        for (stmt in statements) {
            println(makeString(stmt))
        }
    }

    private fun parenthesize(strings: List<String>) = "(${strings.joinToString(" ")})"
    private fun parenthesize(vararg strings: String) = "(${strings.joinToString(" ")})"

    fun makeString(stmt: Stmt): String = when (stmt) {
        is Stmt.Expression -> parenthesize(listOf(makeString(stmt.expression)))
        is Stmt.Block -> parenthesize(stmt.statements.map { s -> makeString(s) })
        is Stmt.Var -> {
            val name = cyan("${stmt.name.lexeme}_${stmt.index}")
            if (stmt.initializer != null) {
                parenthesize("var", name, makeString(stmt.initializer), type(stmt.type))
            } else {
                parenthesize("var", name, yellow("null"), type(stmt.type))
            }
        }
        is Stmt.Const -> parenthesize(
            "const",
            cyan("${stmt.name.lexeme}_${stmt.index}"),
            makeString(stmt.initializer),
            type(stmt.type)
        )
        is Stmt.Function -> parenthesize(
            "fun",
            cyan("${stmt.name.lexeme}_${stmt.index}"),
            parenthesize(stmt.body.map { s -> makeString(s) }),
            type(stmt.type)
        )
        is Stmt.Return -> parenthesize("return", makeString(stmt.value))
        is Stmt.If -> parenthesize(
            "if",
            makeString(stmt.condition),
            makeString(stmt.thenBranch),
            makeString(stmt.elseBranch)
        )
        is Stmt.While -> parenthesize(
            "while",
            makeString(stmt.condition),
            makeString(stmt.body)
        )
        is Stmt.Continue -> parenthesize("continue")
        is Stmt.Break -> parenthesize("break")
        is Stmt.Debug -> parenthesize("debug")
        is Stmt.Empty -> parenthesize("empty")
    }

    private fun type(type: Type?): String = red(type?.toString() ?: "???")

    fun makeString(expr: Expr): String = when (expr) {
        is Expr.Call -> parenthesize(
            "call",
            makeString(expr.callee),
            parenthesize(expr.arguments.map { e -> makeString(e) }),
            type(expr.type)
        )
        is Expr.Variable -> parenthesize(
            cyan("${expr.name.lexeme}_${expr.index}"),
            type(expr.type)
        )
        is Expr.Literal -> parenthesize(yellow(expr.value.toString()), type(expr.type))
        is Expr.Assign -> parenthesize(
            "assign",
            cyan("${expr.name.lexeme}_${expr.index}"),
            makeString(expr.value),
            type(expr.type)
        )
        is Expr.Grouping -> parenthesize(makeString(expr.expr))
        is Expr.Unary -> parenthesize(
            expr.operator.lexeme,
            makeString(expr.right),
            type(expr.type)
        )
        is Expr.Binary -> parenthesize(
            expr.operator.lexeme,
            makeString(expr.left),
            makeString(expr.right),
            type(expr.type)
        )
        is Expr.Ternary -> parenthesize(
            "${expr.operator1.lexeme}-${expr.operator2.lexeme}",
            makeString(expr.left),
            makeString(expr.middle),
            makeString(expr.right),
            type(expr.type)
        )
        is Expr.Logical -> parenthesize(
            expr.operator.lexeme,
            makeString(expr.left),
            makeString(expr.right),
            type(expr.type)
        )
        is Expr.Empty -> "empty"
    }
}
