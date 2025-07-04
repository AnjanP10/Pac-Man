import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.HashSet;
import java.util.Set;

public class Game extends Application {

    public final int TILE_SIZE = 32;
    public final int ROWS = 15;
    public final int COLS = 20;
    private int score = 0;

    public final int[][] map = {
            {1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1},
            {1,0,0,0,0,0,0,1,0,0,0,0,0,1,0,0,0,0,0,1},
            {1,0,1,1,1,0,1,1,1,0,1,1,1,1,0,1,1,1,0,1},
            {1,0,1,0,0,0,0,0,1,0,1,0,0,0,0,0,0,1,0,1},
            {1,0,1,0,1,1,1,0,1,0,1,0,1,1,1,1,0,1,0,1},
            {1,0,0,0,0,0,1,0,0,0,1,0,0,0,0,1,0,0,0,1},
            {1,1,1,1,1,0,1,1,1,1,1,1,1,1,0,1,1,1,1,1},
            {1,0,0,0,1,0,0,0,0,0,0,0,0,1,0,0,0,0,0,1},
            {1,0,1,0,1,1,1,1,1,1,1,1,0,1,1,1,1,1,0,1},
            {1,0,1,0,0,0,0,0,0,0,0,1,0,0,0,0,0,1,0,1},
            {1,0,1,1,1,1,1,1,1,1,0,1,1,1,1,1,0,1,0,1},
            {1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,1,0,1},
            {1,0,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,0,1},
            {1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,1},
            {1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1},
    };

    private final Set<String> pellets = new HashSet<>();
    private PacMan pacman;

    @Override
    public void start(Stage stage) {
        Canvas canvas = new Canvas(COLS * TILE_SIZE, ROWS * TILE_SIZE);
        GraphicsContext gc = canvas.getGraphicsContext2D();

        pacman = new PacMan(TILE_SIZE * 1.5 - 11, TILE_SIZE * 1.5 - 11);

        // Initialize pellets
        for (int r = 0; r < ROWS; r++)
            for (int c = 0; c < COLS; c++)
                if (map[r][c] == 0)
                    pellets.add(r + "," + c);

        Scene scene = new Scene(new StackPane(canvas));
        stage.setScene(scene);
        stage.setTitle("Smooth Pac-Man");
        stage.show();

        scene.setOnKeyPressed(e -> {
            if (e.getCode() == KeyCode.UP) pacman.setDirection("UP");
            else if (e.getCode() == KeyCode.DOWN) pacman.setDirection("DOWN");
            else if (e.getCode() == KeyCode.LEFT) pacman.setDirection("LEFT");
            else if (e.getCode() == KeyCode.RIGHT) pacman.setDirection("RIGHT");
        });

        new AnimationTimer() {
            @Override
            public void handle(long now) {
                pacman.update(Game.this);

                // Clear background
                gc.setFill(Color.BLACK);
                gc.fillRect(0, 0, canvas.getWidth(), canvas.getHeight());

                // Draw walls
                for (int r = 0; r < ROWS; r++)
                    for (int c = 0; c < COLS; c++)
                        if (map[r][c] == 1) {
                            double x = c * TILE_SIZE;
                            double y = r * TILE_SIZE;
                            gc.setFill(Color.DARKBLUE);
                            gc.fillRect(x, y, TILE_SIZE, TILE_SIZE);
                            gc.setStroke(Color.BLUE);
                            gc.setLineWidth(2);
                            gc.strokeRect(x, y, TILE_SIZE, TILE_SIZE);
                        }

                // Draw pellets
                gc.setFill(Color.YELLOW);
                Set<String> eaten = new HashSet<>();
                for (String p : pellets) {
                    String[] parts = p.split(",");
                    int pr = Integer.parseInt(parts[0]);
                    int pc = Integer.parseInt(parts[1]);

                    double px = pc * TILE_SIZE + TILE_SIZE / 2.0;
                    double py = pr * TILE_SIZE + TILE_SIZE / 2.0;

                    if (pacman.checkPelletCollision(px, py)) {
                        eaten.add(p);
                        score += 10; // Each pellet worth 10 points
                    } else {
                        gc.fillOval(px - 4, py - 4, 8, 8);
                    }
                }
                pellets.removeAll(eaten);

                // ✅ Draw the score
                gc.setFill(Color.YELLOW);
                gc.setFont(javafx.scene.text.Font.font(20));
                gc.fillText("Score: " + score, 10, 25);

                if (pellets.isEmpty()) {
                    saveScore();
                    stop(); // Stop animation timer
                    showGameOver(stage);
                }
                pacman.draw(gc);
            }
        }.start();
    }
    public void saveScore() {
        if (Session.currentUser == null) return;

        try (Connection conn = DatabaseConnection.getConnection()) {
            PreparedStatement stmt = conn.prepareStatement(
                    "INSERT INTO scores (username, score) VALUES (?, ?)"
            );
            stmt.setString(1, Session.currentUser);
            stmt.setInt(2, score);
            stmt.executeUpdate();
            System.out.println("Score saved for user: " + Session.currentUser);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void showGameOver(Stage stage) {
        Label msg = new Label("You won!\nYour score: " + score);
        msg.setStyle("-fx-font-size: 18px; -fx-text-fill: #333;");

        Button backBtn = new Button("Back to Login");
        backBtn.setOnAction(e -> LoginScreen.show(stage));

        VBox root = new VBox(15, msg, backBtn);
        root.setAlignment(Pos.CENTER);
        root.setPadding(new Insets(20));
        root.setStyle("-fx-background-color: #f0f0f0;");

        stage.setScene(new Scene(root, 300, 200));
    }

}
