package com.example.agora.data.core.model

import android.content.res.Resources
import android.util.Log
import android.view.View
import androidx.recyclerview.widget.DiffUtil
import com.bumptech.glide.Glide
import com.example.agora.R
import com.example.agora.databinding.ItemCardBinding
import com.example.agora.databinding.ItemCardSimplifiedBinding
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import com.xwray.groupie.viewbinding.BindableItem

private const val TAG = "ItemsWithReference"

class ItemsWithReference(val item: Item, val documentReference: DocumentReference) :
    BindableItem<ItemCardSimplifiedBinding>() {

    override fun bind(viewBinding: ItemCardSimplifiedBinding, position: Int) {
        viewBinding.titleTV.text = item.title
        val priceValue = item.price.toFloatOrNull() ?: 0.0f
        viewBinding.priceTV.text = viewBinding.root.context.getString(R.string.price_tag, priceValue)

//  viewBinding.root.setOnClickListener{
////   Log.d(TAG, "bind: item ${item.title} clicked")
//  }

        val storageRef = Firebase.storage.reference.child("items").child(item.storageRef)
        Log.d(TAG, "bind: storage ref is $storageRef")

        storageRef.list(1).addOnSuccessListener { resultList ->
            if (resultList.items.isNotEmpty()) {
                resultList.items[0].downloadUrl.addOnSuccessListener { uri ->
                    Glide.with(viewBinding.root.context).load(uri).into(viewBinding.pictureIV)
                }.addOnFailureListener {
                    Log.d(TAG, "onBindViewHolder: failed to get Uri ")
                }
            } else {
                Log.d(TAG, "onBindViewHolder: storage reference is empty")
            }
        }.addOnFailureListener {
            Log.d(TAG, "onBindViewHolder: failed ot load storeRef")
        }
    }

    override fun getLayout(): Int {
        return R.layout.item_card_simplified
    }
    override fun initializeViewBinding(view: View): ItemCardSimplifiedBinding {
        return ItemCardSimplifiedBinding.bind(view)
    }

    override fun isSameAs(other: com.xwray.groupie.Item<*>): Boolean {
        return item == (other as ItemsWithReference).item
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as ItemsWithReference

        if (item != other.item) return false
        if (documentReference != other.documentReference) return false

        return true
    }

    override fun hashCode(): Int {
        var result = item.hashCode()
        result = 31 * result + documentReference.hashCode()
        return result
    }

}

class GroupDiffCallback(
    private val oldList: List<ItemsWithReference>,
    private val newList: List<ItemsWithReference>
) : DiffUtil.Callback() {
    override fun getOldListSize() = oldList.size
    override fun getNewListSize() = newList.size
    override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int) =
        oldList[oldItemPosition].id == newList[newItemPosition].id

    override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int) =
        oldList[oldItemPosition] == newList[newItemPosition]
}