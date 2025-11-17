package org.interfacegui;
import java.util.ArrayList;
import org.utils.Point;

public class GomokuRules implements Rules {

    int winner;
    Rules.GameMode gameStatus = Rules.GameMode.PLAYING;
    int boardSize = 19;
    int nbMove = 0;


    @Override
    public boolean undo(){
        if (nbMove > 0)
            nbMove--;
        return true;
    }

    @Override
    public boolean isValidMove(Point point, ArrayList<Map> map) {
        if (!checkEmptySqure(point.x, point.y, map.get(map.size() - 1)))
        {
            return false;
        }
        nbMove++;
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
        if (check_five(map, point)){
            gameStatus = Rules.GameMode.ENDGAME;
            winner = getColor(map, point);
            return true;
        }
        if (nbMove == boardSize * boardSize)
        {
            winner = 0;
            return true;
        }
        return false;
    }

    @Override
    public Rules.GameMode getGameMode(){
        return gameStatus;
    }
    

    @Override
    public int  get_board_size(){
        return boardSize;
    }

    @Override
    public int getWinner(){
        return this.winner;
    }

    @Override
    public boolean hasIa(){
        return true;
    }
    
}
