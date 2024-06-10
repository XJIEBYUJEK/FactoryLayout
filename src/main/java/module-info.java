module com.example.factorylayout {
    requires javafx.controls;
    requires javafx.fxml;
    requires kotlin.stdlib;


    opens com.example.factorylayout to javafx.fxml;
    exports com.example.factorylayout;
    opens com.example.factorylayout.controller to javafx.fxml;
    exports com.example.factorylayout.controller;
}