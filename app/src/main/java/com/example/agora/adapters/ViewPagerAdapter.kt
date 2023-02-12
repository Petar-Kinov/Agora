package com.example.agora.adapters

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.example.agora.fragments.BuyFragment
import com.example.agora.fragments.SellFragment

class ViewPagerAdapter(fragmentManager : FragmentManager, lifecycle: Lifecycle) : FragmentStateAdapter(fragmentManager, lifecycle) {
    override fun getItemCount(): Int {
        return 2
    }

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> BuyFragment()
            else -> SellFragment()
        }
    }
}