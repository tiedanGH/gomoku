package main.ai;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import main.utils.DoubleFree;

// This class is responsible for generating the candidate move list
public class Candidate {

    // all candidate coordinates
    public ArrayList<Coordinate> list = new ArrayList<>();

    // Double-free-three detector, used for rule-based filtering
    static public DoubleFree doubleFreeThree = new DoubleFree();

    // Predefined ordering rules for candidate sorting
    private final List<Double> order = Arrays.asList(
            6.0, 5.0, 4.0, 4.8, 4.7,
            3.8, 3.7, 3.5, 3.4, 3.0,
            2.8, 2.5, 2.4, 2.0, 1.0, 0.0
    );

    // predefined responses for some opening directions
    private static final int [][][] openCan = {
            {{0, -1}, {1, -1}, {1, 0}},
            {{-1, 0}, {-1, -1}, {0, -1}},
            {{-1, 0}, {-1, 1}, {0, 1}},
            {{0, 1}, {1, 1}, {1, 0}}
    };

    // Dynamically maintained upper and lower bounds of the active area
    private static Coordinate limitMax = new Coordinate(1, 1);
    private static Coordinate limitMin = new Coordinate(18, 18);

    static public boolean display = false;

    private int turn;       // Current player
    private int threshold;  // Number of candidates with strength >= 3, used to control pruning scale

    public boolean capturePossible; // Whether double-free checking is enabled

    // Inner class Coordinate
    public static class Coordinate {
        public int x;
        public int y;
        public double strength;

        public Coordinate(int x, int y) {
            this.x = x;
            this.y = y;
        }

        public Coordinate(int x, int y, double strength) {
            this.x = x;
            this.y = y;
            this.strength = strength;
        }

        public double st() {
            return this.strength;
        }
    }

    public Candidate(boolean cap) {
        this.capturePossible = cap;
    }


    // Utility functions

    // Check if the cell contains a player piece
    private boolean isPlayer(int c) {
        return c == 1 || c == 2;
    }

    // Check whether two cells are the same and non-empty
    private boolean isSameNonZero(int a, int b) {
        if (a == b)
            return a != 0;
        return false;
    }

    // Check whether (i,j) is between two identical stones
    private int innerAlignment(int i, int j) {
        // Horizontal sandwich
        if (j+1 != 19 && j-1 != -1 && isSameNonZero(MinimaxEngine.board[i][j + 1], MinimaxEngine.board[i][j - 1])) {
            return MinimaxEngine.board[i][j + 1];
        }
        // Vertical sandwich
        if (i+1 != 19 && i-1 != -1 && isSameNonZero(MinimaxEngine.board[i + 1][j], MinimaxEngine.board[i - 1][j])) {
            return MinimaxEngine.board[i + 1][j];
        }
        // Diagonal sandwich (bottom-left to top-right)
        if (i+1 != 19 && i-1 != -1 && j+1 != 19 && j-1 != -1 && isSameNonZero(MinimaxEngine.board[i+1][j-1], MinimaxEngine.board[i-1][j+1])) {
            return MinimaxEngine.board[i+1][j-1];
        }
        // Diagonal sandwich (top-left to bottom-right)
        if (i+1 != 19 && i-1 != -1 && j+1 != 19 && j-1 != -1 && isSameNonZero(MinimaxEngine.board[i-1][j-1], MinimaxEngine.board[i+1][j+1])) {
            return MinimaxEngine.board[i-1][j-1];
        }

        return 0;
    }

    // Count whether there are any player stones around (i,j) in 8-neighborhood
    private int nearCount(int i, int j) {
        int cmp = 0;
        if (j+1 != 19 && isPlayer(MinimaxEngine.board[i][j + 1])) cmp++;
        if (j-1 != -1 && isPlayer(MinimaxEngine.board[i][j - 1])) cmp++;
        if (i+1 != 19 && isPlayer(MinimaxEngine.board[i + 1][j])) cmp++;
        if (i-1 != -1 && isPlayer(MinimaxEngine.board[i - 1][j])) cmp++;
        if (i+1 != 19 && j-1 != -1 && isPlayer(MinimaxEngine.board[i+1][j-1])) cmp++;
        if (i-1 != -1 && j-1 != -1 && isPlayer(MinimaxEngine.board[i-1][j-1])) cmp++;
        if (i+1 != 19 && j+1 != 19 && isPlayer(MinimaxEngine.board[i+1][j+1])) cmp++;
        if (i-1 != -1 && j+1 != 19 && isPlayer(MinimaxEngine.board[i-1][j+1])) cmp++;
        return cmp;
    }

