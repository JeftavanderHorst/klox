package klox.compiler

enum class Nil { Nil }

class Parser(
    private val tokens: List<Token>
) {
    private val errors: MutableList<ParseError> = ArrayList()
    private var current = 0

    fun parse(): Pair<List<Stmt>, List<ParseError>> {
        val statements: MutableList<Stmt> = ArrayList()
        while (!isAtEnd()) {
            val dec = declaration()
            statements.add(dec)
        }

        return Pair(statements, errors)
    }

    private fun declaration(): Stmt {
        try {
            if (match(TokenType.VAR)) return varDeclaration()
            return statement()
        } catch (e: ParseError) {
            synchronize()
            return Stmt.Empty()
        }
    }

    private fun statement(): Stmt {
        if (match(TokenType.PRINT)) return printStatement()
        if (match(TokenType.LEFT_BRACE)) return Stmt.Block(block())
        return expressionStatement()
    }

    private fun printStatement(): Stmt {
        val value = expression()
        consume(TokenType.SEMICOLON, "Expected ';' after value")
        return Stmt.Print(value)
    }

    private fun varDeclaration(): Stmt {
        val name = consume(TokenType.IDENTIFIER, "Expected variable name")
        val initializer = if (match(TokenType.EQUAL)) expression() else null
        consume(TokenType.SEMICOLON, "Expected ';' after value")
        return Stmt.Var(name, initializer)
    }

    private fun expressionStatement(): Stmt {
        val value = expression()
        consume(TokenType.SEMICOLON, "Expected ';' after value")
        return Stmt.Expression(value)
    }

    private fun block(): List<Stmt> {
        val statements: MutableList<Stmt> = ArrayList()
        while (!check(TokenType.RIGHT_BRACE) && !isAtEnd()) {
            statements.add(declaration())
        }

        consume(TokenType.RIGHT_BRACE, "Expected '}' after block")
        return statements
    }

    private fun expression(): Expr {
        return assignment()
    }

    private fun assignment(): Expr {
        val expr = equality()

        if (match(TokenType.EQUAL)) {
            val equals = previous()
            val value = assignment()

            if (expr is Expr.Variable) {
                val name = expr.name
                return Expr.Assign(name, value)
            }

            throw error(equals, "Invalid assignment target")
        }

        return expr
    }

    private fun equality(): Expr {
        var expression = comparison()

        while (match(TokenType.BANG_EQUAL, TokenType.EQUAL_EQUAL)) {
            expression = Expr.Binary(expression, previous(), comparison())
        }

        return expression
    }

    private fun comparison(): Expr {
        var expression = addition()

        while (match(TokenType.GREATER, TokenType.GREATER_EQUAL, TokenType.LESS, TokenType.LESS_EQUAL)) {
            expression = Expr.Binary(expression, previous(), addition())
        }

        return expression
    }

    private fun addition(): Expr {
        var expression = multiplication()

        while (match(TokenType.MINUS, TokenType.PLUS)) {
            expression = Expr.Binary(expression, previous(), multiplication())
        }

        return expression
    }

    private fun multiplication(): Expr {
        var expression = unary()

        while (match(TokenType.SLASH, TokenType.STAR)) {
            expression = Expr.Binary(expression, previous(), unary())
        }

        return expression
    }

    private fun unary(): Expr {
        if (match(TokenType.BANG, TokenType.MINUS))
            return Expr.Unary(previous(), unary())
        return primary()
    }

    private fun primary(): Expr {
        if (match(TokenType.FALSE)) return Expr.Literal(false)
        if (match(TokenType.TRUE)) return Expr.Literal(true)
        if (match(TokenType.NIL)) return Expr.Literal(Nil.Nil)
        if (match(TokenType.NUMBER, TokenType.STRING)) return Expr.Literal(previous().literal!!) // TODO
        if (match(TokenType.IDENTIFIER)) return Expr.Variable(previous())
        if (match(TokenType.LEFT_PAREN)) {
            val expression = expression()
            consume(TokenType.RIGHT_PAREN, "Expected ')' after expression")
            return Expr.Grouping(expression)
        }

        throw error(peek(), "No expression provided")
    }

    private fun match(vararg types: TokenType): Boolean {
        for (type in types) {
            if (check(type)) {
                advance()
                return true
            }
        }

        return false
    }

    private fun consume(type: TokenType, error: String): Token {
        if (check(type))
            return advance()
        throw error(peek(), error)
    }

    private fun check(type: TokenType): Boolean {
        if (isAtEnd()) return false
        return peek().type == type
    }

    private fun advance(): Token {
        if (!isAtEnd()) current++
        return previous()
    }

    private fun isAtEnd(): Boolean {
        return peek().type === TokenType.EOF
    }

    private fun peek(): Token {
        return tokens[current]
    }

    private fun previous(): Token {
        return tokens[current - 1]
    }

    private fun error(token: Token, message: String): ParseError {
        val error = ParseError(token.line, message)
        errors.add(error)
        return error
    }

    private fun synchronize() {
        advance()

        while (!isAtEnd()) {
            if (previous().type == TokenType.SEMICOLON) {
                return
            }

            val next = peek().type
            if (next == TokenType.CLASS
                || next == TokenType.FUN
                || next == TokenType.VAR
                || next == TokenType.FOR
                || next == TokenType.IF
                || next == TokenType.WHILE
                || next == TokenType.PRINT
                || next == TokenType.RETURN
            ) {
                return
            }

            advance()
        }
    }
}
