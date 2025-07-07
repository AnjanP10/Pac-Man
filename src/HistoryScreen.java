import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class HistoryScreen {

    public static void show(Stage stage) {
        TableView<GameResult> table = new TableView<>();
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        TableColumn<GameResult, Integer> scoreCol = new TableColumn<>("Score");
        scoreCol.setCellValueFactory(new PropertyValueFactory<>("score"));

        TableColumn<GameResult, Integer> timeCol = new TableColumn<>("Time (s)");
        timeCol.setCellValueFactory(new PropertyValueFactory<>("timeTaken"));

        TableColumn<GameResult, String> dateCol = new TableColumn<>("Played At");
        dateCol.setCellValueFactory(new PropertyValueFactory<>("playedAt"));

        table.getColumns().addAll(scoreCol, timeCol, dateCol);

        ObservableList<GameResult> data = FXCollections.observableArrayList(getResultsForUser(Session.currentUser));
        table.setItems(data);

        Label title = new Label("Game History - " + Session.currentUser);
        title.setStyle("-fx-font-size: 20px; -fx-text-fill: rgba(207,159,64,0.94); -fx-font-weight: bold;");

        // Load back icon
        Image backIcon = new Image(HistoryScreen.class.getResourceAsStream("/icons/exit.png"), 16, 16, true, true);

        Button backBtn = new Button("Back to Menu");
        backBtn.setGraphic(new ImageView(backIcon));
        backBtn.setStyle("-fx-background-color: rgba(207,159,64,0.94); -fx-text-fill: rgb(56,64,152); -fx-font-size: 14px; -fx-font-weight: bold;");
        backBtn.setOnAction(e -> MenuScreen.show(stage));

        VBox top = new VBox(10, title);
        top.setPadding(new Insets(20, 0, 10, 0));
        top.setAlignment(Pos.CENTER);

        VBox bottom = new VBox(backBtn);
        bottom.setAlignment(Pos.CENTER);
        bottom.setPadding(new Insets(20));

        BorderPane root = new BorderPane();
        root.setTop(top);
        root.setCenter(table);
        root.setBottom(bottom);
        root.setPadding(new Insets(20));
        root.setStyle(
                "-fx-background-color: rgb(56,64,152); " +
                        "-fx-border-color: rgba(207,159,64,0.94); " +
                        "-fx-border-width: 2px;"
        );

        Scene scene = new Scene(root, 500, 400);
        stage.setScene(scene);
        stage.setTitle("Game History");
        stage.show();
    }

    private static List<GameResult> getResultsForUser(String username) {
        List<GameResult> results = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection()) {
            PreparedStatement stmt = conn.prepareStatement(
                    "SELECT score, time_taken, played_at FROM game_results WHERE username = ? ORDER BY played_at DESC"
            );
            stmt.setString(1, username);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                results.add(new GameResult(
                        rs.getInt("score"),
                        rs.getInt("time_taken"),
                        rs.getString("played_at")
                ));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return results;
    }

    public static class GameResult {
        private final int score;
        private final int timeTaken;
        private final String playedAt;

        public GameResult(int score, int timeTaken, String playedAt) {
            this.score = score;
            this.timeTaken = timeTaken;
            this.playedAt = playedAt;
        }

        public int getScore() {
            return score;
        }

        public int getTimeTaken() {
            return timeTaken;
        }

        public String getPlayedAt() {
            return playedAt;
        }
    }
}
