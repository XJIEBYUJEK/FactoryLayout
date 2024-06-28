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
import javafx.scene.control.*
import javafx.scene.input.MouseEvent
import javafx.scene.layout.HBox
import javafx.scene.layout.VBox
import javafx.scene.paint.Color
import javafx.scene.shape.Rectangle
import javafx.scene.shape.Shape
import javafx.stage.FileChooser
import javafx.stage.Stage
import java.io.File
import java.nio.file.Paths
import java.time.LocalDate


class FactoryController {

    @FXML
    lateinit var topHbox: HBox

    @FXML
    lateinit var leftVbox: VBox

    @FXML
    lateinit var infoTextField: TextArea

    @FXML
    lateinit var errorsLabel: Label

    @FXML
    lateinit var errorsButton: Button

    @FXML
    lateinit var textField: TextArea

    @FXML
    lateinit var dateSlider: Slider

    @FXML
    lateinit var endDatePicker: DatePicker

    @FXML
    lateinit var startDatePicker: DatePicker

    @FXML
    lateinit var currentDatePicker: DatePicker

    @FXML
    lateinit var listView: ListView<Pair<FactoryObject, Coordinate>>

    @FXML
    private lateinit var canvas: Canvas

    private val data = SingletonData.getInstance()
    private var factory = data.getFactoryLayout()
    private var insideDateSlider = Slider()

    @FXML
    fun initialize() {
        textFieldSetup()
        datePickersSetup()
        listObjectsSetup()
        factoryMapSetup()
        checkErrors()
    }

