package com.sinhro.songturn.app.ui.fragments

import androidx.fragment.app.Fragment
import com.sinhro.songturn.app.ui.activities.MainActivity
import com.sinhro.songturn.app.utils.hideKeyboard

open class DisabledDrawerFragment(layout: Int) : Fragment(layout) {

    override fun onStart() {
        super.onStart()
        (activity as MainActivity).mAppDrawer.disableDrawer()
    }

    override fun onStop() {
        super.onStop()
        (activity as MainActivity).mAppDrawer.enableDrawer()
        activity?.title = ""
    }

    override fun onPause() {
        super.onPause()
        hideKeyboard()
    }

    override fun onResume() {
        super.onResume()
        hideKeyboard()
    }

}