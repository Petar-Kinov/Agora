package com.example.agora.adapters

import android.util.Log
import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.View.OnClickListener
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import com.bumptech.glide.RequestManager
import com.example.agora.GlideApp
import com.example.agora.R
import com.example.agora.databinding.ItemCardBinding
import com.example.agora.databinding.MyItemsCardBinding
import com.example.agora.model.Item
import com.example.agora.model.ItemsWithReference
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import kotlin.math.log

private const val TAG = "MyItemsListAdapter"

class MyItemsListAdapter(private val onClickListener: (ItemsWithReference) -> Unit) : ListAdapter<ItemsWithReference,MyItemsListAdapter.MyItemViewHolder> (ItemDiffCallBack()
) {

    private lateinit var glideApp : RequestManager


inner class MyItemViewHolder(binding : MyItemsCardBinding, clickAtPosition: (Int) -> Unit) : RecyclerView.ViewHolder(binding.root) {
    val titleTV : TextView
    val descriptionTV : TextView
    val priceTV : TextView
    val sellerTV : TextView
    val pictureIV : ImageView
    val deleteBtn : ImageView

    init {
        titleTV = binding.nameTV
        descriptionTV = binding.descriptionTV
        priceTV = binding.priceTV
        sellerTV = binding.sellerNameTV
        pictureIV = binding.pictureIV
        deleteBtn = binding.deleteAuctionBtn

        itemView.setOnClickListener {
            clickAtPosition(adapterPosition)
        }
    }

    fun onBind(item : Item) {
        //TODO move binding to here maybe
    }
}
override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyItemViewHolder {
    glideApp = GlideApp.with(parent.context)
    val binding = MyItemsCardBinding.inflate(LayoutInflater.from(parent.context), parent , false)
    return MyItemViewHolder(binding) {
        onClickListener(getItem(it))
    }
}

override fun onBindViewHolder(holder: MyItemViewHolder, position: Int) {
    holder.titleTV.text = getItem(position).item.title
    holder.descriptionTV.text = getItem(position).item.description
    holder.priceTV.text = getItem(position).item.price
    holder.sellerTV.text = getItem(position).item.seller

    val storageRef = Firebase.storage.getReference(getItem(position).item.storageRef)

    storageRef.list(1).addOnSuccessListener { resultList ->
        resultList.items[0].downloadUrl.addOnSuccessListener {
            glideApp.load(it).into(holder.pictureIV)
        }.addOnFailureListener {
            Log.d(TAG, "onBindViewHolder: failed to get Uri ")
        }

    }.addOnFailureListener {
        Log.d(TAG, "onBindViewHolder: failed ot load storeRef")
    }
//        glideApp.load(storageRef.child()).into(holder.pictureIV)

    holder.deleteBtn.setOnClickListener {
        Log.d(TAG, "onBindViewHolder: position is $position")
        Log.d(TAG, "onBindViewHolder: list is ${currentList.toString()}")
        onClickListener(getItem(position))
    }
}

private class ItemDiffCallBack : DiffUtil.ItemCallback<ItemsWithReference>() {
    override fun areItemsTheSame(oldItem: ItemsWithReference, newItem: ItemsWithReference): Boolean =
        oldItem == newItem

    override fun areContentsTheSame(oldItem: ItemsWithReference, newItem: ItemsWithReference): Boolean =
        oldItem == newItem
}

//    interface OnItemClickListener {
//        fun onItemClicked(position: Int)
//    }

}