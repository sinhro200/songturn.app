package com.sinhro.songturn.app.ui.objects

import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.drawerlayout.widget.DrawerLayout
import com.mikepenz.materialdrawer.AccountHeader
import com.mikepenz.materialdrawer.AccountHeaderBuilder
import com.mikepenz.materialdrawer.Drawer
import com.mikepenz.materialdrawer.DrawerBuilder
import com.mikepenz.materialdrawer.model.PrimaryDrawerItem
import com.mikepenz.materialdrawer.model.ProfileDrawerItem
import com.mikepenz.materialdrawer.model.interfaces.IDrawerItem
import com.sinhro.songturn.app.R
import com.sinhro.songturn.app.ui.fragments.CreateRoomFragment
import com.sinhro.songturn.app.ui.fragments.EnterRoomFragment
import com.sinhro.songturn.app.ui.fragments.HowToUseFragment
import com.sinhro.songturn.app.ui.fragments.SettingsFragment
import com.sinhro.songturn.app.utils.replaceFragment
import com.sinhro.songturn.app.view_models.UserInfoViewModel

class AppDrawer(
    private val activity: AppCompatActivity,
    private val toolbar: Toolbar
) {
    private lateinit var mDrawer: Drawer
    private lateinit var mHeader: AccountHeader
    private lateinit var mDrawerLayout: DrawerLayout

    fun create() {
        createHeader()
        createDrawer()
        mDrawerLayout = mDrawer.drawerLayout
        activity.viewModels<UserInfoViewModel>()
            .value
            .userLiveData
            .observe(activity, {
                setProfile(
                    it.nickname,
                    if (it.isDemo) activity.getString(R.string.auth_login_demo_account_tab_title) else it.email
                )
            })
    }

    fun setProfile(name: String, email: String) {
        mHeader.profiles?.clear()
        mHeader.addProfile(
            ProfileDrawerItem()
                .withName(name)
                .withEmail(email),
            0
        )
    }

    fun disableDrawer() {
        mDrawer.actionBarDrawerToggle?.isDrawerIndicatorEnabled = false
        activity.supportActionBar?.setDisplayHomeAsUpEnabled(true)
        mDrawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED)
        toolbar.setNavigationOnClickListener {
            activity.supportFragmentManager.popBackStack()
        }
    }

    fun enableDrawer() {
        activity.supportActionBar?.setDisplayHomeAsUpEnabled(false)
        mDrawer.actionBarDrawerToggle?.isDrawerIndicatorEnabled = true
        mDrawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED)
        toolbar.setNavigationOnClickListener {
            mDrawer.openDrawer()
        }
    }

    private fun createDrawer() {
        mDrawer = DrawerBuilder()
            .withActivity(activity)
            .withToolbar(toolbar)
            .withActionBarDrawerToggle(true)
            .withSelectedItem(-1)
            .withAccountHeader(mHeader)
            .addDrawerItems(
                PrimaryDrawerItem()
                    .withIdentifier(101)
                    .withIconTintingEnabled(true)
                    .withName("Заказать песню")
                    .withSelectable(false),
                PrimaryDrawerItem()
                    .withIdentifier(102)
                    .withIconTintingEnabled(true)
                    .withName("Создать комнату")
                    .withSelectable(false),
                PrimaryDrawerItem()
                    .withIdentifier(103)
                    .withIconTintingEnabled(true)
                    .withName("Войти в комнату")
                    .withSelectable(false),
            )
            .addStickyDrawerItems(
                PrimaryDrawerItem()
                    .withIdentifier(200)
                    .withIconTintingEnabled(true)
                    .withName("Настройки")
                    .withSelectable(false)
            )
            .withOnDrawerItemClickListener(object : Drawer.OnDrawerItemClickListener {
                override fun onItemClick(
                    view: View?,
                    position: Int,
                    drawerItem: IDrawerItem<*>
                ): Boolean {
                    when (drawerItem.identifier) {
                        101L ->
                            activity.replaceFragment(HowToUseFragment())
                        102L ->
                            activity.replaceFragment(CreateRoomFragment())
                        103L ->
                            activity.replaceFragment(EnterRoomFragment())
                        200L ->
                            activity.replaceFragment(SettingsFragment())
                    }
                    return false
                }
            }).build()
    }

    private fun createHeader() {
        mHeader = AccountHeaderBuilder()
            .withActivity(activity)
            .withProfiles(
                mutableListOf(
                    ProfileDrawerItem()
                        .withName("name")
                        .withEmail("email")
                        .withNameShown(true)
                )
            )
            .withHeaderBackground(R.drawable.header)
            .addProfiles(

            )
            .build()
        activity.viewModels<UserInfoViewModel>().value.userLiveData.observe(
            activity, {
                mHeader.updateProfile(
                    ProfileDrawerItem()
                        .withName(it.nickname)
                        .withEmail(it.email)
                )
            }
        )
    }


}