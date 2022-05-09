package com.mankirat.approck.lib.admob

import android.app.Activity
import android.content.Context
import android.util.Log
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import com.mankirat.approck.lib.AdType
import com.mankirat.approck.lib.Utils

class InterstitialAdsHandler private constructor(private val interstitialId: String, private val interstitialIdSplash: String, private val targetClickCount: Int, private val screenOpenCount: Int) {
    companion object {
        private var instance: InterstitialAdsHandler? = null

        fun getInstance(id: String, splashId: String, targetClickCount: Int = 4, screenOpenCount: Int = 2): InterstitialAdsHandler {
            if (instance == null) instance = InterstitialAdsHandler(id, splashId, targetClickCount, screenOpenCount)
            return instance!!
        }
    }

    private var currentClickCount = 0
    private var currentScreenCount = 0

    private var mInterstitialAd: InterstitialAd? = null
    private var mInterstitialAdSplash: InterstitialAd? = null

    private var isInterstitialLoading = false
    private var isInterstitialLoadingSplash = false

    private var isPremium = false

    private fun log(msg: String, t: Throwable? = null) = Log.e("InterstitialAdsHandler", msg, t)

    fun buttonClickCount(activity: Activity, callback: ((success: Boolean) -> Unit)? = null) {
        currentClickCount += 1
        log("buttonClickCount : targetClick = $targetClickCount : currentClick = $currentClickCount")

        if (currentClickCount >= targetClickCount) showInterstitial(activity, callback)
        else callback?.invoke(false)
    }

    fun screenOpenCount(activity: Activity, callback: ((success: Boolean) -> Unit)? = null) {
        currentScreenCount += 1
        log("screenOpenCount : targetClick = $screenOpenCount : currentClick = $currentScreenCount")

        if (currentScreenCount >= screenOpenCount) showInterstitial(activity, callback)
        else callback?.invoke(false)
    }

    fun loadInterstitial(context: Context) {
        log("loadInterstitial : instance = $mInterstitialAd : isLoading = $isInterstitialLoading")
        if (isPremium) return

        if (mInterstitialAd != null || isInterstitialLoading) return

        isInterstitialLoading = true
        InterstitialAd.load(context, interstitialId, AdRequest.Builder().build(), object : InterstitialAdLoadCallback() {
            override fun onAdLoaded(interstitialAd: InterstitialAd) {
                super.onAdLoaded(interstitialAd)
                Utils.loadSuccess(AdType.INTERSTITIAL)

                mInterstitialAd = interstitialAd
                isInterstitialLoading = false
            }

            override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                super.onAdFailedToLoad(loadAdError)
                log("loadInterstitial : onAdFailedToLoad : loadAdError = $loadAdError")
                Utils.loadError(AdType.INTERSTITIAL, loadAdError.code, loadAdError.message)

                mInterstitialAd = null
                isInterstitialLoading = false
            }
        })
    }

    private fun showInterstitial(activity: Activity, callback: ((success: Boolean) -> Unit)? = null) {
        log("showInterstitial : mInterstitialAd = $mInterstitialAd")
        if (isPremium) {
            callback?.invoke(false)
            return
        }

        if (mInterstitialAd == null) {
            loadInterstitial(activity.applicationContext)
            callback?.invoke(false)
            return
        }

        val fullScreenContentCallback = object : FullScreenContentCallback() {
            override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                super.onAdFailedToShowFullScreenContent(adError)
                log("showInterstitial : onAdFailedToShowFullScreenContent : adError = $adError")
                Utils.showError(AdType.INTERSTITIAL, adError.code, adError.message)

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
                Utils.showSuccess(AdType.INTERSTITIAL)

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

        currentClickCount = 0
        currentScreenCount = 0
    }

    fun loadInterstitialSplash(context: Context) {
        log("loadInterstitialSplash : instance = $mInterstitialAdSplash : isLoading = $isInterstitialLoadingSplash")
        if (isPremium) return

        if (mInterstitialAdSplash != null || isInterstitialLoadingSplash) return

        isInterstitialLoadingSplash = true
        InterstitialAd.load(context, interstitialIdSplash, AdRequest.Builder().build(), object : InterstitialAdLoadCallback() {
            override fun onAdLoaded(interstitialAd: InterstitialAd) {
                super.onAdLoaded(interstitialAd)
                log("loadInterstitialSplash : onAdLoaded")
                Utils.loadSuccess(AdType.INTERSTITIAL_SPLASH)

                mInterstitialAdSplash = interstitialAd
                isInterstitialLoadingSplash = false
            }

            override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                super.onAdFailedToLoad(loadAdError)
                log("loadInterstitialSplash : onAdFailedToLoad : loadAdError = $loadAdError")
                Utils.loadError(AdType.INTERSTITIAL_SPLASH, loadAdError.code, loadAdError.message)

                mInterstitialAdSplash = null
                isInterstitialLoadingSplash = false
            }
        })
    }

    fun showInterstitialSplash(activity: Activity, callback: ((success: Boolean) -> Unit)? = null) {
        log("showInterstitialSplash : mInterstitialAd = $mInterstitialAdSplash")
        if (isPremium) {
            callback?.invoke(false)
            return
        }


        if (interstitialIdSplash.isEmpty()) {
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
                Utils.showError(AdType.INTERSTITIAL_SPLASH, adError.code, adError.message)

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
                Utils.showSuccess(AdType.INTERSTITIAL_SPLASH)

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

        currentClickCount = 0
        currentScreenCount = 0
    }
}