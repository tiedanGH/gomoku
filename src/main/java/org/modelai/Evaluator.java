package org.modelai;
import java.util.ArrayList;

import org.utils.*;

/**
 * Evaluator
 * *
 * 五子棋的局部模式评分与快速仿真器（用于 MinimaxEngine 搜索时的局面增量维护）。
 * 说明：
 * - 该类维护两个玩家的方向性模式数组 patternStr1 / patternStr2（维度 [4][19][19]）。
 * - score 保存当前两个玩家的累计评分（Score.one, Score.two）。
 * - 外部（MinimaxEngine）会直接访问 patternStr1 / patternStr2 / score / curTurn，
 *   因此这些字段名保持不变以保证兼容性。
 * *
 * 对外公开的方法（被 MinimaxEngine / GomokuGame 调用）：
 * - analyseMove(x,y,turn)：在棋盘放置棋子后更新模式/评分（对外名保持不变）
 * - analyseUnmove(x,y,turn)：在棋盘撤子后回滚模式/评分（对外名保持不变）
 * *
 * 内部方法与字段均使用驼峰命名以提高可读性。
 */
public class Evaluator {

    /* 对外字段（不要改名以保证与其它文件兼容） */

    // 全局评分对象（包含 one/two 字段）
    Score score;

    // 用于临时引用当前玩家的方向性模式数组（内部使用）
    int [][][] currentPattern;

    // 两位玩家的方向性模式数组（外部 MinimaxEngine 直接读取/写入）
    int [][][] patternStr1;
    int [][][] patternStr2;

    // 当前要仿真的回合（1 或 2），MinimaxEngine 会设置此字段
    int curTurn;

    /* 内部状态变量（已改为驼峰命名） */

    // 当前处理的坐标
    int x;
    int y;
    // 当前方向增量
    int dx;
    int dy;
    // 当前方向索引（0..3）
    int dirIndex;

    // 当前是否检测到胜利（五连）
    boolean victory;

    // 当前方向的阻挡信息列表
    ArrayList<Blocker> blockerList = new ArrayList<>();

    // 模式到分值的映射因子（索引即模式编号）
    static int [] factor = {0, 0, 2, 10, 25, 0, 0, 0, 0, 0};
    // 四个方向偏移（水平、竖直、主对角、次对角）
    static int [][] dirOffsets = {{1, 0}, {0, 1}, {1, 1}, {-1, 1}};

    /**
     * 构造器：初始化评分器与模式数组
     */
    public Evaluator()
    {
        this.score = new Score();
        this.patternStr1 = new int[4][19][19];
        this.patternStr2 = new int[4][19][19];
        this.victory = false;
    }

    // ----------------------
    // 公共/对外方法（保留原名以兼容外部调用）
    // ----------------------

    /**
     * analyseMove
     * 对外接口：在棋盘放置棋子后调用，更新内部模式与评分（增量）。
     * 参数：
     *  - x,y: 棋子坐标
     *  - turn: 当前落子玩家（1 或 2）
     */
    public void analyseMove(int x, int y, int turn)
    {
        // 保持原有行为，但调用已重命名的内部方法
        this.curTurn = turn;
        this.currentPattern = turn == 1 ? patternStr1 : patternStr2;
        this.x = x;
        this.y = y;

        // 四个方向分别检查并 connect（合并/扩展模式）
        if (x + 1 != 19 && MinimaxEngine.board[x+1][y] == curTurn)
        {
            dirIndex = 0;
            dx = 1; dy=0;
            connectInternal();
        }
        else if (x - 1 != -1 && MinimaxEngine.board[x-1][y] == curTurn)
        {
            dirIndex = 0;
            dx=-1; dy=0;
            connectInternal();
        }

        if (y + 1 != 19 && MinimaxEngine.board[x][y+1] == curTurn)
        {
            dirIndex = 1;
            dx=0; dy=1;
            connectInternal();
        }
        else if (y - 1 != -1 && MinimaxEngine.board[x][y-1] == curTurn)
        {
            dirIndex = 1;
            dx=0; dy=-1;
            connectInternal();
        }

        if (x + 1 != 19 && y + 1 != 19 && MinimaxEngine.board[x+1][y+1] == curTurn)
        {
            dirIndex = 2;
            dx=1; dy=1;
            connectInternal();
        }
        else if (x - 1 != -1 && y - 1 != -1 && MinimaxEngine.board[x-1][y-1] == curTurn)
        {
            dirIndex = 2;
            dx=-1; dy=-1;
            connectInternal();
        }

        if (x + 1 != 19 && y - 1 != -1 && MinimaxEngine.board[x+1][y-1] == curTurn)
        {
            dirIndex = 3;
            dx=1; dy=-1;
            connectInternal();
        }
        else if (x - 1 != -1 && y + 1 != 19 && MinimaxEngine.board[x-1][y+1] == curTurn)
        {
            dirIndex = 3;
            dx=-1; dy=1;
            connectInternal();
        }

        // 填充方向边界或创建 blocker
        fillTwo();

        // 更新 blocker 列表（移除已失效的）
        updateBlockers();
    }