    // Reset the candidate search boundaries
    public void reloadLimits() {
        limitMax = new Coordinate(1, 1);
        limitMin = new Coordinate(18, 18);
    }

    // Update the boundaries: any occupied cell expands the limits
    private void loadLimits(int [][] map) {
        for (int i = 0 ; i < 19 ; i++) {
            for (int j = 0 ; j < 19 ; j++) {
                if (isPlayer(map[i][j]))
                    save(new Coordinate(i, j));
            }
        }
    }

    // Add a candidate and update threshold
    private void addCandidate(int x, int y, double val) {
        if (val >= 3)
            this.threshold+=1;

        this.list.add(new Coordinate(x, y, val));
    }

    // Generate candidate score for a given point using Evaluator
    private void loadCase(int x, int y, int ignoredDepth) {
        // two pattern strength categories
        double totCase1 = 0;
        double totCase2 = 0;
        int val;

        // From the 4 directions, take the maximum pattern strength
        for (int i = 0 ; i < 4 ; i++) {
            totCase1 = Math.max(totCase1,
                    MinimaxEngine.evaluator.patternStr1[i][x][y]);
            totCase2 = Math.max(totCase2,
                    MinimaxEngine.evaluator.patternStr2[i][x][y]);
        }

        // Embedded point detection
        val = innerAlignment(x, y);

        // If no pattern strength and not an embedded point, skip
        if (val == 0 && totCase1 == 0 && totCase2 == 0)
            return;

        // If double-free checking is enabled, apply further filtering
        if (capturePossible &&
                !doubleFreeThree.checkDoubleFree(x, y, turn, MinimaxEngine.board))
            return;

        // Adjust the scoring depending on whether this point is an embedded point
        if (val == 0)
            addCandidate(x, y, Math.max(totCase1, totCase2));
        else if (val == 1)
            addCandidate(x, y, Math.max(totCase1 + 1, totCase2));
        else if (val == 2)
            addCandidate(x, y, Math.max(totCase1, totCase2 + 1));
    }

    // Compute candidate points
    public int interestingCandidate(int depth) {
        this.threshold = 0;

        // Iterate through all empty cells within the limitMin ~ limitMax region
        for (int i = limitMin.x - 1 ; i <= limitMax.x + 1 ; i++) {
            for (int j = limitMin.y - 1 ; j <= limitMax.y + 1 ; j++) {
                if (MinimaxEngine.board[i][j] == 0)
                    loadCase(i, j, depth);
            }
        }
        return this.list.size();
    }

    // Check whether (i,j) already exists in the candidate list
    private boolean isInCandidateList(int i, int j) {
        for (Coordinate coordinate : this.list) {
            if (coordinate.x == i && coordinate.y == j)
                return true;
        }
        return false;
    }

    // add neighboring potential moves
    public int addProbableCandidate(int dp, int turn, int ret) {
        int res;

        // Mark all existing candidates at root layer with the highest strength
        for (Coordinate coordinate : this.list) {
            if (dp == Game.maxDepth)
                coordinate.strength = 5;
        }

        // Iterate through all cells within the limitMin ~ limitMax region
        for (int i = limitMin.x - 1 ; i <= limitMax.x + 1 ; i++) {
            for (int j = limitMin.y - 1 ; j <= limitMax.y + 1 ; j++) {

                if (isInCandidateList(i, j))
                    continue;

                res = nearCount(i, j);
                if (MinimaxEngine.board[i][j] == 0 &&
                    res != 0 &&
                    (!capturePossible || doubleFreeThree.checkDoubleFree(i, j, turn, MinimaxEngine.board)))
                {
                    this.list.add(new Coordinate(i, j, res));
                }
            }
        }

        // Sort (Shell sort)
        sortListByOrder(this.list, this.order);

        if (dp == Game.maxDepth && display)
            displayCandidates("before Select");

        if (this.list.size() >= Game.minCan + 1) {
            if (ret == 3)
                this.list = new ArrayList<>(this.list.subList(0, 4));
            else
                this.list = new ArrayList<>(this.list.subList(0, Game.minCan + 1));
        }

        if (dp == Game.maxDepth && display)
            displayCandidates("Candidate Selected");

        return this.list.size();
    }

