package org.interfacegui;

import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.control.Label;
import javafx.scene.text.Font;

public class LeftBox {

    private final VBox gameInfos;
    private final VBox whiteBox = new VBox();
    private final VBox blackBox = new VBox();
    private int sizeX;
    private int sizeY;
    private Label white;
    private Label black;
    private final Label turn = new Label("Round : 0");

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
    }

    public void updateLeftSize(int newY, int newX) {
        sizeX = newX;
        sizeY = newY;
        gameInfos.setPrefSize(sizeX, sizeY);
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

    public VBox getLeftPane() {
        return gameInfos;
    }

    public VBox getBlackBox() {
        return blackBox;
    }

    public VBox getWhiteBox() {
        return whiteBox;
    }
}
