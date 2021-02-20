package com.sinhro.songturn.app.ui.activities

import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import com.sinhro.musicord.storage.Storage
import com.sinhro.songturn.app.databinding.ActivityAuthBinding
import com.sinhro.songturn.app.model.ApplicationData
import com.sinhro.songturn.app.ui.fragments.AuthLoginFragment
import com.sinhro.songturn.app.utils.initAsRootForSnackbar
import com.sinhro.songturn.app.utils.replaceFragment
import com.sinhro.songturn.app.utils.showError
import com.sinhro.songturn.app.view_models.ErrorViewModel

class AuthActivity : AppCompatActivity() {

    private lateinit var mBinding: ActivityAuthBinding
    lateinit var mToolbar: Toolbar

    private val errorViewModel: ErrorViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mBinding = ActivityAuthBinding.inflate(layoutInflater)
        setContentView(mBinding.root)
        ApplicationData.initStorage(Storage.getInstance(this))
    }

    private fun initFields() {
        mToolbar = mBinding.mainToolbar

        ErrorViewModel.initGlobal(errorViewModel)
        initAsRootForSnackbar(mBinding.root)
    }

    private fun initFunctionality() {
        setSupportActionBar(mToolbar)
        errorViewModel.commonErrorMutableLiveData.observe(this, { showError(it) })
    }

    override fun onStart() {
        super.onStart()
        initFields()
        initFunctionality()
        replaceFragment(AuthLoginFragment())
    }
}