package com.sinhro.songturn.app.ui.fragments

import androidx.fragment.app.activityViewModels
import com.sinhro.songturn.app.R
import com.sinhro.songturn.app.model.ApplicationData
import com.sinhro.songturn.app.ui.objects.AppLengthTextWatcher
import com.sinhro.songturn.app.ui.objects.ValidatorProvider
import com.sinhro.songturn.app.utils.SetErrorHelpers
import com.sinhro.songturn.app.utils.replaceFragment
import com.sinhro.songturn.app.utils.showToast
import com.sinhro.songturn.app.view_models.RoomViewModel
import com.sinhro.songturn.rest.model.RoomSettings
import com.sinhro.songturn.rest.request_response.CreateRoomReqData
import com.sinhro.songturn.rest.validation.MAX_LENGTH_ROOM_TITLE
import com.sinhro.songturn.rest.validation.MIN_LENGTH_ROOM_TITLE
import kotlinx.android.synthetic.main.fragment_create_room.*

class CreateRoomFragment : DisabledDrawerFragment(R.layout.fragment_create_room) {

    private var createRoomReqData: CreateRoomReqData = CreateRoomReqData()

    private val roomViewModel: RoomViewModel by activityViewModels()

    override fun onStart() {
        super.onStart()
        initFunctionality()
    }

    override fun onResume() {
        super.onResume()
        activity?.title = getString(R.string.fragment_create_room_title)
    }

    private fun initFunctionality() {
        fragment_create_room_room_title_extendedEditText.addTextChangedListener(
            AppLengthTextWatcher(
                requireContext(),
                fragment_create_room_room_title_textFieldBoxes,
                MIN_LENGTH_ROOM_TITLE,
                MAX_LENGTH_ROOM_TITLE
            )
        )
        fragment_create_room_room_title_extendedEditText.setText(
            getString(R.string.room_,ApplicationData.userInfo?.nickname)
        )


        fragment_create_room_accept_button.setOnClickListener { createRoomButtonClicked() }
    }

    private fun createRoomButtonClicked() {
        collectRoomData()
        if (validateRoomData()) {
            roomViewModel.createRoom(createRoomReqData)
                .withErrorCollectorViewModel()
                .withOnSuccessCallback {
                    replaceFragment(RoomFragment())
                }
                .run()
        } else {
            showToast(getString(R.string.input_data_invalid))
        }
    }

    private fun collectRoomData() {
        createRoomReqData = CreateRoomReqData(
            fragment_create_room_room_title_extendedEditText.text.toString().trim(),
            RoomSettings(
                fragment_create_room_setting_priority_rarely_ordering_toggle_button.isChecked,
                fragment_create_room_setting_allow_votes_toggle_button.isChecked,
                fragment_create_room_setting_song_owner_visible_toggle_button.isChecked,
                fragment_create_room_setting_anybody_can_listen_toggle_button.isChecked
            )
        )
    }

    private fun validateRoomData(): Boolean {
        val vres = ValidatorProvider.validator.validate(createRoomReqData)
        SetErrorHelpers(
            vres.resultForEveryField(),
            mapOf("title" to fragment_create_room_room_title_textFieldBoxes),
            requireContext()
        )
        return vres.resultForErrorFields().isEmpty()
    }
}