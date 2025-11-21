package main.ai;

import main.utils.Point;

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
    public int totalMove;
    // 上次搜索得到的估值
    public Float val;
    // 上次搜索耗时（毫秒）
    public long time;
    // 游戏规则标识字符串（例如 "Gomoku" 或 "None"）
    public String rules;
    static public int maxDepth = 10;
    static public int maxCan = 7;
    static public int minCan = 5;
    static public int fastSearch = 0;

    public Game(String rules, int boardSize) {
        gameMap = new int[boardSize][boardSize];
        totalMove = 0;
        m = minmaxTree(rules);
        m.len = 0;
    }

    private MinimaxEngine minmaxTree(String str) {
        str = Character.toUpperCase(str.charAt(0)) + str.substring(1);
        if (str.equals("Gomoku")) {
            this.rules = "Gomoku";
            maxDepth = 10;
            return new GomokuGame();
        } else {
            this.rules = "None";
            return new MinimaxEngine();
        }
    }

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

    public void move(Point point, int turn) {
        // 将落子写入共享静态棋盘（重命名为 board）
        MinimaxEngine.board[point.y][point.x] = turn;

        // 更新评分结构
        miniScore.analyseMove(point.y, point.x, turn);

        totalMove++;
    }

    public void resetMinMax() {
        for (int i = 0 ; i < 19 ; i++)
            for (int j = 0 ; j < 19 ; j++)
                MinimaxEngine.board[i][j] = 0;
        MinimaxEngine.positionCounter =0;
        MinimaxEngine.moveCount = 0;
        Game.fastSearch = 0;
        miniScore.resetStr();
    }

    private void initializeMap() {
        for (int i = 0 ; i < 19 ; i++)
            System.arraycopy(gameMap[i], 0, MinimaxEngine.board[i], 0, 19);
    }

    public Point bestMove(int turn, int player, boolean display) {
        if (display)
            System.out.printf("Call best_move turn %d player %d et nb move %d\n", turn, player, totalMove);

        if (totalMove == 0)
                resetMinMax();

        initializeMap();
        if (display)
            displayAllBoardInfo();

        time = System.currentTimeMillis();
        if (totalMove == 0)
            m.best = new Candidate.Coordinate(9, 9);
        else {
            m.loadCurrentScore(miniScore, turn);

            if (this.rules.equals("Gomoku"))
                val = m.minimaxAB(maxDepth, turn, player, Float.NEGATIVE_INFINITY, Float.POSITIVE_INFINITY);
            else
                val = m.minimax(maxDepth, turn, player);
        }
        if (display)
            displayAllBoardInfo();

        time = System.currentTimeMillis() - time;

        if (display)
            bestMoveStamp();

        return new Point(m.best.y, m.best.x);
    }

    //display function
    private void displayAllBoardInfo() {
        MinimaxEngine.displayBoardStatic();
        System.out.println();
    }

    //display function
    private void bestMoveStamp() {
        System.out.printf("AI move %d (Turn %d) at %d %d played in %f seconds (%d pos, %d depth, %d speed)\n", totalMove + 1,(totalMove + 1) / 2 + 1, m.best.y, m.best.x,(double)time / 1000, MinimaxEngine.positionCounter, maxDepth, fastSearch);
    }
}
