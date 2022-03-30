package com.mankirat.approck

import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.mankirat.approck.databinding.ActivityHomeBinding
import com.mankirat.approck.lib.admob.AdMobUtil
import com.mankirat.approck.lib.admob.adMobBanner
import com.mankirat.approck.lib.admob.adMobInter
import com.mankirat.approck.lib.admob.adMobNative

class HomeActivity : AppCompatActivity() {

    private val binding by lazy { ActivityHomeBinding.inflate(layoutInflater) }

    private fun log(msg: String, e: Throwable? = null) {
        Log.e("HomeActivity", msg, e)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        AdMobUtil.adMobIds.apply {
            interstitialId = Constants.AdMob.INTERSTITIAL
            interstitialIdSplash = Constants.AdMob.INTERSTITIAL_SPLASH
            bannerId = Constants.AdMob.BANNER
            nativeId = Constants.AdMob.NATIVE
            rewardId = Constants.AdMob.REWARD
            appOpenId = Constants.AdMob.APP_OPEN
        }
        AdMobUtil.setUp(this, 4, Color.RED)

//        binding.btnShowInterstitial.setOnClickListener {
//            adMobInter()
//        }
//        binding.flBannerAd.adMobBanner()
//        binding.flNativeAd.adMobNative()
    }

}