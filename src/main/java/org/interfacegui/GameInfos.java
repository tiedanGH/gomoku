package org.interfacegui;

import javafx.application.Platform;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.control.Label;
import javafx.scene.text.Font;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.DoubleBinding;
import javafx.scene.control.Button;
import javafx.geometry.Insets;

public class GameInfos {

    private final VBox _game_infos;
    private final VBox _whiteBox = new VBox();
    private final VBox _blackBox = new VBox();

    private int _size_x;
    private int _size_y;

    private Label _white;
    private Label _black;

    // 结果显示（主要用于五子棋的计分模式）
    private final Label _whiteResults = new Label();
    private final Label _blackResults = new Label();
    private final VBox _results = new VBox();

    // 回合显示
    private final Label playTurn = new Label("Round : 0");
    private final Label playerTurn = new Label("Black turn");

    // 各种按钮（确保都有实例）
    private final Button _resign;
    private final Button _undo = new Button("undo");
    private final Button _prev;
    private final Button _next;
    private final Button _hint;
    private final Button backHomeButton = new Button("back Home");

    public GameInfos(int y, int x, Home infos) {
        _size_x = x;
        _size_y = y;

        _game_infos = new VBox();
        _game_infos.setPrefSize(x, y);
        _game_infos.setMinWidth(Region.USE_PREF_SIZE);
        _game_infos.setMinHeight(Region.USE_PREF_SIZE);
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
        _whiteBox.getChildren().add(_white);
        _blackBox.getChildren().add(_black);
        _game_infos.getChildren().addAll(_blackBox, _whiteBox);

        // 初始化按钮
        _resign = new Button("resign");
        _prev = new Button("<");
        _next = new Button(">");
        _hint = new Button("hint");

        // 设置最小高度和默认字体，避免字体=0 导致控件高度=0（跨平台稳健）
        setButtonMinHeight(_prev, _next, _hint,
                _resign, _undo, backHomeButton);

        _prev.setPadding(Insets.EMPTY);
        _next.setPadding(Insets.EMPTY);
        // 若 size_x 非零，则给左右按钮合适的 prefWidth
        if (_size_x > 0) {
            _prev.setPrefWidth((double) _size_x / 2 - ((double) _size_x / 10));
            _next.setPrefWidth((double) _size_x / 2 - ((double) _size_x / 10));
        }
        _prev.setFont(Font.font("Arial", 20));
        _next.setFont(Font.font("Arial", 20));

        // 结果区域默认隐藏
        _results.getChildren().addAll(_whiteResults, _blackResults);
        _results.setVisible(false);
        _results.setManaged(false);

        HBox _button_prev_next = new HBox();
        _button_prev_next.getChildren().addAll(_prev, _next);

        // 将所有控件装进 info 面板
        _game_infos.getChildren().addAll(
                _hint,
                _resign,
                _undo,
                _button_prev_next,
                _results
        );

        // 延迟绑定字体（确保控件已加入 Scene 并有尺寸）
        Platform.runLater(this::bindFonts);
    }

    public void updateGameInfo(int new_y, int new_x){
        _size_x = new_x;
        _size_y = new_y;
        _game_infos.setPrefSize(_size_x, _size_y);
    }

    /**
     * 延迟绑定字体大小，避免构造阶段 _game_infos 高度为 0 导致字体变 0。
     * 同时设置字体下限（避免 min = 0）。
     */
    private void bindFonts() {
        // 字体下限设为 8px，避免 fontSize = 0
        DoubleBinding fontSizeBinding = (DoubleBinding) Bindings.max(
                8,
                Bindings.min(
                        _game_infos.widthProperty().multiply(0.1),
                        _game_infos.heightProperty().multiply(0.1)
                )
        );

        bindFont(playTurn, fontSizeBinding);
        bindFont(playerTurn, fontSizeBinding);
        bindFont(_white, fontSizeBinding);
        bindFont(_black, fontSizeBinding);
        bindFont(_whiteResults, fontSizeBinding);
        bindFont(_blackResults, fontSizeBinding);
    }

    private void bindFont(Label label, DoubleBinding binding) {
        label.fontProperty().bind(Bindings.createObjectBinding(
                () -> new Font("Arial", binding.get()),
                binding
        ));
    }

    private void setButtonMinHeight(Button... buttons) {
        for (Button b : buttons) {
            if (b == null) continue;
            b.setMinHeight(28);            // 跨平台稳定高度
            b.setFont(Font.font("Arial", 14)); // 默认字体，避免测量异常
        }
    }

    private void addText() {
        _white = new Label("white turn");
        _black = new Label("black turn");

        // 给初始字体，使得在极端情况下也不会为 0
        _white.setFont(Font.font("Arial", 12));
        _black.setFont(Font.font("Arial", 12));

        // 其它字体绑定由 bindFonts() 延迟完成
    }

    public void setPLayTurn(int turn) {
        playTurn.setText("Round : " + turn);
    }

    // 旧名：setPLayerTurn（若项目中使用此名也兼容）
    public void setPLayerTurn(int play) {
        if (play == 0)
            playerTurn.setText("Black turn");
        else
            playerTurn.setText("White turn");
    }

    public VBox getResultsBox() {
        return _results;
    }

    // ---------- Reset / Clear / Update ----------
    public void reset_infos(Home infos) {
        // 计时已删除，这里只重置回合与当前玩家显示
        playTurn.setText("Round : 0");
        playerTurn.setText("Black turn");
    }

    // ---------- Getters for buttons / boxes ----------
    public Button getBackHomeButton() {
        return backHomeButton;
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

    public Button getResignButton() {
        return _resign;
    }

    public Button getUndoButton() {
        return _undo;
    }

    public Button getHintButton() {
        return _hint;
    }

    public VBox getBlackBox() {
        return _blackBox;
    }

    public VBox getWhiteBox() {
        return _whiteBox;
    }
}
