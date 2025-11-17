package org.interfacegui;

import java.util.ArrayList;

import org.modelai.Game;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.layout.HBox;
import javafx.scene.control.Button;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.scene.control.Label;
import javafx.util.Duration;
import org.utils.Point;
import javafx.scene.text.Font;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.DoubleBinding;
import org.modelai.Candidat;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import javafx.scene.paint.Color;
import java.util.Comparator;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;

public class Gomoku {

    private Pane game_display;
    private ArrayList<Map> _map;
    private Goban goban;
    private GameInfos gameInfos;
    private int _game_infos_size_x;
    public int _game_infos_size_y;
    private Pane _goban_pane;
    private VBox _game_infos_pane;
    private int _nb_line;
    private int map_index;
    private Rules rule;
    private VBox _end_popin = new VBox();
    private Button _replay;
    private Button _back_home;
    private Home _game_infos = new Home(){};

    private int player_turn = 0;
    private boolean game_end = false;
    private Timeline gameLoop;
    private Label _end_text = new Label();
    private int _winner = 0;
    private Label game_name;

    private ArrayList<Point> candidatsList;
    private ArrayList<Point> hintList = null;

    private int round = 0;
    private Game game;
    private ArrayList<Point> saved;
    private boolean toggleCandidat = false;
    private boolean toggleHint = false;
    private boolean ia_playing = false;

    private ExecutorService executor = null;
    private Future<Point> future = null;

    private ExecutorService executor2 = null;
    private Future<Boolean> future2 = null;

    private Label commentLabel = new Label();
    private Rules.GameMode playingMode = Rules.GameMode.PLAYING;
    int _width = 0;
    private ArrayList<Map> cpyMapLst(ArrayList<Map> m) {
        ArrayList<Map> n = new ArrayList<>();
        for (Map map : m) n.add(new Map(map));
        return n;
    }

    private void setEndGame() {
        int winner = rule.getWinner();
        _end_popin.setVisible(true);
        _end_popin.setManaged(true);
        game_end = true;
        ia_playing = false;
        if (gameLoop != null) gameLoop.stop();

        if (winner == 0) _end_text.setText("Draw");
        else if (winner == 1) _end_text.setText("Black Win");
        else _end_text.setText("White Win");
    }

    private Boolean playIa() {
        boolean end = false;
        int mapSize = _map.size();
        int mIndex;

        if (!rule.hasIa()) return false;

        for (mIndex = 0; mIndex < mapSize; mIndex++) {
            Map m = _map.get(mIndex);
            ArrayList<Point> lastMove = m.getLastMove();
            ArrayList<Integer> lastMoveColor = m.getLastMoveColor();

            // 重建 AI 内部棋盘
            updateGameMap(mIndex);

            for (int j = 0; j < lastMove.size(); j++) {
                if (lastMoveColor.get(j) != 0) {
                    game.move(lastMove.get(j), lastMoveColor.get(j));
                    if (!end) end = rule.endGame(_map.get(mIndex), lastMove.get(j));
                    if (end) return true;
                }
            }

            if (mIndex < _map.size() - 1) {
                game.best_move((mIndex % 2 == 0 ? 1 : 2),
                        (mIndex % 2 == 0 ? 1 : 2), true);
                setCandidats(game.m.candidat.lst, game.m.values, mIndex + 1);
                updatePlayerTurn();
            }
        }
        return false;
    }

    private void updateGameMap(int index) {
        for (int i = 0; i < rule.get_board_size(); i++) {
            for (int j = 0; j < rule.get_board_size(); j++) {
                game.gameMap[i][j] = _map.get(index).get_map()[i][j];
            }
        }
    }

    /** 显示候选点 */
    void changeCandidatVisibility(boolean visible) {
        ArrayList<Point> currentCandidats = _map.get(map_index).getCandidatsList();
        if (currentCandidats == null) return;

        for (Point p : currentCandidats) {
            String color = p.val < 0 ? "#FF0000" : "#00FF00";
            goban.set_stone_status(visible, color, p, String.format("%.0f", p.val));
        }
        if (!visible) goban.updateFromMap(_map.get(map_index));
    }

    void setCandidats(ArrayList<Candidat.coord> candidats, float[] values, int index) {
        if (!rule.hasIa() || candidats == null || values == null) return;

        candidatsList = new ArrayList<>();
        changeCandidatVisibility(false);

        for (int i = 0; i < values.length; i++) {
            candidatsList.add(new Point(candidats.get(i).y, candidats.get(i).x));
            candidatsList.get(i).set_val(values[i]);
        }
        _map.get(index).setCandidatsList(candidatsList);
    }

    /** Hint */
    void changeHintVisibility(boolean visible) {
        if (hintList == null) return;

        for (Point p : hintList) {
            goban.set_stone_status(visible, "#00F0FF", p, String.valueOf((int)p.val));
        }
        if (!visible) goban.updateFromMap(_map.get(map_index));
    }