    /**
     * analyseUndo
     * 对外接口：在棋盘撤子后调用，回滚内部模式与评分（对外名保持不变）
     * 参数：
     *  - x,y: 撤去棋子坐标
     *  - turn: 撤去的棋手（1 或 2）
     */
    public void analyseUndo(int x, int y, int turn)
    {
        this.curTurn = turn;
        this.currentPattern = turn == 1 ? this.patternStr1 : this.patternStr2;
        this.x = x;
        this.y = y;

        // 四方向分别尝试回退连接（unconnect）和 unfill
        if ((x + 1 != 19 && isPlayer(MinimaxEngine.board[x+1][y])) || (x - 1 != -1 && isPlayer(MinimaxEngine.board[x-1][y])))
        {
            dirIndex = 0;
            dx = 1; dy = 0;
            if (x + 1 != 19 && MinimaxEngine.board[x+1][y] == curTurn)
                unconnectInternal();
            else if (x - 1 != -1 && MinimaxEngine.board[x-1][y] == curTurn)
            {
                dx=-1;
                unconnectInternal();
            }
            unFill();
        }

        if ((y + 1 != 19 && isPlayer(MinimaxEngine.board[x][y+1])) || (y - 1 != -1 && isPlayer(MinimaxEngine.board[x][y-1])))
        {
            dirIndex = 1;
            dx = 0; dy = 1;
            if (y + 1 != 19 && MinimaxEngine.board[x][y+1] == curTurn)
                unconnectInternal();
            else if (y - 1 != -1 && MinimaxEngine.board[x][y-1] == curTurn)
            {
                dy=-1;
                unconnectInternal();
            }
            unFill();
        }

        if ((x + 1 != 19 && y + 1 != 19 && isPlayer(MinimaxEngine.board[x+1][y+1])) || ( x - 1 != -1 && y - 1 != -1 && isPlayer(MinimaxEngine.board[x-1][y-1])))
        {
            dirIndex = 2;
            dx = 1; dy = 1;
            if (x + 1 != 19 && y + 1 != 19 && MinimaxEngine.board[x+1][y+1] == curTurn)
                unconnectInternal();
            else if ( x - 1 != -1 && y - 1 != -1 && MinimaxEngine.board[x-1][y-1] == curTurn)
            {
                dx=-1; dy=-1;
                unconnectInternal();
            }
            unFill();
        }

        if (x + 1 != 19 && y - 1 != -1 && isPlayer(MinimaxEngine.board[x+1][y-1]) || (x - 1 != -1 && y + 1 != 19 && isPlayer(MinimaxEngine.board[x-1][y+1])))
        {
            dirIndex = 3;
            dx = 1; dy = -1;
            if (x + 1 != 19 && y - 1 != -1 && MinimaxEngine.board[x+1][y-1] == curTurn)
                unconnectInternal();
            else if (x - 1 != -1 && y + 1 != 19 && MinimaxEngine.board[x-1][y+1] == curTurn)
            {
                dx = -1; dy=1;
                unconnectInternal();
            }

            unFill();
        }

        // 处理周边位置的 unfill0（更名为 unfillZero）
        for (int i = 0; i < 4 ; i++)
        {
            unfillZero(x, y, i);
        }

        updateBlockers();
    }