    // Return the quadrant of a point
    private int numCan(int i, int j) {
        if (i <= 9 && j >= 9)
            return 0;
        else if (i > 9 && j >= 9)
            return 1;
        else if (i > 9)
            return 2;
        else
            return 3;
    }

    public int oneMoveCandidate() {
        int num;
        for (int i = limitMin.x - 1 ; i <= limitMax.x + 1 ; i++) {
            for (int j = limitMin.y - 1 ; j <= limitMax.y + 1 ; j++) {
                if (MinimaxEngine.board[i][j] == 1) {

                    if (i != 0 && j != 0 && i != 18 && j != 18) {
                        num = numCan(i, j);
                        this.list.clear();

                        // Special case for (9,9): add a fixed offset point
                        if (i == 9 && j == 9)
                            this.list.add(new Coordinate(8, 9));
                        else {
                            // Add two points according to quadrant
                            if (i > j)
                                this.list.add(new Coordinate(
                                        i+openCan[num][0][0],
                                        j+openCan[num][0][1], 1));
                            else
                                this.list.add(new Coordinate(
                                        i+openCan[num][2][0],
                                        j+openCan[num][2][1], 1));

                            this.list.add(new Coordinate(
                                    i+openCan[num][1][0],
                                    j+openCan[num][1][1], 1));
                        }
                        return this.list.size();
                    }
                }
            }
        }
        return 0;
    }

    // Two-move opening candidate generation
    public int twoMoveCandidate() {
        Coordinate b = new Coordinate(0, 0);
        Coordinate w = new Coordinate(0, 0);

        // Find the positions of the black and white stones
        for (int i = limitMin.x - 1 ; i <= limitMax.x + 1 ; i++) {
            for (int j = limitMin.y - 1 ; j <= limitMax.y + 1 ; j++) {
                if (MinimaxEngine.board[i][j] == 1) {
                    b.x = i; b.y = j;
                } else if (MinimaxEngine.board[i][j] == 2) {
                    w.x = i; w.y = j;
                }
            }
        }

        // If the two stones are not adjacent, return three fixed responses
        if (Math.abs(w.x - b.x) >=2 || Math.abs(w.y - b.y) >= 2) {
            this.list.clear();
            for (int k = 0 ; k < 3 ; k++)
                this.list.add(new Coordinate(8, 8+k, 1));
            return 3;
        }
        return 0;
    }

    // Generate all probable candidates around existing stones
    public int allProbableCandidate(int turn, int ignoredDepth) {
        int res;
        int totalMove = 0;

        for (int i = limitMin.x - 1 ; i <= limitMax.x + 1 ; i++) {
            for (int j = limitMin.y - 1 ; j <= limitMax.y + 1 ; j++) {
                res = nearCount(i, j);
                if (MinimaxEngine.board[i][j] == 1 || MinimaxEngine.board[i][j] == 2)
                    totalMove++;

                if (MinimaxEngine.board[i][j] == 0 && res !=0 &&
                    (!capturePossible || doubleFreeThree.checkDoubleFree(i, j, turn, MinimaxEngine.board)))
                {
                    this.list.add(new Coordinate(i, j, res));
                }
            }
        }

        // If total moves = 1, use one-move opening logic
        if (totalMove == 1 && oneMoveCandidate() != 0)
            return this.list.size();

        // If total moves = 2, use two-move opening logic
        if (totalMove == 2 && twoMoveCandidate() != 0)
            return this.list.size();

        // Sort by strength
        sortListByOrder(this.list, this.order);

        // Truncate the number of candidates based on board size and total moves
        if (this.list.size() >= Game.minCan + 1) {
            if (totalMove >=3) {
                if (limitMax.x - limitMin.x > 14 ||
                    limitMax.y - limitMin.y > 14)
                {
                    this.list = new ArrayList<>(this.list.subList(0, 3));
                } else {
                    this.list = new ArrayList<>(this.list.subList(0, Game.minCan));
                }
            } else {
                if (limitMax.x - limitMin.x > 14 ||
                    limitMax.y - limitMin.y > 14)
                {
                    this.list = new ArrayList<>(this.list.subList(0, Game.minCan));
                } else {
                    this.list = new ArrayList<>(this.list.subList(0, Game.minCan + 1));
                }
            }
        }
        return this.list.size();
    }

