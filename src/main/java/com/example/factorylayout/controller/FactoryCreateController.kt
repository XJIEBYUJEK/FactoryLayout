package com.example.factorylayout.controller

import com.example.factorylayout.FactoryApplication
import com.example.factorylayout.data.SingletonData
import com.example.factorylayout.model.Coordinate
import com.example.factorylayout.model.Factory
import javafx.fxml.FXML
import javafx.fxml.FXMLLoader
import javafx.scene.Parent
import javafx.scene.Scene
import javafx.scene.canvas.Canvas
import javafx.scene.control.Button
import javafx.scene.control.Label
import javafx.scene.control.TextField
import javafx.scene.input.MouseButton
import javafx.scene.input.MouseEvent
import javafx.scene.paint.Color
import javafx.stage.FileChooser
import javafx.stage.Stage
import java.io.File
import java.nio.file.Paths

class FactoryCreateController {

    @FXML
    private lateinit var saveButton: Button

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
    private var scale = 15.0

    @FXML
    fun onCreateClick() {
        warningLabel.text = ""
        val width = widthText.text.toDoubleOrNull()?.times(scale)
        val length = lengthText.text.toDoubleOrNull()?.times(scale)
        if (width != null && length != null && width > 0 && length > 0){
            saveButton.isDisable = false
            canvas.width = length + 1
            canvas.height = width + 1
            onScrollCanvas(canvas)
            val gc = canvas.getGraphicsContext2D()
            gc.clearRect(0.0,0.0, canvas.width, canvas.height)
            var x = 0.5
            var y = 0.5
            while (x < canvas.width - 0.5){
                while (y < canvas.height - 0.5){
                    val dataX = x.toUserCoordinate()
                    val dataY = y.toUserCoordinate()
                    gc.stroke = Color.web("#DDDDDD")
                    gc.lineWidth = 1.0
                    gc.fill = if (coordinateList.contains(Coordinate(dataX,dataY))) Color.RED else Color.WHITE
                    gc.fillRect(x, y, scale, scale)
                    gc.strokeRect(x, y, scale, scale)
                    y += scale
                }
                y = 0.5
                x += scale
            }
        }
        else {
            saveButton.isDisable = true
            warningLabel.text = "error"
        }
    }
    private fun Double.toUserCoordinate() = (this / scale).toInt()
    fun onMouseDragged(mouseEvent: MouseEvent) {
        when (mouseEvent.button){
            MouseButton.PRIMARY -> colorPixels(mouseEvent, false)
            MouseButton.SECONDARY -> colorPixels(mouseEvent, true)
            else -> {}
        }

    }

    fun onMouseClicked(mouseEvent: MouseEvent) {
        when (mouseEvent.button){
            MouseButton.PRIMARY -> colorPixels(mouseEvent, false)
            MouseButton.SECONDARY -> colorPixels(mouseEvent, true)
            else -> {}
        }
    }

    private fun colorPixels(e: MouseEvent, eraser: Boolean){
        val gc = canvas.getGraphicsContext2D()
        val x = e.x - e.x % scale
        val y = e.y - e.y % scale
        if ( x < canvas.width - 1  && x >= 0 && y >= 0 && y < canvas.height - 1){
            val coordinate = Coordinate((x / scale).toInt(), (y / scale).toInt() )
            if (eraser){
                gc.fill =  Color.WHITE
                coordinateList.remove(coordinate)
            }
            else{
                gc.fill = Color.RED
                coordinateList.add(coordinate)
                coordinateList = coordinateList.distinct().toMutableList()
            }
            gc.fillRect(x + 1,y + 1,scale - 1,scale - 1)
        }
    }

    fun saveFactoryLayout() {
        val factoryLayout = Factory(widthText.text.toInt(), lengthText.text.toInt(), coordinateList)
        val stage = this.saveButton.scene.window as Stage
        val fileChooser = FileChooser()
        fileChooser.extensionFilters.add(FileChooser.ExtensionFilter("json Files", "*.json"))
        val currentPath = Paths.get(".").toAbsolutePath().normalize().toString()
        fileChooser.initialDirectory = File(currentPath)
        val file = fileChooser.showSaveDialog(stage)
        file.writeText(factoryLayout.toJsonString())
        data.setFactoryLayout(factoryLayout)
        data.setFileName(file.path)
        val loader = FXMLLoader(FactoryApplication::class.java.getResource("FactoryView.fxml"))
        val scene = Scene(loader.load() as Parent)
        stage.title += " ${file.name}"
        stage.scene = scene
        stage.show()
    }

    fun onBackPressed() {
        val stage = this.canvas.scene.window as Stage
        val loader = FXMLLoader(FactoryApplication::class.java.getResource("MainView.fxml"))
        val scene = Scene(loader.load(), 200.0, 180.0)
        stage.scene = scene
        stage.isResizable = false
        stage.show()
    }

    private fun onScrollCanvas(canvas: Canvas) {
        canvas.setOnScroll {
            if(it.deltaY < 0){
                if (scale != 150.0) scale++
            }
            else {
                if(scale != 5.0) scale --
            }
            onCreateClick()
        }
    }


}
