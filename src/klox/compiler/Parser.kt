package klox.compiler

enum class Nil { Nil }

class Parser(
    private val tokens: List<Token>
) {
    private val errors: MutableList<ParseError> = ArrayList()
    private var current = 0
    private var loopDepth = 0
    private var functionDepth = 0

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
            if (match(TokenType.CONST)) return constDeclaration()
            if (match(TokenType.FUN)) return function()
            return statement()
        } catch (e: ParseError) {
            synchronize()
            return Stmt.Empty()
        }
    }

    private fun statement(): Stmt {
        if (match(TokenType.IF)) return ifStatement()
        if (match(TokenType.WHILE)) return whileStatement()
        if (match(TokenType.FOR)) return forStatement()
        if (match(TokenType.LOOP)) return loopStatement()
        if (match(TokenType.BREAK)) return breakStatement()
        if (match(TokenType.RETURN)) return returnStatement()
        if (match(TokenType.CONTINUE)) return continueStatement()
        if (match(TokenType.DEBUG)) return debugStatement()
        if (match(TokenType.LEFT_BRACE)) return Stmt.Block(block())
        return expressionStatement()
    }

    private fun ifStatement(): Stmt {
        consume(TokenType.LEFT_PAREN, "Expected '(' after 'if'")
        val condition = expression()
        consume(TokenType.RIGHT_PAREN, "Expected ')' after if condition")

        val thenBranch = statement()
        val elseBranch = if (match(TokenType.ELSE)) {
            statement()
        } else {
            Stmt.Empty()
        }

        return Stmt.If(condition, thenBranch, elseBranch)
    }

    private fun returnStatement(): Stmt {
        if (functionDepth <= 0) {
            throw error(previous(), "Must be inside a function to use 'return'")
        }

        val keyword = previous()
        val value = if (!check(TokenType.SEMICOLON)) {
            expression()
        } else {
            Expr.Literal(Nil.Nil)
        }

        consume(TokenType.SEMICOLON, "Expected ';' after return")

        return Stmt.Return(keyword, value)
    }

    private fun breakStatement(): Stmt {
        if (loopDepth <= 0) {
            throw error(previous(), "Must be inside a loop to use 'break'")
        }

        consume(TokenType.SEMICOLON, "Expected ';' after 'break'")
        return Stmt.Break()
    }

    private fun continueStatement(): Stmt {
        if (loopDepth <= 0) {
            throw error(previous(), "Must be inside a loop to use 'continue'")
        }

        consume(TokenType.SEMICOLON, "Expected ';' after 'continue'")
        return Stmt.Continue()
    }

    private fun debugStatement(): Stmt {
        consume(TokenType.SEMICOLON, "Expected ';' after 'debug'")
        return Stmt.Debug(previous().line)
    }

    private fun varDeclaration(): Stmt {
        val name = consume(TokenType.IDENTIFIER, "Expected variable name")
        val initializer = if (match(TokenType.EQUAL)) expression() else null
        // todo initializer may not be present, alter error description
        consume(TokenType.SEMICOLON, "Expected ';' after initializer")
        return Stmt.Var(name, null, initializer)
    }

    private fun constDeclaration(): Stmt {
        val name = consume(TokenType.IDENTIFIER, "Expected const name")
        consume(TokenType.EQUAL, "Const must have an initializer")
        val initializer = expression()
        consume(TokenType.SEMICOLON, "Expected ';' after initializer")
        return Stmt.Const(name, null, initializer)
    }

    private fun whileStatement(): Stmt {
        consume(TokenType.LEFT_PAREN, "Expected '(' after 'while'")
        val condition = expression()
        consume(TokenType.RIGHT_PAREN, "Expected ')' after while condition")

        try {
            loopDepth += 1
            val body = statement()
            return Stmt.While(condition, body)
        } finally {
            loopDepth -= 1
        }
    }

    private fun forStatement(): Stmt {
        consume(TokenType.LEFT_PAREN, "Expected '(' after 'for'")

        val initializer = when {
            match(TokenType.SEMICOLON) -> Stmt.Empty()
            match(TokenType.VAR) -> varDeclaration()
            match(TokenType.CONST) -> throw error(previous(), "Cannot use 'const' inside a for loop declaration")
            else -> expressionStatement()
        }

        var condition: Expr? = null
        if (!check(TokenType.SEMICOLON)) {
            condition = expression()
        }
        consume(TokenType.SEMICOLON, "Expected ';' after loop condition")

        var increment: Expr? = null
        if (!check(TokenType.RIGHT_PAREN)) {
            increment = expression()
        }
        consume(TokenType.RIGHT_PAREN, "Expected ')' after for clause")

        try {
            loopDepth += 1

            var body = statement()

            if (increment != null) {
                body = Stmt.Block(listOf(body, Stmt.Expression(increment)))
            }

            if (condition == null) {
                condition = Expr.Literal(true)
            }
            body = Stmt.While(condition, body)

            body = Stmt.Block(listOf(initializer, body))

            return body
        } finally {
            loopDepth -= 1
        }
    }

    private fun loopStatement(): Stmt {
        try {
            loopDepth += 1
            return Stmt.While(Expr.Literal(true), statement())
        } finally {
            loopDepth -= 1
        }
    }

    private fun expressionStatement(): Stmt {
        val value = expression()
        consume(TokenType.SEMICOLON, "Expected ';' after value")
        return Stmt.Expression(value)
    }

    private fun function(): Stmt.Function {
        val name = consume(TokenType.IDENTIFIER, "Expected function name")
        consume(TokenType.LEFT_PAREN, "Expected '(' after function name")
        val parameters: MutableList<Token> = ArrayList()
        if (!check(TokenType.RIGHT_PAREN)) {
            do {
                parameters.add(consume(TokenType.IDENTIFIER, "Expected parameter name"))
            } while (match(TokenType.COMMA))
        }

        consume(TokenType.RIGHT_PAREN, "Expected ')' after function parameters")
        consume(TokenType.LEFT_BRACE, "Expected '{' to start function body")

        try {
            functionDepth += 1
            val body = block()
            return Stmt.Function(name, null, parameters, body)
        } finally {
            functionDepth -= 1
        }
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
        val expr = ternary()

        if (match(
                TokenType.EQUAL,
                TokenType.PLUS_EQUAL,
                TokenType.MINUS_EQUAL,
                TokenType.STAR_EQUAL,
                TokenType.SLASH_EQUAL,
                TokenType.MODULO_EQUAL,
                TokenType.COALESCE_EQUAL
            )
        ) {
            val operator = previous()

            if (expr is Expr.Variable) {
                val name = expr.name
                val value = assignment()

                if (operator.type != TokenType.EQUAL) {
                    val binaryOperator = when (operator.type) {
                        TokenType.PLUS_EQUAL -> TokenType.PLUS
                        TokenType.MINUS_EQUAL -> TokenType.MINUS
                        TokenType.STAR_EQUAL -> TokenType.STAR
                        TokenType.SLASH_EQUAL -> TokenType.SLASH
                        TokenType.MODULO_EQUAL -> TokenType.MODULO
                        TokenType.COALESCE_EQUAL -> TokenType.COALESCE
                        else -> {
                            throw error(operator, "Unknown assignment operator '${operator.lexeme}'")
                        }
                    }

                    val lexeme = when (operator.type) {
                        TokenType.PLUS_EQUAL -> "+"
                        TokenType.MINUS_EQUAL -> "-"
                        TokenType.STAR_EQUAL -> "*"
                        TokenType.SLASH_EQUAL -> "/"
                        TokenType.MODULO_EQUAL -> "%"
                        TokenType.COALESCE_EQUAL -> "??"
                        else -> {
                            throw error(operator, "Fatal error")
                        }
                    }

                    val token = Token(binaryOperator, lexeme, null, operator.line)
                    return Expr.Assign(
                        name,
                        Expr.Binary(
                            Expr.Variable(name), // TODO how does this mix with consts?
                            token,
                            value
                        )
                    )
                }

                return Expr.Assign(name, value)
            }

            throw error(operator, "Invalid assignment target")
        }

        return expr
    }

    private fun ternary(): Expr {
        var expr = coalescing()

        if (match(TokenType.QUESTION)) {
            expr = Expr.Ternary(
                expr,
                previous(),
                coalescing(),
                consume(TokenType.COLON, "Expected ':' in ternary conditional expression"),
                coalescing()
            )

            if (peek().type == TokenType.QUESTION) {
                throw error(peek(), "Chained ternary conditional expressions are ambiguous and therefore forbidden")
            }
        }

        if (match(TokenType.COLON)) {
            throw error(previous(), "Character ':' used in an illegal context")
        }

        if (match(TokenType.BETWEEN)) {
            expr = Expr.Ternary(
                expr,
                previous(),
                coalescing(),
                consume(TokenType.BETWEEN_AND, "Expected 'and' in 'between' expression"),
                coalescing()
            )

            if (peek().type == TokenType.BETWEEN) {
                throw error(peek(), "Chained uses of 'between' are ambiguous and therefore forbidden")
            }
        }

        if (match(TokenType.BETWEEN_AND)) {
            throw error(previous(), "Keyword 'and' used in an illegal context (did you mean '&&'?)")
        }

        return expr
    }

    private fun coalescing(): Expr {
        var expr = or()

        while (match(TokenType.COALESCE)) {
            expr = Expr.Binary(expr, previous(), or())
        }

        return expr
    }

    private fun or(): Expr {
        var expr = and()

        while (match(TokenType.OR)) {
            val operator = previous()
            val right = and()
            expr = Expr.Logical(expr, operator, right)
        }

        return expr
    }

    private fun and(): Expr {
        var expr = equality()

        while (match(TokenType.AND)) {
            val operator = previous()
            val right = equality()
            expr = Expr.Logical(expr, operator, right)
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

        while (match(TokenType.SLASH, TokenType.STAR, TokenType.MODULO)) {
            expression = Expr.Binary(expression, previous(), unary())
        }

        return expression
    }

    private fun unary(): Expr {
        if (match(TokenType.BANG, TokenType.MINUS))
            return Expr.Unary(previous(), unary())
        return call()
    }

    private fun finishCall(callee: Expr): Expr {
        val arguments: MutableList<Expr> = ArrayList()
        if (!check(TokenType.RIGHT_PAREN)) {
            do {
                arguments.add(expression())
            } while (match(TokenType.COMMA))
        }

        val paren = consume(TokenType.RIGHT_PAREN, "Expected ')' after arguments")

        return Expr.Call(callee, paren, arguments)
    }

    private fun call(): Expr {
        var expr = primary()

        while (true) {
            if (match(TokenType.LEFT_PAREN)) {
                expr = finishCall(expr)
            } else {
                break
            }
        }

        return expr
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
                || next == TokenType.CONST
                || next == TokenType.IF
                || next == TokenType.FOR
                || next == TokenType.LOOP
                || next == TokenType.WHILE
                || next == TokenType.RETURN
                || next == TokenType.BREAK
                || next == TokenType.CONTINUE
                || next == TokenType.DEBUG
            ) {
                return
            }

            advance()
        }

        // TODO: when an error occurs in a for loop initializer, we should skip it entirely
    }
}
