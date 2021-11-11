package jump61;

import java.util.ArrayList;
import java.util.ArrayDeque;
import java.util.Formatter;
import java.util.function.Consumer;

import static jump61.Side.RED;
import static jump61.Side.WHITE;
import static jump61.Side.BLUE;
import static jump61.Square.square;

/** Represents the state of a Jump61 game.  Squares are indexed either by
 *  row and column (between 1 and size()), or by square number, numbering
 *  squares by rows, with squares in row 1 numbered from 0 to size()-1, in
 *  row 2 numbered from size() to 2*size() - 1, etc. (i.e., row-major order).
 *
 *  A Board may be given a notifier---a Consumer<Board> whose
 *  .accept method is called whenever the Board's contents are changed.
 *
 *  @author Zeline Wang
 */
class Board {

    /** An uninitialized Board.  Only for use by subtypes. */
    protected Board() {
        _notifier = NOP;
    }

    /** An N x N board in initial configuration. */
    Board(int N) {
        this();
        _size = N;
        _table = new ArrayList<Square>();
        _numSpots = N * N;
        for (int i = 0; i < N * N; i++) {
            _table.add(square(WHITE, 1));
        }
        _current = 0;
        _history = new ArrayList<>();
        _history.add(new GameState());
        _history.get(0).saveState();
        setNotifier(NOP);
    }

    /** A board whose initial contents are copied from BOARD0, but whose
     *  undo history is clear, and whose notifier does nothing. */
    Board(Board board0) {
        this();
        _size = board0.size();
        _table = new ArrayList<Square>();
        _numSpots = 0;
        for (int i = 0; i < size() * size(); i++) {
            _table.add(square(board0.get(i).getSide(),
                    board0.get(i).getSpots()));
            _numSpots += board0.get(i).getSpots();
        }
        _current = 0;
        _history = new ArrayList<>();
        _history.add(new GameState());
        _history.get(0).saveState();
        setNotifier(NOP);
        _readonlyBoard = new ConstantBoard(this);
    }

    /** Returns a readonly version of this board. */
    Board readonlyBoard() {
        return _readonlyBoard;
    }

    /**
     * @return ind*/
    public ArrayList<Square> getTable() {
        return _table;
    }
    /** (Re)initialize me to a cleared board with N squares on a side. Clears
     *  the undo history and sets the number of moves to
     *  0 (which is _numOfSpot = n*n).
     *  also make notifier do nothing */
    void clear(int N) {
        _size = N;
        _table = new ArrayList<Square>(N);
        _numSpots = N * N;
        for (int i = 0; i < N * N; i++) {
            _table.add(square(WHITE, 1));
        }
        _current = 0;
        _history = new ArrayList<>();
        _history.add(new GameState());
        _history.get(0).saveState();
        setNotifier(NOP);
        announce();
    }

    /** Copy the contents of BOARD into me.
     * everything but whose undo history is clear,
     * and whose notifier does nothing. (which is not announcing)
     * */
    void copy(Board board) {
        clear(board.size());
        _numSpots = board._numSpots;
        _size = board.size();
        for (int i = 0; i < _size * _size; i++) {
            internalSet(i, board.get(i).getSpots(), board.get(i).getSide());
        }
    }

    /** Copy the contents of BOARD into me, without modifying my undo
     *  history.
     *  Assumes BOARD and I have the same size.!!
     *  */
    private void internalCopy(Board board) {
        assert size() == board.size();
        copy(board);
        _current = board._current;
        _history = new ArrayList<>();
        for (int i = 0; i <= _current; i++) {
            _history.add(board._history.get(i));
        }
    }

    /** Return the number of rows and of columns of THIS. */
    int size() {
        return _size;
    }

    /** Returns the contents of the square at row R, column C
     *  1 <= R, C <= size (). */
    Square get(int r, int c) {
        return get(sqNum(r, c));
    }

    /** Returns the contents of square #N, numbering squares by rows, with
     *  squares in row 1 number 0 - size()-1, in row 2 numbered
     *  size() - 2*size() - 1, etc. */
    Square get(int n) {
        if (_table.isEmpty()) {
            return null;
        }
        if (n >= 0 && n < _size * _size) {
            return _table.get(n);
        }
        return null;
    }

    /** Returns the total number of spots on the board.
     *  Use updating board as reference cause there are
     *  no updating functions in set()
     *  numOfSpots don't get changed sometimes
     *  */
    int numPieces() {
        int spotsSum = 0;
        for (int i = 0; i < size() * size(); i++) {
            spotsSum += _table.get(i).getSpots();
        }
        return spotsSum;
    }

