import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import java.util.*;

public class Ghost {

    public double getX() {
        return x + size / 2.0;  // Center X position of ghost
    }

    public double getY() {
        return y + size / 2.0;  // Center Y position of ghost
    }

    private double x, y;
    private final double size = 22;
    private final double speed = 1.8;
    private double speedX = 0, speedY = 0;
    private Color color;
    private GhostState state;
    private int targetRow, targetCol;
    private int scatterTargetRow, scatterTargetCol;

    private final Game game;

    public Ghost(double startX, double startY, Color color, int scatterRow, int scatterCol, Game game) {
        this.x = startX;
        this.y = startY;
        this.color = color;
        this.state = GhostState.SCATTER;
        this.scatterTargetRow = scatterRow;
        this.scatterTargetCol = scatterCol;
        this.game = game;

        // Initially stop moving so we set direction when aligned
        this.speedX = 0;
        this.speedY = 0;
    }

    public void setState(GhostState newState) {
        this.state = newState;
    }

    public void update(double pacX, double pacY, String pacDirection, double blinkyX, double blinkyY) {
        // Calculate tile center coordinates ghost is currently near
        int currentRow = (int)(y / game.TILE_SIZE);
        int currentCol = (int)(x / game.TILE_SIZE);
        double tileCenterX = currentCol * game.TILE_SIZE + game.TILE_SIZE / 2.0;
        double tileCenterY = currentRow * game.TILE_SIZE + game.TILE_SIZE / 2.0;

        // Check if ghost is close enough to tile center (within speed threshold)
        boolean nearTileCenterX = Math.abs(x - tileCenterX) < speed;
        boolean nearTileCenterY = Math.abs(y - tileCenterY) < speed;

        if (nearTileCenterX && nearTileCenterY) {
            // Snap to tile center
            x = tileCenterX;
            y = tileCenterY;

            // Update target based on state
            switch (state) {
                case SCATTER -> {
                    targetRow = scatterTargetRow;
                    targetCol = scatterTargetCol;
                }
                case CHASE -> {
                    if (color.equals(Color.RED)) { // Blinky
                        targetRow = (int)(pacY / game.TILE_SIZE);
                        targetCol = (int)(pacX / game.TILE_SIZE);
                    } else if (color.equals(Color.PINK)) { // Pinky
                        int aheadRow = (int)(pacY / game.TILE_SIZE);
                        int aheadCol = (int)(pacX / game.TILE_SIZE);
                        if ("UP".equals(pacDirection)) aheadRow -= 4;
                        else if ("DOWN".equals(pacDirection)) aheadRow += 4;
                        else if ("LEFT".equals(pacDirection)) aheadCol -= 4;
                        else if ("RIGHT".equals(pacDirection)) aheadCol += 4;
                        targetRow = aheadRow;
                        targetCol = aheadCol;
                    } else if (color.equals(Color.CYAN)) { // Inky
                        int pacRow = (int)(pacY / game.TILE_SIZE);
                        int pacCol = (int)(pacX / game.TILE_SIZE);
                        int vectorRow = pacRow - (int)(blinkyY / game.TILE_SIZE);
                        int vectorCol = pacCol - (int)(blinkyX / game.TILE_SIZE);
                        targetRow = pacRow + vectorRow;
                        targetCol = pacCol + vectorCol;
                    } else if (color.equals(Color.ORANGE)) { // Clyde
                        double dist = Math.hypot(pacX - x, pacY - y);
                        if (dist > 160) {
                            targetRow = (int)(pacY / game.TILE_SIZE);
                            targetCol = (int)(pacX / game.TILE_SIZE);
                        } else {
                            targetRow = scatterTargetRow;
                            targetCol = scatterTargetCol;
                        }
                    }
                }
                case FRIGHTENED -> {
                    targetRow = new Random().nextInt(game.ROWS);
                    targetCol = new Random().nextInt(game.COLS);
                }
            }

            // Find next direction from BFS pathfinding (0=UP,1=DOWN,2=LEFT,3=RIGHT)
            int nextDirection = bfsDirection();

            // Set speed based on direction
            switch (nextDirection) {
                case 0 -> { speedX = 0; speedY = -speed; }  // UP
                case 1 -> { speedX = 0; speedY = speed; }   // DOWN
                case 2 -> { speedX = -speed; speedY = 0; }  // LEFT
                case 3 -> { speedX = speed; speedY = 0; }   // RIGHT
                default -> { speedX = 0; speedY = 0; }      // No move
            }
        }

        // Move ghost smoothly in current direction
        x += speedX;
        y += speedY;
    }

    public void draw(GraphicsContext gc) {
        gc.setFill(state == GhostState.FRIGHTENED ? Color.DARKBLUE : color);
        gc.fillOval(x - size/2, y - size/2, size, size);
    }

    public boolean checkPacmanCollision(double pacX, double pacY) {
        double dx = x - pacX;
        double dy = y - pacY;
        return Math.sqrt(dx*dx + dy*dy) < 16;
    }

    private int bfsDirection() {
        int startRow = (int)(y / game.TILE_SIZE);
        int startCol = (int)(x / game.TILE_SIZE);

        boolean[][] visited = new boolean[game.ROWS][game.COLS];
        int[][] parentDir = new int[game.ROWS][game.COLS];
        for (int i = 0; i < game.ROWS; i++) {
            Arrays.fill(parentDir[i], -1);
        }

        Queue<int[]> queue = new LinkedList<>();
        queue.add(new int[]{startRow, startCol});
        visited[startRow][startCol] = true;

        int[] dr = {-1, 1, 0, 0}; // UP, DOWN, LEFT, RIGHT
        int[] dc = {0, 0, -1, 1};

        boolean pathFound = false;

        while (!queue.isEmpty()) {
            int[] curr = queue.poll();
            if (curr[0] == targetRow && curr[1] == targetCol) {
                pathFound = true;
                break;
            }

            for (int d = 0; d < 4; d++) {
                int nr = curr[0] + dr[d];
                int nc = curr[1] + dc[d];
                if (nr >= 0 && nr < game.ROWS && nc >= 0 && nc < game.COLS
                        && !visited[nr][nc] && game.map[nr][nc] == 0) {
                    queue.add(new int[]{nr, nc});
                    visited[nr][nc] = true;
                    parentDir[nr][nc] = d;
                }
            }
        }

        if (!pathFound) return -1;

        int r = targetRow;
        int c = targetCol;

        while (!(r == startRow && c == startCol)) {
            int d = parentDir[r][c];
            int prevR = r - dr[d];
            int prevC = c - dc[d];

            if (prevR == startRow && prevC == startCol) {
                return d;
            }

            r = prevR;
            c = prevC;
        }
        return -1;
    }
}
