package com.example.factorylayout.factory

import com.example.factorylayout.dateInfo
import com.example.factorylayout.model.Coordinate
import com.example.factorylayout.model.Factory
import com.example.factorylayout.model.FactoryObject
import com.example.factorylayout.toCustomString
import javafx.geometry.Insets
import javafx.geometry.Pos
import javafx.scene.control.Label
import javafx.scene.control.ListCell
import javafx.scene.layout.HBox
import javafx.scene.paint.Color
import javafx.scene.shape.Rectangle
import java.time.LocalDate

class FactoryObjectsTableCellFactory(private val factory: Factory, private val currentDate: LocalDate) : ListCell<Pair<FactoryObject, Coordinate>>()
 {
    override fun updateItem(item: Pair<FactoryObject, Coordinate>?, empty: Boolean) {
        super.updateItem(item, empty)
        graphic = if(empty || item == null){
            null
        } else{
            val hBox = HBox().apply {
                spacing = 10.0
                alignment = Pos.CENTER_LEFT
            }
            val rectangle = Rectangle(10.0, 10.0, item.first.color)
            val label =  Label("${this.index + 1}. ${item.first.name}").apply {
                minWidth = 150.0
                maxWidth = 150.0
                alignment = Pos.CENTER_LEFT
            }
            val dates = Label("${dateInfo(item.first, true, factory).toCustomString()} - ${dateInfo(item.first, false, factory).toCustomString()}").apply {
                minWidth = 150.0
                maxWidth = 150.0
                alignment = Pos.CENTER
            }
            val size = Label(item.first.coordinates.size.toString()).apply {
                minWidth = 50.0
                maxWidth = 50.0
                alignment = Pos.CENTER
            }
            val warningMsg = when((dateInfo(item.first, false, factory).toEpochDay() - currentDate.toEpochDay())){
                2L -> "Истекает через 3 дня"
                1L -> "Истекает через 2 дня"
                0L -> "Истекает через 1 день"
                else -> ""
            }
            val warning = Label(warningMsg).apply {
                minWidth = 150.0
                maxWidth = 150.0
                alignment = Pos.CENTER
                textFill = Color.RED
            }
            hBox.children.addAll(rectangle, label, dates, size, warning)
            HBox.setMargin(rectangle, Insets(3.5,5.0,5.0,5.0))
            hBox
        }
    }
}