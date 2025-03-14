package com.github.gabrielortiz00.byp_costing_jdeploy.controllers;

import com.github.gabrielortiz00.byp_costing_jdeploy.ResultsModel;
import com.github.gabrielortiz00.byp_costing_jdeploy.SceneChanger;
import com.github.gabrielortiz00.byp_costing_jdeploy.utils.ExchangeRateService;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;

public class NewProjectController {

    @FXML private TextField txtProjectName;
    @FXML private TextField txtProductName;
    @FXML private TextField txtUnitCost;
    @FXML private TextField txtIncrementalCost;
    @FXML private TextField txtShippingCost;
    @FXML private TextField txtUnitsPerContainer;
    @FXML private TextField txtTaxRate;
    @FXML private TextField txtExchangeRate;
    @FXML private TextField txtExchangeBuffer;
    @FXML private RadioButton radioMargin;
    @FXML private RadioButton radioPrice;
    @FXML private Label lblMarginOrPrice;
    @FXML private TextField txtMarginOrPrice;
    @FXML private Button btnCalculate;
    @FXML private Button btnRefreshRate;
    @FXML private Button btnBack;

    @FXML
    public void initialize() {
        //togglegroup for margin and price buttons
        ToggleGroup calculationToggle = new ToggleGroup();
        radioMargin.setToggleGroup(calculationToggle);
        radioPrice.setToggleGroup(calculationToggle);
        //by default select margin
        radioMargin.setSelected(true);

        //validate all numeric fields
        ensureNumericInput(txtUnitCost);
        ensureNumericInput(txtIncrementalCost);
        ensureNumericInput(txtShippingCost);
        ensureNumericInput(txtUnitsPerContainer);
        ensureNumericInput(txtTaxRate);
        ensureNumericInput(txtExchangeRate);
        ensureNumericInput(txtExchangeBuffer);
        ensureNumericInput(txtMarginOrPrice);

        //update label for selected radiobtn
        calculationToggle.selectedToggleProperty().addListener((obs, oldVal, newVal)
                -> {updateMarginPriceLabel();});

        //default value for tax field
        txtTaxRate.setText("0.16");
    }

