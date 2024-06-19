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

@Serializable
data class FactoryObject(
    val id: Int,
    var name: String?,
    val coordinates: List<Coordinate>,
    @Serializable (with = ColorAsStringSerializer::class)
    var color: Color,
//    @Contextual val dateStart: Date,
//    @Contextual val dateEnd: Date
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