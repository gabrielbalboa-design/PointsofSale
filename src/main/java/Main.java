import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.animation.PauseTransition;
import javafx.util.Duration;

public class Main extends Application {

    @Override
    public void start(Stage stage){

        Scene loginScene = LoginScreen.createLoginScene(stage);

        stage.setTitle("Food Inventory System");
        stage.setScene(loginScene);
        stage.show();

        PauseTransition timeout = new PauseTransition(Duration.minutes(5));

        timeout.setOnFinished(e -> {

            System.out.println("Session expired.");

            stage.close();

        });

        loginScene.setOnMouseMoved(e -> timeout.playFromStart());
        loginScene.setOnMouseClicked(e -> timeout.playFromStart());
        loginScene.setOnKeyPressed(e -> timeout.playFromStart());

        timeout.play();
    }

    public static void main(String[] args) {
        launch();
    }
}
