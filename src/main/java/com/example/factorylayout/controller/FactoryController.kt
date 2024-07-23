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
import javafx.print.PrinterJob
import javafx.scene.Group
import javafx.scene.Parent
import javafx.scene.Scene
import javafx.scene.canvas.Canvas
import javafx.scene.control.*
import javafx.scene.input.MouseEvent
import javafx.scene.input.ScrollEvent
import javafx.scene.layout.BorderPane
import javafx.scene.layout.HBox
import javafx.scene.layout.VBox
import javafx.scene.paint.Color
import javafx.scene.shape.Rectangle
import javafx.scene.shape.Shape
import javafx.scene.text.Font
import javafx.stage.FileChooser
import javafx.stage.Stage
import java.io.File
import java.nio.file.Paths
import java.time.LocalDate
import kotlin.math.min


class FactoryController {

    @FXML
    lateinit var canvasBorderPane: BorderPane

    @FXML
    lateinit var factoryTextField: TextField

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

    private var scaleMain = 10.0

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
        stage.title = "Планировщик Сборочного Цеха"
        stage.isResizable = false
        stage.show()
    }

    fun onAddObjectPressed() {
        val stage = Stage()
        stage.title = "Добавление объекта"
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
            insideEditButton.text = "Изменить"
            val factoryCanvas = Canvas(factory.length * scaleMain + 1, factory.width * scaleMain + 1)
            val shape = createShape(selectedItem)
            val stage = Stage()
            val group = Group(factoryCanvas, shape)
            stage.title = "Изменение объекта"
            val textField = TextField()
            textField.text = selectedItem.first.name
            val colorPicker = ColorPicker()
            colorPicker.value = selectedItem.first.color
            val currentDateLabel = Label()
            val insideStartDatePicker = DatePicker(selectedItem.first.dateStart)
            fun updateInsideSlider(){
                insideDateSlider.value = insideStartDatePicker.value.toEpochDay().toDouble()
                currentDateLabel.text = LocalDate.ofEpochDay(insideDateSlider.value.toLong()).toCustomString()
                drawFactory(factoryCanvas, tempFactory, LocalDate.ofEpochDay(insideDateSlider.value.toLong()), scaleMain)
            }
            insideStartDatePicker.setOnAction {
                insideDateSlider.min = insideStartDatePicker.value.toEpochDay().toDouble()
                updateInsideSlider()
            }
            val insideEndDatePicker = DatePicker(selectedItem.first.dateEnd)
            insideEndDatePicker.setOnAction {
                insideDateSlider.max = insideEndDatePicker.value.toEpochDay().toDouble()
                updateInsideSlider()
            }
            val hBox = HBox(insideStartDatePicker, insideEndDatePicker, currentDateLabel)
            insideDateSlider.min = insideStartDatePicker.value.toEpochDay().toDouble()
            insideDateSlider.max = insideEndDatePicker.value.toEpochDay().toDouble()
            updateInsideSlider()
            insideDateSlider.setOnMouseDragged {
                currentDateLabel.text = LocalDate.ofEpochDay(insideDateSlider.value.toLong()).toCustomString()
                drawFactory(factoryCanvas, tempFactory, LocalDate.ofEpochDay(insideDateSlider.value.toLong()), scaleMain)
            }
            val vBox = VBox()
            vBox.spacing = 10.0
            vBox.alignment = Pos.CENTER
            vBox.children.addAll(textField, colorPicker, group, hBox, insideDateSlider, insideEditButton)
            val scene = Scene(vBox)
            stage.scene = scene
            stage.isResizable = false
            stage.show()
            shape.setOnMouseDragged { e ->
                shape.layoutX = ((e.sceneX - factoryCanvas.layoutX - canvasBorderPane.padding.left) / scaleMain).toInt() * scaleMain
                shape.layoutY = ((e.sceneY - factoryCanvas.layoutY - colorPicker.height - textField.height - canvasBorderPane.padding.top) / scaleMain).toInt() * scaleMain
                val bounds = shape.boundsInParent
                insideEditButton.isDisable = bounds.minX < 0 || bounds.minY < 0 || bounds.maxX > canvas.width || bounds.maxY > canvas.height
            }
            insideEditButton.setOnAction {
                val bounds = shape.boundsInParent
                selectedItem.second.x = bounds.minX.toInt() / scaleMain.toInt()
                selectedItem.second.y = bounds.minY.toInt() / scaleMain.toInt()
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

    private fun drawFactory(canvas: Canvas, factory: Factory, currentDate: LocalDate, scale: Double){
        val gc = canvas.graphicsContext2D
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
                val dataX = x.toUserCoordinate(scale)
                val dataY = y.toUserCoordinate(scale)
                gc.lineWidth = 1.0
                gc.stroke = Color.web("#DDDDDD")
                if (!excludedCoordinates.contains(Coordinate(dataX, dataY))){
                    gc.fill = Color.WHITE
                    gc.fillRect(x, y, scale, scale)
                    gc.strokeRect(x, y, scale, scale)
                }
                else if(!factory.excludedCoordinates.contains(Coordinate(dataX,dataY))){
                    val colorInfo = factory.objects.first{
                        it.first.coordinates.contains(Coordinate(dataX-it.second.x, dataY-it.second.y))
                    }
                    gc.fill = colorInfo.first.color
                    gc.fillRect(x, y, scale, scale)
                    gc.strokeRect(x, y, scale, scale)
                }
                if (!factory.excludedCoordinates.contains(Coordinate(dataX,dataY))){
                    gc.lineWidth = 2.0
                    gc.stroke = Color.RED
                    if (factory.excludedCoordinates.contains(Coordinate(dataX,dataY - 1)) || dataY == 0){
                        gc.strokeLine(x, y, x + scale, y)
                    }
                    if (factory.excludedCoordinates.contains(Coordinate(dataX,dataY + 1)) || dataY == factory.width - 1){
                        gc.strokeLine(x, y + scale, x + scale, y + scale)
                    }
                    if (factory.excludedCoordinates.contains(Coordinate(dataX - 1, dataY)) || dataX == 0){
                        gc.strokeLine(x, y, x, y + scale)
                    }
                    if (factory.excludedCoordinates.contains(Coordinate(dataX + 1, dataY)) || dataX == factory.length - 1){
                        gc.strokeLine(x + scale, y, x + scale, y + scale)
                    }
                }
                y += scale
            }
            y = 0.5
            x += scale
        }
    }

    private fun createShape(fo: Pair<FactoryObject,Coordinate>): Shape {
        var shape = Rectangle(0.0,0.0,0.0,0.0) as Shape
        fo.first.coordinates.forEach {
            shape = Shape.union(shape, Rectangle(it.x * scaleMain + 1, it.y * scaleMain + 1, scaleMain - 1, scaleMain - 1))
        }
        shape.fill = fo.first.color
        shape.layoutX = fo.second.x * scaleMain
        shape.layoutY = fo.second.y * scaleMain
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
            infoTextField.text = objectInfoText(pair.first)
        }
    }

    private fun factoryMapSetup(){
        scaleMain = min(min(canvasBorderPane.prefHeight/factory.width, canvasBorderPane.prefWidth/factory.length).toInt().toDouble(), 50.0)
        canvas.width = factory.length * scaleMain + 1
        canvas.height = factory.width * scaleMain + 1
        factoryTextField.text = "Цех: ${data.getFileName().split(".")[0].split("\\").last()}." +
                " Длина: ${factory.length}," +
                " Ширина: ${factory.width}." +
                " Отображаемая дата: ${currentDatePicker.value.toCustomString()}," +
                " занимаемая площадь: ${allObjectsArea(currentDatePicker.value)} из ${factory.width * factory.length - factory.excludedCoordinates.size} м²"

        drawFactory(canvas, factory, currentDatePicker.value, scaleMain)
    }

    private fun textFieldSetup(){
        textField.text = ""
        textField.isVisible = false
        errorsButton.text = "Показать ошибки"
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

    private fun Double.toUserCoordinate(scale: Double) = (this / scale).toInt()
    fun onErrorsButtonClick() {
        if (errorsButton.text == "Показать ошибки"){
            errorsButton.text = "Скрыть ошибки"
            textField.isVisible = true
            canvas.isVisible = false
        }
        else{
            errorsButton.text = "Показать ошибки"
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
                    textField.text += "Объект $objectNumber. ${pair.first.name} наслаивается на границы цеха.\n"
                } else {
                    val obj = listView.items.first{ obj -> obj.first.id == it}
                    textField.text += "Объект $objectNumber. ${pair.first.name} имеет коллизии с объектом ${listView.items.indexOf(obj)}. ${obj.first.name}\n"
                }
            }
        }
        if (textField.text != "") errorsLabel.isVisible = true
    }

    fun onMouseExitedCanvas() {
        infoTextField.text = ""
    }

    fun onMouseMovedInsideCanvas(e: MouseEvent) {
        val x = ((e.sceneX - canvas.layoutX - leftVbox.width - 0.5).toInt() / scaleMain).toInt()
        val y = ((e.sceneY - canvas.layoutY - topHbox.height - 0.5).toInt() / scaleMain).toInt()
        infoTextField.text = "x = $x \ny = $y\n"
        if (factory.excludedCoordinates.contains(Coordinate(x, y))){
            infoTextField.text += "Координата вне цеха\n"
        }else{
            if (textField.text == ""){
                try {
                    val pair = factory.objects.first{
                        it.first.dateStart <= currentDatePicker.value
                                && it.first.dateEnd >= currentDatePicker.value
                                && it.first.coordinates.contains(Coordinate(x - it.second.x, y - it.second.y))
                    }
                    infoTextField.text += objectInfoText(pair.first)
                } catch (_: Exception){}
            }
        }

    }

    fun onMouseClickedInsideCanvas(e: MouseEvent) {
        val x = ((e.sceneX - canvas.layoutX - leftVbox.width - 0.5).toInt() / scaleMain).toInt()
        val y = ((e.sceneY - canvas.layoutY - topHbox.height - 0.5).toInt() / scaleMain).toInt()
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
    private fun FactoryObject.objectArea() = this.coordinates.size

    fun onScrollCanvas(scrollEvent: ScrollEvent) {
        /*if(scrollEvent.deltaY < 0){
            if (scale != 150.0) scale++
        }
        else {
            if(scale != 5.0) scale --
        }
        initialize()*/
    }

    private fun objectInfoText(fo: FactoryObject): String = "Имя: ${fo.name}\n" +
            "Начальная дата:\n ${fo.dateStart.toCustomString()}\n" +
            "Конечная дата:\n ${fo.dateEnd.toCustomString()}\n" +
            "Площадь: ${fo.objectArea()} м²"

    private fun allObjectsArea(date: LocalDate): Int{
        var area = 0
        factory.objects.forEach {
            if (it.first.dateStart <= date && it.first.dateEnd >= date) area += it.first.objectArea()
        }
        return area
    }

    fun onPrintButtonClick() {
        val printScale = (scaleMain / 4).toInt().toDouble()
        val createStage = Stage()
        val vBox = VBox()
        val label1 = Label()
        label1.font = Font.font(15.0)
        label1.text = "Цех: ${data.getFileName().split(".")[0].split("\\").last()}." +
                " Длина: ${factory.length}," +
                " Ширина: ${factory.width}."
        val label2 = Label()
        label2.font = Font.font(15.0)
        label2.text = " Отображаемая дата: ${currentDatePicker.value.toCustomString()}," +
                " занимаемая площадь: ${allObjectsArea(currentDatePicker.value)} из ${factory.width * factory.length - factory.excludedCoordinates.size} м²"
        val printCanvas = Canvas(factory.length * printScale + 1, factory.width * printScale + 1)
        drawFactory(printCanvas, factory, currentDatePicker.value, printScale)
        val printButton = Button()
        printButton.text = "Печать"
        printButton.setOnMouseClicked{
            val job = PrinterJob.createPrinterJob()
            job?.showPrintDialog(createStage)
            job?.printPage(vBox)
            job?.endJob()
        }
        vBox.spacing = 10.0
        vBox.alignment = Pos.CENTER
        val vBox2 = VBox()
        vBox2.spacing = 10.0
        vBox2.alignment = Pos.CENTER
        vBox.children.addAll(label1, label2, printCanvas)
        vBox2.children.addAll(vBox, printButton)
        val scene = Scene(vBox2)
        createStage.title = "Печать"
        createStage.scene = scene
        createStage.isResizable = false
        createStage.show()

    }


}
