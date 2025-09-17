import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import java.util.*;

public class Ghost {

    public enum GhostType { BLINKY, PINKY, INKY, CLYDE }
    public enum GhostState { SCATTER, CHASE, FRIGHTENED }

    private double x, y;
    private final double size = 40;
    private final double speed = 1.5;  // Increased speed for visibility
    private double speedX = 0, speedY = 0;

    private Color color;
    private GhostState state;
    private int targetRow, targetCol;
    private int scatterTargetRow, scatterTargetCol;

    private final Game game;
    private Image ghostImage;
    private GhostType type;

    private int lastDirection = -1;
    private long stateStartTime; // For scatter/chase timing

    private static final Random random = new Random();

    public Ghost(double startX, double startY, Color color, int scatterRow, int scatterCol, Game game, String imagePath, GhostType type) {
        this.x = startX + game.TILE_SIZE / 2.0;
        this.y = startY + game.TILE_SIZE / 2.0;
        this.color = color;
        this.state = GhostState.CHASE;  // Force CHASE for debug
        this.scatterTargetRow = scatterRow;
        this.scatterTargetCol = scatterCol;
        this.game = game;
        this.type = type;
        this.stateStartTime = System.currentTimeMillis();

        int row = (int) (y / game.TILE_SIZE);
        int col = (int) (x / game.TILE_SIZE);
        if (game.map[row][col] != 0) {
            System.out.println(type + " spawned in wall at (" + row + "," + col + ")");
        }

        try {
            var url = getClass().getResource(imagePath);
            if (url != null) {
                ghostImage = new Image(url.toExternalForm());
            } else {
                ghostImage = new Image("file:" + imagePath);
            }

            if (ghostImage.getWidth() == 0) {
                ghostImage = null;
                System.out.println(type + " ghost image failed to load.");
            }
        } catch (Exception e) {
            ghostImage = null;
            System.out.println(type + " ghost image failed to load with exception: " + e.getMessage());
        }
    }

