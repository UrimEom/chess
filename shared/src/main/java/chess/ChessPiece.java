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

        if(this.type == PieceType.BISHOP) { //Bishop's move
            int[] possibleRow = {-1, -1, 1, 1};
            int[] possibleCol = {-1, 1, -1 , 1};

            addLinearMove(board, myPosition, moves, possibleRow, possibleCol);
        }else if(this.type == PieceType.KNIGHT) { //Knight's move
            int[] possibleRow = {2,1,-1,-2,-2,-1,1,2};
            int[] possibleCol = {1,2,2,1,-1,-2,-2,-1};

            addDeltaMove(board, myPosition, moves, possibleRow, possibleCol);
        }else if(this.type == PieceType.ROOK) { //Rook's move
            int[] possibleRow = {-1,0,1,0};
            int[] possibleCol = {0,-1,0,1};

            addLinearMove(board, myPosition, moves, possibleRow, possibleCol);
        }else if(this.type == PieceType.QUEEN) { //Queen's move
            int[] possibleRow = {-1,-1,1,1,-1,0,1,0};
            int[] possibleCol = {-1,1,-1,1,0,-1,0,1};

            addLinearMove(board, myPosition, moves, possibleRow, possibleCol);
        }else if(this.type == PieceType.KING) { //King's move
            int[] possibleRow = {-1,-1,1,1,-1,0,1,0};
            int[] possibleCol = {-1,1,-1,1,0,-1,0,1};

            addDeltaMove(board, myPosition, moves, possibleRow, possibleCol);
        }else if(this.type == PieceType.PAWN) { //Pawn's move
            addPawnMoves(board, myPosition, moves);
        }
        return moves;
    }

    private void addPawnMoves(ChessBoard board, ChessPosition myPosition, Collection<ChessMove> moves) {
        int row = myPosition.getRow();
        int col = myPosition.getColumn();

        int next = (pieceColor == ChessGame.TeamColor.WHITE) ? 1 : -1;
        int startRow = (pieceColor == ChessGame.TeamColor.WHITE) ? 2 : 7;
        int promotionRow = (pieceColor == ChessGame.TeamColor.WHITE) ? 8 : 1;

        //white case
        int nextMove = row + next;
        if (nextMove >= 1 && nextMove <= 8) {
            //normal move
            ChessPosition nextForward = new ChessPosition(nextMove, col);
            if(board.getPiece(nextForward) == null) {
                if(nextMove == promotionRow) { //promote
                    ChessPiece.PieceType[] promotion = {PieceType.BISHOP, PieceType.KNIGHT, PieceType.ROOK, PieceType.QUEEN};
                    for (int i = 0; i < 4; i++) {
                        moves.add(new ChessMove(myPosition, nextForward, promotion[i]));
                    }
                }else { //move the pawn
                    moves.add(new ChessMove(myPosition, nextForward, null));

                    //start move
                    if(row == startRow) {
                        ChessPosition startForward = new ChessPosition(row + 2 * next, col);
                        if(board.getPiece(startForward) == null) {
                            moves.add(new ChessMove(myPosition, startForward, null));
                        }
                    }
                }
            }

            //capture the enemy
            int[] possibleCol = {-1, 1};
            for(int possible : possibleCol) {
                int captureCol = col + possible;
                if (captureCol < 1 || captureCol > 8 || nextMove < 1 || nextMove > 8) { continue; }

                ChessPosition capturePiece = new ChessPosition(nextMove, captureCol);
                ChessPiece piece = board.getPiece(capturePiece);

                if (piece != null && piece.getTeamColor() != this.pieceColor) {
                    if (nextMove == promotionRow) { //promote and capture
                        ChessPiece.PieceType[] promotion = {PieceType.BISHOP, PieceType.KNIGHT, PieceType.ROOK, PieceType.QUEEN};
                        for (int i = 0; i < 4; i++) {
                            moves.add(new ChessMove(myPosition, capturePiece, promotion[i]));
                        }
                    } else { //capture
                        moves.add(new ChessMove(myPosition, capturePiece, null));
                    }
                }
            }
        }
    }

    private void addLinearMove(ChessBoard board, ChessPosition myPosition, Collection<ChessMove> moves, int[] possibleRow, int[] possibleCol) {
        for (int i = 0; i < possibleRow.length; i++) {
            int newRow = myPosition.getRow() + possibleRow[i];
            int newCol = myPosition.getColumn() + possibleCol[i];

            while (newRow >= 1 && newRow <= 8 && newCol >= 1 && newCol <= 8) {
                ChessPosition newPosition = new ChessPosition(newRow, newCol);
                ChessPiece other = board.getPiece(newPosition);

                if (other == null) { //move
                    moves.add(new ChessMove(myPosition, newPosition, null));
                } else {
                    if (other.getTeamColor() != this.pieceColor) { //capture the enemy
                        moves.add(new ChessMove(myPosition, newPosition, null));
                    }
                    break; //stop if something in that position is already there
                }
                newRow += possibleRow[i];
                newCol += possibleCol[i];
            }
        }
    }
    private void addDeltaMove(ChessBoard board, ChessPosition myPosition, Collection<ChessMove> moves, int[] possibleRow, int[] possibleCol) {
        for (int i = 0; i < possibleRow.length; i++) {
            int newRow = myPosition.getRow() + possibleRow[i];
            int newCol = myPosition.getColumn() + possibleCol[i];

            if (newRow >= 1 && newRow <= 8 && newCol >= 1 && newCol <= 8) {
                ChessPosition newPosition = new ChessPosition(newRow, newCol);
                ChessPiece other = board.getPiece(newPosition);

                if (other == null || other.getTeamColor() != this.pieceColor) { //move or capture the enemy
                    moves.add(new ChessMove(myPosition, newPosition, null));
                }
            }
        }
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
