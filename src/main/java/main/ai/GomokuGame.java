package main.ai;

/*
 * GomokuGame
 * English Description:
 * GomokuGame extends MinimaxEngine and provides a specialized node
 * implementation for Gomoku (Five-in-a-row).
 *
 * Each GomokuGame instance represents a “node” in the search tree.
 * It will:
 *  - Generate candidate moves under the current board state
 *  - Simulate placing a stone on a candidate move (play)
 *  - Recursively call minimax / minimaxAB to explore deeper game states
 *  - After evaluating a branch, call undo to revert to the parent state
 */

public class GomokuGame extends MinimaxEngine {

    public GomokuGame()
    {
        // Initialize shared board, evaluator, and candidate generator
        // Note: board is inherited from MinimaxEngine as a static 19×19 array
        board = new int[19][19];

        // move indicates “which move created this node”; (-1,-1) means none yet
        this.move = new Candidate.Coordinate(-1, -1);

        // Evaluator: detects and scores local Gomoku patterns
        evaluator = new Evaluator();

        // Candidate generator: produces potential move locations
        candidate = new Candidate(false);

        // Search counter: counts how many nodes (board states) were explored
        positionCounter = 0;

        // Total moves played so far (used for tempo, depth-related heuristics)
        moveCount = 0;
    }

    /*
     * play: simulate placing a stone on the current board state
     *
     * Parameters:
     *   c      - coordinate of the move (from Candidate)
     *   player - the player placing the stone (1 or 2)
     *
     * Returns:
     *   true   - this move directly results in a victory (five-in-a-row)
     *   false  - no immediate win; search continues
     *
     * Logic:
     *   1. First check if this move already forms a win (depending on
     *      the implementation, this may be checked before or after placing).
     *   2. Record the move in "this.move"
     *   3. Mark the stone on the board
     *   4. Update the candidate search boundaries via candidate.save(c)
     *   5. Update the evaluator’s pattern and scoring structures
     */
    public boolean play(Candidate.Coordinate c, int player)
    {
        // If the position already forms a win under the current view, return true
        if (checkWin4Dir(c.x, c.y, player))
            return true;

        // Record which move this node represents
        this.move = c;

        // Actually “play” the move on the board
        board[c.x][c.y] = player;

        // Update candidate active-region boundaries (limitMin / limitMax)
        candidate.save(c);

        // Tell the evaluator that a move was placed here
        evaluator.analyseMove(c.x, c.y, player);

        return false;
    }

    /*
     * undo: revert the move made at this node and restore the parent state
     *
     * This simply calls the parent MinimaxEngine's undo(),
     * which reverts both:
     *   - the move on the board
     *   - evaluator state changes caused by that move
     */
    public void undo(Candidate.Coordinate c, int depth)
    {
        super.undo(c, depth);
    }

    /*
     * Constructor with depth parameter
     *
     * The len parameter is typically used to:
     *   - Record the current depth level in the search tree
     *   - Influence evaluation (e.g., distinguishing fast wins from slow wins)
     */
    public GomokuGame(int len)
    {
        this.len = len + 1;

        // No simulated move at creation
        this.move = new Candidate.Coordinate(-1, -1);

        // Each node has its own Candidate instance for generating moves
        this.candidate = new Candidate(false);
    }

