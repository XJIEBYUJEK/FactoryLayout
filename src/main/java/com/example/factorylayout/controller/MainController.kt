package com.example.factorylayout.controller

import com.example.factorylayout.FactoryApplication
import com.example.factorylayout.SingletonData
import com.example.factorylayout.model.Factory
import javafx.event.ActionEvent
import javafx.fxml.FXML
import javafx.fxml.FXMLLoader
import javafx.geometry.Pos
import javafx.scene.Parent
import javafx.scene.Scene
import javafx.scene.control.Button
import javafx.scene.control.TextField
import javafx.scene.layout.VBox
import javafx.stage.Stage
import kotlinx.serialization.json.Json
import java.io.File

class MainController {

    @FXML
    private lateinit var createButton: Button

    @FXML
    private lateinit var openButton: Button

    private val data = SingletonData.getInstance()



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
    fun openProject(event: ActionEvent) {  //TODO add explorer
        val filenameTextField = TextField()
        val insideOpenButton = Button()
        val openStage = Stage()
        fun createExtraStage(){
            filenameTextField.promptText = "Filename"
            filenameTextField.maxWidth = 150.0
            insideOpenButton.text = "Open"
            val vBox = VBox()
            vBox.spacing = 5.0
            vBox.alignment = Pos.CENTER
            vBox.children.addAll(filenameTextField, insideOpenButton)
            vBox.prefHeight = 150.0
            vBox.prefWidth = 200.0
            val scene = Scene(vBox)
            openStage.title = "Open File"
            openStage.scene = scene
            openStage.show()
        }
        createExtraStage()

        insideOpenButton.setOnAction {
            //TODO   correct filename check
            val textInFile = File("${filenameTextField.text ?: "null"}.json").readText()
            val factoryLayout = Json.decodeFromString<Factory>(textInFile)
            data.setFactoryLayout(factoryLayout)
            data.setCreationFlag(true)
            openStage.close()

            val stage = this.openButton.scene.window as Stage
            val loader = FXMLLoader(FactoryApplication::class.java.getResource("FactoryView.fxml"))
            val scene = Scene(loader.load() as Parent)
            stage.scene = scene
            stage.show()
        }
    }

}
