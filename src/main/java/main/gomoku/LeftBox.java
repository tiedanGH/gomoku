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

    // 用于显示图片的 ImageView（可缩放）
    private final ImageView boxImageView;
    // 包裹图片的容器，用于设置边框/效果与位置偏移
    private final StackPane boxContainer;

    public LeftBox(int y, int x) {
        sizeX = x;
        sizeY = y;

        leftBox = new VBox();
        leftBox.setPrefSize(x, y);
        leftBox.setMinWidth(Region.USE_PREF_SIZE);
        leftBox.setMinHeight(Region.USE_PREF_SIZE);

        // 初始化图片视图并加入到容器（放置在中间偏下一点）
        boxImageView = new ImageView(blackBox);
        boxImageView.setPreserveRatio(true);
        // 初始宽度基于面板宽度的比例，后续 updateLeftSize 会调整
        boxImageView.setFitWidth(Math.min(250, sizeX * 0.95));

        boxContainer = new StackPane(boxImageView);
        boxContainer.setAlignment(Pos.CENTER);

        // 将图片容器加入主容器，并允许垂直伸展以便居中
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
     * 根据当前行动玩家来设置左侧盒子的高亮状态。
     * 如果 currentPlayer 等于黑方（PLAYER_BLACK），为图片添加高亮（光晕）；否则移除高亮。
     *
     * @param currentPlayer 当前行动玩家标识（使用类中的常量 PLAYER_BLACK / PLAYER_WHITE）
     */
    public void highlightForPlayer(int currentPlayer) {
        if (boxContainer == null) return;
        if (currentPlayer == 0) { // Assuming 0 represents BLACK
            // 添加金色光晕作为高亮提示
            DropShadow glow = new DropShadow();
            glow.setColor(Color.web("#FFD700"));
            glow.setRadius(60);
            boxContainer.setEffect(glow);
        } else {
            // 非当前玩家，移除高亮
            boxContainer.setEffect(null);
        }
    }
}
