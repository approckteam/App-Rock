package com.mankirat.approck.lib

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.Uri
import android.os.Build
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import com.google.gson.Gson
import com.mankirat.approck.lib.MyConstants.FirebaseEvent
import com.mankirat.approck.lib.databinding.DialogInternetBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.IOException
import java.net.InetSocketAddress
import java.net.Socket

object Utils {
    var firebaseEventCallback: ((eventName: String, bundle: Bundle?) -> Unit)? = null
    var purchaseCallback: ((status: Boolean) -> Unit)? = null
    var buySubscription: ((id: String) -> Unit)? = null

    fun loadError(type: AdType, statusCode: Int, msg: String) {
        val name = when (type) {
            AdType.INTERSTITIAL -> FirebaseEvent.LOAD_INTERSTITIAL_ERROR
            AdType.INTERSTITIAL_SPLASH -> FirebaseEvent.LOAD_INTERSTITIAL_SPLASH_ERROR
            AdType.NATIVE -> FirebaseEvent.LOAD_NATIVE_ERROR
            AdType.REWARD -> FirebaseEvent.LOAD_REWARD_ERROR
            else -> ""
        }
        val bundle = Bundle()
        bundle.putInt("loadErrorCode", statusCode)
        bundle.putString("loadErrorMsg", msg)
        firebaseEventCallback?.invoke(name, bundle)
    }

    fun loadSuccess(type: AdType) {
        val name = when (type) {
            AdType.INTERSTITIAL -> FirebaseEvent.LOAD_INTERSTITIAL_SUCCESS
            AdType.INTERSTITIAL_SPLASH -> FirebaseEvent.LOAD_INTERSTITIAL_SPLASH_SUCCESS
            AdType.NATIVE -> FirebaseEvent.LOAD_NATIVE_SUCCESS
            AdType.REWARD -> FirebaseEvent.LOAD_REWARD_SUCCESS
            else -> ""
        }
        firebaseEventCallback?.invoke(name, null)
    }

    fun showError(type: AdType, statusCode: Int, msg: String) {
        val name = when (type) {
            AdType.INTERSTITIAL -> FirebaseEvent.SHOW_INTERSTITIAL_ERROR
            AdType.INTERSTITIAL_SPLASH -> FirebaseEvent.SHOW_INTERSTITIAL_SPLASH_ERROR
            AdType.BANNER -> FirebaseEvent.SHOW_BANNER_ERROR
            else -> ""
        }
        val bundle = Bundle()
        bundle.putInt("loadErrorCode", statusCode)
        bundle.putString("loadErrorMsg", msg)
        firebaseEventCallback?.invoke(name, bundle)
    }

    fun showSuccess(type: AdType) {
        val name = when (type) {
            AdType.INTERSTITIAL -> FirebaseEvent.SHOW_INTERSTITIAL_SUCCESS
            AdType.INTERSTITIAL_SPLASH -> FirebaseEvent.SHOW_INTERSTITIAL_SPLASH_SUCCESS
            AdType.BANNER -> FirebaseEvent.SHOW_BANNER_SUCCESS
            else -> ""
        }
        firebaseEventCallback?.invoke(name, null)
    }

    fun putObject(prefs: SharedPreferences?, key: String, obj: Any) {
        val gson = Gson()
        prefs?.edit()?.putString(key, gson.toJson(obj))?.apply()
    }

    fun <T> getObject(prefs: SharedPreferences?, key: String, classOfT: Class<T>): T {
        val json: String = prefs?.getString(key, "") ?: ""
        return Gson().fromJson(json, classOfT) ?: throw NullPointerException()
    }


    fun hasInternetConnection(context: Context, callBack: ((Boolean) -> Unit)) {
        if (isNetworkAvailable(context)) {
            CoroutineScope(Dispatchers.IO).launch {
                hostAvailable {
                    CoroutineScope(Dispatchers.Main).launch {
                        callBack.invoke(it)
                    }
                }
            }
        } else callBack.invoke(false)
    }

    private fun isNetworkAvailable(context: Context): Boolean {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val capabilities = connectivityManager.getNetworkCapabilities(connectivityManager.activeNetwork) ?: return false
            return when {
                capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> true
                capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> true
                capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> true
                else -> false
            }
        } else {
            val info = connectivityManager.activeNetworkInfo
            if (info != null && info.isConnectedOrConnecting) return true
        }
        return false
    }

    private fun hostAvailable(callBack: ((Boolean) -> Unit)) {
        try {
            Socket().use { socket ->
                socket.connect(InetSocketAddress("www.meter.net", 80), 2000)
                callBack.invoke(true)
            }
        } catch (e: IOException) {
            callBack.invoke(false)
        }
    }

    fun openPlayStore(context: Context, targetClick: String, appId: String) {
        try {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(targetClick))
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            context.startActivity(intent)
        } catch (e: ActivityNotFoundException) {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=$appId"))
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            context.startActivity(intent)
        }
    }

    var alertDialog: AlertDialog? = null
    fun internetDialog(activity: Activity, callback: (() -> Unit)? = null) {
        val dialogBinding = DialogInternetBinding.inflate(activity.layoutInflater, null, false)
        dialogBinding.btnOk.setOnClickListener {
            alertDialog?.dismiss()
            alertDialog = null
            callback?.invoke()
        }
        if (alertDialog != null) {
            alertDialog?.dismiss()
            alertDialog = null
        }
        alertDialog = AlertDialog.Builder(activity).setCancelable(false).setView(dialogBinding.root).create()
        alertDialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        CoroutineScope(Dispatchers.IO).launch {
            delay(500)
            withContext(Dispatchers.Main) {
                if (!activity.isFinishing) alertDialog?.show()
            }
        }
    }
}