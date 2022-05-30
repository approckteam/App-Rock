package com.mankirat.approck.lib.admob

import android.app.Activity
import android.app.Application
import android.content.Context
import android.os.Bundle
import android.util.Log
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ProcessLifecycleOwner
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.appopen.AppOpenAd
import com.mankirat.approck.lib.AdType
import com.mankirat.approck.lib.MyConstants
import com.mankirat.approck.lib.Utils

class AppOpenManager(private val id: String, application: Application) : Application.ActivityLifecycleCallbacks, LifecycleEventObserver {

    private var appOpenAd: AppOpenAd? = null
    private var isLoading = false
    private var currentActivity: Activity? = null

    init {
        application.registerActivityLifecycleCallbacks(this)
        ProcessLifecycleOwner.get().lifecycle.addObserver(this)
    }

    private fun log(msg: String, t: Throwable? = null) = Log.e("AppOpenManager", msg, t)

    private fun isPremium(context: Context, default: Boolean = false): Boolean =
        context.getSharedPreferences(MyConstants.SHARED_PREF_IAP, Context.MODE_PRIVATE).getBoolean(MyConstants.IS_PREMIUM, default)

    private val loadCallback = object : AppOpenAd.AppOpenAdLoadCallback() {
        override fun onAdLoaded(ad: AppOpenAd) {
            super.onAdLoaded(ad)
            log("loadAppOpen : onAdLoaded -> AdClass: ${ad.responseInfo.mediationAdapterClassName}")
            Utils.loadSuccess(AdType.APP_OPEN)

            isLoading = false
            appOpenAd = ad
        }

        override fun onAdFailedToLoad(loadAdError: LoadAdError) {
            super.onAdFailedToLoad(loadAdError)
            log("loadAppOpen : onAdFailedToLoad : loadAdError = $loadAdError")
            Utils.loadError(AdType.APP_OPEN, loadAdError.code, loadAdError.message)

            isLoading = false
        }
    }

    private fun loadAppOpen(context: Context) {
        log("loadAppOpen : instance = $appOpenAd : isLoading = $isLoading")
        if (isPremium(context)) return
        if (appOpenAd != null || isLoading) return

        isLoading = true
        AppOpenAd.load(context, id, AdRequest.Builder().build(), AppOpenAd.APP_OPEN_AD_ORIENTATION_PORTRAIT, loadCallback)
    }

    private fun showAppOpen(activity: Activity) {
        log("showAppOpen : instance = $appOpenAd : isLoading = $isLoading")
        if (isPremium(activity, true)) return

        if (appOpenAd == null) {
            loadAppOpen(activity.applicationContext)
            return
        }

        val fullScreenContentCallback = object : FullScreenContentCallback() {
            override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                super.onAdFailedToShowFullScreenContent(adError)

                log("showAppOpen : onAdFailedToShowFullScreenContent : adError = $adError")
                Utils.showError(AdType.APP_OPEN, adError.code, adError.message)

                appOpenAd = null
                loadAppOpen(activity.applicationContext)
            }

            override fun onAdShowedFullScreenContent() {
                super.onAdShowedFullScreenContent()
                log("showAppOpen : onAdShowedFullScreenContent")
            }

            override fun onAdDismissedFullScreenContent() {
                super.onAdDismissedFullScreenContent()
                Utils.showSuccess(AdType.APP_OPEN)

                appOpenAd = null
                loadAppOpen(activity.applicationContext)
            }
        }

        appOpenAd?.fullScreenContentCallback = fullScreenContentCallback
        appOpenAd?.show(activity)
    }

    override fun onActivityCreated(activity: Activity, bundle: Bundle?) {

    }

    override fun onActivityStarted(activity: Activity) {
        currentActivity = activity
    }

    override fun onActivityResumed(activity: Activity) {
        currentActivity = activity
    }

    override fun onActivityPaused(activity: Activity) {

    }

    override fun onActivityStopped(activity: Activity) {

    }

    override fun onActivitySaveInstanceState(activity: Activity, bundle: Bundle) {

    }

    override fun onActivityDestroyed(activity: Activity) {
        currentActivity = null
    }

    override fun onStateChanged(source: LifecycleOwner, event: Lifecycle.Event) {
        if (event == Lifecycle.Event.ON_START && currentActivity != null) {
            showAppOpen(currentActivity!!)
        }
    }
}