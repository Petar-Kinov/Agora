package com.example.agora.data.core.model

import android.util.Log
import android.view.View
import androidx.recyclerview.widget.DiffUtil
import com.bumptech.glide.Glide
import com.example.agora.R
import com.example.agora.databinding.ItemCardBinding
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import com.xwray.groupie.viewbinding.BindableItem

private const val TAG = "ItemsWithReference"

class ItemsWithReference(val item: Item, val documentReference: DocumentReference) :
    BindableItem<ItemCardBinding>() {

    override fun bind(viewBinding: ItemCardBinding, position: Int) {
        viewBinding.nameTV.text = item.title
        viewBinding.descriptionTV.text = item.description
        viewBinding.priceTV.text = item.price
        viewBinding.sellerNameTV.text = item.seller

//  viewBinding.root.setOnClickListener{
////   Log.d(TAG, "bind: item ${item.title} clicked")
//  }

        val storageRef = Firebase.storage.reference.child("items").child(item.storageRef)
        Log.d(TAG, "bind: storage ref is $storageRef")

        storageRef.list(1).addOnSuccessListener { resultList ->
            resultList.items[0].downloadUrl.addOnSuccessListener {
                Glide.with(viewBinding.root.context).load(it).into(viewBinding.pictureIV)
            }.addOnFailureListener {
                Log.d(TAG, "onBindViewHolder: failed to get Uri ")
            }

        }.addOnFailureListener {
            Log.d(TAG, "onBindViewHolder: failed ot load storeRef")
        }
    }


    override fun getLayout(): Int {
        return R.layout.item_card
    }
    override fun initializeViewBinding(view: View): ItemCardBinding {
        return ItemCardBinding.bind(view)
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