package pawnrace

import java.util.Stack

class Game (val board: Board, var player: Piece, val moves: Stack<Move> = Stack()) {
    fun applyMove(move: Move): Game {
        if (board.isValidMove(move, moves.firstOrNull())) board.move(move)
        player = player.getOpposite()
        moves.add(move)
        return this
    }
}

fun main() {
//    val game = Game(Board(File("A"), File("H")), Piece.WHITE)
}