package com.github.gabrielortiz00.byp_costing_jdeploy.controllers;

import com.github.gabrielortiz00.byp_costing_jdeploy.ResultsModel;
import com.github.gabrielortiz00.byp_costing_jdeploy.SceneChanger;
import com.github.gabrielortiz00.byp_costing_jdeploy.utils.PdfExportUtil;
import com.github.gabrielortiz00.byp_costing_jdeploy.utils.ProjectManager;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.FileChooser;
import java.io.*;
import java.text.DecimalFormat;
import java.time.format.DateTimeFormatter;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.chart.PieChart;
import javafx.scene.control.Tooltip;

public class ResultsController {

    @FXML private Label lblProjectName;
    @FXML private Label lblProductName;
    @FXML private Label lblCreatedAt;
    @FXML private Label lblUnitCost;
    @FXML private Label lblIncrementalCost;
    @FXML private Label lblShippingCost;
    @FXML private Label lblUnitsPerContainer;
    @FXML private Label lblTaxRate;
    @FXML private Label lblExchangeRate;
    @FXML private Label lblExchangeBuffer;
    @FXML private Label lblContainerCostUsd;
    @FXML private Label lblContainerCostWithTax;
    @FXML private Label lblContainerCostMxn;
    @FXML private Label lblCostPerUnitMxn;
    @FXML private Label lblSellingPrice;
    @FXML private Label lblProfitMargin;
    @FXML private Button btnExportCsv;
    @FXML private Button btnExportPdf;
    @FXML private Button btnSaveProject;
    @FXML private Button btnBackToHome;
    @FXML private PieChart costPieChart;

    private ResultsModel resultsModel;
    private DecimalFormat currencyFormat = new DecimalFormat("#,##0.00");
    private DecimalFormat percentFormat = new DecimalFormat("0.00%");
    private DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public void setResultsModel(ResultsModel model) {
        this.resultsModel = model;
        displayResults();
        setupPieChart();
    }

    private void displayResults() {
        if (resultsModel == null) return;

        //display project information
        lblProjectName.setText(resultsModel.getProjectName());
        lblProductName.setText(resultsModel.getProductName());
        lblCreatedAt.setText(resultsModel.getCreatedAt().format(dateFormatter));

        //display input params
        lblUnitCost.setText("$" + currencyFormat.format(resultsModel.getUnitCost()) + " USD");
        lblIncrementalCost.setText("$" + currencyFormat.format(resultsModel.getIncrementalCost()) + " USD");
        lblShippingCost.setText("$" + currencyFormat.format(resultsModel.getShippingCost()) + " USD");
        lblUnitsPerContainer.setText(String.valueOf(resultsModel.getUnitsPerContainer()));
        lblTaxRate.setText(percentFormat.format(resultsModel.getTaxRate()));
        lblExchangeRate.setText(currencyFormat.format(resultsModel.getExchangeRate()) + " MXN/USD");
        lblExchangeBuffer.setText(percentFormat.format(resultsModel.getExchangeBuffer()));

        //display results
        lblContainerCostUsd.setText("$" + currencyFormat.format(resultsModel.getContainerCostUsd()) + " USD");
        lblContainerCostWithTax.setText("$" + currencyFormat.format(resultsModel.getContainerCostWithTax()) + " USD");
        lblContainerCostMxn.setText("$" + currencyFormat.format(resultsModel.getContainerCostMxn()) + " MXN");
        lblCostPerUnitMxn.setText("$" + currencyFormat.format(resultsModel.getCostPerUnitMxn()) + " MXN");
        lblSellingPrice.setText("$" + currencyFormat.format(resultsModel.getSellingPrice()) + " MXN");
        lblProfitMargin.setText(percentFormat.format(resultsModel.getProfitMargin()));

        //highlight desired value
        if (resultsModel.isCalculatedFromMargin()) {
            lblSellingPrice.setStyle("-fx-font-weight: bold; -fx-text-fill: #0066cc;");
        } else {
            lblProfitMargin.setStyle("-fx-font-weight: bold; -fx-text-fill: #0066cc;");
        }
    }