    // ----------------------
    // 内部帮助方法（已统一为驼峰命名）
    // ----------------------

    /**
     * isPlayer
     * 辅助：判断该格是否为有效玩家编号（1 或 2）
     */
    public boolean isPlayer(int c)
    {
        return c == 1 || c == 2;
    }

    /**
     * inBoard
     * 辅助：判断坐标是否在 19x19 棋盘内
     */
    private boolean inBoard(int x, int y)
    {
        return x >= 0 && x < 19 && y >= 0 && y < 19;
    }

    /**
     * caseInBoard
     * 辅助：判断坐标在棋盘内且为空格（可放子）
     */
    private boolean caseInBoard(int x, int y)
    {
        return inBoard(x, y) && MinimaxEngine.board[x][y] == 0;
    }

    /**
     * repCase
     * 在 (x,y) 位置根据方向和当前连续性更新 patternStr，并调整 score（回滚/设置模式值）
     * 参数：
     *  - st: 要设置的模式值（例如 2/3/4）
     * *
     * 说明：只对空格（board==0）进行模式赋值并更新 score.one/score.two。
     */
    private void repCase(int x, int y, int st)
    {
        if (x >= 0 && x < 19 && y >= 0 && y < 19 && MinimaxEngine.board[x][y] == 0)
        {
            if (curTurn == 1)
            {
                score.one -= factor[patternStr1[dirIndex][x][y]];
                if (inBoard(x+dx, y+dy) && MinimaxEngine.board[x+dx][y+dy] == curTurn)
                    patternStr1[dirIndex][x][y] = Math.min(4, patternStr1[dirIndex][x][y] + st);
                else
                    patternStr1[dirIndex][x][y] = st;

                score.one += factor[patternStr1[dirIndex][x][y]];
            }
            else
            {
                score.two -= factor[patternStr2[dirIndex][x][y]];
                if (inBoard(x+dx, y+dy) && MinimaxEngine.board[x+dx][y+dy] == curTurn)
                    patternStr2[dirIndex][x][y] = Math.min(4, patternStr2[dirIndex][x][y] + st);
                else
                    patternStr2[dirIndex][x][y] = st;
                score.two += factor[patternStr2[dirIndex][x][y]];
            }
        }
    }

    /**
     * spawnCase
     * 在没有被当前玩家占据的位置上“生成”一个对手的模式（用于 unfill）
     */
    private void spawnCase(int x, int y, int st)
    {
        if (curTurn == 1)
        {
            // curTurn==1 时，生成给对手（玩家2）
            patternStr2[dirIndex][x][y] = st;
            score.two += factor[st];
        }
        else
        {
            patternStr1[dirIndex][x][y] = st;
            score.one += factor[st];
        }
    }

    /**
     * addCase
     * 在 (x,y) 位置直接把模式设置为 st，并修正对应玩家评分（只在空格上操作）
     */
    private void addCase(int x, int y, int st)
    {
        if ( x >= 0 && x < 19 && y >= 0 && y < 19 && MinimaxEngine.board[x][y] == 0)
        {
            if (curTurn == 1)
            {
                score.one -= factor[patternStr1[dirIndex][x][y]];
                patternStr1[dirIndex][x][y] = st;
                score.one += factor[patternStr1[dirIndex][x][y]];
            }
            else
            {
                score.two -= factor[patternStr2[dirIndex][x][y]];
                patternStr2[dirIndex][x][y] = st;
                score.two += factor[patternStr2[dirIndex][x][y]];
            }
        }
    }

    /**
     * newAlignment
     * 根据两个偏移（dec1/dec2）更新相邻位置的 pattern（用于 connectInternal）
     */
    public void newAlignment(int dec1, int dec2, int st)
    {
        if (caseInBoard(x + (dec1 * dx), y + (dec1 * dy)))
            repCase(x + (dec1 * dx), y + (dec1 * dy), st);

        if (caseInBoard(x + (dec2 * dx), y + (dec2 * dy)))
            repCase(x + (dec2 * dx), y + (dec2 * dy), st);
    }