    //method prevents non-numeric input into fields
    private void ensureNumericInput(TextField field) {
        field.textProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal.matches("\\d*(\\.\\d*)?")) {
                field.setText(oldVal);
            }
        });
    }

    private void updateMarginPriceLabel() {
        if (radioMargin.isSelected()) {
            lblMarginOrPrice.setText("Profit Margin (M):");
            txtMarginOrPrice.setPromptText("e.g., 0.20 for 20%");
        } else {
            lblMarginOrPrice.setText("Selling Price (P):");
            txtMarginOrPrice.setPromptText("Price in MXN");
        }
    }

    @FXML
    private void handleRefreshRate(ActionEvent event) {
        btnRefreshRate.setDisable(true);
        btnRefreshRate.setText("Loading...");

        //call exchange rate service
        ExchangeRateService.getCurrentUsdToMxnRate().thenAccept(rate -> {
                    //update from javafx thread
                    javafx.application.Platform.runLater(() -> {
                        //round rate to 2 decimal places
                        String formattedRate = String.format("%.2f", rate);
                        txtExchangeRate.setText(formattedRate);

                        btnRefreshRate.setDisable(false);
                        btnRefreshRate.setText("Refresh Rate");

                        showAlert(Alert.AlertType.INFORMATION, "Exchange Rate", "Current USD to MXN rate: " + formattedRate);
                    });
                })

                .exceptionally(ex -> {
                    javafx.application.Platform.runLater(() -> {

                        btnRefreshRate.setDisable(false);
                        btnRefreshRate.setText("Refresh Rate");

                        showAlert(Alert.AlertType.ERROR, "Exchange Rate Error", "Failed to fetch exchange rate. Using default value.");

                        //set a default value
                        txtExchangeRate.setText("20.00");
                    });
                    return null;
                });
    }

    @FXML
    private void handleCalculate(ActionEvent event) {
        if (!validateInputs()) {
            return;
        }

        try {
            String projectName = txtProjectName.getText();
            String productName = txtProductName.getText();
            double unitCost = Double.parseDouble(txtUnitCost.getText());
            double incrementalCost = Double.parseDouble(txtIncrementalCost.getText());
            double shippingCost = Double.parseDouble(txtShippingCost.getText());
            int unitsPerContainer = Integer.parseInt(txtUnitsPerContainer.getText());
            double taxRate = Double.parseDouble(txtTaxRate.getText());
            double exchangeRate = Double.parseDouble(txtExchangeRate.getText());
            double exchangeBuffer = Double.parseDouble(txtExchangeBuffer.getText());
            double marginOrPrice = Double.parseDouble(txtMarginOrPrice.getText());

            //instantiate results object
            ResultsModel results;

            if (radioMargin.isSelected()) {
                //price from margin
                results = new ResultsModel(
                        projectName, productName,
                        unitCost, incrementalCost, shippingCost,
                        unitsPerContainer, taxRate, exchangeRate,
                        exchangeBuffer, marginOrPrice  //marginOrPrice = M
                );
            } else {
                //margin from price
                results = new ResultsModel(
                        projectName, productName,
                        unitCost, incrementalCost, shippingCost,
                        unitsPerContainer, taxRate, exchangeRate,
                        exchangeBuffer, marginOrPrice, true  //marginOrPrice = P
                );
            }

            //change scenes to results scr
            SceneChanger.changeSceneWithData(btnCalculate, "/com.github.gabrielortiz00.byp_costing_jdeploy/views/Results.fxml", results);

        } catch (NumberFormatException e) {
            showAlert(Alert.AlertType.ERROR, "Input Error",
                    "Please ensure all numeric fields contain valid values.");
        }
    }

    @FXML
    private void handleBack(ActionEvent event) {
        SceneChanger.changeScene(btnBack, "/com.github.gabrielortiz00.byp_costing_jdeploy/views/Welcome.fxml");
    }

    private boolean validateInputs() {
        if (txtProjectName.getText().isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Input Error", "Please enter a project name.");
            return false;
        }

        if (txtProductName.getText().isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Input Error", "Please enter a product name.");
            return false;
        }

        //check all numeric fields are filled
        TextField[] numericFields = {
                txtUnitCost, txtIncrementalCost, txtShippingCost, txtUnitsPerContainer,
                txtTaxRate, txtExchangeRate, txtExchangeBuffer, txtMarginOrPrice
        };

        for (TextField field : numericFields) {
            if (field.getText().isEmpty()) {
                showAlert(Alert.AlertType.ERROR, "Input Error",
                        "Please fill in all numeric fields.");
                return false;
            }
        }

        //specific value ranges
        try {
            //tax rate between 0 and 1
            double taxRate = Double.parseDouble(txtTaxRate.getText());
            if (taxRate < 0 || taxRate > 1) {
                showAlert(Alert.AlertType.ERROR, "Input Error",
                        "Tax rate should be between 0 and 1 (e.g., 0.16 for 16%).");
                return false;
            }

            //exchange buffer between 0 and 1
            double buffer = Double.parseDouble(txtExchangeBuffer.getText());
            if (buffer < 0 || buffer > 1) {
                showAlert(Alert.AlertType.ERROR, "Input Error",
                        "Exchange buffer should be between 0 and 1 (e.g., 0.05 for 5%).");
                return false;
            }

            // margin between 0 and 1 if from margin
            if (radioMargin.isSelected()) {
                double margin = Double.parseDouble(txtMarginOrPrice.getText());
                if (margin < 0 || margin > 1) {
                    showAlert(Alert.AlertType.ERROR, "Input Error",
                            "Profit margin should be between 0 and 1 (e.g., 0.20 for 20%).");
                    return false;
                }
            }

            //units per conatiner is positive
            int units = Integer.parseInt(txtUnitsPerContainer.getText());
            if (units <= 0) {
                showAlert(Alert.AlertType.ERROR, "Input Error",
                        "Units per container must be a positive number.");
                return false;
            }

        } catch (NumberFormatException e) {
            showAlert(Alert.AlertType.ERROR, "Input Error",
                    "Please ensure all numeric fields contain valid numbers.");
            return false;
        }
        return true;
    }

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}