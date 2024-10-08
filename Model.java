package game2048;

import java.util.Formatter;
import java.util.Observable;


/** The state of a game of 2048.
 *  @author TODO: YOUR NAME HERE
 */
public class Model extends Observable {
    /** Current contents of the board. */
    private Board _board;
    private boolean changed;
    /** Current score. */
    private int _score;
    /** Maximum score so far.  Updated when game ends. */
    private int _maxScore;
    /** True iff game is ended. */
    private boolean _gameOver;

    /* Coordinate System: column C, row R of the board (where row 0,
     * column 0 is the lower-left corner of the board) will correspond
     * to board.tile(c, r).  Be careful! It works like (x, y) coordinates.
     */

    /** Largest piece value. */
    public static final int MAX_PIECE = 2048;

    /** A new 2048 game on a board of size SIZE with no pieces
     *  and score 0. */
    public Model(int size) {
        _board = new Board(new int[size][size]);
    }

    /** A new 2048 game where RAWVALUES contain the values of the tiles
     * (0 if null). VALUES is indexed by (row, col) with (0, 0) corresponding
     * to the bottom-left corner. Used for testing purposes. */
    public Model(int[][] rawValues, int score, int maxScore, boolean gameOver) {
        _board = new Board(rawValues);
        _score = score;
        _maxScore = maxScore;
        _gameOver = gameOver;
    }

    /** Return the current Tile at (COL, ROW), where 0 <= ROW < size(),
     *  0 <= COL < size(). Returns null if there is no tile there.
     *  Used for testing. Should be deprecated and removed.
     */
    public Tile tile(int col, int row) {
        return _board.tile(col, row);
    }

    /** Return the number of squares on one side of the board.
     *  Used for testing. Should be deprecated and removed. */
    public int size() {
        return _board.size();
    }

    /** Return true iff the game is over (there are no moves, or
     *  there is a tile with value 2048 on the board). */
    public boolean gameOver() {
        checkGameOver();
        if (_gameOver) {
            _maxScore = Math.max(_score, _maxScore);
        }
        return _gameOver;
    }

    /** Return the current score. */
    public int score() {
        return _score;
    }

    /** Return the current maximum game score (updated at end of game). */
    public int maxScore() {
        return _maxScore;
    }

    /** Clear the board to empty and reset the score. */
    public void clear() {
        _score = 0;
        _gameOver = false;
        _board.clear();
        setChanged();
    }

    /** Add TILE to the board. There must be no Tile currently at the
     *  same position. */
    public void addTile(Tile tile) {
        _board.addTile(tile);
        checkGameOver();
        setChanged();
    }

    /** Tilt the board toward SIDE. Return true iff this changes the board.
     *
     * 1. If two Tile objects are adjacent in the direction of motion and have
     *    the same value, they are merged into one Tile of twice the original
     *    value and that new value is added to the score instance variable
     * 2. A tile that is the result of a merge will not merge again on that
     *    tilt. So each move, every tile will only ever be part of at most one
     *    merge (perhaps zero).
     * 3. When three adjacent tiles in the direction of motion have the same
     *    value, then the leading two tiles in the direction of motion merge,
     *    and the trailing tile does not.
     */

    public void tiltcolumn(int c) {
        for (int r = _board.size() - 1; r > 0; r--) {
            if(_board.tile(c, r) == null) {
                //look for next non null
                for(int i = r - 1; i >= 0; i--) {
                    if(_board.tile(c, i) != null) {
                        _board.move(c,r,_board.tile(c,i));
                        changed = true;
                        break;
                    }
                }
//                _board.move(c, r - 1, _board.tile(c, r));
            }
        }
    }
    public void mergemf(int c) {
        if (_board.tile(c, 0) != null && _board.tile(c, 1) != null && _board.tile(c, 2) != null && _board.tile(c, 3) != null) {
            if(_board.tile(c,0).value() == _board.tile(c,1).value() && _board.tile(c,2).value() == _board.tile(c,0).value() && _board.tile(c,0).value() == _board.tile(c,3).value()) {
                _board.move(c, 0, _board.tile(c, 1));
                _board.tile(c, 0).merge(c, 0, _board.tile(c,0));
                _score += _board.tile(c,0).value();
                _board.move(c, 2, _board.tile(c, 3));
                _board.tile(c, 2).merge(c, 2, _board.tile(c, 2));
                _score += _board.tile(c,2).value();
                changed = true;
            }
        }
        for (int r = _board.size() - 1; r > 0; r--) {
            if (_board.tile(c, r) != null && _board.tile(c, r - 1) != null && _board.tile(c, r).value() == _board.tile(c, r - 1).value()) {
                _board.move(c, r - 1, _board.tile(c, r));
                _board.tile(c, r - 1).merge(c, r - 1, _board.tile(c, r - 1));
                _score += _board.tile(c,r-1).value();
                changed = true;
                 r = r - 1;
                 break;
            }
        }


    }



