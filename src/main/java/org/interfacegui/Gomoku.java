package org.interfacegui;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import org.modelai.Candidate;
import org.modelai.Game;
import org.utils.Point;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.DoubleBinding;
import javafx.scene.paint.Color;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.util.Duration;

public class Gomoku {

    private Point lastMove = null;
    private final Pane gameDisplay;
    private final ArrayList<Map> maps;
    private final Board board;
    private final GameInfos gameInfos;
    private int gameInfosSizeX;
    public int gameInfosSizeY;
    private final Pane boardPane;
    private final int totalLines;
    private int mapIndex;
    private Rules rule;
    private final VBox endPopup = new VBox();
    private final Button replay;
    private final Button backHome;
    private final Home home;

    private int playerTurn = 0;
    private boolean gameEnd = false;
    private Timeline gameLoop;
    private final Label endText = new Label();
    private Label gameName;

    private ArrayList<Point> candidatesList;
    private ArrayList<Point> hintList = null;

    private int round = 0;
    private Game game;
    private boolean toggleHint = false;
    private boolean ia_playing = false;

    private ExecutorService executor = null;
    private Future<Point> future = null;
    private final ExecutorService executor2 = null;
    private Future<Boolean> future2 = null;

    private final Label commentLabel = new Label();
    int width = 0;

    private void setEndGame() {
        int winner = rule.getWinner();
        endPopup.setVisible(true);
        endPopup.setManaged(true);
        gameEnd = true;
        ia_playing = false;
        if (gameLoop != null) gameLoop.stop();

        if (winner == 0) endText.setText("Draw");
        else if (winner == 1) endText.setText("Black Win");
        else endText.setText("White Win");
    }

