package com.example.factorylayout.controller

import com.example.factorylayout.FactoryApplication
import com.example.factorylayout.data.SingletonData
import com.example.factorylayout.model.Coordinate
import javafx.fxml.FXML
import javafx.fxml.FXMLLoader
import javafx.scene.Parent
import javafx.scene.Scene
import javafx.scene.canvas.Canvas
import javafx.scene.paint.Color
import javafx.stage.Stage
import java.io.File


class FactoryController {

    @FXML
    private lateinit var canvas: Canvas

    private val data = SingletonData.getInstance()
    private var factory = data.getFactoryLayout()

    @FXML
    fun initialize() {
        canvas.width = factory.length.toDouble() * 10 + 1
        canvas.height = factory.width.toDouble() * 10 + 1
        drawFactory()
    }

    private fun drawFactory(){
        val gc = canvas.getGraphicsContext2D()
        var x = 0.5
        var y = 0.5
        val excludedCoordinates =  factory.excludedCoordinates.toMutableList()
        factory.objects.forEach {
            it.first.coordinates.forEach {coordinate ->
                excludedCoordinates.add(Coordinate(coordinate.x + it.second.x, coordinate.y + it.second.y))
            }
        }

        while (x < canvas.width - 0.5){
            while (y < canvas.height - 0.5){
                val dataX = x.toUserCoordinate()
                val dataY = y.toUserCoordinate()
                if (!excludedCoordinates.contains(Coordinate(dataX, dataY))){
                    gc.fill = Color.WHITE
                    gc.fillRect(x, y, 9.5, 9.5)
                    gc.fill = Color.BLACK
                    gc.lineWidth = 1.0
                    if (x == 0.5
                        || excludedCoordinates.contains(Coordinate(dataX - 1, dataY))){
                        gc.moveTo(x, y )
                        gc.lineTo(x, y + 10)
                        gc.stroke()
                    }
                    if (x == canvas.width - 10.5
                        || excludedCoordinates.contains(Coordinate(dataX + 1, dataY))){
                        gc.moveTo(x + 10, y )
                        gc.lineTo(x + 10, y + 10)
                        gc.stroke()
                    }
                    if (y == 0.5
                        || excludedCoordinates.contains(Coordinate(dataX, dataY - 1))){
                        gc.moveTo(x , y)
                        gc.lineTo(x + 10, y)
                        gc.stroke()
                    }
                    if (y == canvas.height - 10.5
                        || excludedCoordinates.contains(Coordinate(dataX, dataY + 1))){
                        gc.moveTo(x, y + 10)
                        gc.lineTo(x + 10, y + 10)
                        gc.stroke()
                    }
                }
                else if(factory.excludedCoordinates.contains(Coordinate(dataX,dataY))){
                    gc.fill = Color.GRAY
                    gc.fillRect(x, y, 10.0, 10.0)
                } else{
                    val colorInfo = factory.objects.first{ it.first.coordinates.contains(Coordinate(dataX-it.second.x, dataY-it.second.y)) }
                    gc.fill = colorInfo.first.color
                    gc.fillRect(x, y, 10.0, 10.0)
                    gc.fill = Color.BLACK
                    if (x == 0.5){
                        gc.moveTo(x, y )
                        gc.lineTo(x, y + 10)
                        gc.stroke()
                    }
                    if (x == canvas.width - 10.5){
                        gc.moveTo(x + 10, y )
                        gc.lineTo(x + 10, y + 10)
                        gc.stroke()
                    }
                    if (y == 0.5){
                        gc.moveTo(x , y)
                        gc.lineTo(x + 10, y)
                        gc.stroke()
                    }
                    if (y == canvas.height - 10.5){
                        gc.moveTo(x, y + 10)
                        gc.lineTo(x + 10, y + 10)
                        gc.stroke()
                    }
                }
                y += 10
            }
            y = 0.5
            x += 10
        }
    }
    private fun Double.toUserCoordinate() = (this / 10).toInt()

    fun onBackPressed() {
        val stage = this.canvas.scene.window as Stage
        val loader = FXMLLoader(FactoryApplication::class.java.getResource("MainView.fxml"))
        val scene = Scene(loader.load(), 200.0, 180.0)
        stage.scene = scene
        stage.show()
    }

    fun onAddObjectPressed() {
        val stage = Stage()
        stage.title = "Add Object"
        val loader = FXMLLoader(FactoryApplication::class.java.getResource("ObjectCreateView.fxml"))
        val root = loader.load<Any>() as Parent
        val scene = Scene(root)
        stage.scene = scene
        stage.show()
        stage.setOnHiding {
            this.factory = data.getFactoryLayout()
            drawFactory()
        }
    }

    fun onSaveButtonClick() {
        File("${data.getFileName()}.json").writeText(factory.toJsonString())
    }


}
