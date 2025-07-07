import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

public class PacMan {
    private double x;
    private double y;
    private final double SIZE = 22;
    private final double SPEED = 2.5;
    private double speedX = 0;
    private double speedY = 0;

    private double mouthAngle = 30;
    private boolean mouthOpening = false;

    private String direction = "RIGHT";

    public PacMan(double startX, double startY) {
        this.x = startX;
        this.y = startY;
    }

    public void setDirection(String dir) {
        this.direction = dir;
        switch (dir) {
            case "UP" -> {
                speedX = 0;
                speedY = -SPEED;
            }
            case "DOWN" -> {
                speedX = 0;
                speedY = SPEED;
            }
            case "LEFT" -> {
                speedX = -SPEED;
                speedY = 0;
            }
            case "RIGHT" -> {
                speedX = SPEED;
                speedY = 0;
            }
        }
    }

    public void update(Game game) {
        double nextX = x + speedX;
        double nextY = y + speedY;

        if (canMove(nextX, nextY, game)) {
            x = nextX;
            y = nextY;
        }

        // Animate mouth opening and closing
        if (mouthOpening) {
            mouthAngle += 4;
            if (mouthAngle > 30) mouthOpening = false;
        } else {
            mouthAngle -= 4;
            if (mouthAngle < 5) mouthOpening = true;
        }
    }

    public void draw(GraphicsContext gc) {
        gc.setFill(Color.YELLOW);
        double startAngle = switch (direction) {
            case "RIGHT" -> mouthAngle;
            case "LEFT" -> 180 + mouthAngle;
            case "UP" -> 90 + mouthAngle;
            case "DOWN" -> 270 + mouthAngle;
            default -> mouthAngle;
        };
        double arcExtent = 360 - 2 * mouthAngle;

        gc.fillArc(x, y, SIZE, SIZE, startAngle, arcExtent, javafx.scene.shape.ArcType.ROUND);

        // Draw eye
        gc.setFill(Color.BLACK);
        double eyeX = x + SIZE / 2.7 + (direction.equals("RIGHT") ? 5 : direction.equals("LEFT") ? -5 : 0);
        double eyeY = y + SIZE / 4.5;
        gc.fillOval(eyeX, eyeY, 5, 5);
    }

    public boolean checkPelletCollision(double px, double py) {
        double pacCenterX = x + SIZE / 2.0;
        double pacCenterY = y + SIZE / 2.0;
        double dx = pacCenterX - px;
        double dy = pacCenterY - py;
        return Math.sqrt(dx * dx + dy * dy) < 12;
    }

    private boolean canMove(double nextX, double nextY, Game game) {
        double margin = 2;  // small margin from edges

        double left = nextX + margin;
        double right = nextX + SIZE - 1 - margin;
        double top = nextY + margin;
        double bottom = nextY + SIZE - 1 - margin;

        int leftTile = (int)(left / game.TILE_SIZE);
        int rightTile = (int)(right / game.TILE_SIZE);
        int topTile = (int)(top / game.TILE_SIZE);
        int bottomTile = (int)(bottom / game.TILE_SIZE);

        if (topTile < 0 || bottomTile >= game.ROWS || leftTile < 0 || rightTile >= game.COLS)
            return false;

        return game.map[topTile][leftTile] == 0 &&
                game.map[topTile][rightTile] == 0 &&
                game.map[bottomTile][leftTile] == 0 &&
                game.map[bottomTile][rightTile] == 0;
    }


    public double getX() {
        return x + SIZE / 2.0;
    }

    public double getY() {
        return y + SIZE / 2.0;
    }

    public String getDirection() {
        return direction;
    }
}
