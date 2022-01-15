class DpllSolver {
    sealed class Result {
        class SAT(val model: Map<String, Boolean>): Result() {
            override fun toString(): String {
                return "SAT: $model"
            }
        }
        object UNSAT : Result() {
            override fun toString(): String = "UNSAT"
        }
    }

    fun check(formula: Formula): Result {
        return check(FormulaSet.fromCNF(formula.toCNF()), mutableMapOf())
    }

    private fun check(s: FormulaSet, model: MutableMap<String, Boolean>): Result {
        if (s.isEmpty()) {
            return Result.SAT(model)
        }

        if (s.containsEmptyDsj()) {
            return Result.UNSAT
        }

        var units = s.getUnits()
        while (units.isNotEmpty()) {
            for (l in units) {
                s.unitPropagate(l)
                model.add(l)
            }
            units = s.getUnits()
        }

        val pureLiterals = s.getPureLiterals()
        for (l in pureLiterals) {
            s.eliminatePureLiteral(l)
            model.add(l)
        }

        val l = s.firstLiteralOrNull()
        model.add(l)
        val result = check(s.add(l), model.toMutableMap())
        return if (result is Result.SAT) {
            result
        } else {
            model.add(l?.contr())
            check(s.add(l?.contr()), model.toMutableMap())
        }
    }

    private fun MutableMap<String, Boolean>.add(l: Literal?) {
        if (l == null) {
            return
        }
        this[l.id] = l.pol
    }
}
