package com.example.agora

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.agora.data.Messaging.Model.MessageType
import com.example.agora.data.Messaging.Model.TextMessage
import com.example.agora.data.Messaging.Model.TextMessageItem
import com.example.agora.databinding.ActivityChatBinding
import com.example.agora.util.AppConstants
import com.example.agora.util.FirebaseHelper
import com.example.agora.util.FirestoreUtil
import com.google.firebase.firestore.ListenerRegistration
import com.xwray.groupie.GroupieAdapter
import com.xwray.groupie.Section
import java.util.*

class ChatActivity : AppCompatActivity() {

    private lateinit var binding : ActivityChatBinding
    private lateinit var messagesListenerRegistration : ListenerRegistration
    private var shouldInitRecyclerView = true
    private lateinit var messagesSection: Section
    private var recyclerView: RecyclerView? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityChatBinding.inflate(layoutInflater)
        setContentView(binding.root)
        recyclerView = binding.recyclerViewMessages
        //TODO Actionbar back button impl
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = intent.getStringExtra(AppConstants.USER_NAME)

        val otherUserId = intent.getStringExtra(AppConstants.USER_ID)
        FirestoreUtil.getOrCreateChatChannel(otherUserId!!) {channelId ->
            messagesListenerRegistration = FirestoreUtil.addChatMessageListener(channelId = channelId , context = this , this::updateRecyclerView)

            binding.sendMessageBtn.setOnClickListener {
                val messageToSend = TextMessage(text = binding.editTextMessage.text.toString(), time = Calendar.getInstance().time, FirebaseHelper.getInstance().currentUser!!.uid, type = MessageType.TEXT)
                binding.editTextMessage.setText("")
                FirestoreUtil.sendMessage(messageToSend,channelId)

                binding.sendImageButton.setOnClickListener {
                    //TODO implement sending images
                }
            }
        }
    }

    private fun updateRecyclerView(messages: List<TextMessageItem>) {
        fun init() {
            recyclerView!!.apply {
                layoutManager = LinearLayoutManager(this@ChatActivity)
                adapter = GroupieAdapter().apply {
                    messagesSection = Section(messages)
                    this.add(messagesSection)
                }
            }
            shouldInitRecyclerView = false
        }

        fun updateItems() = messagesSection.update(messages)

        if (shouldInitRecyclerView){
            init()
        } else {
            updateItems()
        }

        recyclerView!!.scrollToPosition(recyclerView!!.adapter!!.itemCount - 1)
    }

    override fun onDestroy() {
        super.onDestroy()
        recyclerView?.adapter = null
        recyclerView = null
        FirestoreUtil.removeListener(messagesListenerRegistration)
    }
}