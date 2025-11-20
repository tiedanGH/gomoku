package org.interfacegui;

import java.util.ArrayList;
import org.utils.Point;

public class Map{

    private final int[][] map;
    int playerColor;
    public ArrayList<Point> lastMove = new ArrayList<>();
    public ArrayList<Integer> lastMoveColor = new ArrayList<>();

    public Map(int size) {
        map = new int[size][size];
        for (int i = 0; i < map.length; i++) {
            for (int j = 0; j < map.length; j++){
                map[i][j] = 0;
            }
        }
    }

    public Map(Map other) {
        map = new int[other.getSize()][other.getSize()];
        for (int i = 0; i < map.length; i++) {
            System.arraycopy(other.map[i], 0, map[i], 0, map[i].length);
        }
        playerColor = other.playerColor;
        for (Point p : other.lastMove){
            lastMove.add(new Point(p.x, p.y));
        }
        lastMoveColor.addAll(other.lastMoveColor);
    }

    public void clearMove(){
        lastMove.clear();
        lastMoveColor.clear();
    }

    public void setColor(int color) {
        playerColor = color;
    }

    public int getSize() {
        return map.length;
    }

    public int[][] getMap(){
        return map;
    }

    public void addMove(Point point, int color){
        lastMove.add(point);
        lastMoveColor.add(color);
        if (point != null)
            map[point.y][point.x] = color;
    }

    public ArrayList<Point> getLastMove(){
        return lastMove;
    }
}