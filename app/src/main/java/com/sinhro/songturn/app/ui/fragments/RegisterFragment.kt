package com.sinhro.songturn.app.ui.fragments

import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.sinhro.songturn.app.R
import com.sinhro.songturn.app.repositories.AuthRepository
import com.sinhro.songturn.app.ui.activities.AuthActivity
import com.sinhro.songturn.app.ui.objects.ValidatorProvider
import com.sinhro.songturn.app.utils.OnEyeClickListener
import com.sinhro.songturn.app.utils.SetErrorHelpers
import com.sinhro.songturn.app.utils.showToast
import com.sinhro.songturn.rest.model.RegisterUserInfo
import kotlinx.android.synthetic.main.fragment_auth_register.*

class RegisterFragment : Fragment(R.layout.fragment_auth_register) {

    private var registerUserInfo: RegisterUserInfo? = null

    private val authRepository: AuthRepository by lazy {
        AuthRepository()
    }

    override fun onResume() {
        super.onResume()
        activity?.title = getString(R.string.auth_register_page_title)
        initFunctionality()
        setupBackButton()
    }

    fun initFunctionality() {
        setHasOptionsMenu(true)

        password_register_text_field_boxes.endIconImageButton.setOnClickListener(
            OnEyeClickListener(
                password_register_extended_edit_text,
                password_register_text_field_boxes
            )
        )
        password_confirmation_register_text_field_boxes.endIconImageButton.setOnClickListener(
            OnEyeClickListener(
                password_confirmation_register_extended_edit_text,
                password_confirmation_register_text_field_boxes
            )
        )

        register_enter_button.setOnClickListener { enterButtonPressed() }
    }

    override fun onPause() {
        super.onPause()
        uninstallBackButton()
    }

    private fun backToLogin() {
        (activity as AuthActivity).supportFragmentManager.popBackStack()
    }

    private fun setupBackButton() {
        if (activity is AppCompatActivity) {
            (activity as AppCompatActivity?)?.apply {
                supportActionBar?.setDisplayHomeAsUpEnabled(true)
            }
            (activity as AuthActivity).apply {
                mToolbar.setNavigationOnClickListener {
                    backToLogin()
                }
            }
        }
    }

    private fun uninstallBackButton() {
        (activity as AppCompatActivity?)?.apply {
            supportActionBar?.setDisplayHomeAsUpEnabled(false)
        }
    }

    private fun enterButtonPressed() {
        collectRegisterData()
        if (validateRegisterData()) {
            registerUser()
        } else
            showToast(getString(R.string.register_data_invalid_unhandeled_common_error_toast))
    }

    fun collectRegisterData() {
        registerUserInfo = RegisterUserInfo(
            login_register_extended_edit_text.text.toString().trim(),
            email_register_extended_edit_text.text.toString().trim(),
            "",
            "",
            username_register_extended_edit_text.text.toString().trim(),
            password_register_extended_edit_text.text.toString().trim(),
        )
    }

    fun validateRegisterData(): Boolean {
        if (password_register_extended_edit_text.text.toString() !=
            password_confirmation_register_extended_edit_text.text.toString()
        ) {
            password_confirmation_register_text_field_boxes.setError(
                getString(R.string.field_data_error_passwords_not_matches), false
            )
            return false
        }
        registerUserInfo?.let {
            val validatorResult = ValidatorProvider.validator.validate(it)
            val resultMap = validatorResult.resultForEveryField()
            SetErrorHelpers(
                resultMap,
                mapOf(
                    "login" to login_register_text_field_boxes,
                    "email" to email_register_text_field_boxes,
                    "password" to password_register_text_field_boxes,
                    "nickname" to username_register_text_field_boxes,
                ),
                requireContext()
            )

            if (validatorResult.resultForErrorFields().isNotEmpty())
                return false
        }
        return true
    }

    fun registerUser() {
        registerUserInfo?.let {
            authRepository.registerUser(it).withErrorCollectorViewModel()
                .withOnSuccessCallback {
                    showToast(
                        if (it.mustConfirmByMail)
                            getString(R.string.ConfirmForFinishRegister)
                        else
                            getString(R.string.UserSuccessfullyRegistered)
                    )
                    backToLogin()
                }
                .run()

        }
    }


}