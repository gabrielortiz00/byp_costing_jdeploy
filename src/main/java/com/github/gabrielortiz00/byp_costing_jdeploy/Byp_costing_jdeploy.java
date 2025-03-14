package com.github.gabrielortiz00.byp_costing_jdeploy;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.File;
import java.io.FileNotFoundException;
import java.net.URL;

public class Byp_costing_jdeploy extends Application {
    @Override
    public void start(Stage primaryStage) throws Exception {
        // Use the path we know works now
        URL location = getClass().getResource("/com.github.gabrielortiz00.byp_costing_jdeploy/views/Login.fxml");
        if (location == null) {
            throw new FileNotFoundException("Could not find Login.fxml");
        }

        FXMLLoader loader = new FXMLLoader(location);
        Scene scene = new Scene(loader.load());
        primaryStage.setScene(scene);
        primaryStage.setTitle("BYP Cost Analysis");
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}