package com.sinhro.songturn.app.ui.activities

import android.app.AlertDialog
import android.content.DialogInterface
import android.os.Bundle
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.snackbar.Snackbar
import com.sinhro.songturn.app.R
import com.sinhro.songturn.app.databinding.ActivityOrderSongBinding
import com.sinhro.songturn.app.model.ApplicationData
import com.sinhro.songturn.app.utils.SnackBarHelper
import com.sinhro.songturn.app.utils.toPrettyString
import com.sinhro.songturn.app.view_models.PlaylistViewModel
import com.sinhro.songturn.rest.core.CommonError
import kotlinx.android.synthetic.main.activity_order_song.*

class OrderSongActivity : AppCompatActivity() {
    private val playlistViewModel: PlaylistViewModel by viewModels()

    private lateinit var mBinding: ActivityOrderSongBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mBinding = ActivityOrderSongBinding.inflate(layoutInflater)
        setContentView(mBinding.root)
    }

    override fun onStart() {
        super.onStart()

        if (ApplicationData.access_token.isBlank() || ApplicationData.roomInfo == null)
            userNotInRoomErrorDialog()
                .show()
        else {
            activity_order_song_room_title.text = ApplicationData.roomInfo?.title

            activity_order_song_button_order.setOnClickListener {
                orderSong()
            }
        }
    }

    private fun userNotInRoomErrorDialog(): AlertDialog {
        return AlertDialog.Builder(this)
            .setNegativeButton(
                getString(R.string.activity_order_song_dialog_exit)
            ) { dialogInterface: DialogInterface?, i: Int ->
                dialogInterface?.cancel()
                finish()
            }
            .setMessage(getString(R.string.activity_order_song_dialog_text))
            .setTitle(getString(R.string.activity_order_song_dialog_title))
            .create()
    }

    fun orderSong() {
        intent.clipData?.getItemAt(0)?.let {
            val songLink = it.text.toString().trim()
            playlistViewModel.orderSong(songLink)
                .withOnSuccessCallback {
                    successToast()
                        .show()
                    this.finish()
                }
                .withErrorCollector {
                    errorSnackbar(it)
                        .show()
                }
                .run()
        }
    }

    private fun successToast(): Toast {
        return Toast.makeText(
            this,
            getString(R.string.activity_order_song_succesfully_ordered),
            Toast.LENGTH_SHORT
        )
    }

    private fun errorSnackbar(error: CommonError): Snackbar {
        return SnackBarHelper.build(
            mBinding.root,
            getString(R.string.activity_order_song_error_cant_order_song)
        )
            .withLength(Snackbar.LENGTH_INDEFINITE)
            .extendedWith(this)
            .withOnCloseAction {
                this.finish()
            }
            .withMessage(error.toPrettyString())
            .withTitle(getString(R.string.error_default_text))
            .buildExtend()
            .build()
    }
}