package org.interfacegui;

import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.text.Text;
import java.util.ArrayList;
import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.beans.property.StringProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.scene.paint.Color;

/**
 * Home Page（主菜单界面）
 * 只保留 Gomoku
 * 保留：SGF 加载、玩家类型选择、AI 难度、棋盘大小
 */
public class HomePage {

    private Pane page;
    private Pane pageContainer;

    private Button load_sgf = new Button("load sgf");

    // 只剩一个规则：Gomoku
    private Button gomoku;

    // 玩家类型选择
    private Button white_human;
    private Button white_ia;
    private Button black_human;
    private Button black_ia;

    private Pane black_player;
    private Pane white_player;

    // 围棋专属字段（现在全部不显示）
    private TextField komi_field;
    private TextField handicap_field;

    private Label boardSizeLabel = new Label("board size");
    private Button nineSize = new Button("9");
    private Button thirteenSize = new Button("13");
    private Button nineteenSize = new Button("19");
    private VBox boardSizeBox = new VBox(10);
    private HBox boardSizeButtonBox = new HBox(10);

    private Label fileName = new Label("");
    private HBox fileBox = new HBox(10);
    private Label reset = new Label("reset");

    private Button validation = new Button("Start");
    private Button learnOrView = new Button("Learn");
    private HBox LaunchButtons = new HBox();

    private ArrayList<Map> sgfMap;
    private boolean sgfFile = false;

    private Label error_file = new Label();
    private Label error_message = new Label();

    private StringProperty rule_type = new SimpleStringProperty("gomoku");
    private Rules rules_instance = null;

    // AI 难度按钮
    private HBox blackBox = new HBox();
    private Button blackIaEasy = new Button("easy");
    private Button blackIaMedium = new Button("medium");
    private Button blackIaHard = new Button("hard");

    private HBox whiteBox = new HBox();
    private Button whiteIaEasy = new Button("easy");
    private Button whiteIaMedium = new Button("medium");
    private Button whiteIaHard = new Button("hard");

    private Button blackCustom = new Button("custom");
    private Button blackBackToButton = new Button("back");

    private Button whiteCustom = new Button("custom");
    private Button whiteBackToButton = new Button("back");

    public HomePage() {

        // AI 难度区域（默认隐藏）
        blackBox.getChildren().addAll(blackIaEasy, blackIaMedium, blackIaHard);
        whiteBox.getChildren().addAll(whiteIaEasy, whiteIaMedium, whiteIaHard);
        blackBox.setManaged(false);
        blackBox.setVisible(false);
        whiteBox.setManaged(false);
        whiteBox.setVisible(false);

        // BOARD SIZE 区域
        boardSizeBox.getChildren().addAll(boardSizeLabel, boardSizeButtonBox);
        boardSizeButtonBox.getChildren().addAll(nineSize, thirteenSize, nineteenSize);
        boardSizeBox.setVisible(false);
        boardSizeBox.setManaged(false);

        // Start / Learn
        LaunchButtons.getChildren().addAll(validation, learnOrView);

        // SGF 文件显示区域
        fileBox.getChildren().addAll(fileName, reset);
        fileBox.setManaged(false);
        fileBox.setVisible(false);

        String selectedColor = "-fx-text-fill: #FFFFFF;";
        String deselectedColor = "-fx-text-fill: #000000;";
        String selectedBackgroundColor = "-fx-background-color: #000000;";
        String deselectedBackgroundColor = "-fx-background-color: #FFFFFF;";

        String deselectedStyle = deselectedBackgroundColor + deselectedColor;
        String selectedStyle = selectedBackgroundColor + selectedColor;

        reset.setStyle(selectedColor);
        fileName.setStyle(selectedColor);

        // 默认 board size = 19
        nineteenSize.setStyle(selectedStyle);
        thirteenSize.setStyle(deselectedStyle);
        nineSize.setStyle(deselectedStyle);

        // —— 保留唯一规则：GOMOKU ——
        gomoku = new Button("Gomoku");
        gomoku.setStyle(selectedStyle);

        HBox game_button = new HBox(10, gomoku);

        // 玩家类型选择
        white_player = new VBox(5);
        black_player = new VBox(5);

        black_human = new Button("black human");
        black_ia = new Button("black ia");
        black_human.setStyle(selectedStyle);
        black_ia.setStyle(deselectedStyle);

        VBox black_info = new VBox(5, new HBox(5, black_human, black_ia, blackBox));
        black_player.getChildren().addAll(black_info);

        white_human = new Button("white human");
        white_ia = new Button("white ia");
        white_human.setStyle(selectedStyle);
        white_ia.setStyle(deselectedStyle);

        VBox white_info = new VBox(5, new HBox(5, white_human, white_ia, whiteBox));
        white_player.getChildren().addAll(white_info);

        // Komi / Handicap（不显示）
        komi_field = new TextField();
        handicap_field = new TextField();
        komi_field.setManaged(false);
        komi_field.setVisible(false);
        handicap_field.setManaged(false);
        handicap_field.setVisible(false);

        // 错误提示
        error_file.setVisible(false);
        error_file.setManaged(false);
        error_message.setVisible(false);
        error_message.setManaged(false);
        error_file.setTextFill(Color.RED);
        error_message.setTextFill(Color.RED);

        // 主界面布局
        page = new VBox(10);
        ((VBox) page).getChildren().addAll(
                error_message,
                error_file,
                fileBox,
                load_sgf,
                game_button,
                black_player,
                white_player,
                boardSizeBox,
                LaunchButtons
        );

        pageContainer = new StackPane();
        pageContainer.getChildren().add(page);

        reset.setOnMouseClicked(e -> {
            deleteFile();
            learnOrView.setManaged(true);
            learnOrView.setVisible(true);
        });
    }

