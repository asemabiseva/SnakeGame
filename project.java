import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

import java.util.LinkedList;
import java.util.Random;

public class SnakeGame extends Application {
    private static final int TILE_SIZE = 20;
    private static final int GRID_SIZE = 20;
    private static final int INITIAL_SNAKE_SIZE = 3;

    // Attributes (Fields)
    private LinkedList<SnakePart> snake = new LinkedList<>();
    private SnakePart food;
    private int direction = 1; //d->1 s->2 a->3 w->4
    private boolean gameOver = false;
    private int counter = 0;
    private int highestcounter = 0;
    private double speed = 5.0; // Initial speed
    private Random random = new Random();



    public static void main(String[] args) {
        launch(args);
    }


    @Override
    public void start(Stage primaryStage) {
        StackPane root = new StackPane();
        Canvas canvas = new Canvas(GRID_SIZE * TILE_SIZE, GRID_SIZE * TILE_SIZE);
        root.getChildren().add(canvas);

        Button resetButton = new Button("RESET");
        resetButton.setOnAction(event -> initializeGame());

        StackPane.setAlignment(resetButton, javafx.geometry.Pos.BOTTOM_CENTER);
        root.getChildren().add(resetButton);

        Scene scene = new Scene(root);

        canvas.setFocusTraversable(true);
        canvas.requestFocus();

        canvas.focusedProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue) {
                canvas.requestFocus();
            }
        });

        scene.setOnKeyPressed(event -> handleKeyPress(event.getCode()));

        primaryStage.setTitle("Snake Game");
        primaryStage.setScene(scene);
        primaryStage.show();

        initializeGame();

        new AnimationTimer() {
            long lastUpdate = 0;

            @Override
            public void handle(long now) {
                if (now - lastUpdate >= 100_000_0000 / 10) {
                    if (!gameOver) {
                        update();
                        draw(canvas.getGraphicsContext2D());
                    }
                    lastUpdate = now;
                }
            }
        }.start();
    }

    private void initializeGame() {
        snake.clear();
        direction = 1;
        gameOver = false;
        counter = 0;
        snake.add(new SnakePart(GRID_SIZE / 2, GRID_SIZE / 2));
        for (int i = 1; i < INITIAL_SNAKE_SIZE; i++) {
            snake.add(new SnakePart(GRID_SIZE / 2 - i, GRID_SIZE / 2));
        }
        generateFood();
    }

    private void generateFood() {
        do {
            int x = random.nextInt(GRID_SIZE);
            int y = random.nextInt(GRID_SIZE);
            food = new SnakePart(x, y);
        } while (snake.contains(food));
    }

    private void handleKeyPress(KeyCode code) {
        switch (code) {
            case UP:
            case W:
                if (direction != 2)
                    direction = 4;
                break;
            case DOWN:
            case S:
                if (direction != 4)
                    direction = 2;
                break;
            case LEFT:
            case A:
                if (direction != 1)
                    direction = 3;
                break;
            case RIGHT:
            case D:
                if (direction != 3)
                    direction = 1;
                break;
            case R:
                initializeGame();
                break;
        }
    }

    private void update() {
        move();
        checkCollision();
        checkFood();
        if (counter % 5 == 0 && counter > 0) {
            speed += 0.5;
        }
    }

    private void move() {
        SnakePart head = snake.getFirst();
        SnakePart newHead = new SnakePart(head.getX(), head.getY());

        switch (direction) {
            case 4:
                newHead.setY((newHead.getY() - 1 + GRID_SIZE) % GRID_SIZE);
                break;
            case 2:
                newHead.setY((newHead.getY() + 1) % GRID_SIZE);
                break;
            case 3:
                newHead.setX((newHead.getX() - 1 + GRID_SIZE) % GRID_SIZE);
                break;
            case 1:
                newHead.setX((newHead.getX() + 1) % GRID_SIZE);
                break;
        }

        snake.addFirst(newHead);

        if (snake.size() > counter + INITIAL_SNAKE_SIZE)
            snake.removeLast();
    }

    private void checkCollision() {
        SnakePart head = snake.getFirst();

        if (head.getX() < 0 || head.getX() >= GRID_SIZE || head.getY() < 0 || head.getY() >= GRID_SIZE || intersects()) {
            gameOver = true;
            if (counter > highestcounter) {
                highestcounter = counter;
            }
        }
    }

    private boolean intersects() {
        SnakePart head = snake.getFirst();
        for (int i = 1; i < snake.size(); i++) {
            if (head.equals(snake.get(i))) {
                return true;
            }
        }
        return false;
    }

    private void checkFood() {
        SnakePart head = snake.getFirst();

        if (head.equals(food)) {
            counter++;
            generateFood();
        }
    }

    private void draw(GraphicsContext gc) {
        gc.setFill(Color.GREENYELLOW);
        gc.fillRect(0, 0, GRID_SIZE * TILE_SIZE, GRID_SIZE * TILE_SIZE);

        gc.setFill(Color.BLUE);
        for (SnakePart part : snake) {
            gc.fillRect(part.getX() * TILE_SIZE, part.getY() * TILE_SIZE, TILE_SIZE, TILE_SIZE);
        }

        gc.setFill(Color.RED);
        gc.fillOval(food.getX() * TILE_SIZE, food.getY() * TILE_SIZE, TILE_SIZE, TILE_SIZE);

        gc.setFill(Color.BLACK);
        gc.fillText("counter: " + counter, 10, 20);
        gc.fillText("Highest counter: " + highestcounter, 10, 40);

        if (gameOver) {
            gc.setFill(Color.RED);
            gc.fillText("GAME OVER", GRID_SIZE * TILE_SIZE / 2 - 50, GRID_SIZE * TILE_SIZE / 2 - 10);
        }
    }

    private static class SnakePart {
    private int x;
    private int y;

    public SnakePart(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public void setX(int x) {
        this.x = x;
    }

    public void setY(int y) {
        this.y = y;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        SnakePart snakePart = (SnakePart) obj;
        return x == snakePart.x && y == snakePart.y;
    }

    @Override
    public int hashCode() {
        return 31 * x + y;
    }
}

}
