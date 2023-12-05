package pawnrace

import java.util.Stack

class Player(val piece: Piece, var opponent: Player? = null) {
    fun getAllPawns(): List<Position> = TODO()

    fun getAllValidMoves(): List<Move> = TODO()

    fun isPassedPawn(pos: Position): Boolean = TODO()

    fun makeMove(game: Game): Move? = TODO()

    override fun toString(): String = piece.toString()
}

class Game (val board: Board, var player: Player, val moves: Stack<Move> = Stack()) {
    fun applyMove(move: Move): Game {
        if (moves.size > 0) println(moves.peek())
        if (move.piece == player.piece && board.isValidMove(move, if (moves.size > 0) moves.peek() else null)) {
            board.move(move)
            player = player.opponent!!
            moves.push(move)
        }
        println(this)
        return this
    }

    fun unapplyMove(): Game {
        if (moves.size == 0) return this
        val move = moves.pop()
        if (move.type == MoveType.PEACEFUL) board.move(Move(move.piece, move.to, move.from, move.type))
        else {
            board.move(Move(move.piece, move.to, move.from, move.type))
            board.add(move.to, move.piece.getOpposite())
        }
        return this
    }

    fun moves(piece: Piece): List<Move> {
        val movesList = mutableListOf<Move>()
        board.getBoard().forEach{ (file, ranks) ->
            ranks.forEach { (rank, currentPiece) ->
                if (piece == currentPiece) {
                    val possibleMoves = board.validMoves(Position("$file$rank"), if (moves.size > 0) moves.peek() else null)
                    possibleMoves.forEach{ (position, moveType) ->
                        movesList.add(Move(piece, Position("$file$rank"), position, moveType))
                    }
                }
            }
        }

        return movesList
    }

    private fun moveForwardBy(pos: Position, step: Int, piece: Piece): Move? = TODO()

    private fun moveDiagonalBy(pos: Position, isLeft: Boolean, piece: Piece, type: MoveType): Move? = TODO()

    fun over(): Boolean {
        if (moves(player.piece).size == 0) return true
        TODO()
    }

    fun winner(): Player? = TODO()

    fun parseMove(san: String): Move? {
        val possibleMoves = moves(player.piece)
        possibleMoves.forEach{
            if (it.toString() == san) return it
        }
        return null
    }

    override fun toString(): String = "Player: $player Moves: $moves\n" + board.toString()
}

fun main() {
    val playerWhite = Player(Piece.WHITE)
    val playerBlack = Player(Piece.BLACK)
    playerWhite.opponent = playerBlack
    playerBlack.opponent = playerWhite
    val game = Game(Board(File.A, File.H), playerWhite)
    println(game)

    game.applyMove(game.parseMove("C4")!!)
    game.applyMove(game.parseMove("B5")!!)
    game.applyMove(game.parseMove("C5")!!)
    game.applyMove(game.parseMove("D5")!!)
    game.applyMove(game.parseMove("CxD6")!!)
    game.applyMove(game.parseMove("C6")!!)
    game.applyMove(game.parseMove("DxE7")!!)
    game.applyMove(game.parseMove("A6")!!)
    game.applyMove(game.parseMove("E8")!!)



//    println(game.unapplyMove())
//    println(game.unapplyMove())
//    println(game.unapplyMove())
}