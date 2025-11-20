package org.interfacegui;

import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;

import static org.interfacegui.Gomoku.backgroundColor;

public class BottomBox {

    private final VBox bottomBox;
    private final int bottomWidth = 350;
    private final HBox bottomButtons;
    private int sizeX;
    private int sizeY;

    private final Button resign = new Button("resign");
    private final Button undo = new Button("undo");
    private final Button previous = new Button("<");
    private final Button next = new Button(">");
    private final Button hint = new Button("hint");

    public BottomBox(int y, int x) {
        sizeX = x;
        sizeY = y;

        bottomBox = new VBox();
        bottomBox.setPrefSize(x, y);
        bottomBox.setMinWidth(Region.USE_PREF_SIZE);
        bottomBox.setMinHeight(Region.USE_PREF_SIZE);
        bottomBox.setBackground(new Background(new BackgroundFill(Color.web(backgroundColor), null, null)));

        ImageButtonUtil.applyImage(hint, "./img/hint.png", 50, 50);
        ImageButtonUtil.applyImage(undo, "./img/undo.png", 50, 50);
        ImageButtonUtil.applyImage(previous, "./img/prev.png", 50, 50);
        ImageButtonUtil.applyImage(next, "./img/next.png", 50, 50);
        ImageButtonUtil.applyImage(resign, "./img/resign.png", 50, 50);

        bottomButtons = new HBox(10, hint, undo, previous, next, resign);
        bottomButtons.setMaxWidth(bottomWidth);
        bottomButtons.setPadding(new Insets(15, 0, 0, (double) (sizeX - bottomWidth) / 2));

        bottomBox.getChildren().add(bottomButtons);
    }

    public void updateBottomSize(int newY, int newX) {
        sizeX = newX;
        sizeY = newY;
        bottomBox.setPrefSize(sizeX, sizeY);
        bottomButtons.setPadding(new Insets(10, 0, 0, (double) (sizeX - bottomWidth) / 2));
    }

    public VBox getBottomPane() {
        return bottomBox;
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
}
