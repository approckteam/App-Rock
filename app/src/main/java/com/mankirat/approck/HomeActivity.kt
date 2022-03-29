package com.mankirat.approck

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.mankirat.approck.databinding.ActivityHomeBinding
import com.mankirat.approck.lib.AdMobUtil

class HomeActivity : AppCompatActivity() {

    private val binding by lazy { ActivityHomeBinding.inflate(layoutInflater) }

    private fun log(msg: String, e: Throwable? = null) {
        Log.e("HomeActivity", msg, e)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        AdMobUtil.adMobIds.interstitialId = Constants.AdMob.INTERSTITIAL
        AdMobUtil.adMobIds.interstitialIdSplash = Constants.AdMob.INTERSTITIAL_SPLASH
        AdMobUtil.adMobIds.bannerId = Constants.AdMob.BANNER
        AdMobUtil.adMobIds.nativeId = Constants.AdMob.NATIVE
        AdMobUtil.adMobIds.rewardId = Constants.AdMob.REWARD
        AdMobUtil.adMobIds.appOpenId = Constants.AdMob.APP_OPEN

        AdMobUtil.setUp(this)
        //MobileAds.initialize(context)

        binding.btnShowInterstitial.setOnClickListener {
            AdMobUtil.showInterstitial(this)
        }

    }

}