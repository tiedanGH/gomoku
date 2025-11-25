package main.gomoku;

import javafx.geometry.Pos;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;

import java.io.File;

/**
 * RightBox
 * Right-side end-game information panel: displays the victory message
 * and the end-game pop-up (which includes the replay and backHome buttons).
 *
 * - Responsible for showing/hiding the pop-up and enlarging/resetting the victory text size
 * - Handles resizing of this panel (called by Gomoku.updateGameDisplay)
 *
 * Extension:
 * Displays a white box image at the lower-middle area, and supports
 * applying/removing a highlight glow depending on which player is currently active.
 */
public class RightBox {

    private final VBox rightBox;
    private int sizeX;
    private int sizeY;

    private static final Image whiteBox =
            new Image(new File("./img/white-box.png").toURI().toString());

    private final ImageView boxImageView;
    private final StackPane boxContainer;

    public RightBox(int y, int x) {
        sizeX = x;
        sizeY = y;

        rightBox = new VBox();
        rightBox.setPrefSize(x, y);
        rightBox.setMinWidth(Region.USE_PREF_SIZE);
        rightBox.setMinHeight(Region.USE_PREF_SIZE);

        // Initialize the white box image view and add it to the container
        boxImageView = new ImageView(whiteBox);
        boxImageView.setPreserveRatio(true);
        boxImageView.setFitWidth(Math.min(250, sizeX * 0.95));

        boxContainer = new StackPane(boxImageView);
        boxContainer.setAlignment(Pos.CENTER);

        VBox.setVgrow(boxContainer, Priority.ALWAYS);
        rightBox.getChildren().add(boxContainer);
    }

    public void updateRightSize(int newY, int newX) {
        sizeX = newX;
        sizeY = newY;
        rightBox.setPrefSize(sizeX, sizeY);

        if (boxImageView != null) {
            double targetWidth = Math.max(50, Math.min(250, sizeX * 0.95));
            boxImageView.setFitWidth(targetWidth);
        }
    }

    public VBox getRightPane() {
        return rightBox;
    }

    /**
     * Sets the highlight state of the right-side box based on the active player.
     * If currentPlayer corresponds to WHITE (PLAYER_WHITE), apply a golden glow.
     * Otherwise, remove any glow.
     *
     * @param currentPlayer Identifier of the current active player
     *                      (using constants PLAYER_BLACK / PLAYER_WHITE)
     */
    public void highlightForPlayer(int currentPlayer) {
        if (boxContainer == null) return;
        if (currentPlayer == 1) { // Assuming 1 represents WHITE
            DropShadow glow = new DropShadow();
            glow.setColor(Color.web("#FFD700"));
            glow.setRadius(60);
            boxContainer.setEffect(glow);
        } else {
            boxContainer.setEffect(null);
        }
    }
}

