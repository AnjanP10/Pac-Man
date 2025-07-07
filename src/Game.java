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
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.HashSet;
import java.util.Set;

public class Game extends Application {

    public enum GameState {
        RUNNING,
        PAUSED,
        GAME_OVER
    }

    public final int TILE_SIZE = 32;
    public final int ROWS = 15;
    public final int COLS = 20;

    private long startTime;
    private long pauseStartTime = 0;
    private long totalPausedDuration = 0;
    private int elapsedSeconds;
    private int score = 0;

    private GameState gameState = GameState.RUNNING;
    private Label pauseLabel;
    private Rectangle pauseOverlay;
    private Button pauseResumeButton;

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

        for (int r = 0; r < ROWS; r++)
            for (int c = 0; c < COLS; c++)
                if (map[r][c] == 0)
                    pellets.add(r + "," + c);

        // Exit Button
        Button exitButton = new Button("Exit");
        exitButton.setStyle(
                "-fx-background-color: rgba(100,149,237,0.94); " +
                        "-fx-text-fill: white; " +
                        "-fx-font-weight: bold; " +
                        "-fx-font-size: 14px; " +
                        "-fx-cursor: hand;"
        );
        exitButton.setOnAction(e -> {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Confirm Exit");
            alert.setHeaderText(null);
            alert.setContentText("Are you sure you want to exit?");
            alert.showAndWait().ifPresent(response -> {
                if (response == ButtonType.OK) {
                    try {
                        stop();
                        LoginScreen.show(stage);
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }
            });
        });

        // Pause/Resume Button
        pauseResumeButton = new Button("Pause");
        pauseResumeButton.setStyle(
                "-fx-background-color: rgba(100,149,237,0.94); " +
                        "-fx-text-fill: white; " +
                        "-fx-font-weight: bold; " +
                        "-fx-font-size: 14px; " +
                        "-fx-cursor: hand;"
        );
        pauseResumeButton.setOnAction(e -> togglePause());

        // Top bar with Exit and Pause/Resume
        HBox topBar = new HBox(10, pauseResumeButton, exitButton);
        topBar.setAlignment(Pos.TOP_RIGHT);
        topBar.setPadding(new Insets(6));

        // Pause overlay
        pauseOverlay = new Rectangle(canvas.getWidth(), canvas.getHeight());
        pauseOverlay.setFill(Color.rgb(0,0,0,0.5));
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
        startTime = System.currentTimeMillis();

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

        new AnimationTimer() {
            @Override
            public void handle(long now) {
                if (gameState == GameState.PAUSED) return;

                pacman.update(Game.this);

                gc.setFill(Color.BLACK);
                gc.fillRect(0,0,canvas.getWidth(),canvas.getHeight());

                for (int r=0;r<ROWS;r++)
                    for (int c=0;c<COLS;c++)
                        if (map[r][c]==1) {
                            double x=c*TILE_SIZE, y=r*TILE_SIZE;
                            gc.setFill(Color.DARKBLUE);
                            gc.fillRect(x,y,TILE_SIZE,TILE_SIZE);
                            gc.setStroke(Color.BLUE);
                            gc.setLineWidth(2);
                            gc.strokeRect(x,y,TILE_SIZE,TILE_SIZE);
                        }

                gc.setFill(Color.YELLOW);
                Set<String> eaten=new HashSet<>();
                for(String p:pellets){
                    String[] parts=p.split(",");
                    int pr=Integer.parseInt(parts[0]);
                    int pc=Integer.parseInt(parts[1]);
                    double px=pc*TILE_SIZE+TILE_SIZE/2.0;
                    double py=pr*TILE_SIZE+TILE_SIZE/2.0;
                    if(pacman.checkPelletCollision(px,py)){
                        eaten.add(p);
                        score+=10;
                    }else{
                        gc.fillOval(px-4,py-4,8,8);
                    }
                }
                pellets.removeAll(eaten);

                elapsedSeconds=(int)((System.currentTimeMillis()-startTime-totalPausedDuration)/1000);

                gc.setFont(javafx.scene.text.Font.font(20));
                gc.fillText("Score: "+score,10,25);
                gc.fillText("Time: "+elapsedSeconds+"s",100,25);

                if(pellets.isEmpty()){
                    saveScore();
                    stop();
                    showGameOver(stage);
                }
                pacman.draw(gc);
            }
        }.start();
    }

    private void togglePause(){
        if (gameState == GameState.RUNNING){
            pauseGame();
        } else if (gameState == GameState.PAUSED){
            resumeGame();
        }
    }

    private void pauseGame(){
        if (gameState != GameState.RUNNING) return;
        gameState = GameState.PAUSED;
        pauseStartTime = System.currentTimeMillis();
        pauseOverlay.setVisible(true);
        pauseLabel.setVisible(true);
        pauseResumeButton.setText("Resume");
    }

    private void resumeGame(){
        if (gameState != GameState.PAUSED) return;
        totalPausedDuration += System.currentTimeMillis()-pauseStartTime;
        gameState = GameState.RUNNING;
        pauseOverlay.setVisible(false);
        pauseLabel.setVisible(false);
        pauseResumeButton.setText("Pause");
    }

    public void saveScore(){
        if(Session.currentUser==null)return;
        try(Connection conn=DatabaseConnection.getConnection()){
            PreparedStatement stmt=conn.prepareStatement(
                    "INSERT INTO game_results (username,score,time_taken) VALUES (?,?,?)"
            );
            stmt.setString(1,Session.currentUser);
            stmt.setInt(2,score);
            stmt.setInt(3,elapsedSeconds);
            stmt.executeUpdate();
        }catch(Exception e){ e.printStackTrace();}
    }

    private void showGameOver(Stage stage){
        Label msg=new Label("You won!\nYour score: "+score+"\nYour time: "+elapsedSeconds+" seconds");
        msg.setStyle("-fx-font-size:18px;-fx-text-fill:rgba(207,159,64,0.94)");

        Button backBtn=new Button("Back to Login");
        Button playAgainBtn=new Button("Play Again");
        Button exitBtn=new Button("Exit");

        backBtn.setOnAction(e->LoginScreen.show(stage));
        playAgainBtn.setOnAction(e->{
            Game newGame=new Game();
            newGame.start(stage);
        });
        exitBtn.setOnAction(e->stage.close());

        HBox buttons=new HBox(15,backBtn,playAgainBtn,exitBtn);
        buttons.setAlignment(Pos.CENTER);

        VBox root=new VBox(20,msg,buttons);
        root.setAlignment(Pos.CENTER);
        root.setPadding(new Insets(20));
        root.setStyle("-fx-background-color:rgb(56,64,152);");

        stage.setScene(new Scene(root,400,220));
    }
}
