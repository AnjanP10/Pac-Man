import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;

public class MenuScreen {
    public static void show(Stage stage) {
        Label title = new Label("Welcome, " + Session.currentUser + "!");
        title.setStyle("-fx-font-size: 24px; -fx-text-fill: rgba(207,159,64,0.94); -fx-font-weight: bold;");

        Button playBtn = new Button("Play Game");
        Button historyBtn = new Button("Player History");
        Button backBtn = new Button("Back");

        playBtn.setPrefWidth(180);
        historyBtn.setPrefWidth(180);
        backBtn.setPrefWidth(180);

        String btnStyle = "-fx-background-color: rgba(207,159,64,0.94); -fx-text-fill: rgb(56,64,152); -fx-font-size: 16px; -fx-font-weight: bold;";
        playBtn.setStyle(btnStyle);
        historyBtn.setStyle(btnStyle);
        backBtn.setStyle("-fx-background-color: #d9534f; -fx-text-fill: white; -fx-font-size: 16px; -fx-font-weight: bold;");

        playBtn.setOnAction(e -> {
            LevelSelectionScreen.show(stage);
        });

        historyBtn.setOnAction(e -> {
            HistoryScreen.show(stage);
        });

        backBtn.setOnAction(e -> {
            LoginScreen.show(stage);
        });

        GridPane grid = new GridPane();
        grid.setAlignment(Pos.CENTER);
        grid.setPadding(new Insets(20));
        grid.setHgap(15);
        grid.setVgap(20);
        grid.setStyle("-fx-background-color: rgb(56,64,152); -fx-border-color: rgba(207,159,64,0.94); -fx-border-width: 2px;");

        grid.add(title, 0, 0, 2, 1);
        grid.add(playBtn, 0, 1, 2, 1);
        grid.add(historyBtn, 0, 2, 2, 1);
        grid.add(backBtn, 0, 3, 2, 1);

        Scene scene = new Scene(grid, 400, 300);
        stage.setScene(scene);
        stage.setTitle("Main Menu");
        stage.show();
    }
}