    /**
     * connectInternal
     * 连接当前新棋子与同一方向上的已有连续棋子，扩展/合并模式并在达到 5 连时标记胜利。
     * （原方法 connect，重命名为 connectInternal）
     */
    public void connectInternal()
    {
        this.currentPattern = MinimaxEngine.board[x][y] == 1 ? patternStr1 : patternStr2;
        if (currentPattern[dirIndex][x][y] == 0)
        {
            if (inBoard(x-dx, y-dy) && MinimaxEngine.board[x-dx][y-dy] == curTurn)
                newAlignment(-2, 2, 3);
            else
                newAlignment(-1, 2, 2);
        }
        else if (currentPattern[dirIndex][x][y] == 2)
        {
            if (inBoard(x-dx, y-dy) && MinimaxEngine.board[x-dx][y-dy] == curTurn)
            {

                if (inBoard(x-2*dx, y-2*dy) && MinimaxEngine.board[x - 2*dx][y - 2*dy] == curTurn)
                    newAlignment(-3, 2, 4);
                else
                    newAlignment(3, -2, 4);

            }
            else
                newAlignment(-1, 3, 3);
        }
        else if (currentPattern[dirIndex][x][y] == 3)
        {
            int decp = 0;
            int decn = 0;
            for (int i = 1; inBoard(x+i*dx, y+i*dy) && MinimaxEngine.board[x+i*dx][y+i*dy] == curTurn; i++)
                decp++;
            for (int i = 1; inBoard(x-i*dx, y-i*dy) && MinimaxEngine.board[x-i*dx][y-i*dy] == curTurn; i++)
                decn++;

            if (decp + decn >= 4)
                saveVictory();
            else
            {
                repCase(x - (decn+1)*dx, y - (decn+1)*dy, 4);
                repCase(x + (decp+1)*dx, y + (decp+1)*dy, 4);
            }

        }
        else if (currentPattern[dirIndex][x][y] == 4)
        {
            saveVictory();
        }
    }

    /**
     * fillTwo
     * 对四个方向处理：清除当前位置已有的模式分值并判断是否需要创建 blocker（边界/远端阻挡）
     */
    public void fillTwo()
    {
        int st;

        for (int i = 0 ; i < 4 ; i++)
        {

            if (patternStr1[i][x][y] != 0)
            {
                st = patternStr1[i][x][y];

                score.one -= (factor[st]);
                patternStr1[i][x][y]=0;
            }

            if (patternStr2[i][x][y] != 0)
            {
                st = patternStr2[i][x][y];

                score.two -= (factor[st]);

                patternStr2[i][x][y]=0;
            }
            if (inBoard(x + 5 * dirOffsets[i][0], y + 5 * dirOffsets[i][1]))
            {
                if (MinimaxEngine.board[x + 5 * dirOffsets[i][0]][y + 5 * dirOffsets[i][1]] == curTurn)
                {
                    createBlocker(i, 1);
                }
            }
            else if (Game.fast_search == 0 && !(x > 4 && x < 14 && y < 4))
                createBlocker(i, 1);

            if (inBoard(x - 5 * dirOffsets[i][0], y - 5 * dirOffsets[i][1]))
            {
                if (MinimaxEngine.board[x - 5 * dirOffsets[i][0]][y - 5 * dirOffsets[i][1]] == curTurn)
                {
                    createBlocker(i, -1);
                }
            }
            else if (Game.fast_search == 0 && !(x > 4 && x < 14 && y < 4))
                createBlocker(i, -1);
        }
    }

    /**
     * createBlocker
     * 创建一个 Blocker 实例并加入 blockerList（用于后续更新/删除）
     * 参数：
     *  - dir: 方向索引（0..3）
     *  - sign: 方向符号（1 或 -1）
     * *
     * 说明：此处保持对 Blocker 提供的原始方法名（bl1/bl2/update_block_info）。
     */
    private void createBlocker(int dir, int sign)
    {
        Blocker res = new Blocker(curTurn, dir, sign);
        res.bl1(x, y);

        if (!inBoard(x + sign * 5 * dirOffsets[dir][0], y + sign * 5 * dirOffsets[dir][1]))
            res.bl2(-1, -1);
        else
            res.bl2(x + 5 * dirOffsets[dir][0] * sign, y + 5 * dirOffsets[dir][1] * sign);

        res.update_block_info();
        this.blockerList.add(res);
    }

