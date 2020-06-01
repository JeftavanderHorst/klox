package klox.compiler

import klox.compiler.std.Std
import java.util.Stack
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

/* TODO:
    - weird output in 'type assignments' with program `fun a(b) { return 2; } a();`
 */

class Typer : Pass {
    class TypeEquation(val left: Type, val right: Type, private val originNode: Any) {
        override fun toString() = when (originNode) {
            is Expr -> "$left = $right ".padEnd(22) + AstPrinter().makeString(originNode)
            is Stmt -> "$left = $right ".padEnd(22) + AstPrinter().makeString(originNode)
            else -> "$left = $right"
        }
    }

    private val equations: MutableList<TypeEquation> = ArrayList()
    private val returnTypes = Stack<Type>()
    private val symbolTable: MutableMap<String, Type> = HashMap() // todo use int
    private val subst: MutableMap<String, Type> = HashMap()
    private val typeErrors: MutableList<TypeError> = ArrayList()

    override fun pass(statements: List<Stmt>): Triple<List<Stmt>, List<TypeError>, List<Warning>> {
        /*
         * Type inference consists of three sub-passes:
         * 1. Type assignments, in which every expression and declaration gets a type variable
         * 2. Equation generation, in which equations between the type variables are deduced
         * 3. Unification, which attempts to reduce each type variable into an actual type
         *
         * References:
         * - https://github.com/eliben/code-for-blog/blob/master/2018/type-inference/typing.py
         * - https://medium.com/@joakim.ahnfelt/type-inference-by-example-part-5-25d7a7c66ca6
         */

        val std = Std().getNativeFunctions()
        std.forEach { (index: Int, name: String, fn: LoxCallable) ->
            symbolTable["${name}_$index"] = fn.type()
        }

        val typedStatements = statements.map { assignTypenames(it) }

        println("Type Assignments:")
        typedStatements.forEach { s -> showTypeAssignments(s) }
        println()

        for (stmt in typedStatements) {
            generateEquations(stmt)
        }

        println("Type Equations:")
        equations.forEach { equation -> println(equation) }
        println()

        equations.forEach { unify(it.left, it.right) }

        println("Symbol table:")
        for ((name, type) in symbolTable) {
            println("${name.padEnd(10)} = ${applyUnifier(type)}")
        }
        println()

        return Triple(typedStatements, typeErrors, emptyList())
    }

    private var typeCounter = 0

    private fun getFreshTypename(): String {
        val name = "t${typeCounter}"
        typeCounter++
        return name
    }

    private fun assignTypenames(stmt: Stmt): Stmt = when (stmt) {
        is Stmt.Var -> {
            val type = Type.TypeVar(getFreshTypename())
            symbolTable["${stmt.name.lexeme}_${stmt.index}"] = type

            val initializer = if (stmt.initializer != null) {
                assignTypenames(stmt.initializer)
            } else {
                stmt.initializer
            }

            Stmt.Var(stmt.name, stmt.index, initializer, type)
        }
        is Stmt.Const -> {
            val type = Type.TypeVar(getFreshTypename())
            symbolTable["${stmt.name.lexeme}_${stmt.index}"] = type
            val initializer = assignTypenames(stmt.initializer)
            Stmt.Const(stmt.name, stmt.index, initializer, type)
        }
        is Stmt.Function -> {
            // Add overall function type to symbolTable
            val type = Type.TypeVar(getFreshTypename())
            symbolTable["${stmt.name.lexeme}_${stmt.index}"] = type

            // Add parameter types to symbolTable
            val paramTypes: LinkedHashMap<String, Type> = LinkedHashMap()
            var paramIndex = stmt.index!! + 1 // TODO store parameter index in AST in Resolver
            for (param in stmt.params) {
                val paramType = Type.TypeVar(getFreshTypename())
                symbolTable["${param.lexeme}_$paramIndex"] = paramType
                paramTypes["${param.lexeme}_$paramIndex"] = paramType
                paramIndex++
            }

            // Assign typenames to all statements in body
            val typedBody = stmt.body.map { assignTypenames(it) }

            Stmt.Function(stmt.name, stmt.index, stmt.params, typedBody, paramTypes, type)
        }
        is Stmt.Return -> {
            val value = assignTypenames(stmt.value)
            Stmt.Return(stmt.keyword, value)
        }
        is Stmt.If -> Stmt.If(
            assignTypenames(stmt.condition),
            assignTypenames(stmt.thenBranch),
            assignTypenames(stmt.elseBranch)
        )
        is Stmt.While -> Stmt.While(
            assignTypenames(stmt.condition),
            assignTypenames(stmt.body)
        )
        is Stmt.Expression -> Stmt.Expression(assignTypenames(stmt.expression))
        is Stmt.Block -> Stmt.Block(stmt.statements.map { assignTypenames(it) })
        is Stmt.Empty, is Stmt.Break, is Stmt.Continue, is Stmt.Debug -> stmt
    }

