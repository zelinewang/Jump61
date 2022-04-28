# Jump61
CS61B fa21 course work project2

LISENSE: all of code belongs to UC Berkeley EECS Department.

The KJumpingCube game1 is a two-person board game. It is a pure strategy game, 
involving no element of chance. For this third project, you are to implement our version of this game, 
which we'll call jump61b, allowing a user to play against a computer or against another person, or to allow 
the computer to play itself. The tested interface is textual, but for extra credit, you can produce a GUI interface for the game.


The game board consists of an N×N array of squares, where N>1. At any time, each square may have one of three colors: red, blue, or white (neutral), and some number of spots (as on dice). Initially, all squares are white and have one spot.

For purposes of naming squares in this description, we'll use the following notation: r:c refers to the square at row r and column c, where 1≤r,c≤N. Rows are numbered from top to bottom (top row is row 1) and columns are numbered from the left. When entering commands, we replace the colon with a space (this being easier to type).

The neighbors of a square are the horizontally and vertically adjacent squares (diagonally adjacent squares are not neighbors). We say that a square is overfull if it contains more spots than it has neighbors. Thus, the four corner squares are overfull when they have more than two spots; other squares on the edge are overfull with more than three spots; and all others are overfull with more than four spots.

There are two players, whom we'll call Red and Blue. The players each move in turn, with Red going first. A move consists of adding one spot on any square that does not have the opponent's color (so Red may add a spot to either a red or white square). A spot placed on any square colors that square with the player's color.

The order in which this happens, as it turns out, does not usually matter—that is, the end result will be the same regardless of which overfull square's spots are removed first, with the exception that the winning position might differ. A player wins when all squares are the player's color.

The rules hold that the game is over as soon as one player's color covers the board. This is a slightly subtle point: it is easy to set up situations where the procedure given above for dealing with overfull squares loops infinitely, swapping spots around in an endless cycle, unless one is careful to stop as soon as a winning position appears. It is acceptable, in fact, for you to report winning positions in which the redistribution procedure described above is prematurely terminated, so that some squares remain overfull.
