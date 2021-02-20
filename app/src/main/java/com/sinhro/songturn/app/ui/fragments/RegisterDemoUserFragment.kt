package com.sinhro.songturn.app.ui.fragments

import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.sinhro.songturn.app.R
import com.sinhro.songturn.app.repositories.AuthRepository
import com.sinhro.songturn.app.ui.activities.MainActivity
import com.sinhro.songturn.app.ui.objects.AppLengthTextWatcher
import com.sinhro.songturn.app.ui.objects.ValidatorProvider
import com.sinhro.songturn.app.utils.SetErrorHelpers
import com.sinhro.songturn.app.utils.replaceActivity
import com.sinhro.songturn.app.utils.showToast
import com.sinhro.songturn.rest.model.RegisterDemoUserInfo
import com.sinhro.songturn.rest.validation.MAX_LENGTH_LOGIN
import com.sinhro.songturn.rest.validation.MIN_LENGTH_LOGIN
import kotlinx.android.synthetic.main.fragment_login_demo_user.*

class RegisterDemoUserFragment : Fragment(R.layout.fragment_login_demo_user) {

    val authRepository: AuthRepository by lazy {
        AuthRepository()
    }

    private var registerDemoUserInfo: RegisterDemoUserInfo? = null

    override fun onStart() {
        super.onStart()
        initFields()
    }

    private fun initFields() {

        auth_demo_login_and_nickname_extended_edit_text.addTextChangedListener(
            AppLengthTextWatcher(
                requireContext(),
                auth_demo_nickname_text_field_boxes,
                MIN_LENGTH_LOGIN,
                MAX_LENGTH_LOGIN
            )
        )
    }

    override fun onResume() {
        super.onResume()
        activity?.title = getString(R.string.auth_login_demo_account_tab_title)
    }

    private fun collectDataDemoUser() {
        val nicknameAndLogin =
            auth_demo_login_and_nickname_extended_edit_text.text.toString().trim()
        registerDemoUserInfo = RegisterDemoUserInfo(
            nicknameAndLogin, nicknameAndLogin
        )
    }

    private fun validateDemoUser(): Boolean {
        registerDemoUserInfo?.let {
            val validatorResult = ValidatorProvider.validator.validate(it)
            val resultMap = validatorResult.resultForEveryField()
            SetErrorHelpers(
                resultMap,
                mapOf(
                    "login" to auth_demo_nickname_text_field_boxes
                ),
                requireContext()
            )

            if (validatorResult.resultForErrorFields().isNotEmpty())
                return false
        }
        return true
    }

    private fun registerDemoUser() {
        registerDemoUserInfo?.let {
            authRepository.registerDemoUser(it)
                .withErrorCollectorViewModel()
                .withOnSuccessCallback {
                    (requireActivity() as AppCompatActivity).replaceActivity(
                        MainActivity()
                    )
                }
                .run()
        }
    }


    fun enterButtonPressed() {
        collectDataDemoUser()
        if (validateDemoUser())
            registerDemoUser()
        else
            showToast(getString(R.string.register_data_invalid))
    }


}