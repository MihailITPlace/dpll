data class Literal(val id: String, val pol: Boolean) {
    fun contr() = Literal(id, !pol)
}

class FormulaSet private constructor (private val s: MutableSet<MutableSet<Literal>>) {
    companion object {
        fun fromCNF(f: Formula.Cnj): FormulaSet {
            val s: MutableSet<MutableSet<Literal>> = f.operands.fold(mutableSetOf()) { acc, d ->
                when (d) {
                    is Formula.Var -> acc.add(mutableSetOf(d.toLiteral()))
                    is Formula.Neg -> acc.add(mutableSetOf(d.toLiteral()))
                    is Formula.Dsj -> acc.add(d.toSet())
                    else -> throw notCnfException
                }
                acc
            }
            return FormulaSet(s)
        }

        private val notCnfException = RuntimeException("Это не КНФ")

        private fun Formula.Dsj.toSet(): MutableSet<Literal> {
            return operands.map {
                when (it) {
                    is Formula.Var -> it.toLiteral()
                    is Formula.Neg -> it.toLiteral()
                    else -> throw notCnfException
                }
            }.toMutableSet()
        }
        private fun Formula.Var.toLiteral() = Literal(this.id, true)
        private fun Formula.Neg.toLiteral() = Literal((this.operand as Formula.Var).id, false)
    }

    fun isEmpty() = s.isEmpty()

    fun containsEmptyDsj() = s.any { it.isEmpty() }

    fun unitPropagate(l: Literal) {
        s.removeIf { it.size == 1 && it.contains(l) }
        s.forEach { it.remove(l.contr()) }
    }

    fun eliminatePureLiteral(l: Literal) {
        s.removeIf { it.contains(l) }
    }

    private fun getLiterals(): Set<Literal> = s.fold(mutableSetOf()) { acc, it ->
        acc.addAll(it)
        acc
    }

    fun firstLiteralOrNull(): Literal? {
        for (d in s) {
            if (d.isNotEmpty()) {
                return d.first()
            }
        }
        return null
    }

    fun getUnits(): Set<Literal> = s.filter { it.size == 1 }.fold(mutableSetOf()) { acc, it ->
        acc.addAll(it)
        acc
    }

    fun getPureLiterals(): Set<Literal> {
        val literals = getLiterals()
        return literals.fold(mutableSetOf()) { acc, l ->
            if (!literals.contains(l.contr())) acc.add(l)
            acc
        }
    }

    fun add(l: Literal?): FormulaSet {
        val s1: MutableSet<MutableSet<Literal>> = s.fold(mutableSetOf()) { acc, it ->
            acc.add(it.toMutableSet())
            acc
        }
        if (l != null)
            s1.add(mutableSetOf(l))
        return FormulaSet(s1)
    }
}