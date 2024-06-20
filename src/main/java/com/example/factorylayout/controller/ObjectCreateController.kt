package com.example.factorylayout.controller

import com.example.factorylayout.data.SingletonData
import com.example.factorylayout.model.Coordinate
import com.example.factorylayout.model.FactoryObject
import javafx.fxml.FXML
import javafx.geometry.Pos
import javafx.scene.Group
import javafx.scene.Scene
import javafx.scene.canvas.Canvas
import javafx.scene.control.*
import javafx.scene.input.MouseButton
import javafx.scene.input.MouseEvent
import javafx.scene.layout.VBox
import javafx.scene.paint.Color
import javafx.scene.shape.Rectangle
import javafx.scene.shape.Shape
import javafx.stage.Stage
import java.time.LocalDate

class ObjectCreateController {

    @FXML
    lateinit var endDatePicker: DatePicker

    @FXML
    lateinit var startDatePicker: DatePicker

    @FXML
    lateinit var addButton: Button

    @FXML
    lateinit var colorPicker: ColorPicker

    @FXML
    private lateinit var canvas: Canvas

    @FXML
    private lateinit var objectNameText: TextField

    private val data = SingletonData.getInstance()
    private var factory = data.getFactoryLayout()

    private val dateSlider = Slider()

    private var coordinateList: MutableList<Coordinate> = mutableListOf()

    @FXML
    fun initialize() {
        colorPicker.value = Color.BLACK
        canvas.width = factory.length.toDouble() * 10 + 1
        canvas.height = factory.width.toDouble() * 10 + 1
        val gc = canvas.getGraphicsContext2D()
        gc.fill = Color.WHITE
        gc.fillRect(0.0, 0.0, canvas.width, canvas.height )
        gc.fill = Color.BLACK
        gc.lineWidth = 1.0
        var x = 0.5
        var y = 0.5
        while (x <= canvas.width) {
            gc.moveTo(x, 0.0)
            gc.lineTo(x, canvas.height - 1)
            gc.stroke()
            x += 10.0
        }
        while (y <= canvas.height) {
            gc.moveTo(0.0, y)
            gc.lineTo(canvas.width - 1, y)
            gc.stroke()
            y += 10.0
        }
    }

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
        val x = e.x - e.x % 10
        val y = e.y - e.y % 10
        if ( x < canvas.width - 1  && x >= 0 && y >= 0 && y < canvas.height - 1){
            val coordinate = Coordinate(x.toInt() / 10, y.toInt() / 10)
            if (eraser){
                gc.fill =  Color.WHITE
                coordinateList.remove(coordinate)
            }
            else{
                gc.fill = Color.GREEN
                coordinateList.add(coordinate)
                coordinateList = coordinateList.distinct().toMutableList()
            }
            gc.fillRect(x + 1,y + 1,9.0,9.0)
        }
        canAddCheck()
    }

    @FXML
    fun onAddButtonClick() {
        val minX = coordinateList.minOf {it.x}
        val minY = coordinateList.minOf {it.y}
        coordinateList.forEach {
            it.x -= minX
            it.y -= minY
        }
        val factoryObject = FactoryObject(
            id = if(factory.objects.size > 0) factory.objects.last().first.id + 1 else 0,
            name = objectNameText.text,
            color = colorPicker.value,
            coordinates = coordinateList,
            dateStart = startDatePicker.value,
            dateEnd = endDatePicker.value)
        data.setFactoryObject(factoryObject)

        val insideAddButton = Button()
        val factoryCanvas = Canvas(factory.length.toDouble() * 10 + 1, factory.width.toDouble() * 10 + 1)
        val shape = createShape(factoryObject)
        val group = Group(factoryCanvas, shape)
        dateSlider.min = startDatePicker.value.toEpochDay().toDouble()
        dateSlider.max = endDatePicker.value.toEpochDay().toDouble()
        dateSlider.value = startDatePicker.value.toEpochDay().toDouble()
        dateSlider.setOnMouseReleased {
            drawFactory(factoryCanvas)
        }
        val createStage = this.canvas.scene.window as Stage
        fun createExtraStage(){
            insideAddButton.text = "Add"
            val vBox = VBox()
            vBox.spacing = 5.0
            vBox.alignment = Pos.CENTER
            vBox.children.addAll(group, dateSlider, insideAddButton)
            vBox.prefHeight = 150.0
            vBox.prefWidth = 200.0
            val scene = Scene(vBox)
            createStage.title = "Add Object"
            createStage.scene = scene
            createStage.show()
        }
        createExtraStage()
        drawFactory(factoryCanvas)
        shape.setOnMouseDragged { e ->
            shape.layoutX = (e.sceneX.toInt() / 20) * 10.0
            shape.layoutY = (e.sceneY.toInt() / 20) * 10.0
            val bounds = shape.boundsInParent
            insideAddButton.isDisable = bounds.minX < 0 || bounds.minY < 0 || bounds.maxX > canvas.width || bounds.maxY > canvas.height
        }
        insideAddButton.setOnAction {
            val bounds = shape.boundsInParent
            val factoryPair = Pair(factoryObject, Coordinate(bounds.minX.toInt() / 10, bounds.minY.toInt() / 10))
            factory.objects.add(factoryPair)
            data.setFactoryLayout(factory)
            createStage.close()
        }
    }

    private fun drawFactory(canvas: Canvas){
        val gc = canvas.getGraphicsContext2D()
        gc.clearRect(0.0,0.0, canvas.width, canvas.height)
        var x = 0.5
        var y = 0.5
        val excludedCoordinates =  factory.excludedCoordinates.toMutableList()
        factory.objects.forEach {
            if (it.first.dateStart <= LocalDate.ofEpochDay(dateSlider.value.toLong()) && it.first.dateEnd >= LocalDate.ofEpochDay(dateSlider.value.toLong())){
                it.first.coordinates.forEach {coordinate ->
                    excludedCoordinates.add(Coordinate(coordinate.x + it.second.x, coordinate.y + it.second.y))
                }
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
                    val colorInfo = factory.objects.first{
                        it.first.coordinates.contains(Coordinate(dataX-it.second.x, dataY-it.second.y))
                    }
                    gc.fill = colorInfo.first.color
                    gc.fillRect(x, y, 10.0, 10.0)
                }
                y += 10
            }
            y = 0.5
            x += 10
        }
    }
    private fun createShape(fo: FactoryObject): Shape{
        var shape = Rectangle(0.0,0.0,0.0,0.0) as Shape
        fo.coordinates.forEach {
            shape = Shape.union(shape, Rectangle(it.x * 10.0 + 1,it.y * 10.0 + 1,9.0,9.0))
        }
        shape.fill = fo.color
        return shape
    }
    private fun Double.toUserCoordinate() = (this / 10).toInt()
    @FXML
    fun onCancelButtonClick() {
        val stage = this.canvas.scene.window as Stage
        stage.close()
    }

    fun startDatePickerChanged() {
        canAddCheck()
    }

    fun endDatePickerChanged() {
        canAddCheck()
    }

    private fun canAddCheck(){
        addButton.isDisable = !(startDatePicker.value != null
                && endDatePicker.value != null
                && startDatePicker.value < endDatePicker.value
                && coordinateList.isNotEmpty())
    }

}
