package com.example.agora.data.Messaging.Model

import java.util.*

data class TextMessage(
    val text: String,
    override val time: Date,
    override val senderId: String,
    override val recipientId : String,
    override val senderName: String,
    override val type: String = MessageType.TEXT
) : Message {
    constructor() : this("", Date(0), "","","")

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as TextMessage

        if (text != other.text) return false
        if (time != other.time) return false
        if (senderId != other.senderId) return false
        if (type != other.type) return false

        return true
    }

    override fun hashCode(): Int {
        var result = text.hashCode()
        result = 31 * result + time.hashCode()
        result = 31 * result + senderId.hashCode()
        result = 31 * result + type.hashCode()
        return result
    }


}