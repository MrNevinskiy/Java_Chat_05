import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;


public class StartClientUI extends Application {

    public Stage primaryStage;

    @Override
    public void start(Stage primaryStage) throws Exception {

        FXMLLoader loader = new FXMLLoader();
        loader.setLocation(getClass().getResource("sample.fxml"));

        Parent root = loader.load();
        this.primaryStage = primaryStage;
        Scene scene = new Scene(root, primaryStage.getWidth(), primaryStage.getHeight());

        primaryStage.setTitle("Chat");
        primaryStage.setScene(scene);
        primaryStage.setResizable(false);
        primaryStage.show();

        Controller controller = loader.getController();
        primaryStage.setOnCloseRequest(controller.getCloseEventHandler());
    }

    public static void main(String[] args) {
        launch(args);
    }
}
