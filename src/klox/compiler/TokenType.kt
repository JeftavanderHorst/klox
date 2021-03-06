package klox.compiler

enum class TokenType {
    // Single-character tokens
    LEFT_PAREN, RIGHT_PAREN, LEFT_BRACE, RIGHT_BRACE, COMMA, DOT, SEMICOLON, QUESTION, COLON,

    // Two character tokens
    AND, OR, COALESCE,

    // One or two character tokens
    BANG, BANG_EQUAL, EQUAL, EQUAL_EQUAL, GREATER, GREATER_EQUAL, LESS, LESS_EQUAL,
    PLUS, PLUS_EQUAL, MINUS, MINUS_EQUAL, SLASH, SLASH_EQUAL, STAR, STAR_EQUAL, MODULO, MODULO_EQUAL, COALESCE_EQUAL,

    // Literals
    IDENTIFIER, STRING, NUMBER,

    // Keywords
    CLASS, ELSE, FALSE, FUN, FOR, IF, NIL, RETURN, SUPER, THIS, TRUE, VAR, WHILE, BREAK, CONTINUE, BETWEEN, BETWEEN_AND,
    CONST, LOOP, DEBUG,

    // Type names
    T_NUMBER, T_BOOL, T_STRING,

    EOF
}
