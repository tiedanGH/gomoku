package main.ai;

public class MinimaxEngine
{
    // Class description:
    // MinimaxEngine is the “algorithm core layer” of the Gomoku AI.
    // It mainly handles:
    //  1. Minimax search
    //  2. Alpha-beta pruning optimization (minimaxAB)
    //  3. Board evaluation (eval)
    //  4. Board simulation (play / undo)
    //  5. Four-in-a-row check (sub-check for detecting five-in-a-row)
    //  6. Coordinate & candidate management

    // Global shared board (static 2D array)
    // Note: all MinimaxEngine / GomokuGame instances share the same board
    static public int[][] board;

    // Current candidate manager (generates candidate moves for this layer)
    public Candidate candidate;

    // Best move determined after search (used by MAX layer)
    public Candidate.Coordinate best;

    // The “move made at this node” (important for undo backtracking)
    public Candidate.Coordinate move;

    // Stores evaluation results for all candidates at this layer
    public float [] values;

    // Current depth level in the search tree (larger len → deeper in the tree)
    public int len = 0;

    // Position evaluator (Evaluator handles pattern recognition)
    static public Evaluator evaluator;

    // Total number of board states visited during search (used to measure complexity)
    static public int positionCounter;

    // Total number of simulated moves (used for pace-based evaluation)
    static public int moveCount;


    //====================================================
    // Copy external score data into the evaluator
    //====================================================
    public void loadCurrentScore(Evaluator score, int turn)
    {
        evaluator.curTurn = turn;

        // patternStr1 / patternStr2 are 4 directions × 19 × 19 pattern caches
        for (int d = 0 ; d < 4 ; d++)
        {
            for(int i = 0 ; i < 19 ; i++)
            {
                for (int j = 0 ; j < 19 ; j++)
                {
                    evaluator.patternStr1[d][i][j] = score.patternStr1[d][i][j];
                    evaluator.patternStr2[d][i][j] = score.patternStr2[d][i][j];
                }
            }
        }

        // one / two represent each player's current total score
        evaluator.score.one = score.score.one;
        evaluator.score.two = score.score.two;

        // Initialize search statistics
        positionCounter = 0;
        moveCount += 1;
    }

    public MinimaxEngine(){}

    public MinimaxEngine(int len)
    {
        this.len = len;
        this.move = new Candidate.Coordinate(-1, -1);
    }

    /*
     * Copy constructor (used for creating child nodes during recursion)
     * Notes:
     *   - New node's len = parent.len + 1
     *   - Each node needs its own Candidate object 
     *     (otherwise candidate ranges overlap and break search)
     *   - reloadLimits() resets candidate search boundaries
     */
    public MinimaxEngine(MinimaxEngine m, int ignoredDepth)
    {
        this.len = m.len + 1;
        this.move = new Candidate.Coordinate(-1, -1);
        this.candidate = new Candidate(false);
        candidate.reloadLimits();
    }


    //====================================================
    // Evaluation function (returns score to minimax)
    //====================================================
    public float eval(int player, int ignoredLen, int ignoredTurn)
    {
        return evaluator.score.evaluate(player);
    }


    //====================================================
    // Check if (x,y) is inside the board
    //====================================================
    static public boolean isInBoard(int x, int y)
    {
        return x >= 0 && x < 19 && y >= 0 && y < 19;
    }


    //====================================================
    // Check whether (x,y) forms a 4-in-a-row segment in direction (dx,dy)
    //====================================================
    protected boolean checkDir(int x, int y, int dx, int dy, int player)
    {
        int count = 0;

        // Forward direction
        for (int i=dx , j = dy; isInBoard(x+i, y+j) && board[x + i][y + j] == player ; i+=dx, j+=dy)
            count +=1;

        // Backward direction
        for (int i = -dx, j = -dy ; isInBoard(x + i, y + j) && board[x + i][y + j] == player ; i-=dx, j-=dy)
            count +=1;

        return count >= 4; // count=4 means five-in-a-row (this cell counts as 1)
    }

    // Combine four directions to detect five-in-a-row
    protected boolean checkWin4Dir(int x, int y, int player)
    {
        if (checkDir(x,y, 0, 1, player))  // vertical
            return true;
        if (checkDir(x, y, 1, 0, player)) // horizontal
            return true;
        if (checkDir(x, y, 1, 1, player)) // main diagonal
            return true;
        return checkDir(x, y, 1, -1, player); // anti-diagonal
    }


    //====================================================
    // play: simulate placing a stone
    //====================================================
    public boolean play(Candidate.Coordinate c, int player)
    {
        this.move = c;
        board[c.x][c.y] = player;

        // Update candidate search boundaries
        this.candidate.save(c);

        // Notify evaluator to update local patterns
        evaluator.analyseMove(c.x, c.y, player);

        // Return whether this is an immediate win
        return checkWin4Dir(c.x, c.y, player);
    }

