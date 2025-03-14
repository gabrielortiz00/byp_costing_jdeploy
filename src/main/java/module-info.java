module com.github.gabrielortiz00.byp_costing_jdeploy {
    requires javafx.controls;
    requires javafx.fxml;
    requires org.apache.pdfbox;

    exports com.github.gabrielortiz00.byp_costing_jdeploy;
    exports com.github.gabrielortiz00.byp_costing_jdeploy.controllers;
    exports com.github.gabrielortiz00.byp_costing_jdeploy.utils;

    opens com.github.gabrielortiz00.byp_costing_jdeploy.controllers to javafx.fxml;
    opens com.github.gabrielortiz00.byp_costing_jdeploy.views to javafx.fxml;
}