package com.example.agora.data.core.model

import android.view.View
import com.example.agora.R
import com.example.agora.databinding.ItemHeaderBinding
import com.xwray.groupie.ExpandableGroup
import com.xwray.groupie.ExpandableItem
import com.xwray.groupie.viewbinding.BindableItem

class HeaderItem(val title: String) : BindableItem<ItemHeaderBinding>(), ExpandableItem {

    private lateinit var expandableGroup: ExpandableGroup

    override fun bind(viewBinding: ItemHeaderBinding, position: Int) {
        viewBinding.categoryTV.text = title
        viewBinding.arrowIV.setImageResource(getArrowIconResId())

        viewBinding.headerRoot.setOnClickListener {
            expandableGroup.onToggleExpanded()
            viewBinding.arrowIV.setImageResource(getArrowIconResId())
        }
    }

    override fun getLayout(): Int = R.layout.item_header

    override fun initializeViewBinding(view: View): ItemHeaderBinding {
        return ItemHeaderBinding.bind(view)
    }

    override fun setExpandableGroup(onToggleListener: ExpandableGroup) {
        expandableGroup = onToggleListener
    }

    private fun getArrowIconResId() =
        if (expandableGroup.isExpanded)
            R.drawable.ic_arrow_up
        else
            R.drawable.ic_arrow_down

}