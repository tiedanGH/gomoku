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

    private final VBox gameInfos;
    private final VBox whiteBox = new VBox();
    private final VBox blackBox = new VBox();

    private int sizeX;
    private int sizeY;

    private Label white;
    private Label black;

    // 回合显示
    private final Label turn = new Label("Round : 0");

    // 各种按钮（确保都有实例）
    private final Button resign;
    private final Button undo = new Button("undo");
    private final Button prev;
    private final Button next;
    private final Button hint;

    public GameInfos(int y, int x) {
        sizeX = x;
        sizeY = y;

        gameInfos = new VBox();
        gameInfos.setPrefSize(x, y);
        gameInfos.setMinWidth(Region.USE_PREF_SIZE);
        gameInfos.setMinHeight(Region.USE_PREF_SIZE);
        gameInfos.setBackground(new Background(new BackgroundFill(Color.web("#ADBAC0"), null, null)));

        // 先加回合数显示
        gameInfos.getChildren().add(turn);

        // 填充白/黑方名称与囚徒数
        addText();

        // 添加白/黑方信息框（名字 + 囚徒数）
        whiteBox.getChildren().add(white);
        blackBox.getChildren().add(black);
        gameInfos.getChildren().addAll(blackBox, whiteBox);

        // 初始化按钮
        resign = new Button("resign");
        prev = new Button("<");
        next = new Button(">");
        hint = new Button("hint");

        // 设置最小高度和默认字体，避免字体=0 导致控件高度=0（跨平台稳健）
        setButtonMinHeight(prev, next, hint, undo, resign);

        prev.setPadding(Insets.EMPTY);
        next.setPadding(Insets.EMPTY);
        // 若 size_x 非零，则给左右按钮合适的 prefWidth
        if (sizeX > 0) {
            prev.setPrefWidth((double) sizeX / 2 - ((double) sizeX / 10));
            next.setPrefWidth((double) sizeX / 2 - ((double) sizeX / 10));
        }
        prev.setFont(Font.font("Arial", 20));
        next.setFont(Font.font("Arial", 20));

        HBox _button_prev_next = new HBox();
        _button_prev_next.getChildren().addAll(prev, next);

        // 将所有控件装进 info 面板
        gameInfos.getChildren().addAll(
                hint,
                undo,
                _button_prev_next,
                resign
        );

        // 延迟绑定字体（确保控件已加入 Scene 并有尺寸）
        Platform.runLater(this::bindFonts);
     // === 图片替换按钮 ===
     
     ImageButtonUtil.applyImage(hint, "./img/hint.png", 30, 30);
     ImageButtonUtil.applyImage(undo, "./img/undo.png", 30, 30);
     ImageButtonUtil.applyImage(prev, "./img/prev.png", 30, 30);
     ImageButtonUtil.applyImage(next, "./img/next.png", 40, 40);
     ImageButtonUtil.applyImage(resign, "./img/resign.png", 30, 30);
     
     HBox bottomButtons = new HBox(20);
     bottomButtons.setLayoutY(sizeY - 120);
     bottomButtons.setLayoutX((sizeX - 400) / 2);
     bottomButtons.getChildren().addAll(hint, undo, prev, next, resign);

     gameInfos.getChildren().add(bottomButtons);


    }

    public void updateGameInfo(int new_y, int new_x){
        sizeX = new_x;
        sizeY = new_y;
        gameInfos.setPrefSize(sizeX, sizeY);
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
                        gameInfos.widthProperty().multiply(0.1),
                        gameInfos.heightProperty().multiply(0.1)
                )
        );

        bindFont(turn, fontSizeBinding);
        bindFont(white, fontSizeBinding);
        bindFont(black, fontSizeBinding);
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
        white = new Label("white turn");
        black = new Label("black turn");

        // 给初始字体，使得在极端情况下也不会为 0
        white.setFont(Font.font("Arial", 12));
        black.setFont(Font.font("Arial", 12));

        // 其它字体绑定由 bindFonts() 延迟完成
    }

    public void setTurn(int turn) {
        this.turn.setText("Round : " + turn);
    }

    public VBox getGameInfos() {
        return gameInfos;
    }

    public Button getPrevButton() {
        return prev;
    }

    public Button getNextButton() {
        return next;
    }

    public Button getResignButton() {
        return resign;
    }

    public Button getUndoButton() {
        return undo;
    }

    public Button getHintButton() {
        return hint;
    }

    public VBox getBlackBox() {
        return blackBox;
    }

    public VBox getWhiteBox() {
        return whiteBox;
    }
}
