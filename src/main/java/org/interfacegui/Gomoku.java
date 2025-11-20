package org.interfacegui;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import javafx.scene.layout.*;
import org.modelai.Candidate;
import org.modelai.Game;
import org.utils.Point;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.scene.paint.Color;
import javafx.scene.control.Button;
import javafx.util.Duration;

public class Gomoku {

    private Point lastMove = null;
    private final Pane gameDisplay;
    private final ArrayList<Map> maps;
    private final Board board;
    private final GameInfos gameInfos;
    private final EndInfos endInfos;
    private int infoSizeX;
    public int infoSizeY;
    private final Pane boardPane;
    private final int totalLines;
    private int mapIndex;
    private Rules rule;
    private final Home home;

    private int playerTurn = 0;
    private boolean gameEnd = false;
    private Timeline gameLoop;

    private ArrayList<Point> hintList = null;

    private int round = 0;
    private Game game;
    private boolean toggleHint = false;
    private boolean iaPlaying = false;

    private ExecutorService executor = null;
    private Future<Point> future = null;
    private Future<Boolean> future2 = null;

    private void setEndGame() {
        int winner = rule.getWinner();
        gameEnd = true;
        iaPlaying = false;
        if (gameLoop != null) gameLoop.stop();
        endInfos.showEnd(winner);
    }

    private void updateGameMap(int index) {
        for (int i = 0; i < rule.getBoardSize(); i++) {
            for (int j = 0; j < rule.getBoardSize(); j++) {
                game.gameMap[i][j] = maps.get(index).getMap()[i][j];
            }
        }
    }

    void hideCandidates() {
        ArrayList<Point> currentCandidates = maps.get(mapIndex).getCandidatsList();
        if (currentCandidates == null) return;
        for (Point p : currentCandidates) {
            String color = p.val < 0 ? "#FF0000" : "#00FF00";
            board.setStoneStatus(false, color, p, String.format("%.0f", p.val));
        }
        board.updateFromMap(maps.get(mapIndex));
    }

    void changeHintVisibility(boolean visible) {
        if (hintList == null) return;

        for (Point p : hintList) {
            board.setStoneStatus(visible, "#00F0FF", p, String.valueOf((int)p.val));
        }
        if (!visible) board.updateFromMap(maps.get(mapIndex));
    }

    void setHint(ArrayList<Candidate.Coordinate> hint, float[] values) {
        if (!rule.hasAI() || hint == null) return;

        hintList = new ArrayList<>();
        for (int i = 0; i < hint.size(); i++) {
            hintList.add(new Point(hint.get(i).y, hint.get(i).x));
            hintList.get(i).setValue(values[i]);
        }
        hintList.sort(Comparator.comparingDouble(Point::getValue));
    }

    private void updatePlayerTurn() {
        playerTurn = 1 - playerTurn;
    }

    // play a move
    private void playMove(Point point) {
        if (mapIndex < (maps.size() - 1) || !rule.isValidMove(point, maps)) return;

        hideCandidates();
        setStepButtonVisibility();
        changeHintVisibility(false);
        toggleHint = false;

        maps.add(new Map(maps.get(maps.size() - 1)));
        Map newMap = maps.get(maps.size() - 1);

        newMap.clearMove();
        newMap.addMove(point, maps.size() % 2 + 1);
        mapIndex = maps.size() - 1;

        lastMove = point;

        if (rule.hasAI()) {
            updateGameMap(mapIndex);
            game.move(point, playerTurn + 1);
        }

        newMap.setColor(playerTurn);

        board.updateFromMap(newMap);
        highlightLastMove();

        if (rule.endGame(newMap, point)) {
            setEndGame();
        }

        if (playerTurn == 0) {
            round++;
            gameInfos.setTurn(round);
        }

        updatePlayerTurn();
        setPlayerColor();
    }

