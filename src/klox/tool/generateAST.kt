package klox.tool

import java.io.PrintWriter
import kotlin.system.exitProcess

fun main(args: Array<String>) {
    if (args.size != 1) {
        println("Usage: generate_ast <output directory>")
        exitProcess(65)
    }

    val outputDirectory = args[0]

    defineAst(
        outputDirectory,
        "Expr",
        hashMapOf(
            "Assign" to listOf("name: Token", "value: Expr"),
            "Binary" to listOf("left: Expr", "operator: Token", "right: Expr"),
            "Ternary" to listOf("left: Expr", "operator1: Token", "middle: Expr", "operator2: Token", "right: Expr"),
            "Call" to listOf("callee: Expr", "paren: Token", "arguments: List<Expr>"),
            "Grouping" to listOf("expr: Expr"),
            "Literal" to listOf("value: Any"),
            "Logical" to listOf("left: Expr", "operator: Token", "right: Expr"),
            "Unary" to listOf("operator: Token", "right: Expr"),
            "Variable" to listOf("name: Token")
        )
    )

    defineAst(
        outputDirectory,
        "Stmt",
        hashMapOf(
            "Block" to listOf("statements: List<Stmt>"),
            "Expression" to listOf("expression: Expr"),
            "If" to listOf("condition: Expr", "thenBranch: Stmt", "elseBranch: Stmt"),
            "Break" to emptyList(),
            "Continue" to emptyList(),
            "While" to listOf("condition: Expr", "body: Stmt"),
            "Var" to listOf("name: Token", "initializer: Expr?"),
            "Function" to listOf("name: Token", "params: List<Token>", "body: List<Stmt>"),
            "Return" to listOf("keyword: Token", "value: Expr?"),
            "Empty" to emptyList()
        )
    )
}

fun defineAst(outputDir: String, baseName: String, types: HashMap<String, List<String>>) {
    val writer = PrintWriter("$outputDir/$baseName.kt", "UTF-8")
    writer.println("package klox.compiler")
    writer.println()
    writer.println("/* Auto-generated using tool/generateAST.kt */")
    writer.println()
    writer.println("abstract class $baseName {")

    defineVisitor(writer, baseName, types)
    writer.println("abstract fun <R> accept(visitor: Visitor<R>): R")
    writer.println()

    defineTypes(writer, baseName, types)

    writer.println("}")

    writer.close()
}

fun defineVisitor(writer: PrintWriter, baseName: String, types: HashMap<String, List<String>>) {
    writer.println("interface Visitor<R> {")

    for ((type, _) in types) {
        writer.println("fun visit$type$baseName(${baseName.toLowerCase()}: $type): R")
    }

    writer.println("}")
}

fun defineTypes(writer: PrintWriter, baseName: String, types: HashMap<String, List<String>>) {
    for ((type, fields) in types) {
        writer.println("class $type(")
        writer.println(fields.joinToString { field -> "val $field" })
        writer.println("): $baseName() {")
        writer.println("override fun <R> accept(visitor: Visitor<R>): R {")
        writer.println("return visitor.visit$type$baseName(this)")
        writer.println("}")
        writer.println("}")
    }
}
