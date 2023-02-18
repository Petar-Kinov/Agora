package com.example.agora.data.Messaging.Model

data class ChatChannel(val userId : MutableList<String>) {
    constructor() : this(mutableListOf())
}