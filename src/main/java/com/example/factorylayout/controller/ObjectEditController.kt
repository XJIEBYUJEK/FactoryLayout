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
import java.text.SimpleDateFormat
import java.time.LocalDate
import kotlin.math.min

class ObjectEditController {

    @FXML
    lateinit var removeButton: Button
    @FXML
    lateinit var unionButton: Button
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
    private var element = Element.ELSE

    enum class Element{
        FIRST,
        LAST,
        ELSE
    }

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
        val listOfDates= (listOf("${selectedObject.dateStart.toCustomString()} - ${selectedObject.dateEnd.toCustomString()}") +
                selectedObject.childObjects.map { id ->
                    val child = id.findFactoryObjectById(factory)
                    "${child.dateStart.toCustomString()} - ${child.dateEnd.toCustomString()}"
                }).toMutableList()
        val label = Label("Выберите редактируемый\n промежуток")
        val dateFormat = SimpleDateFormat("dd.MM.yyyy")
        val choiceBox = ChoiceBox(FXCollections.observableList(listOfDates.sortedBy { range ->
            val  startDateString = range.split(" - ")[0]
            dateFormat.parse(startDateString)
        }))
        val nextButton = Button("Далее").apply {
            isDisable = true
        }
        /*val unionButton = Button("Объединить").apply {
            isDisable = true
        }*/
        choiceBox.setOnAction {
            nextButton.isDisable = choiceBox.value == null
            //unionButton.isDisable = choiceBox.value == null
        }
        /*val hBox = HBox().apply {
            spacing = 10.0
            alignment = Pos.CENTER
            children.addAll(unionButton, nextButton)
        }*/
        val vBox = VBox().apply {
            spacing = 10.0
            alignment = Pos.CENTER
            children.addAll(label, choiceBox, nextButton)
        }
        val createStage = Stage().apply {
            scene = Scene(vBox)
            isResizable = false
            height = 200.0
            width = 200.0
        }

