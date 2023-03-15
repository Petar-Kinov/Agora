package com.example.agora.data.messaging.model

import java.util.*

class LastMessage(val text : String, val time : Date) {

    constructor() : this("",Date(0))
}