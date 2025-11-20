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

/**
 *Board（棋盘渲染类）
 *负责：
 *绘制棋盘线、dots
 *绘制棋子（Circle[][]）
 *绘制AI调试用评分点（Rectangle[][]）
 *动态更新棋盘布局（响应窗口变化）
 *标红最后落子位置（红框）
 */

public class Board {
    private final Pane board; // 主棋盘 Pane
    private final Circle[][] stones; // 棋子数组：1=黑 2=白
    private final Rectangle[][] score;// AI调试评分格子（默认隐藏）
    private final ArrayList<Circle> dots; // 黑点
    private final ArrayList<Point> dotsCoordinate = new ArrayList<>();
    private final ArrayList<Line> lines; // 棋盘线
    private int size; // 棋盘尺寸
    private final int totalLines; // 棋盘线数（=19）
    private int squareSize;  // 每格大小（像素）
    private int marginHeight;  // 垂直居中偏移
    private int marginWidth; // 水平居中偏移
    private final ArrayList<Text> txt = new ArrayList<>(); // 棋子数字标签
    private Rectangle lastMoveRect = null; // 标量的落子标记框

    // Board 构造函数
    /** 初始化棋盘外边距，使棋盘居中显示 */
    private void initMargin(int height, int width) {
        int size = squareSize * (totalLines - 1);
        marginHeight = (height - size) / 2;
        marginWidth = (width - size) / 2;
    }

    /** 初始化每格像素大小（squareSize） */
    private void initSquareSize() {
        squareSize = size / totalLines;
    }
    /** 初始化棋盘外边距，使棋盘居中显示 */
    private void updateDots() {
        for (int i = 0; i < dotsCoordinate.size(); i++) {
            Circle dot = dots.get(i);
            dot.setRadius((double) squareSize / 7);
            dot.setCenterX((squareSize * dotsCoordinate.get(i).x) + marginWidth);
            dot.setCenterY((squareSize * dotsCoordinate.get(i).y) + marginHeight);
            dot.setStroke(Color.TRANSPARENT);
            dot.setFill(Color.BLACK);
            dot.setStrokeWidth(1);
        }
    }
    /** 根据 dotsCoordinate 创建dots */
    private void createDots() {
        for (Point p : dotsCoordinate) {
            Circle dots = new Circle();
            dots.setRadius((double) squareSize / 7);
            dots.setCenterX((squareSize * p.x) + marginWidth);
            dots.setCenterY((squareSize * p.y) + marginHeight);
            dots.setStroke(Color.TRANSPARENT);
            dots.setFill(Color.BLACK);
            dots.setStrokeWidth(1);
            this.dots.add(dots);
        }
    }

    /** 初始化棋盘的（dots） */
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

    /** 创建棋盘线（横线 + 竖线） */
    private void initLines() {
        for (int i = 0; i < totalLines; i++) {
            Line line = new Line(
                marginWidth + (squareSize * i),
                    marginHeight,
                marginWidth + (squareSize * i),
                marginHeight + (squareSize * (totalLines - 1))
            );
            lines.add(line);
        }
        for (int i = 0; i < totalLines; i++) {
            Line line = new Line(
                    marginWidth,
                marginHeight + (squareSize * i),
                marginWidth + (squareSize * (totalLines - 1)),
                marginHeight + (squareSize * i)
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

    /** 返回 Pane 供 Gomoku class 使用 */
    public Pane getBoard() {
        return board;
    }
    /** 更新棋盘线位置（窗口变化时调用） */
    public void updateLines() {
        for (int i = 0; i < lines.size(); i++) {
            if (i < totalLines) {
                lines.get(i).setStartX(marginWidth + (squareSize * i));
                lines.get(i).setStartY(marginHeight);
                lines.get(i).setEndX(marginWidth + (squareSize * i));
                lines.get(i).setEndY(marginHeight + (squareSize * (totalLines - 1)));
            } else {
                int n = i - totalLines;
                lines.get(i).setStartX(marginWidth);
                lines.get(i).setStartY(marginHeight + (squareSize * n));
                lines.get(i).setEndX(marginWidth + (squareSize * (totalLines - 1)));
                lines.get(i).setEndY(marginHeight + (squareSize * n));
            }
        }
    }
    /** 外部调用：更新棋盘在窗口改变时重新布局 */
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
    /** 更新棋子位置与大小 */
    private void updateStones() {
        for (int i = 0; i < stones.length; i++) {
            for (int j = 0; j < stones[i].length; j++) {
                stones[i][j].setRadius((double) squareSize / 2);
                stones[i][j].setCenterX((squareSize * j) + marginWidth);
                stones[i][j].setCenterY((squareSize * i) + marginHeight);
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
        double centerX = (squareSize * j) + marginWidth;
        double centerY = (squareSize * i) + marginHeight;
        r.setX(centerX - size / 2.0);
        r.setY(centerY - size / 2.0);
    }

    /** 初始化棋子 Circle[][] （默认隐藏） */
    public void initStones() {
        for (int i = 0; i < stones.length; i++) {
            for (int j = 0; j < stones[i].length; j++) {
                stones[i][j] = new Circle();
                stones[i][j].setRadius((double) squareSize / 2);
                stones[i][j].setCenterX((squareSize * j) + marginWidth);
                stones[i][j].setCenterY((squareSize * i) + marginHeight);
                stones[i][j].setStroke(Color.TRANSPARENT);
                stones[i][j].setFill(Color.BLUE);
                stones[i][j].setStrokeWidth(1);
                stones[i][j].setVisible(false);
            }
        }
    }

    /** 初始化评分显示，游戏中的矩形提示框（AI 调试） */
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
    /** 建造棋盘*/
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

    /** 初始化棋子 Circle[][] （默认隐藏） */
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

    public int getMarginWidth() {
        return marginWidth;
    }

    public int getMarginHeight(){
        return marginHeight;
    }

    public int getSquareSize() {
        return squareSize;
    }
    /** 从 Map 更新所有棋子显示 */
    public void updateFromMap(Map gameMap) {
        int[][] board = gameMap.getMap();
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
    /** 删除上一个红框 */
    public void removeLastMoveBox() {
        if (lastMoveRect != null) {
            this.getBoard().getChildren().remove(lastMoveRect);
            lastMoveRect = null;
        }
    }
    /** 在最后落子位置绘制红框 */
    public void drawLastMoveBox(Point p) {
        removeLastMoveBox();

        int marginWidth = getMarginWidth();
        int marginHeight = getMarginHeight();

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