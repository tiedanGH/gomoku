package main.gomoku;

import java.util.ArrayList;
import main.utils.Point;

public class GomokuRules implements Rules {

    int winner;
    int boardSize = 19;
    int totalMove = 0;

    ArrayList<Point> verticalWin = new ArrayList<>();
    ArrayList<Point> horizontalWin = new ArrayList<>();
    ArrayList<Point> diagonalLeftWin = new ArrayList<>();
    ArrayList<Point> diagonalRightWin = new ArrayList<>();

    @Override
    public boolean undo(){
        if (totalMove > 0)
            totalMove--;
        return true;
    }

    @Override
    public boolean isValidMove(Point point, ArrayList<Map> map) {
        if (!isEmpty(point.x, point.y, map.get(map.size() - 1))) {
            return false;
        }
        totalMove++;
        return true;
    }

    @Override
    public void  setBoardSize(int value){
        if (value != -1)
            boardSize = value;
        else
            boardSize = 19;
    }

    @Override
    public boolean endGame(Map map, Point point) {
        if (checkWin(map, point)){
            winner = getColor(map, point);
            return true;
        }
        if (totalMove == boardSize * boardSize)
        {
            winner = 0;
            return true;
        }
        return false;
    }

    private boolean isEmpty(int x, int y, Map map) {
        if ((x < 0 || x >= 19) || (y < 0 || y >= 19)){
            return false;
        }
        return map.getMap()[y][x] == 0;
    }

    private boolean direction(Map map, Point point, int dirX, int dirY, int color) {
        return map.getMap()[point.y + dirY][point.x + dirX] == color;
    }

    private void horizontal(Map map, Point point) {
        boolean right = true;
        boolean left = true;
        final int color = map.getMap()[point.y][point.x];
        if (color == 0)
            return;
        horizontalWin.add(point);
        for (int i = 1; (right || left); i++) {
            if (right && point.x - i >= 0 && direction(map, point, -i, 0, color)) {
                horizontalWin.add(new Point(point.x - i, point.y));
            } else {
                right = false;
            }
            if (left  && point.x + i < map.getSize() && direction(map, point, i, 0, color)) {
                horizontalWin.add(new Point(point.x + i, point.y));
            } else {
                left = false;
            }
        }
    }

    private void vertical(Map map, Point point) {
        boolean right = true;
        boolean left = true;
        final int color = map.getMap()[point.y][point.x];
        if (color == 0)
            return;
        verticalWin.add(point);
        for (int i = 1; (right || left); i++) {
            if (right && point.y - i >= 0 && direction(map, point, 0, -i, color)) {
                verticalWin.add(new Point(point.x, point.y - i));
            } else {
                right = false;
            }
            if (left  && point.y + i < map.getSize() && direction(map, point, 0, i, color)) {
                verticalWin.add(new Point(point.x, point.y + i));
            } else {
                left = false;
            }
        }
    }

    private void diagonalLeft(Map map, Point point){
        boolean right = true;
        boolean left = true;
        final int color = map.getMap()[point.y][point.x];
        if (color == 0)
            return;
        diagonalLeftWin.add(point);
        for (int i = 1; (right || left); i++) {
            if (right && point.y - i >= 0 && point.x - i >= 0 && direction(map, point, -i, -i, color)) {
                diagonalLeftWin.add(new Point(point.x - i, point.y - i));
            } else {
                right = false;
            }
            if (left  && point.y + i < map.getSize() && point.x + i < map.getSize() && direction(map, point, i, i, color)){
                diagonalLeftWin.add(new Point(point.x + i, point.y + i));
            } else {
                left = false;
            }
        }
    }

    private void diagonalRight(Map map, Point point){
        boolean right = true;
        boolean left = true;
        final int color = map.getMap()[point.y][point.x];
        if (color == 0)
            return;
        diagonalRightWin.add(point);
        for (int i = 1; (right || left); i++) {
            if (right && point.x + i < map.getSize() && point.y - i >= 0 && direction(map, point, i, -i, color)){
                diagonalRightWin.add(new Point(point.x + i, point.y - i));
            } else {
                right = false;
            }
            if (left && point.y + i < map.getSize() && point.x - i >= 0 && direction(map, point, -i, i, color)){
                diagonalRightWin.add(new Point(point.x - i, point.y + i));
            } else {
                left = false;
            }
        }
    }

    private int getColor(Map map, Point point) {
        return map.getMap()[point.y][point.x];
    }

    private void diagonals(Map map, Point point) {
        diagonalLeft(map, point);
        diagonalRight(map, point);
    }

    private boolean checkWin(Map map, Point point) {
        if (map.getMap()[point.y][point.x] == 0)
            return false;
        verticalWin.clear();
        horizontalWin.clear();
        diagonalLeftWin.clear();
        diagonalRightWin.clear();
        horizontal(map, point);
        vertical(map, point);
        diagonals(map, point);
        return verticalWin.size() >= 5 || horizontalWin.size() >= 5 || diagonalLeftWin.size() >= 5 || diagonalRightWin.size() >= 5;
    }

    @Override
    public int getBoardSize() {
        return boardSize;
    }

    @Override
    public int getWinner() {
        return this.winner;
    }

    @Override
    public boolean hasAI() {
        return true;
    }
}
