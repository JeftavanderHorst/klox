package klox.compiler

class Scanner(
    private val source: String
) {
    private val tokens: MutableList<Token> = ArrayList()
    private val errors: MutableList<ScanError> = ArrayList()
    private var start = 0
    private var current = 0
    private var line = 1
    private val keywords: HashMap<String, TokenType> = hashMapOf(
        "class" to TokenType.CLASS,
        "else" to TokenType.ELSE,
        "false" to TokenType.FALSE,
        "for" to TokenType.FOR,
        "fun" to TokenType.FUN,
        "if" to TokenType.IF,
        "nil" to TokenType.NIL,
        "return" to TokenType.RETURN,
        "super" to TokenType.SUPER,
        "this" to TokenType.THIS,
        "true" to TokenType.TRUE,
        "var" to TokenType.VAR,
        "while" to TokenType.WHILE,
        "between" to TokenType.BETWEEN,
        "and" to TokenType.BETWEEN_AND,
        "break" to TokenType.BREAK,
        "continue" to TokenType.CONTINUE,
        "const" to TokenType.CONST,
        "pure" to TokenType.PURE,
        "debug" to TokenType.DEBUG,
        "loop" to TokenType.LOOP
    )

    fun scanTokens(): Pair<List<Token>, List<ScanError>> {
        while (!isAtEnd()) {
            start = current
            scanToken()
        }

        tokens.add(Token(TokenType.EOF, "", null, line))
        return Pair(tokens, errors)
    }

    private fun scanToken() {
        when (val character = advance()) {
            '(' -> addToken(TokenType.LEFT_PAREN)
            ')' -> addToken(TokenType.RIGHT_PAREN)
            '{' -> addToken(TokenType.LEFT_BRACE)
            '}' -> addToken(TokenType.RIGHT_BRACE)
            ',' -> addToken(TokenType.COMMA)
            '.' -> addToken(TokenType.DOT)
            ';' -> addToken(TokenType.SEMICOLON)
            '+' -> addToken(if (match('=')) TokenType.PLUS_EQUAL else TokenType.PLUS)
            '-' -> addToken(if (match('=')) TokenType.MINUS_EQUAL else TokenType.MINUS)
            '*' -> addToken(if (match('=')) TokenType.STAR_EQUAL else TokenType.STAR)
            '/' -> {
                when {
                    match('/') -> {
                        // We are in a comment, keep consuming characters until we see a newline
                        while (peek() != '\n' && !isAtEnd()) advance()
                    }
                    match('=') -> {
                        addToken(TokenType.SLASH_EQUAL)
                    }
                    else -> {
                        addToken(TokenType.SLASH)
                    }
                }
            }
            '%' -> addToken(if (match('=')) TokenType.MODULO_EQUAL else TokenType.MODULO)
            '&' -> {
                if (match('&')) {
                    addToken(TokenType.AND)
                } else {
                    errors.add(ScanError(line, "Expected second '&'")) // TODO i don't think this advances
                }
            }
            '?' -> {
                if (match('?')) {
                    if (peek() == '=') {
                        advance()
                        addToken(TokenType.COALESCE_EQUAL)
                    } else {
                        addToken(TokenType.COALESCE)
                    }
                } else {
                    addToken(TokenType.QUESTION)
                }
            }
            ':' -> addToken(TokenType.COLON)
            '|' -> {
                if (match('|')) {
                    addToken(TokenType.OR)
                } else {
                    errors.add(ScanError(line, "Expected second '|'"))
                }
            }
            '!' -> addToken(if (match('=')) TokenType.BANG_EQUAL else TokenType.BANG)
            '=' -> addToken(if (match('=')) TokenType.EQUAL_EQUAL else TokenType.EQUAL)
            '<' -> addToken(if (match('=')) TokenType.LESS_EQUAL else TokenType.LESS)
            '>' -> addToken(if (match('=')) TokenType.GREATER_EQUAL else TokenType.GREATER)
            '"' -> stringLiteral()
            in '0'..'9' -> numberLiteral()
            in 'a'..'z', in 'A'..'Z', '_' -> identifier()
            '\n' -> line += 1
            ' ', '\r', '\t' -> {
            }
            else -> {
                errors.add(ScanError(line, "Unexpected character $character"))
            }
        }
    }

    private fun addToken(type: TokenType, literal: Any? = null) {
        val text = source.substring(start, current)
        tokens.add(Token(type, text, literal, line))
    }

    private fun advance(): Char? {
        current += 1
        return source.elementAtOrNull(current - 1)
    }

    private fun peek(): Char? {
        return source.elementAtOrNull(current)
    }

    private fun peekNext(): Char? {
        return source.elementAtOrNull(current + 1)
    }

    private fun match(expected: Char): Boolean {
        val found = source.elementAtOrNull(current)
        if (found == null || found != expected)
            return false

        current++
        return true
    }

    private fun stringLiteral() {
        while (peek() != '"' && !isAtEnd()) {
            if (peek() == '\n')
                line++

            advance()
        }

        if (isAtEnd()) {
            errors.add(ScanError(line, "Unterminated string"))
            return
        }

        // The closing "
        advance()

        addToken(TokenType.STRING, source.substring(start + 1, current - 1))
    }

    private fun numberLiteral() {
        while (peek() in '0'..'9')
            advance()

        if (peek() == '.' && peekNext() in '0'..'9') {
            advance()
            while (peek() in '0'..'9')
                advance()
        }

        addToken(TokenType.NUMBER, source.substring(start, current).toDouble())
    }

    private fun isAlphaNumeric(char: Char): Boolean {
        return char in 'a'..'z'
                || char in 'A'..'Z'
                || char in '0'..'9'
                || char == '_'
    }

    private fun identifier() {
        while (!isAtEnd() && isAlphaNumeric(peek()!!))
            advance()

        val text = source.substring(start, current)
        val type = keywords[text] ?: TokenType.IDENTIFIER
        addToken(type)
    }

    private fun isAtEnd(): Boolean {
        return current >= source.length
    }
}
