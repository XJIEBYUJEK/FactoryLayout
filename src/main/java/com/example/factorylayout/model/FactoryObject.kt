package com.example.factorylayout.model

import java.util.Date

data class FactoryObject(
    val coordinates: List<Coordinate>,
    val dateStart: Date,
    val dateEnd: Date
)