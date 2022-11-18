package com.mankirat.approck.lib.admob

import android.app.Activity
import android.content.Context
import android.content.SharedPreferences
import android.content.res.ColorStateList
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.RatingBar
import android.widget.TextView
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdLoader
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.nativead.MediaView
import com.google.android.gms.ads.nativead.NativeAd
import com.google.android.gms.ads.nativead.NativeAdView
import com.mankirat.approck.lib.AdType
import com.mankirat.approck.lib.MyConstants
import com.mankirat.approck.lib.R
import com.mankirat.approck.lib.Utils

object AdMobUtil {
    val adMobIds = AdMobIds()
    private var sharedPreferences: SharedPreferences? = null

    // Click Count
    private var interstitialAdsHandler: InterstitialAdsHandler? = null
    private var rewardAdsHandler: RewardAdsHandler? = null
    var needButtonClick = false

    private fun log(msg: String, e: Throwable? = null) = Log.e("AdMobUtil", msg, e)

    fun setUp(context: Context, targetClick: Int = 4, targetScreenCount: Int = 2, targetTabChangeCount: Int = 3, nativeColor: Int, testMediation: Boolean = false) {
        log("setUp -> targetClick: $targetClick, targetScreenCount: $targetScreenCount")

        interstitialAdsHandler = InterstitialAdsHandler.getInstance(adMobIds.interstitialId, adMobIds.interstitialIdSplash, targetClick, targetScreenCount, targetTabChangeCount)
        rewardAdsHandler = RewardAdsHandler.getInstance(adMobIds.rewardId)
        sharedPreferences = context.getSharedPreferences(MyConstants.SHARED_PREF_IAP, Context.MODE_PRIVATE)

        MobileAds.initialize(context) { initializationStatus ->
            val statusMap = initializationStatus.adapterStatusMap
            for (adapterClass in statusMap.keys) {
                val status = statusMap[adapterClass]
                log(String.format("Adapter name: %s, Description: %s, Latency: %d", adapterClass, status?.description, status?.latency))
            }
        }
        interstitialAdsHandler?.loadInterstitial(context)
        rewardAdsHandler?.loadRewardedAd(context)

        defaultNativeAdStyle.setColorTheme(nativeColor)
        if (testMediation) MobileAds.openAdInspector(context) {
            log("MobileAds Error: ${it?.message}")
        }
    }

    private fun isPremium(): Boolean = sharedPreferences?.getBoolean(MyConstants.IS_PREMIUM, MyConstants.IAP_DEFAULT_STATUS) ?: false

    /*______________________________ Banner ______________________________*/

    fun loadBanner(adContainer: FrameLayout, adSize: AdSize = AdSize.BANNER): AdView? {
        log("loadBanner")
        if (isPremium()) {
            adContainer.visibility = View.GONE
            return null
        }

        adContainer.visibility = View.VISIBLE

        val adListener: AdListener = object : AdListener() {
            override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                super.onAdFailedToLoad(loadAdError)
                log("loadBanner : onAdFailedToLoad : code =" + loadAdError.code.toString() + " : message =" + loadAdError.message)
                Utils.showError(AdType.BANNER, loadAdError.code, loadAdError.message)
            }

            override fun onAdLoaded() {
                super.onAdLoaded()
                log("loadBanner : onAdLoaded")
                Utils.showSuccess(AdType.BANNER)
            }
        }

        val adView = AdView(adContainer.context)
        adView.setAdSize(adSize)
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
            override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                super.onAdFailedToLoad(loadAdError)
                log("showNativeAd : onAdFailedToLoad : loadAdError = $loadAdError")
                Utils.loadError(AdType.NATIVE, loadAdError.code, loadAdError.message)
            }

            override fun onAdLoaded() {
                super.onAdLoaded()
                log("showNativeAd : onAdLoaded")
                Utils.loadSuccess(AdType.NATIVE)
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

        AdLoader.Builder(adContainer.context, adMobIds.nativeId).forNativeAd(onNativeAdLoadedListener).withAdListener(adListener).build().loadAd(AdRequest.Builder().build())
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
            mediaView.mediaContent = mediaContent
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

    /*______________________________ Interstitial ______________________________*/

    fun buttonClickCount(activity: Activity, callback: ((success: Boolean) -> Unit)? = null) {
        if (needButtonClick) interstitialAdsHandler?.buttonClickCount(activity, callback)
        else callback?.invoke(true)
    }

    fun screenOpenCount(activity: Activity, callback: ((success: Boolean) -> Unit)? = null) {
        interstitialAdsHandler?.screenOpenCount(activity, callback)
    }

    fun tabChangeCount(activity: Activity, callback: ((success: Boolean) -> Unit)? = null) {
        interstitialAdsHandler?.tabChangeCount(activity, callback)
    }

    fun loadInterstitial(context: Context) {
        interstitialAdsHandler?.loadInterstitial(context)
    }

    fun showInterstitial(activity: Activity, callback: ((success: Boolean) -> Unit)? = null) {
        interstitialAdsHandler?.showInterstitial(activity, callback)
    }

    fun loadInterstitialSplash(context: Context, callback: (() -> Unit)? = null) {
        interstitialAdsHandler?.loadInterstitialSplash(context, callback)
    }

    fun showInterstitialSplash(activity: Activity, callback: ((success: Boolean) -> Unit)? = null) {
        interstitialAdsHandler?.showInterstitialSplash(activity, callback)
    }

    /*______________________________ Reward ______________________________*/

    fun loadRewardAd(context: Context) {
        rewardAdsHandler?.loadRewardedAd(context)
    }

    fun showRewardAd(activity: Activity, callback: ((success: Boolean) -> Unit)? = null) {
        rewardAdsHandler?.showRewardAd(activity, callback)
    }
}


/*
* Pending Tasks:
* banner adview gravity center
* */