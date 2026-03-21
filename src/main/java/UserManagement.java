import javafx.scene.control.*;
import javafx.scene.layout.VBox;

public class UserManagement{

    public static VBox createPanel(){

        Label title = new Label("User Management");

        TextField usernameField = new TextField();
        usernameField.setPromptText("Username");

        ComboBox<UserRole> roleBox = new ComboBox<>();
        roleBox.getItems().addAll(UserRole.ADMIN,UserRole.MANAGER,UserRole.STAFF);

        Button createBtn = new Button("Create User");

        createBtn.setOnAction(e -> {

            String username = usernameField.getText();
            UserRole role = roleBox.getValue();

            if(username.isEmpty() || role == null){
                new Alert(Alert.AlertType.ERROR,"Enter username and role").show();
                return;
            }

            String password = UserManager.createUser(username, role);

            if(password == null){
                new Alert(Alert.AlertType.ERROR,"Username already exists!").show();
                return;
            }

            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Account Created");
            alert.setHeaderText("New User Created");

            alert.setContentText(
                    "Username: " + username + "\n" +
                            "Password: " + password
            );

            alert.showAndWait();

            usernameField.clear();
            roleBox.setValue(null);
        });
        VBox layout = new VBox(10,title,usernameField,roleBox,createBtn);

        return layout;

    }

}
