package main.ai;
import java.util.ArrayList;

import main.utils.Blocker;
import main.utils.Score;

// Evaluator
// Responsible for evaluating the board state, updating patterns and scores
public class Evaluator {

    // Global score object
    Score score;
    // Temporary reference to the current player's directional pattern array
    int [][][] currentPattern;
    // Directional pattern arrays for both players
    int [][][] patternStr1;
    int [][][] patternStr2;
    // Current turn being simulated
    int curTurn;

    // Current coordinates being processed
    int x;
    int y;
    // Current directional increments
    int dx;
    int dy;

    // Current direction index (0..3)
    int dirIndex;

    // Whether a victory (five in a row) is detected
    boolean victory;

    // Current list of blockers in this direction
    ArrayList<Blocker> blockerList = new ArrayList<>();

    // Mapping from pattern type to score
    static int [] factor = {0, 0, 2, 10, 25, 0, 0, 0, 0, 0};

    // Four directional offsets (horizontal, vertical, major diagonal, minor diagonal)
    static int [][] dirOffsets = {{1, 0}, {0, 1}, {1, 1}, {-1, 1}};

    /**
     * Constructor: initialize evaluator, score, and pattern arrays
     */
    public Evaluator()
    {
        this.score = new Score();
        this.patternStr1 = new int[4][19][19];
        this.patternStr2 = new int[4][19][19];
        this.victory = false;
    }

    /**
     * analyseMove
     * External interface: called when a new stone is placed.
     * Updates internal patterns and scores incrementally.
     */
    public void analyseMove(int x, int y, int turn)
    {
        // Preserve original behavior while calling renamed internal helpers
        this.curTurn = turn;
        this.currentPattern = turn == 1 ? patternStr1 : patternStr2;
        this.x = x;
        this.y = y;

        // Check and connect in all four directions
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

        // Clear pattern values at current position or create blockers
        fillTwo();

        // Update blocker list (remove invalid ones)
        updateBlockers();
    }

    /**
     * analyseUndo
     * External interface: called when a stone is removed.
     * Rolls back internal pattern and score changes.
     */
    public void analyseUndo(int x, int y, int turn)
    {
        this.curTurn = turn;
        this.currentPattern = turn == 1 ? this.patternStr1 : this.patternStr2;
        this.x = x;
        this.y = y;

        // Roll back direction connections and unFill in all four directions
        if ((x + 1 != 19 && isPlayer(MinimaxEngine.board[x+1][y])) || (x - 1 != -1 && isPlayer(MinimaxEngine.board[x-1][y])))
        {
            dirIndex = 0;
            dx = 1; dy = 0;
            if (x + 1 != 19 && MinimaxEngine.board[x+1][y] == curTurn)
                unConnectInternal();
            else if (x - 1 != -1 && MinimaxEngine.board[x-1][y] == curTurn)
            {
                dx=-1;
                unConnectInternal();
            }
            unFill();
        }

        if ((y + 1 != 19 && isPlayer(MinimaxEngine.board[x][y+1])) || (y - 1 != -1 && isPlayer(MinimaxEngine.board[x][y-1])))
        {
            dirIndex = 1;
            dx = 0; dy = 1;
            if (y + 1 != 19 && MinimaxEngine.board[x][y+1] == curTurn)
                unConnectInternal();
            else if (y - 1 != -1 && MinimaxEngine.board[x][y-1] == curTurn)
            {
                dy=-1;
                unConnectInternal();
            }
            unFill();
        }

        if ((x + 1 != 19 && y + 1 != 19 && isPlayer(MinimaxEngine.board[x+1][y+1])) || ( x - 1 != -1 && y - 1 != -1 && isPlayer(MinimaxEngine.board[x-1][y-1])))
        {
            dirIndex = 2;
            dx = 1; dy = 1;
            if (x + 1 != 19 && y + 1 != 19 && MinimaxEngine.board[x+1][y+1] == curTurn)
                unConnectInternal();
            else if ( x - 1 != -1 && y - 1 != -1 && MinimaxEngine.board[x-1][y-1] == curTurn)
            {
                dx=-1; dy=-1;
                unConnectInternal();
            }
            unFill();
        }

        if (x + 1 != 19 && y - 1 != -1 && isPlayer(MinimaxEngine.board[x+1][y-1]) || (x - 1 != -1 && y + 1 != 19 && isPlayer(MinimaxEngine.board[x-1][y+1])))
        {
            dirIndex = 3;
            dx = 1; dy = -1;
            if (x + 1 != 19 && y - 1 != -1 && MinimaxEngine.board[x+1][y-1] == curTurn)
                unConnectInternal();
            else if (x - 1 != -1 && y + 1 != 19 && MinimaxEngine.board[x-1][y+1] == curTurn)
            {
                dx = -1; dy=1;
                unConnectInternal();
            }

            unFill();
        }

        // Roll back zero-pattern adjustments for surrounding empty positions
        for (int i = 0; i < 4 ; i++)
        {
            unFillZero(x, y, i);
        }

        updateBlockers();
    }

