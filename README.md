# KLox

This is the interpreter of KLox, a variant of the Lox language as described by the book [Crafting Interpreters](http://craftinginterpreters.com/) by Bob Nystrom. I did not follow Lox's spec completely and made several changes to the language, but most Lox programs should also work in KLox.

This interpreter is written in Kotlin and incomplete at this time.

## Operators

`+`, `-`, `/`, `*`, `%`, `+=`, `-=`, `*=`, `/=`, `%=`, `!`, `&&`, `||`, `>`, `<`, `<=`, `>=`, `==`, `!=`, `??`, `??=`, `? :`, `between and`

## Keywords
`for`, `while`, `loop`, `if`, `else`, `break`, `continue`, `return`, `var`, `const`, `pure`, `fun`, `debug`

## Types

KLox is softly typed and supports the following types:
- string
- double
- boolean
- function
- nil

## Native functions

- clock()  
  returns the current time in milliseconds since jan 1st, 1970
- sleep(milliseconds)  
  sleep for the specified number of microseconds
- print(message)  
  takes any value and prints it to stdout, appending a newline
- readLine()  
  read a line from stdin and return it as a string
- substr(string, start, end)  
  retrieve part of a string

## var vs. const

There are two keywords to declare variables, `var` and `const`. `const` must be assigned to while declaring while `var` does not, but both must be assigned to before they can be read. After declaration, variables declared with `const` can never be assigned to again. Neither `var` or `const` can ever be redeclared in the same scope. 

## Pure functions

Functions can be declared pure by using the keyword `pure` in front of their definition, like this:
```
pure fun add(a, b) {
    return a + b;
}
```

Pure functions must be deterministic. They can not read or write to any variables outside their own scope, but they can call other pure functions, and read values from consts.

The interpreter will try to statically validate validate the purities in a program, but since KLox is a dynamic language this is not always possible. Therefore purity is also checked at runtime.
