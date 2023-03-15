package com.example.agora.data.Messaging.Model

class EngagedChatChannel(val channelId : String , val lastMessage: LastMessage, val otherUserName : String) {

    constructor() : this("",LastMessage(), "")
}