    /** Returns the Side of the player who would be next to move.  If the
     *  game is won, this will return the loser (assuming legal position). */
    Side whoseMove() {
        return ((numPieces() + size()) & 1) == 0 ? RED : BLUE;
    }

    /** Return true iff row R and column C denotes a valid square. */
    final boolean exists(int r, int c) {
        return 1 <= r && r <= size() && 1 <= c && c <= size();
    }

    /** Return true iff S is a valid square number. */
    final boolean exists(int s) {
        int N = size();
        return 0 <= s && s < N * N;
    }

    /** Return the row number for square #N. */
    final int row(int n) {
        return n / size() + 1;
    }

    /** Return the column number for square #N. */
    final int col(int n) {
        return n % size() + 1;
    }

    /** Return the square number of row R, column C. */
    final int sqNum(int r, int c) {
        return (c - 1) + (r - 1) * size();
    }

    /** Return a string denoting move (ROW, COL)N. */
    String moveString(int row, int col) {
        return String.format("%d %d", row, col);
    }

    /** Return a string denoting move N. */
    String moveString(int n) {
        return String.format("%d %d", row(n), col(n));
    }

    /** Returns true iff it would currently be legal for PLAYER to add a spot
        to square at row R, column C. */
    boolean isLegal(Side player, int r, int c) {
        return isLegal(player, sqNum(r, c));
    }

    /** Returns true iff it would currently be legal for PLAYER to add a spot
     *  to square #N. */
    boolean isLegal(Side player, int n) {
        return player.playableSquare(get(n).getSide());
    }

    /** Returns true iff PLAYER is allowed to move at this point. */
    boolean isLegal(Side player) {
        return whoseMove() == player;
    }

    /** Returns the winner of the current position, if the game is over,
     *  and otherwise null. */
    final Side getWinner() {
        if (_table == null) {
            System.err.printf("Null table");
            return null;
        }
        if (numOfBlue() == size() * size()) {
            return BLUE;
        } else if (numOfRed() == size() * size()) {
            return RED;
        } else {
            return null;
        }
    }

    /** Return the number of squares of given SIDE. */
    int numOfSide(Side side) {
        int num = 0;
        for (int i = 0; i < _size * _size; i++) {
            if (get(i).getSide() == side) {
                num++;
            }
        }
        return num;
    }
    /** Add a spot from PLAYER at row R, column C.  Assumes
     *  isLegal(PLAYER, R, C).
     *  @return dddd*/
    int numOfRed() {
        return numOfSide(RED);
    }
    /** Add a spot from PLAYER at row R, column C.  Assumes
     *  isLegal(PLAYER, R, C).
     *  @return dddd*/
    int numOfBlue() {
        return numOfSide(BLUE);
    }

    /** Add a spot from PLAYER at row R, column C.  Assumes
     *  isLegal(PLAYER, R, C). */
    void addSpot(Side player, int r, int c) {
        if (!exists(r, c)) {
            return;
        }
        if (!isLegal(player, r, c)) {
            return;
        }
        set(r, c, get(r, c).getSpots() + 1, player);
        _numSpots++;
        if (ifWin()) {
            return;
        }
        if (isOverfull(r, c)) {
            jump(sqNum(r, c));
        }
        markUndo();
    }
    /** if the square.
     * @return rine*/
    public boolean ifWin() {
        return getWinner() != null;
    }

    /** Add a spot from PLAYER at square #N.  Assumes isLegal(PLAYER, N). */
    void addSpot(Side player, int n) {
        addSpot(player, row(n), col(n));
    }
    /** if the square.
     * @param r int
     * @param c int
     * @return rine*/
    boolean isSquare(int r, int c) {
        return r >= 1 && r <= size() && c >= 1 && c <= size();
    }
    /** if the square.
     * @param n int
     * @return rine*/
    boolean isSquare(int n) {
        return n >= 0 && n < size() * size();
    }

    /** if the square is overfull.
     * @param n in
     * @return in */
    boolean isOverfull(int n) {
        return get(n).getSpots() > neighbors(n);
    }

    /** if the square is overfull.
     * @param r int
     * @param c int
     * @return inte*/
    boolean isOverfull(int r, int c) {
        return get(r, c).getSpots() > neighbors(r, c);
    }

    /** Set the square at row R, column C to NUM spots (0 <= NUM), and give
     *  it color PLAYER if NUM > 0 (otherwise, white). */
    void set(int r, int c, int num, Side player) {
        internalSet(r, c, num, player);
        announce();
    }
    /** Set the square at row R, column C to NUM spots (0 <= NUM), and give
     *  it color PLAYER if NUM > 0 (otherwise, white).
     *  @param n int
     *  @param num int
     *  @param player side*/
    void set(int n, int num, Side player) {
        internalSet(n, num, player);
        announce();
    }