    /**
     * updateBlockers
     * 遍历 blockerList，更新每个 blocker 的信息，并删除已失效或与棋盘不匹配的 blocker。
     */
    private void updateBlockers()
    {
        Blocker b;

        for (int i = 0 ; i < this.blockerList.size() ; i++)
        {
            b = this.blockerList.get(i);

            b.update_block_info();
            if (MinimaxEngine.board[b.bl1[0]] [b.bl1[1]] != b.blockcolor || ( b.bl2[0] != -1 &&
                MinimaxEngine.board[b.bl2[0]] [b.bl2[1]] != b.blockcolor))
            {

                this.blockerList.remove(i);
                i--;
            }
            if (i != -1 && Game.fast_search == 0 && b.bl2[0] == -1)
                this.blockerList.remove(i);
        }
    }

    /**
     * saveVictory
     * 标记或切换 victory 标志（用于 connectInternal 中五连检测）
     */
    private void saveVictory()
    {
        if (!this.victory)
        {
            this.victory = true;
            return;
        }
        this.victory=false;
    }

    /**
     * minFour
     * 辅助：根据正负方向计数返回合并后的模式值（上限 4）
     */
    private int minFour(int decx, int decxx)
    {
        if (decx == 1 && decxx == 1)
            return 0;
        if (decx + decxx <=1)
            return 0;
        return Math.min(4, decx + decxx);
    }

    /**
     * unconnectInternal
     * connect 的回滚版本：在撤子时根据当前方向还原周边两个位置的模式值
     */
    public void unconnectInternal()
    {

        int decp;
        int decn;
        int decpp;
        int decnn;

        decp = 0;
        decn = 0;
        decpp = 0;
        decnn = 0;

        for (int i = 1; inBoard(x+i*dx, y+i*dy) && MinimaxEngine.board[x + i * dx][y+ i * dy] == curTurn; i++)
            decp++;
        for (int i = 1; inBoard(x-i*dx, y-i*dy) && MinimaxEngine.board[x- i *dx][y - i *dy] == curTurn; i++)
            decn++;
        if (inBoard(x+(decp + 1)*dx, y+(decp + 1)*dy) && MinimaxEngine.board[x+(decp + 1)*dx][y+(decp + 1)*dy] == 0)
        {
            for (int i = decp+1; inBoard(x+(i+1)*dx, y+(i+1)*dy) && MinimaxEngine.board[x+(i+1)*dx][y+(i+1)*dy] == curTurn; i++)
                decpp++;
        }

        if (inBoard(x-(decn + 1)*dx, y-(decn + 1)*dy) && MinimaxEngine.board[x-(decn + 1)*dx][y-(decn + 1)*dy] == 0)
        {
            for (int i = decn+1; inBoard(x-(i+1)*dx, y-(i+1)*dy) && MinimaxEngine.board[x-(i+1)*dx][y-(i+1)*dy] == curTurn; i++)
                decnn++;
        }

        addCase(x + (decp + 1) * dx, y + (decp + 1) * dy, minFour(decp, decpp));
        addCase(x - (decn + 1) * dx, y - (decn + 1) * dy, minFour(decn, decnn));

        if (decp + decn == 3)
            addCase(x, y, 3);
        else if (decp == 2 || decn == 2)
            addCase(x, y, 2);
        else if (decp + decn >= 4)
            addCase(x, y, 4);

    }

    /**
     * unfill
     * 撤子时根据相邻敌方连续段生成对手的模式（spawnCase）
     */
    public void unFill()
    {
        int val = curTurn == 1 ? 2 : 1;
        int nbp = 0;
        int nbn = 0;

        for (int i = 1; inBoard(x+i*dx, y+i*dy) && MinimaxEngine.board[x + i * dx][y+ i * dy] == val ; i++)
            nbp++;
        for (int i = 1; inBoard(x-i*dx, y-i*dy) && MinimaxEngine.board[x- i *dx][y - i *dy] == val ; i++)
            nbn++;

        if (nbn == 0 && nbp == 0)
            return;

        if (nbn < 2) nbn = 0;
        if (nbp < 2) nbp = 0;

        spawnCase(x, y, Math.min(4, nbp + nbn));
    }

