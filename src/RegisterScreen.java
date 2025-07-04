import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;

public class RegisterScreen {

    public static void show(Stage stage) {
        Label title = new Label("Register for Pac-Man");
        title.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: rgba(207,159,64,0.94)");

        Label userLabel = new Label("Username:");
        userLabel.setStyle("-fx-text-fill: rgba(207,159,64,0.94)");

        TextField username = new TextField();
        username.setPromptText("Enter username");

        Label passLabel = new Label("Password:");
        passLabel.setStyle("-fx-text-fill: rgba(207,159,64,0.94)");

        PasswordField password = new PasswordField();
        password.setPromptText("Enter password");

        Label confirmLabel = new Label("Confirm Password:");
        confirmLabel.setStyle("-fx-text-fill: rgba(207,159,64,0.94)");

        PasswordField confirmPassword = new PasswordField();
        confirmPassword.setPromptText("Re-enter password");

        Button registerBtn = new Button("Register");
        Button backBtn = new Button("Back");

        Label message = new Label();
        message.setStyle("-fx-text-fill: red;");

        // GridPane layout
        GridPane grid = new GridPane();
        grid.setAlignment(Pos.CENTER);
        grid.setPadding(new Insets(20));
        grid.setHgap(15);
        grid.setVgap(15);
        grid.setStyle("-fx-background-color: rgb(56,64,152); -fx-border-color: rgba(207,159,64,0.94); -fx-border-width: 2px;");

        // Add nodes to grid: (col, row)
        grid.add(title, 0, 0, 2, 1);
        grid.add(userLabel, 0, 1);
        grid.add(username, 1, 1);
        grid.add(passLabel, 0, 2);
        grid.add(password, 1, 2);
        grid.add(confirmLabel, 0, 3);
        grid.add(confirmPassword, 1, 3);
        grid.add(registerBtn, 1, 4);
        grid.add(backBtn, 0, 4);
        grid.add(message, 0, 5, 2, 1);

        // Buttons same width
        registerBtn.setMaxWidth(Double.MAX_VALUE);
        backBtn.setMaxWidth(Double.MAX_VALUE);

        // Action handlers
        registerBtn.setOnAction(e -> {
            String user = username.getText();
            String pass = password.getText();
            String confirm = confirmPassword.getText();

            if (user.isEmpty() || pass.isEmpty() || confirm.isEmpty()) {
                message.setText("All fields are required.");
                return;
            }

            if (!pass.equals(confirm)) {
                message.setText("Passwords do not match!");
                return;
            }

            if (UserDAO.register(new User(user, pass))) {
                message.setStyle("-fx-text-fill: green;");
                message.setText("Registered successfully. Please login.");
                //registerBtn.setOnAction(actionEvent -> LoginScreen.show(stage));
            } else {
                message.setStyle("-fx-text-fill: red;");
                message.setText("Username already exists.");
            }
        });

        backBtn.setOnAction(e -> LoginScreen.show(stage));

        stage.setScene(new Scene(grid, 450, 400));
        stage.setTitle("Pac-Man Registration");
        stage.show();
    }
}