    void setHint(ArrayList<Candidat.coord> hint, float[] values) {
        if (!rule.hasIa() || hint == null) return;

        hintList = new ArrayList<>();
        for (int i = 0; i < hint.size(); i++) {
            hintList.add(new Point(hint.get(i).y, hint.get(i).x));
            hintList.get(i).set_val(values[i]);
        }

        hintList.sort(Comparator.comparingDouble(Point::get_val));
    }

    private void updatePlayerTurn() {
        player_turn ^= 1;
    }

    /** 落子主逻辑（纯五子棋） */
    private void playMove(Point point) {
        if (map_index < (_map.size() - 1) || !rule.isValidMove(point, _map)) return;

        changeCandidatVisibility(false);
        changeHintVisibility(false);
        toggleCandidat = false;
        toggleHint = false;

        _map.add(new Map(_map.get(_map.size() - 1)));
        Map newMap = _map.get(_map.size() - 1);
        newMap.clearMove();
        newMap.addMove(point, _map.size() % 2 + 1);

        map_index = _map.size() - 1;

        if (rule.hasIa()) {
            updateGameMap(map_index);
            game.move(point, player_turn + 1);
        }

        newMap.set_color(player_turn);

        if (rule.endGame(newMap, point)) {
            setEndGame();
        }

        goban.updateFromMap(newMap);

        if (player_turn == 0) {
            round++;
            gameInfos.setPLayTurn(round);
        }
        updatePlayerTurn();
        setPlayerColor();
    }

    private void undoMove() {
        if (_map.size() < 2 || map_index < _map.size() - 1) return;

        changeCandidatVisibility(false);
        changeHintVisibility(false);
        toggleCandidat = false;
        toggleHint = false;

        map_index--;
        _map.remove(_map.size() - 1);

        if (rule.hasIa()) {
            Map m = _map.get(map_index);
            updateGameMap(map_index);
        }

        goban.updateFromMap(_map.get(map_index));
        player_turn ^= 1;

        if (map_index % 2 == 0) round--;
        gameInfos.setPLayTurn(round);
    }
    private void setPlayerColor() {
        if (player_turn == 0) {
            gameInfos.getBlackBox().setBackground(new Background(new BackgroundFill(Color.DARKSLATEGRAY, null, null)));
            gameInfos.getWhiteBox().setBackground(new Background(new BackgroundFill(Color.LIGHTGRAY, null, null)));
        } else {
            gameInfos.getBlackBox().setBackground(new Background(new BackgroundFill(Color.LIGHTGRAY, null, null)));
            gameInfos.getWhiteBox().setBackground(new Background(new BackgroundFill(Color.DARKSLATEGRAY, null, null)));
        }
    }

    private void init_rules(String rules_type, int size) {
        rule = new GomokuRules();
        rule.setBoardSize(size);
    }

    public void killIa() {
        if (gameLoop != null) gameLoop.stop();
        if (executor != null) executor.shutdown();
        if (rule.hasIa() && game != null) game.reset_minmax();
    }

    private void handdleButtonPrevNext() {
        gameInfos.getPrevButton().setVisible(map_index > 0);
        gameInfos.getNextButton().setVisible(map_index < _map.size() - 1);
    }

