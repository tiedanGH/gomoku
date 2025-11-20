package org.interfacegui;

import javafx.scene.layout.Pane;
import javafx.scene.control.Button;

/**
 * Home（主菜单逻辑控制）
 * — 负责处理主菜单点击逻辑
 * — 记录玩家设置（AI、Human、难度）
 */
public class Home {

    // 玩家类型：0 = human, 1 = AI
    private int whitePlayerType = 0;
    private int blackPlayerType = 0;

    // 当前规则（固定为 Gomoku）

    private String rule = "Gomoku";

    // AI难度：1=hard, 2=medium, 3=easy
    private int level = 3;

    // UI页面
    private final HomePage homePage = new HomePage();
    private int boardSize = 19;  //棋盘是19格子
    private Rules.GameMode gameMode = Rules.GameMode.PLAYING;
    private String errorMsg;

    public Home() {
    	 String selectedColor = "-fx-text-fill: #000000;";
         String deselectedColor = "-fx-text-fill: #000000;";
         String selectedBackgroundColor = "-fx-background-color: "
         	    + "linear-gradient(#E3C799, #C49A6C, #8B5A2B);" 
         	    + "-fx-background-radius: 10; "
         	    + "-fx-border-color: #B19776; "          
         	    + "-fx-border-width: 2; "
         	    + "-fx-border-radius: 10; "
         	    + "-fx-padding: 10 20 10 20; "
         	    + "-fx-font-size: 22px; "               
         	    + "-fx-font-weight: bold; ";
         String deselectedBackgroundColor = "-fx-background-color: "
         	    + "linear-gradient(#E3C799, #C49A6C, #8B5A2B);" 
         	    + "-fx-background-radius: 10; "
         	    + "-fx-border-color: #B19776; "          
         	    + "-fx-border-width: 2; "
         	    + "-fx-border-radius: 10; "
         	    + "-fx-padding: 10 20 10 20; "
         	    + "-fx-font-size: 22px; "               
         	    + "-fx-font-weight: bold; ";

        String selectedStyle = selectedBackgroundColor + selectedColor;
        String deselectedStyle = deselectedBackgroundColor + deselectedColor;



        // ===========================================================
        //                   黑方玩家选择（AI / Player）
        // ===========================================================

        homePage.getBlackIaTypeButton().setOnAction(e -> {
            blackPlayerType = 1;

            // 显示 AI 难度按钮
            homePage.getBlackEasyButton().setVisible(true);
            homePage.getBlackMediumButton().setVisible(true);
            homePage.getBlackHardButton().setVisible(true);

            homePage.getBlackIaTypeButton().setStyle(selectedStyle);
            homePage.getBlackHumanTypeButton().setStyle(deselectedStyle);
        });

        homePage.getBlackHumanTypeButton().setOnAction(e -> {
            blackPlayerType = 0;

            // 隐藏难度按钮
            homePage.getBlackEasyButton().setVisible(false);
            homePage.getBlackMediumButton().setVisible(false);
            homePage.getBlackHardButton().setVisible(false);

            homePage.getBlackHumanTypeButton().setStyle(selectedStyle);
            homePage.getBlackIaTypeButton().setStyle(deselectedStyle);
        });

        // ===========================================================
        //                   白方玩家选择（AI / Human）
        // ===========================================================

        homePage.getWhiteIaTypeButton().setOnAction(e -> {
            whitePlayerType = 1;

            homePage.getWhiteEasyButton().setVisible(true);
            homePage.getWhiteMediumButton().setVisible(true);
            homePage.getWhiteHardButton().setVisible(true);

            homePage.getWhiteIaTypeButton().setStyle(selectedStyle);
            homePage.getWhiteHumanTypeButton().setStyle(deselectedStyle);
        });

        homePage.getWhiteHumanTypeButton().setOnAction(e -> {
            whitePlayerType = 0;

            homePage.getWhiteEasyButton().setVisible(false);
            homePage.getWhiteMediumButton().setVisible(false);
            homePage.getWhiteHardButton().setVisible(false);

            homePage.getWhiteHumanTypeButton().setStyle(selectedStyle);
            homePage.getWhiteIaTypeButton().setStyle(deselectedStyle);
        });


        // ===========================================================
        //                     AI 难度选择（同步黑白双方）
        // ===========================================================

        homePage.getWhiteEasyButton().setOnAction(e -> {
            level = 3;
            homePage.getWhiteEasyButton().setStyle(selectedStyle);
            homePage.getBlackEasyButton().setStyle(selectedStyle);

            homePage.getWhiteMediumButton().setStyle(deselectedStyle);
            homePage.getBlackMediumButton().setStyle(deselectedStyle);
            homePage.getWhiteHardButton().setStyle(deselectedStyle);
            homePage.getBlackHardButton().setStyle(deselectedStyle);
        });

        homePage.getWhiteMediumButton().setOnAction(e -> {
            level = 2;
            homePage.getWhiteMediumButton().setStyle(selectedStyle);
            homePage.getBlackMediumButton().setStyle(selectedStyle);

            homePage.getWhiteEasyButton().setStyle(deselectedStyle);
            homePage.getBlackEasyButton().setStyle(deselectedStyle);
            homePage.getWhiteHardButton().setStyle(deselectedStyle);
            homePage.getBlackHardButton().setStyle(deselectedStyle);
        });

        homePage.getWhiteHardButton().setOnAction(e -> {
            level = 1;
            homePage.getWhiteHardButton().setStyle(selectedStyle);
            homePage.getBlackHardButton().setStyle(selectedStyle);

            homePage.getWhiteMediumButton().setStyle(deselectedStyle);
            homePage.getBlackMediumButton().setStyle(deselectedStyle);
            homePage.getWhiteEasyButton().setStyle(deselectedStyle);
            homePage.getBlackEasyButton().setStyle(deselectedStyle);
        });

        // 黑方难度按钮触发白方的（同步）
        homePage.getBlackEasyButton().setOnAction(e -> homePage.getWhiteEasyButton().fire());
        homePage.getBlackMediumButton().setOnAction(e -> homePage.getWhiteMediumButton().fire());
        homePage.getBlackHardButton().setOnAction(e -> homePage.getWhiteHardButton().fire());

        // ===========================================================
        //                 BoardSize（你只有一个按钮）
        // ===========================================================
        homePage.get19Button().setOnAction(e -> boardSize = 19);

        // ===========================================================
        //                       Start 按钮
        // ===========================================================
        homePage.getValidationButton().setOnAction(e -> {

        });
    }
    // ===========================================================
    //                        Getter
    // ===========================================================

    public String getRules() { return rule; }

    public int getWhitePlayerType() { return whitePlayerType; }
    public int getBlackPlayerType() { return blackPlayerType; }

    public int getBoardSize() { return boardSize; }
    public int getLevel() { return level; }

    public Pane getHomePage() { return homePage.getHomePage(); }

    public Button getValidationButton() { return homePage.getValidationButton(); }
    public Button getLearnOrViewButton() { return homePage.getLearnOrViewButton(); }

    public Rules getRuleInstance() { return homePage.getRuleInstance(); }
    public void setRulesInstance(Rules r) { homePage.setRulesInstance(r); }

    public String getErrorMsg() { return errorMsg; }
    public void displayErrorMsg() { homePage.set_error(errorMsg); }

    public void setGameMode(Rules.GameMode gm) { gameMode = gm; }
    public Rules.GameMode getGameMode() { return gameMode; }

}
