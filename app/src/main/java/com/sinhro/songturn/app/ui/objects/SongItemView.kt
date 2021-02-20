package com.sinhro.songturn.app.ui.objects

import android.content.Context
import android.util.AttributeSet
import androidx.constraintlayout.widget.ConstraintLayout
import com.sinhro.songturn.app.R

class SongItemView : ConstraintLayout {
    private val STATE_SONG_SELECTED = intArrayOf(R.attr.song_state_selected)
    private val STATE_SONG_PLAYING = intArrayOf(R.attr.song_state_playing)
    private val STATE_SONG_ERROR = intArrayOf(R.attr.song_state_error)
    private val STATE_SONG_LISTENING = intArrayOf(R.attr.song_state_listening)

    private var song_selected = false
    private var song_playing = false
    private var song_listening = false
    private var song_error = false

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)
    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    )

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int, defStyleRes: Int) : super(
        context,
        attrs,
        defStyleAttr,
        defStyleRes
    )

    override fun onCreateDrawableState(extraSpace: Int): IntArray {
        if (song_listening){
            val drawableState = super.onCreateDrawableState(extraSpace + 1)
            mergeDrawableStates(drawableState, STATE_SONG_LISTENING)
            return drawableState
        }
        if (song_error) {
            val drawableState = super.onCreateDrawableState(extraSpace + 1)
            mergeDrawableStates(drawableState, STATE_SONG_ERROR)
            return drawableState
        }
        if (song_selected) {
            val drawableState: IntArray
            if (song_playing) {
                drawableState = super.onCreateDrawableState(extraSpace + 2)

                mergeDrawableStates(drawableState, STATE_SONG_PLAYING)
                mergeDrawableStates(drawableState, STATE_SONG_SELECTED)

            } else {
                drawableState = super.onCreateDrawableState(extraSpace + 1)
                mergeDrawableStates(drawableState, STATE_SONG_SELECTED)
            }
            return drawableState
        }
        return super.onCreateDrawableState(extraSpace)
    }

    public fun setSongListening(isListening: Boolean = true) {
        this.song_listening = isListening
        refreshDrawableState()
    }

    public fun setSongSelected(isSelected: Boolean = true) {
        if (isSelected)
            song_error = false
        this.song_selected = isSelected
        refreshDrawableState()
    }

    public fun setSongPlaying(isPlaying: Boolean = true) {
        this.song_playing = isPlaying
        refreshDrawableState()
    }

    public fun setSongError(isError: Boolean = true) {
        if (isError) {
            this.song_selected = false
            this.song_playing = false
        }
        this.song_error = isError
        refreshDrawableState()
    }
}