    /**
     * isPlayer
     * Helper: check whether a cell contains a valid player (1 or 2)
     */
    public boolean isPlayer(int c)
    {
        return c == 1 || c == 2;
    }

    /**
     * inBoard
     * Helper: check whether coordinates are inside the 19×19 board
     */
    private boolean inBoard(int x, int y)
    {
        return x >= 0 && x < 19 && y >= 0 && y < 19;
    }

    /**
     * caseInBoard
     * Helper: check whether coordinates are inside board AND empty
     */
    private boolean caseInBoard(int x, int y)
    {
        return inBoard(x, y) && MinimaxEngine.board[x][y] == 0;
    }

    /**
     * repCase
     * Update patternStr at (x,y) based on direction and current continuity,
     * and adjust score accordingly (rollback or set).
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
     * Generate the opponent’s pattern on this empty cell during unFill.
     */
    private void spawnCase(int x, int y, int st)
    {
        if (curTurn == 1)
        {
            // curTurn==1 → generate for opponent (player 2)
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
     * Directly sets pattern value on (x,y) and adjusts score
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
     * Update adjacent pattern values based on two offsets (dec1/dec2),
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
     * Connect the newly placed stone with adjacent stones in the same direction.
     * Extends and merges patterns, and detects five-in-a-row.
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
     * For each direction:
     * - Clear any existing pattern values on this cell.
     * - Check whether blockers should be created.
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
            else if (Game.fastSearch == 0 && !(x > 4 && x < 14 && y < 4))
                createBlocker(i, 1);

            if (inBoard(x - 5 * dirOffsets[i][0], y - 5 * dirOffsets[i][1]))
            {
                if (MinimaxEngine.board[x - 5 * dirOffsets[i][0]][y - 5 * dirOffsets[i][1]] == curTurn)
                {
                    createBlocker(i, -1);
                }
            }
            else if (Game.fastSearch == 0 && !(x > 4 && x < 14 && y < 4))
                createBlocker(i, -1);
        }
    }

    /**
     * createBlocker
     * Create a Blocker instance and add it to blockerList.
     */
    private void createBlocker(int dir, int sign)
    {
        Blocker res = new Blocker(curTurn, dir, sign);
        res.bl1(x, y);

        if (!inBoard(x + sign * 5 * dirOffsets[dir][0], y + sign * 5 * dirOffsets[dir][1]))
            res.bl2(-1, -1);
        else
            res.bl2(x + 5 * dirOffsets[dir][0] * sign, y + 5 * dirOffsets[dir][1] * sign);

        res.updateBlockInfo();
        this.blockerList.add(res);
    }

    /**
     * updateBlockers
     * Update each blocker in blockerList and remove invalid or mismatched blockers.
     */
    private void updateBlockers()
    {
        Blocker b;

        for (int i = 0 ; i < this.blockerList.size() ; i++)
        {
            b = this.blockerList.get(i);

            b.updateBlockInfo();
            if (MinimaxEngine.board[b.bl1[0]] [b.bl1[1]] != b.blockColor || ( b.bl2[0] != -1 &&
                MinimaxEngine.board[b.bl2[0]] [b.bl2[1]] != b.blockColor))
            {

                this.blockerList.remove(i);
                i--;
            }
            if (i != -1 && Game.fastSearch == 0 && b.bl2[0] == -1)
                this.blockerList.remove(i);
        }
    }

    /**
     * saveVictory
     * Set or toggle victory flag.
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
     * Helper: returns merged pattern type based on positive/negative direction counts (max 4)
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
     * unConnectInternal
     * Reverse-connect logic: rollback pattern values around the removed stone.
     */
    public void unConnectInternal()
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
     * unFill
     * During undo: regenerate opponent's pattern based on contiguous enemy sequence.
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
     * unFillZero
     * During undo: roll back pattern for nearby empty cells
     */
    public void unFillZero(int x, int y, int dir)
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

    /**
     * resetStr
     * Reset all pattern arrays, clear blocker list, and reset victory + score.
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
}
