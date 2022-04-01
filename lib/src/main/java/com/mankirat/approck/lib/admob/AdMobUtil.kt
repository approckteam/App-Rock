package com.mankirat.approck.lib.admob

import android.app.Activity
import android.content.Context
import android.content.SharedPreferences
import android.content.res.ColorStateList
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.RatingBar
import android.widget.TextView
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdLoader
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import com.google.android.gms.ads.nativead.MediaView
import com.google.android.gms.ads.nativead.NativeAd
import com.google.android.gms.ads.nativead.NativeAdView
import com.mankirat.approck.lib.AdType
import com.mankirat.approck.lib.BuildConfig
import com.mankirat.approck.lib.MyConstants
import com.mankirat.approck.lib.R

object AdMobUtil {

    /*companion object {
        var instance: AdMobUtil? = null
            get() {
                if (field == null) field = AdMobUtil()
                return field ?: AdMobUtil()
            }


        var CURRENT_BUTTON_CLICK_COUNT = 0
    }*/


    @Suppress("MemberVisibilityCanBePrivate")
    val adMobIds = AdMobIds()
    private val iapIds = ArrayList<String>()
    private val subsIds = ArrayList<String>()
    private var sharedPreferences: SharedPreferences? = null //by lazy { mContext.getSharedPreferences(MyConstants.SHARED_PREF_IAP, Context.MODE_PRIVATE) }


    fun setUp(
        context: Context, targetClick: Long, targetScreenCount: Long? = null, nativeColor: Int,
        iapIds: ArrayList<String>? = null, subsIds: ArrayList<String>? = null, debugMode: Boolean = BuildConfig.DEBUG,
    ) {
        log("setUp")
        adMobIds.debugIds = if (BuildConfig.DEBUG) debugMode else false
        targetClickCount = targetClick
        if (targetScreenCount != null) screenOpenCount = targetScreenCount
        sharedPreferences = context.getSharedPreferences(MyConstants.SHARED_PREF_IAP, Context.MODE_PRIVATE)
        this.iapIds.clear()
        if (iapIds != null) this.iapIds.addAll(iapIds)
        this.subsIds.clear()
        if (subsIds != null) this.subsIds.addAll(subsIds)

        MobileAds.initialize(context)

        loadInterstitial(context.applicationContext)
        loadInterstitialSplash(context.applicationContext)
        //loadRewardedAd(activity)

        defaultNativeAdStyle.setColorTheme(nativeColor)
        //if (BuildConfig.DEBUG) MediationTestSuite.launch(context)
    }


    private fun isPremium(isLoad: Boolean = false): Boolean {
        //if (iapIds.isEmpty() && subsIds.isEmpty()) return false
        val defaultStatus = if (isLoad) false else MyConstants.IAP_DEFAULT_STATUS

        val productStatus = isAnyPurchased(iapIds, false, defaultStatus)
        val subStatus = isAnyPurchased(subsIds, true, defaultStatus)
        val isPremium = productStatus || subStatus

        log("isPremium : premium = $isPremium")
        return isPremium
    }

    private fun isAnyPurchased(list: ArrayList<String>?, sub: Boolean, default: Boolean): Boolean {
        var status = false//is any product purchased from list
        list?.forEach { productId ->
            if (getProductStatus(productId, sub, default)) {
                status = true
                return@forEach
            }
        }

        return status
    }

    private fun getProductStatus(productId: String, sub: Boolean, default: Boolean): Boolean {
        val status = if (sub) sharedPreferences?.getBoolean(productId + MyConstants.SUBSCRIPTION_STATUS_POSTFIX, default)
        else sharedPreferences?.getBoolean(productId + MyConstants.PURCHASE_STATUS_POSTFIX, default)

        return status ?: false
    }

    /*___________________________ log and event ___________________________*/

    private fun log(msg: String, e: Throwable? = null) {
        Log.e("AdMobUtil", msg, e)
    }

    @Suppress("MemberVisibilityCanBePrivate")
    var firebaseEventCallback: ((eventName: String, bundle: Bundle) -> Unit)? = null

    fun firebaseEvent(adMobEnum: AdType, isLoad: Boolean, isSuccess: Boolean) {
//        val eventName = adMobEnum.firebaseEvent + "_" +
//                (if (isLoad) "load" else "show") + "_" +
//                (if (isSuccess) "success" else "fail")

        val eventName = "AdMobStatus"
        val bundle = Bundle().apply {
            putString("ad_type", adMobEnum.name)
            putBoolean("is_load", isLoad)
            putBoolean("is_success", isSuccess)
        }

        firebaseEventCallback?.invoke(eventName, bundle)
    }

