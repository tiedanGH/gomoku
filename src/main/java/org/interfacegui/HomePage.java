package org.interfacegui;

import javafx.scene.layout.Pane;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.beans.property.StringProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.scene.paint.Color;

public class HomePage {

    private Pane page;                // 主页面 Pane
    private Pane pageContainer;       // 外层容器（可在 App 中放入 Scene）

    // 玩家选择按钮
    private Button blackPlayer;
    private Button blackAI;
    private Button whitePlayer;
    private Button whiteAI;

    // AI 难度（黑方 & 白方）
    private Button blackEasy, blackMedium, blackHard;
    private Button whiteEasy, whiteMedium, whiteHard;

    // Board Size（只剩 19）
    private Button nineteenButton;

    // Start / Learn
    private Button startButton;
    private Button learnButton;

    // 错误信息
    private Label errorMessage = new Label();

    private StringProperty ruleType = new SimpleStringProperty("gomoku");
    private Rules rulesInstance = null;

    public HomePage() {

        page = new Pane();
        page.setPrefSize(800, 800);   // 你可以改成窗口大小

        // ========== 颜色样式 ==========
        String selectedColor = "-fx-text-fill: #FFFFFF;";
        String deselectedColor = "-fx-text-fill: #000000;";
        String selectedBackground = "-fx-background-color: #000000;";
        String deselectedBackground = "-fx-background-color: #FFFFFF;";

        String selectedStyle = selectedBackground + selectedColor;
        String deselectedStyle = deselectedBackground + deselectedColor;

        // ========== Start 按钮 ==========
        startButton = new Button("Start");
        startButton.setStyle(selectedStyle);
        startButton.setLayoutX(350);
        startButton.setLayoutY(80);
        startButton.setPrefWidth(100);
        page.getChildren().add(startButton);

        // ========== Learn 按钮 ==========
        learnButton = new Button("Learn");
        learnButton.setStyle(deselectedStyle);
        learnButton.setLayoutX(350);
        learnButton.setLayoutY(140);
        learnButton.setPrefWidth(100);
        page.getChildren().add(learnButton);

        // ========== 黑方按钮 ==========
        blackPlayer = new Button("Black Human");
        blackAI = new Button("Black AI");

        blackPlayer.setStyle(selectedStyle);
        blackAI.setStyle(deselectedStyle);

        blackPlayer.setLayoutX(300);
        blackPlayer.setLayoutY(230);

        blackAI.setLayoutX(420);
        blackAI.setLayoutY(230);

        page.getChildren().addAll(blackPlayer, blackAI);

        // ========== 白方按钮 ==========
        whitePlayer = new Button("White Human");
        whiteAI = new Button("White AI");

        whitePlayer.setStyle(selectedStyle);
        whiteAI.setStyle(deselectedStyle);

        whitePlayer.setLayoutX(300);
        whitePlayer.setLayoutY(290);

        whiteAI.setLayoutX(420);
        whiteAI.setLayoutY(290);

        page.getChildren().addAll(whitePlayer, whiteAI);

        // ========== AI 难度（黑方） ==========
        blackEasy = new Button("Easy");
        blackMedium = new Button("Medium");
        blackHard  = new Button("Hard");

        blackEasy.setLayoutX(260);
        blackMedium.setLayoutX(350);
        blackHard.setLayoutX(450);

        blackEasy.setLayoutY(350);
        blackMedium.setLayoutY(350);
        blackHard.setLayoutY(350);

        blackEasy.setVisible(false);
        blackMedium.setVisible(false);
        blackHard.setVisible(false);

        page.getChildren().addAll(blackEasy, blackMedium, blackHard);

        // ========== AI 难度（白方） ==========
        whiteEasy   = new Button("Easy");
        whiteMedium = new Button("Medium");
        whiteHard   = new Button("Hard");

        whiteEasy.setPrefWidth(80);
        whiteMedium.setPrefWidth(80);
        whiteHard.setPrefWidth(80);

        // 默认隐藏
        whiteEasy.setVisible(false);
        whiteMedium.setVisible(false);
        whiteHard.setVisible(false);

        // 绝对布局坐标（可自己微调）
        whiteEasy.setLayoutX(520);
        whiteEasy.setLayoutY(420);

        whiteMedium.setLayoutX(610);
        whiteMedium.setLayoutY(420);

        whiteHard.setLayoutX(700);
        whiteHard.setLayoutY(420);

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

    public Button getLearnOrViewButton() {
        return learnButton;
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

    public void setRulesInstance(Rules r) {
        rulesInstance = r;
    }
}
