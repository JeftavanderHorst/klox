package klox.compiler

import java.lang.Exception

enum class Nil { Nil }

class Parser(
    private val tokens: List<Token>
) {
    private var errors: MutableList<ParseError> = ArrayList()
    private var current = 0

    fun parse(): Pair<List<Expression>, List<ParseError>> {
        return try {
            Pair(listOf(expression()), errors)
        } catch (error: Exception) {
            Pair(ArrayList(), errors)
        }
    }

    private fun expression(): Expression {
        return equality()
    }

    private fun equality(): Expression {
        var expression = comparison()

        while (match(TokenType.BANG_EQUAL, TokenType.EQUAL_EQUAL)) {
            expression = Expression.Binary(expression, previous(), comparison())
        }

        return expression
    }

    private fun comparison(): Expression {
        var expression = addition()

        while (match(TokenType.GREATER, TokenType.GREATER_EQUAL, TokenType.LESS, TokenType.LESS_EQUAL)) {
            expression = Expression.Binary(expression, previous(), addition())
        }

        return expression
    }

    private fun addition(): Expression {
        var expression = multiplication()

        while (match(TokenType.MINUS, TokenType.PLUS)) {
            expression = Expression.Binary(expression, previous(), multiplication())
        }

        return expression
    }

    private fun multiplication(): Expression {
        var expression = unary()

        while (match(TokenType.SLASH, TokenType.STAR)) {
            expression = Expression.Binary(expression, previous(), unary())
        }

        return expression
    }

    private fun unary(): Expression {
        if (match(TokenType.BANG, TokenType.MINUS))
            return Expression.Unary(previous(), unary())
        return primary()
    }

    private fun primary(): Expression {
        if (match(TokenType.FALSE)) return Expression.Literal(false)
        if (match(TokenType.TRUE)) return Expression.Literal(true)
        if (match(TokenType.NIL)) return Expression.Literal(Nil.Nil)
        if (match(TokenType.NUMBER, TokenType.STRING)) return Expression.Literal(previous().literal!!) // TODO
        if (match(TokenType.LEFT_PAREN)) {
            val expression = expression()
            consume(TokenType.RIGHT_PAREN, UnclosedParenthesesError(current))
            return Expression.Grouping(expression)
        }

        errors.add(NoExpressionProvided(current))
        throw Exception()
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

    private fun consume(type: TokenType, error: ParseError): Token? {
        if (check(type)) return advance()
        errors.add(error)
        throw Exception()
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
}