    private void updateGameMap(int index) {
        for (int i = 0; i < rule.get_board_size(); i++) {
            for (int j = 0; j < rule.get_board_size(); j++) {
                game.gameMap[i][j] = maps.get(index).get_map()[i][j];
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

    void setCandidates(ArrayList<Candidate.Coordinate> candidates, float[] values, int index) {
        if (!rule.hasAI() || candidates == null || values == null) return;
        candidatesList = new ArrayList<>();
        hideCandidates();
        for (int i = 0; i < values.length; i++) {
            candidatesList.add(new Point(candidates.get(i).y, candidates.get(i).x));
            candidatesList.get(i).set_val(values[i]);
        }
        maps.get(index).setCandidatsList(candidatesList);
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
            hintList.get(i).set_val(values[i]);
        }

        hintList.sort(Comparator.comparingDouble(Point::get_val));
    }

    private void updatePlayerTurn() {
        playerTurn = 1 - playerTurn;
    }

    // play a move
    private void playMove(Point point) {
        if (mapIndex < (maps.size() - 1) || !rule.isValidMove(point, maps)) return;

        hideCandidates();
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

        newMap.set_color(playerTurn);

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

    public Gomoku(int height, int width, Home game_infos) {
        home = game_infos;
        this.width = width;

        if (home.getRuleInstance() != null)
            rule = home.getRuleInstance();
        else
            initRules(home.getBoardSize());

        totalLines = rule.get_board_size();

        gameInfosSizeX = width / 4;
        gameInfosSizeY = height;

        gameInfos = new GameInfos(height, gameInfosSizeX);

        game = new Game(game_infos.getRules(), rule.get_board_size());
        game.resetMinMax();
        maps = new ArrayList<>();
        board = new Board(height, width - gameInfosSizeX, rule.get_board_size());

        maps.add(new Map(totalLines));

        gameDisplay = new Pane();
        replay = new Button("Replay");
        backHome = new Button("Back Home");
        gameName = new Label(game_infos.getRules());

        endPopup.setVisible(false);
        endPopup.getChildren().addAll(endText, replay, backHome);

        boardPane = board.getBoard();
        VBox gameInfosPane = gameInfos.getGameInfos();
        gameInfosPane.getChildren().add(0, gameName);
        gameInfosPane.getChildren().add(0, endPopup);

        setPlayerColor();

        DoubleBinding fontSizeBinding = (DoubleBinding) Bindings.min(
                gameInfosPane.widthProperty().multiply(0.1),
                gameInfosPane.heightProperty().multiply(0.1)
        );

        gameName.fontProperty().bind(Bindings.createObjectBinding(
                () -> new Font("Arial", fontSizeBinding.get()),
                fontSizeBinding
        ));

        VBox mainVBox = new VBox();
        HBox hbox = new HBox();
        hbox.getChildren().addAll(gameInfosPane, boardPane);
        mainVBox.getChildren().addAll(commentLabel, hbox);
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
            ia_playing = false;
            endText.setText("match resigned");
            endPopup.setVisible(true);
            endPopup.setManaged(true);
            endAI();
            if (future2 != null){
                future2 = null;
                ia_playing = false;
            }
        });

        // Previous Button
        gameInfos.getPreviousButton().setOnAction(event -> {
            if (mapIndex > 0) {
                mapIndex--;
                board.updateFromMap(maps.get(mapIndex));
                setStepButtonVisibility();
            }
        });

        // Next Button
        gameInfos.getNextButton().setOnAction(event -> {
            if (mapIndex < maps.size() - 1) {
                mapIndex++;
                board.updateFromMap(maps.get(mapIndex));
                setStepButtonVisibility();
            }
        });

        // Board Click Event
        board.getBoard().setOnMouseClicked(event -> {
            if (gameEnd) return;
            if ((playerTurn == 0 && home.getBlackPlayerType() == 1)
                    || (playerTurn == 1 && home.getWhitePlayerType() == 1))
                return;

            int margin_w = board.get_margin_width();
            int margin_h = board.get_margin_height();
            int square = board.getSquareSize();
            double x = event.getX() - margin_w;
            double y = event.getY() - margin_h;

            x /= square;
            y /= square;
            x = Math.round(x);
            y = Math.round(y);

            if (x < 0 || x >= rule.get_board_size() || y < 0 || y >= rule.get_board_size())
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
                        if (!ia_playing) {
                            executor = Executors.newSingleThreadExecutor();
                            future = executor.submit(() -> game.bestMove(1, 1, true));
                            ia_playing = true;
                        } else if (future.isDone()) {
                            playMove(future.get());
                            executor.shutdown();
                            executor = null;
                            ia_playing = false;
                        }
                    } else if (playerTurn == 1 && home.getWhitePlayerType() == 1) {
                        if (!ia_playing) {
                            executor = Executors.newSingleThreadExecutor();
                            future = executor.submit(() -> game.bestMove(2, 2, true));
                            ia_playing = true;
                        } else if (future.isDone()) {
                            playMove(future.get());
                            executor.shutdown();
                            executor = null;
                            ia_playing = false;
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
        ia_playing = false;
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
            game = new Game(home.getRules(), rule.get_board_size());
            game.resetMinMax();
            if (home.getBlackPlayerType() == 0 &&
                    home.getWhitePlayerType() == 0)
                game.treeConfig(1);
            else
                game.treeConfig(home.getLevel());
        }

        // clear hint
        candidatesList = null;
        hintList = null;
        toggleHint = false;
        hideCandidates();
        changeHintVisibility(false);

        endPopup.setVisible(false);
        endPopup.setManaged(false);

        createGameLoop();
    }

    public Pane getGameDisplay() {
        return gameDisplay;
    }

    public Button get_home_button() {
        return backHome;
    }

    public Button get_replay_button() {
        return replay;
    }

    public Button getBackHomeButton() { return backHome; }

    public void updateGameDisplay(int newY, int newX){
        width = newX;
        gameInfosSizeX = newX / 4;
        gameInfosSizeY = newY;
        gameInfos.updateGameInfo(newY, gameInfosSizeX);
        double labelHeight = commentLabel.prefHeight(commentLabel.getMaxWidth());
        if (!commentLabel.isVisible())
            labelHeight = 0;
        board.updateBoard(newY - (int)labelHeight, newX - gameInfosSizeX);
        boardPane.setLayoutX(gameInfosSizeX);
    }

    // highlight the last move
    private void highlightLastMove() {
        if (lastMove == null) return;
        board.drawLastMoveBox(lastMove);
    }
}