    //====================================================
    // undo: rollback a move (used in backtracking)
    //====================================================
    public void undo(Candidate.Coordinate c, int depth)
    {
        int val = board[c.x][c.y];
        board[c.x][c.y] = 0;

        // Evaluator reverse-updates patterns affected by this point
        evaluator.analyseUndo(c.x, c.y, val);
    }


    //====================================================
    // Switch player (1 ↔ 2)
    //====================================================
    protected int change(int player)
    {
        if (player == 1)
            return 2;
        else
            return 1;
    }


    //====================================================
    // max: take maximum value and update best accordingly
    //====================================================
    protected float max(float [] val)
    {
        float res = val[0];
        best = candidate.list.get(0);

        for (int i = 0 ; i < val.length ; i++)
        {
            if (val[i] > res)
            {
                res = val[i];
                best = candidate.list.get(i);
            }
        }
        return res;
    }

    // min: take minimum (used by MIN layer)
    protected float min(float [] val)
    {
        float res = val[0];
        for (int i = 0 ; i < val.length ; i++)
        {
            if (val[i] < res)
            {
                res = val[i];
                best = candidate.list.get(i);
            }
        }
        return res;
    }


    //====================================================
    // Intermediate win evaluation (used during pruning)
    //====================================================
    protected float victoryIntermediateValue(int player, int turn, int len)
    {
        positionCounter++;
        if (player == turn)
            return 10000 - len * 100; // earlier win → higher value
        else
            return -10000 + len * 100;
    }

    // Final win evaluation (also used outside alpha-beta)
    protected float victoryValue(int player, int turn, int len)
    {
        positionCounter++;
        if (player == turn)
        {
            if (len == 0)     // win at root
                return 12000;
            return 10000;
        }
        else
        {
            if (len == 0)
                return -12000;
        }
       return -10000;
    }


    //====================================================
    // Classic minimax recursion (no pruning)
    //====================================================
    public float minimax(int depth, int turn, int player)
    {
        int totalCandidates;
        float result;

        // Generate candidate moves for this layer
        totalCandidates = candidate.oldLoad(depth, turn);

        if (depth == 0)
        {
            positionCounter++;
            result = eval(player, len, turn);
            return result;
        }

        values = new float[totalCandidates];

        // Expand the search tree for every candidate
        for (int i = 0 ; i < totalCandidates ; i++)
        {
            MinimaxEngine m = new MinimaxEngine(this, depth);

            if (m.play(candidate.list.get(i), turn))
                values[i] = victoryValue(player, turn, len);
            else
                values[i] = m.minimax(depth - 1, change(turn), player);

            m.undo(m.move, depth);
        }

        if (turn == player)
            return max(values);
        else
            return min(values);
    }


    //====================================================
    // Minimax with alpha-beta pruning
    //====================================================
    public float minimaxAB(int depth, int turn, int player, float alpha, float beta)
    {
        int totalCandidates;
        float curAlpha;
        float curBeta;
        float res;

        evaluator.curTurn = turn;

        totalCandidates = candidate.oldLoad(depth, turn);

        if (depth == 0)
        {
            positionCounter++;
            res = eval(player, len, turn);
            return res;
        }

        if (totalCandidates == 0)
            return 0;

        values = new float[totalCandidates];

        curAlpha = Float.NEGATIVE_INFINITY;
        curBeta = Float.POSITIVE_INFINITY;

        for (int i = 0 ; i < totalCandidates ; i++)
        {
            MinimaxEngine m = new MinimaxEngine(this.len);
            Candidate.Coordinate cand = candidate.list.get(i);

            boolean isMaxLayer = (turn == player);

            // Decide next-layer search window based on MAX/MIN layer
            float recAlpha = isMaxLayer ? Math.max(alpha, curAlpha) : alpha;
            float recBeta  = isMaxLayer ? beta : Math.min(beta, curBeta);

            if (m.play(cand, turn))
            {
                res = victoryIntermediateValue(player, turn, len);
            }
            else
            {
                res = m.minimaxAB(depth - 1, change(turn), player, recAlpha, recBeta);
                m.undo(m.move, depth);
            }

            values[i] = res;

            // MAX layer: raise alpha
            if (isMaxLayer)
            {
                curAlpha = Math.max(curAlpha, res);

                if (curAlpha > beta) // beta cutoff
                {
                    return curAlpha;
                }
            }
            // MIN layer: lower beta
            else
            {
                curBeta = Math.min(curBeta, res);

                if (alpha > curBeta) // alpha cutoff
                {
                    return curBeta;
                }
            }
        }

        if (turn == player)
            return max(values);
        else
            return min(values);
    }


    //====================================================
    // Print current board (debug use only)
    //====================================================
    static public void displayBoardStatic()
    {
        for (int i = 0 ; i < 19 ; i ++)
        {
            for (int j = 0 ; j < 19 ; j++)
            {
                System.out.printf("%2d", MinimaxEngine.board[i][j]);
            }
            System.out.println();
        }
        System.out.println();
    }

}
