package com.sinhro.songturn.app.media_player

import android.app.Service
import android.content.Context
import android.telephony.PhoneStateListener
import android.telephony.TelephonyManager

class AppHandlerIncomingCalls {
    //Handle incoming phone calls
    public var ongoingCall = false
        private set

    private var phoneStateListener: PhoneStateListener? = null
    private var telephonyManager: TelephonyManager? = null

    fun init(context: Context, onIncomingCall: () -> Unit, onHangup: () -> Unit) {
        telephonyManager = context.getSystemService(Service.TELEPHONY_SERVICE) as TelephonyManager

        phoneStateListener = object : PhoneStateListener() {
            override fun onCallStateChanged(state: Int, incomingNumber: String) {
                when (state) {
                    TelephonyManager.CALL_STATE_OFFHOOK,
                    TelephonyManager.CALL_STATE_RINGING -> {
                        onIncomingCall.invoke()
                        ongoingCall = true
                    }

                    TelephonyManager.CALL_STATE_IDLE -> {
                        // Phone idle. Start playing.
                        if (ongoingCall){
                            ongoingCall = false
                            onHangup.invoke()
                        }
                    }

                }
            }
        }

        // Register the listener with the telephony manager
        // Listen for changes to the device call state.
        telephonyManager?.listen(
            phoneStateListener,
            PhoneStateListener.LISTEN_CALL_STATE
        )
    }

    fun disable(){
        //Disable the PhoneStateListener
        if (phoneStateListener != null) {
            telephonyManager?.listen(phoneStateListener, PhoneStateListener.LISTEN_NONE);
        }
    }
}