    private fun assignTypenames(expr: Expr): Expr = when (expr) {
        is Expr.Literal -> {
            val type = when (expr.value) {
                is Double -> Type.NumberType()
                is String -> Type.StringType()
                is Boolean -> Type.BoolType()
                is Nil -> Type.NilType()
                else -> throw Exception("Unexpected literal of type ${expr.value}")
            }
            Expr.Literal(expr.value, type)
        }
        is Expr.Variable -> {
            val key = "${expr.name.lexeme}_${expr.index}"
            if (symbolTable.containsKey(key)) {
                Expr.Variable(expr.name, expr.distance, expr.index, symbolTable[key])
            } else {
                // todo can this ever happen?
                throw Exception("Variable $key not found in symbolTable: $symbolTable")
            }
        }
        is Expr.Call -> {
            val type = Type.TypeVar(getFreshTypename())
            val callee = assignTypenames(expr.callee)
            val arguments = expr.arguments.map { e -> assignTypenames(e) }
            Expr.Call(callee, expr.paren, arguments, type)
        }
        is Expr.Assign -> {
            val type = Type.TypeVar(getFreshTypename())
            val value = assignTypenames(expr.value)
            Expr.Assign(expr.name, value, expr.distance, expr.index, type)
        }
        is Expr.Grouping -> {
            val type = Type.TypeVar(getFreshTypename())
            Expr.Grouping(assignTypenames(expr.expr), type)
        }
        is Expr.Unary -> {
            val type = Type.TypeVar(getFreshTypename())
            Expr.Unary(expr.operator, assignTypenames(expr.right), type)
        }
        is Expr.Binary -> {
            val type = Type.TypeVar(getFreshTypename())
            val left = assignTypenames(expr.left)
            val right = assignTypenames(expr.right)
            Expr.Binary(left, expr.operator, right, type)
        }
        is Expr.Ternary -> {
            val type = Type.TypeVar(getFreshTypename())
            val left = assignTypenames(expr.left)
            val middle = assignTypenames(expr.middle)
            val right = assignTypenames(expr.right)
            Expr.Ternary(left, expr.operator1, middle, expr.operator2, right, type)
        }
        is Expr.Logical -> {
            val type = Type.TypeVar(getFreshTypename())
            val left = assignTypenames(expr.left)
            val right = assignTypenames(expr.right)
            Expr.Binary(left, expr.operator, right, type)
        }
        is Expr.Empty -> Expr.Empty(Type.TypeVar(getFreshTypename()))
    }

    private fun equate(left: Type, right: Type, origin: Any) {
        equations.add(TypeEquation(left, right, origin))
    }

    private fun generateEquations(stmt: Stmt) {
        when (stmt) {
            is Stmt.Var -> {
                if (stmt.initializer != null) {
                    equate(stmt.type!!, stmt.initializer.type!!, stmt)
                    generateEquations(stmt.initializer)
                }
            }
            is Stmt.Const -> generateEquations(stmt.initializer)
            is Stmt.Function -> {
                // We need to register the function signature before
                // entering the body, to allow for recursion. TODO is this still relevant??

                val paramTypes = stmt.paramTypes!!.values.toList()
                returnTypes.push(Type.TypeVar(getFreshTypename())) // todo <-- what is this???
                stmt.body.forEach { s -> generateEquations(s) }
                val returnType = returnTypes.pop()
                equate(stmt.type!!, Type.FunType(paramTypes, returnType), stmt)
            }
            is Stmt.Return -> {
                equate(stmt.value.type!!, returnTypes.peek(), stmt)
                generateEquations(stmt.value)
            }
            is Stmt.If -> {
                equate(stmt.condition.type!!, Type.BoolType(), stmt)
                generateEquations(stmt.condition)
                generateEquations(stmt.thenBranch)
                generateEquations(stmt.elseBranch)
            }
            is Stmt.While -> {
                equate(stmt.condition.type!!, Type.BoolType(), stmt)
                generateEquations(stmt.condition)
                generateEquations(stmt.body)
            }
            is Stmt.Expression -> generateEquations(stmt.expression)
            is Stmt.Block -> stmt.statements.forEach { s -> generateEquations(s) }
            is Stmt.Empty, is Stmt.Break, is Stmt.Continue, is Stmt.Debug -> {
            }
        }
    }

