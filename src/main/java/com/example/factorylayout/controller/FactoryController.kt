package com.example.factorylayout.controller

import com.example.factorylayout.FactoryApplication
import com.example.factorylayout.data.SingletonData
import com.example.factorylayout.model.Coordinate
import com.example.factorylayout.model.FactoryObject
import com.example.factorylayout.factory.FactoryObjectCellFactory
import com.example.factorylayout.model.Factory
import javafx.collections.FXCollections
import javafx.fxml.FXML
import javafx.fxml.FXMLLoader
import javafx.geometry.Pos
import javafx.scene.Group
import javafx.scene.Parent
import javafx.scene.Scene
import javafx.scene.canvas.Canvas
import javafx.scene.canvas.GraphicsContext
import javafx.scene.control.Button
import javafx.scene.control.ColorPicker
import javafx.scene.control.ListView
import javafx.scene.control.TextField
import javafx.scene.layout.VBox
import javafx.scene.paint.Color
import javafx.scene.shape.Rectangle
import javafx.scene.shape.Shape
import javafx.stage.Stage
import java.io.File


class FactoryController {

    @FXML
    lateinit var listView: ListView<Pair<FactoryObject, Coordinate>>

    @FXML
    private lateinit var canvas: Canvas

    private val data = SingletonData.getInstance()
    private var factory = data.getFactoryLayout()

    @FXML
    fun initialize() {

        canvas.width = factory.length.toDouble() * 10 + 1
        canvas.height = factory.width.toDouble() * 10 + 1
        listView.items = FXCollections.observableList(factory.objects)
        listView.setCellFactory { FactoryObjectCellFactory() }
        //drawFactory()
        drawFactoryWithoutOutline(canvas, factory)
    }

   /* private fun drawFactory(){
        val gc = canvas.getGraphicsContext2D()
        gc.clearRect(0.0,0.0, canvas.width, canvas.height)
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
                    gc.fillRect(x, y, 10.0, 10.0)
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
    }*/
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
            initialize()
        }
    }

    fun onSaveButtonClick() {
        File("${data.getFileName()}.json").writeText(factory.toJsonString())
    }

    fun onDeleteButtonClick() {
        val index = listView.selectionModel.selectedIndex
        if(index > -1){
            val selectedItem = listView.items[index]
            listView.items.remove(selectedItem)
            factory.objects.remove(selectedItem)
            data.setFactoryLayout(factory)
            initialize()
        }
    }

    private fun drawFactoryWithoutOutline(canvas: Canvas , factory: Factory){
        val gc = canvas.getGraphicsContext2D()
        gc.clearRect(0.0,0.0, canvas.width, canvas.height)
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
                    gc.fillRect(x, y, 10.0, 10.0)
                }
                else if(!factory.excludedCoordinates.contains(Coordinate(dataX,dataY))){
                    val colorInfo = factory.objects.first{ it.first.coordinates.contains(Coordinate(dataX-it.second.x, dataY-it.second.y)) }
                    gc.fill = colorInfo.first.color
                    gc.fillRect(x, y, 10.0, 10.0)
                }
                y += 10
            }
            y = 0.5
            x += 10
        }
    }

    fun onEditButtonClick() {
        val index = listView.selectionModel.selectedIndex
        if(index > -1){
            val selectedItem = listView.items[index]
            val tempFactory = factory.makeCopy()
            tempFactory.objects.remove(selectedItem)

            val insideEditButton = Button()
            insideEditButton.text = "Edit"
            val factoryCanvas = Canvas(factory.length * 10.0 + 1, factory.width * 10.0 + 1)
            val shape = createShape(selectedItem)
            val stage = Stage()
            val group = Group(factoryCanvas, shape)
            stage.title = "Edit Object"
            val textField = TextField()
            textField.text = selectedItem.first.name
            val colorPicker = ColorPicker()
            colorPicker.value = selectedItem.first.color
            val vBox = VBox()
            vBox.spacing = 5.0
            vBox.alignment = Pos.CENTER
            vBox.children.addAll(textField, colorPicker, group, insideEditButton)
            val scene = Scene(vBox)
            stage.scene = scene
            stage.show()
            drawFactoryWithoutOutline(factoryCanvas, tempFactory)
            insideEditButton.setOnAction {
                val bounds = shape.boundsInParent
                selectedItem.second.x = bounds.minX.toInt() / 10
                selectedItem.second.y = bounds.minY.toInt() / 10
                selectedItem.first.color = colorPicker.value
                selectedItem.first.name = textField.text
                stage.close()
                initialize()
            }


           // data.setFactoryLayout(factory)
            //initialize()
        }
    }

    private fun createShape(fo: Pair<FactoryObject,Coordinate>): Shape {
        var shape = Rectangle(0.0,0.0,0.0,0.0) as Shape
        fo.first.coordinates.forEach {
            shape = Shape.union(shape, Rectangle(it.x * 10.0 + 1,it.y * 10.0 + 1,9.0,9.0))
        }
        shape.fill = fo.first.color
        shape.layoutX = fo.second.x * 10.0
        shape.layoutY = fo.second.y * 10.0
        shape.setOnMouseDragged { e ->
            shape.layoutX = (e.sceneX.toInt() / 20) * 10.0
            shape.layoutY= (e.sceneY.toInt() / 20) * 10.0
        }
        return shape
    }
}
