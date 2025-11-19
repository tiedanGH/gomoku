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
    private final Label turn = new Label("Round : 0");

    private final Button resign;
    private final Button undo = new Button("undo");
    private final Button previous;
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

        HBox buttonStep = new HBox();
        buttonStep.getChildren().addAll(previous, next);

        gameInfos.getChildren().addAll(
                hint,
                undo,
                buttonStep,
                resign
        );

        Platform.runLater(this::bindFonts);
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
