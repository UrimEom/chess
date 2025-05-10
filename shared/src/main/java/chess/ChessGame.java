package chess;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Objects;

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
        board.resetBoard();
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

    public ChessBoard copyBoard(ChessBoard copy) {
        ChessBoard newBoard = new ChessBoard();

        for(int i = 1; i <= 8; i++) {
            for(int j = 1; j <= 8; j++) {
                ChessPosition position = new ChessPosition(i, j);
                ChessPiece piece = board.getPiece(position);
                if(piece != null) {
                    ChessPiece copiedPiece = new ChessPiece(piece.getTeamColor(), piece.getPieceType());
                    newBoard.addPiece(position, copiedPiece);
                }else {
                    newBoard.addPiece(position, null);
                }
            }
        }
        return newBoard;
    }
    /**
     * Gets a valid moves for a piece at the given location
     *
     * @param startPosition the piece to get valid moves for
     * @return Set of valid moves for requested piece, or null if no piece at
     * startPosition
     */
    public Collection<ChessMove> validMoves(ChessPosition startPosition) {
       ChessPiece piece = board.getPiece(startPosition);

       //if there is no piece in start position
       if(piece == null) {
           return new ArrayList<>();
       }

       Collection<ChessMove> validMoves = new ArrayList<>();
       Collection<ChessMove> moves = piece.pieceMoves(board, startPosition);
       for (ChessMove move : moves) {
           //create a deep copy of the board
           ChessBoard temp = copyBoard(board);

           //Check the move on the temporary board
           if (move.getPromotionPiece() != null) { //capture and move with promotion
               temp.addPiece(move.getEndPosition(), new ChessPiece(piece.getTeamColor(), move.getPromotionPiece()));
           } else { //capture and move without promotion
               temp.addPiece(move.getEndPosition(), piece);
           }
           //move if there is nothing in the position
           temp.addPiece(startPosition, null);

           //if there is "check"
           ChessBoard original = this.board;
           this.setBoard(temp);
           boolean check = isInCheck(piece.getTeamColor());
           this.setBoard(original);
           if(!check) {
               validMoves.add(move);
           }

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
        ChessPiece piece = board.getPiece(move.getStartPosition());

        //check if piece is in current team
        if(piece == null || piece.getTeamColor() != team) {
            throw new InvalidMoveException("It's not your turn");
        }

        Collection<ChessMove> validMoves = validMoves(move.getStartPosition());
        //check if making move is valid
        if(!validMoves.contains(move)) {
            throw new InvalidMoveException("Not valid move");
        }

        //move with promotion
        if(move.getPromotionPiece() != null) {
            board.addPiece(move.getEndPosition(), new ChessPiece(piece.getTeamColor(), move.getPromotionPiece()));
        }else { //move without promotion
            board.addPiece(move.getEndPosition(), piece);
        }
        board.addPiece(move.getStartPosition(), null);

        //Switch the team turn
        if(team == TeamColor.WHITE) {
            team = TeamColor.BLACK;
        }else {
            team = TeamColor.WHITE;
        }
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
        for(int row = 1; row <= 8; row++) {
            for(int col = 1; col <= 8; col++) {
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
        for(int row = 1; row <= 8; row++) {
            for(int col = 1; col <= 8; col++) {
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

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ChessGame chessGame = (ChessGame) o;
        return team == chessGame.team && Objects.equals(board, chessGame.board);
    }

    @Override
    public int hashCode() {
        return Objects.hash(team, board);
    }
}