    fun onBackPressed() {
        val stage = this.canvas.scene.window as Stage
        val loader = FXMLLoader(FactoryApplication::class.java.getResource("MainView.fxml"))
        val scene = Scene(loader.load(), 200.0, 180.0)
        stage.scene = scene
        stage.title = "Factory Layout Manager"
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
        //File("${data.getFileName()}.json").writeText(factory.toJsonString())
        File(data.getFileName()).writeText(factory.toJsonString())
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
            val insideStartDatePicker = DatePicker(selectedItem.first.dateStart)
            val insideEndDatePicker = DatePicker(selectedItem.first.dateEnd)
            val hBox = HBox(insideStartDatePicker, insideEndDatePicker)
            insideDateSlider.min = insideStartDatePicker.value.toEpochDay().toDouble()
            insideDateSlider.max = insideEndDatePicker.value.toEpochDay().toDouble()
            insideDateSlider.value = insideStartDatePicker.value.toEpochDay().toDouble()
            insideDateSlider.setOnMouseReleased {
                drawFactory(factoryCanvas, tempFactory, LocalDate.ofEpochDay(insideDateSlider.value.toLong()))
            }
            val vBox = VBox()
            vBox.spacing = 5.0
            vBox.alignment = Pos.CENTER
            vBox.children.addAll(textField, colorPicker, group, hBox, insideDateSlider, insideEditButton)
            val scene = Scene(vBox)
            stage.scene = scene
            stage.show()
            drawFactory(factoryCanvas, tempFactory, LocalDate.ofEpochDay(insideDateSlider.value.toLong()))
            shape.setOnMouseDragged { e ->
                shape.layoutX = (e.sceneX.toInt() / 20) * 10.0
                shape.layoutY = (e.sceneY.toInt() / 20) * 10.0
                val bounds = shape.boundsInParent
                insideEditButton.isDisable = bounds.minX < 0 || bounds.minY < 0 || bounds.maxX > canvas.width || bounds.maxY > canvas.height
            }
            insideEditButton.setOnAction {
                val bounds = shape.boundsInParent
                selectedItem.second.x = bounds.minX.toInt() / 10
                selectedItem.second.y = bounds.minY.toInt() / 10
                selectedItem.first.color = colorPicker.value
                selectedItem.first.name = textField.text
                selectedItem.first.dateStart = insideStartDatePicker.value
                selectedItem.first.dateEnd = insideEndDatePicker.value
                stage.close()
                initialize()
            }
        }
    }

    fun onCurrentDatePickerAction() {
        if(startDatePicker.value != null && startDatePicker.value > currentDatePicker.value)
            startDatePicker.value = currentDatePicker.value
        if(endDatePicker.value != null && endDatePicker.value < currentDatePicker.value)
            endDatePicker.value = currentDatePicker.value
        if (startDatePicker.value != null && endDatePicker.value != null) updateSlider(true)
        initialize()
    }

    fun onStartDatePickerAction() {
        val value = startDatePicker.value
        if (value != null){
            if (value > currentDatePicker.value){
                currentDatePicker.value = value
                initialize()
            }
            if (endDatePicker.value != null && value > endDatePicker.value) endDatePicker.value = null
            if (endDatePicker.value != null){
                updateSlider(true)
            }
        }
        else{
            updateSlider(false)
        }
    }

    fun onEndDatePickerAction() {
        val value = endDatePicker.value
        if (value != null){
            if (value < currentDatePicker.value) {
                currentDatePicker.value = value
                initialize()
            }
            if (startDatePicker.value != null && value < startDatePicker.value) startDatePicker.value = null
            if (startDatePicker.value != null){
                updateSlider(true)
            }
        }
        else{
            updateSlider(false)
        }
    }

    fun onDragSlider() {
        currentDatePicker.value = LocalDate.ofEpochDay(dateSlider.value.toLong())
        initialize()
    }

    private fun drawFactory(canvas: Canvas, factory: Factory, currentDate: LocalDate){
        val gc = canvas.getGraphicsContext2D()
        gc.clearRect(0.0,0.0, canvas.width, canvas.height)
        var x = 0.5
        var y = 0.5
        val excludedCoordinates =  factory.excludedCoordinates.toMutableList()
        factory.objects.forEach {
            if (it.first.dateStart <= currentDate && it.first.dateEnd >= currentDate){
                it.first.coordinates.forEach {coordinate ->
                    excludedCoordinates.add(Coordinate(coordinate.x + it.second.x, coordinate.y + it.second.y))
                }
            }
        }

        while (x < canvas.width - 0.5){
            while (y < canvas.height - 0.5){
                val dataX = x.toUserCoordinate()
                val dataY = y.toUserCoordinate()
                gc.stroke = Color.web("#DDDDDD")
                gc.lineWidth = 1.0
                if (!excludedCoordinates.contains(Coordinate(dataX, dataY))){
                    gc.fill = Color.WHITE
                    gc.fillRect(x, y, 10.0, 10.0)
                    gc.strokeRect(x, y, 10.0, 10.0)
                }
                else if(!factory.excludedCoordinates.contains(Coordinate(dataX,dataY))){
                    val colorInfo = factory.objects.first{
                        it.first.coordinates.contains(Coordinate(dataX-it.second.x, dataY-it.second.y))
                    }
                    gc.fill = colorInfo.first.color
                    gc.fillRect(x, y, 10.0, 10.0)
                    gc.strokeRect(x, y, 10.0, 10.0)
                }
                y += 10
            }
            y = 0.5
            x += 10
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
        return shape
    }

    private fun datePickersSetup(){
        if (currentDatePicker.value == null) currentDatePicker.value = LocalDate.now()
    }

    private fun listObjectsSetup(){
        listView.items = FXCollections.observableList(factory.objects)
        listView.setCellFactory { FactoryObjectCellFactory() }
        listView.setOnMouseClicked {
            val index = listView.selectionModel.selectedIndex
            val pair = listView.items[index]
            infoTextField.text = "Имя: ${pair.first.name}\n" +
                    "Начальная дата: ${pair.first.dateStart.toCustomString()}\n" +
                    "Конечная дата: ${pair.first.dateEnd.toCustomString()}"
        }
    }

    private fun factoryMapSetup(){
        canvas.width = factory.length.toDouble() * 10 + 1
        canvas.height = factory.width.toDouble() * 10 + 1

        drawFactory(canvas, factory, currentDatePicker.value)
    }

    private fun textFieldSetup(){
        textField.text = ""
        textField.isVisible = false
        errorsButton.text = "Show Errors"
        canvas.isVisible = true
        errorsLabel.isVisible = false
    }

    private fun updateSlider(isVisible: Boolean){
        if (isVisible){
            dateSlider.min = startDatePicker.value.toEpochDay().toDouble()
            dateSlider.max = endDatePicker.value.toEpochDay().toDouble()
            dateSlider.value = currentDatePicker.value.toEpochDay().toDouble()
        }
        dateSlider.isVisible = isVisible
    }

    private fun Double.toUserCoordinate() = (this / 10).toInt()
    fun onErrorsButtonClick() {
        if (errorsButton.text == "Show Errors"){
            errorsButton.text = "Hide Errors"
            textField.isVisible = true
            canvas.isVisible = false
        }
        else{
            errorsButton.text = "Show Errors"
            textField.isVisible = false
            canvas.isVisible = true
        }
    }

    private fun checkErrors(){
        for (pair in factory.objects){
            val collisionsId = mutableListOf<Int>()
            for (coordinate in pair.first.coordinates){
                val shiftedX = coordinate.x + pair.second.x
                val shiftedY = coordinate.y + pair.second.y
                if (factory.excludedCoordinates.contains(Coordinate(shiftedX, shiftedY))) {
                    collisionsId.add(-1)
                }
                for (comparedPair in factory.objects){
                    if (comparedPair != pair){
                        if (comparedPair.first.dateStart >= pair.first.dateStart && comparedPair.first.dateStart <= pair.first.dateEnd
                            || comparedPair.first.dateStart <= pair.first.dateStart && comparedPair.first.dateEnd >= pair.first.dateStart){
                            comparedPair.first.coordinates.forEach {
                                if (it.x + comparedPair.second.x == shiftedX && it.y + comparedPair.second.y == shiftedY){
                                    collisionsId.add(comparedPair.first.id)
                                }
                            }
                        }
                    }

                }
            }
            collisionsId.distinct().forEach{
                val objectNumber = listView.items.indexOf(pair)
                if (it == -1) {
                    textField.text += "Object $objectNumber. ${pair.first.name} has collisions with FactoryLayout.\n"
                } else {
                    val obj = listView.items.first{ obj -> obj.first.id == it}
                    textField.text += "Object $objectNumber. ${pair.first.name} has collisions with ${listView.items.indexOf(obj)}. ${obj.first.name}\n"
                }
            }
        }
        if (textField.text != "") errorsLabel.isVisible = true
    }

    fun onMouseExitedCanvas() {
        infoTextField.text = ""
    }

    fun onMouseMovedInsideCanvas(e: MouseEvent) {
        val x = (e.sceneX - canvas.layoutX - leftVbox.width).toInt() / 10
        val y = (e.sceneY - canvas.layoutY - topHbox.height).toInt() / 10
        infoTextField.text = "x = $x \ny = $y\n"
        if (factory.excludedCoordinates.contains(Coordinate(x, y))){
            infoTextField.text += "Excluded coordinate\n"
        }else{
            if (textField.text == ""){
                try {
                    val pair = factory.objects.first{
                        it.first.dateStart <= currentDatePicker.value
                                && it.first.dateEnd >= currentDatePicker.value
                                && it.first.coordinates.contains(Coordinate(x - it.second.x, y - it.second.y))
                    }
                    infoTextField.text += "Имя: ${pair.first.name}\n" +
                            "Начальная дата: ${pair.first.dateStart.toCustomString()}\n" +
                            "Конечная дата: ${pair.first.dateEnd.toCustomString()}"
                } catch (_: Exception){}
            }
        }

    }

    fun onMouseClickedInsideCanvas(e: MouseEvent) {
        val x = (e.sceneX - canvas.layoutX - leftVbox.width).toInt() / 10
        val y = (e.sceneY - canvas.layoutY - topHbox.height).toInt() / 10
        if (textField.text == ""){
            try {
                val pair = factory.objects.first{
                    it.first.dateStart <= currentDatePicker.value
                            && it.first.dateEnd >= currentDatePicker.value
                            && it.first.coordinates.contains(Coordinate(x - it.second.x, y - it.second.y))
                }
                listView.selectionModel.select(listView.items.indexOf(pair))
            } catch (_: Exception){}
        }
    }

    private fun LocalDate.toCustomString() =
        if (this.dayOfMonth < 10) {"0${this.dayOfMonth}."} else {"${this.dayOfMonth}."} +
        if (this.monthValue < 10) {"0${this.monthValue}."} else {"${this.monthValue}."} +
                "${this.year}"

    fun onSaveAsButtonClicked() {
        val stage = this.canvas.scene.window as Stage
        val fileChooser = FileChooser()
        fileChooser.extensionFilters.add(FileChooser.ExtensionFilter("json Files", "*.json"))
        val currentPath = Paths.get(".").toAbsolutePath().normalize().toString()
        fileChooser.initialDirectory = File(currentPath)
        val file = fileChooser.showSaveDialog(stage)
        file.writeText(factory.toJsonString())
    }

    fun onReleasedSlider() {
        currentDatePicker.value = LocalDate.ofEpochDay(dateSlider.value.toLong())
        initialize()
    }

}
