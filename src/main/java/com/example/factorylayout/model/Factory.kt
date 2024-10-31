package com.example.factorylayout.model

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

@Serializable
data class Factory(
    val width: Int,
    val length: Int,
    val excludedCoordinates: List<Coordinate>,
    val objects: MutableList<Pair<FactoryObject, Coordinate>> = mutableListOf()
){
    fun toJsonString() = Json.encodeToString(serializer(), this)
    fun makeCopy() = Factory(this.width, this.length, this.excludedCoordinates, this.objects.toMutableList())

    fun findPairById(id: Int) = this.objects.first { it.first.id == id }
}

