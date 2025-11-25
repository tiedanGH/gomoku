package main.gomoku;

import javafx.scene.layout.Pane;
import javafx.scene.control.Button;

/**
 * Home (main menu logic controller)
 * — Handles main menu button interactions
 * — Stores player settings (AI, Human, difficulty level)
 */
public class Home {

    // Player type: 0 = human, 1 = AI
    private int whitePlayerType = 0;
    private int blackPlayerType = 0;

    // Current rules (fixed to Gomoku)
    private final static String rule = "Gomoku";

    // AI difficulty: 1 = hard, 2 = medium, 3 = easy
    private int level = 3;

    // UI page
    private final HomePage homePage = new HomePage();
    private static final int boardSize = 19;  // Board is 19x19

    public Home() {
        // ===========================================================
        //                 Black player selection (AI / Human)
        // ===========================================================

        homePage.getBlackAITypeButton().setOnAction(e -> {
            blackPlayerType = 1;

            // Change highlight color
            homePage.resetButtons(homePage.getBlackHumanTypeButton(), homePage.getBlackAITypeButton());
            homePage.applySelected(homePage.getBlackAITypeButton());

            // Show black AI difficulty buttons
            homePage.getBlackEasyButton().setVisible(true);
            homePage.getBlackMediumButton().setVisible(true);
            homePage.getBlackHardButton().setVisible(true);

        });

        homePage.getBlackHumanTypeButton().setOnAction(e -> {
            blackPlayerType = 0;

            // Change highlight color
            homePage.resetButtons(homePage.getBlackHumanTypeButton(), homePage.getBlackAITypeButton());
            homePage.applySelected(homePage.getBlackHumanTypeButton());

            // Hide black difficulty buttons
            homePage.getBlackEasyButton().setVisible(false);
            homePage.getBlackMediumButton().setVisible(false);
            homePage.getBlackHardButton().setVisible(false);
        });

        // ======== White player selection (AI / Human) ========

        homePage.getWhiteAITypeButton().setOnAction(e -> {
            whitePlayerType = 1;

            // Change highlight color
            homePage.resetButtons(homePage.getWhiteHumanTypeButton(), homePage.getWhiteAITypeButton());
            homePage.applySelected(homePage.getWhiteAITypeButton());

            // Show white AI difficulty buttons
            homePage.getWhiteEasyButton().setVisible(true);
            homePage.getWhiteMediumButton().setVisible(true);
            homePage.getWhiteHardButton().setVisible(true);
        });

        homePage.getWhiteHumanTypeButton().setOnAction(e -> {
            whitePlayerType = 0;

            // Change highlight color
            homePage.resetButtons(homePage.getWhiteHumanTypeButton(), homePage.getWhiteAITypeButton());
            homePage.applySelected(homePage.getWhiteHumanTypeButton());

            // Hide white AI difficulty buttons
            homePage.getWhiteEasyButton().setVisible(false);
            homePage.getWhiteMediumButton().setVisible(false);
            homePage.getWhiteHardButton().setVisible(false);
        });

        // ======== AI difficulty selection (white side drives sync to black) ========

        homePage.getWhiteEasyButton().setOnAction(e -> {
            level = 3;
            // Update difficulty and sync both sides

            // Update black difficulty button highlight
            homePage.resetButtons(homePage.getBlackEasyButton(), homePage.getBlackMediumButton(), homePage.getBlackHardButton());
            homePage.applySelected(homePage.getBlackEasyButton());

            // Update white difficulty button highlight
            homePage.resetButtons(homePage.getWhiteEasyButton(), homePage.getWhiteMediumButton(), homePage.getWhiteHardButton());
            homePage.applySelected(homePage.getWhiteEasyButton());
        });

        homePage.getWhiteMediumButton().setOnAction(e -> {
            level = 2;

            // Update black highlight
            homePage.resetButtons(homePage.getBlackEasyButton(), homePage.getBlackMediumButton(), homePage.getBlackHardButton());
            homePage.applySelected(homePage.getBlackMediumButton());

            // Update white highlight
            homePage.resetButtons(homePage.getWhiteEasyButton(), homePage.getWhiteMediumButton(), homePage.getWhiteHardButton());
            homePage.applySelected(homePage.getWhiteMediumButton());
        });


        homePage.getWhiteHardButton().setOnAction(e -> {
            level = 1;

            // Update black highlight
            homePage.resetButtons(homePage.getBlackEasyButton(), homePage.getBlackMediumButton(), homePage.getBlackHardButton());
            homePage.applySelected(homePage.getBlackHardButton());

            // Update white highlight
            homePage.resetButtons(homePage.getWhiteEasyButton(), homePage.getWhiteMediumButton(), homePage.getWhiteHardButton());
            homePage.applySelected(homePage.getWhiteHardButton());
        });

        // Black difficulty buttons trigger white buttons (reuse sync logic)
        homePage.getBlackEasyButton().setOnAction(e -> homePage.getWhiteEasyButton().fire());
        homePage.getBlackMediumButton().setOnAction(e -> homePage.getWhiteMediumButton().fire());
        homePage.getBlackHardButton().setOnAction(e -> homePage.getWhiteHardButton().fire());
    }

    // ===========================================================
    //                        Getters
    // ===========================================================

    public String getRules() { return rule; }

    public int getWhitePlayerType() { return whitePlayerType; }
    public int getBlackPlayerType() { return blackPlayerType; }

    public int getBoardSize() { return boardSize; }
    public int getLevel() { return level; }

    public Pane getHomePage() { return homePage.getHomePage(); }

    public Button getStartButton() { return homePage.getStartButton(); }
}


