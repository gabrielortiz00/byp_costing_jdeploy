package com.github.gabrielortiz00.byp_costing_jdeploy;

import com.github.gabrielortiz00.byp_costing_jdeploy.controllers.ResultsController;
import com.github.gabrielortiz00.byp_costing_jdeploy.controllers.WelcomeController;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.scene.Node;
import java.io.IOException;
import java.net.URL;

public class SceneChanger {

    public static void changeScene(Node node, String fxmlFile) {
        try {
            Stage stage = (Stage) node.getScene().getWindow();
            URL fxmlUrl;

            // Direct use of the provided path if it starts with "/"
            if (fxmlFile.startsWith("/")) {
                fxmlUrl = SceneChanger.class.getResource(fxmlFile);
            } else {
                // Otherwise, add the views directory prefix
                fxmlUrl = SceneChanger.class.getResource("/com.github.gabrielortiz00.byp_costing_jdeploy/views/" + fxmlFile);
            }

            if (fxmlUrl == null) {
                throw new RuntimeException("FXML file not found: " + fxmlFile);
            }

            FXMLLoader loader = new FXMLLoader(fxmlUrl);
            Parent root = loader.load();

            if (fxmlFile.endsWith("Welcome.fxml") || fxmlFile.endsWith("/Welcome.fxml")) {
                WelcomeController controller = loader.getController();
                controller.refreshProjects();
            }

            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void changeSceneWithData(Node node, String fxmlFile, Object data) {
        try {
            Stage stage = (Stage) node.getScene().getWindow();
            URL fxmlUrl;

            // Direct use of the provided path if it starts with "/"
            if (fxmlFile.startsWith("/")) {
                fxmlUrl = SceneChanger.class.getResource(fxmlFile);
            } else {
                // Otherwise, add the views directory prefix
                fxmlUrl = SceneChanger.class.getResource("/com.github.gabrielortiz00.byp_costing_jdeploy/views/" + fxmlFile);
            }

            if (fxmlUrl == null) {
                throw new RuntimeException("FXML file not found: " + fxmlFile);
            }

            FXMLLoader loader = new FXMLLoader(fxmlUrl);
            Parent root = loader.load();

            Object controller = loader.getController();

            if (controller instanceof ResultsController && data instanceof ResultsModel) {
                ((ResultsController) controller).setResultsModel((ResultsModel) data);
            }

            if ((fxmlFile.endsWith("Welcome.fxml") || fxmlFile.endsWith("/Welcome.fxml"))
                    && controller instanceof WelcomeController) {
                ((WelcomeController) controller).refreshProjects();
            }

            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}