package com.github.gabrielortiz00.byp_costing_jdeploy.controllers;

import com.github.gabrielortiz00.byp_costing_jdeploy.User;
import com.github.gabrielortiz00.byp_costing_jdeploy.SceneChanger;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

public class LoginController {

    @FXML private Label lblBYP;
    @FXML private TextField txtUser;
    @FXML private Button btnLogIn;
    @FXML private Label lblMsg;
    @FXML private ImageView imageView;
    @FXML private PasswordField txtPasswordField;
    @FXML private TextField txtPasswordVisible;
    @FXML private Button btnTogglePassword;

    private boolean passwordVisible = false;

    @FXML
    public void initialize() {
        try {
            txtPasswordField.textProperty().addListener((observable, oldValue, newValue)
                    -> {txtPasswordVisible.setText(newValue);});

            txtPasswordVisible.textProperty().addListener((observable, oldValue, newValue)
                    -> {txtPasswordField.setText(newValue);});

            Image logoImage = new Image(getClass().getResourceAsStream("/com.github.gabrielortiz00.byp_costing_jdeploy/files/LOGO.jpeg"));
            imageView.setImage(logoImage);
        } catch (Exception e) {
            System.out.println("Error loading image");
            e.printStackTrace();
        }
    }

    @FXML
    public void togglePasswordVisibility() {
        passwordVisible = !passwordVisible;

        if (passwordVisible) {
            txtPasswordVisible.setText(txtPasswordField.getText());
            txtPasswordVisible.setVisible(true);
            txtPasswordField.setVisible(false);
            btnTogglePassword.setText("•");
        } else {
            txtPasswordField.setText(txtPasswordVisible.getText());
            txtPasswordVisible.setVisible(false);
            txtPasswordField.setVisible(true);
            btnTogglePassword.setText("○");
        }
    }

    @FXML
    void btnLoginClick(ActionEvent event) {

        String password = txtPasswordField.getText();
        User currUser = new User(txtUser.getText(), password);

        if (currUser.validateCredentials()) {
            //clear login fields
            txtUser.setText("");
            txtPasswordField.setText("");

            //change scene to welcome / projects scr
            SceneChanger.changeScene(btnLogIn, "/com.github.gabrielortiz00.byp_costing_jdeploy/views/Welcome.fxml");
        } else {
            lblMsg.setVisible(true);
        }
    }
}