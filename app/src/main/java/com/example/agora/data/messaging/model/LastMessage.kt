package com.example.agora.data.messaging.model

import java.util.*

class LastMessage(val text : String, val time : Date, val senderId : String) {

    constructor() : this("",Date(0), "")

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as LastMessage

        if (text != other.text) return false
        if (time != other.time) return false

        return true
    }

    override fun hashCode(): Int {
        var result = text.hashCode()
        result = 31 * result + time.hashCode()
        return result
    }


}