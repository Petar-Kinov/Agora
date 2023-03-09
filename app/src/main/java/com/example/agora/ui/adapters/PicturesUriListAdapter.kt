package com.example.agora.ui.adapters

import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.View.OnClickListener
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.RequestManager
import com.example.agora.databinding.PictureListItemBinding
import com.example.agora.util.GlideApp

class PicturesUriListAdapter(private val onClickListener: (Uri) -> Unit) :
    ListAdapter<Uri, PicturesUriListAdapter.PictureViewHolder>(UriDiffCallback()) {

    private lateinit var glideApp: RequestManager

    inner class PictureViewHolder(
        val binding: PictureListItemBinding,
        clickAtPosition: (Int) -> Unit
    ) : RecyclerView.ViewHolder(binding.root), OnClickListener {


        init {
            itemView.setOnClickListener {
                clickAtPosition(absoluteAdapterPosition)
            }
        }

        override fun onClick(v: View?) {

            if (adapterPosition == RecyclerView.NO_POSITION) return

            val clicked = getItem(absoluteAdapterPosition)
        }

        fun bind(uri: Uri) = with(itemView) {
            GlideApp.with(this).load(uri).into(binding.pictureView)
        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PictureViewHolder {
        glideApp = GlideApp.with(parent.context)
        val binding =
            PictureListItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return PictureViewHolder(binding) {
            onClickListener(getItem(it))
        }
    }

    override fun onBindViewHolder(holder: PictureViewHolder, position: Int) {
        holder.bind(getItem(position))
    }


    fun swapData(data: List<Uri>) {
        submitList(data.toMutableList())
    }


    private class UriDiffCallback : DiffUtil.ItemCallback<Uri>() {
        override fun areItemsTheSame(
            oldItem: Uri,
            newItem: Uri
        ): Boolean {
            return oldItem == newItem
        }

        override fun areContentsTheSame(
            oldItem: Uri,
            newItem: Uri
        ): Boolean {
            return oldItem == newItem
        }
    }
}