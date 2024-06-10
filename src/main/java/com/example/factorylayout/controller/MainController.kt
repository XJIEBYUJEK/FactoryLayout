package com.example.factorylayout.controller

import com.example.factorylayout.FactoryApplication
import javafx.event.ActionEvent
import javafx.fxml.FXML
import javafx.fxml.FXMLLoader
import javafx.scene.Parent
import javafx.scene.Scene
import javafx.scene.control.Button
import javafx.stage.Stage

class MainController {

    @FXML
    private lateinit var createButton: Button

    @FXML
    private lateinit var openButton: Button



    @FXML
    fun createNewProject(event: ActionEvent) {
        val stage = this.createButton.scene.window as Stage
        val loader = FXMLLoader(FactoryApplication::class.java.getResource("FactoryCreateView.fxml"))
        val root = loader.load<Any>() as Parent
        val scene = Scene(root)
        stage.scene = scene
        stage.show()
    }

    @FXML
    fun openProject(event: ActionEvent) {

    }

}
