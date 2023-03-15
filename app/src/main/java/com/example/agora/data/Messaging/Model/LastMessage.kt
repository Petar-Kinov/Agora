package com.example.agora.data.Messaging.Model

import java.util.*

class LastMessage(val text : String, val time : Date) {

    constructor() : this("",Date(0))
}