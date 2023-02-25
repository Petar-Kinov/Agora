package com.example.agora

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.agora.data.Messaging.Model.Person
import com.example.agora.data.Messaging.Model.TextMessageItem
import com.example.agora.databinding.ActivityChatBinding
import com.example.agora.util.AppConstants
import com.example.agora.util.FirestoreUtil
import com.google.firebase.firestore.ListenerRegistration
import com.xwray.groupie.Item

class ChatActivity : AppCompatActivity() {

    private lateinit var binding : ActivityChatBinding
    private lateinit var messagesListenerRegistration : ListenerRegistration
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityChatBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //TODO Actionbar back button impl
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = intent.getStringExtra(AppConstants.USER_NAME)

        binding.textView12.text = "Implement chat Ui. Current chat is with ${intent.getStringExtra(AppConstants.USER_NAME)}"
        val otherUserId = intent.getStringExtra(AppConstants.USER_ID)
        FirestoreUtil.getOrCreateChatChannel(otherUserId!!) {channelId ->
            messagesListenerRegistration = FirestoreUtil.addChatMessageListener(channelId = channelId , context = this , this::onMessagesChanged)
        }


    }

    private fun onMessagesChanged(mesasges: List<TextMessageItem>) {
        Toast.makeText(this,"OnMessagesChangedRunning", Toast.LENGTH_SHORT).show()
    }
}