        nextButton.setOnMouseClicked {
            createStage.close()
            val answer = choiceBox.value
            element = when(choiceBox.items.indexOf(answer)){
                0 -> Element.FIRST
                choiceBox.items.lastIndex -> Element.LAST
                else -> Element.ELSE
            }

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
                    coordinateList = selectedObject.coordinates.map {
                        Coordinate(it.x + selectedObjectCoordinate.x, it.y + selectedObjectCoordinate.y)
                    }.toMutableList()
                    initialize()
                }
            }
        }

        /*unionButton.setOnMouseClicked {
            fun isItParent(value: String) = listOfDates.indexOf(value) == 0
            val answer = choiceBox.value
            val indexInChoiceBox = choiceBox.items.indexOf(answer)
            fun Int.toListIndex() = listOfDates.indexOf(choiceBox.items[this])
            val indexInListOfDates = indexInChoiceBox.toListIndex()
            when(indexInChoiceBox){
                0 -> {
                    fun listUpdate(newValue: String){
                        listOfDates.remove(answer)
                        listOfDates[0] = newValue
                        choiceBox.items.remove(answer)
                        choiceBox.items[0] = newValue
                    }
                    if (isItParent(answer)){   // выбран первый элемент, который является родителем
                        val nextObjectId = selectedObject.childObjects[1]
                        val nextObjectData = factory.findPairById(nextObjectId)
                        val nextObject = nextObjectData.first
                        selectedObject.apply {
                            dateEnd = nextObject.dateEnd
                            childObjects.remove(nextObjectId)
                            coordinates = nextObject.coordinates.map { it.copy() }
                        }
                        selectedObjectCoordinate = nextObjectData.second.copy()
                        factory.objects.remove(nextObjectData)
                        listUpdate("${selectedObject.dateStart.toCustomString()} - ${selectedObject.dateEnd.toCustomString()}")
                    } else {
                        val currentObjectId = selectedObject.childObjects[indexInListOfDates - 1]
                        val currentObjectData = factory.findPairById(currentObjectId)
                        val currentObject = currentObjectData.first
                        if(isItParent(choiceBox.items[1])){ // второй элемент является родительским объектом
                            selectedObject.dateStart = currentObject.dateStart
                            listUpdate("${selectedObject.dateStart.toCustomString()} - ${selectedObject.dateEnd.toCustomString()}")
                        } else{
                            val nextObjectId = selectedObject.childObjects[indexInListOfDates]
                            val nextObjectData = factory.findPairById(nextObjectId)
                            val nextObject = nextObjectData.first
                            nextObject.dateStart = currentObject.dateStart
                            listUpdate("${nextObject.dateStart.toCustomString()} - ${nextObject.dateEnd.toCustomString()}")
                        }
                        selectedObject.childObjects.remove(currentObjectId)
                        factory.objects.remove(currentObjectData)
                    }
                }
                else -> {
                    val previousListIndex = (indexInChoiceBox-1).toListIndex()
                    fun listUpdate(newValue: String){
                        listOfDates.remove(answer)
                        listOfDates[previousListIndex] = newValue
                        choiceBox.items.remove(answer)
                        choiceBox.items[indexInChoiceBox - 1] = newValue
                    }
                    if (isItParent(answer)){
                        val previousObjectId = selectedObject.childObjects[previousListIndex - 1]
                        val previousObjectData = factory.findPairById(previousObjectId)
                        val previousObject = previousObjectData.first
                        selectedObject.apply {
                            dateStart = previousObject.dateStart
                            childObjects.remove(previousObjectId)
                            coordinates = previousObject.coordinates.map { it.copy() }
                        }
                        selectedObjectCoordinate = previousObjectData.second.copy()
                        factory.objects.remove(
                            factory.objects.first{ it.first == previousObject}
                        )
                        listUpdate("${selectedObject.dateStart.toCustomString()} - ${selectedObject.dateEnd.toCustomString()}")
                    }
                }
            }
            data.setFactoryLayout(factory)
        }*/

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
                    selectedObject.coordinates = coordinateList.map { it.copy() }
                } else {
                    fun addChildObject(newFactoryObject: FactoryObject){
                        if (selectedObject.parentObject == null){
                            selectedObject.childObjects.add(newFactoryObject.id)
                        }
                        else{
                            selectedObject.parentObject!!.findFactoryObjectById(factory).childObjects.add(newFactoryObject.id)
                        }
                    }
                    val newFactoryObject = FactoryObject(
                        id = if(factory.objects.size > 0) factory.objects.last().first.id + 1 else 0,
                        name = selectedObject.name,
                        color = selectedObject.color,
                        coordinates = coordinateList.map { it.copy() },
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
                                id = factory.objects.last().first.id + 1,
                                name = selectedObject.name,
                                color = selectedObject.color,
                                coordinates = selectedObject.coordinates.map { it.copy() },
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
        drawObjectShadow(scale)
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
        if (selectedObject.parentObject != null || selectedObject.childObjects.isNotEmpty()){
            startDatePicker.isDisable = true
            endDatePicker.isDisable = true
        }
        when (element){
            Element.FIRST -> startDatePicker.isDisable = false
            Element.LAST -> endDatePicker.isDisable = false
            Element.ELSE -> {}
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
        val isDateCorrect = (startDatePicker.value != null && endDatePicker.value != null && startDatePicker.value <= endDatePicker.value)
        saveButton.isDisable = isFigureOutsideLayout || !isDateCorrect
    }

    private fun upperMenuSetup(){
        colorPicker.value = selectedObject.color
        objectNameText.text = selectedObject.name
        if (selectedObject.isItChild() || selectedObject.isItParent()){
            removeButton.isVisible = true
            unionButton.isVisible = true
            when(element){
                Element.ELSE -> removeButton.isDisable = true
                Element.FIRST -> unionButton.isDisable = true
                Element.LAST -> {}
            }
        } else {
            removeButton.isVisible = false
            unionButton.isVisible = false
        }
    }

    @FXML
    fun onBackPressed() {
        if(this.startDatePicker.scene != null){
            val stage = this.startDatePicker.scene.window as Stage
            stage.close()
        }
    }

    @FXML
    fun onColorPickerAction(){
        shape.fill = colorPicker.value
    }

    @FXML
    fun onSaveButtonClick(event: ActionEvent) {
        selectedObject.dateStart = startDatePicker.value
        selectedObject.dateEnd = endDatePicker.value
        val bounds = shape.boundsInParent
        selectedObjectCoordinate.x = bounds.minX.toUserCoordinate(scale)
        selectedObjectCoordinate.y = bounds.minY.toUserCoordinate(scale)
        fun parentAndChildsSetup(fo: FactoryObject){
            val parent = if (fo.parentObject != null)
                fo.parentObject.findFactoryObjectById(factory)
            else selectedObject
            parent.name = objectNameText.text
            parent.color = colorPicker.value
            parent.childObjects.forEach { id ->
                val child = id.findFactoryObjectById(factory)
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
        val value2 = startDatePicker.value
        val value3 = currentDatePicker.value
        if (value != null){
            if (value < value3) {
                currentDatePicker.value = value
                initialize()
            }
            if (startDatePicker.value != null && value < value2) {
                updateSlider(false)
                startDatePicker.value = null
            }
            if (value2 != null){
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
        val value2 = endDatePicker.value
        val value3 = currentDatePicker.value
        if (value != null){
            if (value > value3){
                currentDatePicker.value = value
                initialize()
            }
            if (endDatePicker.value != null && value > value2) {
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

    @FXML
    fun onUnionButtonClick() {
        val previousObjectPair = findPreviousObject(selectedObject)
        if(selectedObject.isItParent()){
            selectedObject.apply {
                dateStart = previousObjectPair.first.dateStart
                coordinates = previousObjectPair.first.coordinates.map { it.copy() }
                childObjects.remove(previousObjectPair.first.id)
            }
            selectedObjectCoordinate = previousObjectPair.second.copy()
            factory.objects.remove(previousObjectPair)
        } else {
            previousObjectPair.first.dateEnd = selectedObject.dateEnd
            val parent = selectedObject.parentObject!!.findFactoryObjectById(factory)
            parent.childObjects.remove(selectedObjectId)
            factory.objects.remove(selectedObjectData)
        }
        data.setFactoryLayout(factory)
        onBackPressed()
    }

    private fun findPreviousObject(fo: FactoryObject): Pair<FactoryObject, Coordinate>{
        val date = fo.dateStart
        val dateList = mutableListOf<LocalDate>()
        fun generateDateFromList(): LocalDate{
            var dateOfPrevious: LocalDate? = null
            dateList.forEach {
                if( date > it){
                    if (dateOfPrevious == null){
                        dateOfPrevious = it
                    } else {
                        if (dateOfPrevious!! < it){
                            dateOfPrevious = it
                        }
                    }
                }
            }
            return dateOfPrevious!!
        }
        if (fo.isItParent()){
            fo.childObjects.forEach {
                dateList.add(it.findFactoryObjectById(factory).dateStart)
            }
            val dateOfPrevious = generateDateFromList()
            return factory.objects.first {
                (it.first.dateStart == dateOfPrevious) && fo.childObjects.contains(it.first.id)
            }
        }
        else{
            val parent = fo.parentObject!!.findFactoryObjectById(factory)
            dateList.add(parent.dateStart)
            parent.childObjects.forEach {
                dateList.add(it.findFactoryObjectById(factory).dateStart)
            }
            val dateOfPrevious = generateDateFromList()
            return factory.objects.first {
                (it.first.dateStart == dateOfPrevious) && (parent.childObjects.contains(it.first.id) || parent.id == it.first.id)
            }
        }
    }

    @FXML
    fun onRemoveButtonClick() {
        if(selectedObject.isItParent()){
            val child = factory.findPairById(selectedObject.childObjects.first())
            selectedObject.apply {
                dateStart = child.first.dateStart
                dateEnd = child.first.dateEnd
                coordinates = child.first.coordinates.map { it.copy() }
                childObjects.remove(child.first.id)
            }
            selectedObjectCoordinate = child.second.copy()
            factory.objects.remove(child)

        } else {
            val parent = selectedObject.parentObject!!.findFactoryObjectById(factory)
            parent.childObjects.remove(selectedObjectId)
            factory.objects.remove(selectedObjectData)
        }
        data.setFactoryLayout(factory)
        onBackPressed()
    }

    private fun drawObjectShadow(scale: Double){
        val gc = canvas.graphicsContext2D
        fun Int.rearrangeCoordinate(flag: Boolean) =
            if(flag)
                (this + selectedObjectCoordinate.x) * scale
            else
                (this + selectedObjectCoordinate.y) * scale
        gc.lineWidth = 1.0
        gc.stroke = selectedObject.color
        selectedObject.coordinates.forEach {
            if(!selectedObject.coordinates.contains(Coordinate(it.x + 1, it.y))){
                gc.strokeLine(
                    (it.x + 1).rearrangeCoordinate(true),
                    it.y.rearrangeCoordinate(false),
                    (it.x + 1).rearrangeCoordinate(true),
                    (it.y + 1).rearrangeCoordinate(false)
                )
            }
            if(!selectedObject.coordinates.contains(Coordinate(it.x - 1, it.y))){
                gc.strokeLine(
                    it.x.rearrangeCoordinate(true),
                    it.y.rearrangeCoordinate(false),
                    it.x.rearrangeCoordinate(true),
                    (it.y + 1).rearrangeCoordinate(false)
                )
            }
            if(!selectedObject.coordinates.contains(Coordinate(it.x, it.y + 1))){
                gc.strokeLine(
                    it.x.rearrangeCoordinate(true),
                    (it.y + 1).rearrangeCoordinate(false),
                    (it.x + 1).rearrangeCoordinate(true),
                    (it.y + 1).rearrangeCoordinate(false)
                )
            }
            if(!selectedObject.coordinates.contains(Coordinate(it.x, it.y - 1))){
                gc.strokeLine(
                    it.x.rearrangeCoordinate(true),
                    it.y.rearrangeCoordinate(false),
                    (it.x + 1).rearrangeCoordinate(true),
                    it.y.rearrangeCoordinate(false)
                )
            }
        }

    }
}