    private fun generateEquations(expr: Expr) {
        when (expr) {
            is Expr.Literal, is Expr.Empty, is Expr.Variable -> {
            }
            is Expr.Call -> {
                if (expr.callee !is Expr.Variable) {
                    throw Exception("call to wrong expr type")
                }

                // This would never generate any equations, but may do so in the future
                // generateEquations(expr.callee)

                equate(
                    symbolTable["${expr.callee.name.lexeme}_${expr.callee.index}"]!!,
                    Type.FunType(expr.arguments.map { it.type!! }, expr.type!!),
                    expr
                )

                expr.arguments.forEach { generateEquations(it) }
            }
            is Expr.Assign -> {
                equate(expr.value.type!!, expr.type!!, expr)
                equate(expr.value.type!!, symbolTable["${expr.name.lexeme}_${expr.index}"]!!, expr)
                generateEquations(expr.value)
            }
            is Expr.Grouping -> {
                equate(expr.type!!, expr.expr.type!!, expr)
                generateEquations(expr.expr)
            }
            is Expr.Unary -> {
                when (expr.operator.type) {
                    TokenType.BANG -> {
                        equate(expr.type!!, Type.BoolType(), expr)
                        equate(expr.right.type!!, Type.BoolType(), expr)
                    }
                    TokenType.MINUS -> {
                        equate(expr.type!!, Type.NumberType(), expr)
                        equate(expr.right.type!!, Type.NumberType(), expr)
                    }
                    else -> throw Exception("Found unknown unary operator ${expr.operator.type} during type inference")
                }

                generateEquations(expr.right)
            }
            is Expr.Binary -> {
                when (expr.operator.type) {
                    TokenType.EQUAL_EQUAL, TokenType.BANG_EQUAL -> {
                        equate(expr.type!!, Type.BoolType(), expr)
                        equate(expr.left.type!!, expr.right.type!!, expr)
                    }
                    TokenType.GREATER, TokenType.LESS, TokenType.GREATER_EQUAL, TokenType.LESS_EQUAL -> {
                        equate(expr.type!!, Type.BoolType(), expr)
                        equate(expr.left.type!!, Type.NumberType(), expr)
                        equate(expr.right.type!!, Type.NumberType(), expr)
                    }
                    TokenType.AND, TokenType.OR -> {
                        equate(expr.type!!, Type.BoolType(), expr)
                        equate(expr.left.type!!, Type.BoolType(), expr)
                        equate(expr.right.type!!, Type.BoolType(), expr)
                    }
                    TokenType.PLUS, TokenType.MINUS, TokenType.STAR, TokenType.SLASH, TokenType.MODULO -> {
                        equate(expr.type!!, Type.NumberType(), expr)
                        equate(expr.left.type!!, Type.NumberType(), expr)
                        equate(expr.right.type!!, Type.NumberType(), expr)
                    }
                    else -> throw Exception("Found unknown binary operator ${expr.operator.type} during type inference")
                }

                generateEquations(expr.left)
                generateEquations(expr.right)
            }
            is Expr.Ternary -> {
                if (expr.operator1.type == TokenType.BETWEEN && expr.operator2.type == TokenType.BETWEEN_AND) {
                    equate(expr.type!!, Type.BoolType(), expr)
                    equate(expr.left.type!!, Type.NumberType(), expr)
                    equate(expr.middle.type!!, Type.NumberType(), expr)
                    equate(expr.right.type!!, Type.NumberType(), expr)
                } else if (expr.operator1.type == TokenType.QUESTION && expr.operator2.type == TokenType.COLON) {
                    equate(expr.type!!, Type.BoolType(), expr)
                    equate(expr.left.type!!, Type.BoolType(), expr)
                    equate(expr.middle.type!!, expr.right.type!!, expr)
                } else {
                    throw Exception(
                        "Found unknown logical operator ${expr.operator1.type} ${expr.operator2.type} during type inference"
                    )
                }

                generateEquations(expr.left)
                generateEquations(expr.middle)
                generateEquations(expr.right)
            }
            is Expr.Logical -> {
                if (expr.operator.type != TokenType.AND && expr.operator.type != TokenType.OR) {
                    throw Exception("Found unknown logical operator ${expr.operator.type} during type inference")
                }

                equate(expr.type!!, Type.BoolType(), expr)
                equate(expr.left.type!!, Type.BoolType(), expr)
                equate(expr.right.type!!, Type.BoolType(), expr)

                generateEquations(expr.left)
                generateEquations(expr.right)
            }
        }
    }

