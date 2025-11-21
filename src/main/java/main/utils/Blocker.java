package main.utils;

import main.ai.MinimaxEngine;

public class Blocker {

    public int [] bl1 = new int[2];
    public int [] bl2 = new int[2];
    public int color;
    public int blockColor;
    public int dir;
    public int sig;
    public int [][] cases = new int[4][2];
    public int rank;
    static int [][] dirOffsets = {{1, 0}, {0, 1}, {1, 1}, {-1, 1}};

    public void bl1(int x, int y) {
        bl1[0] = x; bl1[1] = y;
    }

    public void bl2(int x, int y) {
        bl2[0] = x; bl2[1] = y;
    }

    public Blocker(int color, int dir, int sig) {
        this.blockColor = color;
        this.dir = dir;
        this.sig = sig;
        this.rank = 0;

        if (color == 1)
            this.color = 2;
        else
            this.color = 1;
    }

    private boolean inBoard(int x, int y) {
        return x >= 0 && x < 19 && y >= 0 && y < 19;
    }

    public void updateBlockInfo() {
        rank = 0;
        
        for (int i = 1 ; i < 5 ; i++) {
            if (!inBoard(bl1[0]+ dirOffsets[dir][0]*sig*i, bl1[1]+ dirOffsets[dir][1]*sig*i))
                break;
            if (MinimaxEngine.board[bl1[0]+ dirOffsets[dir][0]*sig*i][bl1[1]+ dirOffsets[dir][1]*sig*i] == 0) {
                cases[rank][0] = bl1[0]+ dirOffsets[dir][0]*sig*i;
                cases[rank][1] = bl1[1]+ dirOffsets[dir][1]*sig*i;
                rank++;
            }
        }
    }
}

