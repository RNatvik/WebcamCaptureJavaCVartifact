package GUI;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;


public class App extends Application {

    public static void main (String[] args) throws Exception {
        if (!SharedResource.isInitialized()) {
            SharedResource.initialize("127.0.0.1", 9876, 2345, 30);
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

        // Create the FXMLLoader
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/GUI.fxml"));
        System.out.println("AFter loader: " + loader);
        // Path to the FXML File

        // Create the Pane and all Details
        Parent root = loader.load();
        // Create the Scene
        Scene scene = new Scene(root);
        // Set the Scene to the Stage
        primaryStage.setScene(scene);
        // Set the Title to the Stage
        primaryStage.setTitle("RC-Car");
        // Close all windows when the main window gets closed
        primaryStage.onCloseRequestProperty().setValue(e -> Platform.exit());
        //



        // Display the Stage
        primaryStage.show();
    }
}