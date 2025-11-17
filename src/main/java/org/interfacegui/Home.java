package org.interfacegui;

import javafx.scene.layout.Pane;
import javafx.scene.control.Button;
import java.util.ArrayList;

/**
 * Home（主菜单逻辑控制）
 * — 负责处理 UI 的点击逻辑
 * — 负责记录玩家设置：规则、人类/AI、AI 难度
 * — 负责传递设置给 App.java 或 Game.java
 *
 * 只保留了 Gomoku。
 */
public class Home {

    // ====== 时间系统已经删除，但变量还保留以防其他地方调用 ======
    private int white_time = 300000;
    private int black_time = 300000;

    // 玩家类型：0 = human, 1 = AI
    private int white_player_type = 0;
    private int black_player_type = 0;

    // 当前使用规则 —— 始终为 Gomoku
    private String rule = "Gomoku";

    private float komi = -1;       // Go 用的，现在不使用
    private int handicap = -1;     // Go 用的，现在不使用

    // AI 难度等级：1=Hard, 2=Medium, 3=Easy
    private int level = 3;

    // HomePage 是 UI 控制类
    private HomePage home_page = new HomePage();

    // 棋盘大小（默认由 UI 决定）
    private int boardSize = -1;

    // 游戏模式（用于学习/复盘模式）
    private Rules.GameMode _gameMode = Rules.GameMode.PLAYING;

    private String errorMsg;

