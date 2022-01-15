sealed class Formula {
    data class Var(val id: String) : Formula() {
        override fun toString(): String {
            return super.toString()
        }
    }
    data class Neg(val operand: Formula): Formula() {
        override fun toString(): String {
            return super.toString()
        }
    }
    data class Cnj(val operands: MutableSet<Formula>): Formula() {
        override fun toString(): String {
            return super.toString()
        }
    }
    data class Dsj(val operands: MutableSet<Formula>): Formula() {
        override fun toString(): String {
            return super.toString()
        }
    }

    private class VarFactory() {
        private var varCount = 1
        fun next() = Var("p${varCount++}")
    }

    companion object{
        fun mkVar(id: String): Formula = if (id != "p") Var(id) else throw Exception("переменные p заняты")
        fun not(f: Formula): Formula = if (f is Neg) f.operand else Neg(f)
    }

    infix fun and(f: Formula): Cnj {
        val res = mutableSetOf<Formula>()
        if (this is Cnj) {
            res.addAll(this.operands)
        } else {
            res.add(this)
        }

        if (f is Cnj) {
            res.addAll(f.operands)
        } else {
            res.add(f)
        }
        return Cnj(res)
    }

    infix fun or(f: Formula): Dsj {
        val res = mutableSetOf<Formula>()
        if (this is Dsj) {
            res.addAll(this.operands)
        } else {
            res.add(this)
        }

        if (f is Dsj) {
            res.addAll(f.operands)
        } else {
            res.add(f)
        }
        return Dsj(res)
    }

    fun toCNF(): Cnj {
        val vars = VarFactory()
        val (l, d) = tseytinTransform(this, Cnj(mutableSetOf()), vars)
        return l and d
    }

    private fun tseytinTransform(phi: Formula, d: Cnj, vars: VarFactory): Pair<Formula, Cnj> = when (phi) {
        is Var -> phi to d
        is Neg -> {
            val (l, d1) = tseytinTransform(phi.operand, d, vars)
            not(l) to d1
        }
        is Cnj -> {
            val operandsCopy = phi.operands.toMutableSet()
            val fst = operandsCopy.first()
            operandsCopy.remove(fst)
            val snd = if (operandsCopy.size >= 2) Cnj(operandsCopy) else operandsCopy.first()
            val (l1, d1) = tseytinTransform(fst, d, vars)
            val (l2, d2) = tseytinTransform(snd, d1, vars)
            val p = vars.next()
            val np = not(p)

            val d3 = d2 and (np or l1) and (np or l2) and (not(l1) or not(l2) or p)
            p to d3
        }
        is Dsj -> {
            val operandsCopy = phi.operands.toMutableSet()
            val fst = operandsCopy.first()
            operandsCopy.remove(fst)
            val snd = if (operandsCopy.size >= 2) Dsj(operandsCopy) else operandsCopy.first()
            val (l1, d1) = tseytinTransform(fst, d, vars)
            val (l2, d2) = tseytinTransform(snd, d1, vars)
            val p = vars.next()
            val np = not(p)

            val d3 = d2 and (np or l1 or l2) and (not(l1) or p) and (not(l2) or p)
            p to d3
        }
    }

    override fun toString(): String = when(this) {
        is Var -> this.id
        is Neg -> "¬${this.operand}"
        is Dsj -> "(${this.operands.joinToString(" ∨ ")})"
        is Cnj -> "(${this.operands.joinToString(" ∧ ")})"
    }
}