package com.example.agora.data.messaging.model

data class ChatChannel(val userId : MutableList<String>) {
    constructor() : this(mutableListOf())
}