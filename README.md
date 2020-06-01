# KLox

This is the interpreter of KLox, a toy language based on Lox, as described by the book [Crafting Interpreters](http://craftinginterpreters.com/) by Bob Nystrom. KLox is quite different from Lox, but the overall structure of the interpreter is still very similar.

Features include:
- type inference
- `const` variables
- lexical scope

## Operators

`+`, `-`, `/`, `*`, `%`, `+=`, `-=`, `*=`, `/=`, `%=`, `!`, `&&`, `||`, `>`, `<`, `<=`, `>=`, `==`, `!=`, `??`, `??=`, `? :`, `between and`

## Keywords

`for`, `while`, `loop`, `if`, `else`, `break`, `continue`, `return`, `var`, `const`, `fun`, `debug`

## Types

KLox is statically typed and supports `string`, `number`, `bool` and `nil` as primitives. `nil` is currently a separate type, and other types cannot be nil. This will change once I add polymorphism.

Functions are treated as first-class citizens. Types are globally inferred.

## Native functions

- clock()  
  returns the current time in milliseconds since jan 1st, 1970
- sleep(milliseconds)  
  sleep for the specified number of microseconds
- print(message)  
  takes a string and prints it to stdout, appending a newline
- readLine()  
  read a line from stdin and return it as a string
- substr(string, start, end)  
  retrieve part of a string

## var vs. const

There are two keywords to declare variables, `var` and `const`. `const` must be assigned to while declaring while `var` does not, but both must be assigned to before they can be read. After declaration, variables declared with `const` can never be assigned to again. Neither `var` or `const` can ever be redeclared in the same scope. 
