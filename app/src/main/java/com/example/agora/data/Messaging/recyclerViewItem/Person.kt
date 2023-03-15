package com.example.agora.data.Messaging.recyclerViewItem

import android.net.Uri
import android.util.Log
import android.view.View
import com.bumptech.glide.request.RequestOptions
import com.example.agora.R
import com.example.agora.data.Messaging.Model.LastMessage
import com.example.agora.databinding.PersonRowItemBinding
import com.example.agora.util.GlideApp
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import com.xwray.groupie.viewbinding.BindableItem
import java.text.SimpleDateFormat

private const val TAG = "Person"
class Person(val name : String, val firestoreUserId : String, val lastMessage : LastMessage) : BindableItem<PersonRowItemBinding>(){

    override fun bind(viewBinding: PersonRowItemBinding, position: Int) {
        viewBinding.personNameTV.text = name
        viewBinding.lastMessageTV.text = lastMessage.text
        val dateFormat = SimpleDateFormat.getDateTimeInstance(
            SimpleDateFormat.SHORT,
            SimpleDateFormat.SHORT)
        viewBinding.messageTimeTV.text = dateFormat.format(lastMessage.time)

        val storageRef = Firebase.storage.reference.child("avatars/$firestoreUserId")
        storageRef.downloadUrl.addOnSuccessListener {
            loadAvatar(it, viewBinding)
        }.addOnFailureListener {
            Log.d(TAG, "bind: Failed to load avatar for $name")
        }
    }

    private fun loadAvatar(uri : Uri, viewBinding: PersonRowItemBinding) {
        val options: RequestOptions = RequestOptions()
            .circleCrop()

        GlideApp.with(viewBinding.root.context).load(uri).apply(options).into(viewBinding.personAvatarIV)
    }

    override fun getLayout(): Int = R.layout.person_row_item

    override fun initializeViewBinding(view: View): PersonRowItemBinding = PersonRowItemBinding.bind(view)
}