package chess;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
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

    private boolean isWhiteKingMoved = false;
    private boolean isBlackKingMoved = false;
    private boolean isWhiteRookLeftMoved = false;
    private boolean isWhiteRookRightMoved = false;
    private boolean isBlackRookLeftMoved = false;
    private boolean isBlackRookRightMoved = false;

    private boolean validEnPassant = false;
    private boolean[] DoubleMoveWhite = new boolean[8];
    private boolean[] DoubleMoveBlack = new boolean[8];

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

       //Castling move
        if(piece.getPieceType() == ChessPiece.PieceType.KING) {
            validMoves.addAll(castlingMove(startPosition));
        }
        //En Passant move
        if(piece.getPieceType() == ChessPiece.PieceType.PAWN && validEnPassant) {
            validMoves.addAll(enPassantMove(startPosition));
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

        //En Passant capture
        if(piece.getPieceType() == ChessPiece.PieceType.PAWN && board.getPiece(move.getEndPosition()) == null && move.getStartPosition().getColumn() != move.getEndPosition().getColumn()) {
            int captureRow = (piece.getTeamColor() == TeamColor.WHITE) ? move.getEndPosition().getRow() - 1 : move.getEndPosition().getRow() + 1;

            ChessPosition capturePosition = new ChessPosition(captureRow, move.getEndPosition().getColumn());
            board.addPiece(capturePosition, null); //remove the captured pawn
        }

        //move with promotion
        if(move.getPromotionPiece() != null) {
            board.addPiece(move.getEndPosition(), new ChessPiece(piece.getTeamColor(), move.getPromotionPiece()));
        }else { //move without promotion
            board.addPiece(move.getEndPosition(), piece);
        }
        board.addPiece(move.getStartPosition(), null);

        //castling move

        //king castling move
        if(piece.getPieceType() == ChessPiece.PieceType.KING) {
            //left side
            if(move.getStartPosition().getColumn() == 5 && move.getEndPosition().getColumn() == 3) {
                board.addPiece(new ChessPosition(move.getStartPosition().getRow(), 4), board.getPiece(new ChessPosition(move.getStartPosition().getRow(), 1)));
                board.addPiece(new ChessPosition(move.getStartPosition().getRow(), 1), null);
            }else if(move.getStartPosition().getColumn() == 5 && move.getEndPosition().getColumn() == 7) { //right side
                board.addPiece(new ChessPosition(move.getStartPosition().getRow(), 6), board.getPiece(new ChessPosition(move.getStartPosition().getRow(), 8)));
                board.addPiece(new ChessPosition(move.getStartPosition().getRow(), 8), null);
            }
            //update king moved
            if(piece.getTeamColor() == TeamColor.WHITE) {
                isWhiteKingMoved = true;
            }else {
                isBlackKingMoved = true;
            }
        }

        //rook castling move
        if(piece.getPieceType() == ChessPiece.PieceType.ROOK) {
            if(piece.getTeamColor() == TeamColor.WHITE) {
                if(move.getStartPosition().getRow() == 1 && move.getStartPosition().getColumn() == 1) {
                    isWhiteRookLeftMoved = true;
                }
                if(move.getStartPosition().getRow() == 1 && move.getStartPosition().getColumn() == 8) {
                    isWhiteRookRightMoved = true;
                }
            }else {
                if(move.getStartPosition().getRow() == 8 && move.getStartPosition().getColumn() == 1) {
                    isBlackRookLeftMoved = true;
                }
                if(move.getStartPosition().getRow() == 8 && move.getStartPosition().getColumn() == 8) {
                    isBlackRookRightMoved = true;
                }
            }
        }

        //Reset all en passant relating variables
        for(int i = 0; i < 8; i++) {
            DoubleMoveWhite[i] = false;
            DoubleMoveBlack[i] = false;
        }
        validEnPassant = false;

        //En Passant move by pawn double moves
        if(piece.getPieceType() == ChessPiece.PieceType.PAWN) {
            int col = move.getStartPosition().getColumn() -1;
            boolean isDoubleMove = (piece.getTeamColor() == TeamColor.WHITE && move.getStartPosition().getRow() == 2 && move.getEndPosition().getRow() == 4) || (piece.getTeamColor() == TeamColor.BLACK && move.getStartPosition().getRow() == 7 && move.getEndPosition().getRow() == 5);

            if(isDoubleMove) {
                if (piece.getTeamColor() == TeamColor.WHITE) {
                    DoubleMoveWhite[col] = true;
                } else {
                    DoubleMoveBlack[col] = true;
                }
                validEnPassant = true;
            }
        }

        //Switch the team turn
        if(team == TeamColor.WHITE) {
            team = TeamColor.BLACK;
        }else {
            team = TeamColor.WHITE;
        }
    }

    private Collection<ChessMove> castlingMove(ChessPosition startPosition) {
        List<ChessMove> castlingMoves = new ArrayList<>();
        ChessPiece kingPiece = board.getPiece(startPosition);

        //if there is no king in the start position
        if(kingPiece == null || kingPiece.getPieceType() != ChessPiece.PieceType.KING) return castlingMoves;

        //Castling set up
        TeamColor color = kingPiece.getTeamColor();
        int row = startPosition.getRow();
        int col = startPosition.getColumn();

        boolean isKingMoved = (color == TeamColor.WHITE) ? isWhiteKingMoved : isBlackKingMoved;
        boolean leftRookMoved = (color == TeamColor.WHITE) ? isWhiteRookLeftMoved : isBlackRookLeftMoved;
        boolean rightRookMoved = (color == TeamColor.WHITE) ? isWhiteRookRightMoved : isBlackRookRightMoved;

        //check if king has moved or is in check
        if(isKingMoved || col != 5 || isInCheck(color)) return castlingMoves;

        //set up for checking clear path for left side
        boolean isLeftPathClear = true;
        int[] colsForLeft = {4,3,2};
        for(int c : colsForLeft) {
            if(board.getPiece(new ChessPosition(row, c)) != null) {
                isLeftPathClear = false;
            }
        }

        //check left Rook
        boolean possible = true;
        if(!leftRookMoved && isLeftPathClear) {
            int[] possibleCol = {5,4,3};
            for(int c : possibleCol) {
                ChessBoard temp = copyBoard(board);
                temp.addPiece(new ChessPosition(row, c), new ChessPiece(color, ChessPiece.PieceType.KING));
                temp.addPiece(startPosition, null);

                ChessBoard original = this.board;
                this.board = temp;

                if(isInCheck(color)) {
                    possible = false;
                }
                this.board = original;
            }
            if(possible) {
                castlingMoves.add(new ChessMove(startPosition, new ChessPosition(row, 3), null));
            }
        }

        //set up for checking clear path for left side
        boolean isRightPathClear = true;
        int[] colsForRight = {6,7};
        for(int c : colsForRight) {
            if(board.getPiece(new ChessPosition(row, c)) != null) {
                isRightPathClear = false;
            }
        }

        //check right Rook
        possible = true;
        if(!rightRookMoved && isRightPathClear) {
            int[] possibleCol = {5,6,7};
            for(int c : possibleCol) {
                ChessBoard temp = copyBoard(board);
                temp.addPiece(new ChessPosition(row, c), new ChessPiece(color, ChessPiece.PieceType.KING));
                temp.addPiece(startPosition, null);

                ChessBoard original = this.board;
                this.board = temp;

                if(isInCheck(color)) {
                    possible = false;
                }
                this.board = original;
            }

            if(possible) {
                castlingMoves.add(new ChessMove(startPosition, new ChessPosition(row, 7), null));
            }
        }
        return castlingMoves;
    }

    private Collection<ChessMove> enPassantMove(ChessPosition startPosition) {
        List<ChessMove> enPassantMoves = new ArrayList<>();
        ChessPiece pawnPiece = board.getPiece(startPosition);

        //if
        if(pawnPiece == null || pawnPiece.getPieceType() != ChessPiece.PieceType.PAWN) return enPassantMoves;

        int row = startPosition.getRow();
        int col = startPosition.getColumn();
        TeamColor color = pawnPiece.getTeamColor();

        //white pawn case
        if(color == TeamColor.WHITE && row == 5) {
            //check left
            if(col > 1 && DoubleMoveBlack[col - 2]) {
                ChessPosition leftPosition = new ChessPosition(row, col-1);
                ChessPiece leftPiece = board.getPiece(leftPosition);
                if(leftPiece != null && leftPiece.getPieceType() == ChessPiece.PieceType.PAWN && leftPiece.getTeamColor() == TeamColor.BLACK) {
                    ChessPosition movePosition = new ChessPosition(row+1, col-1);
                    ChessMove move = new ChessMove(startPosition, movePosition, null);

                    ChessBoard temp = copyBoard(board);
                    temp.addPiece(movePosition, pawnPiece);
                    temp.addPiece(startPosition, null);
                    temp.addPiece(leftPosition, null); //capture the black pawn

                    ChessBoard original = board;
                    setBoard(temp);
                    if(!isInCheck(color)) {
                        enPassantMoves.add(move);
                    }
                    setBoard(original);
                }
            }
            //check right
            if(col < 8 && DoubleMoveBlack[col]) {
                ChessPosition rightPosition = new ChessPosition(row, col+1);
                ChessPiece rightPiece = board.getPiece(rightPosition);
                if(rightPiece != null && rightPiece.getPieceType() == ChessPiece.PieceType.PAWN && rightPiece.getTeamColor() == TeamColor.BLACK) {
                    ChessPosition movePosition = new ChessPosition(row+1, col+1);
                    ChessMove move = new ChessMove(startPosition, movePosition, null);

                    ChessBoard temp = copyBoard(board);
                    temp.addPiece(movePosition, pawnPiece);
                    temp.addPiece(startPosition, null);
                    temp.addPiece(rightPosition, null); //capture the black pawn

                    ChessBoard original = board;
                    setBoard(temp);
                    if(!isInCheck(color)) {
                        enPassantMoves.add(move);
                    }
                    setBoard(original);
                }
            }
        }

        //black pawn case
        if(color == TeamColor.BLACK && row == 4) {
            //check left
            if(col > 1 && DoubleMoveWhite[col - 2]) {
                ChessPosition leftPosition = new ChessPosition(row, col-1);
                ChessPiece leftPiece = board.getPiece(leftPosition);
                if(leftPiece != null && leftPiece.getPieceType() == ChessPiece.PieceType.PAWN && leftPiece.getTeamColor() == TeamColor.WHITE) {
                    ChessPosition movePosition = new ChessPosition(row-1, col-1);
                    ChessMove move = new ChessMove(startPosition, movePosition, null);

                    ChessBoard temp = copyBoard(board);
                    temp.addPiece(movePosition, pawnPiece);
                    temp.addPiece(startPosition, null);
                    temp.addPiece(leftPosition, null); //capture the black pawn

                    ChessBoard original = board;
                    setBoard(temp);
                    if(!isInCheck(color)) {
                        enPassantMoves.add(move);
                    }
                    setBoard(original);
                }
            }
            //check right
            if(col < 8 && DoubleMoveWhite[col]) {
                ChessPosition rightPosition = new ChessPosition(row, col+1);
                ChessPiece rightPiece = board.getPiece(rightPosition);
                if(rightPiece != null && rightPiece.getPieceType() == ChessPiece.PieceType.PAWN && rightPiece.getTeamColor() == TeamColor.WHITE) {
                    ChessPosition movePosition = new ChessPosition(row-1, col+1);
                    ChessMove move = new ChessMove(startPosition, movePosition, null);

                    ChessBoard temp = copyBoard(board);
                    temp.addPiece(movePosition, pawnPiece);
                    temp.addPiece(startPosition, null);
                    temp.addPiece(rightPosition, null); //capture the black pawn

                    ChessBoard original = board;
                    setBoard(temp);
                    if(!isInCheck(color)) {
                        enPassantMoves.add(move);
                    }
                    setBoard(original);
                }
            }
        }
        return enPassantMoves;
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
        //check if the team is in check
        if(!isInCheck(teamColor)) {
            return false;
        }
        //Check if there is valid move that removes check
        for(int row = 1; row <= 8; row++) {
            for(int col = 1; col <= 8; col++) {
                ChessPosition position = new ChessPosition(row, col);
                ChessPiece piece = board.getPiece(position);
                if(piece != null && piece.getTeamColor() == teamColor) {
                    Collection<ChessMove> moves = validMoves(position);
                    if(!moves.isEmpty()) {
                        return false;
                    }
                }
            }
        }
        //no valid move and when it is in checkmate
        return true;
    }

    /**
     * Determines if the given team is in stalemate, which here is defined as having
     * no valid moves
     *
     * @param teamColor which team to check for stalemate
     * @return True if the specified team is in stalemate, otherwise false
     */
    public boolean isInStalemate(TeamColor teamColor) {
        //check if the team is not in check
        if(isInCheck(teamColor)) {
            return false;
        }

        //Check if there is valid move
        for(int row = 1; row <= 8; row++) {
            for(int col = 1; col <= 8; col++) {
                ChessPosition position = new ChessPosition(row, col);
                ChessPiece piece = board.getPiece(position);
                if(piece != null && piece.getTeamColor() == teamColor) {
                    Collection<ChessMove> moves = validMoves(position);
                    if(!moves.isEmpty()) {
                        return false;
                    }
                }
            }
        }
        //No valid move and when it is not in check
        return true;
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