    /*
     * minimax: classic minimax search (without alpha-beta pruning)
     *
     * Parameters:
     *   depth  - remaining search depth
     *   turn   - whose turn it currently is (1 or 2)
     *   player - identifies the AI’s side (determines MAX or MIN layer)
     *
     * Flow:
     *   1. Call candidate.oldLoad() to generate candidate moves
     *   2. If depth == 0, call eval() and return the evaluation score
     *   3. Otherwise:
     *        - For each candidate, create a GomokuGame child node m
     *        - Call m.play() to simulate the move
     *        - If immediate win, compute victoryValue()
     *        - Else recursively call minimax on the child
     *        - After exploring the branch, call undo to revert the move
     *   4. If current turn == player, this is a MAX layer → return max(values)
     *      Otherwise MIN layer → return min(values)
     */
    public float minimax(int depth, int turn, int player)
    {
        int totalCandidates;

        // Load candidate move list for current state and depth
        totalCandidates = candidate.oldLoad(depth, player);

        // Reached leaf node or depth limit → evaluate board
        if (depth == 0)
        {
            positionCounter++;
            return eval(player, len, turn);
        }

        values = new float[totalCandidates];

        // Explore each candidate move
        for (int i = 0 ; i < totalCandidates ; i++)
        {
            GomokuGame m = new GomokuGame(this.len);

            // Simulate move
            if (m.play(candidate.list.get(i), turn))
                values[i] = victoryValue(player, turn, len); // immediate win
            else
                values[i] = m.minimax(depth - 1, change(turn), player);

            // Revert simulated move
            m.undo(m.move, depth);
        }

        // MAX or MIN layer
        if (turn == player)
            return max(values);
        else
            return min(values);
    }

    /*
     * minimaxAB: minimax search with alpha-beta pruning
     *
     * Parameters:
     *   depth  - remaining search depth
     *   turn   - current player (1 or 2)
     *   player - AI’s side
     *   alpha  - lower bound (for MAX)
     *   beta   - upper bound (for MIN)
     *
     * Main differences from basic minimax:
     *   - Uses alpha and beta to prune branches that cannot affect result
     *   - Uses isMaxLayer to determine pruning behavior
     *   - Uses recAlpha / recBeta as tighter recursive windows
     */
    public float minimaxAB(int depth, int turn, int player, float alpha, float beta)
    {
        int totalCandidates;
        float curAlpha;
        float curBeta;
        float res;

        // Let evaluator know whose turn it is
        evaluator.curTurn = turn;

        // Generate candidate moves
        totalCandidates = candidate.oldLoad(depth, turn);

        // Depth limit reached → evaluate
        if (depth == 0)
        {
            positionCounter++;
            res = eval(player, len, turn);
            return res;
        }

        // Extreme fallback case
        if (totalCandidates == 0)
            return 0;

        values = new float[totalCandidates];

        // Initial α/β window
        curAlpha = Float.NEGATIVE_INFINITY;
        curBeta = Float.POSITIVE_INFINITY;

        // Explore each candidate
        for (int i = 0 ; i < totalCandidates ; i++)
        {
            GomokuGame m = new GomokuGame(this.len);
            Candidate.Coordinate cand = candidate.list.get(i);

            boolean isMaxLayer = (turn == player);

            // Compute next-layer alpha/beta window
            float recAlpha = isMaxLayer ? Math.max(alpha, curAlpha) : alpha;
            float recBeta  = isMaxLayer ? beta : Math.min(beta, curBeta);

            // Simulate move
            if (m.play(cand, turn))
            {
                // Immediate win → short-circuit search for this branch
                res = victoryIntermediateValue(player, turn, len);
            }
            else
            {
                // Recursively search deeper with updated window
                res = m.minimaxAB(depth - 1, change(turn), player, recAlpha, recBeta);
                m.undo(m.move, depth);  // backtrack
            }

            values[i] = res;

            //====================================
            // Alpha-Beta pruning logic
            //====================================
            if (isMaxLayer)
            {
                // MAX layer tries to increase curAlpha
                curAlpha = Math.max(curAlpha, res);

                // If value exceeds beta → prune
                if (curAlpha > beta)
                    return curAlpha;
            }
            else
            {
                // MIN layer tries to lower curBeta
                curBeta = Math.min(curBeta, res);

                // If alpha > curBeta → prune
                if (alpha > curBeta)
                    return curBeta;
            }
        }

        // After exploring children, return MAX or MIN
        if (turn == player)
            return max(values);
        else
            return min(values);
    }
}
