package org.modelai;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import org.utils.DoubleFree;

public class Candidate
{
    // 类说明（中文）：
    // 该类负责在搜索树中为当前局面生成“候选落子”列表，
    // 包括候选的收集、评分、排序与截断。

    public ArrayList<Coordinate> list = new ArrayList<>();
    static public DoubleFree doubleFreeThree = new DoubleFree();
    private final List<Double> order = Arrays.asList(6.0, 5.0, 4.0, 4.8, 4.7, 3.8, 3.7, 3.5, 3.4, 3.0, 2.8, 2.5, 2.4, 2.0, 1.0, 0.0);
    private static final int [][][] openCan = {{{0, -1}, {1, -1}, {1, 0}}, {{-1, 0}, {-1, -1}, {0, -1}}, {{-1, 0}, {-1, 1}, {0, 1}}, {{0, 1}, {1, 1}, {1, 0}}};

    private static Coordinate limitMax = new Coordinate(1, 1);
    private static Coordinate limitMin = new Coordinate(18, 18);
    static  public boolean display = false;
    private int turn;
    private int threshold;
    public boolean capturePossible; // 保留字段但不再用于 Pente 捕子逻辑


    public static class Coordinate
    {
        public int x;
        public int y;
        public double strength;//strength就是hint的强度值，数字越大，这一步棋走的越好，数字越小则反之

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

    public Candidate(boolean cap)
    {
        this.capturePossible = cap;
    }

    private boolean isPlayer(int c)
    {
        return c == 1 || c == 2;
    }

    private boolean isSameNonZero(int a, int b)
    {
        if (a == b)
            return a != 0;
        return false;
    }

    private int innerAlignment(int i, int j)
    {
        if (j+1 != 19 && j-1 != -1 && isSameNonZero(MinimaxEngine.board[i][j + 1], MinimaxEngine.board[i][j - 1]))
            return MinimaxEngine.board[i][j + 1];
        if (i+1 != 19 && i-1 != -1 && isSameNonZero(MinimaxEngine.board[i + 1][j], MinimaxEngine.board[i - 1][j]))
            return MinimaxEngine.board[i + 1][j];
        if (i + 1 != 19 && i-1 != -1 && j + 1 != 19 && j - 1 != -1 && isSameNonZero(MinimaxEngine.board[i+1][j-1], MinimaxEngine.board[i-1][j+1]))
            return MinimaxEngine.board[i+1][j-1];
        if (i + 1 != 19 && i-1 != -1 && j + 1 != 19 && j - 1 != -1 && isSameNonZero(MinimaxEngine.board[i-1][j-1], MinimaxEngine.board[i+1][j+1]))
            return MinimaxEngine.board[i-1][j-1];

        return 0;
    }

    private int nearCount(int i, int j)
    {
        int cmp = 0;
        if (j+1 != 19 && isPlayer(MinimaxEngine.board[i][j + 1]))
            cmp++;
        if (j-1 != -1 && isPlayer(MinimaxEngine.board[i][j - 1]))
            cmp++;
        if (i+1 != 19 && isPlayer(MinimaxEngine.board[i + 1][j]))
            cmp++;
        if (i-1 != -1 && isPlayer(MinimaxEngine.board[i - 1][j]))
            cmp++;
        if (i+1 != 19 && j-1 != -1 && isPlayer(MinimaxEngine.board[i+1][j-1]))
            cmp++;
        if (i-1 != -1 && j-1 != -1 && isPlayer(MinimaxEngine.board[i-1][j-1]))
            cmp++;
        if (i+1 != 19 && j+1 != 19 && isPlayer(MinimaxEngine.board[i+1][j+1]))
            cmp++;
        if (i-1 != -1 && j+1 != 19 && isPlayer(MinimaxEngine.board[i-1][j+1]))
            cmp++;
        return cmp;
    }

    public void reloadLimits()
    {
        limitMax = new Coordinate(1, 1);
        limitMin = new Coordinate(18, 18);
    }

    private void loadLimits(int [][] map)
    {
        for (int i = 0 ; i < 19 ; i++)
        {
            for (int j = 0 ; j < 19 ; j++)
            {
                if (isPlayer(map[i][j]))
                    save(new Coordinate(i, j));
            }
        }
    }

    private void addCandidate(int x, int y, double val)
    {
        if (val >= 3)
            this.threshold+=1;

        this.list.add(new Coordinate(x, y, val));
    }

