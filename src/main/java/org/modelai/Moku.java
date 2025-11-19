package org.modelai;

/*
 * Moku
 *
 * 中文说明：
 * Moku 继承 MinMax，用于 Gomoku（五子棋）专用的节点实现。
 */

public class Moku extends MinMax {

    public Moku()
    {
        // 初始化共享棋盘与评估器、候选管理器
        board = new int[19][19];
        this.move = new Candidate.Coordinate(-1, -1);
        miniScoreSim = new MiniScore();
        candidate = new Candidate(false);
        positionCounter = 0;
        moveCount = 0;
    }

    public boolean play(Candidate.Coordinate c, int player)
    {
        if (checkWin4Dir(c.x, c.y, player))
            return true;
        this.move = c;
        board[c.x][c.y] = player;

        candidate.save(c);
        miniScoreSim.analyseMove(c.x, c.y, player);

        return false;
    }

    public void undo(Candidate.Coordinate c, int depth)
    {
        super.undo(c, depth);
    }

    public Moku(int len)
    {
        this.len = len + 1;
        this.move = new Candidate.Coordinate(-1, -1);
        this.candidate = new Candidate(false);
    }

    public float minimax(int depth, int turn, int player)
    {
        int nb_candidates;

        nb_candidates = candidate.oldLoad(depth, player);

        if (depth == 0)
        {
            positionCounter++;
            return eval(player, len, turn);
        }

        values = new float[nb_candidates];

        for (int i = 0 ; i < nb_candidates ; i++)
        {
            Moku m = new Moku(this.len);
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

    public float minimaxAB(int depth, int turn, int player, float alpha, float beta)
    {
        int nb_candidates;
        float cur_alpha;
        float cur_beta;
        float res;

        miniScoreSim.curTurn = turn;

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
            Moku m = new Moku(this.len);
            Candidate.Coordinate cand = candidate.list.get(i);
            boolean isMaxLayer = (turn == player);

            // 统一计算递归窗口
            float recAlpha = isMaxLayer ? Math.max(alpha, cur_alpha) : alpha;
            float recBeta  = isMaxLayer ? beta : Math.min(beta, cur_beta);

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

            if (isMaxLayer)
            {
                cur_alpha = Math.max(cur_alpha, res);
                if (cur_alpha > beta) // beta cut
                    return cur_alpha;
            }
            else
            {
                cur_beta = Math.min(cur_beta, res);
                if (alpha > cur_beta) // alpha cut
                    return cur_beta;
            }
        }

        if (turn == player)
            return max(values);
        else
            return min(values);
    }
}