    public void update(double pacX, double pacY, String pacDirection, double blinkyX, double blinkyY) {
        long elapsed = System.currentTimeMillis() - stateStartTime;
        if (state == GhostState.SCATTER && elapsed >= 7000) {
            setState(GhostState.CHASE);
        } else if (state == GhostState.CHASE && elapsed >= 20000) {
            setState(GhostState.SCATTER);
        }

        int currentRow = (int) (y / game.TILE_SIZE);
        int currentCol = (int) (x / game.TILE_SIZE);

        double tileCenterX = currentCol * game.TILE_SIZE + game.TILE_SIZE / 2.0;
        double tileCenterY = currentRow * game.TILE_SIZE + game.TILE_SIZE / 2.0;

        // Improved atCenter check
        boolean atCenter = Math.hypot(x - tileCenterX, y - tileCenterY) < 1.5;

        if (atCenter) {
            x = tileCenterX;
            y = tileCenterY;
        } else {
            // Continue moving in current direction
            x += speedX;
            y += speedY;
            return;
        }

        // Set target based on state and ghost type  VECTOR MATH- INKY TARGETING
        switch (state) {
            case SCATTER -> {
                targetRow = scatterTargetRow;
                targetCol = scatterTargetCol;
            }
            case CHASE -> {
                switch (type) {
                    case BLINKY -> {
                        targetRow = (int) (pacY / game.TILE_SIZE);
                        targetCol = (int) (pacX / game.TILE_SIZE);
                    }
                    case PINKY -> {
                        int offset = 4;
                        int pacRow = (int) (pacY / game.TILE_SIZE);
                        int pacCol = (int) (pacX / game.TILE_SIZE);
                        switch (pacDirection) {
                            case "UP" -> pacRow -= offset;
                            case "DOWN" -> pacRow += offset;
                            case "LEFT" -> pacCol -= offset;
                            case "RIGHT" -> pacCol += offset;
                        }
                        targetRow = clamp(pacRow, 0, game.ROWS - 1);
                        targetCol = clamp(pacCol, 0, game.COLS - 1);
                    }
                    case INKY -> {
                        int pacRow = (int) (pacY / game.TILE_SIZE);
                        int pacCol = (int) (pacX / game.TILE_SIZE);
                        int aheadRow = pacRow;
                        int aheadCol = pacCol;
                        switch (pacDirection) {
                            case "UP" -> aheadRow -= 2;
                            case "DOWN" -> aheadRow += 2;
                            case "LEFT" -> aheadCol -= 2;
                            case "RIGHT" -> aheadCol += 2;
                        }
                        int blinkyRow = (int) (blinkyY / game.TILE_SIZE);
                        int blinkyCol = (int) (blinkyX / game.TILE_SIZE);
                        int vecRow = aheadRow - blinkyRow;
                        int vecCol = aheadCol - blinkyCol;
                        targetRow = clamp(blinkyRow + vecRow * 2, 0, game.ROWS - 1);
                        targetCol = clamp(blinkyCol + vecCol * 2, 0, game.COLS - 1);
                    }
                    // Manhattan Distance – Clyde’s Scatter Logic
                    case CLYDE -> {
                        int pacRow = (int) (pacY / game.TILE_SIZE);
                        int pacCol = (int) (pacX / game.TILE_SIZE);
                        int r = (int) (y / game.TILE_SIZE);
                        int c = (int) (x / game.TILE_SIZE);
                        int dist = Math.abs(pacRow - r) + Math.abs(pacCol - c);
                        if (dist > 8) {
                            targetRow = pacRow;
                            targetCol = pacCol;
                        } else {
                            targetRow = scatterTargetRow;
                            targetCol = scatterTargetCol;
                        }
                    }
                }
            }
            case FRIGHTENED -> {
                targetRow = random.nextInt(game.ROWS);
                targetCol = random.nextInt(game.COLS);
            }
        }

        if (type == GhostType.BLINKY) {
            System.out.printf("Blinky: current tile (%d,%d), target tile (%d,%d), pacman (%.2f, %.2f)%n",
                    currentRow, currentCol, targetRow, targetCol, pacX, pacY);
        }

        int nextDirection = bfsDirection();

        if (nextDirection == -1) {
            System.out.println(type + " BFS returned -1");
        }

        if (nextDirection == -1 || !canMove(currentRow, currentCol, nextDirection)) {
            nextDirection = getRandomDirection(currentRow, currentCol);
            if (nextDirection != -1) {
                System.out.println(type + " picked random direction: " + nextDirection);
            }
        }

        if (nextDirection == -1 && lastDirection != -1 && canMove(currentRow, currentCol, oppositeDirection(lastDirection))) {
            nextDirection = oppositeDirection(lastDirection);
            System.out.println(type + " fallback to opposite direction: " + nextDirection);
        }

        // Fixed fallback to prevent getting stuck
        if (nextDirection == -1) {
            if (lastDirection != -1 && canMove(currentRow, currentCol, lastDirection)) {
                nextDirection = lastDirection;
                System.out.println(type + " fallback to last direction: " + nextDirection);
            } else {
                nextDirection = getRandomDirection(currentRow, currentCol);
                System.out.println(type + " last fallback random direction: " + nextDirection);
            }
        }

        if (nextDirection == -1) {
            System.out.println(type + " no available moves, keeping speed");
        } else {
            switch (nextDirection) {
                case 0 -> { speedX = 0; speedY = -speed; }
                case 1 -> { speedX = 0; speedY = speed; }
                case 2 -> { speedX = -speed; speedY = 0; }
                case 3 -> { speedX = speed; speedY = 0; }
            }
            lastDirection = nextDirection;
        }

        x += speedX;
        y += speedY;
    }

    private int clamp(int v, int min, int max) {
        return Math.max(min, Math.min(max, v));
    }

    public void setState(GhostState newState) {
        this.state = newState;
        this.stateStartTime = System.currentTimeMillis();
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
            case 0 -> 1;
            case 1 -> 0;
            case 2 -> 3;
            case 3 -> 2;
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
        return possible.get(random.nextInt(possible.size()));
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

        if (startRow == targetRow && startCol == targetCol) return -1;

        boolean[][] visited = new boolean[game.ROWS][game.COLS];
        int[][] firstMove = new int[game.ROWS][game.COLS];
        for (int[] row : firstMove) Arrays.fill(row, -1);

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

                if (nr < 0 || nr >= game.ROWS || nc < 0 || nc >= game.COLS) continue;
                if (visited[nr][nc] || game.map[nr][nc] != 0) continue;
                visited[nr][nc] = true;
                queue.add(new int[]{nr, nc});
                firstMove[nr][nc] = (r == startRow && c == startCol) ? d : firstMove[r][c];

                if (nr == targetRow && nc == targetCol) {
                    return firstMove[nr][nc];
                }
            }
        }
        return -1;
    }

    public double getX() { return x; }
    public double getY() { return y; }
}