    private void setupPieChart() {
        if (resultsModel == null) return;

        //base cost components
        double unitCostTotal = resultsModel.getUnitCost() * resultsModel.getUnitsPerContainer();
        double incrementalCostTotal = resultsModel.getIncrementalCost() * resultsModel.getUnitsPerContainer();
        double shippingCost = resultsModel.getShippingCost();
        double baseCostUsd = unitCostTotal + incrementalCostTotal + shippingCost;

        double taxAmount = baseCostUsd * resultsModel.getTaxRate();

        double totalCost = baseCostUsd + taxAmount;

        //compute percentages
        double unitCostPerc = unitCostTotal / totalCost;
        double incrementalCostPerc = incrementalCostTotal / totalCost;
        double shippingCostPerc = shippingCost / totalCost;
        double taxPerc = taxAmount / totalCost;

        //create pie chart data
        ObservableList<PieChart.Data> pieChartData = FXCollections.observableArrayList(
                new PieChart.Data("Unit Cost", unitCostTotal),
                new PieChart.Data("Incremental Cost", incrementalCostTotal),
                new PieChart.Data("Shipping Cost", shippingCost),
                new PieChart.Data("Import Tax", taxAmount)
        );

        costPieChart.setData(pieChartData);

        //pie chart colors
        pieChartData.get(0).getNode().setStyle("-fx-pie-color: #2A3178;"); // Dark Blue for Unit Cost
        pieChartData.get(1).getNode().setStyle("-fx-pie-color: #963030;"); // Dark Red for Incremental Cost
        pieChartData.get(2).getNode().setStyle("-fx-pie-color: #307830;"); // Dark Green for Shipping Cost
        pieChartData.get(3).getNode().setStyle("-fx-pie-color: #f76d0d;"); // Gold/Brown for Tax


        //hover functionality for chart
        for (final PieChart.Data data : costPieChart.getData()) {
            String name = data.getName();
            double value = data.getPieValue();
            double percentage = 0;

            if (name.equals("Unit Cost")) {
                percentage = unitCostPerc;
            } else if (name.equals("Incremental Cost")) {
                percentage = incrementalCostPerc;
            } else if (name.equals("Shipping Cost")) {
                percentage = shippingCostPerc;
            } else if (name.equals("Import Tax")) {
                percentage = taxPerc;
            }

            //tooltip for displaying pie chart data when hover
            Tooltip tooltip = new Tooltip(
                    String.format(
                            "%s: $%s (%.1f%%)",
                            name,
                            currencyFormat.format(value),
                            percentage * 100)
            );

            Tooltip.install(data.getNode(), tooltip);
        }
    }

    @FXML
    private void handleExportCsv(ActionEvent event) {

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Save CSV File");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("CSV Files", "*.csv"));

        //set default name for file
        String defaultFilename = resultsModel.getProjectName().replaceAll("\\s+", "_") + ".csv";
        fileChooser.setInitialFileName(defaultFilename);
        File file = fileChooser.showSaveDialog(btnExportCsv.getScene().getWindow());

        if (file != null) {
            try (PrintWriter writer = new PrintWriter(file)) {
                //header
                writer.println("Parameter,Value");

                //project info
                writer.println("Project Name," + resultsModel.getProjectName());
                writer.println("Product Name," + resultsModel.getProductName());
                writer.println("Created At," + resultsModel.getCreatedAt().format(dateFormatter));

                //input params
                writer.println("Unit Cost (USD)," + resultsModel.getUnitCost());
                writer.println("Incremental Cost (USD)," + resultsModel.getIncrementalCost());
                writer.println("Shipping Cost (USD)," + resultsModel.getShippingCost());
                writer.println("Units Per Container," + resultsModel.getUnitsPerContainer());
                writer.println("Tax Rate," + resultsModel.getTaxRate());
                writer.println("Exchange Rate (MXN/USD)," + resultsModel.getExchangeRate());
                writer.println("Exchange Buffer," + resultsModel.getExchangeBuffer());

                //results
                writer.println("Container Cost (USD)," + resultsModel.getContainerCostUsd());
                writer.println("Container Cost with Tax (USD)," + resultsModel.getContainerCostWithTax());
                writer.println("Container Cost (MXN)," + resultsModel.getContainerCostMxn());
                writer.println("Cost Per Unit (MXN)," + resultsModel.getCostPerUnitMxn());
                writer.println("Selling Price (MXN)," + resultsModel.getSellingPrice());
                writer.println("Profit Margin," + resultsModel.getProfitMargin());

                //calculation type
                writer.println("Calculation Type," +
                        (resultsModel.isCalculatedFromMargin() ? "From Margin to Price" : "From Price to Margin"));

                showAlert(Alert.AlertType.INFORMATION, "Export Successful",
                        "Results successfully exported to CSV file.");

            } catch (IOException e) {
                showAlert(Alert.AlertType.ERROR, "Export Error",
                        "Failed to export CSV:");
            }
        }
    }

    @FXML
    private void handleExportPdf(ActionEvent event) {

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Save PDF File");
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("PDF Files", "*.pdf")
        );

        //set default name for file
        String defaultFilename = resultsModel.getProjectName().replaceAll("\\s+", "_") + ".pdf";
        fileChooser.setInitialFileName(defaultFilename);
        File file = fileChooser.showSaveDialog(btnExportPdf.getScene().getWindow());

        if (file != null) {
            try {
                //call pdfExportUtil
                PdfExportUtil.exportResultsToPdf(resultsModel, file.getAbsolutePath());

                showAlert(Alert.AlertType.INFORMATION, "Export Successful",
                        "Results successfully exported to PDF file.");

            } catch (Exception e) {
                showAlert(Alert.AlertType.ERROR, "Export Error",
                        "Failed to export PDF");
            }
        }
    }

    @FXML
    private void handleSaveProject(ActionEvent event) {

        //call saveProject in ProjectManager class
        boolean saved = ProjectManager.saveProject(resultsModel);

        if (saved) {
            showAlert(Alert.AlertType.INFORMATION, "Project Saved",
                    "Project saved successfully.");

            //go back to welcome/projects screen
            SceneChanger.changeScene(btnSaveProject, "/com.github.gabrielortiz00.byp_costing_jdeploy/views/Welcome.fxml");
        } else {
            showAlert(Alert.AlertType.ERROR, "Save Error",
                    "Failed to save project. Please try again.");
        }
    }

    @FXML
    private void handleBack(ActionEvent event) {
        SceneChanger.changeScene(btnBackToHome, "/com.github.gabrielortiz00.byp_costing_jdeploy/views/Welcome.fxml");
    }

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}