package com.example.agora.adapters

import android.graphics.Bitmap
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
import com.example.agora.databinding.PictureListItemBinding

class PictureBitmapListAdapter(private val onClickListener: (Int) -> Unit) :
    ListAdapter<Bitmap, PictureBitmapListAdapter.BitmapViewHolder>(BitmapDC()) {

    private lateinit var deleteBtn : ImageView

    inner class BitmapViewHolder(
        val binding: PictureListItemBinding,
        clickAtPosition: (Int) -> Unit
    ) : RecyclerView.ViewHolder(binding.root), OnClickListener {

        init {
            deleteBtn = binding.deleteBtn
            deleteBtn.setOnClickListener {
                clickAtPosition(adapterPosition)
                val list = currentList.toMutableList()
                list.removeAt(adapterPosition)
                submitList(list)
            }

            itemView.setOnClickListener {
                clickAtPosition(adapterPosition)
            }
        }

        override fun onClick(v: View?) {

            if (adapterPosition == RecyclerView.NO_POSITION) return

            val clicked = getItem(adapterPosition)
        }

        fun bind(bitmap: Bitmap) = with(itemView) {
            binding.pictureView.setImageBitmap(bitmap)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) : BitmapViewHolder {
        val binding = PictureListItemBinding.inflate(LayoutInflater.from(parent.context),parent,false)
        return BitmapViewHolder(binding){
            onClickListener(it)
        }
    }

    override fun onBindViewHolder(holder: BitmapViewHolder, position: Int) {
        holder.bind(getItem(position))

    }


    fun swapData(data: List<Bitmap>) {
        submitList(data.toMutableList())
    }


    private class BitmapDC : DiffUtil.ItemCallback<Bitmap>() {
        override fun areItemsTheSame(
            oldItem: Bitmap,
            newItem: Bitmap
        ): Boolean {
            return oldItem == newItem
        }

        override fun areContentsTheSame(
            oldItem: Bitmap,
            newItem: Bitmap
        ): Boolean {
            //bitmap does not have .equals()
            return false
        }
    }
}