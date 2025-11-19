package org.interfacegui;
import javafx.scene.shape.*;
import java.util.ArrayList;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import org.utils.Point;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.scene.shape.Rectangle;

public class Board {
    private final Pane board;
    private final Circle[][] stones;
    private final Rectangle[][] score;
    private final ArrayList<Circle> dots;
    private final ArrayList<Point> dotsCoordinate = new ArrayList<>();
    private final ArrayList<Line> lines;
    private int size;
    private final int totalLines;
    private int squareSize;
    private int heightMargin;
    private int widthMargin;
    private final ArrayList<Text> txt = new ArrayList<>();
    private Rectangle lastMoveRect = null;

    private void initMargin(int height, int width) {
        int board_size = squareSize * (totalLines - 1);
        heightMargin = (height - board_size) / 2;
        widthMargin = (width - board_size) / 2;
    }

    private void initSquareSize() {
        squareSize = size / totalLines;
    }

    private void updateDots() {
        for (int i = 0; i < dotsCoordinate.size(); i++) {
            Circle dot = dots.get(i);
            dot.setRadius((double) squareSize / 7);
            dot.setCenterX((squareSize * dotsCoordinate.get(i).x) + widthMargin);
            dot.setCenterY((squareSize * dotsCoordinate.get(i).y) + heightMargin);
            dot.setStroke(Color.TRANSPARENT);
            dot.setFill(Color.BLACK);
            dot.setStrokeWidth(1);
        }
    }

    private void createDots() {
        for (Point p : dotsCoordinate) {
            Circle dots = new Circle();
            dots.setRadius((double) squareSize / 7);
            dots.setCenterX((squareSize * p.x) + widthMargin);
            dots.setCenterY((squareSize * p.y) + heightMargin);
            dots.setStroke(Color.TRANSPARENT);
            dots.setFill(Color.BLACK);
            dots.setStrokeWidth(1);
            this.dots.add(dots);
        }
    }

    private void initDots() {
        boolean corner = false;
        boolean line = false;
        if (totalLines >= 13)
            corner = true;
        if (totalLines == 19)
            line = true;
        int centerPosition = totalLines / 2;
        dotsCoordinate.add(new Point(centerPosition, centerPosition));
        if (corner) {
            dotsCoordinate.add(new Point(3, 3));
            dotsCoordinate.add(new Point(3, totalLines - 4));
            dotsCoordinate.add(new Point(totalLines - 4, 3));
            dotsCoordinate.add(new Point(totalLines - 4, totalLines - 4));
        }
        if (line) {
            dotsCoordinate.add(new Point(3, centerPosition));
            dotsCoordinate.add(new Point(centerPosition, 3));
            dotsCoordinate.add(new Point(totalLines -4, centerPosition));
            dotsCoordinate.add(new Point(centerPosition, totalLines -4));
        }
        createDots();
    }

    private void initLines() {
        for (int i = 0; i < totalLines; i++) {
            Line line = new Line(
                widthMargin + (squareSize * i),
                    heightMargin,
                widthMargin + (squareSize * i),
                heightMargin + (squareSize * (totalLines - 1))
            );
            lines.add(line);
        }
        for (int i = 0; i < totalLines; i++) {
            Line line = new Line(
                    widthMargin,
                heightMargin + (squareSize * i),
                widthMargin + (squareSize * (totalLines - 1)),
                heightMargin + (squareSize * i)
            );
            lines.add(line);
        }
    }

    public Board(int height, int width, int lineNum) {
        board = new Pane();
        board.setBackground(new Background(new BackgroundFill(Color.web("#DEB887"), null, null)));
        board.setPrefSize(width, height);
        size = Math.min(width, height);
        totalLines = lineNum;
        stones = new Circle[totalLines][totalLines];
        score = new Rectangle[totalLines][totalLines];
        dots = new ArrayList<>();
        lines = new ArrayList<>();
        initSquareSize();
        initMargin(height, width);
        initLines();
        initDots();
        initStones();
        initScore();
        buildBoard();
    }
    
    public Pane getBoard() {
        return board;
    }

    public void updateLines() {
        for (int i = 0; i < lines.size(); i++) {
            if (i < totalLines) {
                lines.get(i).setStartX(widthMargin + (squareSize * i));
                lines.get(i).setStartY(heightMargin);
                lines.get(i).setEndX(widthMargin + (squareSize * i));
                lines.get(i).setEndY(heightMargin + (squareSize * (totalLines - 1)));
            } else {
                int n = i - totalLines;
                lines.get(i).setStartX(widthMargin);
                lines.get(i).setStartY(heightMargin + (squareSize * n));
                lines.get(i).setEndX(widthMargin + (squareSize * (totalLines - 1)));
                lines.get(i).setEndY(heightMargin + (squareSize * n));
            }
        }
    }

    public void updateBoard(int newY, int newX) {
        board.setPrefSize(newX, newY);
        size = Math.min(newX, newY);
        initSquareSize();
        initMargin(newY, newX);
        updateLines();
        updateStones();
        updateScore();
        updateDots();
    }

    private void updateStones() {
        for (int i = 0; i < stones.length; i++) {
            for (int j = 0; j < stones[i].length; j++) {
                stones[i][j].setRadius((double) squareSize / 2);
                stones[i][j].setCenterX((squareSize * j) + widthMargin);
                stones[i][j].setCenterY((squareSize * i) + heightMargin);
            }
        }
    }