    /** Set the square at row R, column C to NUM spots (0 <= NUM), and give
     *  it color PLAYER if NUM > 0 (otherwise, white).  Does not announce
     *  changes. */
    private void internalSet(int r, int c, int num, Side player) {
        internalSet(sqNum(r, c), num, player);
    }

    /** Set the square #N to NUM spots (0 <= NUM), and give it color PLAYER
     *  if NUM > 0 (otherwise, white). Does not announce changes. */
    private void internalSet(int n, int num, Side player) {
        if (num == 0) {
            _table.remove(n);
            _table.add(n, square(WHITE, num));
        } else if (num > 0) {
            _table.remove(n);
            _table.add(n, square(player, num));
        }
    }

    /** Undo the effects of one move (that is, one addSpot command).  One
     *  can only undo back to the last point at which the undo history
     *  was cleared, or the construction of this Board. */
    void undo() {
        if (_current > 0) {
            _current -= 1;
            _history.get(_current).restoreState();
        }
    }

    /** Record the beginning of a move in the undo history. */
    private void markUndo() {
        _history.add(new GameState());
        _current++;
        _history.get(_current).saveState();
    }

    /** Add DELTASPOTS spots of side PLAYER to row R, column C,
     *  updating counts of numbers of squares of each color. */
    private void simpleAdd(Side player, int r, int c, int deltaSpots) {
        internalSet(r, c, deltaSpots + get(r, c).getSpots(), player);
    }

    /** Add DELTASPOTS spots of color PLAYER to square #N,
     *  updating counts of numbers of squares of each color. */
    private void simpleAdd(Side player, int n, int deltaSpots) {
        internalSet(n, deltaSpots + get(n).getSpots(), player);
    }

/************************************************************************/
    private class GameState extends Board {

        /** A holder for the _cells and _active instance variables of this
         *  Model. */
        GameState() {
            _savedTable = new ArrayList<Square>();
        }

        /** Initialize to the current state of the Model. */
        void saveState() {
            _savedTable.clear();
            _savedTable.addAll(_table);
        }

        /** Restore the current Model's state from our saved state. */
        void restoreState() {
            _table.clear();
            _table.addAll(_savedTable);
        }

        /** Contents of board. */
        private ArrayList<Square> _savedTable;
    }

    /** A sequence of puzzle states.
     * add GAMESTATE after a player's move, which is after addSpot.
     * Save the statues of the BOARD, which is the _table.
     * */
    private ArrayList<GameState> _history = new ArrayList<GameState>();

    /** The position of the current state in _history.  This is always
     *  non-negative and <=_lastHistory.
     *  from 0 to ...
     *  */
    private int _current;

/**********************************************************************
 *
 * @return arraryd
 * **/
    public ArrayList<GameState> getHistory() {
        return _history;
    }

    /** Used in jump to keep track of squares needing processing.  Allocated
     *  here to cut down on allocations. */
    private final ArrayDeque<Integer> _workQueue = new ArrayDeque<>();

    /** Do all jumping on this board, assuming that initially, S is the only
     *  square that might be over-full. */
    private void jump(int S) {
        if (isOverfull(S)) {
            move(S);
        }
        updateWorkQueue();
        if (ifWin()) {
            _workQueue.clear();
        }
        if (_workQueue.isEmpty()) {
            return;
        } else {
            jump(_workQueue.pollFirst());
        }
    }

    /** Update what is in the Queue of Jumping process. */
    private void updateWorkQueue() {
        for (int i = 0; i < size() * size(); i++) {
            if (isOverfull(i) && !_workQueue.contains(i)) {
                _workQueue.add(i);
            }
        }
    }

