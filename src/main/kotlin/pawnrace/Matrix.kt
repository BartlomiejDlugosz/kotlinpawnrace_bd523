import kotlin.concurrent.thread
import kotlin.math.E
import kotlin.math.min
import kotlin.math.pow
import kotlin.random.Random
import kotlin.system.measureTimeMillis

class Matrix(val n: Int, val m: Int = n) {
    var matrix: List<MutableList<Double>> = List(n) { MutableList(m) { 0.0 } }

    override fun toString(): String {
        var str = ""
        for (row in matrix) {
            for (item in row) {
                str += "$item "
            }
            str += "\n"
        }
        return str
    }

    override fun equals(other: Any?): Boolean {
        return (other is Matrix) && this.matrix == other.matrix
    }

    fun generateRandomDouble(lowerBound: Double = 0.0, upperBound: Double = 1.0): Matrix {
        matrix = List(n) { MutableList(m) { Random.nextDouble(lowerBound, upperBound) } }
        return this
    }

    fun generateRandomInt(lowerBound: Int = 0, upperBound: Int = 10): Matrix {
        matrix = List(n) { MutableList(m) { Random.nextInt(lowerBound, upperBound).toDouble() } }
        return this
    }

    fun transpose(): Matrix {
        val result = Matrix(this.m, this.n)
        for (r in result.matrix.indices) {
            for (c in result.matrix[r].indices) {
                result.matrix[r][c] = this.matrix[c][r]
            }
        }
        return result
    }

    fun applySigmoid(): Matrix {
        this.map(::sigmoid)
        return this
    }

    fun sum(): Double {
        var total: Double = 0.0
        for (r in this.matrix.indices) {
            for (c in this.matrix[r].indices) {
                total += this.matrix[r][c]
            }
        }
        return total
    }

    fun map(f: (Double) -> Double): Matrix {
        for (r in this.matrix.indices) {
            for (c in this.matrix[r].indices) {
                this.matrix[r][c] = f(this.matrix[r][c])
            }
        }

        return this
    }

    override fun hashCode(): Int {
        var result = n
        result = 31 * result + m
        result = 31 * result + matrix.hashCode()
        return result
    }

    companion object {
        fun sigmoid(x: Double): Double = 1 / (1 + E.pow(-x))

        fun multiply(m1: Matrix, m2: Matrix): Matrix {
            if (m1.m != m2.n) return error("Matrix dimensions wrong size")

            val result = Matrix(m1.n, m2.m)

            for (r in m1.matrix.indices) {
                for (c in m2.matrix[r].indices) {
                    for (i in m1.matrix[r].indices) {
                        result.matrix[r][c] += m1.matrix[r][i] * m2.matrix[i][c]
                    }
                }
            }

            return result
        }

        fun multiply(num: Int, m1: Matrix): Matrix {
            val result = Matrix(m1.n, m1.m)
            for (r in m1.matrix.indices) {
                for (c in m1.matrix[r].indices) {
                    result.matrix[r][c] = num * m1.matrix[r][c]
                }
            }
            return result
        }

        fun multiThreadMultiply(m1: Matrix, m2: Matrix): Matrix {
            if (m1.m != m2.n) return error("Matrix dimensions wrong size")

            val result = Matrix(m1.n, m2.m)

            val numberOfThreads = Runtime.getRuntime().availableProcessors()
//            val numberOfThreads = 8
            val numberOfRowsPerThread = (m1.matrix.size + numberOfThreads - 1) / numberOfThreads

            val threads = mutableListOf<Thread>()

            for (r in 0..<numberOfThreads) {

                val startRow = r * numberOfRowsPerThread
                val endRow = min((r + 1) * numberOfRowsPerThread, m1.matrix.size)

                val t = thread() {
                    for (j in startRow..<endRow) {
                        for (c in m2.matrix[j].indices) {
                            for (i in m1.matrix[j].indices) {
                                result.matrix[j][c] += m1.matrix[j][i] * m2.matrix[i][c]
                            }
                        }
                    }
                }
                threads.add(t)
            }
            threads.forEach { it.join() }
            return result
        }

        fun add(m1: Matrix, m2: Matrix): Matrix {
            if (m1.n != m2.n || m1.m != m2.m) error("Matrix dimensions wrong size")

            val result: Matrix = Matrix(m1.n, m1.m)
            for (r in m1.matrix.indices) {
                for (c in m1.matrix[r].indices) {
                    result.matrix[r][c] = m1.matrix[r][c] + m2.matrix[r][c]
                }
            }
            return result
        }

        fun subtract(m1: Matrix, m2: Matrix): Matrix {
            val result: Matrix = Matrix(m1.n, m1.m)
            for (r in m1.matrix.indices) {
                for (c in m1.matrix[r].indices) {
                    result.matrix[r][c] = m1.matrix[r][c] - m2.matrix[r][c]
                }
            }
            return result
        }
    }
}

fun getAverageTime(m1: Matrix, m2: Matrix, f: (Matrix, Matrix) -> Matrix, numberOfRepetitions: Int = 50): Long {
    var sum: Long = 0
    for (i in 1..numberOfRepetitions) {
        sum += measureTimeMillis { f(m1, m2) }
    }
    return sum / numberOfRepetitions
}


fun main() {
    val matrixSize: Int = 2000
    val m1 = Matrix(matrixSize, matrixSize).generateRandomDouble()
    val m2 = Matrix(matrixSize, matrixSize).generateRandomDouble()
//    println(m1)
//    println(m2)

    println("Starting benchmark")

    println("Using ${Runtime.getRuntime().availableProcessors()} threads")

    println(getAverageTime(m1, m2, Matrix.Companion::multiThreadMultiply, 2))
//    println(getAverageTime(m1, m2, Matrix.Companion::multiply, 2))


//    println(Matrix.multiply(m1, m2) == Matrix.multiThreadMultiply(m1, m2))
//    println(Matrix.multiThreadMultiply(m1, m2).matrix == Matrix.multiply(m1, m2).matrix)
}