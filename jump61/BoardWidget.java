package jump61;

import ucb.gui2.Pad;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.event.MouseEvent;

import java.util.concurrent.ArrayBlockingQueue;

import static jump61.Side.*;

/** A GUI component that displays a Jump61 board, and converts mouse clicks
 *  on that board to commands that are sent to the current Game.
 *  @author Zeline Wang
 */
class BoardWidget extends Pad {

    /** Length of the side of one square in pixels. */
    private static final int SQUARE_SIZE = 50;
    /** Width and height of a spot. */
    private static final int SPOT_DIM = 8;
    /** Minimum separation of center of a spot from a side of a square. */
    private static final int SPOT_MARGIN = 10;
    /** Width of the bars separating squares in pixels. */
    private static final int SEPARATOR_SIZE = 3;
    /** Width of square plus one separator. */
    private static final int SQUARE_SEP = SQUARE_SIZE + SEPARATOR_SIZE;

    /** Colors of various parts of the displayed board. */
    private static final Color
        NEUTRAL = Color.WHITE,
        SEPARATOR_COLOR = Color.BLACK,
        SPOT_COLOR = Color.BLACK,
        RED_TINT = new Color(255, 200, 200),
        BLUE_TINT = new Color(200, 200, 255);

    /** A new BoardWidget that monitors and displays a game Board, and
     *  converts mouse clicks to commands to COMMANDQUEUE. */
    BoardWidget(ArrayBlockingQueue<String> commandQueue) {
        _commandQueue = commandQueue;
        _side = 6 * SQUARE_SEP + SEPARATOR_SIZE;
        setMouseHandler("click", this::doClick);
    }

    /* .update and .paintComponent are synchronized because they are called
     *  by three different threads (the main thread, the thread that
     *  responds to events, and the display thread).  We don't want the
     *  saved copy of our Board to change while it is being displayed. */

    /** Update my display to show BOARD.  Here, we save a copy of
     *  BOARD (so that we can deal with changes to it only when we are ready
     *  for them), and recompute the size of the displayed board. */
    synchronized void update(Board board) {
        if (board.equals(_board)) {
            return;
        }
        if (_board != null && _board.size() != board.size()) {
            invalidate();
        }
        _board = new Board(board);
        _side = _board.size() * SQUARE_SEP + SEPARATOR_SIZE;
    }

    @Override
    public Dimension getPreferredSize() {
        return new Dimension(_side, _side);
    }

    @Override
    public Dimension getMaximumSize() {
        return new Dimension(_side, _side);
    }

    @Override
    public Dimension getMinimumSize() {
        return new Dimension(_side, _side);
    }

    @Override
    public synchronized void paintComponent(Graphics2D g) {
        if (_board == null) {
            return;
        }
        update(_board);
        g.setColor(SEPARATOR_COLOR);
        g.fillRect(0, 0, SEPARATOR_SIZE, _side);
        g.fillRect(0, 0, _side, SEPARATOR_SIZE);

        for (int i = 1; i <= _board.size(); i += 1) {
            g.setColor(SEPARATOR_COLOR);
            g.fillRect(i * SQUARE_SEP, 0, SEPARATOR_SIZE, _side);
            g.fillRect(0, i * SQUARE_SEP, _side, SEPARATOR_SIZE);
            for (int j = 1; j <= _board.size(); j += 1) {
                renderSquare(g, i, j);
            }
        }
    }

    /** Color and display the square at row R and column C
     *  on G.  (Used by paintComponent). */
    private void renderSquare(Graphics2D g, int r, int c) {
        update(_board);
        if (_board.get(r, c).getSide() == RED) {
            g.setColor(Color.RED.brighter().brighter());
        } else if (_board.get(r, c).getSide() == BLUE) {
            g.setColor(Color.BLUE.brighter().brighter());
        } else if (_board.get(r, c).getSide() == WHITE) {
            g.setColor(Color.WHITE.brighter().brighter());
        }
        int x = SEPARATOR_SIZE + SQUARE_SEP * (r - 1);
        int y = SEPARATOR_SIZE + SQUARE_SEP * (c - 1);
        g.fillRect(x, y, SQUARE_SIZE, SQUARE_SIZE);
        displaySpots(g, r, c);
        repaint();
    }

    /** Color and display the spots on the square at row R and column C
     *  on G.  (Used by paintComponent). */
    private void displaySpots(Graphics2D g, int r, int c) {
        update(_board);
        if (_board.getWinner() != null) {
            return;
        }
        int x0 = SEPARATOR_SIZE + SQUARE_SEP * (r - 1);
        int y0 = SEPARATOR_SIZE + SQUARE_SEP * (c - 1);
        int x = x0;
        int y = y0;
        int spots = _board.get(r, c).getSpots();
        switch (spots) {
        case 1 :
            x += SQUARE_SIZE / 2;
            y += SQUARE_SIZE / 2;
            spot(g, x, y);
            break;
        case 2 :
            for (int i = 0; i < 2; i += 1) {
                x += SQUARE_SIZE / 3;
                y += SQUARE_SIZE / 3;
                spot(g, x, y);
            }
            break;
        case 3 :
            for (int i = 0; i < 3; i += 1) {
                x += SQUARE_SIZE / 4;
                y += SQUARE_SIZE / 4;
                spot(g, x, y);
            }
            break;
        case 4 :
            int foo = SQUARE_SIZE / 3;
            x += foo;
            y += foo;
            spot(g, x, y);
            spot(g, x + foo, y);
            spot(g, x, y + foo);
            spot(g, x + foo, y + foo);
            break;
        default:
            return;
        }
    }

    /** Draw one spot centered at position (X, Y) on G. */
    private void spot(Graphics2D g, int x, int y) {
        g.setColor(SPOT_COLOR);
        g.fillOval(x - SPOT_DIM / 2, y - SPOT_DIM / 2, SPOT_DIM, SPOT_DIM);
    }

    /** Respond to the mouse click depicted by EVENT. */
    public void doClick(String dummy, MouseEvent event) {
        int x = event.getX() - SEPARATOR_SIZE,
            y = event.getY() - SEPARATOR_SIZE;
        int r = x / SQUARE_SEP + 1;
        int c = y / SQUARE_SEP + 1;
        _commandQueue.offer(String.format("%d %d", r, c));
    }

    /** The Board I am displaying. */
    private Board _board;
    /** Dimension in pixels of one side of the board. */
    private int _side;
    /** Destination for commands derived from mouse clicks. */
    private ArrayBlockingQueue<String> _commandQueue;
}
