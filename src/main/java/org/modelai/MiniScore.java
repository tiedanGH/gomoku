package org.modelai;
import java.util.ArrayList;

import org.utils.*;

public class MiniScore {

    Score score;
    int [][][] str;
    int [][][] patternStr1;
    int [][][] patternStr2;
    int curTurn;
    int opponant;
    int x;
    int y;
    int dx;
    int dy;
    int dir;

    boolean victory;

    ArrayList<Blocker> blocklist = new ArrayList<>();

    static int [] factor = {0, 0, 2, 10, 25, 0, 0, 0, 0, 0};
    static int [][] ddir = {{1, 0}, {0, 1}, {1, 1}, {-1, 1}};

    public MiniScore()
    {
        this.score = new Score();
        this.patternStr1 = new int[4][19][19];
        this.patternStr2 = new int[4][19][19];
        this.victory = false;
    }

    //display utils
    boolean noCase(int [][] str)
    {
        for (int i = 0 ; i < 19 ; i++)
        {
            for (int j = 0 ; j < 19 ; j++)
                if (str[i][j] != 0)
                    return false;
        }
        return true;
    }
    //display function
    public void display_str(int player)
    {
        int [][][] choice = player == 1 ? patternStr1 : patternStr2;

        String[] dir = {"VERTICAL","HORIZONTAL", "DIAGPOS", "DIAGNEG"};

        for (int d = 0 ; d < 4 ; d++)
        {
            if (noCase(choice[d]))
                continue;
            System.out.printf("player %d, direction %s\n", player, dir[d]);
            for (int i  = 0 ; i < 19 ; i++)
            {
                for (int j = 0 ; j < 19 ; j++)
                {
                    System.out.printf("%2d", choice[d][i][j]);
                }
                System.out.println();
            }
        }
    }

    //display function
    public void display()
    {
        display_str(1);
        display_str(2);
        display_blockers();
    }

    //display function
    public void display(boolean blk)
    {
        display_str(1);
        display_str(2);
        if (blk)
            display_blockers();
    }