    /*___________________________ click count ___________________________*/

    private var currentClickCount = 0
    private var currentScreenCount = 0
    private var targetClickCount = 4L
    private var screenOpenCount = 2L

    fun buttonClickCount(context: Activity, callback: ((success: Boolean) -> Unit)? = null) {
        currentClickCount += 1
        log("buttonClickCount : targetClick = $targetClickCount : currentClick = $currentClickCount")
        if (currentClickCount >= targetClickCount) {
            //  currentClickCount = 0
            showInterstitial(context, callback)
        } else {
            callback?.invoke(false)
        }
    }

    fun screenOpenCount(context: Activity, callback: ((success: Boolean) -> Unit)? = null) {
        currentScreenCount += 1
        log("screenOpenCount : targetClick = $screenOpenCount : currentClick = $currentScreenCount")
        if (currentScreenCount >= screenOpenCount) {
            // currentScreenCount = 0
            showInterstitial(context, callback)
        } else {
            callback?.invoke(false)
        }
    }

    /*___________________________ Interstitial Ad ___________________________*/

    private var mInterstitialAd: InterstitialAd? = null
    private var mInterstitialAdSplash: InterstitialAd? = null
    private var isInterstitialLoading = false
    private var isInterstitialLoadingSplash = false

    private fun loadInterstitial(context: Context) {
        log("loadInterstitial : instance = $mInterstitialAd : isLoading = $isInterstitialLoading")
        if (isPremium(true)) return

        if (mInterstitialAd != null || isInterstitialLoading) return

        isInterstitialLoading = true
        InterstitialAd.load(context, adMobIds.interstitialId, AdRequest.Builder().build(), object : InterstitialAdLoadCallback() {
            override fun onAdLoaded(interstitialAd: InterstitialAd) {
                super.onAdLoaded(interstitialAd)
                log("loadInterstitial : onAdLoaded")
                firebaseEvent(AdType.INTERSTITIAL, isLoad = true, isSuccess = true)

                mInterstitialAd = interstitialAd
                isInterstitialLoading = false
            }

            override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                super.onAdFailedToLoad(loadAdError)
                log("loadInterstitial : onAdFailedToLoad : loadAdError = $loadAdError")
                firebaseEvent(AdType.INTERSTITIAL, isLoad = true, isSuccess = false)

                mInterstitialAd = null
                isInterstitialLoading = false
            }
        })
    }

    fun showInterstitial(activity: Activity, callback: ((success: Boolean) -> Unit)? = null) {
        log("showInterstitial : mInterstitialAd = $mInterstitialAd")
        if (isPremium()) {
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
                firebaseEvent(AdType.INTERSTITIAL, isLoad = false, isSuccess = false)

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
                firebaseEvent(AdType.INTERSTITIAL, isLoad = false, isSuccess = true)

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

    private fun loadInterstitialSplash(context: Context) {
        log("loadInterstitialSplash : instance = $mInterstitialAdSplash : isLoading = $isInterstitialLoadingSplash")
        if (isPremium(true)) return

        if (mInterstitialAdSplash != null || isInterstitialLoadingSplash) return

        isInterstitialLoadingSplash = true
        InterstitialAd.load(context, adMobIds.interstitialIdSplash, AdRequest.Builder().build(), object : InterstitialAdLoadCallback() {
            override fun onAdLoaded(interstitialAd: InterstitialAd) {
                super.onAdLoaded(interstitialAd)
                log("loadInterstitialSplash : onAdLoaded")
                firebaseEvent(AdType.INTERSTITIAL_SPLASH, isLoad = true, isSuccess = true)

                mInterstitialAdSplash = interstitialAd
                isInterstitialLoadingSplash = false
            }

            override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                super.onAdFailedToLoad(loadAdError)
                log("loadInterstitialSplash : onAdFailedToLoad : loadAdError = $loadAdError")
                firebaseEvent(AdType.INTERSTITIAL_SPLASH, isLoad = true, isSuccess = false)

                mInterstitialAdSplash = null
                isInterstitialLoadingSplash = false
            }
        })
    }

    fun showInterstitialSplash(activity: Activity, callback: ((success: Boolean) -> Unit)? = null) {
        log("showInterstitialSplash : mInterstitialAd = $mInterstitialAdSplash")
        if (isPremium()) {
            callback?.invoke(false)
            return
        }


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
                firebaseEvent(AdType.INTERSTITIAL_SPLASH, isLoad = false, isSuccess = false)

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
                firebaseEvent(AdType.INTERSTITIAL_SPLASH, isLoad = false, isSuccess = true)

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

    /*______________________________ Banner ______________________________*/

    fun loadBanner(adContainer: FrameLayout, adSize: AdSize): AdView? {
        log("loadBanner")
        if (isPremium()) {
            adContainer.visibility = View.GONE
            return null
        }

        adContainer.visibility = View.VISIBLE

        val adListener: AdListener = object : AdListener() {
            override fun onAdClosed() {
                super.onAdClosed()
                log("loadBanner : onAdClosed")
            }

            override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                super.onAdFailedToLoad(loadAdError)
                log("loadBanner : onAdFailedToLoad : code =" + loadAdError.code.toString() + " : message =" + loadAdError.message)
                firebaseEvent(AdType.BANNER, isLoad = true, isSuccess = false)
            }

            override fun onAdOpened() {
                super.onAdOpened()
                log("loadBanner : onAdOpened")
            }

            override fun onAdLoaded() {
                super.onAdLoaded()
                log("loadBanner : onAdLoaded")
                firebaseEvent(AdType.BANNER, isLoad = true, isSuccess = true)
            }

            override fun onAdClicked() {
                super.onAdClicked()
                log("loadBanner : onAdClicked")
            }

            override fun onAdImpression() {
                super.onAdImpression()
                log("loadBanner : onAdImpression")
            }
        }

        val adView = AdView(adContainer.context)
        adView.adSize = adSize
        adView.adUnitId = adMobIds.bannerId
        adView.adListener = adListener
        adView.loadAd(AdRequest.Builder().build())
        adContainer.removeAllViews()
        adContainer.addView(adView)

        return adView
    }

    /*______________________________ Native ______________________________*/

    private val defaultNativeAdStyle = NativeAdStyle()

    fun showNativeAd(adContainer: FrameLayout, nativeAdStyle: NativeAdStyle? = null, callback: ((nativeAd: NativeAd) -> Unit)? = null) {
        log("showNativeAd")
        if (isPremium()) {
            adContainer.visibility = View.GONE
            return
        }

        adContainer.visibility = View.VISIBLE

        val adListener = object : AdListener() {
            override fun onAdClosed() {
                super.onAdClosed()
                log("showNativeAd : onAdClosed")
            }

            override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                super.onAdFailedToLoad(loadAdError)
                log("showNativeAd : onAdFailedToLoad : loadAdError = $loadAdError")
                firebaseEvent(AdType.NATIVE, isLoad = true, isSuccess = false)
            }

            override fun onAdOpened() {
                log("showNativeAd : onAdOpened")
                super.onAdOpened()
            }

            override fun onAdLoaded() {
                super.onAdLoaded()
                log("showNativeAd : onAdLoaded")
                firebaseEvent(AdType.NATIVE, isLoad = true, isSuccess = true)
            }

            override fun onAdClicked() {
                super.onAdClicked()
                log("showNativeAd : onAdClicked")
            }

            override fun onAdImpression() {
                super.onAdImpression()
                log("showNativeAd : onAdImpression")
            }
        }

        val onNativeAdLoadedListener = NativeAd.OnNativeAdLoadedListener { nativeAd ->
            log("showNativeAd : onNativeAdLoaded")

            val layoutInflater = adContainer.context.getSystemService(LayoutInflater::class.java)
            val adView = layoutInflater.inflate(R.layout.native_ad_mob_1, adContainer, false) as NativeAdView

            populateNativeAdViews(adView, nativeAd, nativeAdStyle ?: defaultNativeAdStyle)

            adContainer.removeAllViews()
            adContainer.addView(adView)

            callback?.invoke(nativeAd)
        }

        AdLoader.Builder(adContainer.context, adMobIds.nativeId)
            .forNativeAd(onNativeAdLoadedListener)
            .withAdListener(adListener)
            .build()
            .loadAd(AdRequest.Builder().build())
    }

    private fun populateNativeAdViews(adView: NativeAdView, nativeAd: NativeAd, nativeAdStyle: NativeAdStyle) {
        log("populateNativeAdViews")
        val clMain = adView.findViewById<View>(R.id.cl_main)
        val tvAd = adView.findViewById<TextView>(R.id.tv_ad)
        val mediaView = adView.findViewById<MediaView>(R.id.media_view)//either video or image
        val tvHeadline = adView.findViewById<TextView>(R.id.tv_headline)
        val tvAdvertiser = adView.findViewById<TextView>(R.id.tv_advertiser)
        val tvBody = adView.findViewById<TextView>(R.id.tv_body)
        val tvPrice = adView.findViewById<TextView>(R.id.tv_price)
        val tvStore = adView.findViewById<TextView>(R.id.tv_store)
        val btnAction = adView.findViewById<Button>(R.id.btn_action)
        val ivIcon = adView.findViewById<ImageView>(R.id.iv_icon)
        val rbStars = adView.findViewById<RatingBar>(R.id.rb_stars)

        clMain.background = nativeAdStyle.getBackground(adView.context)
        tvBody.setTextColor(nativeAdStyle.bodyTextColor)
        rbStars.progressTintList = ColorStateList.valueOf(nativeAdStyle.starTint)
        tvHeadline.setTextColor(nativeAdStyle.headlineTextColor)
        tvAdvertiser.setTextColor(nativeAdStyle.advertiserTextColor)
        tvAd.setTextColor(nativeAdStyle.adTextColor)
        tvAd.backgroundTintList = ColorStateList.valueOf(nativeAdStyle.adBackColor)
        tvPrice.setTextColor(nativeAdStyle.priceTextColor)
        tvStore.setTextColor(nativeAdStyle.storeTextColor)
        btnAction.setTextColor(nativeAdStyle.actionTextColor)
        btnAction.setBackgroundColor(nativeAdStyle.actionBackColor)

        tvHeadline.text = nativeAd.headline
        tvAdvertiser.text = nativeAd.advertiser
        tvBody.text = nativeAd.body
        tvPrice.text = nativeAd.price
        tvStore.text = nativeAd.store
        btnAction.text = nativeAd.callToAction
        tvAdvertiser.visibility = if (nativeAd.advertiser?.trim()?.isNotEmpty() == true) View.VISIBLE else View.GONE
        val mediaContent = nativeAd.mediaContent
        mediaView.visibility = if (mediaContent == null) {
            View.GONE
        } else {
            mediaView.setMediaContent(mediaContent)
            mediaView.setImageScaleType(ImageView.ScaleType.CENTER_CROP)
            View.VISIBLE
        }
        val icon = nativeAd.icon
        ivIcon.visibility = if (icon == null) {
            View.GONE
        } else {
            ivIcon.setImageDrawable(icon.drawable)
            View.VISIBLE
        }
        val starRating = nativeAd.starRating
        rbStars.visibility = if (starRating == null) {
            View.GONE
        } else {
            rbStars.rating = starRating.toFloat()
            View.VISIBLE
        }


        adView.headlineView = tvHeadline
        adView.iconView = ivIcon
        adView.mediaView = mediaView
        adView.advertiserView = tvAdvertiser
        adView.starRatingView = rbStars
        adView.bodyView = tvBody
        adView.priceView = tvPrice
        adView.storeView = tvStore
        adView.callToActionView = btnAction
        adView.setNativeAd(nativeAd)
    }

}

