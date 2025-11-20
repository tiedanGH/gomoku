package org.interfacegui;

import java.util.ArrayList;
import org.utils.Point;

public interface Rules {
    boolean isValidMove(Point point, ArrayList<Map> map);
    boolean endGame(Map map, Point point);
    int getWinner();
    boolean hasAI();
    int getBoardSize();
    void  setBoardSize(int value);
    boolean undo();
}
