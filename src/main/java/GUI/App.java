package GUI;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Properties;


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
        Controller controller = loader.getController();
        primaryStage.onCloseRequestProperty().setValue(event -> {
            controller.test();
            Platform.exit();
        });
        //



        // Display the Stage
        primaryStage.show();
    }
}