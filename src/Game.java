import javafx.animation.AnimationTimer;
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
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.*;

public class Game {

    private int selectedLevel = 0;

    public Game(int selectedLevel) {
        this.selectedLevel = selectedLevel;
    }

    public enum GameState {
        RUNNING,
        PAUSED
    }

    public final int TILE_SIZE = 32;
    public final int ROWS = 15;
    public final int COLS = 20;

    private long startTime;
    private long pauseStartTime = 0;
    private long totalPausedDuration = 0;
    private int elapsedSeconds;
    private LocalDateTime played_at;
    private int score = 0;

    private GameState gameState = GameState.RUNNING;
    private Label pauseLabel;
    private Rectangle pauseOverlay;
    private Button pauseResumeButton;

    public int[][] map;
    private final List<int[][]> levels = new ArrayList<>();
    private final Set<String> pellets = new HashSet<>();
    private PacMan pacman;
    private List<Ghost> ghosts = new ArrayList<>();

    public void show(Stage stage) {
        initLevels();

        if (selectedLevel < 0 || selectedLevel >= levels.size()) {
            selectedLevel = 0;
        }
        map = levels.get(selectedLevel);

        Canvas canvas = new Canvas(COLS * TILE_SIZE, ROWS * TILE_SIZE);
        GraphicsContext gc = canvas.getGraphicsContext2D();

        pacman = new PacMan(TILE_SIZE * 1.5 - 11, TILE_SIZE * 1.5 - 11);

        // Initialize ghosts with their scatter targets
        ghosts.clear();
        ghosts.add(new Ghost(TILE_SIZE * 9, TILE_SIZE * 7, Color.RED, 0, COLS - 1, this, "src/icons/red_ghost.png"));
        ghosts.add(new Ghost(TILE_SIZE * 9, TILE_SIZE * 9, Color.ORANGE, ROWS - 1, 0, this, "src/icons/orange_ghost.png"));

        pellets.clear();
        for (int r = 0; r < ROWS; r++)
            for (int c = 0; c < COLS; c++)
                if (map[r][c] == 0)
                    pellets.add(r + "," + c);

        Button exitButton = new Button("Exit");
        exitButton.setStyle(
                "-fx-background-color: rgba(100,149,237,0.94); " +
                        "-fx-text-fill: white; " +
                        "-fx-font-weight: bold; " +
                        "-fx-font-size: 14px;"
        );
        exitButton.setOnAction(e -> {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Confirm Exit");
            alert.setHeaderText(null);
            alert.setContentText("Are you sure you want to exit?");
            alert.showAndWait().ifPresent(response -> {
                if (response == ButtonType.OK) {
                    MenuScreen.show(stage);
                }
            });
        });

        pauseResumeButton = new Button("Pause");
        pauseResumeButton.setStyle(
                "-fx-background-color: rgba(100,149,237,0.94); " +
                        "-fx-text-fill: white; " +
                        "-fx-font-weight: bold; " +
                        "-fx-font-size: 14px;"
        );
        pauseResumeButton.setOnAction(e -> togglePause());

        HBox topBar = new HBox(10, pauseResumeButton, exitButton);
        topBar.setAlignment(Pos.TOP_RIGHT);
        topBar.setPadding(new Insets(6));

        pauseOverlay = new Rectangle(canvas.getWidth(), canvas.getHeight());
        pauseOverlay.setFill(Color.rgb(0, 0, 0, 0.5));
        pauseOverlay.setVisible(false);

        pauseLabel = new Label("PAUSED");
        pauseLabel.setStyle("-fx-font-size: 36px; -fx-text-fill: yellow;");
        pauseLabel.setVisible(false);

        StackPane overlay = new StackPane(pauseOverlay, pauseLabel);
        overlay.setPickOnBounds(false);

        StackPane root = new StackPane(canvas, overlay, topBar);
        StackPane.setAlignment(topBar, Pos.TOP_RIGHT);

        Scene scene = new Scene(root);
        stage.setScene(scene);
        stage.setTitle("Smooth Pac-Man");
        stage.show();

        root.requestFocus();

        startTime = System.currentTimeMillis();
        played_at = LocalDateTime.now(); // ✅ Set played_at

        scene.setOnKeyPressed(e -> {
            if (gameState == GameState.RUNNING) {
                if (e.getCode() == KeyCode.UP) pacman.setDirection("UP");
                else if (e.getCode() == KeyCode.DOWN) pacman.setDirection("DOWN");
                else if (e.getCode() == KeyCode.LEFT) pacman.setDirection("LEFT");
                else if (e.getCode() == KeyCode.RIGHT) pacman.setDirection("RIGHT");
            }
            if (e.getCode() == KeyCode.P) {
                pauseGame();
            } else if (e.getCode() == KeyCode.R) {
                resumeGame();
            }
        });

        scene.setOnMouseClicked(e -> root.requestFocus());

        new AnimationTimer() {
            @Override
            public void handle(long now) {
                if (gameState == GameState.PAUSED) return;

                pacman.update(Game.this);

                gc.setFill(Color.BLACK);
                gc.fillRect(0, 0, canvas.getWidth(), canvas.getHeight());

                for (int r = 0; r < ROWS; r++) {
                    for (int c = 0; c < COLS; c++) {
                        if (map[r][c] == 1) {
                            double x = c * TILE_SIZE;
                            double y = r * TILE_SIZE;
                            gc.setStroke(Color.BLUE);
                            gc.setLineWidth(3);
                            gc.strokeRect(x, y, TILE_SIZE, TILE_SIZE);
                        }
                    }
                }

                gc.setFill(Color.WHITE);
                Set<String> eaten = new HashSet<>();
                for (String p : pellets) {
                    String[] parts = p.split(",");
                    int pr = Integer.parseInt(parts[0]);
                    int pc = Integer.parseInt(parts[1]);
                    double px = pc * TILE_SIZE + TILE_SIZE / 2.0;
                    double py = pr * TILE_SIZE + TILE_SIZE / 2.0;

                    if (pacman.checkPelletCollision(px, py)) {
                        eaten.add(p);
                        score += 10;
                    } else {
                        gc.fillOval(px - 2, py - 2, 4, 4);
                    }
                }
                pellets.removeAll(eaten);

                elapsedSeconds = (int) ((System.currentTimeMillis() - startTime - totalPausedDuration) / 1000);

                gc.setFont(javafx.scene.text.Font.font(20));
                gc.fillText("Score: " + score, 10, 25);
                gc.fillText("Time: " + elapsedSeconds + "s", 120, 25);

                // ✅ Update and draw ghosts
                for (Ghost g : ghosts) {
                    g.setState(GhostState.CHASE); // Make sure ghosts chase
                    g.update(pacman.getX(), pacman.getY(), pacman.getDirection(), ghosts.get(0).getX(), ghosts.get(0).getY());
                    g.draw(gc);

                    if (g.checkPacmanCollision(pacman.getX(), pacman.getY())) {
                        stop();
                        showGameOver(stage);
                        return;
                    }
                }

                if (pellets.isEmpty()) {
                    saveScore();
                    stop();
                    showGameOver(stage);
                }

                pacman.draw(gc);
            }
        }.start();
    }

