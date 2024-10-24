package com.example.factorylayout.controller

import com.example.factorylayout.*
import com.example.factorylayout.data.SingletonData
import com.example.factorylayout.model.Coordinate
import com.example.factorylayout.model.FactoryObject
import javafx.collections.FXCollections
import javafx.event.ActionEvent
import javafx.fxml.FXML
import javafx.fxml.FXMLLoader
import javafx.geometry.Pos
import javafx.scene.Group
import javafx.scene.Parent
import javafx.scene.Scene
import javafx.scene.canvas.Canvas
import javafx.scene.control.Button
import javafx.scene.control.ChoiceBox
import javafx.scene.control.ColorPicker
import javafx.scene.control.DatePicker
import javafx.scene.control.Label
import javafx.scene.control.Slider
import javafx.scene.control.TextField
import javafx.scene.input.MouseButton
import javafx.scene.input.MouseEvent
import javafx.scene.layout.BorderPane
import javafx.scene.layout.VBox
import javafx.scene.paint.Color
import javafx.scene.shape.Rectangle
import javafx.scene.shape.Shape
import javafx.scene.transform.Rotate
import javafx.stage.Stage
import java.lang.Math.toRadians
import java.time.LocalDate
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin

class ObjectEditController {

    @FXML
    lateinit var editSizeButton: Button

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
    private var selectedObjectId = data.getObjectId()
    private var selectedObjectData = factory.objects.first{
        it.first.id == selectedObjectId
    }
    private var selectedObject = selectedObjectData.first
    private var selectedObjectCoordinate = selectedObjectData.second
    private lateinit var shape: Shape
    private var flag: Boolean = true
    private var coordinateList = selectedObject.coordinates.map {
        Coordinate(it.x + selectedObjectCoordinate.x, it.y + selectedObjectCoordinate.y)
    }.toMutableList()

    @FXML
    fun initialize() {
        if(selectedObject.childObjects.isNotEmpty() && flag){

            dialogOptionSetup()
        } else {
            datePickersSetup()
            canvasSetup()
            upperMenuSetup()
            isItOkToSave()
            if (selectedObject.parentObject != null && flag){
                editSizeButton.isDisable = true
            }
        }
    }

    private fun dialogOptionSetup(){
        flag = false
        val listOfDates = listOf("${selectedObject.dateStart.toCustomString()} - ${selectedObject.dateEnd.toCustomString()}") +
                selectedObject.childObjects.map { id ->
                    val child = factory.objects.first { it.first.id == id }.first
                    "${child.dateStart.toCustomString()} - ${child.dateEnd.toCustomString()}"
                }
        val label = Label("Выберите редактируемый\n промежуток")
        val choiceBox = ChoiceBox(FXCollections.observableList(listOfDates))
        val nextButton = Button("Далее")
        choiceBox.setOnAction {
            nextButton.isDisable = choiceBox.value == null
        }
        nextButton.isDisable = true
        val vBox = VBox()
        vBox.spacing = 10.0
        vBox.alignment = Pos.CENTER
        vBox.children.addAll(label, choiceBox, nextButton)
        val scene = Scene(vBox)
        val createStage = Stage()
        createStage.scene = scene
        createStage.isResizable = false
        createStage.height = 200.0
        createStage.width = 200.0
        nextButton.setOnMouseClicked {
            createStage.close()
            val answer = choiceBox.value
            when(val index = listOfDates.indexOf(answer)){
                -1 -> onBackPressed()
                0 -> initialize()
                else -> {
                    selectedObjectId = selectedObject.childObjects[index-1]
                    selectedObjectData = factory.objects.first{
                        it.first.id == selectedObjectId
                    }
                    selectedObject = selectedObjectData.first
                    selectedObjectCoordinate = selectedObjectData.second
                    initialize()
                }
            }
        }
        createStage.showAndWait()
    }

