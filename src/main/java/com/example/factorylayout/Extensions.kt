package com.example.factorylayout

import com.example.factorylayout.model.Coordinate
import com.example.factorylayout.model.Factory
import com.example.factorylayout.model.FactoryObject
import javafx.scene.canvas.Canvas
import javafx.scene.control.DatePicker
import javafx.scene.control.Slider
import javafx.scene.input.MouseEvent
import javafx.scene.paint.Color
import javafx.scene.transform.Scale
import java.time.LocalDate

fun dateCheck(fo: FactoryObject, date: LocalDate) = fo.dateStart <= date && fo.dateEnd >= date

fun drawFactory(canvas: Canvas, factory: Factory, currentDate: LocalDate, scale: Double, withoutObjects: Boolean = false){
    val gc = canvas.graphicsContext2D
    gc.clearRect(0.0,0.0, canvas.width, canvas.height)
    var x = 0.5
    var y = 0.5
    val excludedCoordinates =  factory.excludedCoordinates.toMutableList()
    if(!withoutObjects){
        factory.objects.forEach {
            if (dateCheck(it.first, currentDate)){
                it.first.coordinates.forEach {coordinate ->
                    excludedCoordinates.add(Coordinate(coordinate.x + it.second.x, coordinate.y + it.second.y))
                }
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
                    it.first.coordinates.contains(Coordinate(dataX-it.second.x, dataY-it.second.y)) &&
                            dateCheck(it.first, currentDate)
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

fun Double.toUserCoordinate(scale: Double) = (this / scale).toInt()

fun updateSlider(isVisible: Boolean,
                         slider: Slider,
                         startDatePicker: DatePicker,
                         endDatePicker: DatePicker,
                         currentDatePicker: DatePicker){
    if (isVisible){
        slider.min = startDatePicker.value.toEpochDay().toDouble()
        slider.max = endDatePicker.value.toEpochDay().toDouble()
        slider.value = currentDatePicker.value.toEpochDay().toDouble()
    }
    slider.isVisible = isVisible
}

fun currentDatePickerSettings(slider: Slider,
                              startDatePicker: DatePicker,
                              endDatePicker: DatePicker,
                              currentDatePicker: DatePicker) {
    if(startDatePicker.value != null && startDatePicker.value > currentDatePicker.value)
        startDatePicker.value = currentDatePicker.value
    if(endDatePicker.value != null && endDatePicker.value < currentDatePicker.value)
        endDatePicker.value = currentDatePicker.value
    if (startDatePicker.value != null && endDatePicker.value != null)
        updateSlider(true, slider, startDatePicker, endDatePicker, currentDatePicker)
}

fun updateDatePickerFromSlider(slider: Slider, currentDatePicker: DatePicker){
    currentDatePicker.value = LocalDate.ofEpochDay(slider.value.toLong())
}

fun Double.scaleRefactor(scale: Double) = (this/scale).toInt() * scale

fun LocalDate.toCustomString() =
    if (this.dayOfMonth < 10) {"0${this.dayOfMonth}."} else {"${this.dayOfMonth}."} +
            if (this.monthValue < 10) {"0${this.monthValue}."} else {"${this.monthValue}."} +
            "${this.year}"

fun Int.findFactoryObjectById(factory: Factory) = factory.objects.first { it.first.id == this }.first