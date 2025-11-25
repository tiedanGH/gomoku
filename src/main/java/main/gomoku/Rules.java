package main.gomoku;

import java.util.ArrayList;
import main.utils.Point;

public interface Rules {
    // Validate if a move at the given point is valid on the current map state
    boolean isValidMove(Point point, ArrayList<Map> map);
    // Check if the game has ended after a move at the given point on the current map state
    boolean endGame(Map map, Point point);
    // Get the winner of the game
    int getWinner();
    // Check if the game includes an AI player
    boolean hasAI();
    // Get the current board size
    int getBoardSize();
    // Set the board size
    void  setBoardSize(int value);
    // Undo the last move made in the game
    boolean undo();
}
