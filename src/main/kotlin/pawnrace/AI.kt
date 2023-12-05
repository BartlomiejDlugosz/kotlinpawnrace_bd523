package pawnrace

import Matrix

class NeuralNetwork(private val inputNodes: Int, private val hiddenNodes: Int, private val outputNodes: Int) {
  private val inputHiddenWeights = Matrix(inputNodes, hiddenNodes).generateRandomDouble()
  private val hiddenOutputWeights = Matrix(hiddenNodes, outputNodes).generateRandomDouble()

  fun propagate(inputs: List<Int>) {
    val input = Matrix(1, inputs.size)
    inputs.forEachIndexed{index, value ->
      input.matrix[0][index] = value.toDouble()
    }
    val newHidden = Matrix.multiThreadMultiply(input, inputHiddenWeights).map(Matrix::sigmoid)
    val output = Matrix.multiThreadMultiply(newHidden, hiddenOutputWeights)
    val total = output.sum()
    println(output.map {it / total})
    val newOutputs: MutableList<Pair<Double, Int>> = mutableListOf()
    output.matrix.forEach { list ->
      list.forEachIndexed {index2, item ->
        newOutputs.add(item to index2)
      }
    }
    newOutputs.sortByDescending{it.first}
    println(newOutputs)
  }
}

fun flattenInputs(board: Board): List<Int> {
  val b = board.getBoard()
  val inputs = mutableListOf<Int>()
  for (file in File.entries) {
    for (rank in Rank.entries) {
      if (b[file]?.get(rank) == Piece.WHITE) inputs.add(1)
      else if (b[file]?.get(rank) == Piece.BLACK) inputs.add(-1)
      else inputs.add(0)
    }
  }
  return inputs
}

fun main() {
  val player1 = Player(Piece.WHITE)
  val player2 = Player(Piece.BLACK)
  player1.opponent = player2
  player2.opponent = player1

  val nn = NeuralNetwork(64, 100, 28)

  val game = Game(Board(File.A, File.H), player1)
  flattenInputs(game.board)
  nn.propagate(flattenInputs(game.board))
//  println(game)
}