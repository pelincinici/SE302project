package edu.ieu.se302.examscheduler;

import javafx.application.Application;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

/**
 * Entry point of the Exam Scheduler desktop application.
 * For now it only opens an empty "hello" window; the real screens
 * will be added under the ui package.
 */
public class App extends Application {

    public static final String APP_TITLE = "Exam Scheduler";

    @Override
    public void start(Stage stage) {
        Label hello = new Label("Hello! Exam Scheduler is running.");
        Label version = new Label("Java " + System.getProperty("java.version")
                + "  |  JavaFX " + System.getProperty("javafx.version"));

        VBox root = new VBox(10, hello, version);
        root.setAlignment(Pos.CENTER);

        stage.setTitle(APP_TITLE);
        stage.setScene(new Scene(root, 640, 400));
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