@Suppress("unused")
fun Activity.adMobClickCount(callback: ((success: Boolean) -> Unit)? = null) {
    AdMobUtil.buttonClickCount(this, callback)
}

@Suppress("unused")
fun Activity.adMobScreenCount(callback: ((success: Boolean) -> Unit)? = null) {
    AdMobUtil.screenOpenCount(this, callback)
}

@Suppress("unused")
fun Activity.adMobInter(callback: ((success: Boolean) -> Unit)? = null) {
    AdMobUtil.showInterstitial(this, callback)
}

@Suppress("unused")
fun Activity.adMobInterSplash(callback: ((success: Boolean) -> Unit)? = null) {
    AdMobUtil.showInterstitialSplash(this, callback)
}

@Suppress("unused")
fun FrameLayout.adMobBanner(adSize: AdSize = AdSize.BANNER): AdView? {
    return AdMobUtil.loadBanner(this, adSize)
}

@Suppress("unused")
fun FrameLayout.adMobNative(nativeAdStyle: NativeAdStyle? = null, callback: ((nativeAd: NativeAd) -> Unit)? = null) {
    AdMobUtil.showNativeAd(this, nativeAdStyle, callback)
}


/*
* Pending Tasks:
* banner adview gravity center
* reward ad missing
* test suit
* */