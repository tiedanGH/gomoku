package main.utils;

public class DoubleFree {

    static private final int[][] dir = {{0, 1}, {1, 0}, {1, 1}, {1, -1}};

    private boolean inBoard(int x, int y) {
        int boardSize = 19;
        return x >= 0 && x < boardSize && y >= 0 && y < boardSize;
    }

    private boolean checkWin(int x, int y, int val, int i, int [][]map) {
        int curX;
        int curY;
        int dep = 1;
        int res = 0;
    
        curX = x + dep * dir[i][0];
        curY = y + dep * dir[i][1];
        while(inBoard(curX, curY) && map[curX][curY] == val) {
            res++;
            dep++;
            curX = x + dep * dir[i][0];
            curY = y + dep * dir[i][1];
        }

        dep = -1;
        curX = x + dep * dir[i][0];
        curY = y + dep * dir[i][1];
        while(inBoard(curX, curY) && map[curX][curY] == val) {
            res++;
            dep--;
            curX = x + dep * dir[i][0];
            curY = y + dep * dir[i][1];
        }
        return res >= 4;
    }

    public boolean checkDoubleFree(int x, int y, int val, int[][] map) {
        int total0;
        int dep;
        int totalValue;
        int curX;
        int curY;
        int totalFree = 0;
    
        final int sep = val == 1 ? 2 : 1;

        if (map[x][y] != 0) return true;

        for (int i = 0 ; i < 4 ; i++) {
            total0 = 0;
            dep = 1;
            totalValue = 0;

            for (int j = 0 ; j < 4 ; j++) {
                if (checkWin(x, y, val, j, map)) return true;
            }

            curX = x + dep * dir[i][0];
            curY = y + dep * dir[i][1];
            while(inBoard(curX, curY) && total0 != 2 && map[curX][curY] != sep) {
                if (map[curX][curY] == 0)
                    total0++;
                if (map[curX][curY] == val)
                    totalValue++;
                dep++;
                curX = x + dep * dir[i][0];
                curY = y + dep * dir[i][1];
            }

            if (inBoard(curX, curY) && map[curX][curY] == sep) {
                if (!inBoard(curX -dir[i][0], curY - dir[i][1]) || map[curX - dir[i][0]][curY - dir[i][1]] !=0)
                    continue;
                if (curX-dir[i][0] == x && curY - dir[i][1] == y)
                    continue;
            }

            if (inBoard( curX - dir[i][0], curY - dir[i][1]) && inBoard(curX - 2 * dir[i][0], curY - 2 * dir[i][1]) &&
                map[curX - 2 * dir[i][0]][curY - 2 * dir[i][1]] == 0 && map[curX - dir[i][0]][curY - dir[i][1]] == 0)
                total0 = 0;
            else
                total0 -=1;
            dep = 1;
            curX = x - (dep * dir[i][0]);
            curY = y - (dep * dir[i][1]);

            while( inBoard(curX, curY) && total0 != 2 && map[curX][curY] != sep) {
                if (map[curX][curY] == 0)
                    total0++;
                if (map[curX][curY] == val)
                    totalValue++;
                dep++;
                curX = x - (dep * dir[i][0]);
                curY = y - (dep * dir[i][1]);
            }

            if (inBoard(curX, curY) && map[curX][curY] == sep) {
                if (!inBoard( + dir[i][0], curY + dir[i][1]) ||
                map[curX + dir[i][0]][curY + dir[i][1]] !=0)
                    continue;
                if (curX + dir[i][0] == x && curY + dir[i][1] == y)
                    continue;
            }
            if (totalValue == 2)
                totalFree++;
            if (totalFree >= 2)
                return false;
        }
        return true;
    }
}