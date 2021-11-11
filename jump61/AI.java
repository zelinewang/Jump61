package jump61;

import java.util.ArrayList;
import java.util.Random;

import static jump61.Side.*;

/** An automated Player.
 *  @author Zeline Wang
 */
class AI extends Player {
    /** An AI player choosing the best move
     *  from depth 4.
     *  @param game game
     *  @param color the color
     *  @param seed random seed, kinda unless, depends on you
     *  */
    AI(Game game, Side color, long seed) {
        super(game, color);
        _random = new Random(seed);
    }
    @Override
    String getMove() {
        Board board = getGame().getBoard();
        assert getSide() == board.whoseMove();
        int choice = searchForMove();
        getGame().reportMove(board.row(choice), board.col(choice));
        return String.format("%d %d", board.row(choice), board.col(choice));
    }



    /** Return a move after searching the game tree to DEPTH>0 moves
     *  from the current position. Assumes the game is not over. */
    private int searchForMove() {
        Board work = new Board(getBoard());
        int value;
        assert getSide() == work.whoseMove();
        _foundMove = -1;
        if (getSide() == RED) {
            value = minMax(work, 4, true,
                    1, Integer.MIN_VALUE, Integer.MAX_VALUE);
        } else {
            value = minMax(work, 4, true,
                    -1, Integer.MIN_VALUE, Integer.MAX_VALUE);
        }
        return _foundMove;
    }

    /** Returns valid moves for current player.
     * @param board find from
     * @param color now player
     * */
    ArrayList<Integer> finder(Board board, Side color) {
        ArrayList<Integer> res = new ArrayList<>();
        for (int i = 0; i < board.size() * board.size(); i++) {
            if (board.isLegal(color, i)) {
                res.add(i);
            }
        }
        return res;
    }

    /**Through Game Tree finding the min and Max.
     * @param alpha the min
     * @param beta the max
     * @param board the working current board
     * @param depth Tree depth
     * @param saveMove save the move or just testing
     * @param sense sense == 1 is RED, -1 is BLUE
     * @return min or max
     * */
    private int minMax(Board board, int depth, boolean saveMove,
                       int sense, int alpha, int beta) {
        Board copy = new Board(board);
        if (depth == 0) {
            return staticEval(board, Integer.MAX_VALUE);
        } else {
            if (sense == 1) {
                if (board.getWinner() == BLUE) {
                    return Integer.MIN_VALUE;
                }
                int best = Integer.MIN_VALUE;
                for (int i : finder(copy, RED)) {
                    Board test = new Board(copy);
                    Side player = test.whoseMove();
                    test.addSpot(player, i);
                    int response = minMax(test, depth - 1,
                            false, -1, alpha, beta);
                    best = Math.max(best, response);
                    alpha = Math.max(alpha, best);
                    if (response == best && saveMove) {
                        _foundMove = i;
                    }
                    if (alpha >= beta) {
                        break;
                    }
                }
                return best;
            } else  {
                if (board.getWinner() == RED) {
                    return Integer.MAX_VALUE;
                }
                int best = Integer.MAX_VALUE;
                for (int i : finder(copy, BLUE)) {
                    Board test = new Board(copy);
                    Side player = test.whoseMove();
                    test.addSpot(player, i);
                    int response = minMax(test, depth - 1,
                            false, 1, alpha, beta);
                    best = Math.min(best, response);
                    beta = Math.min(beta, best);
                    if (response == best && saveMove) {
                        _foundMove = i;
                    }
                    if (beta <= alpha) {
                        break;
                    }
                }
                return best;
            }
        }
    }

    /** Return a heuristic estimate of the value of board position B.
     *  Use WINNINGVALUE to indicate a win for Red and -WINNINGVALUE to
     *  indicate a win for Blue. */
    private int staticEval(Board b, int winningValue) {
        int redCount = b.numOfSide(RED);
        int blueCount = b.numOfSide(BLUE);
        if (redCount == b.size() * b.size()) {
            return winningValue;
        } else if (blueCount == b.size() * b.size()) {
            return -winningValue;
        } else {
            return redCount - blueCount;
        }
    }

    /** A random-number generator used for move selection. */
    private Random _random;

    /** Used to convey moves discovered by minMax. */
    private int _foundMove;
}
