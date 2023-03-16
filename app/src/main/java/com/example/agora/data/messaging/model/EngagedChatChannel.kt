package com.example.agora.data.messaging.model

import android.net.Uri
import android.util.Log
import android.view.View
import com.bumptech.glide.request.RequestOptions
import com.example.agora.R
import com.example.agora.databinding.EngagedChatRowItemBinding
import com.example.agora.util.GlideApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import com.xwray.groupie.Item
import com.xwray.groupie.viewbinding.BindableItem
import java.text.SimpleDateFormat
import java.util.*

private const val TAG = "EngagedChatChannel"
class EngagedChatChannel(val channelId : String, val lastMessage: LastMessage, val otherUserName : String, val otherUserId : String) : BindableItem<EngagedChatRowItemBinding>() {

    constructor() : this("", LastMessage(), "", "")

    override fun bind(viewBinding: EngagedChatRowItemBinding, position: Int) {
        viewBinding.personNameTV.text = otherUserName

        setLastMessageText(viewBinding,lastMessage)
        setLastMessageTime(viewBinding, lastMessage)

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
    
    private fun setLastMessageText(viewBinding: EngagedChatRowItemBinding, lastMessage : LastMessage) {
        if (lastMessage.text == "Photo") {
            if (lastMessage.senderId == FirebaseAuth.getInstance().currentUser!!.uid){
                viewBinding.lastMessageTV.text = "You sent a Photo"
            } else {
                viewBinding.lastMessageTV.text = "$otherUserName sent a Photo"
            }
        } else {
            viewBinding.lastMessageTV.text = lastMessage.text
        }
    }

    private fun setLastMessageTime(viewBinding: EngagedChatRowItemBinding, lastMessage: LastMessage) {
        val messageDate: Calendar = Calendar.getInstance()
        messageDate.time = lastMessage.time
        when {
            isToday(messageDate) -> viewBinding.messageTimeTV.text = getTimeFromDate(lastMessage.time)
            isThisWeek(messageDate) -> viewBinding.messageTimeTV.text = getDayOfWeek(lastMessage.time) ?: ""
            isThisYear(messageDate) -> viewBinding.messageTimeTV.text = getDayOfYear(lastMessage.time) ?: ""
            else -> viewBinding.messageTimeTV.text = getDayMonthYear(lastMessage.time) ?: ""
        }
    }

    private fun isToday(date: Calendar): Boolean {
        return (today.get(Calendar.YEAR) == date.get(Calendar.YEAR)
                && today.get(Calendar.DAY_OF_YEAR) == date.get(Calendar.DAY_OF_YEAR))
    }

    private fun isThisWeek(date: Calendar): Boolean {
        return (today.get(Calendar.YEAR) == date.get(Calendar.YEAR)
                && today.get(Calendar.WEEK_OF_YEAR) == date.get(Calendar.WEEK_OF_YEAR))
    }

    private fun isThisYear(date: Calendar) : Boolean {
        return (today.get(Calendar.YEAR) == date.get(Calendar.YEAR))
    }

    private fun getTimeFromDate(date: Date): String? {
        val dateFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
        return dateFormat.format(date)
    }

    private fun getDayOfWeek(date: Date) : String? {
        val dateFormat = SimpleDateFormat("EEE", Locale.getDefault())
        return dateFormat.format(date)
    }

    private fun getDayOfYear(date: Date) : String? {
        val dateFormat = SimpleDateFormat("d MMM", Locale.getDefault())
        return dateFormat.format(date)
    }

    private fun getDayMonthYear(date : Date) : String? {
        val dateFormat = SimpleDateFormat("d MMM", Locale.getDefault())
        return dateFormat.format(date)
    }

    override fun isSameAs(other: Item<*>): Boolean {
        return other is EngagedChatChannel && other.channelId == channelId
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is EngagedChatChannel) return false

        if (channelId != other.channelId) return false
        if (lastMessage != other.lastMessage) return false
        if (otherUserName != other.otherUserName) return false
        if (otherUserId != other.otherUserId) return false

        return true
    }

    override fun hashCode(): Int {
        var result = channelId.hashCode()
        result = 31 * result + lastMessage.hashCode()
        result = 31 * result + otherUserName.hashCode()
        result = 31 * result + otherUserId.hashCode()
        return result
    }


    companion object {
        val today: Calendar = Calendar.getInstance()
    }

}