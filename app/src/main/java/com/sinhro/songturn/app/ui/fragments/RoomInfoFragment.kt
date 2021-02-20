package com.sinhro.songturn.app.ui.fragments

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context.CLIPBOARD_SERVICE
import androidx.fragment.app.activityViewModels
import com.sinhro.songturn.app.R
import com.sinhro.songturn.app.ui.objects.AppUsersRecyclerViewAdapter
import com.sinhro.songturn.app.utils.showToast
import com.sinhro.songturn.app.view_models.RoomViewModel
import com.sinhro.songturn.rest.model.PublicUserInfo
import kotlinx.android.synthetic.main.fragment_room_info.*


class RoomInfoFragment : DisabledDrawerFragment(R.layout.fragment_room_info) {

    private lateinit var usersRecyclerViewAdapter: AppUsersRecyclerViewAdapter
    private val roomViewModel: RoomViewModel by activityViewModels()
    private val clipboardManager: ClipboardManager? by lazy {
        activity?.getSystemService(CLIPBOARD_SERVICE) as ClipboardManager?
    }

    override fun onStart() {
        super.onStart()
        initFields()
        initFunctionality()
    }

    private fun initFunctionality() {
        fragment_room_info_users_in_room_recyclerView.adapter = usersRecyclerViewAdapter

        roomViewModel.usersInRoomLiveData.observe(this, {
            usersRecyclerViewAdapter.setUsers(it)
        })

        roomViewModel.roomLiveData.observe(this, {
            fragment_room_info_invite_code_textView.text = it.inviteCode
            fragment_room_info_room_title_textView.text = it.title
        })

        fragment_room_info_invite_code_copy_imageView.setOnClickListener{
            copyInviteCodeClicked()
        }
    }

    private fun initFields() {
        usersRecyclerViewAdapter = AppUsersRecyclerViewAdapter(
            requireContext(),
            mutableListOf()
        )
    }

    fun copyInviteCodeClicked() {
        val text: String = fragment_room_info_invite_code_textView.text.toString()
        val clipData = ClipData.newPlainText("text", text)
        clipboardManager?.setPrimaryClip(clipData)
        showToast("Текст скопирован")
    }

    override fun onResume() {
        super.onResume()
        activity?.title = getString(R.string.fragment_room_info_title)
    }
}