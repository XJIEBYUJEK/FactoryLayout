package com.example.factorylayout.controller

import com.example.factorylayout.FactoryApplication
import com.example.factorylayout.data.SingletonData
import com.example.factorylayout.model.Factory
import javafx.fxml.FXML
import javafx.fxml.FXMLLoader
import javafx.scene.Parent
import javafx.scene.Scene
import javafx.scene.control.Alert
import javafx.scene.control.Button
import javafx.stage.FileChooser
import javafx.stage.Stage
import kotlinx.serialization.json.Json
import java.io.File
import java.nio.file.Paths


class MainController {

    @FXML
    private lateinit var createButton: Button

    @FXML
    private lateinit var openButton: Button

    private val data = SingletonData.getInstance()

    @FXML
    fun createNewProject() {
        val stage = this.createButton.scene.window as Stage
        val loader = FXMLLoader(FactoryApplication::class.java.getResource("FactoryCreateView.fxml"))
        val root = loader.load<Any>() as Parent
        val scene = Scene(root)
        val newStage = Stage()
        newStage.title = stage.title
        newStage.scene = scene
        newStage.show()
        stage.close()
    }

    @FXML
    fun openProject() {  //TODO add explorer
        val stage = this.openButton.scene.window as Stage
        val fileChooser = FileChooser()
        fileChooser.extensionFilters.add(FileChooser.ExtensionFilter("json Files", "*.json"))
        val currentPath = Paths.get(".").toAbsolutePath().normalize().toString()
        fileChooser.initialDirectory = File(currentPath)
        val file = fileChooser.showOpenDialog(stage)
        val textInFile = file.readText()
        try{
            val factoryLayout = Json.decodeFromString<Factory>(textInFile)
            data.setFactoryLayout(factoryLayout)
            data.setFileName(file.path)
            val loader = FXMLLoader(FactoryApplication::class.java.getResource("FactoryView.fxml"))
            val scene = Scene(loader.load() as Parent)
            val newStage = Stage()
            newStage.title = stage.title + " ${file.name}"
            newStage.scene = scene
            newStage.show()
            stage.close()
        } catch (e: Exception){
            val alert = Alert(Alert.AlertType.ERROR, "Incorrect file content\n$e")
            alert.show()
        }
    }
}
