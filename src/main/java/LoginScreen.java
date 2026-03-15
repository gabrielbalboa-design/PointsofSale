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

            User user = UserManager.login(username,password);

            if(user == null){

                new Alert(Alert.AlertType.ERROR,"Invalid login").show();
                return;

            }

            if(user.firstLogin){

                TextInputDialog dialog = new TextInputDialog();
                dialog.setHeaderText("Change Password");

                dialog.showAndWait().ifPresent(newPass -> {

                    user.password = newPass;
                    user.firstLogin = false;

                    UserManager.saveUsers();
                });

            }

            Dashboard dashboard = new Dashboard(user.role);

            Scene dashboardScene = new Scene(dashboard.getView(),600,400);

            stage.setScene(dashboardScene);

        });

        VBox layout = new VBox(15,title,usernameField,passwordField,loginBtn);

        return new Scene(layout,300,200);
    }
}
