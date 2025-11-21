package main.gomoku;

import javafx.scene.layout.Pane;
import javafx.scene.control.Button;

public class HomePage {

    private final Pane pageContainer;       // 外层容器（可在 App 中放入 Scene）

    // 玩家选择按钮
    private final Button blackPlayer;
    private final Button blackAI;
    private final Button whitePlayer;
    private final Button whiteAI;

    // AI 难度
    private final Button AIEasy, AIMedium, AIHard;

    // Start / Learn
    private final Button startButton;

    // Base style ensures padding, border and font remain the same so switching color won't change size.
    private static final String BASE_STYLE =
            "-fx-background-radius: 10; "
            + "-fx-border-color: #B19776; "
            + "-fx-border-width: 2; "
            + "-fx-border-radius: 10; "
            + "-fx-padding: 10 20 10 20; "
            + "-fx-font-size: 16px; "
            + "-fx-font-weight: bold; ";

    public HomePage() {
        // 主页面 Pane
        Pane page = new Pane();
        page.setPrefSize(800, 800);   // 你可以改成窗口大小

        // ========== 颜色样式 ==========
        // Use applySelected/applyDefault below to set color while keeping BASE_STYLE the same.

        // ========== Start 按钮 ==========
        startButton = new Button("Start");
        applyDefault(startButton);
        startButton.setLayoutX(650);
        startButton.setLayoutY(80);
        startButton.setPrefWidth(100);
        page.getChildren().add(startButton);

        // ========== 黑方按钮 ==========
        blackPlayer = new Button("Black Player");
        blackAI = new Button("Black AI");

        applySelected(blackPlayer);
        applyDefault(blackAI);

        //黑子选择按钮
        blackPlayer.setLayoutX(500);
        blackPlayer.setLayoutY(170);

        blackAI.setLayoutX(700);
        blackAI.setLayoutY(170);

        blackPlayer.setPrefWidth(180);
        blackAI.setPrefWidth(180);

        page.getChildren().addAll(blackPlayer, blackAI);

        // ========== 白方按钮 ==========
        whitePlayer = new Button("White Player");
        whiteAI = new Button("White AI");

        applySelected(whitePlayer);
        applyDefault(whiteAI);

        //白子选择按钮
        whitePlayer.setLayoutX(500);
        whitePlayer.setLayoutY(220);

        whiteAI.setLayoutX(700);
        whiteAI.setLayoutY(220);

        whitePlayer.setPrefWidth(180);
        whiteAI.setPrefWidth(180);

        page.getChildren().addAll(whitePlayer, whiteAI);

        // ========== AI 难度 ==========
        AIEasy = new Button("Easy");
        AIMedium = new Button("Medium");
        AIHard = new Button("Hard");

        applySelected(AIEasy);
        applyDefault(AIMedium);
        applyDefault(AIHard);

        AIEasy.setPrefWidth(100);
        AIMedium.setPrefWidth(140);
        AIHard.setPrefWidth(100);

        // 默认隐藏
        AIEasy.setVisible(false);
        AIMedium.setVisible(false);
        AIHard.setVisible(false);

        // 白子难度位置
        AIEasy.setLayoutX(520);
        AIMedium.setLayoutX(630);
        AIHard.setLayoutX(780);

        AIEasy.setLayoutY(300);
        AIMedium.setLayoutY(300);
        AIHard.setLayoutY(300);

        page.getChildren().addAll(AIEasy, AIMedium, AIHard);

        // 页面容器
        pageContainer = new Pane(page);
    }

    // ========= Getter，用于 Home.java 调用 =========

    public Pane getHomePage() {
        return pageContainer;
    }

    public Button getStartButton() {
        return startButton;
    }

    public Button getWhiteHumanTypeButton() {
        return whitePlayer;
    }

    public Button getWhiteAITypeButton() {
        return whiteAI;
    }

    public Button getBlackHumanTypeButton() {
        return blackPlayer;
    }

    public Button getBlackAITypeButton() {
        return blackAI;
    }

    public Button getWhiteEasyButton() {
        return AIEasy;
    }

    public Button getWhiteMediumButton() {
        return AIMedium;
    }

    public Button getWhiteHardButton() {
        return AIHard;
    }

    // 选中样式（浅红/金色）
    public void applySelected(Button btn) {
        btn.setStyle("-fx-background-color: #DC143C; -fx-text-fill: #000000; " + BASE_STYLE);
    }

    // 默认样式（原金色背景）
    public void applyDefault(Button btn) {
        btn.setStyle("-fx-background-color: linear-gradient(#E3C799, #C49A6C, #8B5A2B); -fx-text-fill: #000000; " + BASE_STYLE);
    }
    // 将某个按钮组全部恢复默认样式
    public void resetButtons(Button... buttons) {
        for (Button b : buttons) {
            applyDefault(b);
        }
    }
}
