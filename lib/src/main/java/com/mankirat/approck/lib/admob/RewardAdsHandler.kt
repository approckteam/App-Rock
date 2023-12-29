package com.mankirat.approck.lib.admob

import android.app.Activity
import android.content.Context
import android.util.Log
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.OnUserEarnedRewardListener
import com.google.android.gms.ads.rewarded.RewardedAd
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback
import com.mankirat.approck.lib.AdType
import com.mankirat.approck.lib.MyConstants
import com.mankirat.approck.lib.Utils

class RewardAdsHandler private constructor(private val id: String) {

    private var mRewardedAd: RewardedAd? = null
    var isRewardedLoading = false

    companion object {
        private var instance: RewardAdsHandler? = null

        fun getInstance(id: String): RewardAdsHandler {
            if (instance == null) instance = RewardAdsHandler(id)
            return instance!!
        }
    }

    private fun log(msg: String, t: Throwable? = null) = Log.e("RewardAdsHandler", msg, t)

    private fun isPremium(context: Context): Boolean =
        context.getSharedPreferences(MyConstants.SHARED_PREF_IAP, Context.MODE_PRIVATE).getBoolean(MyConstants.IS_PREMIUM, MyConstants.IAP_DEFAULT_STATUS)

    fun loadRewardedAd(context: Context) {
        log("loadRewardedAd : mRewardedAd = $mRewardedAd : isRewardedLoading = $isRewardedLoading")
        if (isPremium(context)) return

        if (mRewardedAd == null && !isRewardedLoading) {
            isRewardedLoading = true
            RewardedAd.load(context, id, AdRequest.Builder().build(), object : RewardedAdLoadCallback() {
                override fun onAdLoaded(rewardedAd: RewardedAd) {
                    super.onAdLoaded(rewardedAd)
                    log("loadRewardedAd : onAdLoaded -> AdClass: ${rewardedAd.responseInfo.mediationAdapterClassName}")
                    Utils.loadSuccess(AdType.REWARD)

                    mRewardedAd = rewardedAd
                    isRewardedLoading = false
                }

                override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                    super.onAdFailedToLoad(loadAdError)
                    log("loadRewardedAd : onAdFailedToLoad : loadAdError = $loadAdError")
                    Utils.loadError(AdType.REWARD, loadAdError.code, loadAdError.message)

                    mRewardedAd = null
                    isRewardedLoading = false
                }
            })
        }
    }

    fun showRewardAd(activity: Activity, callback: ((success: Boolean) -> Unit)? = null) {
        log("showRewardAd : mRewardedAd = $mRewardedAd")
        if (isPremium(activity)) {
            callback?.invoke(false)
            return
        }

        if (mRewardedAd == null) {
            callback?.invoke(false)
            loadRewardedAd(activity)
            return
        }

        val fullScreenContentCallback = object : FullScreenContentCallback() {
            override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                super.onAdFailedToShowFullScreenContent(adError)
                log("showRewardAd : onAdFailedToShowFullScreenContent : adError = $adError")
                Utils.showError(AdType.REWARD, adError.code, adError.message)

                callback?.invoke(false)
                mRewardedAd = null
                loadRewardedAd(activity)
            }

            override fun onAdShowedFullScreenContent() {
                super.onAdShowedFullScreenContent()
                log("showRewardAd : onAdShowedFullScreenContent")
            }

            override fun onAdDismissedFullScreenContent() {
                super.onAdDismissedFullScreenContent()
                log("showRewardAd : onAdDismissedFullScreenContent")
                Utils.showSuccess(AdType.REWARD)

                mRewardedAd = null
                loadRewardedAd(activity)
            }

            override fun onAdImpression() {
                super.onAdImpression()
                log("showRewardAd : onAdImpression")
            }
        }

        val onUserEarnedRewardListener = OnUserEarnedRewardListener { rewardItem ->
            log("showRewardAd : onUserEarnedReward : rewardType = ${rewardItem.type} : rewardAmount = ${rewardItem.amount}")
            callback?.invoke(true)
        }

        mRewardedAd?.fullScreenContentCallback = fullScreenContentCallback
        mRewardedAd?.show(activity, onUserEarnedRewardListener)
    }
}