    private void undoMove() {
        if (maps.size() < 2 || mapIndex < maps.size() - 1) return;

        hideCandidates();
        changeHintVisibility(false);
        toggleHint = false;

        mapIndex--;
        maps.remove(maps.size() - 1);

        if (rule.hasAI()) {
            updateGameMap(mapIndex);
        }

        board.updateFromMap(maps.get(mapIndex));

        ArrayList<Point> last = maps.get(mapIndex).getLastMove();
        if (last != null && !last.isEmpty()) {
            lastMove = last.get(last.size() - 1);
            highlightLastMove();
        } else {
            lastMove = null;
        }

        updatePlayerTurn();

        if (mapIndex % 2 == 0) round--;
        gameInfos.setTurn(round);
    }

    private void setPlayerColor() {
        if (playerTurn == 0) {
            gameInfos.getBlackBox().setBackground(
                    new Background(new BackgroundFill(Color.GOLDENROD, null, null)));
            gameInfos.getWhiteBox().setBackground(
                    new Background(new BackgroundFill(Color.LIGHTGRAY, null, null)));
        } else {
            gameInfos.getBlackBox().setBackground(
                    new Background(new BackgroundFill(Color.LIGHTGRAY, null, null)));
            gameInfos.getWhiteBox().setBackground(
                    new Background(new BackgroundFill(Color.GOLDENROD, null, null)));
        }
    }

    private void initRules(int size) {
        rule = new GomokuRules();
        rule.setBoardSize(size);
    }

    public void endAI() {
        if (gameLoop != null) gameLoop.stop();
        if (executor != null) executor.shutdown();
        if (rule.hasAI() && game != null) game.resetMinMax();
    }

    private void setStepButtonVisibility() {
        gameInfos.getPreviousButton().setVisible(mapIndex > 0);
        gameInfos.getNextButton().setVisible(mapIndex < maps.size() - 1);
    }

    public Gomoku(int height, int width, Home gameInfosRules) {
        home = gameInfosRules;

        if (home.getRuleInstance() != null)
            rule = home.getRuleInstance();
        else
            initRules(home.getBoardSize());

        totalLines = rule.getBoardSize();

        infoSizeX = width / 5;
        infoSizeY = height;

        gameInfos = new GameInfos(height, infoSizeX);
        endInfos = new EndInfos(height, infoSizeX);

        game = new Game(gameInfosRules.getRules(), rule.getBoardSize());
        game.resetMinMax();
        maps = new ArrayList<>();
        board = new Board(height, width - infoSizeX * 2, rule.getBoardSize());

        maps.add(new Map(totalLines));

        gameDisplay = new Pane();

        endInfos.hidePopup();

        boardPane = board.getBoard();
        VBox gameInfosPane = gameInfos.getGameInfos();
        VBox endInfosPane = endInfos.getEndInfos();

        setPlayerColor();

        VBox mainVBox = new VBox();
        HBox hbox = new HBox();
        hbox.getChildren().addAll(gameInfosPane, boardPane, endInfosPane);
        mainVBox.getChildren().addAll(hbox);
        gameDisplay.getChildren().add(mainVBox);

        // Undo Button
        gameInfos.getUndoButton().setOnAction(event -> {
            if (!rule.undo()) return;
            undoMove();
        });

        // Hint Button
        gameInfos.getHintButton().setOnAction(event -> {
            if (!rule.hasAI() || gameEnd) return;

            toggleHint = !toggleHint;

            game.bestMove(playerTurn + 1, playerTurn + 1, true);
            setHint(game.m.candidate.list, game.m.values);
            changeHintVisibility(toggleHint);

            if (!toggleHint) board.updateFromMap(maps.get(mapIndex));
        });

        // Resign Button
        gameInfos.getResignButton().setOnAction(event -> {
            if (rule.getGameMode() == Rules.GameMode.ENDGAME)
                return ;
            gameLoop.stop();
            gameEnd = true;
            iaPlaying = false;
            endInfos.showEnd("match resigned");
            endAI();
            if (future2 != null){
                future2 = null;
                iaPlaying = false;
            }
        });

        // Previous Button
        gameInfos.getPreviousButton().setOnAction(event -> {
            if (mapIndex > 0) {
                mapIndex--;
                board.updateFromMap(maps.get(mapIndex));
                setStepButtonVisibility();
                board.removeLastMoveRect();
            }
        });

        // Next Button
        gameInfos.getNextButton().setOnAction(event -> {
            if (mapIndex < maps.size() - 1) {
                mapIndex++;
                board.updateFromMap(maps.get(mapIndex));
                setStepButtonVisibility();
                if (mapIndex == maps.size() - 1) {
                    highlightLastMove();
                }
            }
        });

        // Board Click Event
        board.getBoard().setOnMouseClicked(event -> {
            if (gameEnd) return;
            if ((playerTurn == 0 && home.getBlackPlayerType() == 1)
                    || (playerTurn == 1 && home.getWhitePlayerType() == 1))
                return;

            int marginWidth = board.getMarginWidth();
            int marginHeight = board.getMarginHeight();
            int square = board.getSquareSize();
            double x = event.getX() - marginWidth;
            double y = event.getY() - marginHeight;

            x /= square;
            y /= square;
            x = Math.round(x);
            y = Math.round(y);

            if (x < 0 || x >= rule.getBoardSize() || y < 0 || y >= rule.getBoardSize())
                return;

            playMove(new Point((int) x, (int) y));
        });

        createGameLoop();
    }

