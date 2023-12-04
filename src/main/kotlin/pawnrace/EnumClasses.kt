package pawnrace

import java.lang.StringBuilder

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

    fun nextRank(piece: Piece): Rank? {
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
    val file: File = File.valueOf(pos[0].toString())
    val rank: Rank = Rank.fromValue(pos[1].toString().toInt())!!

    fun file() = file
    fun rank() = rank

    fun getDiagonals(piece: Piece): List<Position> {
        val diagonals = mutableListOf<Position>()
        val nextRank = rank.nextRank(piece)
        val previousFile = file.previous()
        val nextFile = file.next()
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

class Board(val whiteGap: File, val blackGap: File) {
    val board: HashMap<File, HashMap<Rank, Piece>> = hashMapOf()

    init {
        for (i in 1..8) {
            val currentFile = File.fromValue(i)!!
            val fileList = board.getOrPut(currentFile) { hashMapOf() }
            if (currentFile != whiteGap) fileList[startingRanks[Piece.WHITE]!!] = Piece.WHITE
            if (currentFile != blackGap) fileList[startingRanks[Piece.BLACK]!!] = Piece.BLACK
        }
    }

    fun pieceAt(pos: Position): Piece? = board[pos.file()]?.get(pos.rank())

    fun positionsOf(piece: Piece): List<Position> {
        val list: MutableList<Position> = mutableListOf()
        for ((file, ranks) in board) {
            list.addAll(ranks.filter { it.value == piece }.map { (rank, _) -> Position("${file}${rank}") })
        }
        return list
    }

    fun isValidMove(move: Move, lastMove: Move? = null): Boolean {
        // Peaceful moves
        if (move.type == MoveType.PEACEFUL) {
            // Check if simple 1 or 2 move forward
            if (move.from.file() == move.to.file() && pieceAt(move.to) == null && pieceAt(move.from) == move.piece &&
                ((move.from.rank().nextRank(move.piece) == move.to.rank()) ||
                        (move.from.rank() == startingRanks[move.piece] && move.from.rank().nextRank(move.piece)
                            ?.nextRank(move.piece) == move.to.rank())
                        )
            ) return true
        } else if (move.type == MoveType.CAPTURE) {
            if (move.from.getDiagonals(move.piece)
                    .contains(move.to) && pieceAt(move.to) == move.piece.getOpposite()
            ) return true
        } else {
            TODO()
        }
        return false
    }

    fun move(m: Move): Board {
        board[m.to.file()]!![m.to.rank()] = m.piece
        board[m.from.file()]!!.remove(m.from.rank())
        return this
    }

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
//    println(a.move(Move(Piece.BLACK, Position("B7"), Position("B5"), MoveType.PEACEFUL)))
//    println(a.move(Move(Piece.WHITE, Position("B2"), Position("B3"), MoveType.PEACEFUL)))
//    println(a.move(Move(Piece.BLACK, Position("B5"), Position("A4"), MoveType.CAPTURE)))
//    println(a.move(Move(Piece.WHITE, Position("B3"), Position("A4"), MoveType.CAPTURE)))
//    println(a.positionsOf(Piece.WHITE))
    println(Rank.fromValue(2)?.nextRank(Piece.WHITE))
}