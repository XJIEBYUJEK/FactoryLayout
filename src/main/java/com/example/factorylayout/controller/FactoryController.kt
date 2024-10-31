package com.example.factorylayout.controller

import com.example.factorylayout.*
import com.example.factorylayout.data.SingletonData
import com.example.factorylayout.factory.FactoryObjectCellFactory
import com.example.factorylayout.model.Coordinate
import com.example.factorylayout.model.FactoryObject
import javafx.collections.FXCollections
import javafx.embed.swing.SwingFXUtils
import javafx.fxml.FXML
import javafx.fxml.FXMLLoader
import javafx.geometry.Insets
import javafx.geometry.Pos
import javafx.scene.*
import javafx.scene.canvas.Canvas
import javafx.scene.control.*
import javafx.scene.image.WritableImage
import javafx.scene.input.MouseEvent
import javafx.scene.input.ScrollEvent
import javafx.scene.layout.BorderPane
import javafx.scene.layout.HBox
import javafx.scene.layout.VBox
import javafx.scene.text.Font
import javafx.stage.FileChooser
import javafx.stage.Stage
import javafx.stage.Window
import org.apache.poi.ss.usermodel.*
import org.apache.poi.xssf.usermodel.XSSFCellStyle
import org.apache.poi.xssf.usermodel.XSSFColor
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.nio.file.Paths
import java.time.LocalDate
import javax.imageio.ImageIO
import kotlin.math.max
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

    private var previousSavePath: String? = null

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
        val textLabel = Label("Вы уверены, что хотите удалить объект?")
        val yesButton = Button("Да")
        val cancelButton = Button("Отмена")
        val hBox = HBox().apply {
            spacing = 10.0
            alignment = Pos.CENTER
            children.addAll(cancelButton, yesButton)
        }
        val vBox = VBox().apply {
            spacing = 10.0
            alignment = Pos.CENTER
            children.addAll(textLabel, hBox)
        }
        val createStage = Stage().apply {
            scene = Scene(vBox)
            isResizable = false
            height = 100.0
            width = 250.0
        }
        yesButton.setOnMouseClicked {
            val index = listView.selectionModel.selectedIndex
            if(index > -1){
                val selectedItem = listView.items[index]
                listView.items.remove(selectedItem)
                factory.objects.remove(selectedItem)
                selectedItem.first.childObjects.forEach { id ->
                    factory.objects.remove(factory.objects.first { it.first.id == id })
                }
                data.setFactoryLayout(factory)
                initialize()
            }
            createStage.close()
        }
        cancelButton.setOnMouseClicked {
            createStage.close()
        }
        createStage.show()
    }

    fun onEditButtonClick(){
        val index = listView.selectionModel.selectedIndex
        if(index > -1){
            val selectedItemId = listView.items[index].first.id
            data.setObjectId(selectedItemId)
            val stage = Stage()
            stage.title = "Редактирование объекта"
            val loader = FXMLLoader(FactoryApplication::class.java.getResource("ObjectEditView.fxml"))
            val root = loader.load<Any>() as Parent
            val scene = Scene(root)
            stage.scene = scene
            stage.show()
            stage.setOnHiding {
                this.factory = data.getFactoryLayout()
                initialize()
            }
        }

    }

    fun onCurrentDatePickerAction() {
        currentDatePickerSettings(dateSlider, startDatePicker, endDatePicker, currentDatePicker)
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
        updateDatePickerFromSlider(dateSlider, currentDatePicker)
        initialize()
    }

    private fun datePickersSetup(){
        if (currentDatePicker.value == null) currentDatePicker.value = LocalDate.now()
    }

    private fun listObjectsSetup(){
        listView.items = FXCollections.observableList(factory.objects.filter { it.first.parentObject == null })
        listView.setCellFactory { FactoryObjectCellFactory() }
        listView.setOnMouseClicked {
            val index = listView.selectionModel.selectedIndex
            val pair = listView.items[index]
            infoTextField.text = objectInfoText(pair.first)
        }
    }

    private fun factoryMapSetup(){
        scaleMain = min(min((canvasBorderPane.prefHeight-factoryTextField.prefHeight*2)/factory.width, canvasBorderPane.prefWidth/factory.length).toInt().toDouble(), 50.0)
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
            collisionsId.distinct().forEach{ id ->

                val objectNumber = if (pair.first.parentObject != null){
                    listView.items.indexOf(factory.objects.first {it.first.id == pair.first.parentObject  })
                } else {
                    listView.items.indexOf(pair)
                }


                if (id == -1) {
                    textField.text += "Объект $objectNumber. ${pair.first.name} наслаивается на границы цеха.\n"
                } else {
                    val obj = listView.items.first{ obj ->
                        val tempObject = id.findFactoryObjectById(factory)
                        if(tempObject.parentObject == null){
                            obj.first.id == id
                        } else {
                            obj.first.id == tempObject.parentObject
                        }

                    }
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
                        dateCheck(it.first, currentDatePicker.value)
                                && it.first.coordinates.contains(Coordinate(x - it.second.x, y - it.second.y))
                    }
                    infoTextField.text += if (pair.first.parentObject == null)
                        objectInfoText(pair.first)
                    else
                        objectInfoText(pair.first.parentObject!!.findFactoryObjectById(factory))
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
                    dateCheck(it.first, currentDatePicker.value)
                            && it.first.coordinates.contains(Coordinate(x - it.second.x, y - it.second.y))
                }
                if (pair.first.parentObject == null){
                    listView.selectionModel.select(listView.items.indexOf(pair))
                } else {
                    listView.selectionModel.select(listView.items.indexOf(factory.objects.first {
                        it.first.id == pair.first.parentObject
                    }))
                }

            } catch (_: Exception){}
        }
    }

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
        updateDatePickerFromSlider(dateSlider, currentDatePicker)
        initialize()
    }
    private fun FactoryObject.objectArea() = this.coordinates.size

    private fun areaInfoText(fo: FactoryObject): String{
        return if(fo.childObjects.isNotEmpty()){
            var minArea = fo.objectArea()
            var maxArea = fo.objectArea()
            fo.childObjects.forEach { id ->
                val tempArea = id.findFactoryObjectById(factory).objectArea()
                minArea = min(minArea, tempArea)
                maxArea = max(maxArea, tempArea)
            }
            if (minArea == maxArea){
                "${fo.objectArea()}"
            } else {
                "от $minArea до $maxArea"
            }
        } else "${fo.objectArea()}"
    }

    private fun dateInfo(fo: FactoryObject, startDate: Boolean): LocalDate{
        return if(fo.childObjects.isNotEmpty()){
            var minDate = fo.dateStart
            var maxDate = fo.dateEnd
            fo.childObjects.forEach { id ->
                val childObject = id.findFactoryObjectById(factory)
                if (minDate > childObject.dateStart) minDate = childObject.dateStart
                if (maxDate < childObject.dateEnd) maxDate = childObject.dateEnd
            }
            if(startDate) minDate
            else maxDate
        }
        else {
            if(startDate) fo.dateStart
            else fo.dateEnd
        }
    }

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
            "Начальная дата:\n ${dateInfo(fo, true).toCustomString()}\n" +
            "Конечная дата:\n ${dateInfo(fo, false).toCustomString()}\n" +
            "Площадь:\n ${areaInfoText(fo)} м²"

    private fun allObjectsArea(date: LocalDate): Int{
        var area = 0
        factory.objects.forEach {
            if (dateCheck(it.first, date)) area += it.first.objectArea()
        }
        return area
    }

    fun onPrintButtonClick() {
        val printScale = (scaleMain / 1.5).toInt().toDouble()
        val createStage = Stage()
        val vBox = VBox()
        val label1 = Label()
        label1.font = Font.font(20.0)
        label1.text = "Цех: ${data.getFileName().split(".")[0].split("\\").last()}." +
                " Длина: ${factory.length}," +
                " Ширина: ${factory.width}."
        val label2 = Label()
        label2.font = Font.font(20.0)
        label2.text = " Отображаемая дата: ${currentDatePicker.value.toCustomString()}," +
                " занимаемая площадь: ${allObjectsArea(currentDatePicker.value)} из ${factory.width * factory.length - factory.excludedCoordinates.size} м²"
        val printCanvas = Canvas(factory.length * printScale + 1, factory.width * printScale + 1)
        drawFactory(printCanvas, factory, currentDatePicker.value, printScale)
        val printButton = Button()
        printButton.text = "Сохранить"
        printButton.setOnMouseClicked{
            saveAsPng(vBox, createStage)
            createStage.close()
        }
        vBox.spacing = 10.0
        vBox.alignment = Pos.CENTER
        val vBox2 = VBox()
        vBox2.spacing = 10.0
        vBox2.alignment = Pos.CENTER
        vBox.children.addAll(label1, label2, printCanvas)
        vBox2.children.addAll(vBox, printButton)
        VBox.setMargin(printCanvas, Insets(0.0,10.0,10.0,10.0))
        VBox.setMargin(printButton, Insets(0.0,10.0,10.0,10.0))
        val scene = Scene(vBox2)
        createStage.title = "Сохранить изображение"
        createStage.scene = scene
        createStage.isResizable = false
        createStage.show()
    }

    private fun saveAsPng(node: Node, stage: Window) = saveAsPng(node, stage, SnapshotParameters())

    private fun saveAsPng(node: Node, stage: Window, ssp: SnapshotParameters?) {
        val image: WritableImage = node.snapshot(ssp, null)
        val fileChooser = FileChooser()
        fileChooser.extensionFilters.add(FileChooser.ExtensionFilter("Image", "*.png"))
        val currentPath = if (previousSavePath != null)
            previousSavePath!!
        else
            Paths.get(".").toAbsolutePath().normalize().toString()
        fileChooser.initialDirectory = File(currentPath)
        val file = fileChooser.showSaveDialog(stage)
        previousSavePath = file.path.split("\\${file.name}")[0]
        try {
            ImageIO.write(SwingFXUtils.fromFXImage(image, null), "png", file)
        } catch (e: IOException) {
            // TODO: handle exception here
        }
        createSpreadsheet(File("$previousSavePath\\legend_of_${file.name}.xlsx"))
    }

    private fun createSpreadsheet(file: File){
        val workbook = XSSFWorkbook()
        val spreadsheet: Sheet = workbook.createSheet("legend")
        var row: Row = spreadsheet.createRow(0)
        row.createCell(0).setCellValue("Цвет")
        row.createCell(1).setCellValue("Имя")
        row.createCell(2).setCellValue("Начальная дата")
        row.createCell(3).setCellValue("Конечная дата")
        row.createCell(4).setCellValue("Площадь")
        val currentDateObjects = FXCollections.observableList(factory.objects.filter {
            it.first.dateStart <= currentDatePicker.value && it.first.dateEnd >= currentDatePicker.value
        })
        var index = 1

        if (currentDateObjects.isNotEmpty()){
            for (i in currentDateObjects){
                val color = i.first.color
                val red = (color.red * 255).toInt()
                val green = (color.green * 255).toInt()
                val blue = (color.blue * 255).toInt()
                val rgb = byteArrayOf(red.toByte(), green.toByte(), blue.toByte())
                val xssfColor = XSSFColor(rgb, null)
                val style:XSSFCellStyle = workbook.createCellStyle()

                style.setFillForegroundColor(xssfColor)
                style.fillPattern = FillPatternType.SOLID_FOREGROUND

                val objForDate = if (i.first.parentObject != null){
                    i.first.parentObject!!.findFactoryObjectById(factory)
                } else i.first

                row = spreadsheet.createRow(index)
                row.createCell(0).cellStyle = style
                row.createCell(1).setCellValue(i.first.name)
                row.createCell(2).setCellValue(dateInfo(objForDate, true).toCustomString())
                row.createCell(3).setCellValue(dateInfo(objForDate, false).toCustomString())
                row.createCell(4).setCellValue(i.first.coordinates.size.toString())
                index++
            }
        }
        spreadsheet.autoSizeColumn(0)
        spreadsheet.autoSizeColumn(1)
        spreadsheet.autoSizeColumn(2)
        spreadsheet.autoSizeColumn(3)
        row = spreadsheet.createRow(index)
        row.createCell(0).setCellValue("Цех: ${data.getFileName().split(".")[0].split("\\").last()}."
                + " Дата: ${currentDatePicker.value.toCustomString()}"
        )
        val stream = FileOutputStream(file)
        workbook.write(stream)
        stream.close()
    }

    private fun updateSlider(isVisible: Boolean) = updateSlider(isVisible, dateSlider, startDatePicker, endDatePicker, currentDatePicker)
}