    private void togglePause() {
        if (gameState == GameState.RUNNING) {
            pauseGame();
        } else if (gameState == GameState.PAUSED) {
            resumeGame();
        }
    }

    private void pauseGame() {
        if (gameState != GameState.RUNNING) return;
        gameState = GameState.PAUSED;
        pauseStartTime = System.currentTimeMillis();
        pauseOverlay.setVisible(true);
        pauseLabel.setVisible(true);
        pauseResumeButton.setText("Resume");
    }

    private void resumeGame() {
        if (gameState != GameState.PAUSED) return;
        totalPausedDuration += System.currentTimeMillis() - pauseStartTime;
        gameState = GameState.RUNNING;
        pauseOverlay.setVisible(false);
        pauseLabel.setVisible(false);
        pauseResumeButton.setText("Pause");
    }

    public void saveScore() {
        if (Session.currentUser == null) return;
        try (Connection conn = DatabaseConnection.getConnection()) {
            PreparedStatement stmt = conn.prepareStatement(
                    "INSERT INTO game_results (username,score,time_taken,played_at) VALUES (?,?,?,?)"
            );
            stmt.setString(1, Session.currentUser);
            stmt.setInt(2, score);
            stmt.setInt(3, elapsedSeconds);
            stmt.setTimestamp(4, Timestamp.valueOf(played_at));
            stmt.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void showGameOver(Stage stage) {
        Label msg = new Label("Game Over!\nYour score: " + score + "\nYour time: " + elapsedSeconds + " seconds");
        msg.setStyle("-fx-font-size:18px;-fx-text-fill:rgba(207,159,64,0.94)");

        Button backBtn = new Button("Back to Menu");
        Button playAgainBtn = new Button("Play Again");
        Button exitBtn = new Button("Exit");

        backBtn.setOnAction(e -> MenuScreen.show(stage));
        playAgainBtn.setOnAction(e -> new Game(selectedLevel).show(stage));
        exitBtn.setOnAction(e -> stage.close());

        HBox buttons = new HBox(15, backBtn, playAgainBtn, exitBtn);
        buttons.setAlignment(Pos.CENTER);

        VBox root = new VBox(20, msg, buttons);
        root.setAlignment(Pos.CENTER);
        root.setPadding(new Insets(20));
        root.setStyle("-fx-background-color:rgb(56,64,152);");

        stage.setScene(new Scene(root, 400, 220));
    }

    private void initLevels() {
        int[][] level = {
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
        levels.add(level);
        levels.add(level);
        levels.add(level);
    }
}
