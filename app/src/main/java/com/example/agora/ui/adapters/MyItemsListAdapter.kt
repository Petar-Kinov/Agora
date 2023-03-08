package com.example.agora.ui.adapters

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.RequestManager
import com.example.agora.data.core.model.ItemsWithReference
import com.example.agora.databinding.MyItemsCardBinding
import com.example.agora.util.GlideApp
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage

private const val TAG = "MyItemsListAdapter"

class MyItemsListAdapter(private val onClickListener: (ItemsWithReference) -> Unit) : ListAdapter<ItemsWithReference, MyItemsListAdapter.MyItemViewHolder> (
    ItemDiffCallBack()
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

        deleteBtn.setOnClickListener {
            clickAtPosition(adapterPosition)
        }
    }

    fun onBind(item : ItemsWithReference) {
        titleTV.text = item.item.title
        descriptionTV.text = item.item.description
        priceTV.text = item.item.price
        sellerTV.text = item.item.seller

        val storageRef = Firebase.storage.reference.child("items").child(item.item.storageRef)

        storageRef.list(1).addOnSuccessListener { resultList ->
            resultList.items[0].downloadUrl.addOnSuccessListener {
                glideApp.load(it).into(pictureIV)
            }.addOnFailureListener {
                Log.d(TAG, "onBindViewHolder: failed to get Uri ")
            }

        }.addOnFailureListener {
            Log.d(TAG, "onBindViewHolder: failed ot load storeRef")
        }

        deleteBtn.setOnClickListener {
            onClickListener(item)
        }
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
     holder.onBind(getItem(position))
}

private class ItemDiffCallBack : DiffUtil.ItemCallback<ItemsWithReference>() {
    override fun areItemsTheSame(oldItem: ItemsWithReference, newItem: ItemsWithReference): Boolean =
        oldItem == newItem

    override fun areContentsTheSame(oldItem: ItemsWithReference, newItem: ItemsWithReference): Boolean =
        //sometimes causes IndexOutOfBounds , have to change the equals() check
//        oldItem == newItem
        false
}

//    interface OnItemClickListener {
//        fun onItemClicked(position: Int)
//    }

}