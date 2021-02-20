package com.sinhro.songturn.app.ui.fragments

import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.sinhro.songturn.app.R
import com.sinhro.songturn.app.repositories.AuthRepository
import com.sinhro.songturn.app.ui.activities.MainActivity
import com.sinhro.songturn.app.utils.OnEyeClickListener
import com.sinhro.songturn.app.utils.replaceActivity
import com.sinhro.songturn.rest.request_response.AuthReqData
import kotlinx.android.synthetic.main.fragment_login.*

class LoginFragment : Fragment(R.layout.fragment_login) {

    //    val authViewModel: AuthViewModel by activityViewModels()
    val authRepository: AuthRepository by lazy {
        AuthRepository()
    }

    override fun onStart() {
        super.onStart()
        auth_password_text_field_boxes.endIconImageButton.setOnClickListener(
            OnEyeClickListener(
                auth_password_extended_edit_text,
                auth_password_text_field_boxes
            )
        )

    }

    fun enterButtonPressed() {
        loginUser()
    }

    override fun onResume() {
        super.onResume()
        setHasOptionsMenu(true)
        activity?.title = getString(R.string.auth_login_page_title)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        activity?.menuInflater?.inflate(
            R.menu.login_menu, menu
        )
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.login_menu_register ->

                requireActivity().supportFragmentManager
                    .beginTransaction()
                    .addToBackStack(null)
                    .replace(
                        R.id.dataContainer,
                        RegisterFragment()
                    )
                    .commit()
        }
        return true
    }

    private fun loginUser() {
        val login = auth_login_extended_edit_text.text.toString().trim()
        val pass = auth_password_extended_edit_text.text.toString().trim()
        authRepository.authUser(AuthReqData(login, pass))
            .withErrorCollectorViewModel()
            .withOnSuccessCallback {
                (activity as AppCompatActivity).replaceActivity(MainActivity())
            }
            .run()
    }
}