    // 内部：基于 Evaluator 仿真结果为位置 (x,y) 生成候选评分（已移除 Pente 分支）
    private void loadCase(int x, int y, int ignoredDepth)
    {
        // 功能：基于 Evaluator（MinimaxEngine.miniScoreSim）中记录的模式强度为位置 (x,y) 生成候选评分
        // 说明：已移除与 Pente 捕子相关的特殊处理与分支，保留模式强度汇总与连子检测。

        double totCase1 = 0;
        double totCase2 = 0;
        int val;

        for (int i = 0 ; i < 4 ; i++)
        {
            // 将 Evaluator 中四个方向的模式强度纳入评分汇总
            totCase1 = Math.max(totCase1, MinimaxEngine.evaluator.patternStr1[i][x][y]);
            totCase2 = Math.max(totCase2, MinimaxEngine.evaluator.patternStr2[i][x][y]);
        }

        val = innerAlignment(x, y);

        if (val == 0 && totCase1 == 0 && totCase2 == 0)
            return;
        // 保留 double-free-three 检查调用点（若需要可以由外部配置 capturePossible）
        if (capturePossible && !doubleFreeThree.check_double_free(x, y, turn, MinimaxEngine.board))
            return;

        if (val == 0)
            addCandidate(x, y, Math.max(totCase1, totCase2));
        else if (val == 1)
            addCandidate(x, y, Math.max(totCase1 + 1, totCase2));
        else if (val == 2)
            addCandidate(x, y, Math.max(totCase1, totCase2 + 1));
    }

    public int interestingCandidate(int depth)
    {
        this.threshold = 0;
        for (int i = limitMin.x - 1 ; i <= limitMax.x + 1 ; i++)
        {
           for (int j = limitMin.y - 1 ; j <= limitMax.y + 1 ; j++)
            {
                if (MinimaxEngine.board[i][j] == 0)
                    loadCase(i, j, depth);
            }
        }
        return this.list.size();
    }

    private boolean isInCandidateList(int i, int j)
    {
        for (Coordinate coord : this.list) {
            if (coord.x == i && coord.y == j)
                return true;
        }
        return false;
    }

    public int addProbableCandidate(int dp, int turn, int ret)
    {
        int res;

        for (Coordinate coord : this.list) {
            if (dp == Game.max_depth)
                coord.strength = 5;
        }

        for (int i = limitMin.x - 1 ; i <= limitMax.x + 1 ; i++)
        {
            for (int j = limitMin.y - 1 ; j <= limitMax.y + 1 ; j++)
            {
                if (isInCandidateList(i, j))
                    continue;

                res = nearCount(i, j);
                if (MinimaxEngine.board[i][j] == 0 && res !=0 && (!capturePossible || doubleFreeThree.check_double_free(i, j, turn, MinimaxEngine.board)))
                    this.list.add(new Coordinate(i, j, res));
            }
        }

        // TODO sort need replace
        Collections.sort(this.list, Comparator.comparing(item ->
        this.order.indexOf(item.st())));
        if (dp == Game.max_depth && display)
                displayCandidates("before Select");
        if (this.list.size() >= Game.min_can  + 1)
        {
            if (ret == 3)
                this.list = new ArrayList<>(this.list.subList(0, 4));
            else
                this.list = new ArrayList<>(this.list.subList(0, Game.min_can + 1));
        }

        if (dp == Game.max_depth && display)
                displayCandidates("Candidate Selected");
        return this.list.size();
    }

    private int numCan(int i, int j)
    {
        if (i <= 9 && j >= 9)
            return 0;
        else if (i > 9 && j >= 9)
            return 1;
        else if (i > 9)
            return 2;
        else
            return 3;
    }

    public int oneMoveCandidate()
    {
        int num;
        for (int i = limitMin.x - 1 ; i <= limitMax.x + 1 ; i++)
        {
           for (int j = limitMin.y - 1 ; j <= limitMax.y + 1 ; j++)
            {
                if (MinimaxEngine.board[i][j] == 1)
                {
                    if (i !=0 && j !=0 && i != 18 && j != 18)
                    {
                        num = numCan(i, j);
                        this.list.clear();

                        if (i == 9 && j == 9)
                            this.list.add(new Coordinate(8, 9));
                        else
                        {
                            if (i > j)
                                this.list.add(new Coordinate(i+openCan[num][0][0], j+openCan[num][0][1], 1));
                            else
                                this.list.add(new Coordinate(i+openCan[num][2][0], j+openCan[num][2][1], 1));

                            this.list.add(new Coordinate(i+openCan[num][1][0], j+openCan[num][1][1], 1));
                        }
                        return this.list.size();
                    }
                }
            }
        }
        return 0;
    }

    public int twoMoveCandidate()
    {
        Coordinate b = new Coordinate(0, 0);
        Coordinate w = new Coordinate(0, 0);


        for (int i = limitMin.x - 1 ; i <= limitMax.x + 1 ; i++)
        {
           for (int j = limitMin.y - 1 ; j <= limitMax.y + 1 ; j++)
            {
                if (MinimaxEngine.board[i][j] == 1)
                {
                    b.x = i; b.y=j;
                }
                else if (MinimaxEngine.board[i][j] == 2)
                {
                    w.x = i; w.y = j;
                }
            }
        }

        if (Math.abs(w.x - b.x) >=2 || Math.abs(w.y - b.y) >= 2)
        {
            this.list.clear();
            for (int k = 0 ; k < 3 ; k++)
                this.list.add(new Coordinate(8, 8+k, 1));
            return 3;
        }
        return 0;
    }

