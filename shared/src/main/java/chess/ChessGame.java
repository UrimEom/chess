package chess;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;

/**
 * For a class that can manage a chess game, making moves on a board
 * <p>
 * Note: You can add to this class, but you may not alter
 * signature of the existing methods.
 */
public class ChessGame {
    private ChessGame.TeamColor team = TeamColor.WHITE;
    private ChessBoard board = new ChessBoard();

    public ChessGame() {

    }

    /**
     * @return Which team's turn it is
     */
    public TeamColor getTeamTurn() {
        return team;
    }

    /**
     * Set's which teams turn it is
     *
     * @param team the team whose turn it is
     */
    public void setTeamTurn(TeamColor team) {
        this.team = team;
    }

    /**
     * Enum identifying the 2 possible teams in a chess game
     */
    public enum TeamColor {
        WHITE,
        BLACK
    }

    /**
     * Gets a valid moves for a piece at the given location
     *
     * @param startPosition the piece to get valid moves for
     * @return Set of valid moves for requested piece, or null if no piece at
     * startPosition
     */
    public Collection<ChessMove> validMoves(ChessPosition startPosition) {
       ChessBoard board = getBoard();
       ChessPiece piece = board.getPiece(startPosition);

       if(piece == null || piece.getTeamColor() != this.team) {
           return null;
       }

       Collection<ChessMove> validMoves = new ArrayList<>();
       Collection<ChessMove> moves = piece.pieceMoves(board, startPosition);
       for (ChessMove move : moves) {
           //create a deep copy of the board
           ChessBoard temp = board.clone();
           //Check the move on the temporary board
           if (move.getPromotionPiece() != null) { //move with promotion
               temp.addPiece(move.getEndPosition(), new ChessPiece(piece.getTeamColor(), move.getPromotionPiece()));
           } else { //move without promotion
               temp.addPiece(move.getEndPosition(), piece);
           }
           //move if there is nothing in the position
           temp.addPiece(startPosition, null);


       }
       return validMoves;
    }

    /**
     * Makes a move in a chess game
     *
     * @param move chess move to perform
     * @throws InvalidMoveException if move is invalid
     */
    public void makeMove(ChessMove move) throws InvalidMoveException {

    }

    /**
     * Determines if the given team is in check
     *
     * @param teamColor which team to check for check
     * @return True if the specified team is in check
     */
    public boolean isInCheck(TeamColor teamColor) {
        ChessBoard board = getBoard();
        ChessPosition kingPos = null;
        //find the King position
        for(int row = 0; row < 8; row++) {
            for(int col = 0; col < 8; col++) {
                ChessPosition position = new ChessPosition(row,col);
                if(board.getPiece(position) != null && board.getPiece(position).getPieceType() == ChessPiece.PieceType.KING && board.getPiece(position).getTeamColor() == teamColor) {
                    kingPos = position;
                    break;
                }
            }
        }
        //if King is not found
        if(kingPos == null) {
            return false;
        }
        //find enemy pieces
        for(int row = 0; row < 8; row++) {
            for(int col = 0; col < 8; col++) {
                ChessPosition position = new ChessPosition(row, col);
                if(board.getPiece(position) != null && board.getPiece(position).getTeamColor() != board.getPiece(kingPos).getTeamColor()) {
                    Collection<ChessMove> moves = board.getPiece(position).pieceMoves(board, position);
                    for(ChessMove move : moves) {
                        if(kingPos.equals(move.getEndPosition())) return true;
                    }
                }
            }
        }
        return false;
    }

    /**
     * Determines if the given team is in checkmate
     *
     * @param teamColor which team to check for checkmate
     * @return True if the specified team is in checkmate
     */
    public boolean isInCheckmate(TeamColor teamColor) {
        throw new RuntimeException("Not implemented");
    }

    /**
     * Determines if the given team is in stalemate, which here is defined as having
     * no valid moves
     *
     * @param teamColor which team to check for stalemate
     * @return True if the specified team is in stalemate, otherwise false
     */
    public boolean isInStalemate(TeamColor teamColor) {
        throw new RuntimeException("Not implemented");
    }

    /**
     * Sets this game's chessboard with a given board
     *
     * @param board the new board to use
     */
    public void setBoard(ChessBoard board) {
        this.board = board;
    }

    /**
     * Gets the current chessboard
     *
     * @return the chessboard
     */
    public ChessBoard getBoard() {
        return board;
    }
}