    private fun showTypeAssignments(stmt: Stmt) {
        when (stmt) {
            is Stmt.Return -> showTypeAssignments(stmt.value)
            is Stmt.Var -> {
                println("${stmt.type!!.toString().padEnd(4)} = ${AstPrinter().makeString(stmt)}")
                if (stmt.initializer != null) showTypeAssignments(stmt.initializer)
            }
            is Stmt.Const -> {
                println("${stmt.type!!.toString().padEnd(4)} = ${AstPrinter().makeString(stmt)}")
                showTypeAssignments(stmt.initializer)
            }
            is Stmt.While -> {
                showTypeAssignments(stmt.condition)
                showTypeAssignments(stmt.body)
            }
            is Stmt.Function -> {
                println("${stmt.type.toString().padEnd(4)} = ${AstPrinter().makeString(stmt)}")
                println("       ${stmt.paramTypes!!}")
                stmt.body.forEach { s -> showTypeAssignments(s) }
            }
            is Stmt.Expression -> showTypeAssignments(stmt.expression)
            is Stmt.Block -> stmt.statements.forEach { s -> showTypeAssignments(s) }
            is Stmt.If -> {
                showTypeAssignments(stmt.condition)
                showTypeAssignments(stmt.thenBranch)
                showTypeAssignments(stmt.elseBranch)
            }
            is Stmt.Empty, is Stmt.Break, is Stmt.Continue, is Stmt.Debug -> {
            }
        }
    }

    private fun showTypeAssignments(expr: Expr) {
        println("${expr.type!!.toString().padEnd(4)} = ${AstPrinter().makeString(expr)}")
        when (expr) {
            is Expr.Call -> showTypeAssignments(expr.callee)
            is Expr.Ternary -> {
                showTypeAssignments(expr.left)
                showTypeAssignments(expr.middle)
                showTypeAssignments(expr.right)
            }
            is Expr.Logical -> {
                showTypeAssignments(expr.left)
                showTypeAssignments(expr.right)
            }
            is Expr.Assign -> showTypeAssignments(expr.value)
            is Expr.Grouping -> showTypeAssignments(expr.expr)
            is Expr.Binary -> {
                showTypeAssignments(expr.left)
                showTypeAssignments(expr.right)
            }
            is Expr.Unary -> showTypeAssignments(expr.right)
            is Expr.Empty, is Expr.Variable, is Expr.Literal -> {
            }
        }
    }

    private fun unify(t1: Type, t2: Type) {
        if (t1 == t2) {
            return
        }

        when {
            t1 is Type.TypeVar -> unifyVariable(t1, t2)
            t2 is Type.TypeVar -> unifyVariable(t2, t1)
            t1 is Type.FunType && t2 is Type.FunType -> {
                if (t1.argTypes.size != t2.argTypes.size) {
                    // todo line number
                    typeErrors.add(TypeError(0, "Different number of parameters ($t1 vs. $t2)"))
                    return
                }

                unify(t1.returnType, t2.returnType)
                for (i in t1.argTypes.indices) {
                    unify(t1.argTypes[i], t2.argTypes[i])
                }
            }
            else -> {
                // todo line number
                typeErrors.add(TypeError(0, "Cannot unify $t1 and $t2"))
            }
        }
    }

    private fun unifyVariable(v: Type.TypeVar, typ: Type) {
        if (subst.containsKey(v.toString())) {
            unify(subst[v.toString()]!!, typ)
        } else if (typ is Type.TypeVar && subst.containsKey(typ.toString())) {
            unify(v, subst[typ.toString()]!!)
        } else if (occursCheck(v, typ)) {
            throw Exception("occurscheck??") // todo
        } else {
            subst[v.toString()] = typ
        }
    }

    /** Checks whether v occurs anywhere inside typ */
    private fun occursCheck(v: Type.TypeVar, typ: Type): Boolean {
        return if (v == typ) {
            true
        } else if (typ is Type.TypeVar && typ.toString() in subst) {
            occursCheck(v, subst[typ.toString()]!!)
        } else if (typ is Type.FunType) {
            occursCheck(v, typ.returnType) || typ.argTypes.any { occursCheck(v, it) }
        } else {
            false
        }
    }

    private fun applyUnifier(typ: Type): Type {
        if (subst.isEmpty()) {
            return typ
        }

        return when (typ) {
            is Type.TypeVar -> {
                if (typ.toString() in subst) {
                    applyUnifier(subst[typ.toString()]!!)
                } else {
                    typ
                }
            }
            is Type.FunType -> {
                Type.FunType(typ.argTypes.map { applyUnifier(it) }, applyUnifier(typ.returnType))
            }
            else -> typ // Constant type
        }
    }
}