    public Home() {

        // ===== UI 配色设置（选中/未选中） =====
        String selectedColor = "-fx-text-fill: #FFFFFF;";
        String deselectedColor = "-fx-text-fill: #000000;";
        String selectedBackgroundColor = "-fx-background-color: #000000;";
        String deselectedBackgroundColor = "-fx-background-color: #FFFFFF;";
        String deselectedStyle = deselectedBackgroundColor + deselectedColor;
        String selectedStyle = selectedBackgroundColor + selectedColor;
        

        // ===========================================================
        //                    玩家选择（AI / Human）
        // ===========================================================

        // ——— 黑方选择 AI ———
        home_page.getBlackIaTypeButton().setOnAction(e -> {
            black_player_type = 1;
            home_page.getBlackBox().setManaged(true);
            home_page.getBlackBox().setVisible(true);

            home_page.getBlackIaTypeButton().setStyle(selectedStyle);
            home_page.getBlackHumanTypeButton().setStyle(deselectedStyle);
        });

        // ——— 黑方选择 Human ———
        home_page.getBlackHumanTypeButton().setOnAction(e -> {
            black_player_type = 0;
            home_page.getBlackBox().setManaged(false);
            home_page.getBlackBox().setVisible(false);

            home_page.getBlackHumanTypeButton().setStyle(selectedStyle);
            home_page.getBlackIaTypeButton().setStyle(deselectedStyle);
        });

        // ——— 白方选择 AI ———
        home_page.getWhiteIaTypeButton().setOnAction(e -> {
            white_player_type = 1;
            home_page.getWhiteBox().setManaged(true);
            home_page.getWhiteBox().setVisible(true);

            home_page.getWhiteIaTypeButton().setStyle(selectedStyle);
            home_page.getWhiteHumanTypeButton().setStyle(deselectedStyle);
        });

        // ——— 白方选择 Human ———
        home_page.getWhiteHumanTypeButton().setOnAction(e -> {
            white_player_type = 0;
            home_page.getWhiteBox().setManaged(false);
            home_page.getWhiteBox().setVisible(false);

            home_page.getWhiteHumanTypeButton().setStyle(selectedStyle);
            home_page.getWhiteIaTypeButton().setStyle(deselectedStyle);
        });

        // ===========================================================
        //                    AI 难度选择（白方）
        // ===========================================================
        home_page.getWhiteEasyButton().setOnAction(e -> {
            level = 3;  // easy
            home_page.getWhiteEasyButton().setStyle(selectedStyle);
            home_page.getBlackEasyButton().setStyle(selectedStyle);

            home_page.getWhiteMediumButton().setStyle(deselectedStyle);
            home_page.getBlackMediumButton().setStyle(deselectedStyle);
            home_page.getWhiteHardButton().setStyle(deselectedStyle);
            home_page.getBlackHardButton().setStyle(deselectedStyle);
        });

        home_page.getWhiteMediumButton().setOnAction(e -> {
            level = 2;  // medium
            home_page.getWhiteMediumButton().setStyle(selectedStyle);
            home_page.getBlackMediumButton().setStyle(selectedStyle);

            home_page.getWhiteEasyButton().setStyle(deselectedStyle);
            home_page.getBlackEasyButton().setStyle(deselectedStyle);
            home_page.getWhiteHardButton().setStyle(deselectedStyle);
            home_page.getBlackHardButton().setStyle(deselectedStyle);
        });

        home_page.getWhiteHardButton().setOnAction(e -> {
            level = 1;  // hard
            home_page.getWhiteHardButton().setStyle(selectedStyle);
            home_page.getBlackHardButton().setStyle(selectedStyle);

            home_page.getWhiteMediumButton().setStyle(deselectedStyle);
            home_page.getBlackMediumButton().setStyle(deselectedStyle);
            home_page.getWhiteEasyButton().setStyle(deselectedStyle);
            home_page.getBlackEasyButton().setStyle(deselectedStyle);
        });

        // 黑方难度按钮 = 同步触发白方按钮
        home_page.getBlackEasyButton().setOnAction(e -> home_page.getWhiteEasyButton().fire());
        home_page.getBlackMediumButton().setOnAction(e -> home_page.getWhiteMediumButton().fire());
        home_page.getBlackHardButton().setOnAction(e -> home_page.getWhiteHardButton().fire());

        // ===========================================================
        //              规则切换（⚠ 现在只剩 Gomoku）
        // ===========================================================

        home_page.getGomokuButton().setOnAction(e -> {
            rule = "Gomoku";  // 唯一规则

            // Gomoku 不需要棋盘大小选择 → 隐藏
            home_page.getBoardSizeBox().setVisible(false);
            home_page.getBoardSizeBox().setManaged(false);

            // UI 高亮
            home_page.getGomokuButton().setStyle(selectedStyle);
        });

        // Go / Pente 的按钮已在 HomePage 删除，因此不需要处理事件了。

        // ===========================================================
        //                  棋盘大小（只给 Go 用）
        //        现在 Gomoku 永远都是 19x19，可忽略
        // ===========================================================
        home_page.get9Button().setOnAction(e -> boardSize = 9);
        home_page.get13Button().setOnAction(e -> boardSize = 13);
        home_page.get19Button().setOnAction(e -> boardSize = 19);

        // 点击 Start 按钮（验证逻辑）
        home_page.getValidationButton().setOnAction(e -> {
            // Gomoku 不需要特殊验证
        });
    }

    // ===========================================================
    //                          Getter 区域
    // ===========================================================
    public int get_white_time() { return white_time; }
    public int get_black_time() { return black_time; }

    public String get_rules() { return rule; }

    public int get_white_player_type() { return white_player_type; }
    public int get_black_player_type() { return black_player_type; }

    public int get_handicap() { return handicap; }
    public float get_komi() { return komi; }

    public int get_board_size() { return boardSize; }
    public int getLevel() { return level; }

    public Pane getHomePage() { return home_page.getHomePage(); }

    public Button getValidationButton() { return home_page.getValidationButton(); }
    public Button getLearnOrViewButton(){ return home_page.getLearnOrViewButton(); }

    public Rules getRuleInstance(){ return home_page.getRuleInstance(); }
    public void setRulesInstance(Rules r){ home_page.setRulesInstance(r); }

    // ===========================================================
    //      一些方法是 App.java 其他地方需要，用来兼容
    // ===========================================================
    public String getErrorMsg() {
        return errorMsg;
    }

    public void setErrorMsg(String msg) {
        errorMsg = msg;
    }

    public void displayErrorMsg() {
        home_page.set_error(errorMsg);
    }

    public void setGameMode(Rules.GameMode gm) {
        _gameMode = gm;
    }

    public Rules.GameMode getGameMode() {
        return _gameMode;
    }
}
