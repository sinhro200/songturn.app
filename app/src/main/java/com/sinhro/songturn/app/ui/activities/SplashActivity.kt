package com.sinhro.songturn.app.ui.activities

import android.content.Context
import android.graphics.drawable.Animatable2
import android.graphics.drawable.Drawable
import android.os.Build
import android.os.Bundle
import android.util.AttributeSet
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.vectordrawable.graphics.drawable.Animatable2Compat
import com.google.android.material.snackbar.Snackbar
import com.sinhro.musicord.storage.Storage
import com.sinhro.songturn.app.R
import com.sinhro.songturn.app.databinding.ActivitySplashBinding
import com.sinhro.songturn.app.model.ApplicationData
import com.sinhro.songturn.app.repositories.UserRepository
import com.sinhro.songturn.app.utils.SnackBarHelper
import com.sinhro.songturn.app.utils.initAsRootForSnackbar
import com.sinhro.songturn.app.utils.replaceActivity
import com.sinhro.songturn.app.utils.showError
import com.sinhro.songturn.app.view_models.ErrorViewModel
import com.sinhro.songturn.rest.ErrorCodes
import kotlinx.android.synthetic.main.activity_splash.*

class SplashActivity : AppCompatActivity() {

    /*
        There is no reason update user info,
        just need check internet connection and any Authorized endpoint for decide
            what next activity should replace to : Auth or Main.
     */
    private val userRepository: UserRepository by lazy {
        UserRepository(ApplicationData.access_token)
    }

    private lateinit var mBinding: ActivitySplashBinding

    private var numberUpdateTimes = 1

    private lateinit var loader: Drawable
    private var animationInProgress = true

    fun animationStart() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && loader is Animatable2)
            (loader as Animatable2).start()
        else
            (loader as Animatable2Compat).start()
    }

    fun animationStop() {
        animationInProgress = false
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && loader is Animatable2)
            (loader as Animatable2).stop()
        else
            (loader as Animatable2Compat).stop()
    }

    private fun initFields() {
        loader = loader_view.drawable

        ErrorViewModel.initGlobal(
            viewModels<ErrorViewModel>().value
        )

        initAsRootForSnackbar(mBinding.root)
    }

    private fun initFunctionality() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && loader is Animatable2) {
            object : Animatable2.AnimationCallback() {
                override fun onAnimationEnd(drawable: Drawable?) {
                    if (animationInProgress)
                        animationStart()
                }
            }.also { (loader as Animatable2).registerAnimationCallback(it) }
        } else if (loader is Animatable2Compat) {
            object : Animatable2Compat.AnimationCallback() {
                override fun onAnimationEnd(drawable: Drawable?) {
                    if (animationInProgress)
                        animationStart()
                }
            }.also {
                (loader as Animatable2Compat).registerAnimationCallback(it)
            }
        }

        viewModels<ErrorViewModel>().value.commonErrorMutableLiveData.observe(this,
            { showError(it) })
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mBinding = ActivitySplashBinding.inflate(layoutInflater)
        setContentView(mBinding.root)

        ApplicationData.initStorage(Storage.getInstance(this))
        ApplicationData.loadFromStorage()
    }

    override fun onStart() {
        super.onStart()
        initFields()
        initFunctionality()

        if (ApplicationData.access_token.isBlank()) {
            //user not logged in before
            replaceActivity(AuthActivity())
        } else {
            loadUserInfo()
        }

        animationStart()
    }

    override fun onStop() {
        super.onStop()
        animationStop()
    }

    fun cantLoadUserInfo() {
        animationStop()

        SnackBarHelper.APPLICATION_ROOT_VIEW?.let {
            SnackBarHelper.build(it, getString(R.string.error_cant_load_user_info))
                .withLength(Snackbar.LENGTH_INDEFINITE)
                .extendedWith(this)
                .withOnCloseAction {
                    this.finish()
                }
                .withMessage(getString(R.string.error_cant_load_user_info_before_close_app))
                .withTitle(getString(R.string.error_default_text))
                .buildExtend()
                .build()
                .show()
        }
    }

    fun loadUserInfoLastTry() {
        userRepository.currentUserData()
            .withErrorCollector {
                if (it.errorCode == ErrorCodes.NETWORK_TIMEOUT)
                    cantLoadUserInfo()
                else
                    ErrorViewModel.getGlobalInstance()?.error(it)
            }
            .withOnSuccessCallback {
                animationStop()
                replaceActivity(MainActivity())
            }
            .run()
    }

    fun loadUserInfoBadTry() {
        if (--numberUpdateTimes > 0 && !this.isDestroyed)
            loadUserInfo()
        else {
            loadUserInfoLastTry()
        }
    }


    fun loadUserInfo() {
        userRepository.currentUserData()
            .withOnSuccessCallback {
                animationStop()
                replaceActivity(MainActivity())
            }
            .withErrorCollector {
                if (it.errorCode == ErrorCodes.NETWORK_TIMEOUT)
                    loadUserInfoBadTry()
                else
                    replaceActivity(AuthActivity())
            }
            .run()
    }

    override fun onCreateView(name: String, context: Context, attrs: AttributeSet): View? {
        return super.onCreateView(name, context, attrs)
    }
}