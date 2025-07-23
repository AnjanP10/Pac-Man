import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;

public class LoginScreen {

    public static void show(Stage stage) {
        javafx.scene.image.Image loginIcon = new javafx.scene.image.Image(
                LoginScreen.class.getResourceAsStream("/icons/login.png"),
                16, 16, true, true
        );
        javafx.scene.image.Image registerIcon = new javafx.scene.image.Image(
                LoginScreen.class.getResourceAsStream("/icons/login.png"),
                16, 16, true, true
        );

        Label title = new Label("Login to Anjan");
        title.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: rgba(207,159,64,0.94)");

        Label userLabel = new Label("Username:");
        userLabel.setStyle("-fx-text-fill: rgba(207,159,64,0.94)");
        TextField username = new TextField();
        username.setPromptText("Enter username");

        Label passLabel = new Label("Password:");
        passLabel.setStyle("-fx-text-fill: rgb(207,159,64)");
        PasswordField password = new PasswordField();
        password.setPromptText("Enter password");

        Button loginBtn = new Button("Login");
        Button registerBtn = new Button("Register");

        Label message = new Label();
        message.setStyle("-fx-text-fill: red;");

        GridPane grid = new GridPane();
        grid.setAlignment(Pos.CENTER);
        grid.setPadding(new Insets(20));
        grid.setHgap(15);
        grid.setVgap(15);
        grid.setStyle("-fx-background-color: rgb(56,64,152); -fx-border-color: rgba(207,159,64,0.94); -fx-border-width: 2px;");

        grid.add(title, 0, 0, 2, 1); // span 2 columns
        grid.add(userLabel, 0, 1);
        grid.add(username, 1, 1);
        grid.add(passLabel, 0, 2);
        grid.add(password, 1, 2);
        grid.add(loginBtn, 1, 3);
        grid.add(registerBtn, 0, 3);
        grid.add(message, 0, 4, 2, 1);

        loginBtn.setMaxWidth(Double.MAX_VALUE);
        loginBtn.setGraphic(new javafx.scene.image.ImageView(loginIcon));
        registerBtn.setMaxWidth(Double.MAX_VALUE);
        registerBtn.setGraphic(new javafx.scene.image.ImageView(registerIcon));

        loginBtn.setOnAction(e -> {
            if (username.getText().isEmpty() || password.getText().isEmpty()) {
                message.setText("All fields required!");
                return;
            }
            if (UserDAO.login(username.getText(), password.getText())) {
                Session.currentUser = username.getText();
                message.setStyle("-fx-text-fill: green;");
                message.setText("Login Success!");
                MenuScreen.show(stage);
            } else {
                message.setStyle("-fx-text-fill: red;");
                message.setText("Invalid credentials!");
            }
        });

        registerBtn.setOnAction(e -> RegisterScreen.show(stage));

        stage.setScene(new Scene(grid, 400, 350));
        stage.setTitle("Pac-Man Login");
        stage.show();
    }
}