    /**
     * unfillZero
     * 回滚时调整临近空格的模式（原名 unfill0）
     */
    public void unfillZero(int x, int y, int dir)
    {
        int cmp;
        if (inBoard(x+dirOffsets[dir][0], y+dirOffsets[dir][1]) &&
        MinimaxEngine.board[x+dirOffsets[dir][0]][y+dirOffsets[dir][1]] == 0)
        {
            if (curTurn == 1 && patternStr1[dir][x+dirOffsets[dir][0]][y+dirOffsets[dir][1]] > 2
            || curTurn == 2 && patternStr2[dir][x+dirOffsets[dir][0]][y+dirOffsets[dir][1]] > 2)
            {
                cmp = 0;
                for (int i = 2; inBoard(x+i*dirOffsets[dir][0], y+i*dirOffsets[dir][1])
                && MinimaxEngine.board[x+i*dirOffsets[dir][0]][y+i*dirOffsets[dir][1]] == curTurn; i++)
                    cmp++;
                if (curTurn == 1)
                {
                    score.one -= factor[patternStr1[dir][x+dirOffsets[dir][0]][y+dirOffsets[dir][1]]];
                    patternStr1[dir][x+dirOffsets[dir][0]][y+dirOffsets[dir][1]]=cmp;
                    score.one += factor[cmp];
                }
                else
                {
                    score.two -= factor[patternStr2[dir][x+dirOffsets[dir][0]][y+dirOffsets[dir][1]]];
                    patternStr2[dir][x+dirOffsets[dir][0]][y+dirOffsets[dir][1]]=cmp;
                    score.two += factor[cmp];
                }
            }
        }

        if (inBoard(x-dirOffsets[dir][0], y-dirOffsets[dir][1]) &&
        MinimaxEngine.board[x-dirOffsets[dir][0]][y-dirOffsets[dir][1]] == 0)
        {
            if (curTurn == 1 && patternStr1[dir][x-dirOffsets[dir][0]][y-dirOffsets[dir][1]] > 2
            || curTurn == 2 && patternStr2[dir][x-dirOffsets[dir][0]][y-dirOffsets[dir][1]] > 2)
            {
                cmp = 0;
                for (int i = 2; inBoard(x-i*dirOffsets[dir][0], y-i*dirOffsets[dir][1])
                && MinimaxEngine.board[x-i*dirOffsets[dir][0]][y-i*dirOffsets[dir][1]] == curTurn; i++)
                    cmp++;
                if (curTurn == 1)
                {
                    score.one -= factor[patternStr1[dir][x-dirOffsets[dir][0]][y-dirOffsets[dir][1]]];
                    patternStr1[dir][x-dirOffsets[dir][0]][y-dirOffsets[dir][1]]=cmp;
                    score.one += factor[cmp];
                }
                else
                {
                    score.two -= factor[patternStr2[dir][x-dirOffsets[dir][0]][y-dirOffsets[dir][1]]];
                    patternStr2[dir][x-dirOffsets[dir][0]][y-dirOffsets[dir][1]]=cmp;
                    score.two += factor[cmp];
                }
            }
        }
    }

    // ----------------------
    // 清理 / 重置方法
    // ----------------------

    /**
     * resetStr
     * 将所有方向模式数组置零，清空 blocker 列表并重置 victory 与 score
     * （保留原名以便外部可能直接调用）
     */
    public void resetStr()
    {
        for (int d = 0 ; d < 4 ; d++)
        {
            for (int i  = 0 ; i < 19 ; i++)
            {
                for (int j = 0 ; j < 19 ; j++)
                {
                    this.patternStr1[d][i][j] = 0;
                    this.patternStr2[d][i][j] = 0;
                }
            }
        }
        this.victory=false;
        this.blockerList.clear();
        this.score.reset();
    }

    // ----------------------
    // 已删除/移除的方法说明（记录）
    // ----------------------
    // 为了精简类并避免与项目其它部分耦合，已移除以下仅作为调试/打印或未被项目其它文件调用的函数：
    // - display_str / display / display_blockers / noCase
    // - check_str / iscapt / check_capt
    // 这些函数为输出/调试用途，删除不会影响 MinimaxEngine 搜索逻辑。如需调试可单独恢复。
}