    /** if one square is overfull, then move the spots to neighbors.
     *  same as addSpot() but not changing _numSpot and _turn, and whoseMove
     *  does nothing if r,c is not a square or the r,c is not overfull
     * @param n int
     *  */
    void move(int n) {
        move(row(n), col(n));
    }
    /** if one square is overfull, then move the spots to neighbors.
     *  same as addSpot() but not changing _numSpot and _turn, and whoseMove
     *  does nothing if r,c is not a square or the r,c is not overfull
     *  @param r int
     *  @param c int
     *  */
    void move(int r, int c) {
        if (isSquare(r, c)) {
            Side player = get(r, c).getSide();
            if (isOverfull(r, c)) {
                set(r, c, get(r, c).getSpots() - neighbors(r, c), player);
                if (isSquare(r - 1, c)) {
                    set(r - 1, c, get(r - 1, c).getSpots() + 1, player);
                }
                if (isSquare(r + 1, c)) {
                    set(r + 1, c, get(r + 1, c).getSpots() + 1, player);
                }
                if (isSquare(r, c - 1)) {
                    set(r, c - 1, get(r, c - 1).getSpots() + 1, player);
                }
                if (isSquare(r, c + 1)) {
                    set(r, c + 1, get(r, c + 1).getSpots() + 1, player);
                }
            }
        }
    }

    /** Returns my dumped representation.
     *
     * hint: toString() in project0
     *
     *     @Override
     *     public String toString() {
     *         Formatter out = new Formatter();
     *         Set<Place> marked = markedCells();  //return _readonlycells!
     *         String sep;
     *         for (int row = 0; row < height(); row += 1) {
     *             for (int col = 0; col < width(); col += 1) {
     *                 sep = "";
     *                 if (marked.contains(pl(row, col))) {
     *                     out.format("%s*%d", sep, get(row, col));
     *                 } else {
     *                     out.format("%s%2d", sep, get(row, col));
     *                 }
     *                 sep = " ";
     *             }
     *             out.format("%n");
     *         }
     *         return out.toString();
     *     }
     *     */
    @Override
    public String toString() {
        Formatter out = new Formatter();
        String sep;
        out.format("===%n");
        for (int row = 1; row < size() + 1; row++) {
            out.format("   ");
            for (int col = 1; col < size() + 1; col++) {
                sep = " ";
                if (this.exists(row, col)) {
                    Square square = this.get(row, col);
                    if (square.getSide() == WHITE) {
                        out.format("%s%d" + "-", sep, square.getSpots());
                    } else if (square.getSide() == RED) {
                        out.format("%s%d" + "r", sep, square.getSpots());
                    } else if (square.getSide() == BLUE) {
                        out.format("%s%d" + "b", sep, square.getSpots());
                    }
                } else {
                    System.err.printf("board is not completed!");
                }
                sep = "";
            }
            out.format("%n");
        }
        out.format("===%n");
        return out.toString();
    }

    /** Returns an external rendition of me, suitable for human-readable
     *  textual display, with row and column numbers.  This is distinct
     *  from the dumped representation (returned by toString). */
    public String toDisplayString() {
        String[] lines = toString().trim().split("\\R");
        Formatter out = new Formatter();
        for (int i = 1; i + 1 < lines.length; i += 1) {
            out.format("%2d %s%n", i, lines[i].trim());
        }
        out.format("  ");
        for (int i = 1; i <= size(); i += 1) {
            out.format("%3d", i);
        }
        return out.toString();
    }

    /** Returns the number of neighbors of the square at row R, column C. */
    int neighbors(int r, int c) {
        int size = size();
        int n;
        n = 0;
        if (r > 1) {
            n += 1;
        }
        if (c > 1) {
            n += 1;
        }
        if (r < size) {
            n += 1;
        }
        if (c < size) {
            n += 1;
        }
        return n;
    }

    /** Returns the number of neighbors of square #N. */
    int neighbors(int n) {
        return neighbors(row(n), col(n));
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof Board)) {
            return false;
        } else {
            Board B = (Board) obj;
            if (this.size() != B.size() || this.numPieces() != B.numPieces()) {
                return false;
            }
            for (int i = 0; i < size() * size(); i++) {
                if (this.get(i).getSide() != B.get(i).getSide()
                        || this.get(i).getSpots() != B.get(i).getSpots()) {
                    return false;
                }
            }
            return true;
        }
    }

    @Override
    public int hashCode() {
        return numPieces();
    }

    /** Set my notifier to NOTIFY. */
    public void setNotifier(Consumer<Board> notify) {
        _notifier = notify;
        announce();
    }

    /** Take any action that has been set for a change in my state.
     *  For GUI */
    private void announce() {
        _notifier.accept(this);
    }

    /** A notifier that does nothing. */
    private static final Consumer<Board> NOP = (s) -> { };

    /** A read-only version of this Board. */
    private ConstantBoard _readonlyBoard;

    /** Use _notifier.accept(B) to announce changes to this board.
     * for GUI
     * */
    private Consumer<Board> _notifier;

    /** Size of the board. */
    private int _size;

    /** The storage of squares in a board. */
    private ArrayList<Square> _table;

    /** Total spots in the board. */
    private int _numSpots;
}
