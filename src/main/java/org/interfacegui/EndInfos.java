package org.interfacegui;

import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.text.Font;

/**
 * EndInfos
 * 右侧结束信息面板：展示胜负文本与结束弹窗（包含 replay / backHome 两个按钮）。
 * - 负责展示/隐藏弹窗与放大/恢复胜利文本字号
 * - 负责自身尺寸更新（由 Gomoku.updateGameDisplay 调用）
 */
public class EndInfos {

    private final VBox endInfos;
    private int sizeX;
    private int sizeY;
    private final Label endText;
    private final VBox popupBox;
    private final Button replay;
    private final Button backHome;

    public EndInfos(int y, int x) {
        sizeX = x;
        sizeY = y;

        endInfos = new VBox();
        endInfos.setPrefSize(x, y);
        endInfos.setMinWidth(Region.USE_PREF_SIZE);
        endInfos.setMinHeight(Region.USE_PREF_SIZE);
        endInfos.setBackground(new Background(new BackgroundFill(Color.web("#DEB887"), null, null)));

        endText = new Label("");
        endText.setFont(Font.font("Arial", 16));
        // popup 只包含按钮（避免节点重复父节点问题）
        popupBox = new VBox();
        replay = new Button("Replay");
        backHome = new Button("Back Home");

        ImageButtonUtil.applyImage(replay, "./img/replay.png", 30, 30);
        ImageButtonUtil.applyImage(backHome, "./img/home.png", 30, 30);

        setButtonMinHeight(replay, backHome);

        popupBox.getChildren().addAll(replay, backHome);
        popupBox.setVisible(false);
        popupBox.setManaged(false);

        endInfos.getChildren().addAll(endText, popupBox);
    }

    /**
     * 显示胜利信息并显示弹窗（同时放大文字）
     */
    public void showEnd(int winner) {
        if (winner == 0) endText.setText("Draw");
        else if (winner == 1) endText.setText("Black Win");
        else endText.setText("White Win");
        endText.setFont(Font.font("Arial", 28));
        // ensure right-side pane is visible and contributes layout
        endInfos.setVisible(true);
        endInfos.setManaged(true);
        showPopup();
    }

    public void showEnd(String message) {
        endText.setText(message);
        endText.setFont(Font.font("Arial", 28));
        showPopup();
    }

    public void showPopup() {
        popupBox.setVisible(true);
        popupBox.setManaged(true);
    }

    public void hidePopup() {
        popupBox.setVisible(false);
        popupBox.setManaged(false);
        endText.setFont(Font.font("Arial", 16));
        endText.setText("");
    }

    public void updateEndInfo(int newY, int newX) {
        sizeX = newX;
        sizeY = newY;
        endInfos.setPrefSize(sizeX, sizeY);
    }

    private void setButtonMinHeight(Button... buttons) {
        for (Button b : buttons) {
            if (b == null) continue;
            b.setMinHeight(28);
            b.setFont(Font.font("Arial", 14));
        }
    }

    public VBox getEndInfos() {
        return endInfos;
    }

    public Button getReplayButton() {
        return replay;
    }

    public Button getBackHomeButton() {
        return backHome;
    }
}