    // ============= Getter 区域 ==============

    Pane getHomePage() { return pageContainer; }
    Button getLoadSgf() { return load_sgf; }

    Button getGomokuButton() { return gomoku; }

    Button getValidationButton() { return validation; }
    Button getLearnOrViewButton() { return learnOrView; }

    Button getWhiteIaTypeButton() { return white_ia; }
    Button getWhiteHumanTypeButton() { return white_human; }
    Button getBlackIaTypeButton() { return black_ia; }
    Button getBlackHumanTypeButton() { return black_human; }

    public VBox getBoardSizeBox() { return boardSizeBox; }
    public Button get9Button() { return nineSize; }
    public Button get13Button() { return thirteenSize; }
    public Button get19Button() { return nineteenSize; }

    public void addFileBox(VBox scrollPane) {
        ((Pane) pageContainer).getChildren().add(scrollPane);
        scrollPane.toFront();
    }

    public void set_error(String msg) {
        error_message.setText(msg);
        error_message.setVisible(true);
        error_message.setManaged(true);
    }

    public void removeFileBox() {
        ObservableList<Node> children = pageContainer.getChildren();
        children.remove(children.size() - 1);

        if (!SGF.parseFile()) {
            error_file.setText(SGF.get_file_name() + " is not a valid sgf file");
            error_file.setManaged(true);
            error_file.setVisible(true);
            return;
        }

        error_file.setManaged(false);
        error_file.setVisible(false);

        sgfMap = SGF.get_game_moves();
        load_sgf.setManaged(false);
        load_sgf.setVisible(false);

        fileName.setText(SGF.get_file_name());
        fileBox.setManaged(true);
        fileBox.setVisible(true);

        learnOrView.setText("view SGF");
        sgfFile = true;
        rule_type.set("gomoku");  // 统一规则
        rules_instance = SGF.getRuleInstance();

        learnOrView.setManaged(false);
        learnOrView.setVisible(false);
    }

    public void closeFileBox() {
        ObservableList<Node> children = pageContainer.getChildren();
        children.remove(children.size() - 1);

        error_file.setManaged(false);
        error_file.setVisible(false);
        load_sgf.setManaged(true);
        load_sgf.setVisible(true);
        fileBox.setManaged(false);
        fileBox.setVisible(false);
        learnOrView.setText("learn");
    }

    private void deleteFile() {
        load_sgf.setManaged(true);
        load_sgf.setVisible(true);
        fileName.setText("");
        fileBox.setManaged(false);
        fileBox.setVisible(false);
        learnOrView.setText("learn");
        sgfFile = false;
        rule_type.set("gomoku");
    }

    public StringProperty getStringRule() { return rule_type; }
    public boolean is_sgf() { return sgfFile; }

    public ArrayList<Map> getSgfMap() { return sgfMap; }
    public void setSgfMap(ArrayList<Map> map) { sgfMap = map; }

    public Rules getRuleInstance() { return rules_instance; }
    public void setRulesInstance(Rules r) { rules_instance = r; }

    // —— AI 区域 Getter ——
    public HBox getBlackBox() { return blackBox; }
    public HBox getWhiteBox() { return whiteBox; }

    public Button getBlackEasyButton() { return blackIaEasy; }
    public Button getBlackMediumButton() { return blackIaMedium; }
    public Button getBlackHardButton() { return blackIaHard; }

    public Button getWhiteEasyButton() { return whiteIaEasy; }
    public Button getWhiteMediumButton() { return whiteIaMedium; }
    public Button getWhiteHardButton() { return whiteIaHard; }

    public Button getBlackBackButton() { return blackBackToButton; }
    public Button getWhiteBackButton() { return whiteBackToButton; }
    public Button getBlackCustom() { return blackCustom; }
    public Button getWhiteCustom() { return whiteCustom; }

}
