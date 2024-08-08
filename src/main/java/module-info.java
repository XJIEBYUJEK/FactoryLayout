module com.example.factorylayout {
    requires javafx.controls;
    requires javafx.fxml;
    requires kotlin.stdlib;
    requires kotlinx.serialization.core;
    requires kotlinx.serialization.json;
    requires javafx.swing;
    requires poi;
    requires poi.ooxml;


    opens com.example.factorylayout to javafx.fxml;
    exports com.example.factorylayout;
    opens com.example.factorylayout.controller to javafx.fxml;
    exports com.example.factorylayout.controller;
}