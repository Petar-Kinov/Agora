package com.example.agora.data.Messaging.Model

import android.view.View
import com.example.agora.R
import com.example.agora.databinding.PersonRowItemBinding
import com.xwray.groupie.viewbinding.BindableItem

class Chat(val name : String, val userId : String) : BindableItem<PersonRowItemBinding>(){

    override fun bind(viewBinding: PersonRowItemBinding, position: Int) {
        viewBinding.personNameTV.text = name
    }

    override fun getLayout(): Int = R.layout.person_row_item

    override fun initializeViewBinding(view: View): PersonRowItemBinding = PersonRowItemBinding.bind(view)
}