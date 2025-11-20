package org.interfacegui;

import javafx.scene.layout.*;
import javafx.scene.paint.Color;

/**
 * RightBox
 * 右侧结束信息面板：展示胜负文本与结束弹窗（包含 replay / backHome 两个按钮）。
 * - 负责展示/隐藏弹窗与放大/恢复胜利文本字号
 * - 负责自身尺寸更新（由 Gomoku.updateGameDisplay 调用）
 */
public class RightBox {

    private final VBox rightBox;
    private int sizeX;
    private int sizeY;

    public RightBox(int y, int x) {
        sizeX = x;
        sizeY = y;

        rightBox = new VBox();
        rightBox.setPrefSize(x, y);
        rightBox.setMinWidth(Region.USE_PREF_SIZE);
        rightBox.setMinHeight(Region.USE_PREF_SIZE);
        rightBox.setBackground(new Background(new BackgroundFill(Color.web("#DEB887"), null, null)));

//        rightBox.getChildren().addAll();
    }

    public void updateRightSize(int newY, int newX) {
        sizeX = newX;
        sizeY = newY;
        rightBox.setPrefSize(sizeX, sizeY);
    }

    public VBox getRightPane() {
        return rightBox;
    }
}
