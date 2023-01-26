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
import com.example.agora.databinding.SellingRowItemBinding
import com.example.agora.model.Item

class SellItemsRecyclerAdapter : ListAdapter<Item,SellItemsRecyclerAdapter.MyViewHolder> (ItemDiffCallBack()) {

    private lateinit var glide : RequestManager

    inner class MyViewHolder(binding : SellingRowItemBinding) : RecyclerView.ViewHolder(binding.root) {
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
        }
        fun onBind(item : Item) {
        }
    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        glide = Glide.with(parent.context)
        val binding = SellingRowItemBinding.inflate(LayoutInflater.from(parent.context), parent , false)
        return MyViewHolder(binding)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        holder.titleTV.text = getItem(position).title
        holder.descriptionTV.text = getItem(position).description
        holder.priceTV.text = getItem(position).price
        holder.sellerTV.text = getItem(position).seller
        glide.load(getItem(position).downloadUrl).into(holder.pictureIV)
    }

    private class ItemDiffCallBack : DiffUtil.ItemCallback<Item>() {
        override fun areItemsTheSame(oldItem: Item, newItem: Item): Boolean =
            oldItem == newItem

        override fun areContentsTheSame(oldItem: Item, newItem: Item): Boolean =
            oldItem == newItem
    }

}