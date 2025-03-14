package com.github.gabrielortiz00.byp_costing_jdeploy.controllers;

import com.github.gabrielortiz00.byp_costing_jdeploy.ResultsModel;
import com.github.gabrielortiz00.byp_costing_jdeploy.SceneChanger;
import com.github.gabrielortiz00.byp_costing_jdeploy.utils.ProjectManager;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import java.text.DecimalFormat;
import java.util.List;

public class WelcomeController {

    @FXML private Label lblHomeScr;
    @FXML private ListView<String> listView;
    @FXML private Button btnOpnProj;
    @FXML private Button btnDelProj;
    @FXML private Button btnNewProj;
    @FXML private Button btnLogOut;


    private ObservableList<String> displayList = FXCollections.observableArrayList(); //displayed list
    private List<ResultsModel> projectModels; //projects list

    private static final DecimalFormat CURRENCY_FORMAT = new DecimalFormat("#,##0.00");
    private static final DecimalFormat PERCENT_FORMAT = new DecimalFormat("0.00%");

    @FXML
    void initialize() {

        displayList.clear();
        projectModels = ProjectManager.loadProjects();

        //displaying and formatting for observable list
        for (ResultsModel model : projectModels) {

            String calculationType = model.isCalculatedFromMargin() ? "Margin→Price" : "Price→Margin";

            String displayName = String.format("%s | %s | Unit: $%s | Margin: %s",
                    model.getProjectName(),
                    calculationType,
                    CURRENCY_FORMAT.format(model.getCostPerUnitMxn()),
                    PERCENT_FORMAT.format(model.getProfitMargin())
            );
            displayList.add(displayName);
        }

        listView.setItems(displayList);

        //disable buttons if no project selected
        btnOpnProj.setDisable(true);
        btnDelProj.setDisable(true);

        //detect selection
        listView.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            boolean hasSelection = (newVal != null);
            btnOpnProj.setDisable(!hasSelection);
            btnDelProj.setDisable(!hasSelection);
        });
    }

    @FXML
    void btnLogOutClick(ActionEvent event) {
        SceneChanger.changeScene(btnLogOut, "/com.github.gabrielortiz00.byp_costing_jdeploy/views/Login.fxml");
    }

    @FXML
    void btnNewClick(ActionEvent event) {
        SceneChanger.changeScene(btnNewProj, "/com.github.gabrielortiz00.byp_costing_jdeploy/views/NewProject.fxml");
    }

    @FXML
    void btnDelProjClick(ActionEvent event) {
        int selectedIndex = listView.getSelectionModel().getSelectedIndex();

        //confirm project deletion
        String projectName = projectModels.get(selectedIndex).getProjectName();
        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("Confirm Deletion");
        confirmAlert.setHeaderText("Delete Project");
        confirmAlert.setContentText("Are you sure you want to delete the project \"" + projectName + "\"?\nThis action cannot be undone.");
        ButtonType buttonYes = new ButtonType("Yes, Delete");
        ButtonType buttonNo = new ButtonType("Cancel", ButtonBar.ButtonData.CANCEL_CLOSE);
        confirmAlert.getButtonTypes().setAll(buttonYes, buttonNo);

        // Show the confirmation dialog and wait for response
        confirmAlert.showAndWait().ifPresent(buttonType -> {
            if (buttonType == buttonYes) {
                //deletion confirmed
                ResultsModel projectToDelete = projectModels.get(selectedIndex);
                ProjectManager.deleteProject(projectToDelete);

                //refresh list of projects
                initialize();
                showAlert(Alert.AlertType.INFORMATION, "Project Deleted", "The selected project has been deleted.");
            }
        });
    }

    @FXML
    void btnOpnProjClick(ActionEvent event) {
        int selectedIndex = listView.getSelectionModel().getSelectedIndex();

        try {
            //open selected project in results scr
            ResultsModel selectedProject = projectModels.get(selectedIndex);
            SceneChanger.changeSceneWithData(btnOpnProj, "/com.github.gabrielortiz00.byp_costing_jdeploy/views/Results.fxml", selectedProject);
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Error", "Failed to load project details");
        }
    }

    public void refreshProjects() {

        displayList.clear();
        projectModels = ProjectManager.loadProjects();

        //displaying and formatting for observable list
        for (ResultsModel model : projectModels) {
            String calculationType = model.isCalculatedFromMargin() ?
                    "Margin→Price" : "Price→Margin";

            String displayName = String.format("%s | %s | Unit: $%s | Margin: %s",
                    model.getProjectName(),
                    calculationType,
                    CURRENCY_FORMAT.format(model.getCostPerUnitMxn()),
                    PERCENT_FORMAT.format(model.getProfitMargin()));

            displayList.add(displayName);
        }
        listView.setItems(displayList);

        //disable buttons if no project selected
        btnOpnProj.setDisable(true);
        btnDelProj.setDisable(true);
    }

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}