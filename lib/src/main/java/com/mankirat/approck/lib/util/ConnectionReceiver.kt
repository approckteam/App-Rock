package com.mankirat.approck.lib.util

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.mankirat.approck.lib.Utils

class ConnectionReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        Utils.hasInternetConnection(context) {
            internetListener?.onNetworkChange(it)
        }
    }

    interface ReceiverListener {
        fun onNetworkChange(isConnected: Boolean)
    }

    companion object {
        var internetListener: ReceiverListener? = null
    }
}