    // Controls the number of candidates per depth
    private int numCandidates(int depth) {
        // If threshold is high, limit candidates more strictly
        if (this.threshold > Game.minCan) {
            if (depth == Game.maxDepth)
                return Math.min(this.threshold, Game.maxCan +1);
            else
                return Math.min(this.threshold, 10);
        }

        // Root layer
        if (depth == Game.maxDepth)
            return Game.maxCan;
        // One layer below root
        else if (depth == Game.maxDepth - 1 || depth == Game.maxDepth - 2)
            return Game.minCan + 1;
        // Deeper layers
        else
            return Game.minCan;
    }

    // Old version of candidate loading logic
    public int oldLoad(int depth, int turn) {
        int ret;
        this.turn = turn;

        // No need to generate candidates at the bottom of recursion
        if (depth == 0)
            return 0;

        this.list.clear();

        // Root layer needs to reload board boundaries
        if (depth == Game.maxDepth) {
            loadLimits(MinimaxEngine.board);
        }

        // Try pattern candidates first
        ret = interestingCandidate(depth);

        // If there are more than 2 pattern candidates,
        // proceed with sorting and truncation
        if (ret > 2) {
            if (display && depth == Game.maxDepth)
                displayCandidates("Candidate before sort");

            sortListByOrder(this.list, this.order);

            if (display && depth == Game.maxDepth)
                displayCandidates("Candidate after sort");

            if (ret >= numCandidates(depth) + 1) {
                this.list = new ArrayList<>(this.list.subList(0, numCandidates(depth)));
                if (display && depth == Game.maxDepth)
                    displayCandidates("Candidate selected");
            }
            return this.list.size();
        } else {
            // If pattern candidates are insufficient, add probable candidates
            if (ret == 0)
                ret = allProbableCandidate(turn, depth);
            else
                ret = addProbableCandidate(depth, turn, ret);
        }
        return ret;
    }

    // Record active-area boundaries
    public void save(Coordinate c) {
        // Update minimum coordinates
        if (c.x < limitMin.x)
            limitMin.x = Math.max(1, c.x);
        if (c.y < limitMin.y)
            limitMin.y = Math.max(1, c.y);

        // Update maximum coordinates
        if (c.x > limitMax.x)
            limitMax.x = Math.min(17, c.x);
        if (c.y > limitMax.y)
            limitMax.y = Math.min(17, c.y);
    }

    // print candidate list
    public void displayCandidates(String msg) {
        System.out.println(msg);
        for (Coordinate coordinate : this.list) {
            System.out.printf("%d %d %f\n", coordinate.x, coordinate.y, coordinate.strength);
        }
    }

    // Shell sort: sort using the priority defined in order
    private void sortListByOrder(List<Coordinate> list, List<Double> order) {
        int n = list.size();
        int gap = n / 2;

        while (gap > 0) {
            for (int i = gap; i < n; i++) {
                Coordinate temp = list.get(i);
                int tempIdx = order.indexOf(temp.st());

                int j = i;
                while (j >= gap) {
                    int prevIdx = order.indexOf(list.get(j - gap).st());
                    if (prevIdx <= tempIdx) {
                        break;
                    }
                    list.set(j, list.get(j - gap));
                    j -= gap;
                }

                list.set(j, temp);
            }
            gap /= 2;
        }
    }
}
