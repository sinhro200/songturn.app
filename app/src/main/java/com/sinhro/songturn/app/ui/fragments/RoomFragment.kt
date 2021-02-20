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
import com.sinhro.songturn.app.view_models.UserInfoViewModel
import com.sinhro.songturn.rest.ErrorCodes
import com.sinhro.songturn.rest.model.SongInfoVoted
import kotlinx.android.synthetic.main.fragment_room.*
import java.util.*

class RoomFragment : Fragment(R.layout.fragment_room) {

    private val roomViewModel: RoomViewModel by activityViewModels()
    private val playlistViewModel: PlaylistViewModel by activityViewModels()
    private val userInfoViewModel: UserInfoViewModel by activityViewModels()
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
            playlistViewModel.voteForSong(it.id, 1)
                .withOnSuccessCallback {
                    playlistViewModel.updatePlaylistSongsVoted()
                        .run()
                }
                .run()
        }
        songsRecyclerViewAdapter.onSongArrowDownClickCallback = {
            playlistViewModel.voteForSong(it.id, -1)
                .withOnSuccessCallback {
                    playlistViewModel.updatePlaylistSongsVoted()
                        .run()
                }
                .run()
        }
        songsRecyclerViewAdapter.onSongClickCallback = { songInfo ->


            //TODO check can user listen or not
//            val listenerId =
//                roomViewModel.playlistsInRoomLiveData.value?.find { it.title == ApplicationData.playlist_title }?.listenerId
//            val canListen: Boolean =
//                (roomViewModel.roomLiveData.value?.roomSettings?.anyCanListen ?: false) ||
//                        (listenerId != null && userInfoViewModel.userLiveData.value?.id == listenerId)
//            if (canListen) {
            if (ApplicationData.isInListenMode) {
                if (ApplicationData.songCurrent != null && ApplicationData.songCurrent == songInfo)
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
        //TimeZone.getDefault().getDisplayName(Locale.getDefault())
        val d = Date()
        val g = GregorianCalendar.getInstance(Locale.getDefault())
        g.time = d
        g.timeZone = TimeZone.getDefault()
        g.add(GregorianCalendar.MINUTE, 30)

        songsRecyclerViewAdapter = AppSongsRecyclerViewAdapter(
            requireContext(), mutableListOf()
        )
    }

    fun initFields() {
        fragment_room_songs_recycler_view.adapter = songsRecyclerViewAdapter
    }

    fun observeViewModels() {
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

    fun initFunctionality() {
        setHasOptionsMenu(true)
        observeViewModels()
        roomViewModel.updateFullRoomInfo()
            .withOnSuccessCallback {
                /*playlistViewModel.updatePlaylistSongs()
                    .run()*/
                playlistViewModel.updatePlaylistSongsVoted()
                    .run()
            }
            .withOnErrorCallback {
                if (it.errorCode == ErrorCodes.ROOM_NOT_FOUND || it.errorCode == ErrorCodes.USER_NOT_IN_ROOM) {
                    ApplicationData.room_token = ""
                    ApplicationData.playlist_title = ""
                    replaceFragment(EnterCreateRoomFragment())
                }
            }
            .run()

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
            R.id.room_menu_listen -> { changeListenMode() }
        }
        return true
    }

    private fun changeListenMode() {
        ApplicationData.isInListenMode = !ApplicationData.isInListenMode
        if (ApplicationData.isInListenMode)
            showToast("Вы в режиме прослушивания")
        else
            showToast("Вы вышли из режима прослушивания")
    }

    override fun onResume() {
        super.onResume()
        hideKeyboard()
    }
}