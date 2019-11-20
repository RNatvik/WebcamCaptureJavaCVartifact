package GUI;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import java.io.IOException;

/**
 * This is the main class of the gui-side. When started, it initializes the shared resource and launch the application.
 * After start is done, The controllers takes command over the fxml files.
 * When application is closed, the shared resource is cleared and all scenes is closed.
 *
 * @version 1.0
 * @since 20.11.2019
 */
public class App extends Application {

    public static void main (String[] args) throws Exception {
        if (!SharedResource.isInitialized()) {
            SharedResource.initialize(30);
        }
        launch(args);
        SharedResource.clear();
    }

    /**
     * This method is called automatically by JavaFX when the application is
     * launched
     *
     * @param primaryStage The main "stage" where the GUI will be rendered
     * @throws Exception
     */

    @Override
    public void start(Stage primaryStage) throws IOException{
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/GUI.fxml")); // Create path and the FXMLLoader
        Parent root = loader.load();
        Scene scene = new Scene(root);
        primaryStage.setScene(scene); // Set the Scene to the Stage
        primaryStage.setTitle("RC-Car");
        Controller controller = loader.getController();// Close all windows when the main window gets closed
        primaryStage.onCloseRequestProperty().setValue(event -> {
            controller.safeStopSceduledExicuter();
            Platform.exit();
            System.exit(0);
        });
        primaryStage.show();
    }
}