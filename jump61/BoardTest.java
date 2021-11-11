package jump61;

import static jump61.Side.*;

import org.junit.Test;
import static org.junit.Assert.*;

/** Unit tests of Boards.
 * You can find some individual unit tests in the BoardTest.java class.
 * Remember, passing these tests do not ensure your project is correct.
 * You should write more unit tests to assist with debugging and ensure
 * correctness.
 *  @author Zeline Wang
 */

public class BoardTest {

    private static final String NL = System.getProperty("line.separator");

    @Test
    public void testBoard() {
        Board B = new Board(5);
        assertEquals("bad initialization", Square.square(WHITE, 1), B.get(1));
        assertEquals("bad initialization", Square.square(WHITE, 1), B.get(3));
        assertEquals("bad initialization", Square.square(WHITE, 1), B.get(0));
        assertEquals("bad initialization", Square.square(WHITE, 1), B.get(5));
        assertEquals("bad initialization", Square.square(WHITE, 1), B.get(24));
        assertEquals("bad initialization", null, B.get(25));
        assertEquals("bad initialization", null, B.get(30));
    }

    @Test
    public void testSize() {
        Board B = new Board(5);
        assertEquals("bad length", 5, B.size());
        Board E = new Board(10);
        assertEquals("bad length", 10, E.size());
        ConstantBoard C = new ConstantBoard(B);
        assertEquals("bad length", 5, C.size());
        Board D = new Board(C);
        assertEquals("bad length", 5, D.size());
    }

    @Test
    public void testSet() {
        Board B = new Board(5);
        B.set(2, 2, 1, RED);
        assertEquals("wrong number of spots", 1, B.get(2, 2).getSpots());
        assertEquals("wrong color", RED, B.get(2, 2).getSide());
        assertEquals("wrong count", 1, B.numOfSide(RED));
        assertEquals("wrong count", 0, B.numOfSide(BLUE));
        assertEquals("wrong count", 24, B.numOfSide(WHITE));
    }

    @Test
    public void testClear() {
        Board B = new Board(6);
        checkBoard("#0", B);
        B.addSpot(RED, 1, 1);
        checkBoard("#1", B, 1, 1, 2, RED);
        B.addSpot(BLUE, 2, 1);
        checkBoard("#2", B, 1, 1, 2, RED, 2, 1, 2, BLUE);
        B.addSpot(RED, 1, 1);
        B.clear(5);
        assertEquals("bad clear", Square.square(WHITE, 1), B.get(1));
        assertEquals("bad clear", Square.square(WHITE, 1), B.get(3));
        assertEquals("bad clear", Square.square(WHITE, 1), B.get(0));
        assertEquals("bad clear", Square.square(WHITE, 1), B.get(5));
        assertEquals("bad clear", Square.square(WHITE, 1), B.get(24));
        assertEquals("bad clear", null, B.get(25));
        assertEquals("bad clear", null, B.get(30));
        B.undo();
        assertEquals("bad clear", Square.square(WHITE, 1), B.get(1));
        assertEquals("bad clear", Square.square(WHITE, 1), B.get(3));
        assertEquals("bad clear", Square.square(WHITE, 1), B.get(0));
        assertEquals("bad clear", Square.square(WHITE, 1), B.get(5));
        assertEquals("bad clear", Square.square(WHITE, 1), B.get(24));
        assertEquals("bad clear", null, B.get(25));
        assertEquals("bad clear", null, B.get(30));
    }

    @Test
    public void testCopy() {
        Board B = new Board(6);
        checkBoard("#0", B);
        B.addSpot(RED, 1, 1);
        checkBoard("#1", B, 1, 1, 2, RED);
        B.addSpot(BLUE, 2, 1);
        checkBoard("#2", B, 1, 1, 2, RED, 2, 1, 2, BLUE);
        B.addSpot(RED, 1, 1);
        checkBoard("#3", B, 1, 1, 1, RED, 2, 1, 3, RED, 1, 2, 2, RED);
        B.toDisplayString();
        Board A = new Board(5);
        A.toDisplayString();
        A.copy(B);
        A.toDisplayString();
        checkBoard("#3C", A, 1, 1, 1, RED, 2, 1, 3, RED, 1, 2, 2, RED);
        assertTrue(A.getHistory().size() == 1);
    }

    @Test
    public void testMove() {
        Board B = new Board(6);
        checkBoard("#0", B);
        B.addSpot(RED, 1, 1);
        System.out.printf(B.toString());
        checkBoard("#1", B, 1, 1, 2, RED);
        B.addSpot(BLUE, 2, 1);
        System.out.printf(B.toString());
        checkBoard("#2", B, 1, 1, 2, RED, 2, 1, 2, BLUE);
        B.addSpot(RED, 1, 1);
        System.out.printf(B.toString());
        checkBoard("#3", B, 1, 1, 1, RED, 2, 1, 3, RED, 1, 2, 2, RED);
        B.undo();
        System.out.printf(B.toString());
        checkBoard("#2U", B, 1, 1, 2, RED, 2, 1, 2, BLUE);
        B.undo();
        System.out.printf(B.toString());
        checkBoard("#1U", B, 1, 1, 2, RED);
        B.undo();
        System.out.printf(B.toString());
        checkBoard("#0U", B);
    }


    /** Checks that B conforms to the description given by CONTENTS.
     *  CONTENTS should be a sequence of groups of 4 items:
     *  r, c, n, s, where r and c are row and column number of a square of B,
     *  n is the number of spots that are supposed to be there and s is the
     *  color (RED or BLUE) of the square.  All squares not listed must
     *  be WHITE with one spot.  Raises an exception signaling a unit-test
     *  failure if B does not conform. */
    private void checkBoard(String msg, Board B, Object... contents) {
        for (int k = 0; k < contents.length; k += 4) {
            String M = String.format("%s at %d %d", msg, contents[k],
                                     contents[k + 1]);
            assertEquals(M, (int) contents[k + 2],
                         B.get((int) contents[k],
                               (int) contents[k + 1]).getSpots());
            assertEquals(M, contents[k + 3],
                         B.get((int) contents[k],
                               (int) contents[k + 1]).getSide());
        }
        int c;
        c = 0;
        for (int i = B.size() * B.size() - 1; i >= 0; i -= 1) {
            assertTrue("bad white square #" + i,
                       (B.get(i).getSide() != WHITE)
                       || (B.get(i).getSpots() == 1));
            if (B.get(i).getSide() != WHITE) {
                c += 1;
            }
        }
        assertEquals("extra squares filled", contents.length / 4, c);
    }

}
