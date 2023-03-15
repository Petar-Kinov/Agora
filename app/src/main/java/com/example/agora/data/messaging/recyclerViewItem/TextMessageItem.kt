package com.example.agora.data.messaging.recyclerViewItem

import android.content.Context
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import com.example.agora.R
import com.example.agora.data.messaging.model.TextMessage
import com.example.agora.databinding.ItemTextMessageBinding
import com.example.agora.util.FirebaseHelper
import com.xwray.groupie.Item
import java.text.SimpleDateFormat

private const val TAG = "TextMessageItem"
class TextMessageItem(val message : TextMessage, val context : Context)  : MessageItem<ItemTextMessageBinding>(message){

    override fun bind(viewBinding: ItemTextMessageBinding, position: Int) {
        viewBinding.messageTV.text = message.text
        setTimeText(viewBinding)
        setMessageRootGravity(viewBinding)
    }

    override fun getLayout(): Int = R.layout.item_text_message

    override fun initializeViewBinding(view: View): ItemTextMessageBinding = ItemTextMessageBinding.bind(view)

    private fun setTimeText(viewBinding: ItemTextMessageBinding) {
        val dateFormat = SimpleDateFormat.getDateTimeInstance(SimpleDateFormat.SHORT,SimpleDateFormat.SHORT)
        viewBinding.timeTV.text = dateFormat.format(message.time)
    }

    private fun setMessageRootGravity(viewBinding: ItemTextMessageBinding){
        if (message.senderId == FirebaseHelper.getInstance().currentUser?.uid){
            viewBinding.messageRoot.apply {
                setBackgroundResource(R.drawable.rect_round_primary_color)
                val lParams = FrameLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,ViewGroup.LayoutParams.WRAP_CONTENT, Gravity.END)
                this.layoutParams = lParams
            }
        } else {
            viewBinding.messageRoot.apply {
                setBackgroundResource(R.drawable.rect_round_darkblue)
                val lParams = FrameLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,ViewGroup.LayoutParams.WRAP_CONTENT, Gravity.START)
                this.layoutParams = lParams
            }
        }
    }

    override fun isSameAs(other: Item<*>): Boolean {
        if (other !is TextMessageItem) return false
        // Compare items by their message field
        return message.equals(other.message)
    }

    override fun equals(other: Any?): Boolean {
        if (other == null || other !is TextMessageItem) return false
        // Compare items by their message field
        return message.equals(other.message)
    }

    override fun hashCode(): Int {
        var result = message.hashCode()
        result = 31 * result + context.hashCode()
        return result
    }

}