package com.example.agora.data.Messaging.recyclerViewItem

import android.util.Log
import android.view.View
import com.bumptech.glide.request.RequestOptions
import com.example.agora.R
import com.example.agora.databinding.PersonRowItemBinding
import com.example.agora.util.GlideApp
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import com.xwray.groupie.viewbinding.BindableItem

private const val TAG = "Person"
class Person(val name : String, val firestoreUserId : String) : BindableItem<PersonRowItemBinding>(){

    override fun bind(viewBinding: PersonRowItemBinding, position: Int) {
        viewBinding.personNameTV.text = name

        val storageRef = Firebase.storage.reference.child("avatars/$firestoreUserId")
        storageRef.downloadUrl.addOnSuccessListener {
            val options: RequestOptions = RequestOptions()
                .circleCrop()
                .placeholder(R.mipmap.ic_launcher_round)
                .error(R.mipmap.ic_launcher_round)

            GlideApp.with(viewBinding.root.context).load(it).apply(options).into(viewBinding.personAvatarIV)
        }.addOnFailureListener {
            Log.d(TAG, "bind: Failed to load avatar for $name")
        }
    }

    override fun getLayout(): Int = R.layout.person_row_item

    override fun initializeViewBinding(view: View): PersonRowItemBinding = PersonRowItemBinding.bind(view)
}