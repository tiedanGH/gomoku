package org.interfacegui;

import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;

import java.io.File;

import static org.interfacegui.Gomoku.backgroundColor;

public class TopBox {

    private final VBox topBox;
    private int sizeX;
    private int sizeY;

    private final HBox topPane;
    private final ImageView win = new ImageView();
    private final Button replay = new Button("Replay");
    private final Button backHome = new Button("Back Home");

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
        topBox.setBackground(new Background(new BackgroundFill(Color.web(backgroundColor), null, null)));

        ImageButtonUtil.applyImage(replay, "./img/replay.png", 50, 50);
        ImageButtonUtil.applyImage(backHome, "./img/home.png", 50, 50);

        topPane = new HBox(10, win, replay, backHome);
        topPane.setAlignment(Pos.CENTER_LEFT);
        topBox.setAlignment(Pos.CENTER_LEFT);
        topPane.setVisible(false);
        topPane.setManaged(false);

        topBox.getChildren().add(topPane);
    }

    public void updateTopSize(int newY, int newX) {
        sizeX = newX;
        sizeY = newY;
        topBox.setPrefSize(sizeX, sizeY);
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

    public Button getBackHomeButton() {
        return backHome;
    }
}
