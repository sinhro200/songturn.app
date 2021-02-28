package com.sinhro.songturn.app.ui.fragments

import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.sinhro.songturn.app.R
import com.sinhro.songturn.app.model.ApplicationData
import com.sinhro.songturn.app.ui.activities.MainActivity
import com.sinhro.songturn.app.ui.objects.AppSongsRecyclerViewAdapter
import com.sinhro.songturn.app.utils.hideKeyboard
import com.sinhro.songturn.app.utils.replaceFragment
import com.sinhro.songturn.app.utils.showToast
import com.sinhro.songturn.app.view_models.PlaylistViewModel
import com.sinhro.songturn.app.view_models.RoomViewModel
import com.sinhro.songturn.rest.ErrorCodes
import kotlinx.android.synthetic.main.fragment_room.*
import java.util.*


class RoomFragment : Fragment(R.layout.fragment_room) {

    private val roomViewModel: RoomViewModel by activityViewModels()
    private val playlistViewModel: PlaylistViewModel by activityViewModels()
    private lateinit var songsRecyclerViewAdapter: AppSongsRecyclerViewAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        createRecycleView()
        initRecycleViewCallbacks()
    }

    override fun onStart() {
        super.onStart()
        initFields()

        initFunctionality()
    }

    private fun initRecycleViewCallbacks() {
        songsRecyclerViewAdapter.onSongArrowUpClickCallback = {
            playlistViewModel.voteForSong(it.songInfo.id, if (it.action < 0) 0 else 1)
                .withOnSuccessCallback {
                    playlistViewModel.updatePlaylistSongsVoted()
                        .run()
                }
                .run()
        }
        songsRecyclerViewAdapter.onSongArrowDownClickCallback = {
            playlistViewModel.voteForSong(it.songInfo.id, if (it.action > 0) 0 else -1)
                .withOnSuccessCallback {
                    playlistViewModel.updatePlaylistSongsVoted()
                        .run()
                }
                .run()
        }
        songsRecyclerViewAdapter.onSongClickCallback = { songInfo ->
            if (ApplicationData.isInListenMode) {
                if (ApplicationData.songCurrent != null &&
                    ApplicationData.songCurrent == songInfo
                )
                    (activity as MainActivity).pauseResumeAudio()
                else {
                    playlistViewModel.setCurrentPlayingSong(songInfo.id)
                        .withOnSuccessCallback {
                            ApplicationData.songCurrent = songInfo
                            (activity as MainActivity).playAudio()
                            playlistViewModel.updatePlaylistSongsVoted()
                                .run()
                        }
                        .run()
                }
            } else {
                showToast(getString(R.string.listening_mode_off))
            }
        }
    }

    private fun createRecycleView() {
        songsRecyclerViewAdapter = AppSongsRecyclerViewAdapter(
            requireContext(), mutableListOf()
        )
    }

    private fun initFields() {
        fragment_room_songs_recycler_view.adapter = songsRecyclerViewAdapter
    }

    private fun observeViewModels() {
        roomViewModel.roomLiveData.observe(this, {
            activity?.title = it.title
        })
        /*playlistViewModel.currentListeningSongMutableLiveData.observe(
            this, {
                songsRecyclerViewAdapter.setListeningSong(it?.id)
            }
        )*/
        playlistViewModel.currentListeningSongMutableLiveData.observe(
            this, {
                songsRecyclerViewAdapter.setListeningSong(it?.id)
            }
        )
        /*playlistViewModel.playlistSongsMutableLiveData.observe(
            this, { songs ->
                if (songs.songsInQueue.isEmpty() && songs.currentSong == null)
                    fragment_room_playlist_empty_tooltip.visibility = View.VISIBLE
                else
                    fragment_room_playlist_empty_tooltip.visibility = View.INVISIBLE

                val listSongs = songs.songsInQueue.toMutableList()
    //                ApplicationData.songCurrent?.let {
    //                    listSongs.add(0, it)
    //                }
                songs.currentSong?.let {
                    listSongs.add(0, it)
                }

                songsRecyclerViewAdapter.setSongs(
                    listSongs
                )
            }
        )*/
        playlistViewModel.playlistSongsVotedMutableLiveData.observe(this, { songs ->
            if (songs.songsInQueue.isEmpty() && songs.currentSong == null)
                fragment_room_playlist_empty_tooltip.visibility = View.VISIBLE
            else
                fragment_room_playlist_empty_tooltip.visibility = View.INVISIBLE

            val listSongs = songs.songsInQueue.toMutableList()
//                ApplicationData.songCurrent?.let {
//                    listSongs.add(0, it)
//                }
            songs.currentSong?.let {
                listSongs.add(0, it)
            }

            songsRecyclerViewAdapter.setSongsVoted(
                listSongs
            )
        })
    }


    private val updateFullRoomInfoRequest by lazy {
        roomViewModel.updateFullRoomInfo()
            .withOnSuccessCallback {
                playlistViewModel.updatePlaylistSongsVoted()
                    .run()
            }
            .withOnErrorCallback {
                if (it.errorCode == ErrorCodes.ROOM_NOT_FOUND || it.errorCode == ErrorCodes.USER_NOT_IN_ROOM) {
                    roomViewModel.roomLiveData.postValue(null)
                    ApplicationData.playlistInfo = null
                    replaceFragment(EnterCreateRoomFragment())
                }
            }
    }

    fun initFunctionality() {
        setHasOptionsMenu(true)
        observeViewModels()
        updateFullRoomInfoRequest.run()

        fragment_room_update_button.setOnClickListener {
            /*playlistViewModel.updatePlaylistSongs()
                .run()*/
            playlistViewModel.updatePlaylistSongsVoted()
                .run()
        }

        ApplicationData.onCompleteInPlayerServiceCallback =
            fun(): Boolean {
                Log.d("OnCompleteCallback", "started")
                try {
                    playlistViewModel.updatePlaylistSongsVoted()
                        .withOnSuccessCallback {
                            if (ApplicationData.songsInQueue.isNotEmpty()) {
                                val curSong = ApplicationData.songsInQueue[0]
                                val songId = curSong.id
                                ApplicationData.songCurrent = curSong
                                (activity as MainActivity).playAudio()
                                playlistViewModel.setCurrentPlayingSong(songId)
                                    .run()
                            }
                        }
                        .run()
                    return true
                } catch (e: Exception) {
                    return false
                }
            }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.room_menu, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.room_menu_info -> replaceFragment(RoomInfoFragment())
            R.id.room_menu_listen -> {
                changeListenMode()
            }
        }
        return true
    }

    override fun onPrepareOptionsMenu(menu: Menu) {
        val item = menu.findItem(R.id.room_menu_listen)
        if (ApplicationData.isInListenMode) {
            item.title = getString(R.string.fragment_room_menu_listen_finish)
        } else {
            item.title = getString(R.string.fragment_room_menu_listen_wanna)
        }
        super.onPrepareOptionsMenu(menu)
    }

    private val stopListenPlaylistRequest by lazy {
        playlistViewModel.stopListen()
            .withOnSuccessCallback {
                ApplicationData.isInListenMode =
                    it.playlist.listenerId == ApplicationData.userInfo?.id
                if (!ApplicationData.isInListenMode) {
                    showToast(getString(R.string.user_out_of_listen_mode))
                    (activity as MainActivity).closeAudio()
                }
                else
                    showToast(getString(R.string.cant_out_of_listen_mode))
            }
            .withOnErrorCallback {
                showToast(getString(R.string.cant_out_of_listen_mode))
            }
    }

    private val wannaListenPlaylistRequest by lazy {
        playlistViewModel.wannaListen()
            .withOnSuccessCallback {
                ApplicationData.isInListenMode =
                    ApplicationData.userInfo?.id != null &&
                            it.playlist.listenerId == ApplicationData.userInfo?.id
                if (ApplicationData.isInListenMode)
                    showToast(getString(R.string.user_in_listen_mode))
                else
                    showToast(getString(R.string.cant_listen))
            }
            .withOnErrorCallback {
                if (it.errorCode == ErrorCodes.PLAYLIST_ALREADY_HAS_LISTENER)
                    showToast(getString(R.string.playlist_already_has_listener))
                else
                    showToast(getString(R.string.cant_listen))
            }
    }

    private fun changeListenMode() {
        if (ApplicationData.isInListenMode)
            stopListenPlaylistRequest.run()
        else
            wannaListenPlaylistRequest.run()

    }

    override fun onResume() {
        super.onResume()
        hideKeyboard()
    }
}