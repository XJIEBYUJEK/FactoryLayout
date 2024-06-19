package com.example.factorylayout.factory

import com.example.factorylayout.model.Coordinate
import com.example.factorylayout.model.FactoryObject
import javafx.scene.control.ListCell


class FactoryObjectCellFactory: ListCell<Pair<FactoryObject, Coordinate>>() {
    override fun updateItem(item: Pair<FactoryObject, Coordinate>?, empty: Boolean) {
        super.updateItem(item, empty)
        text = if (empty || item == null) {
            ""
        } else {
            "${item.first.id}. ${item.first.name}"
        }
    }
}