    public int allProbableCandidate(int turn, int ignoredDepth)
    {
        int res;
        int nb_mv = 0;

        for (int i = limitMin.x - 1 ; i <= limitMax.x + 1 ; i++)
        {
           for (int j = limitMin.y - 1 ; j <= limitMax.y + 1 ; j++)
            {
                res = nearCount(i, j);
                if (MinimaxEngine.board[i][j] == 1 || MinimaxEngine.board[i][j] == 2)
                    nb_mv++;
                if (MinimaxEngine.board[i][j] == 0 && res !=0 && (!capturePossible || doubleFreeThree.check_double_free(i, j, turn, MinimaxEngine.board)))
                    this.list.add(new Coordinate(i, j, res));
            }
        }

            if (nb_mv == 1 && oneMoveCandidate() != 0)
                return this.list.size();

            if (nb_mv == 2 && twoMoveCandidate() != 0)
                return this.list.size();

        // TODO sort need replace
        Collections.sort(this.list, Comparator.comparing(item ->
        this.order.indexOf(item.st())));



        if (this.list.size() >= Game.min_can + 1)
        {
            if (nb_mv >=3)
            {
                if (limitMax.x - limitMin.x > 14 || limitMax.y - limitMin.y > 14)
                    this.list = new ArrayList<>(this.list.subList(0, 3));
                else
                    this.list = new ArrayList<>(this.list.subList(0, Game.min_can));
            }
            else
            {
                if (limitMax.x - limitMin.x > 14 || limitMax.y - limitMin.y > 14)
                    this.list = new ArrayList<>(this.list.subList(0, Game.min_can));
                else
                    this.list = new ArrayList<>(this.list.subList(0, Game.min_can + 1));
            }
        }
        return this.list.size();
    }

    private int numCandidates(int depth)
    {
        if (this.threshold > Game.min_can)
        {
            if (depth == Game.max_depth)
                return Math.min(this.threshold, Game.max_can +1);
            else
                return Math.min(this.threshold, 10);
        }
        if (depth == Game.max_depth)
            return Game.max_can;
        else if (depth == Game.max_depth - 1 || depth == Game.max_depth - 2)
            return Game.min_can + 1;
        else
            return Game.min_can;
    }

    public int oldLoad(int depth, int turn)
    {
        int ret;
        this.turn = turn;
        if (depth == 0)
            return 0;

        this.list.clear();

        if (depth == Game.max_depth)
        {
            loadLimits(MinimaxEngine.board);
        }

        ret = interestingCandidate(depth);

        if (ret > 2)
        {
            if (display && depth == Game.max_depth)
                displayCandidates("Candidate before sort");

            // TODO sort need replace
            Collections.sort(this.list, Comparator.comparing(item ->
            this.order.indexOf(item.st())));

            if (display && depth == Game.max_depth)
                displayCandidates("Candidate after sort");

            if (ret >= numCandidates(depth) + 1)
            {
                this.list = new ArrayList<>(this.list.subList(0, numCandidates(depth)));
                if (display && depth == Game.max_depth)
                    displayCandidates("Candidate selected");
            }
            return this.list.size();
        }
        else
        {
            if (ret == 0)
                ret = allProbableCandidate(turn, depth);
            else
                ret = addProbableCandidate(depth, turn, ret);
        }
        return ret;
    }

    public void save(Coordinate c)
    {
        if (c.x < limitMin.x)
            limitMin.x = Math.max(1, c.x);
        if (c.y < limitMin.y )
            limitMin.y = Math.max(1, c.y);
        if (c.x > limitMax.x )
            limitMax.x = Math.min(17, c.x);
        if (c.y > limitMax.y )
            limitMax.y = Math.min(17, c.y);
    }

    //display function
    public void displayCandidateMap()
    {
        System.out.printf("candidate number : %d\n", list.size());
        int [] [] mapc = new int [19][19];
        for (int i = 0 ; i < 19 ; i++)
        {
            System.arraycopy(MinimaxEngine.board[i], 0, mapc[i], 0, 19);
        }
        for (Coordinate coord : this.list) {
            mapc[coord.x][coord.y] = 3;
            System.out.printf("%d %d\n", coord.x, coord.y);
        }
        MinimaxEngine.displayBoardStatic(mapc);
    }

    //display function
    public void displayCandidates(String msg)
    {
        System.out.println(msg);
        for (Coordinate coord : this.list) {
            System.out.printf("%d %d %f\n", coord.x, coord.y, coord.strength);
        }
    }
}
