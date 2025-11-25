package main.gomoku;

import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Background;

public class ImageButtonUtil {

    /** turn regular button into image button */
    public static void applyImage(Button btn, String imgPath, double w, double h) {

        Image img = new Image("file:" + imgPath);
        ImageView view = new ImageView(img);

        view.setFitWidth(w);
        view.setFitHeight(h);
        view.setPreserveRatio(true);

        btn.setGraphic(view);
        btn.setText("");
        btn.setBackground(Background.EMPTY);
    }
}

