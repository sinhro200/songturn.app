package com.sinhro.songturn.app.ui.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.sinhro.songturn.app.R
import com.sinhro.songturn.app.utils.replaceFragment
import kotlinx.android.synthetic.main.fragment_enter_create_room.*

class EnterCreateRoomFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_enter_create_room, container, false)
    }

    override fun onStart() {
        super.onStart()
        initFields()
        activity?.title = resources.getString(R.string.app_name)
    }

    fun initFields(){
        fragment_enter_create_room_enter_button.setOnClickListener {
            replaceFragment(EnterRoomFragment())
        }
        fragment_enter_create_room_create_button.setOnClickListener {
            replaceFragment(CreateRoomFragment())
        }
    }

}