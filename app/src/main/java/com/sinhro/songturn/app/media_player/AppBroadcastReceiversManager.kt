package com.sinhro.songturn.app.media_player

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter

class AppBroadcastReceiversManager {

    private val receiversMap = mutableMapOf<IntentFilter, BroadcastReceiver>()

    fun addReceiver(
        intentFilter: String,
        onReceive: (context: Context?, intent: Intent?) -> Unit
    ): AppBroadcastReceiversManager {
        return addReceiver(
            IntentFilter(intentFilter),
            object : BroadcastReceiver() {
                override fun onReceive(context: Context?, intent: Intent?) {
                    onReceive.invoke(context, intent)
                }
            }
        )
    }

    fun addReceiver(
        intentFilter: IntentFilter,
        onReceive: (context: Context?, intent: Intent?) -> Unit
    ): AppBroadcastReceiversManager {
        return addReceiver(
            intentFilter,
            object : BroadcastReceiver() {
                override fun onReceive(context: Context?, intent: Intent?) {
                    onReceive.invoke(context, intent)
                }
            }
        )
    }

    fun addReceiver(intentFilter: String,bcr: BroadcastReceiver): AppBroadcastReceiversManager {
        return addReceiver(IntentFilter(intentFilter),bcr)
    }

    fun addReceiver( intentFilter: IntentFilter,bcr: BroadcastReceiver): AppBroadcastReceiversManager {
        receiversMap.put(intentFilter, bcr)
        return this
    }

    fun registerReceivers(context: Context) {
        receiversMap.forEach {
            context.registerReceiver(it.value, it.key)
        }
    }

    fun unregisterReceivers(context: Context) {
        receiversMap.forEach {
            context.registerReceiver(it.value, it.key)
        }
    }
}