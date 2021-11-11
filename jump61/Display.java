package jump61;

import ucb.gui2.TopLevel;
import ucb.gui2.LayoutSpec;

import java.util.concurrent.ArrayBlockingQueue;

import static jump61.Side.*;

/** The GUI controller for jump61.  To require minimal change to textual
 *  interface, we adopt the strategy of converting GUI input (mouse clicks)
 *  into textual commands that are sent to the Game object through a
 *  a Writer.  The Game object need never know where its input is coming from.
 *  A Display is an Observer of Games and Boards so that it is notified when
 *  either changes.
 *  @author Zeline Wang
 */
class Display extends TopLevel implements View, CommandSource, Reporter {

    /** A new window with given TITLE displaying GAME, and using COMMANDWRITER
     *  to send commands to the current game. */
    Display(String title) {
        super(title, true);
        addMenuButton("Game->Quit", this::quit);
        addMenuButton("Game->New Game", this::newGame);
        addMenuButton("Game->Restart", this::restartGame);
        addSeparator("Game");
        addMenuButton("Game->Undo", this::undo);
        addSeparator("Game");
        addMenuButton("Size->2*2", this::resize2);
        addMenuButton("Size->3*3", this::resize3);
        addMenuButton("Size->4*4", this::resize4);
        addMenuButton("Size->5*5", this::resize5);
        addMenuButton("Size->6*6", this::resize6);

        addMenuButton("RED->AI", this::autoRed);
        addMenuButton("BLUE->AI", this::autoBlue);
        addMenuButton("RED->manual", this::manualRed);
        addMenuButton("BLUE->manual", this::manualBlue);

        _boardWidget = new BoardWidget(_commandQueue);
        add(_boardWidget, new LayoutSpec("y", 1, "width", 2));
        display(true);
    }

    /** Response to "Quit" button click. */
    void quit(String dummy) {
        System.exit(0);
    }

    /** Response to "New Game" button click. */
    void newGame(String dummy) {
        _commandQueue.offer("new");
    }

    /** Response to "Restart" button click. */
    void restartGame(String dummy) {
        _commandQueue.offer("restartGame");
    }

    /** Response to "Undo" button click. */
    void undo(String dummy) {
        _commandQueue.offer("undo");
    }

    /** A thing.
     * @param dummy shabi*/
    void resize2(String dummy) {
        _commandQueue.offer("size 2");
    }
    /** A thing.
     * @param dummy shabi*/
    void resize3(String dummy) {
        _commandQueue.offer("size 3");
    }
    /** A thing.
     * @param dummy shabi*/
    void resize4(String dummy) {
        _commandQueue.offer("size 4");
    }
    /** A thing.
     * @param dummy shabi*/
    void resize5(String dummy) {
        _commandQueue.offer("size 5");
    }
    /** A thing.
     * @param dummy shabi*/
    void resize6(String dummy) {
        _commandQueue.offer("size 6");
    }
    /** A thing.
     * @param dummy shabi*/
    void autoRed(String dummy) {
        _commandQueue.offer("auto red");
    }
    /** A thing.
     * @param dummy shabi*/
    void autoBlue(String dummy) {
        _commandQueue.offer("auto blue");
    }
    /** A thing.
     * @param dummy shabi*/
    void manualRed(String dummy) {
        _commandQueue.offer("manual red");
    }
    /** A thing.
     * @param dummy shabi*/
    void manualBlue(String dummy) {
        _commandQueue.offer("manual blue");
    }

    @Override
    public void update(Board board) {
        _boardWidget.repaint();
        _boardWidget.update(board);
        pack();
        _boardWidget.repaint();
    }

    @Override
    public String getCommand(String ignored) {
        try {
            return _commandQueue.take();
        } catch (InterruptedException excp) {
            throw new Error("unexpected interrupt");
        }
    }

    @Override
    public void announceWin(Side side) {
        showMessage(String.format("%s wins!", side.toCapitalizedString()),
                "Game Over", "information");
    }

    @Override
    public void announceMove(int row, int col) {
    }

    @Override
    public void msg(String format, Object... args) {
        showMessage(String.format(format, args), "", "information");
    }

    @Override
    public void err(String format, Object... args) {
        showMessage(String.format(format, args), "Error", "error");
    }

    /** Time interval in msec to wait after a board update. */
    static final long BOARD_UPDATE_INTERVAL = 50;

    /** The widget that displays the actual playing board. */
    private BoardWidget _boardWidget;
    /** Queue for commands going to the controlling Game. */
    private final ArrayBlockingQueue<String> _commandQueue =
            new ArrayBlockingQueue<>(5);
}

