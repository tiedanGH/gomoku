package org.openjfx;

import org.interfacegui.*;
import javafx.application.Application;
import javafx.scene.layout.Pane;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import java.io.File;

public class App extends Application {

    private Gomoku gomoku;
    private Stage stage;


    private Scene board, home;
    private Pane home_root = new Pane();
    private Home home_page = new Home();

    private ImageView background;
    private ImageView title;

    private void openBackground() {
        File f1 = new File("./img/background.png");
        File f2 = new File("./img/title.png");

        Image img1 = new Image(f1.toURI().toString(), false);
        Image img2 = new Image(f2.toURI().toString(), false);

        background = new ImageView(img1);
        title = new ImageView(img2);

        background.setFitWidth(home_root.getWidth());
        background.setFitHeight(home_root.getHeight());
        background.setPreserveRatio(false);

        title.setFitWidth(home_root.getWidth());
        title.setFitHeight(home_root.getHeight());
        title.setPreserveRatio(true);
    }

    /** 主菜单按钮事件 */
    public void set_home_event() {

        home_page.getValidationButton().setOnMouseClicked(event -> {

            if (home_page.getErrorMsg() != null && !home_page.getErrorMsg().isEmpty()) {
                home_page.displayErrorMsg();
                return;
            }

            double scenex = stage.getWidth();
            double sceney = stage.getHeight();

            // 只创建 Gomoku，不再加载 Go/Pente
            gomoku = new Gomoku((int) sceney, (int) scenex, home_page);
            Pane board_root = new Pane();
            board = new Scene(board_root, scenex, sceney);
            set_board_event(board_root);
            board_root.getChildren().add(gomoku.getGameDisplay());

            switchScene(board);
            stage.setResizable(true);
        });

        // 只支持 Gomoku 的教学 SGF
        home_page.getLearnOrViewButton().setOnMouseClicked(event -> {

            // TODO rule
//            SGF.setFile("./", "tuto/Gomoku.sgf");
//            if (!SGF.parseFile()) {
//                home_page.setErrorMsg("Missing Gomoku tutorial SGF.");
//                home_page.displayErrorMsg();
//                return;
//            }

//            ArrayList<Map> sgfMap = SGF.get_game_moves();
//            home_page.setRulesInstance(SGF.getRuleInstance());
            home_page.setGameMode(Rules.GameMode.LEARNING);

            double scenex = stage.getWidth();
            double sceney = stage.getHeight();

            gomoku = new Gomoku((int) sceney, (int) scenex, home_page);
            Pane board_root = new Pane();
            board = new Scene(board_root, scenex, sceney);
            set_board_event(board_root);
            board_root.getChildren().add(gomoku.getGameDisplay());

            switchScene(board);
            stage.setResizable(true);
        });
    }

    private void setNewHome() {
        home_root = new Pane();
        home = new Scene(home_root, 962, 550);

        home_page = new Home();
        openBackground();

        background.setMouseTransparent(true);
        title.setMouseTransparent(true);

        home_root.getChildren().add(background);
        home_root.getChildren().add(title);

        set_home_event();
        home_root.getChildren().add(home_page.getHomePage());

        stage.setResizable(false);
        home_page.getHomePage().setTranslateY(160);

        switchScene(home);
    }

    private void set_board_event(Pane board_root) {
        gomoku.getBackHomeButton().setOnMouseClicked(event -> {
            gomoku.killIa();
            setNewHome();
        });

        gomoku.get_home_button().setOnMouseClicked(event -> setNewHome());

        gomoku.get_replay_button().setOnMouseClicked(event -> gomoku.reset_gomoku());

        board.widthProperty().addListener((obs, oldV, newV) -> {
            gomoku.updateGameDisplay((int) board.getHeight(), newV.intValue());
        });

        board.heightProperty().addListener((obs, oldV, newV) -> {
            gomoku.updateGameDisplay(newV.intValue(), (int) board.getWidth());
        });
        Pane board = gomoku.getGameDisplay();
        board.layoutXProperty().bind(
            board.widthProperty().subtract(board.widthProperty()).divide(2)
        );
        board.layoutYProperty().bind(
            board.heightProperty().subtract(board.heightProperty()).divide(2).subtract(80)
        );

    }

    @Override
    public void start(Stage primaryStage) {

        stage = primaryStage;
        stage.setTitle("Gomoku");

        home = new Scene(home_root, 962, 550);
        openBackground();

        background.setMouseTransparent(true);
        title.setMouseTransparent(true);
        home_root.getChildren().add(background);
        home_root.getChildren().add(title);

        set_home_event();
        home_root.getChildren().add(home_page.getHomePage());
        home_page.getHomePage().setTranslateY(160);

        stage.setResizable(false);
        stage.centerOnScreen();
        stage.setScene(home);
        stage.show();
    }

    public void switchScene(Scene newScene) {
        stage.setScene(newScene);
    }

    public static void main(String[] args) {
        launch(args);
    }
}
