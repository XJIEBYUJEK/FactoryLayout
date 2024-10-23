package com.example.factorylayout.controller

import com.example.factorylayout.*
import com.example.factorylayout.data.SingletonData
import com.example.factorylayout.model.Coordinate
import com.example.factorylayout.model.FactoryObject
import javafx.event.ActionEvent
import javafx.fxml.FXML
import javafx.scene.Group
import javafx.scene.canvas.Canvas
import javafx.scene.control.Button
import javafx.scene.control.ColorPicker
import javafx.scene.control.DatePicker
import javafx.scene.control.Slider
import javafx.scene.control.TextField
import javafx.scene.input.MouseEvent
import javafx.scene.layout.BorderPane
import javafx.scene.shape.Rectangle
import javafx.scene.shape.Shape
import javafx.stage.Stage
import java.time.LocalDate
import kotlin.math.min

class ObjectEditController {

    @FXML
    private lateinit var borderPane: BorderPane

    @FXML
    private lateinit var canvas: Canvas

    @FXML
    private lateinit var groupCanvas: Group

    @FXML
    private lateinit var colorPicker: ColorPicker

    @FXML
    private lateinit var currentDatePicker: DatePicker

    @FXML
    private lateinit var dateSlider: Slider

    @FXML
    private lateinit var endDatePicker: DatePicker

    @FXML
    private lateinit var objectNameText: TextField

    @FXML
    private lateinit var saveButton: Button

    @FXML
    private lateinit var startDatePicker: DatePicker

    private val data = SingletonData.getInstance()
    private var factory = data.getFactoryLayout()
    private var scale = 10.0
    private val selectedObjectId = data.getObjectId()
    private val selectedObjectData = factory.objects.first{
        it.first.id == selectedObjectId
    }
    private val selectedObject = selectedObjectData.first
    private val selectedObjectCoordinate = selectedObjectData.second
    private lateinit var shape: Shape

    @FXML
    fun initialize() {
        datePickersSetup()
        canvasSetup()
        upperMenuSetup()
        isItOkToSave()
    }

    private fun canvasSetup(){
        scale = min(
            min((borderPane.prefHeight-100)/factory.width,
                borderPane.prefWidth/factory.length).toInt().toDouble(),
            50.0)

        canvas.width = factory.length * scale + 1
        canvas.height = factory.width * scale + 1
        val tempFactory = factory.makeCopy()
        tempFactory.objects.remove(selectedObjectData)
        drawFactory(canvas, tempFactory, currentDatePicker.value, scale)
        if(groupCanvas.children.size == 1){
            shape = createShape(selectedObjectData)
            shape.setOnMouseDragged { e ->
                shape.layoutX = (e.sceneX - getCanvasPadding(false)).scaleRefactor(scale)
                shape.layoutY = (e.sceneY - getCanvasPadding(true)).scaleRefactor(scale)
                isItOkToSave()
            }
            groupCanvas.children.add(shape)
        }
    }

    private fun getCanvasPadding(isVertical: Boolean): Double{
        val scene = canvas.scene
        val topToolsSize = 50.0
        val bottomToolsSize = 30.0
        return if(isVertical){
            (scene.height - topToolsSize - bottomToolsSize - canvas.height) / 2 + topToolsSize
        } else{
            (scene.width - canvas.width) / 2
        }
    }
    private fun datePickersSetup(){
        if (currentDatePicker.value == null) {
            val dateAtTheMoment = LocalDate.now()
            if (dateCheck(selectedObject, dateAtTheMoment)){
                currentDatePicker.value = dateAtTheMoment
            }
            else {
                currentDatePicker.value = selectedObject.dateStart
            }
        }
        if (startDatePicker.value == null && endDatePicker.value == null){
            startDatePicker.value = selectedObject.dateStart
            endDatePicker.value = selectedObject.dateEnd
            updateSlider(true)
        }
    }

    private fun isItOkToSave(){
        val bounds = shape.boundsInParent
        val isFigureOutsideLayout = bounds.minX < 0 || bounds.minY < 0 || bounds.maxX > canvas.width || bounds.maxY > canvas.height
        val isDateCorrect = (startDatePicker.value != null || endDatePicker.value != null)
        saveButton.isDisable = isFigureOutsideLayout || !isDateCorrect
    }

    private fun upperMenuSetup(){
        colorPicker.value = selectedObject.color
        objectNameText.text = selectedObject.name
    }
    @FXML
    fun onBackPressed() {
        val stage = this.canvas.scene.window as Stage
        stage.close()
    }

    @FXML
    fun onColorPickerAction(){
        shape.fill = colorPicker.value
    }

    @FXML
    fun onSaveButtonClick(event: ActionEvent) {
        selectedObject.color = colorPicker.value
        selectedObject.dateStart = startDatePicker.value
        selectedObject.dateEnd = endDatePicker.value
        selectedObject.name = objectNameText.text
        val bounds = shape.boundsInParent
        selectedObjectCoordinate.x = bounds.minX.toUserCoordinate(scale)
        selectedObjectCoordinate.y = bounds.minY.toUserCoordinate(scale)
        data.setFactoryLayout(factory)
        val stage = this.canvas.scene.window as Stage
        stage.close()
    }

    @FXML
    fun onReleasedSlider() {
        updateDatePickerFromSlider(dateSlider, currentDatePicker)
        initialize()
    }

    @FXML
    fun onDragSlider(event: MouseEvent) {
        updateDatePickerFromSlider(dateSlider, currentDatePicker)
        initialize()
    }

    @FXML
    fun onEndDatePickerAction() {
        val value = endDatePicker.value
        if (value != null){
            if (value < currentDatePicker.value) {
                currentDatePicker.value = value
                initialize()
            }
            if (startDatePicker.value != null && value < startDatePicker.value) {
                updateSlider(false)
                startDatePicker.value = null
            }
            if (startDatePicker.value != null){
                updateSlider(true)
            }
        }
        else{
            updateSlider(false)
        }
        isItOkToSave()
    }

    @FXML
    fun onStartDatePickerAction() {
        val value = startDatePicker.value
        if (value != null){
            if (value > currentDatePicker.value){
                currentDatePicker.value = value
                initialize()
            }
            if (endDatePicker.value != null && value > endDatePicker.value) {
                endDatePicker.value = null
                updateSlider(false)
            }
            if (endDatePicker.value != null){
                updateSlider(true)
            }
        }
        else{
            updateSlider(false)
        }
        isItOkToSave()
    }

    @FXML
    fun onCurrentDatePickerAction() {
        currentDatePickerSettings(dateSlider, startDatePicker, endDatePicker, currentDatePicker)
        initialize()
    }

    private fun createShape(fo: Pair<FactoryObject,Coordinate>): Shape {
        var shape = Rectangle(0.0,0.0,0.0,0.0) as Shape
        fo.first.coordinates.forEach {
            shape = Shape.union(shape, Rectangle(it.x * scale + 1, it.y * scale + 1, scale - 1, scale - 1))
        }
        shape.fill = fo.first.color
        shape.layoutX = fo.second.x * scale
        shape.layoutY = fo.second.y * scale
        return shape
    }
    private fun updateSlider(isVisible: Boolean) = updateSlider(isVisible, dateSlider, startDatePicker, endDatePicker, currentDatePicker)

}

