package com.sinhro.songturn.app.ui.fragments

import android.app.Activity
import android.content.ClipboardManager
import androidx.fragment.app.activityViewModels
import com.sinhro.songturn.app.R
import com.sinhro.songturn.app.ui.objects.ValidatorProvider
import com.sinhro.songturn.app.utils.getClipboardText
import com.sinhro.songturn.app.utils.replaceFragment
import com.sinhro.songturn.app.utils.showToast
import com.sinhro.songturn.app.view_models.RoomViewModel
import com.sinhro.songturn.rest.request_response.EnterRoomReqData
import kotlinx.android.synthetic.main.fragment_enter_room.*

class EnterRoomFragment : DisabledDrawerFragment(R.layout.fragment_enter_room) {

    private var enterRoomReqData = EnterRoomReqData()
    private val roomViewModel: RoomViewModel by activityViewModels()
    private lateinit var manager : ClipboardManager

    override fun onStart() {
        super.onStart()
        initFunctionality()
        manager = requireActivity().getSystemService(Activity.CLIPBOARD_SERVICE) as ClipboardManager
    }

    override fun onResume() {
        super.onResume()
        activity?.title = getString(R.string.fragment_enter_room_title)
        activity?.apply {
                val textIn = fragment_enter_room_room_invite_extendedEditText.text
                manager.getClipboardText(applicationContext)?.let { clipText ->
                    if (clipText.length in 3..9 && textIn.isEmpty())
                        fragment_enter_room_room_invite_extendedEditText.setText(clipText)
                }
        }
    }

    private fun initFunctionality() {
        fragment_enter_room_accept_button.setOnClickListener {
            enterRoomButtonClicked()
        }

        fragment_enter_room_room_invite_textFieldBoxes.endIconImageButton.setOnClickListener {
            fragment_enter_room_room_invite_extendedEditText.setText("")
        }
    }

    private fun enterRoomButtonClicked() {
        collectRoomData()
        if (validateRoomData()) {
            roomViewModel.enterRoom(enterRoomReqData)
                .withErrorCollectorViewModel()
                .withOnSuccessCallback {
                    roomViewModel.roomLiveData.postValue(it.roomInfo)
                    replaceFragment(RoomFragment())
                }
                .run()
        } else {
            showToast("data invalid")
        }
    }

    private fun collectRoomData() {
        enterRoomReqData = EnterRoomReqData(
            fragment_enter_room_room_invite_extendedEditText.text.toString().trim()
        )
    }

    private fun validateRoomData(): Boolean {
        val vres = ValidatorProvider.validator.validate(enterRoomReqData)
        /*SetErrorHelpers(
            vres.resultForEveryField(),
            mapOf("title" to fragment_create_room_room_title_textFieldBoxes),
            requireContext()
        )*/
        return vres.resultForErrorFields().isEmpty()
    }
}