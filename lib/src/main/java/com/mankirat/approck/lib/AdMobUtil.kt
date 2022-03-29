package com.mankirat.approck.lib

import android.app.Activity
import android.content.Context
import android.os.Bundle
import android.util.Log
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback

object AdMobUtil {

    /*companion object {
        var instance: AdMobUtil? = null
            get() {
                if (field == null) field = AdMobUtil()
                return field ?: AdMobUtil()
            }


        var CURRENT_BUTTON_CLICK_COUNT = 0
    }*/

    var CURRENT_BUTTON_CLICK_COUNT = 0

    val adMobIds = AdMobIds()

    private fun log(msg: String, e: Throwable? = null) {
        Log.e("AdMobUtil", msg, e)
    }

    fun firebaseEvent(adMobEnum: AdMobEnum, isLoad: Boolean, isSuccess: Boolean) {
        val eventName = adMobEnum.firebaseEvent + "_" +
                (if (isLoad) "load" else "show") + "_" +
                (if (isSuccess) "success" else "fail")

        //if (ApplicationGlobal.instance != null) {
        //    ApplicationGlobal.instance.firebaseAnalytics.logEvent(name, bundle)
        //}
    }


    fun setUp(context: Activity, debugMode: Boolean = BuildConfig.DEBUG) {
        log("setUp")
        adMobIds.debugIds = debugMode
        MobileAds.initialize(context)

        loadInterstitial(context.applicationContext)
        loadInterstitialSplash(context.applicationContext)
        //loadRewardedAd(activity)

        //if (BuildConfig.DEBUG) MediationTestSuite.launch(context)
    }

    /*___________________________ Interstitial Ad ___________________________*/

    private var mInterstitialAd: InterstitialAd? = null
    private var mInterstitialAdSplash: InterstitialAd? = null
    var isInterstitialLoading = false
    var isInterstitialLoadingSplash = false

