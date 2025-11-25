package main.ai;

// implementation for Gomoku (Five-in-a-row)
public class GomokuGame extends MinimaxEngine {

    // constructor
    public GomokuGame() {
        board = new int[19][19];
        this.move = new Candidate.Coordinate(-1, -1);
        evaluator = new Evaluator();
        candidate = new Candidate(false);
        positionCounter = 0;
        moveCount = 0;
    }

    /*
     * play: simulate placing a stone on the current board state
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
     */
    public void undo(Candidate.Coordinate c, int depth)
    {
        super.undo(c, depth);
    }

    /*
     * Constructor with depth parameter
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

            // Alpha-Beta pruning logic
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
