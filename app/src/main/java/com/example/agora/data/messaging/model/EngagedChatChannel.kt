package com.example.agora.data.messaging.model

import android.net.Uri
import android.util.Log
import android.view.View
import com.bumptech.glide.request.RequestOptions
import com.example.agora.R
import com.example.agora.databinding.EngagedChatRowItemBinding
import com.example.agora.util.GlideApp
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import com.xwray.groupie.viewbinding.BindableItem
import java.text.SimpleDateFormat

private const val TAG = "EngagedChatChannel"
class EngagedChatChannel(val channelId : String, val lastMessage: LastMessage, val otherUserName : String, val otherUserId : String) : BindableItem<EngagedChatRowItemBinding>() {

    constructor() : this("", LastMessage(), "", "")

    override fun bind(viewBinding: EngagedChatRowItemBinding, position: Int) {
        viewBinding.personNameTV.text = otherUserName
        viewBinding.lastMessageTV.text = lastMessage.text
        val dateFormat = SimpleDateFormat.getDateTimeInstance(
            SimpleDateFormat.SHORT,
            SimpleDateFormat.SHORT)
        viewBinding.messageTimeTV.text = dateFormat.format(lastMessage.time)

        val storageRef = Firebase.storage.reference.child("avatars/$otherUserId")
        storageRef.downloadUrl.addOnSuccessListener {
            loadAvatar(it, viewBinding)
        }.addOnFailureListener {
            Log.d(TAG, "bind: Failed to load avatar for $otherUserName")
        }
    }

    override fun getLayout(): Int = R.layout.engaged_chat_row_item

    override fun initializeViewBinding(view: View): EngagedChatRowItemBinding = EngagedChatRowItemBinding.bind(view)

    private fun loadAvatar(uri : Uri, viewBinding: EngagedChatRowItemBinding) {
        val options: RequestOptions = RequestOptions()
            .circleCrop()

        GlideApp.with(viewBinding.root.context).load(uri).apply(options).into(viewBinding.personAvatarIV)
    }
}