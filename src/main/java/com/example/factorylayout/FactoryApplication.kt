package com.example.factorylayout

import com.example.factorylayout.model.Factory
import javafx.application.Application
import javafx.fxml.FXMLLoader
import javafx.scene.Scene
import javafx.stage.Stage

class FactoryApplication : Application() {
    
    override fun start(stage: Stage) {
        val fxmlLoader = FXMLLoader(FactoryApplication::class.java.getResource("MainView.fxml"))
        val scene = Scene(fxmlLoader.load(), 200.0, 180.0)
        stage.title = "Factory Layout Manager"
        stage.isResizable = false
        stage.scene = scene
        stage.show()
    }
}

fun main() {
    Application.launch(FactoryApplication::class.java)
}