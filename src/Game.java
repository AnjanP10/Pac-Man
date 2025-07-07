import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.scene.layout.HBox;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.HashSet;
import java.util.Set;

public class Game extends Application {

    public final int TILE_SIZE = 32;
    public final int ROWS = 15;
    public final int COLS = 20;

    private long startTime; // ⏱️ Store game start time
    private int elapsedSeconds; // ⌛ Track elapsed seconds
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

        // Exit Button
        Button exitButton = new Button("Exit");
        exitButton.setStyle(
                "-fx-background-color: rgba(207,159,64,0.94); " +        // Bootstrap's red
                        "-fx-text-fill: #384098; " +
                        "-fx-font-weight: bold; " +
                        "-fx-font-size: 14px; " +
                        "-fx-cursor: hand;"
        );
        exitButton.setOnAction(e -> {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Confirm Exit");
            alert.setHeaderText(null);
            alert.setContentText("Are you sure you want to exit the game?");

            alert.showAndWait().ifPresent(response -> {
                if (response == ButtonType.OK) {
                    try {
                        stop(); // stop AnimationTimer
                        LoginScreen.show(stage);
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }
            });
        });

// Top bar with Exit button
        HBox topBar = new HBox(exitButton);
        topBar.setAlignment(Pos.TOP_RIGHT);
        topBar.setPadding(new Insets(6));

// Root layout stacking top bar and game canvas
        StackPane root = new StackPane(canvas, topBar);

        Scene scene = new Scene(root);
        stage.setScene(scene);
        stage.setTitle("Smooth Pac-Man");
        stage.show();
        startTime = System.currentTimeMillis();

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

                // ⏱️ Calculate time elapsed in seconds
                elapsedSeconds = (int)((System.currentTimeMillis() - startTime) / 1000);

                // ✅ Draw the score
                gc.setFill(Color.YELLOW);
                gc.setFont(javafx.scene.text.Font.font(20));
                gc.fillText("Score: " + score, 10, 25);

                // ✅ Draw Timer
                gc.fillText("Time: " + elapsedSeconds + "s", 100, 25);

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
                    "INSERT INTO game_results (username, score, time_taken) VALUES (?, ?, ?)"
            );
            stmt.setString(1, Session.currentUser);
            stmt.setInt(2, score);
            stmt.setInt(3, elapsedSeconds);
            stmt.executeUpdate();
            System.out.println("Result saved for user: " + Session.currentUser);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void showGameOver(Stage stage) {
        Label msg = new Label("You won!\nYour score: " + score + "\nYour time: " + elapsedSeconds + " seconds");
        msg.setStyle("-fx-font-size: 18px; -fx-text-fill: rgba(207,159,64,0.94)");

        // Create buttons
        Button backBtn = new Button("Back to Login");
        Button playAgainBtn = new Button("Play Again");
        Button exitBtn = new Button("Exit");

        // Load icons
        javafx.scene.image.Image loginIcon = new javafx.scene.image.Image(getClass().getResourceAsStream("/icons/login.png"), 16, 16, true, true);
        javafx.scene.image.Image replayIcon = new javafx.scene.image.Image(getClass().getResourceAsStream("/icons/replay.png"), 16, 16, true, true);
        javafx.scene.image.Image exitIcon = new javafx.scene.image.Image(getClass().getResourceAsStream("/icons/exit.png"), 16, 16, true, true);

        // Assign icons to buttons
        backBtn.setGraphic(new javafx.scene.image.ImageView(loginIcon));
        playAgainBtn.setGraphic(new javafx.scene.image.ImageView(replayIcon));
        exitBtn.setGraphic(new javafx.scene.image.ImageView(exitIcon));

        // Button actions
        backBtn.setOnAction(e -> LoginScreen.show(stage));
        playAgainBtn.setOnAction(e -> {
            Game newGame = new Game();
            newGame.start(stage);
        });
        exitBtn.setOnAction(e -> stage.close());

        // Layout
        javafx.scene.layout.HBox buttons = new javafx.scene.layout.HBox(15, backBtn, playAgainBtn, exitBtn);
        buttons.setAlignment(Pos.CENTER);

        VBox root = new VBox(20, msg, buttons);
        root.setAlignment(Pos.CENTER);
        root.setPadding(new Insets(20));
        root.setStyle("-fx-background-color: rgb(56,64,152);");

        stage.setScene(new Scene(root, 400, 220));
    }


}
