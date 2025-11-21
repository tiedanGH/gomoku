package main.gomoku;

import javafx.geometry.Pos;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;

import java.io.File;

import static main.gomoku.Gomoku.backgroundColor;

/**
 * RightBox
 * 右侧结束信息面板：展示胜负文本与结束弹窗（包含 replay / backHome 两个按钮）。
 * - 负责展示/隐藏弹窗与放大/恢复胜利文本字号
 * - 负责自身尺寸更新（由 Gomoku.updateGameDisplay 调用）
 * *
 * 此处扩展：在右侧中下部展示白色盒子图片，并支持根据当前行动玩家为图片添加/移除高亮提示。
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
        rightBox.setBackground(new Background(new BackgroundFill(Color.web(backgroundColor), null, null)));

        // 初始化白色盒子图片视图并加入容器
        boxImageView = new ImageView(whiteBox);
        boxImageView.setPreserveRatio(true);
        boxImageView.setFitWidth(Math.min(250, sizeX * 0.95));

        boxContainer = new StackPane(boxImageView);
        boxContainer.setAlignment(Pos.CENTER);
//        boxContainer.setTranslateY(sizeY * 0.2);

        VBox.setVgrow(boxContainer, Priority.ALWAYS);
        rightBox.getChildren().add(boxContainer);

//        rightBox.getChildren().addAll();
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
     * 根据当前行动玩家来设置右侧盒子的高亮状态。
     * 如果 currentPlayer 等于白方（PLAYER_WHITE），为图片添加高亮（光晕）；否则移除高亮。
     *
     * @param currentPlayer 当前行动玩家标识（使用类中的常量 PLAYER_BLACK / PLAYER_WHITE）
     */
    public void highlightForPlayer(int currentPlayer) {
        if (boxContainer == null) return;
        if (currentPlayer == 1) { // Assuming 1 represents WHITE
            DropShadow glow = new DropShadow();
            glow.setColor(Color.web("#FFD700"));
            glow.setRadius(50);
            boxContainer.setEffect(glow);
        } else {
            boxContainer.setEffect(null);
        }
    }
}
