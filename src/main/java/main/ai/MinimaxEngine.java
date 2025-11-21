package main.ai;

public class MinimaxEngine
{
    // 类说明：MinimaxEngine 实现了用于五子棋的极大极小搜索逻辑，
    // 变量名重构后提高可读性：board, miniScoreSim, positionCounter, moveCount 等。

    // 全局棋盘表示（19x19），0=空,1=黑,2=白
    static public int[][] board;
    // 当前候选集合对象（在 Candidate 类中管理候选位置）
    public Candidate candidate;
    // 当前评估出的最佳坐标（搜索时使用）
    public Candidate.Coordinate best;
    // 当前走子坐标（用于回溯时 undo）
    public Candidate.Coordinate move;
    // 存储每个候选位置对应的评估值
    public float [] values;
    // 当前搜索深度或步数计数（语义由调用方使用）
    public int len = 0;
    // 用于仿真评分的共享对象（Evaluator）
    static public Evaluator evaluator;
    // 统计已评估的局面数
    static public int positionCounter;
    // 已走步数计数（在 loadCurrentScore 中自增）
    static public int moveCount;

    // 将传入的 Evaluator 复制到 miniScoreSim，并设置当前回合
    public void loadCurrentScore(Evaluator score, int turn)
    {
        evaluator.curTurn = turn;
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
        evaluator.score.one = score.score.one;
        evaluator.score.two = score.score.two;

        positionCounter = 0;
        moveCount += 1;
    }

    public MinimaxEngine(){}

    public MinimaxEngine(int len)
    {
        this.len = len;
        this.move = new Candidate.Coordinate(-1, -1);
    }

    public MinimaxEngine(MinimaxEngine m, int ignoredDepth)
    {
        this.len = m.len + 1;
        this.move = new Candidate.Coordinate(-1, -1);
        this.candidate = new Candidate(false);
        candidate.reloadLimits();
    }

    // 评估函数：返回当前仿真评分（封装在 Evaluator 中）
    public float eval(int player, int ignoredLen, int ignoredTurn)
    {
        return evaluator.score.evaluate(player);
    }

    // 判断坐标是否在棋盘内（保护方法）
    static public boolean isInBoard(int x, int y)
    {
        return x >= 0 && x < 19 && y >= 0 && y < 19;
    }

    // 检查某方向上是否已连成4（用于判断是否为胜利向量的一部分）
    protected boolean checkDir(int x, int y, int dx, int dy, int player)
    {
        int count = 0;

        for (int i=dx , j = dy; isInBoard(x+i, y+j) && board[x + i][y + j] == player ; i+=dx, j+=dy)
            count +=1;

        for (int i = -dx, j = -dy ; isInBoard(x + i, y + j) && board[x + i][y + j] == player ; i-=dx, j-=dy)
            count +=1;

        return count >= 4;
    }

    protected boolean checkWin4Dir(int x, int y, int player)
    {
        if (checkDir(x,y, 0, 1, player))
            return true;
        if (checkDir(x, y, 1, 0, player))
            return true;
        if (checkDir(x, y, 1, 1, player))
            return true;
        return checkDir(x, y, 1, -1, player);
    }

    // 执行落子：设置棋盘、保存候选、并在 Evaluator 中进行分析；返回是否达成四连（胜利检测）
    public boolean play(Candidate.Coordinate c, int player)
    {
        this.move = c;
        board[c.x][c.y] = player;
        this.candidate.save(c);
        evaluator.analyseMove(c.x, c.y, player);
        return checkWin4Dir(c.x, c.y, player);
    }

    // 撤销落子：在棋盘上清空位置，并在 Evaluator 中撤销分析
    public void undo(Candidate.Coordinate c, int depth)
    {
        int val = board[c.x][c.y];
        board[c.x][c.y] = 0;
        evaluator.analyseUndo(c.x, c.y, val);
    }

    // 切换玩家（1<->2）
    protected int change(int player)
    {
        if (player == 1)
            return 2;
        else
            return 1;
    }

    // 在一组评估值中取最大值，并设置 best 为对应坐标
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

    // 在一组评估值中取最小值，并设置 best 为对应坐标
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

    // 胜利的中间值（用于 alpha-beta 剪枝时遇到立即胜利的估值）
    protected float victoryIntermediateValue(int player, int turn, int len)
    {
        positionCounter++;
        if (player == turn)
            return 10000 - len * 100;
        else
            return -10000 + len * 100;
    }

    // 胜利的最终估值（区分先手/后手以及 len==0 的情况）
    protected float victoryValue(int player, int turn, int len)
    {
        positionCounter++;
        if (player == turn)
        {
            if (len == 0)
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

    // 经典的深度为 depth 的 minimax 递归
    //针对每个候选走法进行模拟
    //递归切换 max/min 层
    //返回最大值或最小值
    public float minimax(int depth, int turn, int player)
    {

        int nb_candidates;
        float reteval;

        nb_candidates = candidate.oldLoad(depth, turn);

        if (depth == 0)
        {

            positionCounter++;
            reteval = eval(player, len, turn);

            return reteval;
        }

        values = new float[nb_candidates];

        for (int i = 0 ; i < nb_candidates ; i++)
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

    // 带 alpha-beta 剪枝的 minimax 实现
    public float minimaxAB(int depth, int turn, int player, float alpha, float beta)
    {
        int nb_candidates;
        float cur_alpha;
        float cur_beta;
        float res;

        evaluator.curTurn = turn;

        nb_candidates = candidate.oldLoad(depth, turn);
        if (depth == 0)
        {
            positionCounter++;
            res = eval(player, len, turn);
            return res;
        }

        if (nb_candidates == 0)
            return 0;

        values = new float[nb_candidates];

        cur_alpha = Float.NEGATIVE_INFINITY;
        cur_beta = Float.POSITIVE_INFINITY;

        for (int i = 0 ; i < nb_candidates ; i++)
        {
            MinimaxEngine m = new MinimaxEngine(this.len);
            Candidate.Coordinate cand = candidate.list.get(i);
            boolean isMaxLayer = (turn == player);

            // 根据当前层决定递归时传入的窗口（不改变外部 alpha/beta）
            float recAlpha = isMaxLayer ? Math.max(alpha, cur_alpha) : alpha;
            float recBeta  = isMaxLayer ? beta : Math.min(beta, cur_beta);

            if (m.play(cand, turn))
            {
                // 立即胜利（终止节点）
                res = victoryIntermediateValue(player, turn, len);
            }
            else
            {
                // 递归调用并撤销（保持原有顺序与深度参数）
                res = m.minimaxAB(depth - 1, change(turn), player, recAlpha, recBeta);
                m.undo(m.move, depth);
            }

            values[i] = res;

            // 更新局部窗口并判断剪枝
            if (isMaxLayer)
            {
                cur_alpha = Math.max(cur_alpha, res);
                if (cur_alpha > beta) // beta cut
                {
                    return cur_alpha;
                }
            }
            else
            {
                cur_beta = Math.min(cur_beta, res);
                if (alpha > cur_beta) // alpha cut
                {
                    return cur_beta;
                }
            }
        }

        if (turn == player)
            return max(values);
        else
            return min(values);
    }

    // 输出静态棋盘到控制台（静态方法）
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
