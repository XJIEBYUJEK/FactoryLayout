package com.example.factorylayout.model

import kotlinx.serialization.Serializable
import java.util.Date
import javafx.scene.paint.Color
import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import java.time.LocalDate

@Serializable
data class FactoryObject(
    val id: Int,
    var name: String?,
    var coordinates: List<Coordinate>,
    @Serializable (with = ColorAsStringSerializer::class)
    var color: Color,
    @Serializable (with = DateAsStringSerializer::class)
    var dateStart: LocalDate,
    @Serializable (with = DateAsStringSerializer::class)
    var dateEnd: LocalDate,
    val parentObject: Int? = null,
    val childObjects: MutableList<Int> = mutableListOf()
)

object ColorAsStringSerializer : KSerializer<Color> {
    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("Color", PrimitiveKind.STRING)

    override fun serialize(encoder: Encoder, value: Color) {
        val string = value.toString()
        encoder.encodeString(string)
    }

    override fun deserialize(decoder: Decoder): Color {
        val string = decoder.decodeString()
        return Color.web(string)
    }
}

object DateAsStringSerializer : KSerializer<LocalDate> {
    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("Color", PrimitiveKind.STRING)

    override fun serialize(encoder: Encoder, value: LocalDate) {
        val string = value.toString()
        encoder.encodeString(string)
    }

    override fun deserialize(decoder: Decoder): LocalDate {
        val string = decoder.decodeString()
        return LocalDate.parse(string)
    }
}