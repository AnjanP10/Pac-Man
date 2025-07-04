import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

import java.util.HashSet;
import java.util.Set;

public class PacManGame extends Application {

    private final int TILE_SIZE = 32;
    private final int ROWS = 15;
    private final int COLS = 20;

    private double pacX = TILE_SIZE * 1.5;
    private double pacY = TILE_SIZE * 1.5;
    private final double PAC_SIZE = 22;

    private double speedX = 0, speedY = 0;
    private final double MOVE_SPEED = 3.5;

    private final int[][] map = {
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

    private double mouthAngle = 30;
    private boolean mouthOpening = false;

    @Override
    public void start(Stage stage) {
        Canvas canvas = new Canvas(COLS * TILE_SIZE, ROWS * TILE_SIZE);
        GraphicsContext gc = canvas.getGraphicsContext2D();

        // Initialize pellets on empty tiles
        for (int r = 0; r < ROWS; r++)
            for (int c = 0; c < COLS; c++)
                if (map[r][c] == 0)
                    pellets.add(r + "," + c);

        Scene scene = new Scene(new StackPane(canvas));
        stage.setScene(scene);
        stage.setTitle("Smooth Pac-Man");
        stage.show();

        scene.setOnKeyPressed(e -> {
            if (e.getCode() == KeyCode.UP) {
                speedX = 0;
                speedY = -MOVE_SPEED;
            } else if (e.getCode() == KeyCode.DOWN) {
                speedX = 0;
                speedY = MOVE_SPEED;
            } else if (e.getCode() == KeyCode.LEFT) {
                speedX = -MOVE_SPEED;
                speedY = 0;
            } else if (e.getCode() == KeyCode.RIGHT) {
                speedX = MOVE_SPEED;
                speedY = 0;
            }
        });

        new AnimationTimer() {
            @Override
            public void handle(long now) {
                double nextX = pacX + speedX;
                double nextY = pacY + speedY;

                if (canMove(nextX, nextY)) {
                    pacX = nextX;
                    pacY = nextY;
                }

                // Clear background
                gc.setFill(Color.web("#000010"));
                gc.fillRect(0, 0, canvas.getWidth(), canvas.getHeight());

                // Draw walls
                for (int r = 0; r < ROWS; r++) {
                    for (int c = 0; c < COLS; c++) {
                        if (map[r][c] == 1) {
                            double x = c * TILE_SIZE;
                            double y = r * TILE_SIZE;
                            gc.setFill(Color.web("#1E90FF"));
                            gc.fillRoundRect(x + 1, y + 1, TILE_SIZE - 2, TILE_SIZE - 2, 10, 10);
                            gc.setStroke(Color.web("#00BFFF"));
                            gc.setLineWidth(2);
                            gc.strokeRoundRect(x + 1, y + 1, TILE_SIZE - 2, TILE_SIZE - 2, 10, 10);
                        }
                    }
                }

                // Draw pellets with glow
                gc.setFill(Color.web("#FFFACD"));
                Set<String> eaten = new HashSet<>();
                for (String p : pellets) {
                    String[] parts = p.split(",");
                    int pr = Integer.parseInt(parts[0]);
                    int pc = Integer.parseInt(parts[1]);

                    double px = pc * TILE_SIZE + TILE_SIZE / 2.0 - 4;
                    double py = pr * TILE_SIZE + TILE_SIZE / 2.0 - 4;

                    double pacCenterX = pacX + PAC_SIZE / 2.0;
                    double pacCenterY = pacY + PAC_SIZE / 2.0;
                    double dx = pacCenterX - (px + 4);
                    double dy = pacCenterY - (py + 4);

                    if (Math.sqrt(dx * dx + dy * dy) < 12) {
                        eaten.add(p);
                    } else {
                        gc.fillOval(px, py, 8, 8);
                        gc.setStroke(Color.web("#FFF68F"));
                        gc.setLineWidth(1);
                        gc.strokeOval(px, py, 8, 8);
                    }
                }
                pellets.removeAll(eaten);

                // Animate mouth opening/closing
                if (mouthOpening) {
                    mouthAngle += 4;
                    if (mouthAngle > 30) mouthOpening = false;
                } else {
                    mouthAngle -= 4;
                    if (mouthAngle < 5) mouthOpening = true;
                }

                // Draw Pac-Man
                gc.setFill(Color.web("#FFD700"));
                double startAngle = mouthAngle;
                double arcExtent = 360 - 2 * mouthAngle;

                if (speedX > 0) startAngle = mouthAngle;
                else if (speedX < 0) startAngle = 180 + mouthAngle;
                else if (speedY < 0) startAngle = 90 + mouthAngle;
                else if (speedY > 0) startAngle = 270 + mouthAngle;

                gc.fillArc(pacX, pacY, PAC_SIZE, PAC_SIZE, startAngle, arcExtent, javafx.scene.shape.ArcType.ROUND);

                // Draw eye
                gc.setFill(Color.BLACK);
                double eyeX = pacX + PAC_SIZE / 2.7 + (speedX != 0 ? (speedX > 0 ? 5 : -5) : 0);
                double eyeY = pacY + PAC_SIZE / 4.5;
                gc.fillOval(eyeX, eyeY, 5, 5);
            }
        }.start();
    }

    private boolean canMove(double nextX, double nextY) {
        double left = nextX;
        double right = nextX + PAC_SIZE;
        double top = nextY;
        double bottom = nextY + PAC_SIZE;

        int leftTile = (int) (left / TILE_SIZE);
        int rightTile = (int) (right / TILE_SIZE);
        int topTile = (int) (top / TILE_SIZE);
        int bottomTile = (int) (bottom / TILE_SIZE);

        if (topTile < 0 || bottomTile >= ROWS || leftTile < 0 || rightTile >= COLS)
            return false;

        return map[topTile][leftTile] == 0 &&
                map[topTile][rightTile] == 0 &&
                map[bottomTile][leftTile] == 0 &&
                map[bottomTile][rightTile] == 0;
    }

    public static void main(String[] args) {
        launch();
    }
}
