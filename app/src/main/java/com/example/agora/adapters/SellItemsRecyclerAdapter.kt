package com.example.agora.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.RequestManager
import com.example.agora.databinding.ItemCardBinding
import com.example.agora.model.Item

class SellItemsRecyclerAdapter(private val onClickListener: (Item) -> Unit) : ListAdapter<Item,SellItemsRecyclerAdapter.MyViewHolder> (ItemDiffCallBack()) {

    private lateinit var glide : RequestManager


    inner class MyViewHolder(binding : ItemCardBinding, clickAtPosition: (Int) -> Unit) : RecyclerView.ViewHolder(binding.root) {
        val titleTV : TextView
        val descriptionTV : TextView
        val priceTV : TextView
        val sellerTV : TextView
        val pictureIV : ImageView

        init {
            titleTV = binding.nameTV
            descriptionTV = binding.descriptionTV
            priceTV = binding.priceTV
            sellerTV = binding.sellerNameTV
            pictureIV = binding.pictureIV

            itemView.setOnClickListener {
                clickAtPosition(adapterPosition)
            }
        }

        fun onBind(item : Item) {
        }
    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        glide = Glide.with(parent.context)
        val binding = ItemCardBinding.inflate(LayoutInflater.from(parent.context), parent , false)
        return MyViewHolder(binding) {
            onClickListener(getItem(it))
        }
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        holder.titleTV.text = getItem(position).title
        holder.descriptionTV.text = getItem(position).description
        holder.priceTV.text = getItem(position).price
        holder.sellerTV.text = getItem(position).seller
        glide.load(getItem(position).downloadUrl).into(holder.pictureIV)

//        holder.itemView.setOnClickListener {
//            onClickListener(getItem(position))
//        }
    }

    private class ItemDiffCallBack : DiffUtil.ItemCallback<Item>() {
        override fun areItemsTheSame(oldItem: Item, newItem: Item): Boolean =
            oldItem == newItem

        override fun areContentsTheSame(oldItem: Item, newItem: Item): Boolean =
            oldItem == newItem
    }

//    interface OnItemClickListener {
//        fun onItemClicked(position: Int)
//    }

}