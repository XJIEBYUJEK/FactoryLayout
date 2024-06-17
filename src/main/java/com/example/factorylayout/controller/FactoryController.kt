package com.example.factorylayout.controller

import com.example.factorylayout.FactoryApplication
import com.example.factorylayout.SingletonData
import com.example.factorylayout.model.Coordinate
import javafx.event.ActionEvent
import javafx.fxml.FXML
import javafx.fxml.FXMLLoader
import javafx.scene.Scene
import javafx.scene.canvas.Canvas
import javafx.scene.paint.Color
import javafx.stage.Stage


class FactoryController {

    @FXML
    private lateinit var canvas: Canvas

    private val data = SingletonData.getInstance()
    private var factory = data.getFactoryLayout()
    private val openedFromFileFlag = data.getCreationFlag()

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
        while (x < canvas.width - 0.5){
            while (y < canvas.height - 0.5){

                if (!factory.excludedCoordinates.contains(Coordinate(x.toUserCoordinate(), y.toUserCoordinate()))){
                    gc.fill = Color.WHITE
                    gc.fillRect(x, y, 9.5, 9.5)
                    gc.fill = Color.BLACK
                    gc.lineWidth = 1.0
                    if (x == 0.5
                        || factory.excludedCoordinates.contains(Coordinate(x.toUserCoordinate() - 1, y.toUserCoordinate()))){
                        gc.moveTo(x, y )
                        gc.lineTo(x, y + 10)
                        gc.stroke()
                    }
                    if (x == canvas.width - 10.5
                        || factory.excludedCoordinates.contains(Coordinate(x.toUserCoordinate() + 1, y.toUserCoordinate()))){
                        gc.moveTo(x + 10, y )
                        gc.lineTo(x + 10, y + 10)
                        gc.stroke()
                    }
                    if (y == 0.5
                        || factory.excludedCoordinates.contains(Coordinate(x.toUserCoordinate(), y.toUserCoordinate() - 1))){
                        gc.moveTo(x , y)
                        gc.lineTo(x + 10, y)
                        gc.stroke()
                    }
                    if (y == canvas.height - 10.5
                        || factory.excludedCoordinates.contains(Coordinate(x.toUserCoordinate(), y.toUserCoordinate() + 1))){
                        gc.moveTo(x, y + 10)
                        gc.lineTo(x + 10, y + 10)
                        gc.stroke()
                    }
                }
                else{
                    gc.fill = Color.GRAY
                    gc.fillRect(x, y, 10.0, 10.0)
                }
                y += 10
            }
            y = 0.5
            x += 10
        }
    }
    private fun Double.toUserCoordinate() = (this / 10).toInt()

    fun onBackPressed(actionEvent: ActionEvent) {
        val stage = this.canvas.scene.window as Stage
        val loader = FXMLLoader(FactoryApplication::class.java.getResource("MainView.fxml"))
        val scene = Scene(loader.load(), 200.0, 180.0)
        stage.scene = scene
        stage.show()
    }



}
