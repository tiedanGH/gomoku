package main.gomoku;

import java.util.ArrayList;
import main.utils.Point;

public class Map implements MapInterface {

    private final int[][] map;
    private final int size;
    private int playerColor;
    private final ArrayList<Point> lastMove = new ArrayList<>();
    private final ArrayList<Integer> lastMoveColor = new ArrayList<>();

    public Map(int size) {
        this.size = size;
        map = new int[size][size];
        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++){
                map[i][j] = 0;
            }
        }
    }

    public Map(Map other) {
        size = other.getSize();
        map = new int[other.getSize()][other.getSize()];
        for (int i = 0; i < size; i++) {
            System.arraycopy(other.map[i], 0, map[i], 0, map[i].length);
        }
        playerColor = other.playerColor;
        for (Point p : other.lastMove){
            lastMove.add(new Point(p.x, p.y));
        }
        lastMoveColor.addAll(other.lastMoveColor);
    }

    @Override
    public void addMove(Point point, int color){
        lastMove.add(point);
        lastMoveColor.add(color);
        if (point != null)
            map[point.y][point.x] = color;
    }

    @Override
    public void clearMove() {
        lastMove.clear();
        lastMoveColor.clear();
    }

    @Override
    public void setColor(int color) {
        playerColor = color;
    }

    @Override
    public int getSize() {
        return size;
    }

    @Override
    public int[][] getMap() {
        int[][] copy = new int[size][];
        for (int i = 0; i < size; i++) {
            copy[i] = new int[map[i].length];
            System.arraycopy(map[i], 0, copy[i], 0, map[i].length);
        }
        return copy;
    }

    @Override
    public ArrayList<Point> getLastMove(){
        return lastMove;
    }
}