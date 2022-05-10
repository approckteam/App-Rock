package com.mankirat.approck.lib

import android.content.SharedPreferences
import android.os.Bundle
import com.google.gson.Gson
import com.mankirat.approck.lib.MyConstants.FirebaseEvent

object Utils {
    var firebaseEventCallback: ((eventName: String, bundle: Bundle?) -> Unit)? = null
    var purchaseCallback: ((status: Boolean) -> Unit)? = null

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
}