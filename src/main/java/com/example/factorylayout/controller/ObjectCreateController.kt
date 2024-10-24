package com.example.factorylayout.controller

import com.example.factorylayout.data.SingletonData
import com.example.factorylayout.dateCheck
import com.example.factorylayout.drawFactory
import com.example.factorylayout.model.Coordinate
import com.example.factorylayout.model.FactoryObject
import com.example.factorylayout.scaleRefactor
import com.example.factorylayout.toUserCoordinate
import javafx.fxml.FXML
import javafx.geometry.Pos
import javafx.scene.Group
import javafx.scene.Scene
import javafx.scene.canvas.Canvas
import javafx.scene.control.*
import javafx.scene.input.MouseButton
import javafx.scene.input.MouseEvent
import javafx.scene.layout.BorderPane
import javafx.scene.layout.VBox
import javafx.scene.paint.Color
import javafx.scene.shape.Rectangle
import javafx.scene.shape.Shape
import javafx.stage.Stage
import java.time.LocalDate
import kotlin.math.min

class ObjectCreateController {

    @FXML
    lateinit var borderPane: BorderPane

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

    private var scale = 10.0

    @FXML
    fun initialize() {
        colorPicker.value = Color.BLACK
        canvasSetup()
    }

    private fun canvasSetup(){
        scale = min(min((borderPane.prefHeight-100)/factory.width, borderPane.prefWidth/factory.length).toInt().toDouble(), 50.0)
        canvas.width = factory.length * scale + 1
        canvas.height = factory.width * scale + 1
        drawFactory(canvas, factory, LocalDate.now(), scale, true)
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
        val gc = canvas.graphicsContext2D
        val x = e.x - e.x % scale
        val y = e.y - e.y % scale
        if ( x < canvas.width - 1  && x >= 0 && y >= 0 && y < canvas.height - 1){
            val coordinate = Coordinate(x.toInt() / scale.toInt(), y.toInt() / scale.toInt())
            if (!factory.excludedCoordinates.contains(coordinate)){
                if (eraser){
                    gc.fill =  Color.WHITE
                    coordinateList.remove(coordinate)
                }
                else{
                    gc.fill = Color.GREEN
                    coordinateList.add(coordinate)
                    coordinateList = coordinateList.distinct().toMutableList()
                }
                gc.fillRect(x + 1, y + 1, scale - 1, scale - 1)
            }
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
        val factoryCanvas = Canvas(factory.length * scale + 1, factory.width * scale + 1)
        val shape = createShape(factoryObject)
        val group = Group(factoryCanvas, shape)
        dateSlider.min = startDatePicker.value.toEpochDay().toDouble()
        dateSlider.max = endDatePicker.value.toEpochDay().toDouble()
        dateSlider.value = startDatePicker.value.toEpochDay().toDouble()
        dateSlider.setOnMouseDragged {
            drawFactory(factoryCanvas, factory, LocalDate.ofEpochDay(dateSlider.value.toLong()), scale)
        }
        val createStage = this.canvas.scene.window as Stage
        fun createExtraStage(){
            insideAddButton.text = "Добавить"
            val vBox = VBox()
            vBox.spacing = 5.0
            vBox.alignment = Pos.CENTER
            vBox.children.addAll(group, dateSlider, insideAddButton)
            val scene = Scene(vBox)
            createStage.title = "Добавление объекта"
            createStage.scene = scene
            createStage.isResizable = false
            createStage.show()
        }
        createExtraStage()
        drawFactory(factoryCanvas, factory, LocalDate.ofEpochDay(dateSlider.value.toLong()), scale)
        shape.setOnMouseDragged { e ->
            shape.layoutX = (e.sceneX - factoryCanvas.layoutX).scaleRefactor(scale)
            shape.layoutY = (e.sceneY - factoryCanvas.layoutY).scaleRefactor(scale)
            val bounds = shape.boundsInParent
            insideAddButton.isDisable = bounds.minX < 0 || bounds.minY < 0 || bounds.maxX > canvas.width || bounds.maxY > canvas.height
        }
        insideAddButton.setOnAction {
            val bounds = shape.boundsInParent
            val factoryPair = Pair(factoryObject, Coordinate(bounds.minX.toInt() / scale.toInt(), bounds.minY.toInt() / scale.toInt()))
            factory.objects.add(factoryPair)
            data.setFactoryLayout(factory)
            createStage.close()
        }
    }

    private fun createShape(fo: FactoryObject): Shape{
        var shape = Rectangle(0.0,0.0,0.0,0.0) as Shape
        fo.coordinates.forEach {
            shape = Shape.union(shape, Rectangle(it.x * scale + 1, it.y * scale + 1, scale - 1, scale - 1))
        }
        shape.fill = fo.color
        return shape
    }
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
