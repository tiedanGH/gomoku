package main.ai;

import main.utils.Point;

// Represents the overall state of a game, acting as a “bridge layer”
// between MinimaxEngine and the external UI / rules module.
public class Game {
    // Current MinimaxEngine instance
    public MinimaxEngine m;
    // Game board
    public int[][] gameMap;
    // Evaluator for the current board state
    public Evaluator miniScore = new Evaluator();
    // Total number of moves made so far
    public int totalMove;
    // Last search evaluation value
    public Float val;
    // Time spent on the current search
    public long time;
    // Rule name
    public String rules;

    // Search depth
    static public int maxDepth = 10;
    // Max number of candidate moves per layer
    static public int maxCan = 7;
    // Minimum number of candidate moves per layer
    static public int minCan = 5;
    // Whether fast-search mode is enabled
    static public int fastSearch = 0;


    // Constructor: initialize board and search engine
    public Game(String rules, int boardSize) {
        gameMap = new int[boardSize][boardSize];
        totalMove = 0;
        m = minmaxTree(rules);   // Create Minimax implementation based on rules
        m.len = 0;
    }


    // Create MinimaxEngine subclass based on game rule
    private MinimaxEngine minmaxTree(String str) {
        str = Character.toUpperCase(str.charAt(0)) + str.substring(1);
        if (str.equals("Gomoku")) {
            this.rules = "Gomoku";
            maxDepth = 10;
            return new GomokuGame();  // Use Gomoku-specialized search engine
        } else {
            this.rules = "None";
            return new MinimaxEngine();  // Default generic engine
        }
    }

    // Search configuration
    public void treeConfig(int level) {
        if (level == 1)
        {
            maxDepth = 10;
            maxCan = 8;
            minCan = 5;
            fastSearch = 0;
        }
        else if (level == 2)
        {
            maxDepth = 9;
            maxCan = 7;
            minCan = 6;
            fastSearch = 0;
        }
        else if (level == 3)
        {
            maxDepth = 3;
            maxCan = 8;
            minCan = 8;
            fastSearch = 0;
        }
        else if (level == 4)
        {
            maxDepth = 9;
            maxCan = 8;
            minCan = 7;
            fastSearch = 0;
        }
    }

    // make a move
    public void move(Point point, int turn) {
        // Update global board (MinimaxEngine.board)
        MinimaxEngine.board[point.y][point.x] = turn;
        // Update evaluator (pattern recognition)
        miniScore.analyseMove(point.y, point.x, turn);
        totalMove++;
    }

    // Reset Minimax engine
    public void resetMinMax() {
        // Reset board
        for (int i = 0 ; i < 19 ; i++)
            for (int j = 0 ; j < 19 ; j++)
                MinimaxEngine.board[i][j] = 0;

        // Reset counters
        MinimaxEngine.positionCounter = 0;
        MinimaxEngine.moveCount = 0;
        Game.fastSearch = 0;

        // Reset Evaluator internal states
        miniScore.resetStr();
    }

    // Copy gameMap into MinimaxEngine.board
    private void initializeMap() {
        for (int i = 0 ; i < 19 ; i++)
            System.arraycopy(gameMap[i], 0, MinimaxEngine.board[i], 0, 19);
    }

    // Get AI's best move
    public Point bestMove(int turn, int player, boolean display) {

        if (display)
            System.out.printf("Call best move turn %d player %d at total move %d\n", turn, player, totalMove);

        // First move: reset Minimax engine
        if (totalMove == 0)
            resetMinMax();

        // Synchronize board
        initializeMap();

        if (display)
            displayAllBoardInfo();

        // Timestamp: search start
        time = System.currentTimeMillis();

        // First move → AI plays center (9,9)
        if (totalMove == 0)
            m.best = new Candidate.Coordinate(9, 9);

        else {
            // Load evaluator state into MinimaxEngine
            m.loadCurrentScore(miniScore, turn);

            // Gomoku uses alpha-beta search
            if (this.rules.equals("Gomoku"))
                val = m.minimaxAB(
                        maxDepth,
                        turn,
                        player,
                        Float.NEGATIVE_INFINITY,
                        Float.POSITIVE_INFINITY
                );
            else
                val = m.minimax(maxDepth, turn, player);
        }

        if (display)
            displayAllBoardInfo();

        // Compute elapsed search time
        time = System.currentTimeMillis() - time;

        if (display)
            bestMoveStamp();

        // Return AI-selected point
        // NOTE: best.x and best.y are stored reversed
        return new Point(m.best.y, m.best.x);
    }

    // Print current board
    private void displayAllBoardInfo() {
        MinimaxEngine.displayBoardStatic();
        System.out.println();
    }

    // Print search statistics
    private void bestMoveStamp() {
        System.out.printf(
                "AI move %d (Turn %d) at %d %d played in %f seconds (%d pos, %d depth, %d speed)\n",
                totalMove + 1,
                (totalMove + 1) / 2 + 1,
                m.best.y,
                m.best.x,
                (double)time / 1000,
                MinimaxEngine.positionCounter,
                maxDepth,
                fastSearch
        );
    }
}
