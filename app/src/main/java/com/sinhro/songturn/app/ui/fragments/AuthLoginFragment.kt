package com.sinhro.songturn.app.ui.fragments

import androidx.fragment.app.Fragment
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.sinhro.songturn.app.R
import com.sinhro.songturn.app.ui.objects.AuthTabsPagerAdapter
import kotlinx.android.synthetic.main.fragment_auth_login.*

class AuthLoginFragment : Fragment(R.layout.fragment_auth_login) {

    private lateinit var adapter: AuthTabsPagerAdapter
    private lateinit var tabLayoutMediator: TabLayoutMediator

    private val loginDemoUserFragment = RegisterDemoUserFragment()
    private val loginFragment = LoginFragment()


    fun initFields() {
        adapter = AuthTabsPagerAdapter(
            childFragmentManager, lifecycle,
            loginDemoUserFragment, loginFragment
        )

        // Link the TabLayout and the ViewPager2 together and Set Text & Icons
        tabLayoutMediator = TabLayoutMediator(auth_tab_layout, auth_tabs_viewpager) { tab, position ->
            when (position) {
                0 -> {
                    tab.text = getString(R.string.auth_login_demo_account_tab_title)
                }
                1 -> {
                    tab.text = getString(R.string.auth_login_regular_account_tab_title)
                }
            }
        }
    }

    fun initFunctionality() {
        auth_tab_layout.tabMode = TabLayout.MODE_FIXED
        auth_tab_layout.isInlineLabel = true

        auth_tabs_viewpager.adapter = adapter
        auth_tabs_viewpager.isUserInputEnabled = true


        tabLayoutMediator.attach()


        auth_enter_button.setOnClickListener {
            if (auth_tab_layout.selectedTabPosition==0)
                loginDemoUserFragment.enterButtonPressed()
            else if (auth_tab_layout.selectedTabPosition==1)
                loginFragment.enterButtonPressed()
        }
    }

    override fun onStart() {
        super.onStart()
        initFields()
    }

    override fun onResume() {
        super.onResume()
        initFunctionality()
    }
}