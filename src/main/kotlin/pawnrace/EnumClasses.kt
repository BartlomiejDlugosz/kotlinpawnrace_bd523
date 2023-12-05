package pawnrace

import java.lang.StringBuilder
import java.util.*

val startingRanks = hashMapOf<Piece, Rank>(
    Piece.WHITE to Rank.TWO,
    Piece.BLACK to Rank.SEVEN
)

const val LOWEST_FILE = 'A'
const val HIGHEST_FILE = 'H'

enum class Piece {
    BLACK, WHITE;

    fun getOpposite(): Piece = if (this == Piece.BLACK) Piece.WHITE else Piece.BLACK
    override fun toString(): String = if (this == Piece.WHITE) "W" else "B"

}

enum class MoveType { PEACEFUL, CAPTURE, EN_PASSANT }

enum class File(val value: Int) {
    A(1), B(2), C(3), D(4), E(5), F(6), G(7), H(8);

    fun next(): File? {
        val newOrdinal = ordinal + 1
        return if (newOrdinal < entries.size) {
            entries[newOrdinal]
        } else null
    }

    fun previous(): File? {
        val newOrdinal = ordinal - 1
        return if (newOrdinal >= 0) {
            entries[newOrdinal]
        } else null
    }

    companion object {
        fun fromValue(value: Int): File? = entries.find { it.value == value }
    }
}

enum class Rank(val value: Int) {
    ONE(1), TWO(2), THREE(3), FOUR(4), FIVE(5), SIX(6), SEVEN(7), EIGHT(8);

    fun nextRank(piece: Piece?): Rank? {
        val newRank = ordinal + (if (piece == Piece.WHITE) 1 else -1)
        if (newRank < entries.size) return entries[newRank]
        return null
    }

    companion object {
        fun fromValue(value: Int): Rank? = entries.find { it.value == value }
    }

    override fun toString(): String = value.toString()
}

data class Position(val pos: String) {
    private val file: File = File.valueOf(pos[0].toString())
    private val rank: Rank = Rank.fromValue(pos[1].toString().toInt())!!

    fun file(): File = file
    fun rank(): Rank = rank

    fun getDiagonals(piece: Piece?): List<Position> {
        val diagonals = mutableListOf<Position>()
        val nextRank = rank.nextRank(piece)
        val (previousFile, nextFile) = file.previous() to file.next()

        if (nextRank == null) return emptyList()
        if (previousFile != null) diagonals.add(Position("${previousFile}${nextRank}"))
        if (nextFile != null) diagonals.add(Position("${nextFile}${nextRank}"))

        return diagonals
    }

    override fun toString() = "$file$rank"
}

class Move(val piece: Piece, val from: Position, val to: Position, val type: MoveType) {
    override fun toString(): String = if (type == MoveType.PEACEFUL) "$to" else "${from.file()}x$to"
}

class Board(private val whiteGap: File, private val blackGap: File) {
    private val board: EnumMap<File, EnumMap<Rank, Piece>> = EnumMap(File::class.java)

    init {
        for (i in 1..8) {
            val currentFile = File.fromValue(i)!!
            val fileList = board.getOrPut(currentFile) { EnumMap(Rank::class.java) }
            if (currentFile != whiteGap) fileList[startingRanks[Piece.WHITE]!!] = Piece.WHITE
            if (currentFile != blackGap) fileList[startingRanks[Piece.BLACK]!!] = Piece.BLACK
        }
    }

    fun pieceAt(pos: Position): Piece? = board[pos.file()]?.get(pos.rank())

    fun positionsOf(piece: Piece): List<Position> {
        val positions: MutableList<Position> = mutableListOf()
        for ((file, ranks) in board) {
            positions.addAll(ranks.filter { it.value == piece }.map { (rank, _) -> Position("${file}${rank}") })
        }
        return positions
    }

