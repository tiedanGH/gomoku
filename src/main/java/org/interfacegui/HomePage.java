package org.interfacegui;

import javafx.scene.layout.Pane;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.beans.property.StringProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.scene.paint.Color;

public class HomePage {

    private final Pane page;                // 主页面 Pane
    private final Pane pageContainer;       // 外层容器（可在 App 中放入 Scene）

    // 玩家选择按钮
    private final Button blackPlayer;
    private final Button blackAI;
    private final Button whitePlayer;
    private final Button whiteAI;

    // AI 难度（黑方 & 白方）
    private final Button blackEasy, blackMedium, blackHard;
    private final Button whiteEasy, whiteMedium, whiteHard;

    // Board Size（只剩 19）
    private final Button nineteenButton;

    // Start / Learn
    private final Button startButton;

    // 错误信息
    private final Label errorMessage = new Label();

    private StringProperty cruleType = new SimpleStringProperty("gomoku");
    private Rules rulesInstance = null;

    public HomePage() {

        page = new Pane();
        page.setPrefSize(800, 800);   // 你可以改成窗口大小

        // ========== 颜色样式 ==========
        String selectedColor = "-fx-text-fill: #000000;";
        String deselectedColor = "-fx-text-fill: #000000;";
        String selectedBackground = "-fx-background-color: "
                + "linear-gradient(#E3C799, #C49A6C, #8B5A2B);"
                + "-fx-background-radius: 10; "
                + "-fx-border-color: #B19776; "
                + "-fx-border-width: 2; "
                + "-fx-border-radius: 10; "
                + "-fx-padding: 10 20 10 20; "
                + "-fx-font-size: 16px; "
                + "-fx-font-weight: bold; ";
        String deselectedBackground = "-fx-background-color: "
                + "linear-gradient(#E3C799, #C49A6C, #8B5A2B);"
                + "-fx-background-radius: 10; "
                + "-fx-border-color: #B19776; "
                + "-fx-border-width: 2; "
                + "-fx-border-radius: 10; "
                + "-fx-padding: 10 20 10 20; "
                + "-fx-font-size: 16px; "
                + "-fx-font-weight: bold; ";

        String selectedStyle = selectedBackground + selectedColor;
        String deselectedStyle = deselectedBackground + deselectedColor;

        // ========== Start 按钮 ==========
        startButton = new Button("Start");
        startButton.setStyle(selectedStyle);
        startButton.setLayoutX(650);
        startButton.setLayoutY(80);
        startButton.setPrefWidth(100);
        page.getChildren().add(startButton);

        // ========== 黑方按钮 ==========
        blackPlayer = new Button("Black Player");
        blackAI = new Button("Black AI");

        blackPlayer.setStyle(selectedStyle);
        blackAI.setStyle(deselectedStyle);

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

        whitePlayer.setStyle(selectedStyle);
        whiteAI.setStyle(deselectedStyle);

        //白子选择按钮
        whitePlayer.setLayoutX(500);
        whitePlayer.setLayoutY(220);

        whiteAI.setLayoutX(700);
        whiteAI.setLayoutY(220);

        whitePlayer.setPrefWidth(180);
        whiteAI.setPrefWidth(180);

        page.getChildren().addAll(whitePlayer, whiteAI);

        // ========== AI 难度（黑方） ==========
        blackEasy = new Button("Easy");
        blackMedium = new Button("Medium");
        blackHard  = new Button("Hard");

        blackEasy.setStyle(selectedStyle);
        blackMedium.setStyle(selectedStyle);
        blackHard.setStyle(selectedStyle);

        blackEasy.setPrefWidth(100);
        blackMedium.setPrefWidth(140);
        blackHard.setPrefWidth(100);

        //黑子难度位置显示
        blackEasy.setLayoutX(520);
        blackMedium.setLayoutX(630);
        blackHard.setLayoutX(780);

        blackEasy.setLayoutY(300);
        blackMedium.setLayoutY(300);
        blackHard.setLayoutY(300);

        blackEasy.setVisible(false);
        blackMedium.setVisible(false);
        blackHard.setVisible(false);

        page.getChildren().addAll(blackEasy, blackMedium, blackHard);

        // ========== AI 难度（白方） ==========
        whiteEasy   = new Button("Easy");
        whiteMedium = new Button("Medium");
        whiteHard   = new Button("Hard");

        whiteEasy.setStyle(deselectedStyle);
        whiteMedium.setStyle(deselectedStyle);
        whiteHard.setStyle(deselectedStyle);

        whiteEasy.setPrefWidth(100);
        whiteMedium.setPrefWidth(140);
        whiteHard.setPrefWidth(100);

        // 默认隐藏
        whiteEasy.setVisible(false);
        whiteMedium.setVisible(false);
        whiteHard.setVisible(false);

        // 白子难度位置
        whiteEasy.setLayoutX(520);
        whiteMedium.setLayoutX(630);
        whiteHard.setLayoutX(780);

        whiteEasy.setLayoutY(300);
        whiteMedium.setLayoutY(300);
        whiteHard.setLayoutY(300);

        page.getChildren().addAll(whiteEasy, whiteMedium, whiteHard);

        // ========== Board Size（19） ==========
        nineteenButton = new Button("19");
        nineteenButton.setStyle(selectedStyle);
        nineteenButton.setLayoutX(370);
        nineteenButton.setLayoutY(460);
        page.getChildren().add(nineteenButton);

        // ========== 错误信息 ==========
        errorMessage.setTextFill(Color.RED);
        errorMessage.setLayoutX(300);
        errorMessage.setLayoutY(520);
        errorMessage.setVisible(false);

        page.getChildren().add(errorMessage);

        // 页面容器
        pageContainer = new Pane(page);
    }

    // ========= Getter，用于 Home.java 调用 =========

    public Pane getHomePage() {
        return pageContainer;
    }

    public Button getValidationButton() {
        return startButton;
    }

    public Button getWhiteHumanTypeButton() {
        return whitePlayer;
    }

    public Button getWhiteIaTypeButton() {
        return whiteAI;
    }

    public Button getBlackHumanTypeButton() {
        return blackPlayer;
    }

    public Button getBlackIaTypeButton() {
        return blackAI;
    }

    public Button getWhiteEasyButton() {
        return whiteEasy;
    }

    public Button getWhiteMediumButton() {
        return whiteMedium;
    }

    public Button getWhiteHardButton() {
        return whiteHard;
    }

    public Button getBlackEasyButton() {
        return blackEasy;
    }

    public Button getBlackMediumButton() {
        return blackMedium;
    }

    public Button getBlackHardButton() {
        return blackHard;
    }

    public Button get19Button() {
        return nineteenButton;
    }

    public void set_error(String msg) {
        errorMessage.setText(msg);
        errorMessage.setVisible(true);
    }

    public Rules getRuleInstance() {
        return rulesInstance;
    }

    // 选中样式（浅红/金色）
    public void applySelected(Button btn) {
        btn.setStyle("-fx-background-color: #DC143C; -fx-text-fill: #000000; "
                + "-fx-background-radius: 10; -fx-font-size: 16px; -fx-font-weight: bold;");
    }

    // 默认样式（原金色背景）
    public void applyDefault(Button btn) {
        btn.setStyle("-fx-background-color: linear-gradient(#E3C799, #C49A6C, #8B5A2B); "
                + "-fx-text-fill: #000000; "
                + "-fx-background-radius: 10; -fx-font-size: 16px; -fx-font-weight: bold;");
    }
    // 将某个按钮组全部恢复默认样式
    public void resetGroup(Button... buttons) {
        for (Button b : buttons) {
            applyDefault(b);
        }
    }
}
