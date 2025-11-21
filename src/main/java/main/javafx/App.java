package main.javafx;

import main.gomoku.Gomoku;
import main.gomoku.Home;
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
    private Pane homeRoot = new Pane();
    private Home homePage = new Home();

    private ImageView background;
    private ImageView title;

    private void openBackground() {
        File f1 = new File("./img/background.png");
        File f2 = new File("./img/title.png");

        Image img1 = new Image(f1.toURI().toString(), false);
        Image img2 = new Image(f2.toURI().toString(), false);

        background = new ImageView(img1);
        title = new ImageView(img2);

        background.setFitWidth(homeRoot.getWidth());
        background.setFitHeight(homeRoot.getHeight());
        background.setPreserveRatio(false);

        title.setFitWidth(homeRoot.getWidth());
        title.setFitHeight(homeRoot.getHeight());
        title.setPreserveRatio(true);
    }

    /**
     * 主菜单按钮事件
     */
    public void setHomeEvent() {
        homePage.getStartButton().setOnMouseClicked(event -> {
            double sceneX = 830;
            double sceneY = 700;

            gomoku = new Gomoku((int) sceneY, (int) sceneX, homePage);
            Pane boardRoot = new Pane();
            board = new Scene(boardRoot, sceneX, sceneY);
            setBoardEvent();
            boardRoot.getChildren().add(gomoku.getGameDisplay());

            switchScene(board);
            stage.setResizable(true);
        });
    }

    private void setNewHome() {
        homeRoot = new Pane();
        home = new Scene(homeRoot, 962, 550);

        homePage = new Home();
        openBackground();

        background.setMouseTransparent(true);
        title.setMouseTransparent(true);

        homeRoot.getChildren().add(background);
        homeRoot.getChildren().add(title);

        setHomeEvent();
        homeRoot.getChildren().add(homePage.getHomePage());

        stage.setResizable(false);
        homePage.getHomePage().setTranslateY(160);

        switchScene(home);
    }

    private void setBoardEvent() {
        gomoku.getBackHomeButton().setOnMouseClicked(event -> {
            gomoku.endAI();
            setNewHome();
        });
        gomoku.getReplayButton().setOnMouseClicked(event -> gomoku.resetGame());
        board.widthProperty().addListener((obs, oldV, newV) ->
                gomoku.updateGameDisplay((int) board.getHeight(), newV.intValue())
        );
        board.heightProperty().addListener((obs, oldV, newV) ->
                gomoku.updateGameDisplay(newV.intValue(), (int) board.getWidth())
        );
    }

    @Override
    public void start(Stage primaryStage) {

        stage = primaryStage;
        stage.setTitle("Gomoku");

        home = new Scene(homeRoot, 962, 550);
        openBackground();

        background.setMouseTransparent(true);
        title.setMouseTransparent(true);
        homeRoot.getChildren().add(background);
        homeRoot.getChildren().add(title);

        setHomeEvent();
        homeRoot.getChildren().add(homePage.getHomePage());
        homePage.getHomePage().setTranslateY(160);

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
