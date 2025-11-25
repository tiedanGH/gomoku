package main.gomoku;

import main.utils.Point;

import java.util.ArrayList;

public interface MapInterface {
    void addMove(Point point, int color);
    void clearMove();
    void setColor(int color);
    int getSize();
    int[][] getMap();
    ArrayList<Point> getLastMove();
}
