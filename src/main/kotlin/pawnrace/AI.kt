package pawnrace

import Matrix

class NeuralNetwork(private val inputNodes: Int, private val hiddenNodes: List<Int>, private val outputNodes: Int) {
  private val inputHiddenWeights: List<Matrix>
  private val hiddenOutputWeights: Matrix

  // Add bias terms
  private val hiddenBiases: List<Matrix>
  private val outputBias: Matrix

  init {
    // Initialize input-hidden weights and biases for each hidden layer
    inputHiddenWeights = mutableListOf()
    hiddenBiases = mutableListOf()

    // Initialize weights and biases for the first hidden layer
    inputHiddenWeights.add(Matrix(inputNodes, hiddenNodes[0]).generateRandomDouble())
    hiddenBiases.add(Matrix(1, hiddenNodes[0]).generateRandomDouble())

    // Initialize weights and biases for subsequent hidden layers
    for (i in 1 until hiddenNodes.size) {
      inputHiddenWeights.add(Matrix(hiddenNodes[i - 1], hiddenNodes[i]).generateRandomDouble())
      hiddenBiases.add(Matrix(1, hiddenNodes[i]).generateRandomDouble())
    }

    // Initialize hidden-output weights and bias
    hiddenOutputWeights = Matrix(hiddenNodes.last(), outputNodes).generateRandomDouble()
    outputBias = Matrix(1, outputNodes).generateRandomDouble()
  }

  fun propagate(inputs: List<Double>): List<Pair<Double, Int>> {
    var currentLayerOutput = inputs

    // Forward pass through hidden layers
    val hiddenLayerOutputs = mutableListOf<List<Double>>()
    for (i in 0 until hiddenNodes.size) {
      val weightedInput = Matrix.multiThreadMultiply(Matrix(1, currentLayerOutput.size, currentLayerOutput), inputHiddenWeights[i])
      val hiddenLayerOutput = Matrix.add(weightedInput, hiddenBiases[i]).map(Matrix::sigmoid).flatten()
      hiddenLayerOutputs.add(hiddenLayerOutput)
      currentLayerOutput = hiddenLayerOutput
    }

    // Forward pass through the output layer
    val output = Matrix.add(Matrix.multiThreadMultiply(Matrix(1, currentLayerOutput.size, currentLayerOutput), hiddenOutputWeights), outputBias)

    // Apply softmax to normalize output
    val total = output.sum()
    val normalizedOutput = output.map { it / total }

    // Convert the output to a list of pairs
    val newOutputs: MutableList<Pair<Double, Int>> = mutableListOf()
    normalizedOutput.flatten().forEachIndexed { index, item ->
      newOutputs.add(item to index)
    }
    newOutputs.sortByDescending { it.first }

    println(newOutputs)
    // Return normalized output probabilities
    return newOutputs
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

  val nn = NeuralNetwork(64, listOf(100, 100, 100), 28)

  val game = Game(Board(File.A, File.H), player1)
  flattenInputs(game.board)
  nn.propagate(flattenInputs(game.board).map{it.toDouble()})
//  println(game)
}