    @FXML
    fun onEditSizeButtonClick() {
        //first scene
        val textLabel = Label("Выберите промежуток дат")
        val firstDatePicker = DatePicker(selectedObject.dateStart)
        val secondDatePicker = DatePicker(selectedObject.dateEnd)
        val nextButton = Button("Далее")
        fun DatePicker.correctDatePickerCheck() = dateCheck(selectedObject, this.value) && firstDatePicker.value < secondDatePicker.value
        firstDatePicker.setOnAction {
            nextButton.isDisable = !firstDatePicker.correctDatePickerCheck()
        }
        secondDatePicker.setOnAction {
            nextButton.isDisable = !secondDatePicker.correctDatePickerCheck()
        }
        val vBox = VBox()
        vBox.spacing = 10.0
        vBox.alignment = Pos.CENTER
        vBox.children.addAll(textLabel, firstDatePicker, secondDatePicker, nextButton)
        val scene = Scene(vBox)
        val createStage = Stage()
        createStage.scene = scene
        createStage.isResizable = false
        createStage.height = 200.0
        createStage.width = 200.0
        createStage.show()

        //second scene
        nextButton.setOnMouseClicked {
            textLabel.text = "Измените размер, если необходимо"
            val extraCanvas = Canvas(factory.length * scale + 1, factory.width * scale + 1)
            drawFactory(extraCanvas, factory, firstDatePicker.value, scale)
            extraCanvas.setOnMouseClicked {
                when (it.button){
                    MouseButton.PRIMARY -> colorPixels(it, false, extraCanvas)
                    MouseButton.SECONDARY -> colorPixels(it, true, extraCanvas)
                    else -> {}
                }
            }
            extraCanvas.setOnMouseDragged {
                when (it.button){
                    MouseButton.PRIMARY -> colorPixels(it, false, extraCanvas)
                    MouseButton.SECONDARY -> colorPixels(it, true, extraCanvas)
                    else -> {}
                }
            }
            vBox.children.clear()
            vBox.children.addAll(textLabel, extraCanvas, nextButton)
            createStage.height = extraCanvas.height + 100.0
            createStage.width = extraCanvas.width + 30.0
            nextButton.setOnMouseClicked {
                val minX = coordinateList.minOf {it.x}
                val minY = coordinateList.minOf {it.y}
                coordinateList.forEach {
                    it.x -= minX
                    it.y -= minY
                }
                if (firstDatePicker.value == selectedObject.dateStart && secondDatePicker.value == selectedObject.dateEnd){
                    selectedObject.coordinates = coordinateList
                } else {
                    fun addChildObject(newFactoryObject: FactoryObject){
                        if (selectedObject.parentObject == null){
                            selectedObject.childObjects.add(newFactoryObject.id)
                        }
                        else{
                            factory.objects.first {
                                it.first.id == selectedObject.parentObject
                            }.first.childObjects.add(newFactoryObject.id)
                        }
                    }
                    val newFactoryObject = FactoryObject(
                        id = if(factory.objects.size > 0) factory.objects.last().first.id + 1 else 0,
                        name = selectedObject.name,
                        color = selectedObject.color,
                        coordinates = coordinateList,
                        dateStart = firstDatePicker.value,
                        dateEnd = secondDatePicker.value,
                        parentObject = selectedObject.parentObject ?: selectedObjectId
                    )
                    factory.objects.add(Pair(newFactoryObject, selectedObjectCoordinate.copy()))
                    addChildObject(newFactoryObject)

                    if (firstDatePicker.value > selectedObject.dateStart){
                        val tempDate = selectedObject.dateEnd
                        selectedObject.dateEnd = firstDatePicker.value.minusDays(1)
                        if (secondDatePicker.value < tempDate){
                            val extraFactoryObject = FactoryObject(
                                id = factory.objects.last().first.id + 2,
                                name = selectedObject.name,
                                color = selectedObject.color,
                                coordinates = selectedObject.coordinates,
                                dateStart = secondDatePicker.value.plusDays(1),
                                dateEnd = tempDate,
                                parentObject = selectedObject.parentObject ?: selectedObjectId
                            )
                            factory.objects.add(Pair(extraFactoryObject, selectedObjectCoordinate.copy()))
                            addChildObject(extraFactoryObject)
                        }
                    } else if(secondDatePicker.value < selectedObject.dateEnd){
                        selectedObject.dateStart = secondDatePicker.value.plusDays(1)
                    }
                    data.setObjectId(newFactoryObject.id)
                }
                createStage.close()
                val stage = this.canvas.scene.window as Stage
                val loader = FXMLLoader(FactoryApplication::class.java.getResource("ObjectEditView.fxml"))
                stage.scene = Scene(loader.load() as Parent)
                stage.show()
            }
        }
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
            shapeSetup()
        }
    }

    private fun shapeSetup(){
        shape = createShape(selectedObjectData)
        shape.setOnMouseDragged { e ->
            shape.layoutX = (e.sceneX - getCanvasPadding(false)).scaleRefactor(scale)
            shape.layoutY = (e.sceneY - getCanvasPadding(true)).scaleRefactor(scale)
            isItOkToSave()
        }
        val rotate = Rotate()
        shape.transforms.add(rotate)
        shape.setOnScroll {
            val delta = it.deltaY
            if (delta < 0){
                rotate.angle -= 90.0
                rotateCoordinates(selectedObject.coordinates, false)
            }
            else{
                rotate.angle += 90.0
                rotateCoordinates(selectedObject.coordinates, true)
            }

        }
        groupCanvas.children.add(shape)
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
        if (selectedObject.parentObject != null){
            startDatePicker.isDisable = true
            endDatePicker.isDisable = true
        }
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
        //selectedObject.color = colorPicker.value
        selectedObject.dateStart = startDatePicker.value
        selectedObject.dateEnd = endDatePicker.value
        //selectedObject.name = objectNameText.text
        val bounds = shape.boundsInParent
        selectedObjectCoordinate.x = bounds.minX.toUserCoordinate(scale)
        selectedObjectCoordinate.y = bounds.minY.toUserCoordinate(scale)
        fun parentAndChildsSetup(fo: FactoryObject){
            val parent = factory.objects.first { it.first.id == fo.parentObject }.first
            parent.name = objectNameText.text
            parent.color = colorPicker.value
            parent.childObjects.forEach { id ->
                val child = factory.objects.first { it.first.id == id }.first
                child.name = objectNameText.text
                child.color = colorPicker.value
            }
        }
        parentAndChildsSetup(selectedObject)
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

    private fun rotateCoordinates(coordinates: List<Coordinate>, isPositive: Boolean){
        val sinAngle = if (isPositive) 1 else -1 //sin(pi/2) or (-pi/2)

        coordinates.forEach { coordinate ->
            // cos is always 0
            val newX = - coordinate.y * sinAngle
            val newY = coordinate.x * sinAngle
            coordinate.x = newX
            coordinate.y = newY
        }
        val minY = coordinates.minOf { it.y }
        val minX = coordinates.minOf { it.x }
        coordinates.forEach {
            it.x -= minX
            it.y -= minY
        }
        isItOkToSave()
    }

    private fun colorPixels(e: MouseEvent, eraser: Boolean, canvas: Canvas){
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
                    gc.fill = selectedObject.color
                    coordinateList.add(coordinate)
                    coordinateList = coordinateList.distinct().toMutableList()
                }
                gc.fillRect(x + 1, y + 1, scale - 1, scale - 1)
            }
        }
    }
}

