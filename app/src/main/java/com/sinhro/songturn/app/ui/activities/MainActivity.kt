package com.sinhro.songturn.app.ui.activities

import android.content.*
import android.os.Bundle
import android.os.IBinder
import android.os.PersistableBundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import com.sinhro.musicord.storage.Storage
import com.sinhro.songturn.app.R
import com.sinhro.songturn.app.databinding.ActivityMainBinding
import com.sinhro.songturn.app.media_player.Broadcast_PAUSE_RESUME_AUDIO
import com.sinhro.songturn.app.media_player.Broadcast_PLAY_NEW_AUDIO
import com.sinhro.songturn.app.media_player.MediaPlayerServiceV3
import com.sinhro.songturn.app.model.ApplicationData
import com.sinhro.songturn.app.ui.fragments.EnterCreateRoomFragment
import com.sinhro.songturn.app.ui.fragments.EnterRoomFragment
import com.sinhro.songturn.app.ui.fragments.RoomFragment
import com.sinhro.songturn.app.ui.objects.AppDrawer
import com.sinhro.songturn.app.utils.*
import com.sinhro.songturn.app.view_models.ErrorViewModel
import com.sinhro.songturn.app.view_models.PlaylistViewModel
import com.sinhro.songturn.app.view_models.RoomViewModel
import com.sinhro.songturn.app.view_models.UserInfoViewModel


class MainActivity : AppCompatActivity() {

    private lateinit var mBinding: ActivityMainBinding
    lateinit var mAppDrawer: AppDrawer

    private lateinit var mToolbar: Toolbar

    private var player: MediaPlayerServiceV3? = null
    var serviceBound = false

    private val playlistViewModel: PlaylistViewModel by viewModels()
    private val userInfoViewModel: UserInfoViewModel by viewModels()
    private val roomViewModel: RoomViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mBinding = ActivityMainBinding.inflate(layoutInflater)

        setContentView(mBinding.root)
        ApplicationData.initStorage(Storage.getInstance(this))
    }

    override fun onStart() {
        super.onStart()
        initFields()
        initFunctionality()
    }

    private fun initFunctionality() {
        if (ApplicationData.access_token.isBlank()) {
            replaceActivity(SplashActivity())
        } else {
            initAsRootForSnackbar(mBinding.root)
            setSupportActionBar(mToolbar)
            mAppDrawer.create()
            viewModels<ErrorViewModel>().value.commonErrorMutableLiveData.observe(this,
                { showError(it) })

            roomViewModel.usersInRoomLiveData.observe(this,{
                ApplicationData.usersInRoom = it
            })



            if (ApplicationData.room_token.isBlank())
                replaceFragment(EnterCreateRoomFragment())
            else
                replaceFragment(RoomFragment())
        }
    }

    private fun initFields() {
        mToolbar = mBinding.mainToolbar
        mAppDrawer = AppDrawer(this, mToolbar)
        Storage.initGlobal(this)

        ErrorViewModel.initGlobal(
            viewModels<ErrorViewModel>().value
        )
    }

    fun logout() {
        ApplicationData.access_token = ""
        ApplicationData.room_token = ""
        ApplicationData.playlist_title = ""
        ApplicationData.saveToStorage()
        userInfoViewModel.logoutUser()
            .withOnEndLoad{
                replaceActivity(SplashActivity())
            }
            .run()
    }

    override fun onSaveInstanceState(outState: Bundle, outPersistentState: PersistableBundle) {
        super.onSaveInstanceState(outState, outPersistentState)
        outState.putBoolean("serviceStatus", serviceBound);
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        serviceBound = savedInstanceState.getBoolean("serviceStatus");
    }

    //Binding this Client to the AudioPlayer Service
    private val serviceConnection: ServiceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName, service: IBinder) {
            // We've bound to LocalService, cast the IBinder and get LocalService instance
            val binder = service as MediaPlayerServiceV3.LocalBinder
            player = binder.service
            serviceBound = true
        }

        override fun onServiceDisconnected(name: ComponentName) {
            serviceBound = false
        }
    }

    fun playAudio() {
        ApplicationData.songCurrent?.id?.let {
            playlistViewModel.setCurrentPlayingSong(it)
                .withOnSuccessCallback {
                    playlistViewModel.updateCurrentPlayingSong()
                        .run()
                }
                .run()
        }
        if (ApplicationData.songCurrent == null || ApplicationData.songCurrent!!.link.isNullOrBlank())
            SnackBarHelper.build(mBinding.root, getString(R.string.unable_to_play_audio))
                .extendedWith(this)
                .withMessage(getString(R.string.cant_find_song_link))
                .buildExtend()
                .build()
                .show()
        else
        //Check is service is active
            if (!serviceBound) {

                val playerIntent = Intent(this, MediaPlayerServiceV3::class.java)
                startService(playerIntent)
                bindService(playerIntent, serviceConnection, Context.BIND_AUTO_CREATE)
            } else {
                //Service is active
                //Send a broadcast to the service -> PLAY_NEW_AUDIO
                val broadcastIntent = Intent(Broadcast_PLAY_NEW_AUDIO)
                sendBroadcast(broadcastIntent)
            }
    }

    fun pauseResumeAudio() {
        if (!serviceBound) {
            val playerIntent = Intent(this, MediaPlayerServiceV3::class.java)
            startService(playerIntent)
            bindService(playerIntent, serviceConnection, Context.BIND_AUTO_CREATE)
        } else {
            //Service is active
            //Send a broadcast to the service -> PLAY_NEW_AUDIO
            val broadcastIntent = Intent(Broadcast_PAUSE_RESUME_AUDIO)
            sendBroadcast(broadcastIntent)
        }
    }
}