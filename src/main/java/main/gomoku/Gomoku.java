package main.gomoku;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import javafx.scene.layout.*;
import main.ai.Candidate;
import main.ai.Game;
import main.utils.Point;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.scene.control.Button;
import javafx.util.Duration;

public class Gomoku {

    static final String backgroundColor = "#7A8F5C";

    private Point lastMove = null;
    private final Pane gameDisplay;
    private final ArrayList<Map> maps;
    private final Board board;
    private final LeftBox leftBox;
    private final RightBox rightBox;
    private final TopBox topBox;
    private final BottomBox bottomBox;
    private int sideBoxSizeX;
    private int sideBoxSizeY;
    private int boardSizeX;
    private int boardSizeY;
    private int topBoxSizeY;
    private int bottomSizeY;
    private final Pane boardPane;
    private final int totalLines;
    private int mapIndex;
    private Rules rule;
    private final Home home;

    private int playerTurn = 0;
    private boolean gameEnd = false;
    private Timeline gameLoop;

    private ArrayList<Point> hintList = null;

    private Game game;
    private boolean toggleHint = false;
    private boolean iaPlaying = false;

    private ExecutorService executor = null;
    private Future<Point> future = null;
    private Future<Boolean> future2 = null;

    private void setEndGame() {
        int winner = rule.getWinner();
        endAI();
        gameEnd = true;
        iaPlaying = false;
        if (gameLoop != null) gameLoop.stop();
        topBox.showEnd(winner);
        bottomBox.gameEnd();
    }

    private void updateGameMap(int index) {
        for (int i = 0; i < rule.getBoardSize(); i++) {
            for (int j = 0; j < rule.getBoardSize(); j++) {
                game.gameMap[i][j] = maps.get(index).getMap()[i][j];
            }
        }
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

        updatePlayerTurn();
        setBoxHighlight();
    }

    private void undoMove() {
        if (maps.size() < 2 || mapIndex < maps.size() - 1) return;

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
    }

    private void setBoxHighlight() {
        leftBox.highlightForPlayer(playerTurn);
        rightBox.highlightForPlayer(playerTurn);
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
        bottomBox.getPreviousButton().setDisable(mapIndex <= 0);
        bottomBox.getNextButton().setDisable(mapIndex >= maps.size() - 1);
    }

    public Gomoku(int height, int width, Home rules) {
        home = rules;

        initRules(home.getBoardSize());

        totalLines = rule.getBoardSize();

        updateAllSizes(height, width);

        leftBox = new LeftBox(sideBoxSizeY, sideBoxSizeX);
        rightBox = new RightBox(sideBoxSizeY, sideBoxSizeX);
        topBox = new TopBox(topBoxSizeY, boardSizeX);
        bottomBox = new BottomBox(bottomSizeY, boardSizeX);

        game = new Game(rules.getRules(), rule.getBoardSize());
        game.resetMinMax();
        maps = new ArrayList<>();
        board = new Board(boardSizeY, boardSizeX, rule.getBoardSize());

        maps.add(new Map(totalLines));

        gameDisplay = new Pane();

        topBox.hideEnd();

        boardPane = board.getBoard();
        VBox leftPane = leftBox.getLeftPane();
        VBox rightPane = rightBox.getRightPane();
        VBox topPane = topBox.getTopPane();
        VBox bottomPane = bottomBox.getBottomPane();

        blockHintIfAllAI();
        setBoxHighlight();

        // create main layout
        Pane mainPane = new Pane();
        VBox middlePane = new VBox();
        middlePane.getChildren().addAll(topPane, boardPane, bottomPane);
        HBox hbox = new HBox();
        hbox.getChildren().addAll(leftPane, middlePane, rightPane);
        mainPane.getChildren().addAll(hbox);
        gameDisplay.getChildren().add(mainPane);

        // Undo Button
        bottomBox.getUndoButton().setOnAction(event -> {
            if (!rule.undo()) return;
            undoMove();
        });

        // Hint Button
        bottomBox.getHintButton().setOnAction(event -> {
            if (!rule.hasAI() || gameEnd) return;

            toggleHint = !toggleHint;

            game.bestMove(playerTurn + 1, playerTurn + 1, true);
            setHint(game.m.candidate.list, game.m.values);
            changeHintVisibility(toggleHint);

            if (!toggleHint) board.updateFromMap(maps.get(mapIndex));
        });

        // Resign Button
        bottomBox.getResignButton().setOnAction(event -> {
            if (gameEnd) return;

            gameLoop.stop();
            gameEnd = true;
            iaPlaying = false;
            topBox.showEnd(0);
            bottomBox.gameEnd();
            endAI();
            if (future2 != null){
                future2 = null;
                iaPlaying = false;
            }
        });

        // Previous Button
        bottomBox.getPreviousButton().setOnAction(event -> {
            if (mapIndex > 0) {
                mapIndex--;
                board.updateFromMap(maps.get(mapIndex));
                setStepButtonVisibility();
                board.removeLastMoveBox();
            }
        });

        // Next Button
        bottomBox.getNextButton().setOnAction(event -> {
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

    private void updateAllSizes(int height, int width) {
        sideBoxSizeX = width / 5;
        sideBoxSizeY = height;
        boardSizeY = boardSizeX = width - sideBoxSizeX * 2;
        topBoxSizeY = (height - boardSizeY) * 4 / 7;
        bottomSizeY = (height - boardSizeY) * 3 / 7;
    }

    private void blockHintIfAllAI() {
        bottomBox.getHintButton().setDisable(home.getBlackPlayerType() == 1 && home.getWhitePlayerType() == 1);
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
        board.removeLastMoveBox();
        changeHintVisibility(false);
        topBox.hideEnd();
        bottomBox.resetButtons();

        createGameLoop();
    }

    public Pane getGameDisplay() {
        return gameDisplay;
    }

    public Button getReplayButton() {
        return topBox.getReplayButton();
    }

    public Button getHomeButton() {
        return topBox.getHomeButton();
    }

    public void updateGameDisplay(int newY, int newX){
        updateAllSizes(newY, newX);
        leftBox.updateLeftSize(newY, sideBoxSizeX);
        rightBox.updateRightSize(newY, sideBoxSizeX);
        topBox.updateTopSize(topBoxSizeY, boardSizeX);
        bottomBox.updateBottomSize(bottomSizeY, boardSizeX);
        board.updateBoard(boardSizeY, boardSizeX, lastMove);
        boardPane.setLayoutX(sideBoxSizeX);
    }

    // highlight the last move
    private void highlightLastMove() {
        if (lastMove == null) return;
        board.drawLastMoveBox(lastMove);
    }
}
