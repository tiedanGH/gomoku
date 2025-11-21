package main.gomoku;

import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;

import java.io.File;

public class TopBox {

    private final VBox topBox;
    private int sizeX;
    private int sizeY;

    private final HBox topPane;
    private final ImageView win = new ImageView();
    private final Button replay = new Button();
    private final Button home = new Button();

    private final Image draw =
            new Image(new File("./img/draw.png").toURI().toString());
    private final Image blackWin =
            new Image(new File("./img/black-win.png").toURI().toString());
    private final Image whiteWin =
            new Image(new File("./img/white-win.png").toURI().toString());

    public TopBox(int y, int x) {
        sizeX = x;
        sizeY = y;

        topBox = new VBox();
        topBox.setPrefSize(x, y);
        topBox.setMinWidth(Region.USE_PREF_SIZE);
        topBox.setMinHeight(Region.USE_PREF_SIZE);

        ImageButtonUtil.applyImage(replay, "./img/replay.png", 50, 50);
        ImageButtonUtil.applyImage(home, "./img/home.png", 50, 50);

        topPane = new HBox(10, win, replay, home);
        topPane.setAlignment(Pos.CENTER);
        topBox.setAlignment(Pos.CENTER);
        topPane.prefWidthProperty().bind(topBox.widthProperty());
        topPane.setVisible(false);
        topPane.setManaged(false);
        topBox.getChildren().add(topPane);

        // adjust image sizes
        adjustImageSizes();
        topBox.widthProperty().addListener((obs, oldV, newV) -> adjustImageSizes());
        topBox.heightProperty().addListener((obs, oldV, newV) -> adjustImageSizes());
    }

    public void updateTopSize(int newY, int newX) {
        sizeX = newX;
        sizeY = newY;
        topBox.setPrefSize(sizeX, sizeY);
        adjustImageSizes();
    }

    private void adjustImageSizes() {
        double totalWidth = topBox.getWidth() > 0 ? topBox.getWidth() : sizeX;
        double totalHeight = topBox.getHeight() > 0 ? topBox.getHeight() : sizeY;
        int btnCount = 0;
        for (Node n : topPane.getChildren()) {
            if (n instanceof Button) btnCount++;
        }

        double spacing = topPane.getSpacing() * Math.max(0, topPane.getChildren().size() - 1);
        double availableWidth = Math.max(0, totalWidth - spacing - 20);
        double winReserved = (win.isVisible() && win.getImage() != null) ? availableWidth * 0.70 : 0;
        double remainingWidth = Math.max(0, availableWidth - winReserved);

        double buttonWidth = btnCount > 0 ? remainingWidth / btnCount : remainingWidth;
        double btnSize = Math.max(16, Math.min(buttonWidth, totalHeight - 6));
        double winWidth = getWinTargetWidth(totalHeight, winReserved);
        // apply sizes
        for (Node n : topPane.getChildren()) {
            if (n instanceof Button b) {
                Node g = b.getGraphic();
                if (g instanceof ImageView iv) {
                    iv.setPreserveRatio(true);
                    iv.setFitWidth(btnSize);
                    iv.setFitHeight(btnSize);
                }
            } else if (n == win && win.getImage() != null) {
                win.setPreserveRatio(true);
                if (winWidth > 0) {
                    win.setFitWidth(winWidth);
                    win.setFitHeight(0);
                } else {
                    double fallback = Math.max(20, totalHeight - 6);
                    win.setFitHeight(fallback);
                    win.setFitWidth(0);
                }
            }
        }
    }

    private double getWinTargetWidth(double totalHeight, double winReserved) {
        double winWidth = 0;
        if (win.isVisible() && win.getImage() != null) {
            Image img = win.getImage();
            double imgRatio = img.getWidth() > 0 ? (img.getHeight() / img.getWidth()) : 1.0;
            double maxHeight = Math.max(20, totalHeight - 4);
            double desiredWidth = winReserved;
            double impliedHeight = desiredWidth * imgRatio;
            if (impliedHeight > maxHeight) {
                desiredWidth = maxHeight / imgRatio;
            }
            winWidth = Math.max(20, desiredWidth);
        }
        return winWidth;
    }

    public void showEnd(int winner) {
        Image img;
        if (winner == 0)       img = draw;
        else if (winner == 1)  img = blackWin;
        else                   img = whiteWin;

        win.setImage(img);
        win.setVisible(true);
        topPane.setVisible(true);
        topPane.setManaged(true);
        adjustImageSizes();
    }

    public void hideEnd() {
        win.setVisible(false);
        topPane.setVisible(false);
        topPane.setManaged(false);
    }

    public VBox getTopPane() {
        return topBox;
    }

    public Button getReplayButton() {
        return replay;
    }

    public Button getHomeButton() {
        return home;
    }
}
