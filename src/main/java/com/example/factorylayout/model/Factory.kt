package com.example.factorylayout.model

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

@Serializable
data class Factory(
    val width: Int,
    val length: Int,
    val excludedCoordinates: List<Coordinate>
){
    fun jsonString() = Json.encodeToString(serializer(), this)
}

