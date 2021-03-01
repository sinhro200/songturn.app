package com.sinhro.songturn.app.ui.fragments

import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import androidx.fragment.app.activityViewModels
import com.sinhro.songturn.app.R
import com.sinhro.songturn.app.ui.activities.MainActivity
import com.sinhro.songturn.app.utils.changeValueDialog
import com.sinhro.songturn.app.view_models.UserInfoViewModel
import com.sinhro.songturn.rest.model.RegisterUserInfo
import kotlinx.android.synthetic.main.fragment_settings.*

class SettingsFragment : DisabledDrawerFragment(R.layout.fragment_settings) {

    private val userInfoViewModel: UserInfoViewModel by activityViewModels()

    override fun onResume() {
        super.onResume()
        activity?.title = getString(R.string.settings_fragment_title)
        setHasOptionsMenu(true)
        initFields()
        initFunctionality()
    }

    fun initFunctionality() {
        settings_btn_nickname.setOnClickListener {
            changeValueDialog(
                getString(R.string.settings_dialog_change_nickname_label),
                settings_nickname.text.toString()
            ) {
                if (it != settings_nickname.text.toString())
                    userInfoViewModel.changeUser(RegisterUserInfo(nickname = it))
                        .withErrorCollectorViewModel()
                        .run()
            }
        }
    }

    fun initFields() {
        userInfoViewModel.userLiveData.observe(this, {
            settings_nickname.text = it.nickname
            settings_login.text = it.login
            settings_email.text =
                if (it.isDemo) getString(R.string.auth_login_demo_account_tab_title) else it.email
            settings_user_nickname.text = it.nickname
        })
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        activity?.menuInflater?.inflate(R.menu.settings_action_menu, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.settings_menu_exit -> {
                (activity as MainActivity).logout()
            }
        }
        return true
    }
}