    private fun loadInterstitial(context: Context) {
        log("loadInterstitial : instance = $mInterstitialAd : isLoading = $isInterstitialLoading")
        //if (isPremium(false)) return

        if (mInterstitialAd != null || isInterstitialLoading) return

        isInterstitialLoading = true
        InterstitialAd.load(context, adMobIds.interstitialId, AdRequest.Builder().build(), object : InterstitialAdLoadCallback() {
            override fun onAdLoaded(interstitialAd: InterstitialAd) {
                super.onAdLoaded(interstitialAd)
                log("loadInterstitial : onAdLoaded")
                firebaseEvent(AdMobEnum.INTERSTITIAL, true, true)

                mInterstitialAd = interstitialAd
                isInterstitialLoading = false
            }

            override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                super.onAdFailedToLoad(loadAdError)
                log("loadInterstitial : onAdFailedToLoad : loadAdError = $loadAdError")
                firebaseEvent(AdMobEnum.INTERSTITIAL, true, false)

                mInterstitialAd = null
                isInterstitialLoading = false
            }
        })
    }

    fun showInterstitial(activity: Activity, callback: ((success: Boolean) -> Unit)? = null) {
        log("showInterstitial : mInterstitialAd = $mInterstitialAd")
        //if (isPremium()) {
        //    callback?.invoke(false)
        //    return
        //}

        if (mInterstitialAd == null) {
            loadInterstitial(activity.applicationContext)
            callback?.invoke(false)
            return
        }

        val fullScreenContentCallback = object : FullScreenContentCallback() {
            override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                super.onAdFailedToShowFullScreenContent(adError)
                log("showInterstitial : onAdFailedToShowFullScreenContent : adError = $adError")
                firebaseEvent(AdMobEnum.INTERSTITIAL, isLoad = false, isSuccess = false)

                callback?.invoke(false)
                mInterstitialAd = null
                loadInterstitial(activity.applicationContext)
            }

            override fun onAdShowedFullScreenContent() {
                super.onAdShowedFullScreenContent()
                log("showInterstitial : onAdShowedFullScreenContent")
            }

            override fun onAdDismissedFullScreenContent() {
                super.onAdDismissedFullScreenContent()
                log("showInterstitial : onAdDismissedFullScreenContent")
                firebaseEvent(AdMobEnum.INTERSTITIAL, isLoad = false, isSuccess = true)

                callback?.invoke(true)
                mInterstitialAd = null
                loadInterstitial(activity.applicationContext)
            }

            override fun onAdImpression() {
                super.onAdImpression()
                log("showInterstitial : onAdImpression")
            }
        }

        mInterstitialAd?.fullScreenContentCallback = fullScreenContentCallback
        mInterstitialAd?.show(activity)

        CURRENT_BUTTON_CLICK_COUNT = 0
    }

    private fun loadInterstitialSplash(context: Context) {
        log("loadInterstitialSplash : instance = $mInterstitialAdSplash : isLoading = $isInterstitialLoadingSplash")
        //if (isPremium(false)) return

        if (mInterstitialAdSplash != null || isInterstitialLoadingSplash) return

        isInterstitialLoadingSplash = true
        InterstitialAd.load(context, adMobIds.interstitialIdSplash, AdRequest.Builder().build(), object : InterstitialAdLoadCallback() {
            override fun onAdLoaded(interstitialAd: InterstitialAd) {
                super.onAdLoaded(interstitialAd)
                log("loadInterstitialSplash : onAdLoaded")
                firebaseEvent(AdMobEnum.INTERSTITIAL_SPLASH, isLoad = true, isSuccess = true)

                mInterstitialAdSplash = interstitialAd
                isInterstitialLoadingSplash = false
            }

            override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                super.onAdFailedToLoad(loadAdError)
                log("loadInterstitialSplash : onAdFailedToLoad : loadAdError = $loadAdError")
                firebaseEvent(AdMobEnum.INTERSTITIAL_SPLASH, isLoad = true, isSuccess = false)

                mInterstitialAdSplash = null
                isInterstitialLoadingSplash = false
            }
        })
    }

    fun showInterstitialSplash(activity: Activity, callback: ((success: Boolean) -> Unit)? = null) {
        log("showInterstitialSplash : mInterstitialAd = $mInterstitialAdSplash")
//if (isPremium()) {
        //    callback?.invoke(false)
        //    return
        //}


        if (adMobIds.interstitialIdSplash.isEmpty()) {
            showInterstitial(activity, callback)
            return
        }

        if (mInterstitialAdSplash == null) {
            loadInterstitialSplash(activity.applicationContext)
            callback?.invoke(false)
            return
        }

        val fullScreenContentCallback = object : FullScreenContentCallback() {
            override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                super.onAdFailedToShowFullScreenContent(adError)
                log("showInterstitialSplash : onAdFailedToShowFullScreenContent : adError = $adError")
                firebaseEvent(AdMobEnum.INTERSTITIAL_SPLASH, isLoad = false, isSuccess = false)

                callback?.invoke(false)
                mInterstitialAdSplash = null
                loadInterstitialSplash(activity.applicationContext)
            }

            override fun onAdShowedFullScreenContent() {
                super.onAdShowedFullScreenContent()
                log("showInterstitialSplash : onAdShowedFullScreenContent")
            }

            override fun onAdDismissedFullScreenContent() {
                super.onAdDismissedFullScreenContent()
                log("showInterstitialSplash : onAdDismissedFullScreenContent")
                firebaseEvent(AdMobEnum.INTERSTITIAL_SPLASH, isLoad = false, isSuccess = true)

                callback?.invoke(true)
                mInterstitialAdSplash = null
                loadInterstitialSplash(activity.applicationContext)
            }

            override fun onAdImpression() {
                super.onAdImpression()
                log("showInterstitialSplash : onAdImpression")
            }
        }

        mInterstitialAdSplash?.fullScreenContentCallback = fullScreenContentCallback
        mInterstitialAdSplash?.show(activity)

        CURRENT_BUTTON_CLICK_COUNT = 0
    }

}