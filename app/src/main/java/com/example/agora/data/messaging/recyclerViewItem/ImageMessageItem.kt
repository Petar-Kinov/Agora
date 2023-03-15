package com.example.agora.data.messaging.recyclerViewItem

import android.content.Context
import android.util.Log
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import com.example.agora.R
import com.example.agora.data.messaging.model.ImageMessage
import com.example.agora.databinding.ItemImageMessageBinding
import com.example.agora.util.FirebaseHelper
import com.example.agora.util.GlideApp
import com.example.agora.util.StorageUtil
import com.xwray.groupie.Item
import java.text.SimpleDateFormat

private const val TAG = "ImageMessageItem"

class ImageMessageItem(val message : ImageMessage, val context : Context): MessageItem<ItemImageMessageBinding>(message) {

    override fun bind(viewBinding: ItemImageMessageBinding, position: Int) {
//        viewBinding.imageMessageIV.set
        GlideApp.with(context)
            .load(StorageUtil.pathToReference(message.imagePath))
            .placeholder(R.drawable.something)
            .into(viewBinding.imageMessageIV)

        Log.d(TAG, "bind: imagePath is ${StorageUtil.pathToReference(message.imagePath)}")

        setTimeText(viewBinding)
        setMessageRootGravity(viewBinding)
    }

    override fun getLayout(): Int = R.layout.item_image_message

    override fun initializeViewBinding(view: View): ItemImageMessageBinding =
        ItemImageMessageBinding.bind(view)

    private fun setTimeText(viewBinding: ItemImageMessageBinding) {
        val dateFormat = SimpleDateFormat.getDateTimeInstance(
            SimpleDateFormat.SHORT,
            SimpleDateFormat.SHORT
        )
        viewBinding.timeTV.text = dateFormat.format(message.time)
    }

    private fun setMessageRootGravity(viewBinding: ItemImageMessageBinding) {
        if (message.senderId == FirebaseHelper.getInstance().currentUser?.uid) {
            viewBinding.messageRoot.apply {
                setBackgroundResource(R.drawable.rect_round_primary_color)
                val lParams = FrameLayout.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT, Gravity.END
                )
                this.layoutParams = lParams
            }
        } else {
            viewBinding.messageRoot.apply {
                setBackgroundResource(R.drawable.rect_round_darkblue)
                val lParams = FrameLayout.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT, Gravity.START
                )
                this.layoutParams = lParams
            }
        }
    }

    override fun isSameAs(other: Item<*>): Boolean {
        if (other !is ImageMessageItem)
            return false
        if (this.message != other.message)
            return false
        return true
    }
    override fun equals(other: Any?): Boolean {
        return isSameAs((other as ImageMessageItem))
    }

    override fun hashCode(): Int {
        var result = message.hashCode()
        result = 31 * result + context.hashCode()
        return result
    }

}