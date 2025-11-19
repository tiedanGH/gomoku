package org.modelai;

import org.utils.*;

public class Game {

    // 类说明：代表一盘游戏的状态和与 MinimaxEngine 搜索的交互封装，
    // 包括棋盘数据、计时信息、规则类型以及对 MinimaxEngine 的调用接口。

    // 当前 MinimaxEngine 实例（搜索树）
    public MinimaxEngine m;
    // 游戏棋盘（方阵，0=空，1=黑，2=白）
    public int[][] gameMap;
    // 当前棋局评分对象（Evaluator）
    public Evaluator miniScore =  new Evaluator();
    // 已落子数量
    public int nb_move;
    // 上次搜索得到的估值
    public Float val;
    // 上次搜索耗时（毫秒）
    public long time;
    // 游戏规则标识字符串（例如 "Gomoku" 或 "None"）
    public String rules;
    static public int max_depth = 10;
    static public int max_can = 7;
    static public int min_can = 5;
    static public int fast_search = 0;


    public Game(String rules, int board_size)
    {
        gameMap = new int[board_size][board_size];
        nb_move = 0;
        m = minmaxTree(rules);
        m.len = 0;
    }

    private MinimaxEngine minmaxTree(String str)
    {
        str = Character.toUpperCase(str.charAt(0)) + str.substring(1);
        if (str.equals("Gomoku")) {
            this.rules = "Gomoku";
            max_depth = 10;
            return new GomokuGame();
        } else {
            this.rules = "None";
            return new MinimaxEngine();
        }
    }

    public void treeConfig(int lvl)
    {
        if (lvl == 1)
        {
            max_depth = 10;
            max_can = 8;
            min_can = 5;
            fast_search = 0;
        }
        else if (lvl == 2)
        {
            max_depth = 9;
            max_can = 7;
            min_can = 6;
            fast_search = 0;
        }
        else if (lvl == 3)
        {
            max_depth = 3;
            max_can = 8;
            min_can = 8;
            fast_search = 0;
        }
        else if (lvl == 4)
        {
            max_depth = 9;
            max_can = 8;
            min_can = 7;
            fast_search = 0;
        }
    }  

    public void move(Point point, int turn)
    {
        // 将落子写入共享静态棋盘（重命名为 board）
        MinimaxEngine.board[point.y][point.x] = turn;

        // 更新评分结构
        miniScore.analyseMove(point.y, point.x, turn);

        nb_move++;
    }

    public void resetMinMax()
    {
        for (int i = 0 ; i < 19 ; i++)
            for (int j = 0 ; j < 19 ; j++)
                MinimaxEngine.board[i][j] = 0;
        MinimaxEngine.positionCounter =0;
        MinimaxEngine.moveCount = 0;
        Game.fast_search = 0;
        miniScore.resetStr();
    }

    private void initializeMap()
    {
        for (int i = 0 ; i < 19 ; i++)
            System.arraycopy(gameMap[i], 0, MinimaxEngine.board[i], 0, 19);
    }

    public Point bestMove(int turn, int player, boolean display)
    {
        if (display)
            System.out.printf("Call best_move turn %d player %d et nb move %d\n", turn, player, nb_move);

        if (nb_move == 0)
                resetMinMax();

        initializeMap();
        if (display)
            displayAllBoardInfo();

        time = System.currentTimeMillis();
        if (nb_move == 0)
            m.best = new Candidate.Coordinate(9, 9);
        else
        {
            m.loadCurrentScore(miniScore, turn);

            // 仅对 Gomoku 使用带 alpha-beta 的 minimaxAB；Pente 分支已移除
            if (this.rules.equals("Gomoku"))
                val = m.minimaxAB(max_depth, turn, player, Float.NEGATIVE_INFINITY, Float.POSITIVE_INFINITY);
            else
                val = m.minimax(max_depth, turn, player);
        }
        if (display)
            displayAllBoardInfo();

        time = System.currentTimeMillis() - time;

        if (display)
            bestMoveStamp();

        return new Point(m.best.y, m.best.x);
    }

    //display function
    private void displayAllBoardInfo()
    {
        MinimaxEngine.displayBoardStatic();
        System.out.println();
    }

    //display function
    private void bestMoveStamp()
    {
        System.out.printf("AI move %d (Turn %d) at %d %d played in %f seconds (%d pos, %d depth, %d speed)\n", nb_move + 1,(nb_move + 1) / 2 + 1, m.best.y, m.best.x,(double)time / 1000, MinimaxEngine.positionCounter, max_depth, fast_search);
    }
}
