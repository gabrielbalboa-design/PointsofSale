import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class LoginScreen {

    public static Scene createLoginScene(Stage stage){

        Label title = new Label("Food Inventory Login");

        TextField usernameField = new TextField();
        usernameField.setPromptText("Username");

        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("Password");

        Button loginBtn = new Button("Login");

        loginBtn.setOnAction(e -> {

            String username = usernameField.getText();
            String password = passwordField.getText();

            User user = UserManager.login(username, password);

            if(user != null){

                stage.setScene(new Scene(new Dashboard(user.role).getView()));

            } else {

                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setHeaderText("Login Failed");
                alert.setContentText("Invalid username or password.");
                alert.showAndWait();

            }

        });

        VBox layout = new VBox(15,title,usernameField,passwordField,loginBtn);

        return new Scene(layout,300,200);
    }
}
