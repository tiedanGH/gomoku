package main.gomoku;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;

/*
 * The BottomBox class creates and manages the bottom control panel of the Gomoku UI.
 */
public class BottomBox {

    private final VBox bottomBox;
    private final HBox bottomButtons;
    private int sizeX;
    private int sizeY;

    private final Button resign = new Button();
    private final Button undo = new Button();
    private final Button previous = new Button();
    private final Button next = new Button();
    private final Button hint = new Button();

    public BottomBox(int y, int x) {
        sizeX = x;
        sizeY = y;

        bottomBox = new VBox();
        bottomBox.setPrefSize(x, y);
        bottomBox.setMinWidth(Region.USE_PREF_SIZE);
        bottomBox.setMinHeight(Region.USE_PREF_SIZE);

        ImageButtonUtil.applyImage(hint, "./img/hint.png", 50, 50);
        ImageButtonUtil.applyImage(undo, "./img/undo.png", 50, 50);
        ImageButtonUtil.applyImage(previous, "./img/prev.png", 50, 50);
        ImageButtonUtil.applyImage(next, "./img/next.png", 50, 50);
        ImageButtonUtil.applyImage(resign, "./img/resign.png", 50, 50);

        bottomButtons = new HBox(10, hint, undo, previous, next, resign);
        // center buttons horizontally and let HBox fill the container width
        bottomButtons.setAlignment(Pos.CENTER);
        bottomButtons.prefWidthProperty().bind(bottomBox.widthProperty());
        bottomButtons.setPadding(new Insets(15, 0, 0, 0)); // top padding only

        bottomBox.getChildren().add(bottomButtons);

        // adjust image sizes initially and when container size changes
        adjustButtonImageSizes();
        bottomBox.widthProperty().addListener((obs, oldV, newV) -> adjustButtonImageSizes());
        bottomBox.heightProperty().addListener((obs, oldV, newV) -> adjustButtonImageSizes());
    }

    public void updateBottomSize(int newY, int newX) {
        sizeX = newX;
        sizeY = newY;
        bottomBox.setPrefSize(sizeX, sizeY);
        adjustButtonImageSizes();
    }

    private void adjustButtonImageSizes() {
        double imageSize = getImageSize();
        for (Node n : bottomButtons.getChildren()) {
            if (n instanceof Button b) {
                Node graphic = b.getGraphic();
                if (graphic instanceof ImageView iv) {
                    iv.setPreserveRatio(true);
                    iv.setFitWidth(imageSize);
                    iv.setFitHeight(imageSize);
                }
            }
        }
    }

    // compute available width
    private double getImageSize() {
        final int btnCount = bottomButtons.getChildren().size();
        double totalWidth = bottomBox.getWidth() > 0 ? bottomBox.getWidth() : sizeX;
        double spacing = bottomButtons.getSpacing() * (btnCount - 1);
        double availableForButtons = Math.max(0, totalWidth - spacing - 40);
        double maxByWidth = availableForButtons / btnCount;
        double maxByHeight = Math.max(20, bottomBox.getHeight() - 30);
        return Math.min(Math.min(maxByWidth, maxByHeight), 100);
    }

    public void gameEnd() {
        hint.setDisable(true);
        resign.setDisable(true);
        undo.setDisable(true);
    }

    public void resetButtons() {
        hint.setDisable(false);
        resign.setDisable(false);
        undo.setDisable(false);
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
