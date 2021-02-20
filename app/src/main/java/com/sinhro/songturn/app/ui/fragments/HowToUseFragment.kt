package com.sinhro.songturn.app.ui.fragments

import android.graphics.BitmapFactory
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.Target.SIZE_ORIGINAL
import com.sinhro.songturn.app.R
import kotlinx.android.synthetic.main.fragment_how_to_use.*

class HowToUseFragment : DisabledDrawerFragment(R.layout.fragment_how_to_use) {

    override fun onStart() {
        super.onStart()
        Glide.with(this)
            .load(R.raw.share_instruction_1)
            .override(SIZE_ORIGINAL, SIZE_ORIGINAL)
            .fitCenter()
            .into(fragment_how_to_use_image_1)

        Glide.with(this)
            .load(R.raw.share_instruction_2)
            .override(SIZE_ORIGINAL, SIZE_ORIGINAL)
            .fitCenter()
            .into(fragment_how_to_use_image_2)
    }

    override fun onResume() {
        super.onResume()
        activity?.title = getString(R.string.fragment_how_to_use_fragment_title)
    }
}