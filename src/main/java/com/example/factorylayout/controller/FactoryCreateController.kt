package com.example.factorylayout.controller

import com.example.factorylayout.FactoryApplication
import com.example.factorylayout.data.SingletonData
import com.example.factorylayout.model.Coordinate
import com.example.factorylayout.model.Factory
import javafx.fxml.FXML
import javafx.fxml.FXMLLoader
import javafx.geometry.Pos
import javafx.scene.Parent
import javafx.scene.Scene
import javafx.scene.canvas.Canvas
import javafx.scene.control.Button
import javafx.scene.control.CheckBox
import javafx.scene.control.Label
import javafx.scene.control.TextField
import javafx.scene.input.MouseEvent
import javafx.scene.layout.VBox
import javafx.scene.paint.Color
import javafx.stage.Stage
import java.io.File

class FactoryCreateController {

    @FXML
    private lateinit var saveButton: Button

    @FXML
    private lateinit var undoCheckBox: CheckBox

    @FXML
    private lateinit var warningLabel: Label

    @FXML
    private lateinit var lengthText: TextField

    @FXML
    private lateinit var widthText: TextField

    @FXML
    private lateinit var canvas: Canvas

    private var coordinateList: MutableList<Coordinate> = mutableListOf()

    private val data = SingletonData.getInstance()

    @FXML
    fun onCreateClick() {
        warningLabel.text = ""
        val width = widthText.text.toDoubleOrNull()?.times(10)
        val length = lengthText.text.toDoubleOrNull()?.times(10)
        if (width != null && length != null && width > 0 && width <= 500 && length > 0 && length <= 1000){
            saveButton.isDisable = false
            canvas.width = length + 1
            canvas.height = width + 1
            val gc = canvas.getGraphicsContext2D()
            gc.fill = Color.WHITE
            gc.fillRect(0.0, 0.0, canvas.width, canvas.height )
            gc.fill = Color.BLACK
            gc.lineWidth = 1.0
            var x = 0.5
            var y = 0.5
            while (x <= length + 0.5) {
                gc.moveTo(x, 0.0)
                gc.lineTo(x, width)
                gc.stroke()
                x += 10.0
            }
            while (y <= width + 0.5) {
                gc.moveTo(0.0, y)
                gc.lineTo(length, y)
                gc.stroke()
                y += 10.0
            }
        }
        else {
            saveButton.isDisable = true
            warningLabel.text = "error"
        }
    }

    fun onMouseDragged(mouseEvent: MouseEvent) {
        colorPixels(mouseEvent)
    }

    fun onMouseClicked(mouseEvent: MouseEvent) {
        colorPixels(mouseEvent)
    }

    private fun colorPixels(e: MouseEvent){
        val gc = canvas.getGraphicsContext2D()
        val x = e.x - e.x % 10
        val y = e.y - e.y % 10
        if ( x < canvas.width - 1  && x >= 0 && y >= 0 && y < canvas.height - 1){
            val coordinate = Coordinate(x.toInt() / 10, y.toInt() / 10)
            if (undoCheckBox.isSelected){
                gc.fill =  Color.WHITE
                coordinateList.remove(coordinate)
            }
            else{
                gc.fill = Color.BLACK
                coordinateList.add(coordinate)
                coordinateList = coordinateList.distinct().toMutableList()
            }
            gc.fillRect(x + 1,y + 1,9.0,9.0)
        }
    }

    fun saveFactoryLayout() {

        val factoryLayout = Factory(widthText.text.toInt(), lengthText.text.toInt(), coordinateList)
        val filenameTextField = TextField()
        val insideSaveButton = Button()
        val createStage = Stage()
        fun createExtraStage(){
            filenameTextField.promptText = "Filename"
            filenameTextField.maxWidth = 150.0
            insideSaveButton.text = "Save"
            val vBox = VBox()
            vBox.spacing = 5.0
            vBox.alignment = Pos.CENTER
            vBox.children.addAll(filenameTextField, insideSaveButton)
            vBox.prefHeight = 150.0
            vBox.prefWidth = 200.0
            val scene = Scene(vBox)
            createStage.title = "Create File"
            createStage.scene = scene
            createStage.show()
        }
        createExtraStage()

        insideSaveButton.setOnAction {
            //TODO   correct filename check
            File("${filenameTextField.text ?: "null"}.json").writeText(factoryLayout.toJsonString())
            data.setFactoryLayout(factoryLayout)
            createStage.close()

            val stage = this.saveButton.scene.window as Stage
            val loader = FXMLLoader(FactoryApplication::class.java.getResource("FactoryView.fxml"))
            val scene = Scene(loader.load() as Parent)
            stage.scene = scene
            stage.show()
        }
    }

    fun onBackPressed() {
        val stage = this.canvas.scene.window as Stage
        val loader = FXMLLoader(FactoryApplication::class.java.getResource("MainView.fxml"))
        val scene = Scene(loader.load(), 200.0, 180.0)
        stage.scene = scene
        stage.show()
    }


}