    public void createGameLoop() {
        gameLoop = new Timeline();
        KeyFrame keyFrame = new KeyFrame(Duration.millis(50), event -> {
            if (rule.hasAI()) {
                try {
                    if (playerTurn == 0 && home.getBlackPlayerType() == 1) {
                        if (!iaPlaying) {
                            executor = Executors.newSingleThreadExecutor();
                            future = executor.submit(() -> game.bestMove(1, 1, true));
                            iaPlaying = true;
                        } else if (future.isDone()) {
                            playMove(future.get());
                            executor.shutdown();
                            executor = null;
                            iaPlaying = false;
                        }
                    } else if (playerTurn == 1 && home.getWhitePlayerType() == 1) {
                        if (!iaPlaying) {
                            executor = Executors.newSingleThreadExecutor();
                            future = executor.submit(() -> game.bestMove(2, 2, true));
                            iaPlaying = true;
                        } else if (future.isDone()) {
                            playMove(future.get());
                            executor.shutdown();
                            executor = null;
                            iaPlaying = false;
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        gameLoop.getKeyFrames().add(keyFrame);
        gameLoop.setCycleCount(Timeline.INDEFINITE);
        gameLoop.play();
    }

    public void resetGame() {
        iaPlaying = false;
        gameEnd = false;
        playerTurn = 0;
        lastMove = null;

        if (gameLoop != null) {
            gameLoop.stop();
        }
        if (executor != null) {
            executor.shutdown();
            executor = null;
        }

        // reset map and board
        maps.clear();
        maps.add(new Map(totalLines));

        board.updateFromMap(maps.get(0));
        mapIndex = 0;
        setStepButtonVisibility();

        if (rule.hasAI()) {
            game = new Game(home.getRules(), rule.getBoardSize());
            game.resetMinMax();
            if (home.getBlackPlayerType() == 0 &&
                    home.getWhitePlayerType() == 0)
                game.treeConfig(1);
            else
                game.treeConfig(home.getLevel());
        }

        hintList = null;
        toggleHint = false;
        lastMove = null;
        board.removeLastMoveRect();
        hideCandidates();
        changeHintVisibility(false);
        endInfos.hidePopup();

        createGameLoop();
    }

    public Pane getGameDisplay() {
        return gameDisplay;
    }

    public Button getReplayButton() {
        return endInfos.getReplayButton();
    }

    public Button getBackHomeButton() {
        return endInfos.getBackHomeButton();
    }

    public void updateGameDisplay(int newY, int newX){
        infoSizeX = newX / 5;
        infoSizeY = newY;
        gameInfos.updateGameInfo(newY, infoSizeX);
        endInfos.updateEndInfo(newY, infoSizeX);
        board.updateBoard(newY, newX - infoSizeX * 2);
        boardPane.setLayoutX(infoSizeX);
    }

    // highlight the last move
    private void highlightLastMove() {
        if (lastMove == null) return;
        board.drawLastMoveBox(lastMove);
    }
}
