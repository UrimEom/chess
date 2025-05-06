package chess;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Objects;

/**
 * Represents a single chess piece
 * <p>
 * Note: You can add to this class, but you may not alter
 * signature of the existing methods.
 */
public class ChessPiece {
    private ChessGame.TeamColor pieceColor;
    private ChessPiece.PieceType type;

    public ChessPiece(ChessGame.TeamColor pieceColor, ChessPiece.PieceType type) {
        this.pieceColor = pieceColor;
        this.type = type;
    }

    /**
     * The various different chess piece options
     */
    public enum PieceType {
        KING,
        QUEEN,
        BISHOP,
        KNIGHT,
        ROOK,
        PAWN
    }

    /**
     * @return Which team this chess piece belongs to
     */
    public ChessGame.TeamColor getTeamColor() {
        return pieceColor;
    }

    /**
     * @return which type of chess piece this piece is
     */
    public PieceType getPieceType() {
        return type;
    }

    /**
     * Calculates all the positions a chess piece can move to
     * Does not take into account moves that are illegal due to leaving the king in
     * danger
     *
     * @return Collection of valid moves
     */
    public Collection<ChessMove> pieceMoves(ChessBoard board, ChessPosition myPosition) {
        Collection<ChessMove> moves = new ArrayList<>();

        //move Bishop
        if(this.type == PieceType.BISHOP) {
            int[][] possibleMove = {{1,1}, {1, -1}, {-1, -1}, {-1, 1}};

            for (int[] mv : possibleMove) {
                int row = myPosition.getRow() + mv[0];
                int col = myPosition.getColumn() + mv[1];

                while (row >= 1 && row <= 8 && col >= 1 && col <= 8) {
                    ChessPosition newPosition = new ChessPosition(row, col);
                    ChessPiece other = board.getPiece(newPosition);

                    if (other == null) {
                        moves.add(new ChessMove(myPosition, newPosition, null));
                    } else {
                        if (other.getTeamColor() != this.pieceColor) {
                            moves.add(new ChessMove(myPosition, newPosition, null));
                        }
                        break;
                    }
                    row += mv[0];
                    col += mv[1];
                }
            }
        }

        //Knight

        //Rook

        //Pawn

        //Queen

        //King

        return moves;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ChessPiece that = (ChessPiece) o;
        return pieceColor == that.pieceColor && type == that.type;
    }

    @Override
    public int hashCode() {
        return Objects.hash(pieceColor, type);
    }
}
