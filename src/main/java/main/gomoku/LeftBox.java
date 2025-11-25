package main.gomoku;

import javafx.geometry.Pos;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;

import java.io.File;

public class LeftBox {

    private final VBox leftBox;
    private int sizeX;
    private int sizeY;

    private static final Image blackBox =
            new Image(new File("./img/black-box.png").toURI().toString());

    // ImageView used to display the image (resizable)
    private final ImageView boxImageView;
    // Container that wraps the image, allowing border effects / glow / positioning adjustments
    private final StackPane boxContainer;

    public LeftBox(int y, int x) {
        sizeX = x;
        sizeY = y;

        leftBox = new VBox();
        leftBox.setPrefSize(x, y);
        leftBox.setMinWidth(Region.USE_PREF_SIZE);
        leftBox.setMinHeight(Region.USE_PREF_SIZE);

        // Initialize the ImageView and add it to the container (slightly lower than center)
        boxImageView = new ImageView(blackBox);
        boxImageView.setPreserveRatio(true);
        // Initial width based on a percentage of the panel width; updated later by updateLeftSize()
        boxImageView.setFitWidth(Math.min(250, sizeX * 0.95));

        boxContainer = new StackPane(boxImageView);
        boxContainer.setAlignment(Pos.CENTER);

        // Allow the container to grow vertically so the image stays centered
        VBox.setVgrow(boxContainer, Priority.ALWAYS);
        leftBox.getChildren().add(boxContainer);
    }

    public void updateLeftSize(int newY, int newX) {
        sizeX = newX;
        sizeY = newY;
        leftBox.setPrefSize(sizeX, sizeY);

        if (boxImageView != null) {
            double targetWidth = Math.max(50, Math.min(250, sizeX * 0.95));
            boxImageView.setFitWidth(targetWidth);
        }
    }

    public VBox getLeftPane() {
        return leftBox;
    }

    /**
     * Sets the highlight status of the left-side box based on the current active player.
     * If currentPlayer equals the black player (PLAYER_BLACK), a glowing highlight is applied.
     * Otherwise, the highlight is removed.
     *
     * @param currentPlayer Identifier of the current player (using constants PLAYER_BLACK / PLAYER_WHITE)
     */
    public void highlightForPlayer(int currentPlayer) {
        if (boxContainer == null) return;
        if (currentPlayer == 0) { // Assuming 0 represents BLACK
            // Apply a golden glow as a highlight indicator
            DropShadow glow = new DropShadow();
            glow.setColor(Color.web("#FFD700"));
            glow.setRadius(60);
            boxContainer.setEffect(glow);
        } else {
            // Not the current player → remove highlight
            boxContainer.setEffect(null);
        }
    }
}