    public Gomoku(int heigh, int width, Home game_infos) {

        _game_infos = game_infos;
        _width = width;

        if (_game_infos.getRuleInstance() != null)
            rule = _game_infos.getRuleInstance();
        else
            init_rules(_game_infos.get_rules(), _game_infos.get_board_size());

        _nb_line = rule.get_board_size();

        _game_infos_size_x = width / 4;
        _game_infos_size_y = heigh;

        gameInfos = new GameInfos(heigh, _game_infos_size_x, game_infos);
        playingMode = game_infos.getGameMode();

        game = new Game(game_infos.get_rules(), rule.get_board_size());
        game.reset_minmax();

        _map = new ArrayList<>();
        goban = new Goban(heigh, width - _game_infos_size_x, rule.get_board_size());

        if (game_infos.getSgfMap() != null) {
            _map = cpyMapLst(game_infos.getSgfMap());
        } else {
            _map.add(new Map(_nb_line));
        }

        saved = new ArrayList<>();
        game_display = new Pane();
        _replay = new Button("Replay");
        _back_home = new Button("Back Home");
        game_name = new Label(game_infos.get_rules());

        _end_popin.setVisible(false);
        _end_popin.getChildren().addAll(_end_text, _replay, _back_home);

        _goban_pane = goban.get_goban();
        _game_infos_pane = gameInfos.getGameInfos();
        _game_infos_pane.getChildren().add(0, game_name);
        _game_infos_pane.getChildren().add(0, _end_popin);

        setPlayerColor();

        DoubleBinding fontSizeBinding = (DoubleBinding) Bindings.min(
                _game_infos_pane.widthProperty().multiply(0.1),
                _game_infos_pane.heightProperty().multiply(0.1)
        );

        game_name.fontProperty().bind(Bindings.createObjectBinding(
                () -> new Font("Arial", fontSizeBinding.get()),
                fontSizeBinding
        ));

        VBox mainVBox = new VBox();
        HBox hbox = new HBox();
        hbox.getChildren().addAll(_game_infos_pane, _goban_pane);
        mainVBox.getChildren().addAll(commentLabel, hbox);
        game_display.getChildren().add(mainVBox);

        // Undo
        gameInfos.getUndoButton().setOnAction(event -> {
            if (!rule.undo()) return;
            undoMove();
        });

        // Hint
        gameInfos.getHintButton().setOnAction(event -> {
            if (!rule.hasIa() || game_end) return;

            toggleHint = !toggleHint;

            if (hintList == null) {
                game.best_move(player_turn + 1, player_turn + 1, true);
                setHint(game.m.candidat.lst, game.m.values);
            }
            changeHintVisibility(toggleHint);

            if (!toggleHint) goban.updateFromMap(_map.get(map_index));
        });

        // SGF Export
        gameInfos.getExportButton().setOnAction(event -> {
            SGF.createSgf(_map, rule.getGameType());
        });

        // Prev
        gameInfos.getPrevButton().setOnAction(event -> {
            if (map_index > 0) {
                map_index--;
                goban.updateFromMap(_map.get(map_index));
                handdleButtonPrevNext();
            }
        });

        // Next
        gameInfos.getNextButton().setOnAction(event -> {
            if (map_index < _map.size() - 1) {
                map_index++;
                goban.updateFromMap(_map.get(map_index));
                handdleButtonPrevNext();
            }
        });

        // Mouse click to play
        goban.get_goban().setOnMouseClicked(event -> {
            if (game_end) return;
            if ((player_turn == 0 && _game_infos.get_black_player_type() == 1)
                    || (player_turn == 1 && _game_infos.get_white_player_type() == 1))
                return;

            int margin_w = goban.get_margin_width();
            int margin_h = goban.get_margin_height();
            int square = goban.getSquareSize();
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

        // Create game loop for AI
        createDelayedGameLoop();
    }

    public void createDelayedGameLoop() {
        gameLoop = new Timeline();
        KeyFrame keyFrame = new KeyFrame(Duration.millis(50), event -> {

            if (rule.hasIa()) {
                try {
                    if (player_turn == 0 && _game_infos.get_black_player_type() == 1) {
                        if (!ia_playing) {
                            executor = Executors.newSingleThreadExecutor();
                            future = executor.submit(() -> game.best_move(1, 1, true));
                            ia_playing = true;
                        } else if (future.isDone()) {
                            playMove(future.get());
                            executor.shutdown();
                            executor = null;
                            ia_playing = false;
                        }
                    } else if (player_turn == 1 && _game_infos.get_white_player_type() == 1) {
                        if (!ia_playing) {
                            executor = Executors.newSingleThreadExecutor();
                            future = executor.submit(() -> game.best_move(2, 2, true));
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
    public void reset_gomoku() {

        ia_playing = false;
        game_end = false;
        player_turn = 0;

        if (gameLoop != null) {
            gameLoop.stop();
        }
        if (executor != null) {
            executor.shutdown();
            executor = null;
        }

        // 清空棋盘历史
        _map.clear();
        _map.add(new Map(_nb_line));

        // 更新 UI
        goban.updateFromMap(_map.get(0));
        map_index = 0;
        handdleButtonPrevNext();

        // 重置 AI
        if (rule.hasIa()) {
            game = new Game(_game_infos.get_rules(), rule.get_board_size());
            game.reset_minmax();
            if (_game_infos.get_black_player_type() == 0 &&
                    _game_infos.get_white_player_type() == 0)
                game.tree_config(1);
            else
                game.tree_config(_game_infos.getLevel());
        }

        // 清空提示
        candidatsList = null;
        hintList = null;
        toggleCandidat = false;
        toggleHint = false;
        changeCandidatVisibility(false);
        changeHintVisibility(false);

        // 隐藏结束弹窗
        _end_popin.setVisible(false);
        _end_popin.setManaged(false);

        // 重新启动 AI 循环
        createDelayedGameLoop();
    }


    public Pane getGameDisplay() {
        return game_display;
    }

    public Button get_home_button() {
        return _back_home;
    }

    public Button get_replay_button() {
        return _replay;
    }

    public Button getBackHomeButton() { return _back_home; }


    public void updateGameDisplay(int height, int i) {
    }
}