    public boolean tilt(Side side) {
        //tilt the board so its north facing up

        //no go through each column and see which columns collapse
//        boolean changed;
//        changed = false;
        _board.setViewingPerspective(side);
        for(int i = 0; i < _board.size(); i++) {
            tiltcolumn(i);
            mergemf(i);
            tiltcolumn(i);
        }

        _board.setViewingPerspective(Side.NORTH);
        checkGameOver();
        if (changed) {
            setChanged();
        }
        return changed;
    }

    /** Checks if the game is over and sets the gameOver variable
     *  appropriately.
     */
    private void checkGameOver() {
        _gameOver = checkGameOver(_board);
    }

    /** Determine whether game is over. */
    private static boolean checkGameOver(Board b) {
        return maxTileExists(b) || !atLeastOneMoveExists(b);
    }

    /** Returns true if at least one space on the Board is empty.
     *  Empty spaces are stored as null.
     */
    public static boolean emptySpaceExists(Board b) {
        for(int r = 0; r < b.size(); r++) {
            for(int c = 0; c < b.size(); c++) {
                if(b.tile(r, c) == null) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Returns true if any tile is equal to the maximum valid value.
     * Maximum valid value is given by MAX_PIECE. Note that
     * given a Tile object t, we get its value with t.value().
     */
    public static boolean maxTileExists(Board b) {
        for(int r = 0; r < b.size(); r++) {
            for(int c = 0; c < b.size(); c++) {
                if(b.tile(r, c) != null && b.tile(r, c).value() == MAX_PIECE) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Returns true if there are any valid moves on the board.
     * There are two ways that there can be valid moves:
     * 1. There is at least one empty space on the board.
     * 2. There are two adjacent tiles with the same value.
     */
    public static boolean atLeastOneMoveExists(Board b) {
        //check corner
        //when row is 0
        if(b.tile(b.size() - 1, b.size() - 1) == null) {
            return true;
        }
        if(b.tile(0, 0) == null) {
            return true;
        }
        if(b.tile(b.size() - 1, 0) == null) {
            return true;
        }
        for(int r = 1; r < b.size() - 1; r++) {
            for(int c = 1; c < b.size() - 1; c++) {
                    Tile top = b.tile(r - 1, c);
                    Tile bottom = b.tile(r + 1, c);
                    Tile left =  b.tile(r, c - 1);
                    Tile right = b.tile(r, c + 1);
                    if(top == null || bottom == null || left == null || right == null || b.tile(r, c).value() == top.value()|| b.tile(r, c).value() == bottom.value() || b.tile(r, c).value() == left.value() || b.tile(r, c).value() == right.value()) {
                        //System.out.println("0 " + b.tile(r, c));
                        return true;
                    }
            }
        }

        int curr = 0;
        if((b.tile(0, b.size() - 1)) != null) {
             curr = (b.tile(0, b.size() - 1)).value();
        }
        if(b.tile(0, 1) == null || b.tile(0, 1) == null || b.tile(0,0).value() == b.tile(0, 1).value() || b.tile(0, 1).value() == b.tile(0,0).value()) {
            //System.out.println("1 " + curr);
            return true;
        }
        if(b.tile(0,  b.size() - 1) == null || b.tile( 1,  b.size() - 1) == null || curr == b.tile( 1,  b.size() - 1).value() || b.tile( 1,  b.size() - 1).value() == curr) {
            //System.out.println("2 " + curr);
            return true;
        }
        if(b.tile(b.size() - 1, 0) != null) {
            curr = b.tile(b.size() - 1, 0).value();
        }

        if(b.tile(b.size() - 2, 0) == null || b.tile(b.size() - 1, 1) == null || curr == b.tile(b.size() - 2, 0).value() || curr == b.tile(  b.size() - 1, 1).value()) {
            //System.out.println("3 " + curr);
            return true;
        }
        if((b.tile(b.size() - 1, b.size() - 1)) != null) {
            curr = b.tile(b.size() - 1, b.size() - 1).value();
        }
        if(b.tile(b.size() - 1, b.size() - 2) == null || b.tile(b.size() - 2, b.size() - 1) == null || curr == b.tile(b.size() - 1, b.size() - 2).value() || curr == b.tile(b.size() - 2, b.size() - 1).value()) {
            //System.out.println("4 " + curr);
            return true;
        }
        return false;
    }

    /** Returns the model as a string, used for debugging. */
    @Override
    public String toString() {
        Formatter out = new Formatter();
        out.format("%n[%n");
        for (int row = size() - 1; row >= 0; row -= 1) {
            for (int col = 0; col < size(); col += 1) {
                if (tile(col, row) == null) {
                    out.format("|    ");
                } else {
                    out.format("|%4d", tile(col, row).value());
                }
            }
            out.format("|%n");
        }
        String over = gameOver() ? "over" : "not over";
        out.format("] %d (max: %d) (game is %s) %n", score(), maxScore(), over);
        return out.toString();
    }

    /** Returns whether two models are equal. */
    @Override
    public boolean equals(Object o) {
        if (o == null) {
            return false;
        } else if (getClass() != o.getClass()) {
            return false;
        } else {
            return toString().equals(o.toString());
        }
    }

    /** Returns hash code of Model’s string. */
    @Override
    public int hashCode() {
        return toString().hashCode();
    }
}