    fun validMoves(pos: Position, lastMove: Move? = null): List<Pair<Position, MoveType>> {
        val moves = mutableListOf<Pair<Position, MoveType>>()
        val piece = pieceAt(pos) ?: return emptyList()
        val rank = pos.rank()
        val file = pos.file()

        // 1 move forward
        val nextRank = rank.nextRank(piece)
        val nextPosition = Position("${file}${nextRank}")
        if (pieceAt(nextPosition) == null) {
            if (nextRank != null) moves.add(nextPosition to MoveType.PEACEFUL)

            // 2 moves forward

            if (rank == startingRanks[piece]) {
                val nextNextPosition = Position("$file${nextRank?.nextRank(piece)}")
                if (pieceAt(nextNextPosition) == null) moves.add(nextNextPosition to MoveType.PEACEFUL)
            }
        }



        // Capture
        val diagonals = pos.getDiagonals(piece)
        for (position in diagonals) {
            if (pieceAt(position) == piece.getOpposite()) moves.add(Position("$position") to MoveType.CAPTURE)
        }

        // En-passant
        if (lastMove != null) {
            if (lastMove.from.rank() == startingRanks[lastMove.piece] &&
                pos.rank() == lastMove.to.rank() &&
                (pos.file().previous() == lastMove.to.file() || pos.file().next() == lastMove.to.file())
            )
                moves.add(Position("${lastMove.to.file()}${lastMove.from.rank().nextRank(lastMove.piece)}") to MoveType.EN_PASSANT)
        }

        return moves
    }

    fun isValidMove(move: Move, lastMove: Move? = null): Boolean = validMoves(move.from, lastMove).map{it.first}.contains(move.to)

    fun move(m: Move, lastMove: Move? = null): Board {
        isValidMove(m, lastMove)
        if (m.type == MoveType.PEACEFUL || m.type == MoveType.CAPTURE) {
            board[m.to.file()]!![m.to.rank()] = m.piece
            board[m.from.file()]!!.remove(m.from.rank())
        } else {
            board[m.to.file()]!![m.to.rank()] = m.piece
            board[m.to.file()]!![m.to.rank().nextRank(m.piece.getOpposite())] = null
            board[m.from.file()]!!.remove(m.from.rank())
        }
        return this
    }

    fun add(pos: Position, piece: Piece) = board[pos.file()]?.set(pos.rank(), piece)

    fun getBoard() = board

    override fun toString(): String {
        val str = StringBuilder()
        val ranks = "\t " + listOf("A", "B", "C", "D", "E", "F", "G", "H").joinToString(" ") + "\t \n"
        str.append(ranks)
        str.append("\n")

        for (i in 8 downTo 1) {
            str.append("$i\t")
            for (j in 1..8) {
                str.append(
                    " ${
                        if (board[File.fromValue(j)]?.get(Rank.fromValue(i)) == null) "." else board[File.fromValue(j)]?.get(
                            Rank.fromValue(i)
                        ).toString()
                    }"
                )
            }
            str.append("\t $i\n")
        }

        str.append("\n")
        str.append(ranks)

        return str.toString()
    }
}

fun main() {
    val a = Board(File.H, File.A)
    println(a)
//    println(a.isValidMove(Move(Piece.WHITE, Position("A5"), Position("A6"), MoveType.PEACEFUL)))
    println(a.move(Move(Piece.WHITE, Position("A2"), Position("A4"), MoveType.PEACEFUL)))
    println(a.move(Move(Piece.BLACK, Position("B7"), Position("B5"), MoveType.PEACEFUL)))
    println(a.move(Move(Piece.WHITE, Position("C2"), Position("C5"), MoveType.PEACEFUL)))
    println(a.move(Move(Piece.BLACK, Position("D7"), Position("D5"), MoveType.PEACEFUL)))
    println(a.validMoves(Position("C5"), Move(Piece.WHITE, Position("A4"), Position("A5"), MoveType.PEACEFUL)))
//    println(a.move(Move(Piece.BLACK, Position("B7"), Position("B5"), MoveType.PEACEFUL)))
//    println(a.move(Move(Piece.WHITE, Position("B2"), Position("B3"), MoveType.PEACEFUL)))
//    println(a.move(Move(Piece.BLACK, Position("B5"), Position("A4"), MoveType.CAPTURE)))
//    println(a.move(Move(Piece.WHITE, Position("B3"), Position("A4"), MoveType.CAPTURE)))
//    println(a.positionsOf(Piece.WHITE))
//    println(Rank.fromValue(2)?.nextRank(Piece.WHITE))
}