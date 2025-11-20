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
import javafx.geometry.Pos;

public class LeftBox {

    private final VBox gameInfos;
    private final VBox whiteBox = new VBox();
    private final VBox blackBox = new VBox();
    private int sizeX;
    private int sizeY;
    private Label white;
    private Label black;
    private final Label turn = new Label("Round : 0");

    private final Button resign;
    private final Button undo = new Button("undo");
    private final Button previous;
    private final Button next;
    private final Button hint;

    public LeftBox(int y, int x) {
        sizeX = x;
        sizeY = y;

        gameInfos = new VBox();
        gameInfos.setPrefSize(x, y);
        gameInfos.setMinWidth(Region.USE_PREF_SIZE);
        gameInfos.setMinHeight(Region.USE_PREF_SIZE);
        gameInfos.setBackground(new Background(new BackgroundFill(Color.web("#DEB887"), null, null)));
        gameInfos.getChildren().add(turn);

        addText();

        whiteBox.getChildren().add(white);
        blackBox.getChildren().add(black);
        gameInfos.getChildren().addAll(blackBox, whiteBox);

        resign = new Button("resign");
        previous = new Button("<");
        next = new Button(">");
        hint = new Button("hint");

        setButtonMinHeight(previous, next, hint, undo, resign);

        previous.setPadding(Insets.EMPTY);
        next.setPadding(Insets.EMPTY);

        if (sizeX > 0) {
            previous.setPrefWidth((double) sizeX / 2 - ((double) sizeX / 10));
            next.setPrefWidth((double) sizeX / 2 - ((double) sizeX / 10));
        }
        previous.setFont(Font.font("Arial", 20));
        next.setFont(Font.font("Arial", 20));

        // 按钮排版：三行（每行水平居中）
        Platform.runLater(this::bindFonts);

        ImageButtonUtil.applyImage(hint, "./img/hint.png", 50, 50);
        ImageButtonUtil.applyImage(undo, "./img/undo.png", 50, 50);
        ImageButtonUtil.applyImage(previous, "./img/prev.png", 50, 50);
        ImageButtonUtil.applyImage(next, "./img/next.png", 50, 50);
        ImageButtonUtil.applyImage(resign, "./img/resign.png", 50, 50);

        HBox row1 = new HBox(hint);
        row1.setAlignment(Pos.CENTER);
        HBox row2 = new HBox(undo);
        row2.setAlignment(Pos.CENTER);
        HBox row3 = new HBox(-15, previous, next);
        row3.setAlignment(Pos.CENTER);
        HBox row4 = new HBox(resign);
        row4.setAlignment(Pos.CENTER);

        VBox bottomButtons = new VBox(22, row1, row2, row3, row4);
        bottomButtons.setAlignment(Pos.CENTER);
        // 若需靠底部放置，可在外部调整 gameInfos 的布局；此处确保居中对齐
        gameInfos.getChildren().add(bottomButtons);
    }

    public void updateGameInfo(int newY, int newX) {
        sizeX = newX;
        sizeY = newY;
        gameInfos.setPrefSize(sizeX, sizeY);
    }

    // bind fonts to gameInfos size
    private void bindFonts() {
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
            b.setMinHeight(28);
            b.setFont(Font.font("Arial", 14));
        }
    }

    private void addText() {
        white = new Label("white turn");
        black = new Label("black turn");
        white.setFont(Font.font("Arial", 12));
        black.setFont(Font.font("Arial", 12));
    }

    public void setTurn(int turn) {
        this.turn.setText("Round : " + turn);
    }

    public VBox getGameInfos() {
        return gameInfos;
    }

    public Button getPreviousButton() {
        return previous;
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
