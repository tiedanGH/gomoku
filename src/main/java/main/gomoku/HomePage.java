package main.gomoku;

import javafx.scene.layout.Pane;
import javafx.scene.control.Button;

public class HomePage {

    private final Pane pageContainer;       // Outer container (placed into the Scene by the App)

    // Player selection buttons
    private final Button blackPlayer;
    private final Button blackAI;
    private final Button whitePlayer;
    private final Button whiteAI;

    // AI difficulty buttons (Black & White)
    private final Button blackEasy, blackMedium, blackHard;
    private final Button whiteEasy, whiteMedium, whiteHard;

    // Start / Learn buttons
    private final Button startButton;

    // Base style ensures padding, border, and font stay the same so color switching won't change button size.
    private static final String BASE_STYLE =
            "-fx-background-radius: 10; "
            + "-fx-border-color: #B19776; "
            + "-fx-border-width: 2; "
            + "-fx-border-radius: 10; "
            + "-fx-padding: 10 20 10 20; "
            + "-fx-font-size: 16px; "
            + "-fx-font-weight: bold; ";

    public HomePage() {
        // Main screen Pane
        Pane page = new Pane();
        page.setPrefSize(800, 800);   // You can set this to your window size

        // ========== Color styles ==========
        // Use applySelected/applyDefault below to control colors while keeping BASE_STYLE fixed.

        // ========== Start Button ==========
        startButton = new Button("Start");
        applyDefault(startButton);
        startButton.setLayoutX(650);
        startButton.setLayoutY(80);
        startButton.setPrefWidth(100);
        page.getChildren().add(startButton);

        // ========== Black Player Buttons ==========
        blackPlayer = new Button("Black Player");
        blackAI = new Button("Black AI");

        applyDefault(blackPlayer);
        applyDefault(blackAI);

        // Black side selection buttons
        blackPlayer.setLayoutX(500);
        blackPlayer.setLayoutY(170);

        blackAI.setLayoutX(700);
        blackAI.setLayoutY(170);

        blackPlayer.setPrefWidth(180);
        blackAI.setPrefWidth(180);

        page.getChildren().addAll(blackPlayer, blackAI);

        // ========== White Player Buttons ==========
        whitePlayer = new Button("White Player");
        whiteAI = new Button("White AI");

        applyDefault(whitePlayer);
        applyDefault(whiteAI);

        // White side selection buttons
        whitePlayer.setLayoutX(500);
        whitePlayer.setLayoutY(220);

        whiteAI.setLayoutX(700);
        whiteAI.setLayoutY(220);

        whitePlayer.setPrefWidth(180);
        whiteAI.setPrefWidth(180);

        page.getChildren().addAll(whitePlayer, whiteAI);

        // ========== AI Difficulty Buttons (Black) ==========
        blackEasy = new Button("Easy");
        blackMedium = new Button("Medium");
        blackHard = new Button("Hard");

        applySelected(blackEasy);
        applyDefault(blackMedium);
        applyDefault(blackHard);

        blackEasy.setPrefWidth(100);
        blackMedium.setPrefWidth(140);
        blackHard.setPrefWidth(100);

        // Hidden by default
        blackEasy.setVisible(false);
        blackMedium.setVisible(false);
        blackHard.setVisible(false);

        // Black difficulty button positions
        blackEasy.setLayoutX(520);
        blackMedium.setLayoutX(630);
        blackHard.setLayoutX(780);

        blackEasy.setLayoutY(300);
        blackMedium.setLayoutY(300);
        blackHard.setLayoutY(300);

        page.getChildren().addAll(blackEasy, blackMedium, blackHard);

        // ========== AI Difficulty Buttons (White) ==========
        whiteEasy = new Button("Easy");
        whiteMedium = new Button("Medium");
        whiteHard = new Button("Hard");

        applySelected(whiteEasy);
        applyDefault(whiteMedium);
        applyDefault(whiteHard);

        whiteEasy.setPrefWidth(100);
        whiteMedium.setPrefWidth(140);
        whiteHard.setPrefWidth(100);

        // Hidden by default
        whiteEasy.setVisible(false);
        whiteMedium.setVisible(false);
        whiteHard.setVisible(false);

        // White difficulty button positions
        whiteEasy.setLayoutX(520);
        whiteMedium.setLayoutX(630);
        whiteHard.setLayoutX(780);

        whiteEasy.setLayoutY(300);
        whiteMedium.setLayoutY(300);
        whiteHard.setLayoutY(300);

        page.getChildren().addAll(whiteEasy, whiteMedium, whiteHard);

        // Page container
        pageContainer = new Pane(page);
    }

    // ========= Getters used by Home.java =========

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

    // Selected style (orange-gold highlight)
    public void applySelected(Button btn) {
        btn.setStyle("-fx-background-color: #FFB84D; -fx-text-fill: #000000; " + BASE_STYLE);
    }

    // Default style (gold gradient background)
    public void applyDefault(Button btn) {
        btn.setStyle("-fx-background-color: linear-gradient(#E3C799, #C49A6C, #8B5A2B); -fx-text-fill: #000000; " + BASE_STYLE);
    }

    // Reset a group of buttons to default style
    public void resetButtons(Button... buttons) {
        for (Button b : buttons) {
            applyDefault(b);
        }
    }
}

