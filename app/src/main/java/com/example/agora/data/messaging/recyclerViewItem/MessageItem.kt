package com.example.agora.data.messaging.recyclerViewItem

import android.view.Gravity
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.viewbinding.ViewBinding
import com.example.agora.R
import com.example.agora.data.messaging.model.Message
import com.example.agora.databinding.ItemTextMessageBinding
import com.example.agora.util.FirebaseHelper
import com.xwray.groupie.viewbinding.BindableItem
import java.text.SimpleDateFormat

abstract class MessageItem<T: ViewBinding>(private val message : Message) : BindableItem<T>() {

//    abstract fun getBinding(viewBinding: ViewBinding) : ViewBinding

    private fun setTimeText(viewBinding: ItemTextMessageBinding) {
        val dateFormat = SimpleDateFormat.getDateTimeInstance(
            SimpleDateFormat.SHORT,
            SimpleDateFormat.SHORT)
        viewBinding.timeTV.text = dateFormat.format(message.time)
    }

    private fun setMessageRootGravity(viewBinding: ItemTextMessageBinding){
        if (message.senderId == FirebaseHelper.getInstance().currentUser?.uid){
            viewBinding.messageRoot.apply {
                setBackgroundResource(R.drawable.rect_round_primary_color)
                val lParams = FrameLayout.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT, Gravity.END)
                this.layoutParams = lParams
            }
        } else {
            viewBinding.messageRoot.apply {
                setBackgroundResource(R.drawable.rect_round_darkblue)
                val lParams = FrameLayout.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT, Gravity.START)
                this.layoutParams = lParams
            }
        }
    }
}