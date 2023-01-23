package com.example.agora.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.agora.databinding.SellingRowItemBinding
import com.example.agora.model.Item

class SellItemsRecyclerAdapter : ListAdapter<Item,SellItemsRecyclerAdapter.MyViewHolder> (ItemDiffCallBack()) {

    inner class MyViewHolder(binding : SellingRowItemBinding) : RecyclerView.ViewHolder(binding.root) {
        val name : TextView
        val description : TextView
        val price : TextView
        val seller : TextView

        init {
            name = binding.nameTV
            description = binding.descriptionTV
            price = binding.priceTV
            seller = binding.sellerNmaeTV
        }
        fun onBind(item : Item) {

        }

    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val binding = SellingRowItemBinding.inflate(LayoutInflater.from(parent.context), parent , false)
        return MyViewHolder(binding)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        holder.name.text = getItem(position).title
        holder.description.text = getItem(position).description
        holder.price.text = getItem(position).price
        holder.seller.text = getItem(position).seller
    }


    private class ItemDiffCallBack : DiffUtil.ItemCallback<Item>() {
        override fun areItemsTheSame(oldItem: Item, newItem: Item): Boolean =
            oldItem == newItem

        override fun areContentsTheSame(oldItem: Item, newItem: Item): Boolean =
            oldItem == newItem
    }

}