    //display function
    public void display_blockers()
    {
        Blocker b;
        if (!this.blocklist.isEmpty())
        {
            for (Blocker blocker : blocklist) {
                b = blocker;
                System.out.printf("blockers [%d %d]  [%d %d] ",
                        b.bl1[0], b.bl1[1], b.bl2[0], b.bl2[1]);
                for (int j = 0; j < b.rank; j++) {
                    System.out.printf("|%d %d|", b.cases[j][0], b.cases[j][1]);
                }
                System.out.printf("\n");
            }

        }
        else
        {
            System.out.println("no blockers");
        }
    }

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
        this.blocklist.clear();
        this.score.reset();
    }

    //utils
    private boolean inGoban(int x, int y) 
    {
        return x >= 0 && x < 19 && y >= 0 && y < 19;
    }

    //utils
    private boolean caseInGoban(int x, int y)
    {
        return inGoban(x, y) && MinMax.board[x][y] == 0;
    }

    //utils
    public boolean isPlayer(int c)
    {
        if (c == 1 || c == 2)
            return true;
        return false;
    }

    private void repCase(int x, int y, int st)
    {
        if (x >= 0 && x < 19 && y >= 0 && y < 19 && MinMax.board[x][y] == 0)
        {
            if (curTurn == 1)
            {
                score.one -= factor[patternStr1[dir][x][y]];
                if (inGoban(x+dx, y+dy) && MinMax.board[x+dx][y+dy] == curTurn)
                    patternStr1[dir][x][y] = Math.min(4, patternStr1[dir][x][y] + st);
                else
                    patternStr1[dir][x][y] = st;

                score.one += factor[patternStr1[dir][x][y]];
            }
            else
            {
                score.two -= factor[patternStr2[dir][x][y]];
                if (inGoban(x+dx, y+dy) && MinMax.board[x+dx][y+dy] == curTurn)
                    patternStr2[dir][x][y] = Math.min(4, patternStr2[dir][x][y] + st);
                else
                    patternStr2[dir][x][y] = st;
                score.two += factor[patternStr2[dir][x][y]];
            }
        }
    }

    private void spownCase(int x, int y, int st)
    {
        if (curTurn == 1)
        {
            patternStr2[dir][x][y] = st;
            score.two += factor[st];
        }
        else
        {
            patternStr1[dir][x][y] = st;
            score.one += factor[st];
        }
    }

    private void add_case(int x, int y, int st)
    {
        if ( x >= 0 && x < 19 && y >= 0 && y < 19 && MinMax.board[x][y] == 0)
        {
            if (curTurn == 1)
            {
                score.one -= factor[patternStr1[dir][x][y]];
                patternStr1[dir][x][y] = st;
                score.one += factor[patternStr1[dir][x][y]];
            }
            else
            {
                score.two -= factor[patternStr2[dir][x][y]];
                patternStr2[dir][x][y] = st;
                score.two += factor[patternStr2[dir][x][y]];
            }
        }
    }

    public void new_alignment(int dec1, int dec2, int st)
    {


        if (caseInGoban(x + (dec1 * dx), y + (dec1 * dy)))
            repCase(x + (dec1 * dx), y + (dec1 * dy), st);

        if (caseInGoban(x + (dec2 * dx), y + (dec2 * dy)))
            repCase(x + (dec2 * dx), y + (dec2 * dy), st);
    }

    public void connect()
    {
        this.str = MinMax.board[x][y] == 1 ? patternStr1 : patternStr2;
        if (str[dir][x][y] == 0)
        {
            if (inGoban(x-dx, y-dy) && MinMax.board[x-dx][y-dy] == curTurn)
                new_alignment(-2, 2, 3);
            else
                new_alignment(-1, 2, 2);
        }
        else if (str[dir][x][y] == 2)
        {
            if (inGoban(x-dx, y-dy) && MinMax.board[x-dx][y-dy] == curTurn)
            {

                if (inGoban(x-2*dx, y-2*dy) && MinMax.board[x - 2*dx][y - 2*dy] == curTurn)
                    new_alignment(-3, 2, 4);
                else
                    new_alignment(3, -2, 4);

            }
            else
                new_alignment(-1, 3, 3);
        }
        else if (str[dir][x][y] == 3)
        {
            int decp = 0;
            int decn = 0;
            for (int i = 1; inGoban(x+i*dx, y+i*dy) && MinMax.board[x+i*dx][y+i*dy] == curTurn; i++)
                decp++;
            for (int i = 1; inGoban(x-i*dx, y-i*dy) && MinMax.board[x-i*dx][y-i*dy] == curTurn; i++)
                decn++;

            if (decp + decn >= 4)
                save_victory();
            else
            {
                repCase(x - (decn+1)*dx, y - (decn+1)*dy, 4);
                repCase(x + (decp+1)*dx, y + (decp+1)*dy, 4);
            }

        }
        else if (str[dir][x][y] == 4)
        {
            save_victory();
        }
    }

    public void fill2()
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
            if (inGoban(x + 5 * ddir[i][0], y + 5 * ddir[i][1]))
            {
                if (MinMax.board[x + 5 * ddir[i][0]][y + 5 * ddir[i][1]] == curTurn)
                {
                    create_blocker(i, 1);   
                }
            }
            else if (Game.fast_search == 0 && !(x > 4 && x < 14 && y < 4))
                create_blocker(i, 1);

            if (inGoban(x - 5 * ddir[i][0], y - 5 * ddir[i][1]))
            { 
                if (MinMax.board[x - 5 * ddir[i][0]][y - 5 * ddir[i][1]] == curTurn)
                {
                    create_blocker(i, -1);   
                }
            }
            else if (Game.fast_search == 0 && !(x > 4 && x < 14 && y < 4))
                create_blocker(i, -1);
        }
    }

    private void create_blocker(int dir, int sig)
    {
        Blocker res = new Blocker(curTurn, dir, sig);
        res.bl1(x, y);

        if (!inGoban(x+ sig* 5*ddir[dir][0], y + sig * 5 * ddir[dir][1]))
            res.bl2(-1, -1);
        else
            res.bl2(x+ 5*ddir[dir][0]*sig, y + 5 * ddir[dir][1]*sig);

        res.update_block_info();
        this.blocklist.add(res);
    }

    private void update_blocker()
    {
        Blocker b;

        for (int i = 0 ; i < this.blocklist.size() ; i++)
        {
            b = this.blocklist.get(i);

            b.update_block_info();
            if (MinMax.board[b.bl1[0]] [b.bl1[1]] != b.blockcolor || ( b.bl2[0] != -1 &&
                MinMax.board[b.bl2[0]] [b.bl2[1]] != b.blockcolor))
            {

                this.blocklist.remove(i);
                i--;
            }
            if (i != -1 && Game.fast_search == 0 && b.bl2[0] == -1)
                this.blocklist.remove(i);
        }
    }

    private void save_victory()
    {
            if (!this.victory)
            {
                this.victory = true;
                return;
            }
            this.victory=false;
    }

    private int min4(int decx, int decxx)
    {
        if (decx == 1 && decxx == 1)
            return 0;
        if (decx + decxx <=1)
            return 0;
        return Math.min(4, decx + decxx);
    }
    
    public void unconnect()
    {

        int decp;
        int decn;
        int decpp;
        int decnn;
    
        decp = 0;
        decn = 0;
        decpp = 0;
        decnn = 0;

        for (int i = 1; inGoban(x+i*dx, y+i*dy) && MinMax.board[x + i * dx][y+ i * dy] == curTurn; i++)
            decp++;
        for (int i = 1; inGoban(x-i*dx, y-i*dy) && MinMax.board[x- i *dx][y - i *dy] == curTurn; i++)
            decn++;
        if (inGoban(x+(decp + 1)*dx, y+(decp + 1)*dy) && MinMax.board[x+(decp + 1)*dx][y+(decp + 1)*dy] == 0)
        {
            for (int i = decp+1; inGoban(x+(i+1)*dx, y+(i+1)*dy) && MinMax.board[x+(i+1)*dx][y+(i+1)*dy] == curTurn; i++)
                decpp++;
        }
    
        if (inGoban(x-(decn + 1)*dx, y-(decn + 1)*dy) && MinMax.board[x-(decn + 1)*dx][y-(decn + 1)*dy] == 0)
        {
            for (int i = decn+1; inGoban(x-(i+1)*dx, y-(i+1)*dy) && MinMax.board[x-(i+1)*dx][y-(i+1)*dy] == curTurn; i++)
                decnn++;
        }

        add_case(x + (decp + 1) * dx, y + (decp + 1) * dy, min4(decp, decpp));
        add_case(x - (decn + 1) * dx, y - (decn + 1) * dy, min4(decn, decnn));

        if (decp + decn == 3)
            add_case(x, y, 3);
        else if (decp == 2 || decn == 2)
            add_case(x, y, 2);
        else if (decp + decn >= 4)
            add_case(x, y, 4);

    }

    public void unfill()
    {
        int val = curTurn == 1 ? 2 : 1;
        int nbp = 0;
        int nbn = 0;

        for (int i = 1; inGoban(x+i*dx, y+i*dy) && MinMax.board[x + i * dx][y+ i * dy] == val ; i++)
            nbp++;
        for (int i = 1; inGoban(x-i*dx, y-i*dy) && MinMax.board[x- i *dx][y - i *dy] == val ; i++)
            nbn++;

        if (nbn == 0 && nbp == 0)
            return;

        if (nbn < 2) nbn = 0;
        if (nbp < 2) nbp = 0;

        spownCase(x, y, Math.min(4, nbp + nbn));
    }

    public void analyseUnmove(int x, int y, int turn)
    {

        this.curTurn = turn;
        this.str = turn == 1 ? this.patternStr1 : this.patternStr2;
        this.opponant = turn == 1 ? 2 : 1;
        this.x=x;
        this.y=y;

        if ((x + 1 != 19 && isPlayer(MinMax.board[x+1][y])) || (x - 1 != -1 && isPlayer(MinMax.board[x-1][y])))
        {
            dir = 0;
            dx = 1; dy = 0;
            if (x + 1 != 19 && MinMax.board[x+1][y] == curTurn)
                unconnect();

            else if (x - 1 != -1 && MinMax.board[x-1][y] == curTurn)
            {
                dx=-1;
                unconnect();
            }
            unfill();
        }

        if ((y + 1 != 19 && isPlayer(MinMax.board[x][y+1])) || (y - 1 != -1 && isPlayer(MinMax.board[x][y-1])))
        {
            dir = 1;
            dx = 0; dy = 1;
            if (y + 1 != 19 && MinMax.board[x][y+1] == curTurn)
                unconnect();

            else if (y - 1 != -1 && MinMax.board[x][y-1] == curTurn)
            {
                dy=-1;
                unconnect();
            }
            unfill();
        }

        if ((x + 1 != 19 && y + 1 != 19 && isPlayer(MinMax.board[x+1][y+1])) || ( x - 1 != -1 && y - 1 != -1 && isPlayer(MinMax.board[x-1][y-1])))
        {
            dir = 2;
            dx = 1; dy = 1;
            if (x + 1 != 19 && y + 1 != 19 && MinMax.board[x+1][y+1] == curTurn)
                unconnect();

            else if ( x - 1 != -1 && y - 1 != -1 && MinMax.board[x-1][y-1] == curTurn)
            {
                dx=-1; dy=-1;
                unconnect();
            }
            unfill();
        }

        if (x + 1 != 19 && y - 1 != -1 && isPlayer(MinMax.board[x+1][y-1]) || (x - 1 != -1 && y + 1 != 19 && isPlayer(MinMax.board[x-1][y+1])))
        {
            dir = 3;
            dx = 1; dy = -1;
            if (x + 1 != 19 && y - 1 != -1 && MinMax.board[x+1][y-1] == curTurn)
                unconnect();  

            else if (x - 1 != -1 && y + 1 != 19 && MinMax.board[x-1][y+1] == curTurn)
            {
                dx = -1; dy=1;
                unconnect();
            }
            
            unfill();
        }

        for (int i = 0; i < 4 ; i++)
        {
            unfill0(x, y, i);
        }

        update_blocker();
    }


    public void unfill0(int x, int y, int dir)
    {
        int cmp;
        if (inGoban(x+ddir[dir][0], y+ddir[dir][1]) &&
        MinMax.board[x+ddir[dir][0]][y+ddir[dir][1]] == 0)
        {
            if (curTurn == 1 && patternStr1[dir][x+ddir[dir][0]][y+ddir[dir][1]] > 2
            || curTurn == 2 && patternStr2[dir][x+ddir[dir][0]][y+ddir[dir][1]] > 2)
            {
                cmp = 0;
                for (int i = 2; inGoban(x+i*ddir[dir][0], y+i*ddir[dir][1]) 
                && MinMax.board[x+i*ddir[dir][0]][y+i*ddir[dir][1]] == curTurn; i++)
                    cmp++;
                if (curTurn == 1)
                {
                    score.one -= factor[patternStr1[dir][x+ddir[dir][0]][y+ddir[dir][1]]];
                    patternStr1[dir][x+ddir[dir][0]][y+ddir[dir][1]]=cmp;
                    score.one += factor[cmp];
                }
                else
                {
                    score.two -= factor[patternStr2[dir][x+ddir[dir][0]][y+ddir[dir][1]]];
                    patternStr2[dir][x+ddir[dir][0]][y+ddir[dir][1]]=cmp;
                    score.two += factor[cmp];
                }
            }
        }

        if (inGoban(x-ddir[dir][0], y-ddir[dir][1]) &&
        MinMax.board[x-ddir[dir][0]][y-ddir[dir][1]] == 0)
        {
            if (curTurn == 1 && patternStr1[dir][x-ddir[dir][0]][y-ddir[dir][1]] > 2
            || curTurn == 2 && patternStr2[dir][x-ddir[dir][0]][y-ddir[dir][1]] > 2)
            {
                cmp = 0;
                for (int i = 2; inGoban(x-i*ddir[dir][0], y-i*ddir[dir][1]) 
                && MinMax.board[x-i*ddir[dir][0]][y-i*ddir[dir][1]] == curTurn; i++)
                    cmp++;
                if (curTurn == 1)
                {
                    score.one -= factor[patternStr1[dir][x-ddir[dir][0]][y-ddir[dir][1]]];
                    patternStr1[dir][x-ddir[dir][0]][y-ddir[dir][1]]=cmp;
                    score.one += factor[cmp];
                }
                else
                {
                    score.two -= factor[patternStr2[dir][x-ddir[dir][0]][y-ddir[dir][1]]];
                    patternStr2[dir][x-ddir[dir][0]][y-ddir[dir][1]]=cmp;
                    score.two += factor[cmp];
                }
            }
        }
    }

    public void analyseMove(int x, int y, int turn)
    {
        this.curTurn = turn;
        this.opponant = turn == 1 ? 2 : 1;
        this.str = turn == 1 ? patternStr1 : patternStr2;
        this.x = x;
        this.y = y;

        if (x + 1 != 19 && MinMax.board[x+1][y] == curTurn)
        {
            dir=0;
            dx=1;dy=0;
            connect();

        }
        else if (x - 1 != -1 && MinMax.board[x-1][y] == curTurn)
        {
            dir=0;
            dx=-1; dy=0;
            connect();
        }

        if (y + 1 != 19 && MinMax.board[x][y+1] == curTurn)
        {
            dir=1;
            dx=0;dy=1;
            connect();
        }
        else if (y - 1 != -1 && MinMax.board[x][y-1] == curTurn)
        {
            dir = 1;
            dx=0;dy=-1;
            connect();
        }

        if (x + 1 != 19 && y + 1 != 19 && MinMax.board[x+1][y+1] == curTurn)
        {
            dir = 2;
            dx=1;dy=1;
            connect();
        }
        else if (x - 1 != -1 && y - 1 != -1 && MinMax.board[x-1][y-1] == curTurn)
        {
            dir = 2;
            dx=-1;dy=-1;
            connect();
        }

        if (x + 1 != 19 && y - 1 != -1 && MinMax.board[x+1][y-1] == curTurn)
        {
            dir = 3;
            dx=1;dy=-1;
            connect();
        }
        else if (x - 1 != -1 && y + 1 != 19 && MinMax.board[x-1][y+1] == curTurn)
        {
            dir = 3;
            dx=-1;dy=1;
            connect();
        }
        fill2();
        update_blocker();
    }


    //debug function
    public boolean check_str()
    {
        int score1 = 0;
        int score2 = 0;
    
        for (int d = 0 ; d < 4 ; d++)
        {
            for (int i = 0 ; i < 19 ; i++)
            {
                for (int j = 0 ; j < 19 ; j++)
                {
                    if (patternStr1[d][i][j] != 0)
                        score1 += factor[patternStr1[d][i][j]];
                    if (patternStr2[d][i][j] != 0)
                        score2 += factor[patternStr2[d][i][j]];
                    if (patternStr1[d][i][j] >= 5 || patternStr2[d][i][j] >=5)
                        return false;
                    if (patternStr1[d][i][j] != 0 && MinMax.board[i][j] != 0)
                        return false;
                    if (patternStr2[d][i][j] != 0 && MinMax.board[i][j] != 0)
                        return false;
                }
            }
        }
        if (score1 != score.one || score2 != score.two)
        {
            System.out.printf("%d %d found (%d %d)\n", score.one, score.two, score1, score2);
            return false;
        }
        return true;
    }

    //debug function
    private int iscapt(int x, int y)
    {
        int res = 0;
        int o = MinMax.board[x][y] == 1 ? 2 : 1;

        for (int d = 0 ; d < 4 ; d++)
        {
            if ( inGoban(x+3*ddir[d][0], y+3*ddir[d][1]) &&
                MinMax.board[x+ddir[d][0]][y+ddir[d][1]] == o && MinMax.board[x+2*ddir[d][0]][y+2*ddir[d][1]] == o 
            && MinMax.board[x+3*ddir[d][0]][y+3*ddir[d][1]] ==0)
                res++;
            if (
                inGoban(x-3*ddir[d][0], y-3*ddir[d][1]) &&
                MinMax.board[x-ddir[d][0]][y-ddir[d][1]] == o && MinMax.board[x-2*ddir[d][0]][y-2*ddir[d][1]] == o 
            && MinMax.board[x-3*ddir[d][0]][y-3*ddir[d][1]]==0)
                res++;
        }
        return res;
    }

    //debug function
    public boolean check_capt()
    {
        int capt1 = 0;
        int capt2 = 0;
        int nb_cap;

        for (int i = 0 ; i < 19 ; i++)
        {
            for (int j = 0 ; j < 19 ; j ++)
            {
                if (MinMax.board[i][j] != 0)
                {
                    nb_cap = iscapt(i, j);
                    if (nb_cap != 0) 
                    {
                        if (MinMax.board[i][j] == 1)
                            capt1+=nb_cap;
                        else
                            capt2+=nb_cap;
                    }
                }
            }
        }

        if (!check_str())
        {
            System.out.println("check str wrong");
            return true;
        }
        return false;
    }
}