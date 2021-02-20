package com.sinhro.songturn.app.ui.objects

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.viewpager2.adapter.FragmentStateAdapter

class AuthTabsPagerAdapter(
    fm: FragmentManager,
    lifecycle: Lifecycle,
    vararg val fragments: Fragment
) : FragmentStateAdapter(
    fm, lifecycle
) {
    override fun getItemCount(): Int = fragments.size

    override fun createFragment(position: Int): Fragment =
        fragments[position]
}