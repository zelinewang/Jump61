package jump61;

/** Describes a source of input commands.  The possible text commands are as
 *  follows (parts of a command are separated by whitespace):
 *    - SIZE s:   Replace the current board with one that is s x s cells.
 *                Then start a new puzzle.  Requires that s be an
 *                integer numeral > 2.
 *    - NEW:      Start a new game with current parameters.
 *                Abandons the current game
 *                (if one is in progress), and clears the board
 *                to its initial configuration
 *                (all squares neutral with one spot, Red's move).
 *    - <row>:<col> Add a spot to the cell at (<row>, <col>).
 *    - UNDO:     Go back one move.
 *    - REDO:     Go forward one previously undone move.
 *    - SEED s:   Set a new random seed.
 *    - QUIT:     Exit the program.
 *    - auto P    Causes player P to be played by an automated
 *                player (an AI) henceforth.
 *                The value P must be red (or r), or blue (or b).
 *                Ignore case—Red, RED, or R) also work.
 *                Initially, Blue is an automated player.
 *    - manual P  Causes player P to take moves entered by the user.
 *                The value of P is as for the auto command.
 *                Initially, Red is a manual player.
 *
 *    Your program should respond to the following textual
 *    commands (you may add others).
 *    There is one command per line, but otherwise, whitespace
 *    may precede and follow command
 *    names and operands freely. Empty lines have no effect,
 *    and a command line whose first
 *    non-blank character is # is ignored as a comment.
 *    Extra arguments to a command (beyond
 *    those specified below) are ignored. An end-of-file
 *    indication on the command input should
 *    have the same effect as the quit command.
 *
 *  @author P. N. Hilfinger
 */
interface CommandSource {

    /** Returns one command string, trimmed of preceding and following
     *  whitespace and converted to upper case.  If the CommandSource
     *  prompts for input, use PROMPT, if not null, to do so. */
    String getCommand(String prompt);

}
