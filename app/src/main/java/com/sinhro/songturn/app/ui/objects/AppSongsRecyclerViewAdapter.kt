package com.sinhro.songturn.app.ui.objects

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.sinhro.songturn.app.R
import com.sinhro.songturn.app.model.ApplicationData
import com.sinhro.songturn.rest.model.SongInfo
import com.sinhro.songturn.rest.model.SongInfoVoted

class AppSongsRecyclerViewAdapter(
    context: Context,
    private val songs: MutableList<SongInfo> = mutableListOf(),
    private val songsVoted: MutableList<SongInfoVoted> = mutableListOf()
) : RecyclerView.Adapter<AppSongsRecyclerViewAdapter.CustomViewHolder>() {
    private val layoutInflater: LayoutInflater = LayoutInflater.from(context)

    private var listeningSongId: Int? = null
    private var chosenSongId: Int? = null
    private var playingSongId: Int? = null

    var onSongArrowUpClickCallback: ((SongInfo) -> Unit)? = null
    var onSongArrowDownClickCallback: ((SongInfo) -> Unit)? = null
    var onSongClickCallback: ((SongInfo) -> Unit)? = null

    fun notifyChangedBySongId(songId: Int?) {
        songId?.let { sid ->
            notifyItemChanged(
                //  ###     may return -1
                songs.indexOfFirst { it.id == sid }
            )
        }
    }

    fun setListeningSong(songId: Int?) {
        val prevListenSongId = listeningSongId
        this.listeningSongId = songId


        notifyChangedBySongId(prevListenSongId)
        notifyChangedBySongId(listeningSongId)
    }

    fun setChosenSong(songId: Int?) {
        val prevChosenSongId = chosenSongId
        this.chosenSongId = songId


        notifyChangedBySongId(prevChosenSongId)
        notifyChangedBySongId(chosenSongId)
    }

    fun setPlayingSong(songId: Int?) {
        val prevPlayingSongId = playingSongId
        this.playingSongId = songId


        notifyChangedBySongId(prevPlayingSongId)
        notifyChangedBySongId(playingSongId)
    }

    override fun getItemId(position: Int): Long {
        return songs[position].id.toLong()
    }


    fun clear() {
        songs.clear()
        notifyDataSetChanged()
    }

    fun setSongs(si: List<SongInfo>) {
        songs.clear()
        songs.addAll(si)
        notifyDataSetChanged()
    }

    fun setSongsVoted(siv: List<SongInfoVoted>) {
        songsVoted.clear()
        songsVoted.addAll(siv)
        notifyDataSetChanged()
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CustomViewHolder {
        val v = layoutInflater.inflate(
            R.layout.fragment_room_song_view_holder,
            parent,
            false
        )
        return CustomViewHolder(v)
    }

    override fun onBindViewHolder(holderCustom: CustomViewHolder, position: Int) {
        val songInfoVoted = if (songsVoted.isEmpty())
            SongInfoVoted(songs[position], 0)
        else
            songsVoted[position]
        bindFromSongVoted(holderCustom, position, songInfoVoted)

        /*holderCustom.numberTextView.text = (position + 1).toString()
        holderCustom.titleTextView.text = songs[position].title
        holderCustom.artistTextView.text = songs[position].artist
        holderCustom.durationTextView.text =
            "${(songs[position].durationSeconds / 60)}:${(songs[position].durationSeconds % 60)}"
        holderCustom.ownerTextView.text =
            if (songs[position].userId == null) ""
            else ApplicationData.usersInRoom.find { it.id == songs[position].userId }?.nickname
                ?: ""

        holderCustom.ratingUpButton.setOnClickListener {
            onSongArrowUpClickCallback?.invoke(songs[position])
        }
        holderCustom.ratingDownButton.setOnClickListener {
            onSongArrowDownClickCallback?.invoke(songs[position])
        }
        holderCustom.customSongView.setOnClickListener {
            onSongClickCallback?.invoke(songs[position])
        }

        holderCustom.setDefault()

        listeningSongId?.let {
            if (songs[position].id == it)
                holderCustom.setSongListening()
        }*/
    }

    private fun bindFromSongVoted(
        holderCustom: CustomViewHolder,
        position: Int,
        songInfoVoted: SongInfoVoted
    ) {

        holderCustom.numberTextView.text = (position + 1).toString()
        holderCustom.titleTextView.text = songInfoVoted.songInfo.title
        holderCustom.artistTextView.text = songInfoVoted.songInfo.artist
        holderCustom.durationTextView.text =
            "${(songInfoVoted.songInfo.durationSeconds / 60)}:${(songInfoVoted.songInfo.durationSeconds % 60)}"
        holderCustom.ownerTextView.text = if (
            ApplicationData.roomSettings.songOwnersVisible && songInfoVoted.songInfo.userId != null
        )
            ApplicationData.usersInRoom.find { it.id == songInfoVoted.songInfo.userId }?.nickname
                ?: ""
        else ""

        if (ApplicationData.roomSettings.allowVotes) {
            holderCustom.arrowsContainer.visibility = View.VISIBLE
            holderCustom.ratingUpButton.isSelected = songInfoVoted.action > 0
            holderCustom.ratingDownButton.isSelected = songInfoVoted.action < 0
        } else {
            holderCustom.arrowsContainer.visibility = View.INVISIBLE
        }

        holderCustom.ratingUpButton.setOnClickListener {
            onSongArrowUpClickCallback?.invoke(songInfoVoted.songInfo)
        }
        holderCustom.ratingDownButton.setOnClickListener {
            onSongArrowDownClickCallback?.invoke(songInfoVoted.songInfo)
        }
        holderCustom.customSongView.setOnClickListener {
            onSongClickCallback?.invoke(songInfoVoted.songInfo)
        }

        holderCustom.setDefault()

        listeningSongId?.let {
            if (songInfoVoted.songInfo.id == it)
                holderCustom.setSongListening()
        }
    }


    override fun getItemCount(): Int {
        return songsVoted.count()
    }

    inner class CustomViewHolder : RecyclerView.ViewHolder {

        constructor(view: View) : super(view) {
            this.customSongView = view as SongItemView
            this.numberTextView =
                customSongView.findViewById(R.id.fragment_room_song_number_textView)
            this.titleTextView =
                customSongView.findViewById(R.id.fragment_room_song_view_title_textView)
            this.artistTextView =
                customSongView.findViewById(R.id.fragment_room_song_view_artist_textView)
            this.durationTextView =
                customSongView.findViewById(R.id.fragment_room_song_view_duration_textView)
            this.ownerTextView =
                customSongView.findViewById(R.id.fragment_room_song_view_owner_textView)
            this.ratingUpButton =
                customSongView.findViewById(R.id.fragment_room_song_view_arrow_up_imageView)
            this.ratingDownButton =
                customSongView.findViewById(R.id.fragment_room_song_view_arrow_down_imageView)
            this.arrowsContainer =
                customSongView.findViewById(R.id.fragment_room_song_view_arrows_container)
            initListeners()
        }

        private fun initListeners() {
            ratingUpButton.setOnClickListener {
                songs[adapterPosition]
            }
        }

        val customSongView: SongItemView

        val numberTextView: TextView
        val titleTextView: TextView
        val artistTextView: TextView
        val durationTextView: TextView
        val ownerTextView: TextView
        val ratingUpButton: View
        val ratingDownButton: View
        val arrowsContainer: View

        fun setSongListening() {
            customSongView.setSongListening()
        }

        fun setSongPlaying() {
            customSongView.setSongPlaying()
        }

        fun setSongError() {
            customSongView.setSongError()
        }

        fun setSongSelected() {
            customSongView.setSongError()
        }

        fun setDefault() {
            customSongView.setSongError(false)
            customSongView.setSongPlaying(false)
            customSongView.setSongListening(false)
            customSongView.setSongSelected(false)
        }
    }
}