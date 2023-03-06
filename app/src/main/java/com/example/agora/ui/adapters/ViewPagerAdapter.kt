package com.example.agora.ui.adapters

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.example.agora.ui.fragments.core.BuyFragment
import com.example.agora.ui.fragments.core.SellFragment

class ViewPagerAdapter(fragmentManager : FragmentManager, lifecycle: Lifecycle) : FragmentStateAdapter(fragmentManager, lifecycle) {
    override fun getItemCount(): Int {
        return 2
    }

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> BuyFragment()
            1 -> SellFragment()
            else -> throw IllegalArgumentException("Invalid ViewPager position: $position")
        }
    }
}