    private void updateScore() {
        double size = squareSize / 5.0;
        for (int i = 0; i < score.length; i++) {
            for (int j = 0; j < score[i].length; j++) {
                Rectangle r = score[i][j];
                setScoreLayout(size, i, j, r);
            }
        }
    }

    private void setScoreLayout(double size, int i, int j, Rectangle r) {
        r.setWidth(size);
        r.setHeight(size);
        double centerX = (squareSize * j) + widthMargin;
        double centerY = (squareSize * i) + heightMargin;
        r.setX(centerX - size / 2.0);
        r.setY(centerY - size / 2.0);
    }

    public void initStones() {
        for (int i = 0; i < stones.length; i++) {
            for (int j = 0; j < stones[i].length; j++) {
                stones[i][j] = new Circle();
                stones[i][j].setRadius((double) squareSize / 2);
                stones[i][j].setCenterX((squareSize * j) + widthMargin);
                stones[i][j].setCenterY((squareSize * i) + heightMargin);
                stones[i][j].setStroke(Color.TRANSPARENT);
                stones[i][j].setFill(Color.BLUE);
                stones[i][j].setStrokeWidth(1);
                stones[i][j].setVisible(false);
            }
        }
    }

    public void initScore() {
        double size = squareSize / 5.0;
        for (int i = 0; i < score.length; i++) {
            for (int j = 0; j < score[i].length; j++) {
                Rectangle r = new Rectangle();
                setScoreLayout(size, i, j, r);
                r.setStroke(Color.BLACK);
                r.setFill(Color.BLUE);
                r.setStrokeWidth(1);
                r.setVisible(false);
                score[i][j] = r;
            }
        }
    }

    private void buildBoard() {
        board.getChildren().addAll(lines);
        board.getChildren().addAll(dots);
        for (Circle[] stone : stones) {
            for (Circle circle : stone) {
                board.getChildren().add(circle);
            }
        }
        for (Rectangle[] rectangles : score) {
            for (Rectangle rectangle : rectangles) {
                board.getChildren().add(rectangle);
            }
        }
    }

    public void setStoneStatus(boolean visible, String color, Point point, String text) {
        Circle stone = stones[point.y][point.x];
        stone.setVisible(visible);
    
        Pane parent = (Pane) stone.getParent();
        parent.getChildren().removeIf(node -> 
            node instanceof Text && 
            node.getUserData() != null && 
            node.getUserData().equals("label_" + point.x + "_" + point.y));
        
        if (visible) {
            stone.setFill(javafx.scene.paint.Color.web(color));

            Text label = new Text(text);
            txt.add(label);
            label.setFill(javafx.scene.paint.Color.BLACK);
            label.setUserData("label_" + point.x + "_" + point.y);
            double fontSize = stone.getRadius() * 0.7;
            label.setFont(new Font(fontSize));
            parent.getChildren().add(label);
            Runnable updateText = () -> {
                double width = label.getBoundsInLocal().getWidth();
                double height = label.getBoundsInLocal().getHeight();
                label.setX(stone.getCenterX() - width / 2);
                label.setY(stone.getCenterY() + height / 4);
            };
            updateText.run();
            stone.radiusProperty().addListener((obs, oldVal, newVal) -> {
                label.setFont(new Font(newVal.doubleValue() * 0.7));
                updateText.run();
            });
            stone.centerXProperty().addListener((obs, oldVal, newVal) -> updateText.run());
            stone.centerYProperty().addListener((obs, oldVal, newVal) -> updateText.run());
        }
    }

    public int get_margin_width() {
        return widthMargin;
    }

    public int get_margin_height(){ 
        return heightMargin;
    }

    public int getSquareSize() {
        return squareSize;
    }

    public void updateFromMap(Map gameMap) {
        int[][] board = gameMap.get_map();
        for (Text t : txt) {
            t.setManaged(false);
            t.setVisible(false);
        }
        txt.clear();
        for (int i = 0; i < board.length; i++) {
            for (int j = 0; j < board[i].length; j++) {
                Circle stone = stones[i][j];
                
                if (board[i][j] == 0) {
                    stone.setVisible(false);
                } else {
                    stone.setVisible(true);
                    if (board[i][j] == 1) {
                        stone.setFill(Color.BLACK);
                    } else if (board[i][j] == 2) {
                        stone.setFill(Color.SNOW);
                    } else if (board[i][j] == 3) {
                        stone.setFill(Color.web("rgba(0,0,0,0.5)"));
                    } else{
                        stone.setFill(Color.web("rgba(255,255,255,0.5)"));
                    }
                }
            }
        }
    }
    
    public void drawLastMoveBox(Point p) {
        // 移除旧红框
        if (lastMoveRect != null) {
            this.getBoard().getChildren().remove(lastMoveRect);
            lastMoveRect = null;
        }

        int marginWidth = get_margin_width();
        int marginHeight = get_margin_height();

        // 棋子实际中心点，必须和stones 一样
        double centerX = marginWidth + (p.x * squareSize);
        double centerY = marginHeight + (p.y * squareSize);

        // 红框大小比棋子稍大一点
        double size = squareSize * 1.1;

        Rectangle rect = new Rectangle(
                centerX - size / 2,
                centerY - size / 2,
                size,
                size
        );

        rect.setFill(Color.TRANSPARENT);
        rect.setStroke(Color.RED);
        rect.setStrokeWidth(2);

        lastMoveRect = rect;

        this.getBoard().getChildren().add(rect);
    }
}