import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import java.util.*;

public class Ghost {
    private double x, y;
    private final double size = 40;
    private final double speed = 1.0;
    private double speedX = 0, speedY = 0;
    private Color color;
    private GhostState state;
    private int targetRow, targetCol;
    private int scatterTargetRow, scatterTargetCol;

    private final Game game;
    private Image ghostImage;

    private int lastDirection = -1;

    public Ghost(double startX, double startY, Color color, int scatterRow, int scatterCol, Game game, String imagePath) {
        this.x = startX + game.TILE_SIZE / 2.0;
        this.y = startY + game.TILE_SIZE / 2.0;
        this.color = color;
        this.state = GhostState.SCATTER;
        this.scatterTargetRow = scatterRow;
        this.scatterTargetCol = scatterCol;
        this.game = game;

        try {
            var url = getClass().getResource(imagePath);
            if (url != null) {
                ghostImage = new Image(url.toExternalForm());
            } else {
                ghostImage = new Image("file:" + imagePath);
            }

            if (ghostImage.getWidth() == 0) {
                ghostImage = null;
            }
        } catch (Exception e) {
            ghostImage = null;
        }
    }

    public void setState(GhostState newState) {
        this.state = newState;
    }

    public void update(double pacX, double pacY, String pacDirection, double blinkyX, double blinkyY) {
        int currentRow = (int) (y / game.TILE_SIZE);
        int currentCol = (int) (x / game.TILE_SIZE);

        double tileCenterX = currentCol * game.TILE_SIZE + game.TILE_SIZE / 2.0;
        double tileCenterY = currentRow * game.TILE_SIZE + game.TILE_SIZE / 2.0;

        // Check if ghost is at center of tile
        boolean atCenter = Math.abs(x - tileCenterX) < 1 && Math.abs(y - tileCenterY) < 1;

        if (atCenter) {
            // Snap to center exactly
            x = tileCenterX;
            y = tileCenterY;

            // Determine target based on state
            switch (state) {
                case SCATTER -> {
                    targetRow = scatterTargetRow;
                    targetCol = scatterTargetCol;
                }
                case CHASE -> {
                    targetRow = (int) (pacY / game.TILE_SIZE);
                    targetCol = (int) (pacX / game.TILE_SIZE);
                }
                case FRIGHTENED -> {
                    targetRow = new Random().nextInt(game.ROWS);
                    targetCol = new Random().nextInt(game.COLS);
                }
            }
            int nextDirection = bfsDirection();

            if (nextDirection == -1 || !canMove(currentRow, currentCol, nextDirection)) {
                nextDirection = getRandomDirection(currentRow, currentCol);
            }

            if (nextDirection == -1 && lastDirection != -1 && canMove(currentRow, currentCol, oppositeDirection(lastDirection))) {
                nextDirection = oppositeDirection(lastDirection);
            }

            if (nextDirection == -1) {
                speedX = 0;
                speedY = 0;
            } else {
                switch (nextDirection) {
                    case 0 -> { speedX = 0; speedY = -speed; } // UP
                    case 1 -> { speedX = 0; speedY = speed; }  // DOWN
                    case 2 -> { speedX = -speed; speedY = 0; } // LEFT
                    case 3 -> { speedX = speed; speedY = 0; }  // RIGHT
                }
                lastDirection = nextDirection;
            }
        }

        // Move smoothly without snapping mid-tile
        x += speedX;
        y += speedY;
    }

    private boolean canMove(int row, int col, int direction) {
        int[] dr = {-1, 1, 0, 0};
        int[] dc = {0, 0, -1, 1};
        int nr = row + dr[direction];
        int nc = col + dc[direction];
        return nr >= 0 && nr < game.ROWS && nc >= 0 && nc < game.COLS && game.map[nr][nc] == 0;
    }

    private int oppositeDirection(int dir) {
        return switch (dir) {
            case 0 -> 1; // UP -> DOWN
            case 1 -> 0; // DOWN -> UP
            case 2 -> 3; // LEFT -> RIGHT
            case 3 -> 2; // RIGHT -> LEFT
            default -> -1;
        };
    }

    private int getRandomDirection(int row, int col) {
        List<Integer> possible = new ArrayList<>();
        int[] dr = {-1, 1, 0, 0};
        int[] dc = {0, 0, -1, 1};

        for (int d = 0; d < 4; d++) {
            int nr = row + dr[d];
            int nc = col + dc[d];
            if (nr >= 0 && nr < game.ROWS && nc >= 0 && nc < game.COLS && game.map[nr][nc] == 0) {
                possible.add(d);
            }
        }

        if (possible.isEmpty()) return -1;
        return possible.get(new Random().nextInt(possible.size()));
    }

    public void draw(GraphicsContext gc) {
        if (ghostImage != null) {
            gc.drawImage(ghostImage, x - size / 2, y - size / 2, size, size);
        } else {
            gc.setFill(state == GhostState.FRIGHTENED ? Color.DARKBLUE : color);
            gc.fillOval(x - size / 2, y - size / 2, size, size);
        }
    }

    public boolean checkPacmanCollision(double pacX, double pacY) {
        double dx = x - pacX;
        double dy = y - pacY;
        double pacmanRadius = 11.0;
        double ghostRadius = size / 2.0;
        return Math.sqrt(dx * dx + dy * dy) < (pacmanRadius + ghostRadius - 5);
    }

    private int bfsDirection() {
        int startRow = (int) (y / game.TILE_SIZE);
        int startCol = (int) (x / game.TILE_SIZE);

        if (startRow == targetRow && startCol == targetCol) {
            return -1;
        }

        boolean[][] visited = new boolean[game.ROWS][game.COLS];
        int[][] firstMove = new int[game.ROWS][game.COLS];
        for (int[] row : firstMove) {
            Arrays.fill(row, -1);
        }

        Queue<int[]> queue = new LinkedList<>();
        queue.add(new int[]{startRow, startCol});
        visited[startRow][startCol] = true;

        int[] dr = {-1, 1, 0, 0};
        int[] dc = {0, 0, -1, 1};

        while (!queue.isEmpty()) {
            int[] current = queue.poll();
            int r = current[0], c = current[1];

            for (int d = 0; d < 4; d++) {
                int nr = r + dr[d];
                int nc = c + dc[d];

                if (nr >= 0 && nr < game.ROWS && nc >= 0 && nc < game.COLS &&
                        !visited[nr][nc] && game.map[nr][nc] == 0) {

                    visited[nr][nc] = true;
                    queue.add(new int[]{nr, nc});

                    if (r == startRow && c == startCol) {
                        firstMove[nr][nc] = d;
                    } else {
                        firstMove[nr][nc] = firstMove[r][c];
                    }

                    if (nr == targetRow && nc == targetCol) {
                        return firstMove[nr][nc];
                    }
                }
            }
        }

        return -1;
    }


    public double getX() { return x; }
    public double getY() { return y; }
}
