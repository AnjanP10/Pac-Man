import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class PacManGame extends Application {

    private double pacX = 100, pacY = 100;
    private final double PAC_SIZE = 30;
    private double speedX = 0, speedY = 0;

    private final List<double[]> pellets = new ArrayList<>();

    @Override
    public void start(Stage stage) {
        Canvas canvas = new Canvas(600, 400);
        GraphicsContext gc = canvas.getGraphicsContext2D();

        // Create pellets grid
        for (int i = 50; i < 600; i += 40) {
            for (int j = 50; j < 400; j += 40) {
                pellets.add(new double[]{i, j});
            }
        }

        Scene scene = new Scene(new StackPane(canvas));
        stage.setScene(scene);
        stage.setTitle("Pac-Man Basic Graphics");
        stage.show();

        // Controls
        scene.setOnKeyPressed(e -> {
            if (e.getCode() == KeyCode.UP) {
                speedY = -2; speedX = 0;
            } else if (e.getCode() == KeyCode.DOWN) {
                speedY = 2; speedX = 0;
            } else if (e.getCode() == KeyCode.LEFT) {
                speedX = -2; speedY = 0;
            } else if (e.getCode() == KeyCode.RIGHT) {
                speedX = 2; speedY = 0;
            }
        });

        // Game Loop
        new AnimationTimer() {
            @Override
            public void handle(long now) {
                // Move Pac-Man
                pacX += speedX;
                pacY += speedY;

                // Clear screen
                gc.setFill(Color.DARKBLUE);
                gc.fillRect(0, 0, canvas.getWidth(), canvas.getHeight());

                // Draw a simple maze grid
                gc.setStroke(Color.NAVY);
                for (int i = 0; i < 600; i += 40) {
                    gc.strokeLine(i, 0, i, 400);
                }
                for (int j = 0; j < 400; j += 40) {
                    gc.strokeLine(0, j, 600, j);
                }

                // Draw pellets
                gc.setFill(Color.WHITE);
                for (double[] pellet : pellets) {
                    gc.fillOval(pellet[0] + 15, pellet[1] + 15, 6, 6);
                }

                // Check collision
                Iterator<double[]> iterator = pellets.iterator();
                while (iterator.hasNext()) {
                    double[] pellet = iterator.next();
                    double dx = pacX - pellet[0];
                    double dy = pacY - pellet[1];
                    if (Math.sqrt(dx * dx + dy * dy) < 15) {
                        iterator.remove(); // Eat pellet
                    }
                }

                // Draw Pac-Man (yellow arc mouth)
                gc.setFill(Color.YELLOW);
                double arcExtent = 300;
                double startAngle = 30; // default facing right

                if (speedX > 0) { // Moving right
                    startAngle = 30;
                } else if (speedX < 0) { // Moving left
                    startAngle = 210;
                } else if (speedY < 0) { // Moving up
                    startAngle = 120;
                } else if (speedY > 0) { // Moving down
                    startAngle = 300;
                }

                gc.fillArc(pacX, pacY, PAC_SIZE, PAC_SIZE, startAngle, arcExtent, javafx.scene.shape.ArcType.ROUND);
            }
        }.start();
    }

    public static void main(String[] args) {
        launch();
    }
}
