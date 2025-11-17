package org.interfacegui;

import javafx.scene.paint.Color;
import javafx.scene.layout.VBox;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.control.Label;
import javafx.scene.text.Font;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.DoubleBinding;
import javafx.scene.control.Button;
import javafx.geometry.Insets;

/**
 * GameInfos 精简版 —— 已完全删除计时功能
 * -----------------------------------------------------
 * 删除内容：
 *  - 白方/黑方倒计时
 *  - last move time / average move time
 *  - 所有与时间相关的字段、Label、方法
 *
 * 保留内容：
 *  - 白/黑方名称
 *  - 囚徒数量显示
 *  - 回合数显示（Round）
 *  - 结果显示（for Go 计分结果）
 *  - 各种操作按钮（prev/next/undo/pass/hint/candidats/forbidden/export/resign/backHome）
 */
public class GameInfos {

    private VBox _game_infos;
    private VBox _whiteBox = new VBox();
    private VBox _blackBox = new VBox();

    private int _size_x;
    private int _size_y;

    private Label _white;
    private Label _black;

    private String _black_prisonners;
    private String _white_prisonners;

    private Label _black_label_prisonners = new Label();
    private Label _white_label_prisonners = new Label();

    // 结果显示（主要用于 Go 计分模式）
    private Label _whiteResults = new Label();
    private Label _blackResults = new Label();
    private VBox _results = new VBox();

    // 回合显示
    private Label playTurn = new Label("Round : 0");
    private Label playerTurn = new Label("Black turn");

    // 各种按钮
    private Button _resign;
    private Button _undo = new Button("undo");
    private Button _export = new Button("export");
    private Button _prev;
    private Button _next;
    private Button _pass = new Button("pass");
    private Button _candidats;
    private Button _hint;
    private Button _forbidden = new Button("forbiddens");
    private Button backHomeButton = new Button("back Home");

    private HBox _button_prev_next = new HBox();

    public GameInfos(int y, int x, Home infos) {
        _size_x = x;
        _size_y = y;

        _game_infos = new VBox();
        _game_infos.setPrefSize(x, y);
        _game_infos.setBackground(new Background(new BackgroundFill(Color.web("#ADBAC0"), null, null)));

        // 默认隐藏“返回主页”按钮，某些模式（如 LEARNING）中再打开
        backHomeButton.setManaged(false);
        backHomeButton.setVisible(false);
        _game_infos.getChildren().add(backHomeButton);

        // 先加回合数显示
        _game_infos.getChildren().add(playTurn);

        // 填充白/黑方名称与囚徒数
        addText();

        // 添加白/黑方信息框（名字 + 囚徒数）
        _whiteBox.getChildren().addAll(_white, _white_label_prisonners);
        _blackBox.getChildren().addAll(_black, _black_label_prisonners);
        _game_infos.getChildren().addAll(_blackBox, _whiteBox);

        // 初始化按钮
        _resign = new Button("resign");
        _prev = new Button("<");
        _next = new Button(">");
        _candidats = new Button("show candidats");
        _hint = new Button("hint");

        _prev.setPadding(Insets.EMPTY);
        _next.setPadding(Insets.EMPTY);
        _prev.setPrefWidth(_size_x / 2 - (_size_x / 10));
        _prev.setFont(Font.font("Arial", 20));

        _next.setPrefWidth(_size_x / 2 - (_size_x / 10));
        _next.setFont(Font.font("Arial", 20));

        // 结果区域，默认隐藏
        _results.getChildren().addAll(_whiteResults, _blackResults);
        _results.setVisible(false);
        _results.setManaged(false);

        _button_prev_next.getChildren().addAll(_prev, _next);

        // 最终把所有控件装进 info 面板
        _game_infos.getChildren().addAll(
                _candidats,
                _hint,
                _forbidden,
                _resign,
                _undo,
                _button_prev_next,
                _pass,
                _results,
                _export
        );
    }

    /**
     * 根据面板大小动态设定字体，初始化白/黑方 Label 与囚徒 Label。
     */
    private void addText() {
        _white = new Label("white");
        _black = new Label("black");

        _white.setFont(new Font("Arial", 8));
        _black.setFont(new Font("Arial", 8));

        DoubleBinding fontSizeBinding = (DoubleBinding) Bindings.min(
                _game_infos.widthProperty().multiply(0.1),
                _game_infos.heightProperty().multiply(0.1)
        );

        _white.fontProperty().bind(Bindings.createObjectBinding(
                () -> new Font("Arial", fontSizeBinding.get()),
                fontSizeBinding
        ));

        _black.fontProperty().bind(Bindings.createObjectBinding(
                () -> new Font("Arial", fontSizeBinding.get()),
                fontSizeBinding
        ));

        // 初始化囚徒数
        _white_prisonners = "prisonners: 0";
        _black_prisonners = "prisonners: 0";

        _white_label_prisonners.setText(_white_prisonners);
        _black_label_prisonners.setText(_black_prisonners);

        _white_label_prisonners.fontProperty().bind(Bindings.createObjectBinding(
                () -> new Font("Arial", fontSizeBinding.get()),
                fontSizeBinding
        ));
        _black_label_prisonners.fontProperty().bind(Bindings.createObjectBinding(
                () -> new Font("Arial", fontSizeBinding.get()),
                fontSizeBinding
        ));
    }

    // ================= 供外部调用的接口（Gomoku 等会用到） =================

    public void setWhiteResults(String res) {
        _whiteResults.setText(res);
    }

    public void setBlackResults(String res) {
        _blackResults.setText(res);
    }

    public void setPLayTurn(Integer turn) {
        playTurn.setText("Round : " + turn.toString());
    }

    public void setPLayerTurn(int play) {
        if (play == 0)
            playerTurn.setText("Black turn");
        else
            playerTurn.setText("White turn");
    }

    public VBox getResultsBox() {
        return _results;
    }

    public void set_white_prisonners(String str) {
        _white_label_prisonners.setText("prisonners : " + str);
    }

    public void set_black_prisonners(String str) {
        _black_label_prisonners.setText("prisonners : " + str);
    }

    public void reset_infos(Home infos) {
        // 计时已删除，这里只重置回合与当前玩家显示
        playTurn.setText("Round : 0");
        playerTurn.setText("Black turn");
    }

    public Button getBackHomeButton() {
        return backHomeButton;
    }

    public Button getForbiddeButton() {
        return _forbidden;
    }

    public VBox getGameInfos() {
        return _game_infos;
    }

    public Button getPrevButton() {
        return _prev;
    }

    public Button getNextButton() {
        return _next;
    }

    public Button getCandidatsButton() {
        return _candidats;
    }

    public Button getResignButton() {
        return _resign;
    }

    public Button getUndoButton() {
        return _undo;
    }

    public Button getExportButton() {
        return _export;
    }

    public Button getHintButton() {
        return _hint;
    }

    public Button getPassButton() {
        return _pass;
    }

    public VBox getBlackBox() {
        return _blackBox;
    }

    public VBox getWhiteBox() {
        return _whiteBox;
    }

    public void updateGameInfo(int new_y, int new_x) {
        _size_x = new_x;
        _size_y = new_y;
        _game_infos.setPrefSize(new_x, new_y);
    }

    public void clear() {
        // 目前不用做什么，如果以后想重置信息可以在这里补
    }
}

