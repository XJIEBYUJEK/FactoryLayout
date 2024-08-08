package com.example.factorylayout.factory

import com.example.factorylayout.model.Coordinate
import com.example.factorylayout.model.FactoryObject
import javafx.geometry.Insets
import javafx.scene.control.Label
import javafx.scene.control.ListCell
import javafx.scene.layout.HBox
import javafx.scene.shape.Rectangle


class FactoryObjectCellFactory: ListCell<Pair<FactoryObject, Coordinate>>() {
    override fun updateItem(item: Pair<FactoryObject, Coordinate>?, empty: Boolean) {
        super.updateItem(item, empty)
        graphic = if(empty || item == null){
            null
        } else{
            val hBox = HBox()
            val rectangle = Rectangle(10.0, 10.0, item.first.color)
            val label =  Label("${this.index}. ${item.first.name}")
            hBox.children.addAll(rectangle, label)
            HBox.setMargin(rectangle, Insets(3.5,5.0,0.0,0.0))
            hBox
        }
    }
}
