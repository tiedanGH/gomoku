package main.gomoku;

import main.utils.Point;

import java.util.ArrayList;

public interface MapInterface {
    // Add a move to the map at the specified point with the given color
    void addMove(Point point, int color);
    // Clear all recorded moves from the map
    void clearMove();
    // Set the current player's color
    void setColor(int color);
    // Get the size of the board
    int getSize();
    // Get the board state as a 2D int array
    int[][] getMap();
    // Get the list of last move points
    ArrayList<Point> getLastMove();
}
