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
        "Expression",
        hashMapOf(
            "Binary" to listOf("left: Expression", "operator: Token", "right: Expression"),
            "Grouping" to listOf("expression: Expression"),
            "Literal" to listOf("value: Any"),
            "Unary" to listOf("operator: Token", "right: Expression")
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
