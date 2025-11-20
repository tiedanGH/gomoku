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
    private int boardSize = 19;  // 棋盘是19格子
    private String errorMsg;

    public Home() {

        // ======== 黑方玩家选择（AI / Player） ========

        homePage.getBlackIaTypeButton().setOnAction(e -> {
            blackPlayerType = 1;

            // 变色
            homePage.resetGroup(homePage.getBlackHumanTypeButton(), homePage.getBlackIaTypeButton());
            homePage.applySelected(homePage.getBlackIaTypeButton());

            // 显示黑方 AI 难度按钮
            homePage.getBlackEasyButton().setVisible(true);
            homePage.getBlackMediumButton().setVisible(true);
            homePage.getBlackHardButton().setVisible(true);

        });

        homePage.getBlackHumanTypeButton().setOnAction(e -> {
            blackPlayerType = 0;

            // 变色
            homePage.resetGroup(homePage.getBlackHumanTypeButton(), homePage.getBlackIaTypeButton());
            homePage.applySelected(homePage.getBlackHumanTypeButton());

            // 隐藏黑方难度按钮
            homePage.getBlackEasyButton().setVisible(false);
            homePage.getBlackMediumButton().setVisible(false);
            homePage.getBlackHardButton().setVisible(false);
        });

        // ======== 白方玩家选择（AI / Human） ========

        homePage.getWhiteIaTypeButton().setOnAction(e -> {
            whitePlayerType = 1;

            //变色
            homePage.resetGroup(homePage.getWhiteHumanTypeButton(), homePage.getWhiteIaTypeButton());
            homePage.applySelected(homePage.getWhiteIaTypeButton());

            // 显示白方 AI 难度按钮
            homePage.getWhiteEasyButton().setVisible(true);
            homePage.getWhiteMediumButton().setVisible(true);
            homePage.getWhiteHardButton().setVisible(true);
        });

        homePage.getWhiteHumanTypeButton().setOnAction(e -> {
            whitePlayerType = 0;

            //变色
            homePage.resetGroup(homePage.getWhiteHumanTypeButton(), homePage.getWhiteIaTypeButton());
            homePage.applySelected(homePage.getWhiteHumanTypeButton());

            // 隐藏白方 AI 难度按钮
            homePage.getWhiteEasyButton().setVisible(false);
            homePage.getWhiteMediumButton().setVisible(false);
            homePage.getWhiteHardButton().setVisible(false);
        });

        // ======== AI 难度选择（以白方按钮为主，同步黑白） ========

        homePage.getWhiteEasyButton().setOnAction(e -> {
            level = 3;
            // 更新难度和同步

            //变色
            homePage.resetGroup(homePage.getBlackEasyButton(), homePage.getBlackMediumButton(), homePage.getBlackHardButton());
            homePage.applySelected(homePage.getBlackEasyButton());

            homePage.resetGroup(homePage.getWhiteEasyButton(), homePage.getWhiteMediumButton(), homePage.getWhiteHardButton());
            homePage.applySelected(homePage.getWhiteEasyButton());
        });

        homePage.getWhiteMediumButton().setOnAction(e -> {
            level = 2;

            //变色
            homePage.resetGroup(homePage.getBlackEasyButton(), homePage.getBlackMediumButton(), homePage.getBlackHardButton());
            homePage.applySelected(homePage.getBlackMediumButton());

            homePage.resetGroup(homePage.getWhiteEasyButton(), homePage.getWhiteMediumButton(), homePage.getWhiteHardButton());
            homePage.applySelected(homePage.getWhiteMediumButton());
        });


        homePage.getWhiteHardButton().setOnAction(e -> {
            level = 1;

            //变色
            homePage.resetGroup(homePage.getBlackEasyButton(), homePage.getBlackMediumButton(), homePage.getBlackHardButton());
            homePage.applySelected(homePage.getBlackHardButton());

            homePage.resetGroup(homePage.getWhiteEasyButton(), homePage.getWhiteMediumButton(), homePage.getWhiteHardButton());
            homePage.applySelected(homePage.getWhiteHardButton());
        });

        // 黑方难度按钮触发白方按钮（继续复用原来的“同步”逻辑）
        homePage.getBlackEasyButton().setOnAction(e -> homePage.getWhiteEasyButton().fire());
        homePage.getBlackMediumButton().setOnAction(e -> homePage.getWhiteMediumButton().fire());
        homePage.getBlackHardButton().setOnAction(e -> homePage.getWhiteHardButton().fire());

        // ======== BoardSize ========
        homePage.get19Button().setOnAction(e -> boardSize = 19);

        // Start 按钮
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

    public Rules getRuleInstance() { return homePage.getRuleInstance(); }

    public String getErrorMsg() { return errorMsg; }
    public void displayErrorMsg() { homePage.set_error(errorMsg); }
}

