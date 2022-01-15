import Formula.Companion.mkVar
import Formula.Companion.not

fun main() {
    val x1 = mkVar("x1")
    val x2 = mkVar("x2")
    val x3 = mkVar("x3")
    val x4 = mkVar("x4")
    val x5 = mkVar("x5")

    val f = x1 and (not(x1) or not(x2) or x3) and (x2 or not(x4)) and x4 and (not(x5) or not(x3)) and x5

    println(f)
    println(f.toCNF())
    println(DpllSolver().check(f))
}