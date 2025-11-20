package org.interfacegui;

import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Background;

public class ImageButtonUtil {

    /** 将普通按钮变成图片按钮 */
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

