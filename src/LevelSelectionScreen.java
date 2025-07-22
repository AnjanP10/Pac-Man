import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class LevelSelectionScreen {

    public static void show(Stage stage) {
        Label title = new Label("Select Level");
        title.setStyle("-fx-font-size:24px;-fx-text-fill:#CF9F40;-fx-font-weight:bold;");

        Button level1Btn = new Button("Level 1");
        Button level2Btn = new Button("Level 2");
        Button level3Btn = new Button("Level 3");
        Button backBtn = new Button("Back");

        level1Btn.setPrefWidth(200);
        level2Btn.setPrefWidth(200);
        level3Btn.setPrefWidth(200);
        backBtn.setPrefWidth(200);

        level1Btn.setStyle("-fx-background-color:#384098; -fx-text-fill:white; -fx-font-size:16px;");
        level2Btn.setStyle("-fx-background-color:#384098; -fx-text-fill:white; -fx-font-size:16px;");
        level3Btn.setStyle("-fx-background-color:#384098; -fx-text-fill:white; -fx-font-size:16px;");
        backBtn.setStyle("-fx-background-color:#d9534f; -fx-text-fill:white; -fx-font-size:16px;");

        level1Btn.setOnAction(e -> new Game(0).show(stage));
        level2Btn.setOnAction(e -> new Game(1).show(stage));
        level3Btn.setOnAction(e -> new Game(2).show(stage));
        backBtn.setOnAction(e -> MenuScreen.show(stage));

        VBox root = new VBox(20, title, level1Btn, level2Btn, level3Btn, backBtn);
        root.setAlignment(Pos.CENTER);
        root.setPadding(new Insets(30));
        root.setStyle("-fx-background-color:#282c34;");

        Scene scene = new Scene(root, 400, 300);
        stage.setScene(scene);
        stage.setTitle("Select Level");
        stage.show();
    }
}

