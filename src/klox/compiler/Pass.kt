package klox.compiler

interface Pass {
    fun pass(statements: List<Stmt>): Triple<List<Stmt>, List<KLoxError>, List<Warning>>
}
